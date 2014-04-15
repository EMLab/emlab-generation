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
package emlab.gen.role.market;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import agentspring.role.AbstractRole;
import agentspring.role.Role;
import agentspring.role.RoleComponent;
import emlab.gen.domain.agent.EnergyConsumer;
import emlab.gen.domain.agent.EnergyProducer;
import emlab.gen.domain.contract.Contract;
import emlab.gen.domain.contract.LongTermContract;
import emlab.gen.domain.contract.LongTermContractOffer;
import emlab.gen.domain.contract.LongTermContractType;
import emlab.gen.domain.gis.Zone;
import emlab.gen.domain.market.Bid;
import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.domain.market.electricity.Segment;
import emlab.gen.domain.market.electricity.SegmentClearingPoint;
import emlab.gen.domain.technology.PowerPlant;
import emlab.gen.repository.Reps;
import emlab.gen.util.MapValueComparator;

/**
 * {@link EnergyProducer} submits offers to the {@link ElectricitySpotMarket}.
 * One {@link Bid} per {@link PowerPlant}.
 * 
 * @author <a href="mailto:A.Chmieliauskas@tudelft.nl">Alfredas
 *         Chmieliauskas</a> @author <a
 *         href="mailto:E.J.L.Chappin@tudelft.nl">Emile Chappin</a>
 * 
 */
@RoleComponent
public class SelectLongTermElectricityContractsRole extends
AbstractRole<EnergyConsumer> implements Role<EnergyConsumer> {

    @Autowired
    Reps reps;

    HashMap<Zone, ExistingContractsInformation> existingContractsInformations;

    @Override
    @Transactional
    public void act(EnergyConsumer consumer) {

        logger.info("{} will now accept offers or not", consumer);
        existingContractsInformations = new HashMap<Zone, ExistingContractsInformation>();

        // Make an overview of the capacity of existing contracts per ltc type.
        // Store that in a nested info class.
        for (Zone zone : reps.genericRepository.findAll(Zone.class)) {
            ExistingContractsInformation info = new ExistingContractsInformation(
                    zone, consumer);
            existingContractsInformations.put(zone, info);
            info.updateExisingContractsInformation();
        }

        // Rank the offers, by multiplying by better factors,
        // TODO based on past performance??
        Map<LongTermContractOffer, Double> offersUnsorted = new HashMap<LongTermContractOffer, Double>();

        // Adjust the price based on duration of the offer
        for (LongTermContractOffer offer : findLongTermContractOffersActiveAtTime()) {
            double duration = offer.getDuration().getDuration();
            double thisPrice = offer.getPrice() * (1 + ((duration-1)*consumer.getContractDurationPreferenceFactor()));
            offersUnsorted.put(offer, thisPrice);
        }

        @SuppressWarnings("unused")
        MapValueComparator comp = new MapValueComparator(offersUnsorted);
        Map<LongTermContractOffer, Double> offersRanked = new TreeMap<LongTermContractOffer, Double>(
                comp);
        offersRanked.putAll(offersUnsorted);

        for (LongTermContractOffer offer : offersRanked.keySet()) {

            // is this offer still valid (i.e. no other conflicting offer been
            // accepted?)
            boolean stillvalid = true;

            // if a similar offer has been accepted (same power plant)
            // we have to ignore this offer
            if (reps.contractRepository
                    .findLongTermContractForPowerPlantActiveAtTime(
                            offer.getUnderlyingPowerPlant(), getCurrentTick()) != null) {
                stillvalid = false;
            } else {
                // check whether there is load to meet this type.
                double volumeInContractTypePossible = consumer
                        .getLtcMaximumCoverageFraction()
                        * determineVolumeForContractType(
                                offer.getLongTermContractType(),
                                offer.getZone());
                // check what the capacity is of existing contracts for this ltc
                // type.
                double volumeInCurrentContracts = existingContractsInformations
                        .get(offer.getZone()).capacityContractedInZonesForLTCType
                        .get(offer.getLongTermContractType());
                if (volumeInCurrentContracts + offer.getCapacity() > volumeInContractTypePossible) {
                    logger.info("Contract impossible for {}", offer
                            .getLongTermContractType().getName());
                    stillvalid = false;
                } else {
                    logger.info("Contract possible for {}", offer
                            .getLongTermContractType().getName());
                }
                logger.info(
                        "Volume in current contracts: {}, while possible for load: {}",
                        volumeInCurrentContracts, volumeInContractTypePossible);
            }

            if (stillvalid) {
                // It is possible, but do we want it?
                // determine the weighted average spot price for this contract
                // if it is then adjusted price, make a contract.
                double hours = 0d;
                double weightedElectricitySpotPrices = 0d;
                for (Segment s : offer.getLongTermContractType().getSegments()) {
                    hours += s.getLengthInHours();

                    SegmentClearingPoint point = (SegmentClearingPoint) reps.clearingPointRepositoryOld
                            .findClearingPointForSegmentAndTime(s,
 getCurrentTick() - 1, false);
                    weightedElectricitySpotPrices += point.getPrice()
                            * s.getLengthInHours();
                    logger.info("Found a clearing point {} for segment {}",
                            point, s);
                }

                double averageElectricityPrice = weightedElectricitySpotPrices
                        / hours;

                double price = offersUnsorted.get(offer);
                if (price < (averageElectricityPrice*consumer.getContractWillingnessToPayFactor())) {
                    reps.contractRepository.submitLongTermContractForElectricity(
                            offer.getUnderlyingPowerPlant(), offer
                            .getUnderlyingPowerPlant().getOwner(),
                            consumer, offer.getUnderlyingPowerPlant()
                            .getLocation().getZone(), offer.getPrice(),
                            offer.getUnderlyingPowerPlant()
                            .getAvailableCapacity(getCurrentTick()),
                            offer.getLongTermContractType(), getCurrentTick(),
                            offer.getDuration(), true, offer.getMainFuel(),
                            offer.getFuelPassThroughFactor(), offer
                            .getCo2PassThroughFactor(), offer
                            .getFuelPriceStart(), offer
                            .getCo2PriceStart());
                    logger.info(
                            "Accepted LTC offer type {}, duration {}, submitted contract",
                            offer.getLongTermContractType(),
                            offer.getDuration());
                    logger.warn(
                            "Accepted LTC for powerplant {}, price {} euro/MWh",
                            offer.getUnderlyingPowerPlant(), offer.getPrice());

                    // Update the info on existing contracts for this zone
                    existingContractsInformations.get(
                            offer.getUnderlyingPowerPlant().getLocation()
                            .getZone())
                            .updateExisingContractsInformation();

                }

            }
        }
    }

    public double determineVolumeForContractType(LongTermContractType type,
            Zone zone) {

        // calculate minimum load of the segments in this contract type
        double minimumLoadInSegmentsOfContractType = Double.MAX_VALUE;
        for (Segment segment : type.getSegments()) {
            double loadOfSegment = reps.marketRepository
                    .findSegmentLoadForElectricitySpotMarketForZone(zone,
                            segment).getBaseLoad()
                            * reps.marketRepository.findElectricitySpotMarketForZone(zone)
                            .getDemandGrowthTrend().getValue(getCurrentTick());
            if (loadOfSegment < minimumLoadInSegmentsOfContractType) {
                minimumLoadInSegmentsOfContractType = loadOfSegment;
            }
        }
        logger.info(
                "For ltc type {}, the lowest load of the segments covered is {}",
                type, minimumLoadInSegmentsOfContractType);
        return minimumLoadInSegmentsOfContractType;
    }

    public Iterable<LongTermContractOffer> findLongTermContractOffersActiveAtTime() {

        List<LongTermContractOffer> list = new ArrayList<LongTermContractOffer>();
        for (LongTermContractOffer ltcOffer : reps.genericRepository
                .findAll(LongTermContractOffer.class)) {
            // If active
            if (ltcOffer.getStart() == getCurrentTick()) {
                list.add(ltcOffer);
            }
        }
        return list;
    }

    /**
     * Contains the existing contracts information for a consumer for a zone.
     * 
     * @author ejlchappin
     * 
     */
    private class ExistingContractsInformation {
        private final Zone zone;
        private final EnergyConsumer consumer;
        private final HashMap<LongTermContractType, Double> capacityContractedInZonesForLTCType;

        public ExistingContractsInformation(Zone zone, EnergyConsumer consumer) {
            this.zone = zone;
            this.consumer = consumer;
            capacityContractedInZonesForLTCType = new HashMap<LongTermContractType, Double>();
        }

        public void updateExisingContractsInformation() {

            for (LongTermContractType type : reps.genericRepository
                    .findAll(LongTermContractType.class)) {
                capacityContractedInZonesForLTCType
                .put(type,
                        findCapacityOfLongTermContractsForEnergyConsumerAlreadyActiveAtTimeForSegmentsForZone(
                                consumer, type));
            }
        }

        private double findCapacityOfLongTermContractsForEnergyConsumerAlreadyActiveAtTimeForSegmentsForZone(
                EnergyConsumer consumer, LongTermContractType type) {

            double maxCapacity = 0d;
            for (Segment segment : type.getSegments()) {
                double thisCapacity = 0d;
                for (Contract c : reps.contractRepository
                        .findLongTermContractsForEnergyConsumerForSegmentForZoneActiveAtTime(
                                consumer, segment, zone, getCurrentTick())) {
                    LongTermContract ltc = (LongTermContract) c;
                    thisCapacity += ltc.getCapacity();
                    logger.info(
                            "Found existing contract {} active in segment {}",
                            ltc, segment);
                }
                // More contracts for this segment? Keep track of the largest
                // contracted capacity of each of the valid segments
                if (thisCapacity > maxCapacity) {
                    maxCapacity = thisCapacity;
                }
            }
            return maxCapacity;
        }
    }
}
