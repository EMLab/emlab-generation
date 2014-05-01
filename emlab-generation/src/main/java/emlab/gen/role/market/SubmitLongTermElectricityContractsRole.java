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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import agentspring.role.Role;
import agentspring.role.RoleComponent;
import emlab.gen.domain.agent.EnergyProducer;
import emlab.gen.domain.contract.LongTermContractDuration;
import emlab.gen.domain.contract.LongTermContractOffer;
import emlab.gen.domain.contract.LongTermContractType;
import emlab.gen.domain.gis.Zone;
import emlab.gen.domain.market.Bid;
import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.domain.market.electricity.Segment;
import emlab.gen.domain.market.electricity.SegmentClearingPoint;
import emlab.gen.domain.technology.PowerPlant;
import emlab.gen.domain.technology.Substance;
import emlab.gen.repository.Reps;
import emlab.gen.role.AbstractEnergyProducerRole;

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
public class SubmitLongTermElectricityContractsRole extends
AbstractEnergyProducerRole implements Role<EnergyProducer> {

    @Autowired
    Reps reps;

    @Override
    @Transactional
    public void act(EnergyProducer producer) {

        // TODO Contracts for checking assigned to power plants??
        // When dismantling, take over existing contract by new power plant?
        for (PowerPlant plant : reps.powerPlantRepository
                .findOperationalPowerPlantsByOwner(producer, getCurrentTick())) {

            // if it is not in the first tick,
            // and if the plant is within the technical lifetime
            // and if it is applicable for LTC's (i.e. not wind)
            // and if there is no contract yet
            if (getCurrentTick() > 0
                    && plant.isWithinTechnicalLifetime(getCurrentTick())
                    && plant.getTechnology().isApplicableForLongTermContract()
                    && reps.contractRepository
                    .findLongTermContractForPowerPlantActiveAtTime(
                            plant, getCurrentTick()) == null) {

                Zone zone = plant.getLocation().getZone();
                double capacity = plant.getAvailableCapacity(getCurrentTick());
                double fuelPriceStart = 0d;

                Substance mainFuel = null;
                if (!plant.getFuelMix().isEmpty()) {
                    mainFuel = plant.getFuelMix().iterator().next()
                            .getSubstance();
                    fuelPriceStart = findLastKnownPriceForSubstance(mainFuel, getCurrentTick());
                }

                double co2PriceStart = findLastKnownCO2Price(getCurrentTick());

                for (LongTermContractType type : reps.genericRepository
                        .findAll(LongTermContractType.class)) {

                    double hours = 0d;
                    for (Segment s : type.getSegments()) {
                        hours += s.getLengthInHours();
                    }

                    double averageElectricityPrice = determineAverageElectricityPrice(
                            hours, (int) producer.getLongTermContractPastTimeHorizon(), type);

                    // Count the hours in this contract type.

                    logger.info("LTC type: {}, number of hours: {}", type,
                            hours);
                    logger.info("Weighted average e-price: {}",
                            averageElectricityPrice);

                    double passThroughCO2 = determineCO2Cost(plant)
                            / determineTotalAverageCost(plant, hours);
                    double passThroughFuel = determineFuelCost(plant)
                            / determineTotalAverageCost(plant, hours);

                    long minimumDuration = Long.MAX_VALUE;
                    long maximumDuration = Long.MIN_VALUE;
                    for (LongTermContractDuration duration : reps.genericRepository
                            .findAll(LongTermContractDuration.class)) {
                        if (duration.getDuration() < minimumDuration) {
                            minimumDuration = duration.getDuration();
                        }
                        if (duration.getDuration() > maximumDuration) {
                            maximumDuration = duration.getDuration();
                        }
                    }
                    logger.info("Minimum duration: {} and maximum: {}",
                            minimumDuration, maximumDuration);

                    for (LongTermContractDuration duration : reps.genericRepository
                            .findAll(LongTermContractDuration.class)) {

                        double minimumPrice = (1 + producer
                                .getLongTermContractMargin())
                                * determineTotalAverageCost(plant, hours);
                        double maximumPrice = averageElectricityPrice;
                        // TODO use risk double riskAversionFactor = 1;

                        // Check whether the maximum price is at
                        // least the minimum contract price. Otherwise,
                        // we'll use the minimum price.
                        double thisPrice = 0d;
                        if (maximumPrice < minimumPrice) {
                            thisPrice = minimumPrice;
                        } else {
                            thisPrice = minimumPrice
                                    + (((maximumDuration - duration
                                            .getDuration()) * (maximumPrice - minimumPrice)) / (maximumDuration - minimumDuration));
                        }
                        // Skip the first tick for now. Otherwise the
                        // contracts in the
                        LongTermContractOffer offer = reps.contractRepository
                                .submitLongTermContractOfferForElectricity(
                                        producer, plant, zone, thisPrice,
                                        capacity, type, getCurrentTick(),
                                        duration, mainFuel, passThroughFuel,
                                        passThroughCO2, fuelPriceStart,
                                        co2PriceStart);
                        logger.info(
                                "Submitted offer for type {}. The offer: {}",
                                type, offer);
                    }
                }
            }
        }
    }

    /**
     * Determines the average electricity price weighted by hours of segments of a previous number of years.
     * @param hours the total hours in these segments
     * @param type the contract type
     * @param the horizon to look back
     * @return the average price
     */
    private double determineAverageElectricityPrice(double hours, int horizon,
            LongTermContractType type) {

        // Keep track of the number of prices found and the averages
        int nrOfPrices = 0;
        double averageElectricityPrice = 0d;

        // For each of the previous years
        for (int i = -horizon; i <= -1; i++) {

            // Still valid
            boolean valid = true;
            double weightedElectricitySpotPrices = 0d;

            // For each of the segments in this type
            for (Segment s : type.getSegments()) {

                // Try to find a price for this year
                SegmentClearingPoint point = (SegmentClearingPoint) reps.clearingPointRepositoryOld
                        .findClearingPointForSegmentAndTime(s,
 getCurrentTick() + i, false);

                // If there is a price, add it multiplied to the number of hours to the total.
                if(point != null){
                    weightedElectricitySpotPrices += point.getPrice()
                            * s.getLengthInHours();
                } else {
                    // Otherwise, no valid price is found for this year
                    valid = false;
                }
            }

            // If valid prices were found, we can use it.
            if(valid){
                nrOfPrices++;
                averageElectricityPrice += weightedElectricitySpotPrices / hours;
            }
        }

        // Return the average
        return averageElectricityPrice/nrOfPrices;
    }

    /**
     * Determines the total average cost for a power plant, based on fixed and
     * variable cost, averaged out over a number of running hours.
     * 
     * @param plant
     * @param hours
     * @return
     */
    private double determineTotalAverageCost(PowerPlant plant, double hours) {
        double fixedOMCost = calculateFixedOperatingCost(plant, getCurrentTick())
                / plant.getAvailableCapacity(getCurrentTick());
        double fixedcapitalCost = plant.getActualInvestedCapital() //Doesn't consider the cost of capital, but just the overall invested capital
                / plant.getTechnology().getDepreciationTime()/ plant.getAvailableCapacity(getCurrentTick());
        return determineFuelCost(plant) + determineCO2Cost(plant)
                + ((fixedOMCost + fixedcapitalCost) / hours);
    }

    private double determineCO2Cost(PowerPlant plant) {
        return calculateMarginalCO2Cost(plant, getCurrentTick(), false);
    }

    private double determineFuelCost(PowerPlant plant) {
        return calculateMarginalFuelCost(plant, getCurrentTick());
    }
}
