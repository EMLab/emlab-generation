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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math.stat.regression.SimpleRegression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.transaction.annotation.Transactional;

import agentspring.role.AbstractRole;
import emlab.gen.domain.agent.CommoditySupplier;
import emlab.gen.domain.agent.DecarbonizationModel;
import emlab.gen.domain.agent.EnergyConsumer;
import emlab.gen.domain.agent.EnergyProducer;
import emlab.gen.domain.agent.Government;
import emlab.gen.domain.contract.LongTermContract;
import emlab.gen.domain.gis.Zone;
import emlab.gen.domain.market.Bid;
import emlab.gen.domain.market.ClearingPoint;
import emlab.gen.domain.market.DecarbonizationMarket;
import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.domain.market.electricity.PowerPlantDispatchPlan;
import emlab.gen.domain.market.electricity.Segment;
import emlab.gen.domain.technology.PowerPlant;
import emlab.gen.domain.technology.Substance;
import emlab.gen.repository.Reps;
import emlab.gen.util.GeometricTrendRegression;

/**
 * Creates and clears the {@link ElectricitySpotMarket} for two {@link Zone}s. The market is divided into {@link Segment}s and cleared for each segment. A global CO2 emissions market is cleared. The
 * process is iterative and the target is to let the total emissions match the cap.
 * 
 * @author <a href="mailto:E.J.L.Chappin@tudelft.nl">Emile Chappin</a>
 * 
 * @author <a href="mailto:A.Chmieliauskas@tudelft.nl">Alfredas Chmieliauskas</a>
 * 
 */
public abstract class AbstractClearElectricitySpotMarketRole<T extends DecarbonizationModel> extends AbstractRole<T> {

    @Autowired
    private Reps reps;

    @Autowired
    private Neo4jTemplate template;

    protected final double epsilon = 1e-3;

    class MarketSegmentClearingOutcome {
        HashMap<ElectricitySpotMarket, Double> loads = new HashMap<ElectricitySpotMarket, Double>();
        HashMap<ElectricitySpotMarket, Double> prices = new HashMap<ElectricitySpotMarket, Double>();
        HashMap<ElectricitySpotMarket, Double> supplies = new HashMap<ElectricitySpotMarket, Double>();

        @Override
        public String toString() {
            return new String("Market outcome: loads " + loads + " prices: " + prices + " supplies: " + supplies);
        }
    }

    class GlobalSegmentClearingOutcome {
        Map<ElectricitySpotMarket, Double> loads;
        Map<ElectricitySpotMarket, Double> supplies = new HashMap<ElectricitySpotMarket, Double>();
        double globalLoad;
        double globalPrice;
        double globalSupply;

        @Override
        public String toString() {
            return "Global Data; loads: " + loads + ", supplies: " + supplies + " globalLoad: " + globalLoad + ", globalSupply: "
                    + globalSupply;
        }
    }

    public class CO2Iteration {
        public boolean stable;
        public double co2Price;
        public double co2Emissions;

    }

    public class CO2PriceStability extends CO2Iteration {

        public boolean positive;
        public double iterationSpeedFactor;
        public double changeInDeviationFromLastStep;

    }

    public class CO2SecantSearch extends CO2Iteration {
        public boolean twoPricesExistWithBelowAboveEmissions;
        public double higherCO2Price;
        public int iteration = 0;
        public PriceEmissionPair tooLowEmissionsPair;
        public PriceEmissionPair tooHighEmissionsPair;
        public double bankingEffectiveMinimumPrice;

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            // TODO Auto-generated method stub
            return "Stable: " + this.stable + ", co2Price: " + this.co2Price + ", co2Emissions: " + this.co2Emissions;
        }

    }

    class PriceEmissionPair {
        public double price;
        public double emission;
    }

    CO2SecantSearch co2PriceSecantSearchUpdate(CO2SecantSearch co2SecantSearch, DecarbonizationModel model,
            Government government, boolean forecast, long clearingTick, double co2CapAdjustment) {

        co2SecantSearch.stable = false;
        double capDeviationCriterion = model.getCapDeviationCriterion();
        double co2Cap = government.getCo2Cap(clearingTick) + co2CapAdjustment;
        co2SecantSearch.co2Emissions = determineTotalEmissionsBasedOnPowerPlantDispatchPlan(forecast, clearingTick);

        double deviation = (co2SecantSearch.co2Emissions - co2Cap) / co2Cap;

        // Check if current price leads to emissions close to the cap.
        if (Math.abs(deviation) < capDeviationCriterion) {
            // logger.warn("Deviation is less than capDeviationCriterion");
            co2SecantSearch.stable = true;
            return co2SecantSearch;
        }

        // Check and update the twoPricesExistWithBelowAboveEmissions

        if (co2SecantSearch.tooHighEmissionsPair != null && co2SecantSearch.tooLowEmissionsPair != null) {
            co2SecantSearch.twoPricesExistWithBelowAboveEmissions = true;
        } else if (co2SecantSearch.co2Price == government.getMinCo2Price(clearingTick)
                && co2SecantSearch.co2Emissions < co2Cap) {
            // logger.warn("Deviation CO2 price has reached minimum");
            // check if stable enough --> 2. Cap is met with a co2Price
            // equal to the minimum co2 price
            co2SecantSearch.stable = true;
            return co2SecantSearch;
        } else if (co2SecantSearch.co2Price >= government.getCo2Penalty(clearingTick)
                && co2SecantSearch.co2Emissions >= co2Cap) {
            // Only if above the cap...
            // logger.warn("CO2 price ceiling reached {}", co2SecantSearch.co2Price);
            co2SecantSearch.co2Price = government.getCo2Penalty(clearingTick);
            co2SecantSearch.stable = true;
            return co2SecantSearch;
        }

        // Check whether we know two pairs, one with EmissionsAboveCap, one with EmissionsBelowCap
        // in case of yes: calculate new CO2 price via secant calculation. In case of no: Take last known
        // price above or below, or halve/double the price.
        if (co2SecantSearch.twoPricesExistWithBelowAboveEmissions) {

            // Update the emission pairs
            if (deviation > 0) {
                co2SecantSearch.tooHighEmissionsPair.price = co2SecantSearch.co2Price;
                co2SecantSearch.tooHighEmissionsPair.emission = co2SecantSearch.co2Emissions;
            } else {
                co2SecantSearch.tooLowEmissionsPair.price = co2SecantSearch.co2Price;
                co2SecantSearch.tooLowEmissionsPair.emission = co2SecantSearch.co2Emissions;
            }

            double p2 = co2SecantSearch.tooHighEmissionsPair.price;
            double p1 = co2SecantSearch.tooLowEmissionsPair.price;
            double e2 = co2SecantSearch.tooHighEmissionsPair.emission - co2Cap;
            double e1 = co2SecantSearch.tooLowEmissionsPair.emission - co2Cap;

            // Interrupts long iterations by making a binary search step.
            if (co2SecantSearch.iteration < 5) {
                co2SecantSearch.co2Price = p1 - (e1 * (p2 - p1) / (e2 - e1));
                co2SecantSearch.iteration++;
                // logger.warn("New CO2 Secant price {}", co2SecantSearch.co2Price);
            } else {
                co2SecantSearch.co2Price = (p1 + p2) / 2;
                co2SecantSearch.iteration = 0;
                // logger.warn("New CO2 Binary price {}", co2SecantSearch.co2Price);
            }

        } else {

            if (deviation > 0) {
                if (co2SecantSearch.tooHighEmissionsPair == null)
                    co2SecantSearch.tooHighEmissionsPair = new PriceEmissionPair();

                co2SecantSearch.tooHighEmissionsPair.price = co2SecantSearch.co2Price;
                co2SecantSearch.tooHighEmissionsPair.emission = co2SecantSearch.co2Emissions;

                if (co2SecantSearch.tooLowEmissionsPair == null) {
                    co2SecantSearch.co2Price = (co2SecantSearch.co2Price != 0d) ? ((co2SecantSearch.co2Price * 2 < government
                            .getCo2Penalty(clearingTick)) ? (co2SecantSearch.co2Price * 2) : government
                                    .getCo2Penalty(clearingTick)) : 5d;
                            // logger.warn("New doubled CO2 search price {}", co2SecantSearch.co2Price);
                } else {
                    double p2 = co2SecantSearch.tooHighEmissionsPair.price;
                    double p1 = co2SecantSearch.tooLowEmissionsPair.price;
                    double e2 = co2SecantSearch.tooHighEmissionsPair.emission - co2Cap;
                    double e1 = co2SecantSearch.tooLowEmissionsPair.emission - co2Cap;

                    co2SecantSearch.co2Price = p1 - (e1 * (p2 - p1) / (e2 - e1));
                    co2SecantSearch.iteration++;
                    // logger.warn("New CO2 Secant price {}", co2SecantSearch.co2Price);
                }

            } else {

                if (co2SecantSearch.tooLowEmissionsPair == null)
                    co2SecantSearch.tooLowEmissionsPair = new PriceEmissionPair();

                co2SecantSearch.tooLowEmissionsPair.price = co2SecantSearch.co2Price;
                co2SecantSearch.tooLowEmissionsPair.emission = co2SecantSearch.co2Emissions;

                if (co2SecantSearch.tooHighEmissionsPair == null) {
                    co2SecantSearch.co2Price = (co2SecantSearch.co2Price / 2);
                    // logger.warn("New halved CO2 search price {}", co2SecantSearch.co2Price);
                } else {
                    double p2 = co2SecantSearch.tooHighEmissionsPair.price;
                    double p1 = co2SecantSearch.tooLowEmissionsPair.price;
                    double e2 = co2SecantSearch.tooHighEmissionsPair.emission - co2Cap;
                    double e1 = co2SecantSearch.tooLowEmissionsPair.emission - co2Cap;

                    co2SecantSearch.co2Price = p1 - (e1 * (p2 - p1) / (e2 - e1));
                    // logger.warn("New CO2 Secant price {}", co2SecantSearch.co2Price);
                    co2SecantSearch.iteration++;

                }

                if (co2SecantSearch.co2Price < 0.5
                        || co2SecantSearch.co2Price - government.getMinCo2Price(clearingTick) < 0.5) {
                    co2SecantSearch.co2Price = government.getMinCo2Price(clearingTick);
                    co2SecantSearch.stable = true;
                }

            }

        }

        return co2SecantSearch;

    }

    /**
     * Clears a the global market, under the assumption that no capacity constraints apply, and that demand is fixed in that segment. Has been taken out of the main function, to make it transactional.
     * 
     * @param segment
     * @param markets
     * @param globalOutcome
     * @return
     */

    double clearGlobalMarketWithNoCapacityConstraints(Segment segment, GlobalSegmentClearingOutcome globalOutcome,
            boolean forecast, long clearingTick) {

        double marginalPlantMarginalCost = Double.MAX_VALUE;

        for (PowerPlantDispatchPlan plan : reps.powerPlantDispatchPlanRepository.findSortedPowerPlantDispatchPlansForSegmentForTime(
                segment, clearingTick, forecast)) {
            ElectricitySpotMarket myMarket = (ElectricitySpotMarket) plan.getBiddingMarket();

            // Make it produce as long as there is load.
            double plantSupply = determineProductionOnSpotMarket(plan, globalOutcome.globalSupply, globalOutcome.globalLoad);

            if (plantSupply > 0) {
                // Plant is producing, store the information to determine price
                // and so on.
                marginalPlantMarginalCost = plan.getPrice();
                globalOutcome.supplies.put(myMarket, globalOutcome.supplies.get(myMarket) + plantSupply);
                globalOutcome.globalSupply = +globalOutcome.globalSupply + plantSupply;
                // logger.warn("Storing price: {} for plant {} in market " +
                // myMarket, plantCost.getValue(), plant);

            }
        }

        return marginalPlantMarginalCost;
    }

    /**
     * Determine for each power plant whether it will be covered (partially) by long-term contracts for each of the segments and stores that in the respective power plant dipatch plan.
     * 
     * @param plants
     *            all plants
     * @param segments
     *            segments
     * @param marginalCostMap
     *            the marginal cost (including Co2 cost)
     */
    @Transactional
    void determineCommitmentOfPowerPlantsOnTheBasisOfLongTermContracts(List<Segment> segments, boolean forecast) {

        for (EnergyProducer producer : reps.genericRepository.findAll(EnergyProducer.class)) {

            for (Segment segment : segments) {

                // How much capacity is contracted by long term contracts in
                // this segment?
                double contractedCapacityInSegment = 0;

                for (LongTermContract ltc : reps.contractRepository.findLongTermContractsForEnergyProducerForSegmentActiveAtTime(producer,
                        segment, getCurrentTick())) {
                    contractedCapacityInSegment += ltc.getCapacity();
                }

                // for all power plants in the sorted marginal cost map
                for (PowerPlantDispatchPlan plan : reps.powerPlantDispatchPlanRepository
                        .findAllPowerPlantDispatchPlansForEnergyProducerForTimeAndSegment(segment, producer,
                                getCurrentTick(), forecast)) {
                    PowerPlant plant = plan.getPowerPlant();

                    double availableCapacity = plant.getAvailableCapacity(getCurrentTick(), segment, segments.size());

                    // logger.warn("Capacity of plant " + plant.toString() +
                    // " is " + availableCapacity);

                    if (plant.getTechnology().isApplicableForLongTermContract()) {

                        if (contractedCapacityInSegment - availableCapacity > 0) {

                            // the whole plant has to be used for long term
                            // contract
                            plan.setCapacityLongTermContract(availableCapacity);
                        } else {
                            // use the contractedCapacity left for long term
                            // contract
                            plan.setCapacityLongTermContract(contractedCapacityInSegment);
                        }
                    } else {
                        // Not applicable for LTC, so save 0 capacity for
                        // it.
                        plan.setCapacityLongTermContract(0);
                    }

                    // Update the capacity available for the spot market,
                    // which is the total capacity available
                    // minus committed to LTC's.
                    plan.setAmount(availableCapacity - plan.getCapacityLongTermContract());
                    contractedCapacityInSegment -= plan.getCapacityLongTermContract();

                }
            }
        }
    }

    /**
     * Determine demand in this segment for each market, based on the total load
     * in this tick minus the load covered by LongTermContracts. If now
     * demandGrowthMap is supplied, assume current tick.
     * 
     * @param segment
     * @return the total demand
     */
    Map<ElectricitySpotMarket, Double> determineActualDemandForSpotMarkets(Segment segment, Map<ElectricitySpotMarket,Double> demandGrowthMap) {

        if (demandGrowthMap == null) {
            demandGrowthMap = new HashMap<ElectricitySpotMarket, Double>();
            for (ElectricitySpotMarket market : reps.marketRepository.findAllElectricitySpotMarkets()) {
                demandGrowthMap.put(market, market.getDemandGrowthTrend().getValue(getCurrentTick()));
            }
        }

        Map<ElectricitySpotMarket, Double> loadInMarkets = new HashMap<ElectricitySpotMarket, Double>();

        for (ElectricitySpotMarket market : reps.marketRepository.findAllElectricitySpotMarkets()) {
            double baseLoad = reps.segmentLoadRepository.returnSegmentBaseLoadBySegmentAndMarket(segment, market);
            double load;
            load = baseLoad * demandGrowthMap.get(market);

            // Load may be covered by long term contracts.
            double loadCoveredByLTC = 0d;

            // for each energy consumer
            for (EnergyConsumer consumer : reps.genericRepository.findAll(EnergyConsumer.class)) {
                // for each active LTC
                for (LongTermContract ltc : reps.contractRepository.findLongTermContractsForEnergyConsumerForSegmentForZoneActiveAtTime(
                        consumer, segment, market.getZone(), getCurrentTick())) {
                    // add tot the total
                    loadCoveredByLTC += ltc.getCapacity();
                }
            }

            // Part of the load may be covered by long term contracts. We
            // subtract that.
            loadInMarkets.put(market, load - loadCoveredByLTC);
        }

        return loadInMarkets;
    }

    /**
     * Determine the total load by summing up the loads of individual markets in a loadInMarkets map.
     * 
     * @param loadInMarkets
     * @return the total load.
     */

    double determineTotalLoadFromLoadMap(Map<ElectricitySpotMarket, Double> loadInMarkets) {
        double totalLoad = 0d;
        for (ElectricitySpotMarket market : loadInMarkets.keySet()) {
            totalLoad += loadInMarkets.get(market);
        }
        return totalLoad;
    }

    /**
     * Determine the production of a power plant on the spot market, based on supply so far and load to be covered. The result is saved to the respective power plant dispatch plan, as well as the
     * Status of the respective Bid.
     * 
     * @param plant
     * @param segment
     * @param supplySoFar
     * @param load
     * @return
     */
    double determineProductionOnSpotMarket(PowerPlantDispatchPlan plan, double supplySoFar, double load) {

        double plantCapacity = plan.getAmount();
        double plantSupply = 0d;

        // if after adding the supply of this extra plant demand
        // is not yet met
        if ((supplySoFar + plantCapacity) < load) {

            // Plant will be supplying completely
            plantSupply = plantCapacity;
            plan.setStatus(Bid.ACCEPTED);
        } else {

            // Plant will by partly supplying and this is the
            // final plant or is not supplying at all
            plantSupply = load - supplySoFar;
            if (plantSupply > 0) {
                plan.setStatus(Bid.PARTLY_ACCEPTED);
            } else
                plan.setStatus(Bid.FAILED);
        }

        plan.setAcceptedAmount(plantSupply);

        return plantSupply;
    }

    /**
     * Determine the total CO2 emissions based on all current power plant dispatch plans.
     * 
     * @return the total CO2 emissions
     */
    double determineTotalEmissionsBasedOnPowerPlantDispatchPlan(boolean forecast, long clearingTick) {
        double totalEmissions = 0d;
        //int counter = 0;
        for (PowerPlantDispatchPlan plan : reps.powerPlantDispatchPlanRepository.findAllPowerPlantDispatchPlansForTime(
                clearingTick, forecast)) {
            double operationalCapacity = plan.getCapacityLongTermContract() + plan.getAcceptedAmount();
            double emissionIntensity = plan.getPowerPlant().calculateEmissionIntensity();
            double hours = plan.getSegment().getLengthInHours();
            totalEmissions += operationalCapacity * emissionIntensity * hours;
            //    counter++;
        }
        // logger.warn("Total emissions: {} based on {} power plant dispatch plans", totalEmissions, counter);
        return totalEmissions;
    }

    /**
     * Determine the total CO2 emissions of EnergyProducer based on all current
     * power plant dispatch plans.
     * 
     * @return the total CO2 emissions
     */
    double determineTotalEmissionsBasedOnPowerPlantDispatchPlanForEnergyProducer(boolean forecast, long clearingTick,
            EnergyProducer producer) {
        double totalEmissions = 0d;
        // int counter = 0;
        for (PowerPlantDispatchPlan plan : reps.powerPlantDispatchPlanRepository
                .findAllAcceptedPowerPlantDispatchPlansForEnergyProducerForTime(producer, clearingTick, forecast)) {
            double operationalCapacity = plan.getCapacityLongTermContract() + plan.getAcceptedAmount();
            double emissionIntensity = plan.getPowerPlant().calculateEmissionIntensity();
            double hours = plan.getSegment().getLengthInHours();
            totalEmissions += operationalCapacity * emissionIntensity * hours;
            // counter++;
        }
        // logger.warn("Total emissions: {} based on {} power plant dispatch plans",
        // totalEmissions, counter);
        return totalEmissions;
    }

    /**
     * Determines the stability of CO2 and electricity prices, and, if not stable, adjusts the CO2 price for a next iteration.
     * 
     * @param co2PriceStability
     *            the co2PriceStability so far
     * @param model
     *            the model for some of the parameters used in the determination of stability.
     * @param government
     *            the government for some of the parameters used in the determination of stability.
     * @return the co2PriceStability object with possibly adjustments in the CO2 price, emissions, stability and direction of the change
     */
    CO2PriceStability determineStabilityOfCO2andElectricityPricesAndAdjustIfNecessary(CO2PriceStability co2PriceStability,
            DecarbonizationModel model, Government government, boolean forecast,
            long clearingTick) {

        double co2Cap = government.getCo2Cap(clearingTick);
        double minimumCo2Price = government.getMinCo2Price(clearingTick);
        double co2Penalty = government.getCo2Penalty(clearingTick);
        double iterationSpeedCriterion = model.getIterationSpeedCriterion();
        double capDeviationCriterion = model.getCapDeviationCriterion();

        co2PriceStability.co2Emissions = determineTotalEmissionsBasedOnPowerPlantDispatchPlan(forecast, clearingTick);
        double deviation = (co2PriceStability.co2Emissions - co2Cap) / co2Cap;

        // Determine the deviation from the cap.
        logger.warn("Cap {} (euro/ton) vs emissions {} (euro/ton)", co2Cap, co2PriceStability.co2Emissions);
        logger.warn("Tick {} Deviation: {} %", clearingTick, deviation * 100);

        // check if the deviation is smaller then the criterion --> 1.
        // Close to the cap or almost stopped moving
        if (Math.abs(deviation) < capDeviationCriterion) {
            logger.warn("Deviation is less than capDeviationCriterion");
            co2PriceStability.stable = true;
        } else if (co2PriceStability.iterationSpeedFactor < iterationSpeedCriterion) {
            logger.warn("Deviation iterationSpeedFactor is less than iterationSpeedCriterion");
            co2PriceStability.stable = true;
        } else if (co2PriceStability.co2Price == minimumCo2Price && co2PriceStability.co2Emissions < co2Cap) {
            logger.warn("Deviation CO2 price has reached minimum");
            // check if stable enough --> 2. Cap is met with a co2Price
            // equal to the minimum co2 price
            co2PriceStability.stable = true;

        } else if (co2PriceStability.co2Price >= co2Penalty && co2PriceStability.co2Emissions >= co2Cap) {
            // Only if above the cap...
            logger.warn("CO2 price ceiling reached {}", co2PriceStability.co2Price);
            co2PriceStability.stable = true;
        } else {
            co2PriceStability.co2Price = co2PriceStability.co2Price * (1 + deviation * co2PriceStability.iterationSpeedFactor);
            logger.warn("Deviation updated CO2 price to {}", co2PriceStability.co2Price);
        }

        // if price is 0, but the cap is not met, we have to
        // change it, otherwise, you could never get out of 0.
        // In that case assume stability and assume a price of 2.
        if (co2PriceStability.co2Price == 0 && co2PriceStability.co2Emissions >= co2Cap) {
            logger.warn("Deviation resetting CO2 price to 2");
            co2PriceStability.co2Price = 2;
            co2PriceStability.stable = true;
        }

        // make the speed smaller if we passed by the target
        if ((co2PriceStability.positive && deviation < 0) || (!co2PriceStability.positive && deviation > 0)) {
            co2PriceStability.iterationSpeedFactor = co2PriceStability.iterationSpeedFactor / 2;
            logger.warn("Deviation speed factor decreased {}", co2PriceStability.iterationSpeedFactor);
        }

        // If we are below the cap and close to or below the minimum
        // CO2
        // price set the price to the minimum co2
        // price.
        if ((co2PriceStability.co2Price < (0.1 + minimumCo2Price)) && (co2PriceStability.co2Emissions < co2Cap)) {
            logger.warn("Deviation reseting CO2 price to minimum");
            co2PriceStability.co2Price = minimumCo2Price;
        }

        // record whether the last change was positive or not
        if (deviation < 0) {
            co2PriceStability.positive = false;
        } else {
            co2PriceStability.positive = true;
        }

        return co2PriceStability;
    }

    /**
     * Finds the last known price for a substance. We try to find the market for it and get it get the price on that market for this tick, previous tick, or from a possible supplier directly. If
     * multiple prices are found, the average is returned. This is the case for electricity spot markets, as they may have segments.
     * 
     * @param substance
     *            the price we want for
     * @return the (average) price found
     */
    double findLastKnownPriceForSubstance(Substance substance) {

        DecarbonizationMarket market = reps.marketRepository.findFirstMarketBySubstance(substance);
        if (market == null) {
            logger.warn("No market found for {} so no price can be found", substance.getName());
            return 0d;
        } else {
            return findLastKnownPriceOnMarket(market);
        }
    }

    /**
     * Finds the last known price on a specific market. We try to get it for this tick, previous tick, or from a possible supplier directly. If multiple prices are found, the average is returned. This
     * is the case for electricity spot markets, as they may have segments.
     * 
     * @param substance
     *            the price we want for
     * @return the (average) price found
     */
    double findLastKnownPriceOnMarket(DecarbonizationMarket market) {
        Double average = calculateAverageMarketPriceBasedOnClearingPoints(reps.clearingPointRepositoryOld
                .findClearingPointsForMarketAndTime(market, getCurrentTick(), false));
        Substance substance = market.getSubstance();

        if (average != null) {
            logger.info("Average price found on market for this tick for {}", substance.getName());
            return average;
        }

        average = calculateAverageMarketPriceBasedOnClearingPoints(reps.clearingPointRepositoryOld.findClearingPointsForMarketAndTime(
                market, getCurrentTick() - 1, false));
        if (average != null) {
            logger.info("Average price found on market for previous tick for {}", substance.getName());
            return average;
        }

        if (market.getReferencePrice() > 0) {
            logger.info("Found a reference price found for market for {}", substance.getName());
            return market.getReferencePrice();
        }

        for (CommoditySupplier supplier : reps.genericRepository.findAll(CommoditySupplier.class)) {
            if (supplier.getSubstance().equals(substance)) {

                logger.info("Price found for {} by asking the supplier {} directly", substance.getName(), supplier.getName());
                return supplier.getPriceOfCommodity().getValue(getCurrentTick());
            }
        }

        logger.info("No price has been found for {}", substance.getName());
        return 0d;
    }

    /**
     * Calculates the volume-weighted average price on a market based on a set of clearingPoints.
     * 
     * @param clearingPoints
     *            the clearingPoints with the volumes and prices
     * @return the weighted average
     */
    private Double calculateAverageMarketPriceBasedOnClearingPoints(Iterable<ClearingPoint> clearingPoints) {
        double priceTimesVolume = 0d;
        double volume = 0d;

        for (ClearingPoint point : clearingPoints) {
            priceTimesVolume += point.getPrice() * point.getVolume();
            volume += point.getVolume();
        }
        if (volume > 0) {
            return priceTimesVolume / volume;
        }
        return null;
    }

    public Reps getReps() {
        return reps;
    }

    public Map<Substance, Double> predictFuelPrices(long numberOfYearsBacklookingForForecasting, long futureTimePoint) {
        // Fuel Prices
        Map<Substance, Double> expectedFuelPrices = new HashMap<Substance, Double>();
        for (Substance substance : reps.substanceRepository.findAllSubstancesTradedOnCommodityMarkets()) {

            Iterable<ClearingPoint> cps = reps.clearingPointRepository
                    .findAllClearingPointsForSubstanceTradedOnCommodityMarkesAndTimeRange(substance, getCurrentTick()
                            - (numberOfYearsBacklookingForForecasting - 1), getCurrentTick() - 1,
                            false);

            SimpleRegression gtr = new SimpleRegression();
            for (ClearingPoint clearingPoint : cps) {

                gtr.addData(clearingPoint.getTime(), clearingPoint.getPrice());
            }
            gtr.addData(getCurrentTick(), findLastKnownPriceForSubstance(substance));
            double forecast = gtr.predict(futureTimePoint);
            if (Double.isNaN(forecast)) {
                expectedFuelPrices.put(substance, findLastKnownPriceForSubstance(substance));
            } else {
                expectedFuelPrices.put(substance, forecast);
            }
        }
        return expectedFuelPrices;
    }

    public Map<ElectricitySpotMarket, Double> predictDemand(long numberOfYearsBacklookingForForecasting,
            long futureTimePoint) {
        Map<ElectricitySpotMarket, Double> expectedDemand = new HashMap<ElectricitySpotMarket, Double>();
        for (ElectricitySpotMarket elm : reps.template.findAll(ElectricitySpotMarket.class)) {
            GeometricTrendRegression gtr = new GeometricTrendRegression();
            for (long time = getCurrentTick(); time > getCurrentTick() - numberOfYearsBacklookingForForecasting
                    && time >= 0; time = time - 1) {
                gtr.addData(time, elm.getDemandGrowthTrend().getValue(time));
            }
            double forecast = gtr.predict(futureTimePoint);
            if (Double.isNaN(forecast))
                forecast = elm.getDemandGrowthTrend().getValue(getCurrentTick());
            expectedDemand.put(elm, forecast);
        }
        return expectedDemand;
    }

    @Transactional
    void updatePowerPlanDispatchPlansWithNewCO2Prices(double co2Price,
            Map<ElectricitySpotMarket, Double> nationalMinCo2Prices, long clearingTick, boolean forecast) {
        for (PowerPlantDispatchPlan plan : reps.powerPlantDispatchPlanRepository.findAllPowerPlantDispatchPlansForTime(
                clearingTick, forecast)) {
            if (nationalMinCo2Prices.get(plan.getBiddingMarket()) > co2Price) {
                plan.setPrice(plan.getBidWithoutCO2()
                        + (nationalMinCo2Prices.get(plan.getBiddingMarket()) * plan.getPowerPlant().calculateEmissionIntensity()));
            } else {
                plan.setPrice(plan.getBidWithoutCO2() + (co2Price * plan.getPowerPlant().calculateEmissionIntensity()));
            }
        }
    }

}
