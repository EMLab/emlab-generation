/*******************************************************************************
 * Copyright 2012 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package emlab.role.market;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.transaction.annotation.Transactional;

import agentspring.role.AbstractRole;
import emlab.domain.market.Bid;
import emlab.domain.market.ClearingPoint;
import emlab.domain.market.DecarbonizationMarket;
import emlab.domain.market.electricity.ElectricitySpotMarket;
import emlab.repository.Reps;

/**
 * Calculates {@link ClearingPoint} for any {@link Market}.
 * 
 * @author <a href="mailto:E.J.L.Chappin@tudelft.nl">Emile Chappin</a>
 */
public abstract class AbstractMarketRole<T extends DecarbonizationMarket> extends AbstractRole<T> {

    @Autowired
    Neo4jTemplate template;

    @Autowired
    Reps reps;

    @Transactional
    public ClearingPoint calculateClearingPoint(DecarbonizationMarket market, long time) {
        double clearedVolume = 0d;
        double clearedPrice = 0d;
        double totalSupplyPrice = calculateTotalSupplyPriceForMarketForTime(market, time);
        double totalSupply = calculateTotalSupplyForMarketForTime(market, time);
        logger.info("total supply {} total price {}", totalSupply, totalSupplyPrice);
        double totalDemandForPrice = calculateTotalDemandForMarketForTimeForPrice(market, time, totalSupplyPrice);
        logger.info("total demand {} for price {}", totalDemandForPrice, totalSupplyPrice);

        // Not enough to meet demand
        if (totalDemandForPrice > totalSupply) {
            clearedVolume = totalSupply;
            if (market.isAuction()) {
                clearedPrice = calculateTotalDemandForMarketForTimeForPrice(market, time, 0d);
            } else {
                clearedPrice = market instanceof ElectricitySpotMarket ? ((ElectricitySpotMarket) market).getValueOfLostLoad()
                        : totalSupplyPrice;
            }
        } else { // Supply exceeds demand
            double totalOfferAmount = 0d;
            double previousPrice = 0d;
            for (Bid offer : reps.bidRepository.findOffersForMarketForTime(market, time)) {
                double price = offer.getPrice();
                double amount = offer.getAmount();
                double demand = calculateTotalDemandForMarketForTimeForPrice(market, time, price);
                if (demand < totalOfferAmount + amount) {
                    if (demand == 0) {
                        if (getCurrentTick() > 0) {
                            ClearingPoint cp = reps.clearingPointRepository.findClearingPointForMarketAndTime(market, getCurrentTick() - 1);
                            if (cp != null)
                                previousPrice = cp.getPrice();
                        }
                        clearedPrice = previousPrice;
                        clearedVolume = totalOfferAmount;
                    } else if (totalOfferAmount >= demand) {
                        clearedPrice = previousPrice;
                        clearedVolume = totalOfferAmount;
                    } else {
                        clearedPrice = price;
                        clearedVolume = demand;
                    }
                    break;
                }
                totalOfferAmount += amount;
                previousPrice = price;
            }
        }
        ClearingPoint point = new ClearingPoint().persist();
        point.setAbstractMarket(market);
        point.setTime(time);
        point.setPrice(Math.max(0, clearedPrice));
        point.setVolume(clearedVolume);

        // set bids to accepted and check for partial acceptance
        // DEMAND
        double previousPrice = markAcceptedBids(point, false);
        // if auction - last accepted demand bid sets the price
        if (market.isAuction()) {
            point.setPrice(Math.max(0, previousPrice));
        }
        // SUPPLY
        markAcceptedBids(point, true);
        return point;
    }

    private double markAcceptedBids(ClearingPoint point, boolean isSupply) {
        long time = point.getTime();
        DecarbonizationMarket market = point.getAbstractMarket();
        double clearedPrice = point.getPrice();
        double clearedVolume = point.getVolume();
        double totalBidVolume = 0d;
        double previousPrice = Double.NEGATIVE_INFINITY;
        double accpetedSamePriceVolume = 0d;

        Iterable<Bid> bids = isSupply ? reps.bidRepository.findOffersForMarketForTimeBelowPrice(market, time, clearedPrice) : market
                .isAuction() ? reps.bidRepository.findDemandBidsForMarketForTime(market, time) : reps.bidRepository
                .findDemandBidsForMarketForTimeAbovePrice(market, time, clearedPrice);

        for (Bid bid : bids) {
            double amount = bid.getAmount();
            totalBidVolume += amount;
            accpetedSamePriceVolume = bid.getPrice() == previousPrice ? accpetedSamePriceVolume + amount : amount;
            if (totalBidVolume < clearedVolume) {
                bid.setStatus(Bid.ACCEPTED);
                bid.setAcceptedAmount(bid.getAmount());
            } else {
                double lastAvailableBidSize = clearedVolume - (totalBidVolume - accpetedSamePriceVolume);
                double samePriceVolume = calculateBidsForMarketForTimeForPrice(market, time, bid.getPrice(), isSupply);
                double adjustRatio = lastAvailableBidSize / samePriceVolume;
                for (Bid partBid : isSupply ? reps.bidRepository.findOffersForMarketForTimeForPrice(market, time, bid.getPrice())
                        : reps.bidRepository.findDemandBidsForMarketForTimeForPrice(market, time, bid.getPrice())) {
                    partBid.setStatus(Bid.PARTLY_ACCEPTED);
                    partBid.setAcceptedAmount(partBid.getAmount() * adjustRatio);
                }
                break;
            }
            previousPrice = bid.getPrice();
        }
        return previousPrice;
    }

    private double calculateBidsForMarketForTimeForPrice(DecarbonizationMarket market, long time, double price, boolean isSupply) {
        try {
            return isSupply ? reps.bidRepository.calculateOffersForMarketForTimeForPrice(market, time, price) : reps.bidRepository
                    .calculateDemandBidsForMarketForTimeForPrice(market, time, price);
        } catch (NullPointerException npe) {
        }
        return 0d;
    }

    private double calculateTotalDemandForMarketForTimeForPrice(DecarbonizationMarket market, long time, double price) {
        try {
            return market.isAuction() ? reps.bidRepository.calculateTotalDemandForMarketForTime(market, time) : reps.bidRepository
                    .calculateTotalDemandForMarketForTimeForPrice(market, time, price);
        } catch (NullPointerException npe) {
        }
        return 0d;
    }

    private double calculateTotalSupplyPriceForMarketForTime(DecarbonizationMarket market, long time) {
        try {
            return reps.bidRepository.calculateTotalSupplyPriceForMarketForTime(market, time);
        } catch (NullPointerException e) {
        }
        return 0d;
    }

    private double calculateTotalSupplyForMarketForTime(DecarbonizationMarket market, long time) {
        try {
            return reps.bidRepository.calculateTotalSupplyForMarketForTime(market, time);
        } catch (NullPointerException e) {
        }
        return 0d;
    }

    public abstract Reps getReps();

    // -------------------------------------------------------------------------------------------------

    // /**
    // * Old clearing algorithm. Not deleted yet for compatability reasons. WARNS if it is used.
    // *
    // * @param market
    // * @param supplyBidsIterable
    // * @param demandBidsIterable
    // * @param time
    // * @return
    // */
    // public ClearingPoint calculateClearingPoint(DecarbonizationMarket market, Iterable<Bid> supplyBidsIterable,
    // Iterable<Bid> demandBidsIterable, long time) {
    //
    // logger.warn("{} still used the old clearing algorithm!", market);
    //
    // List<Bid> supplyBids = Utils.asList(supplyBidsIterable);
    // List<Bid> demandBids = Utils.asList(demandBidsIterable);
    //
    // logger.info("Number of supply bids: " + supplyBids.size() + " and demand: " + demandBids.size());
    //
    // if (supplyBids.size() == 0 || demandBids.size() == 0) {
    // logger.info("Either no supply bids or no demand bids - supply: {}; demand: {}", +supplyBids.size(), demandBids.size());
    // return null;
    // } else {
    // logger.info("{} supply bids and {} demand bids present on " + market, supplyBids.size(), demandBids.size());
    // }
    //
    // double totalSupply = 0d;
    // for (Bid bid : supplyBids) {
    // totalSupply += bid.getAmount();
    // }
    // double totalDemand = 0d;
    // for (Bid bid : demandBids) {
    // totalDemand += bid.getAmount();
    // }
    // logger.info("Total supply: {} -- total demand: {}", totalSupply, totalDemand);
    //
    // Collections.sort(supplyBids, new BidPriceComparator());
    // Collections.sort(demandBids, new BidPriceReverseComparator());
    // logger.info("Bids sorted on price");
    //
    // // TODO check whether there are negative amounts bid. Negative prices
    // // may be ok, possibly warn. Negative amounts are not allowed.
    //
    // boolean settled = false;
    // double price = 0d;
    // double amount = 0d;
    //
    // double totalSupplyAccepted = 0d;
    // double totalDemandAccepted = 0d;
    // double lastSupplyPrice = 0d;
    // double lastDemandPrice = 0d;
    //
    // int bidDemandIndex = 0;
    // int bidSupplyIndex = 0;
    //
    // boolean done = false;
    //
    // // SUPPLY LOOP
    // while (!done) {
    // boolean supplyBidMet = false;
    //
    // if (supplyBids.size() <= bidSupplyIndex) {
    // logger.info("No more supply bids");
    // // no more supply bids.
    // done = true;
    // settled = true;
    //
    // amount = totalSupplyAccepted;
    // if (market.isAuction()) {
    // price = lastDemandPrice;
    // } else {
    // price = lastSupplyPrice;
    // }
    //
    // logger.info("Accepted the last demand bid partly as a final bid");
    //
    // // Supply bid is accepted
    // double partialAcceptance = totalSupplyAccepted - totalDemandAccepted;
    //
    // calculateAndDetermineSharedAcceptance(demandBids, demandBids.get(bidDemandIndex), partialAcceptance);
    //
    // totalDemandAccepted += partialAcceptance;
    //
    // logger.info("Cleared: partial demand bid, no more supply bids.");
    //
    // } else {
    // double supplyAmount = supplyBids.get(bidSupplyIndex).getAmount();
    // double supplyPrice = supplyBids.get(bidSupplyIndex).getPrice();
    //
    // // DEMAND LOOP
    // while (!supplyBidMet && !done) {
    //
    // if (demandBids.size() <= bidDemandIndex) {
    // // no more demand bids;
    // logger.info("No more demand bids, settle with accepted demand so far. Maybe a partial supply bid.");
    // done = true;
    //
    // if ((totalDemandAccepted) > (totalSupplyAccepted)) {
    //
    // settled = true;
    // logger.info("Accepted a partial supply bid as a final bid");
    // logger.info("Accepted a demand bid as a final bid");
    //
    // demandBids.get(bidDemandIndex - 1).updateStatus(Bid.ACCEPTED);
    //
    // double partialAcceptance = totalDemandAccepted - totalSupplyAccepted;
    //
    // calculateAndDetermineSharedAcceptance(supplyBids, supplyBids.get(bidSupplyIndex), partialAcceptance);
    //
    // logger.info("Supply bid part accepted is " + partialAcceptance);
    // totalSupplyAccepted += partialAcceptance;
    // price = supplyPrice;
    // amount = totalSupplyAccepted;
    // logger.info("Done case 2: partial supply bid.");
    // }
    //
    // } else {
    // double demandAmount = demandBids.get(bidDemandIndex).getAmount();
    // double demandPrice = demandBids.get(bidDemandIndex).getPrice();
    // lastDemandPrice = demandPrice;
    //
    // // logger.info("Demand price: " + demandPrice);
    //
    // // Should this demand bid be accepted? If true, the
    // // demand bid is still above the supply bid on the bid
    // // ladder.
    //
    // if (supplyPrice <= demandPrice) {
    //
    // // Is this demand bid is smaller in amount than the
    // // supply bid including the current one?
    // if ((totalDemandAccepted + demandAmount) < (totalSupplyAccepted + supplyAmount)) {
    //
    // logger.info("Accepted a demand bid");
    // // Demand bid is accepted
    // demandBids.get(bidDemandIndex).updateStatus(Bid.ACCEPTED);
    // // lastDemandPrice = demandPrice;
    // totalDemandAccepted += demandAmount;
    // bidDemandIndex++;
    //
    // } else if ((totalDemandAccepted + demandAmount) == (totalSupplyAccepted + supplyAmount)) {
    //
    // logger.info("Accepted both a demand and a supply bid");
    // // Demand and supply bids are accepted
    // logger.info("Bid repository {}", getReps().bidRepository);
    // demandBids.get(bidDemandIndex).updateStatus(Bid.ACCEPTED);
    // supplyBids.get(bidSupplyIndex).updateStatus(Bid.ACCEPTED);
    // // lastDemandPrice = demandPrice;
    // lastSupplyPrice = supplyPrice;
    // totalDemandAccepted += demandAmount;
    // totalSupplyAccepted += supplyAmount;
    // bidDemandIndex++;
    // bidSupplyIndex++;
    //
    // } else {
    //
    // // this demand bid is larger then the supply
    // // bid, we should check the next supply bid and
    // // leave the demand bid as it is
    // logger.info("Accepted a supply bid");
    // // Supply bid is accepted
    // supplyBids.get(bidSupplyIndex).updateStatus(Bid.ACCEPTED);
    // supplyBidMet = true; // We go out the demand
    // // loop
    // bidSupplyIndex++;
    // lastSupplyPrice = supplyPrice;
    // totalSupplyAccepted += supplyAmount;
    // }
    // } else {
    // logger.info("Demand curve now below supply curve, so the clearing has been passed");
    // done = true;
    //
    // if (totalDemandAccepted == totalSupplyAccepted) {
    // // We have not settled yet!
    // logger.info("We are in balance. Settle later.");
    // } else {
    // if ((totalDemandAccepted + demandAmount) > (totalSupplyAccepted)) {
    //
    // settled = true;
    // // this demand bid is larger then the supply
    // // bid, we should check the next supply bid
    // // and leave the demand bid as it is
    // logger.info("Accepted a partial demand bid as a final bid");
    // // Supply bid is accepted
    //
    // supplyBids.get(bidSupplyIndex).updateStatus(Bid.FAILED);
    // // lastDemandPrice = demandPrice;
    // double partialAcceptance = totalSupplyAccepted - totalDemandAccepted;
    //
    // calculateAndDetermineSharedAcceptance(demandBids, demandBids.get(bidDemandIndex), partialAcceptance);
    //
    // logger.info("Demand bid part accepted is " + partialAcceptance);
    // totalDemandAccepted += partialAcceptance;
    // price = demandPrice;
    // amount = totalDemandAccepted;
    // logger.info("Done case 3: partial demand bid.");
    // }
    // }
    //
    // }
    //
    // }
    // if (done && !settled) {
    //
    // // supply is now larger then demand. We're done
    // // somewhere in
    // // this area. With, without this bid, or something.
    //
    // // Found a price and a demand
    // // logger.info("Done but not yet settled");
    // // logger.info("Total supply: " + totalSupplyAccepted);
    // // logger.info("Total demand: " + totalDemandAccepted);
    //
    // // Case 1. We're exactly on the crossing
    // if (totalDemandAccepted == totalSupplyAccepted) {
    // logger.info("Done case 4: even bid.");
    // amount = totalSupplyAccepted;
    // price = lastSupplyPrice;
    // supplyBidMet = true;
    // }
    // }
    // }
    //
    // }
    // }
    // logger.info("Cleared with amount: " + amount + " and price: " + price);
    //
    // ClearingPoint clearingPoint = getReps().clearingPointRepositoryOld.createOrUpdateClearingPoint(market, price, amount, time);
    //
    // // Setting all bids that are still Submitted to Failed
    // for (Bid bid : demandBids) {
    // if (bid.getStatus() == Bid.SUBMITTED) {
    // bid.updateStatus(Bid.FAILED);
    // }
    // }
    // for (Bid bid : supplyBids) {
    // if (bid.getStatus() == Bid.SUBMITTED) {
    // bid.updateStatus(Bid.FAILED);
    // }
    // }
    // logger.info("Set other bids to failed");
    // return clearingPoint;
    // }
    //
    // private void calculateAndDetermineSharedAcceptance(List<Bid> bids, Bid bid, double partialAcceptance) {
    // // check whether there are more demand bids that match the same
    // // price level and make them all accept part
    // ArrayList<Bid> bidsThatAreAllToBePartial = getBidsWithSamePrice(bids, bid);
    // double ratio = partialAcceptance / getTotalVolumeOfBids(bidsThatAreAllToBePartial);
    // for (Bid _bid : bidsThatAreAllToBePartial) {
    // _bid.updateStatus(Bid.PARTLY_ACCEPTED);
    // overrideBidAmount(_bid, ratio);
    // }
    //
    // logger.info("For " + bidsThatAreAllToBePartial.size() + " demand bid(s) they are partly accepted, total " + partialAcceptance
    // + ", ratio: " + ratio);
    //
    // }
    //
    // private void overrideBidAmount(Bid bid, double ratio) {
    // bid.updateAmount(bid.getAmount() * ratio);
    // }
    //
    // private ArrayList<Bid> getBidsWithSamePrice(List<Bid> bids, Bid bidToMatch) {
    // logger.info("Finding bids in a list with the same price as some bid");
    // ArrayList<Bid> bidsThatMatch = new ArrayList<Bid>();
    //
    // for (Bid bid : bids) {
    // if (bid.getPrice() == bidToMatch.getPrice()) {
    // bidsThatMatch.add(bid);
    // }
    // }
    // return bidsThatMatch;
    // }
    //
    // private double getTotalVolumeOfBids(List<Bid> bids) {
    // logger.info("Calculating volumes of a number of bids");
    // double volume = 0d;
    // for (Bid bid : bids) {
    // volume += bid.getAmount();
    // }
    // return volume;
    // }
    //
    // //
    // // public SegmentClearingPoint
    // // calculateSegmentClearingPoint(DecarbonizationMarket market,
    // // Iterable<ElectricitySpotBid> supplyBidsIterable,
    // // Iterable<ElectricitySpotBid> demandBidsIterable, long time, Segment
    // // segment) {
    // //
    // // List<Bid> demandBids = Utils.asDownCastedList(demandBidsIterable);
    // // List<Bid> supplyBids = Utils.asDownCastedList(supplyBidsIterable);
    // //
    // // ClearingPoint clearingPoint = calculateClearingPoint(market, supplyBids,
    // // demandBids, time);
    // //
    // // if (clearingPoint != null) {
    // // SegmentClearingPoint segmentClearingPoint =
    // // getClearingPointRepository().createOrUpdateSegmentClearingPoint(segment,
    // // clearingPoint.getAbstractMarket(), clearingPoint.getPrice(),
    // // clearingPoint.getVolume(), clearingPoint.getTime(),
    // // clearingPoint.getIteration());
    // //
    // // return segmentClearingPoint;
    // // } else {
    // // return null;
    // // }
    // //
    // // }

}
