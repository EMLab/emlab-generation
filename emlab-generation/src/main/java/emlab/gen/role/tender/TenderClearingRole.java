/*******************************************************************************
 * Copyright 2013 the original author or authors.
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
package emlab.gen.role.tender;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.transaction.annotation.Transactional;

import agentspring.role.AbstractRole;
import agentspring.role.Role;
import emlab.gen.domain.agent.Regulator;
import emlab.gen.domain.market.Bid;
import emlab.gen.domain.market.capacity.CapacityClearingPoint;
import emlab.gen.domain.policy.renewablesupport.TenderBid;
import emlab.gen.repository.Reps;

/**
 * @author rjjdejeu
 *
 */
public class TenderClearingRole extends AbstractRole<Regulator> implements Role<Regulator> {

    @Autowired
    Reps reps;

    @Autowired
    Neo4jTemplate template;

    @Override
    @Transactional
    public void act(Regulator regulator) {

        Iterable<TenderBid> sortedListofTenderDispatchPlan = null;

        // Query needs to be made for sortedTenderBidsByPrice in a
        // tenderReposity so I can put that in a list
        // something like this;
        // @Query(value="g.idx('__types__')[[className:'emlab.gen.domain.market.Bid']].filter{it.time == tick}.sort{it.price}._()",
        // type=QueryType.Gremlin)
        // Iterable<Bid> findAllSortedBidsByPrice(@Param("tick") long time);

        sortedListofTenderDispatchPlan = reps.tenderRepository.sortedTenderBidsByPrice(getCurrentTick());
        double demand = 0d;
        double sumOfTenderBidQuantityAccepted = 0d;
        double acceptedSubsidyPrice = 0d;
        boolean isTheTenderCleared = false;

        // This epsilon is to account for rounding errors for java (only
        // relevant for exact clearing)
        double clearingEpsilon = 0.001d;

        // I added this in regulator.java (in domain.agent);
        // public double getRelativeRenewableTarget() {
        // return RelativeRenewableTarget;
        // }
        //
        // public void setRelativeRenewableTarget(double
        // RelativeRenewableTarget) {
        // this.RelativeRenewableTarget = RelativeRenewableTarget;

        if (regulator.getRelativeRenewableTarget() == 0) {
            isTheTenderCleared = true;
            acceptedSubsidyPrice = 0;
        }

        for (TenderBid currentTenderDispatchPlan : sortedListofTenderDispatchPlan) {

            if (currentTenderDispatchPlan.getPrice() <= regulator.getCapacityMarketPriceCap()) {

                demand = regulator.getRelativeRenewableTarget()
                        * (1 - regulator.getReserveDemandLowerMargin())
                        + ((regulator.getCapacityMarketPriceCap() - currentTenderDispatchPlan.getPrice())
                                * (regulator.getReserveDemandUpperMargin() + regulator.getReserveDemandLowerMargin()) * regulator
                                    .getRelativeRenewableTarget()) / regulator.getCapacityMarketPriceCap();

                // logger.warn("Price of this cdp is " + currentCDP.getPrice());
                // logger.warn("Demand at this cdp is " + demand);

                if (isTheTenderCleared == false) {
                    if (demand - (sumOfTenderBidQuantityAccepted + currentTenderDispatchPlan.getAmount()) >= -clearingEpsilon) {
                        acceptedSubsidyPrice = currentTenderDispatchPlan.getPrice();
                        currentTenderDispatchPlan.setStatus(Bid.ACCEPTED);
                        currentTenderDispatchPlan.setAcceptedAmount(currentTenderDispatchPlan.getAmount());
                        sumOfTenderBidQuantityAccepted = sumOfTenderBidQuantityAccepted
                                + currentTenderDispatchPlan.getAmount();
                        // logger.warn("Price of this cdp is " +
                        // currentCDP.getPrice());
                        // logger.warn("accepted price" + acceptedPrice);
                    }

                    else if (demand - (sumOfTenderBidQuantityAccepted + currentTenderDispatchPlan.getAmount()) < clearingEpsilon) {

                        currentTenderDispatchPlan.setStatus(Bid.PARTLY_ACCEPTED);
                        currentTenderDispatchPlan.setAcceptedAmount((sumOfTenderBidQuantityAccepted - demand));
                        acceptedSubsidyPrice = currentTenderDispatchPlan.getPrice();
                        sumOfTenderBidQuantityAccepted = sumOfTenderBidQuantityAccepted
                                + currentTenderDispatchPlan.getAcceptedAmount();
                        isTheTenderCleared = true;

                        // logger.warn("accepted price" + acceptedPrice);

                    }

                    // else if (demand - sumofSupplyBidsAccepted <
                    // clearingEpsilon) {
                    // isTheMarketCleared = true;
                    // }
                } else {
                    currentTenderDispatchPlan.setStatus(Bid.FAILED);
                    currentTenderDispatchPlan.setAcceptedAmount(0);
                }

                // logger.warn("Cumulatively Accepted Supply " +
                // sumofSupplyBidsAccepted);
                currentTenderDispatchPlan.persist();

            }

            // logger.warn("Current CDP Price " + currentCDP.getPrice());
            // logger.warn("Cumulatively accepted volume " +
            // sumofSupplyBidsAccepted);
        }
        // logger.warn("Demand for the capacity market at tick {} is " + demand,
        // getCurrentTick());

        CapacityClearingPoint clearingPoint = new CapacityClearingPoint();
        if (isTheTenderCleared == true) {
            // sumofSupplyBidsAccepted = demand;
            logger.warn("MARKET CLEARED at price" + acceptedSubsidyPrice);
            clearingPoint.setPrice(acceptedSubsidyPrice);
            clearingPoint.setVolume(sumOfTenderBidQuantityAccepted);
            clearingPoint.setTime(getCurrentTick());
            clearingPoint.setCapacityMarket(market);
            clearingPoint.persist();

            logger.warn("Clearing point Price {} and volume " + clearingPoint.getVolume(), clearingPoint.getPrice());

        } else {
            acceptedSubsidyPrice = regulator.getCapacityMarketPriceCap()
                    * (1 + ((regulator.getRelativeRenewableTarget() * (1 - regulator.getReserveDemandLowerMargin()) - sumOfTenderBidQuantityAccepted) / ((regulator
                            .getReserveDemandUpperMargin() + regulator.getReserveDemandLowerMargin()) * regulator
                            .getRelativeRenewableTarget())));
            clearingPoint.setPrice(max(regulator.getCapacityMarketPriceCap(), acceptedSubsidyPrice));
            clearingPoint.setVolume(sumOfTenderBidQuantityAccepted);
            clearingPoint.setTime(getCurrentTick());
            clearingPoint.setCapacityMarket(market);
            clearingPoint.persist();
            logger.warn("MARKET UNCLEARED at price" + clearingPoint.getPrice());
            logger.warn("Clearing point Price {} and volume " + clearingPoint.getVolume(), clearingPoint.getPrice());

        }
        // clearingPoint.persist();
        // logger.warn("is the market cleared? " + isTheMarketCleared);
        // logger.warn("Clearing point Price" + clearingPoint.getPrice());
        // logger.warn("Clearing Point Volume" + clearingPoint.getVolume());

        // VERIFICATION
        double q2 = clearingPoint.getVolume();
        double q1 = regulator.getRelativeRenewableTarget()
                * (1 - regulator.getReserveDemandLowerMargin())
                + ((regulator.getCapacityMarketPriceCap() - clearingPoint.getPrice())
                        * (regulator.getReserveDemandUpperMargin() + regulator.getReserveDemandLowerMargin()) * regulator
                            .getRelativeRenewableTarget()) / regulator.getCapacityMarketPriceCap();
        if (q1 == q2) {
            logger.warn("matches");
        } else {
            logger.warn("does not match");
        }

    }

    /**
     * @param capacityMarketPriceCap
     * @param acceptedPrice
     * @return
     */
    private double max(double capacityMarketPriceCap, double acceptedPrice) {
        if (acceptedPrice >= capacityMarketPriceCap)
            return capacityMarketPriceCap;
        else
            return acceptedPrice;
    }

}
