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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.transaction.annotation.Transactional;

import agentspring.role.Role;
import agentspring.role.RoleComponent;
import emlab.gen.domain.agent.DecarbonizationModel;
import emlab.gen.domain.agent.Government;
import emlab.gen.domain.agent.NationalGovernment;
import emlab.gen.domain.gis.Zone;
import emlab.gen.domain.market.CO2Auction;
import emlab.gen.domain.market.ClearingPoint;
import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.domain.market.electricity.PowerPlantDispatchPlan;
import emlab.gen.domain.market.electricity.Segment;
import emlab.gen.domain.technology.Interconnector;
import emlab.gen.domain.technology.PowerPlant;
import emlab.gen.domain.technology.Substance;
import emlab.gen.repository.Reps;
import emlab.gen.util.Utils;

/**
 * Creates and clears the {@link ElectricitySpotMarket} for two {@link Zone}s. The market is divided into {@link Segment}s and cleared for each segment. A global CO2 emissions market is cleared. The
 * process is iterative and the target is to let the total emissions match the cap.
 * 
 * @author <a href="mailto:E.J.L.Chappin@tudelft.nl">Emile Chappin</a>
 * 
 * @author <a href="mailto:A.Chmieliauskas@tudelft.nl">Alfredas Chmieliauskas</a>
 * 
 * @author <a href="mailto:J.C.Richstein@tudelft.nl">Joern C. Richstein</a>
 * 
 */
@RoleComponent
public class ClearIterativeCO2AndElectricitySpotMarketTwoCountryRole extends AbstractClearElectricitySpotMarketRole<DecarbonizationModel>
implements Role<DecarbonizationModel> {

    @Autowired
    private Reps reps;

    @Autowired
    SubmitOffersToElectricitySpotMarketRole submitOffersToElectricitySpotMarketRole;

    @Autowired
    Neo4jTemplate template;

    @Override
    @Transactional
    public void act(DecarbonizationModel model) {

        // find all operational power plants and store the ones operational to a
        // list.

        logger.info("Clearing the CO2 and electricity spot markets using iteration for 2 countries ");

        // find all fuel prices
        Map<Substance, Double> fuelPriceMap = new HashMap<Substance, Double>();
        for (Substance substance : template.findAll(Substance.class)) {
            fuelPriceMap.put(substance, findLastKnownPriceForSubstance(substance));
        }

        // find all interconnectors
        Interconnector interconnector = template.findAll(Interconnector.class).iterator().next();

        // find all segments
        List<Segment> segments = Utils.asList(reps.segmentRepository.findAll());

        // find the EU government
        Government government = template.findAll(Government.class).iterator().next();

        // find national minimum CO2 prices. Initial Map size is 2.
        Map<ElectricitySpotMarket, Double> nationalMinCo2Prices = new HashMap<ElectricitySpotMarket, Double>(2);
        Iterable<NationalGovernment> nationalGovernments = template.findAll(NationalGovernment.class);
        for (NationalGovernment nG : nationalGovernments) {
            if (model.isCo2TradingImplemented()) {
                nationalMinCo2Prices.put(reps.marketRepository.findElectricitySpotMarketByNationalGovernment(nG), nG
                        .getMinNationalCo2PriceTrend().getValue(getCurrentTick()));
            } else {
                nationalMinCo2Prices.put(reps.marketRepository.findElectricitySpotMarketByNationalGovernment(nG), 0d);
            }

        }

        CO2Auction co2Auction = template.findAll(CO2Auction.class).iterator().next();

        if (model.isCo2TradingImplemented()) {
            // Old Iteration
            // CO2PriceStability co2PriceStability = new CO2PriceStability();
            // co2PriceStability.stable = false;
            // co2PriceStability.positive = false;
            // co2PriceStability.iterationSpeedFactor = model.getIterationSpeedFactor();
            // co2PriceStability.co2Price = findLastKnownPriceOnMarket(co2Auction);
            // New Iteration
            CO2SecantSearch co2SecantSearch = new CO2SecantSearch();
            co2SecantSearch.stable = false;
            co2SecantSearch.twoPricesExistWithBelowAboveEmissions = false;
            co2SecantSearch.co2Price = findLastKnownPriceOnMarket(co2Auction);
            co2SecantSearch.tooHighEmissionsPair = null;
            co2SecantSearch.tooLowEmissionsPair = null;

            // Change Iteration algorithm here, and a few lines below...

            ClearingPoint lastClearingPointOfCo2Market = reps.clearingPointRepositoryOld.findClearingPointForMarketAndTime(co2Auction,
                    getCurrentTick() - 1);
            if (lastClearingPointOfCo2Market != null) {
                co2SecantSearch.co2Emissions = lastClearingPointOfCo2Market.getVolume();
            } else {
                co2SecantSearch.co2Emissions = 0d;
            }

            int breakOffIterator = 0;
            while (!co2SecantSearch.stable) {

                if (breakOffIterator > 15) {
                    logger.warn("Iteration cancelled, last found CO2 Price is used.");
                    break;
                }

                // Clear the electricity markets with the expected co2Price

                //updatePowerPlanDispatchPlansWithNewCO2Prices(co2SecantSearch.co2Price, nationalMinCo2Prices);
                submitOffersToElectricitySpotMarketRole.updateMarginalCostInclCO2AfterFuelMixChange(co2SecantSearch.co2Price, nationalMinCo2Prices);

                if (model.isLongTermContractsImplemented())
                    determineCommitmentOfPowerPlantsOnTheBasisOfLongTermContracts(segments);

                for (Segment segment : segments) {
                    clearOneOrTwoConnectedElectricityMarketsAtAGivenCO2PriceForOneSegment(interconnector.getCapacity(), segment, government);
                }

                // Change Iteration algorithm here
                // co2PriceStability = determineStabilityOfCO2andElectricityPricesAndAdjustIfNecessary(co2PriceStability, model, government);
                co2SecantSearch = co2PriceSecantSearchUpdate(co2SecantSearch, model, government);
                breakOffIterator++;

            }

            // Check if iteration is stable if fuel mix change is considered.
            // co2SecantSearch.stable = false;
            // co2SecantSearch.twoPricesExistWithBelowAboveEmissions = false;
            // co2SecantSearch.tooHighEmissionsPair = null;
            // co2SecantSearch.tooLowEmissionsPair = null;
            //
            // while (!co2SecantSearch.stable && co2SecantSearch.co2Price != findLastKnownPriceOnMarket(co2Auction)) {
            //
            // submitOffersToElectricitySpotMarketRole.updateMarginalCostInclCO2AfterFuelMixChange(co2SecantSearch.co2Price,
            // nationalMinCo2Prices);
            // if (model.isLongTermContractsImplemented())
            // determineCommitmentOfPowerPlantsOnTheBasisOfLongTermContracts(segments);
            //
            // for (Segment segment : segments) {
            // clearTwoConnectedElectricityMarketsAtAGivenCO2PriceForOneSegment(interconnector.getCapacity(), segment, government);
            // }
            //
            // co2SecantSearch = co2PriceSecantSearchUpdate(co2SecantSearch, model, government);
            // }

            // Save the resulting CO2 price to the CO2 auction
            reps.clearingPointRepositoryOld.createOrUpdateClearingPoint(co2Auction, co2SecantSearch.co2Price, co2SecantSearch.co2Emissions,
                    getCurrentTick());
        } else {
            if (model.isLongTermContractsImplemented())
                determineCommitmentOfPowerPlantsOnTheBasisOfLongTermContracts(segments);
            for (Segment segment : segments) {
                clearOneOrTwoConnectedElectricityMarketsAtAGivenCO2PriceForOneSegment(interconnector.getCapacity(), segment, government);
            }
        }

    }

    /**
     * Clears a time segment of all electricity markets for a given CO2 price.
     * 
     * @param powerPlants
     *            to be used
     * @param markets
     *            to clear
     * @return the total CO2 emissions
     */
    @Transactional
    void clearOneOrTwoConnectedElectricityMarketsAtAGivenCO2PriceForOneSegment(double interconnectorCapacity, Segment segment,
            Government government) {

        GlobalSegmentClearingOutcome globalOutcome = new GlobalSegmentClearingOutcome();

        globalOutcome.loads = determineActualDemandForSpotMarkets(segment);

        globalOutcome.globalLoad = determineTotalLoadFromLoadMap(globalOutcome.loads);

        // Keep track of supply per market. Start at 0.
        for (ElectricitySpotMarket m : reps.marketRepository.findAllElectricitySpotMarkets()) {
            globalOutcome.supplies.put(m, 0d);
        }

        // empty list of plants that are supplying.
        double marginalPlantMarginalCost = clearGlobalMarketWithNoCapacityConstraints(segment, globalOutcome);

        // For each plant in the cost-ordered list

        // Determine the flow over the interconnector.
        ElectricitySpotMarket firstMarket = reps.marketRepository.findAllElectricitySpotMarkets().iterator().next();
        double loadInFirstMarket = globalOutcome.loads.get(firstMarket);
        double supplyInFirstMarket = globalOutcome.supplies.get(firstMarket);

        // Interconnector flow defined as from market A --> market B = positive
        double interconnectorFlow = supplyInFirstMarket - loadInFirstMarket;

        logger.info("Before market coupling interconnector flow: {}, available interconnector capacity {}", interconnectorFlow,
                interconnectorCapacity);

        // if interconnector is not limiting or there is only one market, there is one price
        if (reps.marketRepository.countAllElectricitySpotMarkets() < 2 || Math.abs(interconnectorFlow) <= interconnectorCapacity) {
            // Set the price to the bid of the marginal plant.
            for (ElectricitySpotMarket market : reps.marketRepository.findAllElectricitySpotMarkets()) {
                double supplyInThisMarket = globalOutcome.supplies.get(market);

                // globalOutcome.globalSupply += supplyInThisMarket;

                if (globalOutcome.globalLoad <= globalOutcome.globalSupply) {
                    globalOutcome.globalPrice = marginalPlantMarginalCost;
                } else {
                    globalOutcome.globalPrice = market.getValueOfLostLoad();
                }

                // updatePowerDispatchPlansAfterTwoCountryClearingIsComplete(segment);

                reps.clearingPointRepositoryOld.createOrUpdateSegmentClearingPoint(segment, market, globalOutcome.globalPrice,
                        supplyInThisMarket * segment.getLengthInHours(), getCurrentTick());
                logger.info("Stored a system-uniform price for market " + market + " / segment " + segment + " -- supply "
                        + supplyInThisMarket + " -- price: " + globalOutcome.globalPrice);
            }

        } else {

            MarketSegmentClearingOutcome marketOutcomes = new MarketSegmentClearingOutcome();
            for (ElectricitySpotMarket m : reps.marketRepository.findAllElectricitySpotMarkets()) {
                marketOutcomes.supplies.put(m, 0d);
                marketOutcomes.prices.put(m, m.getValueOfLostLoad());
            }

            // else there are two prices
            logger.info("There should be multiple prices, but first we should do market coupling.");

            boolean firstImporting = true;
            if (interconnectorFlow > 0) {
                firstImporting = false;
            }

            boolean first = true;
            for (ElectricitySpotMarket market : reps.marketRepository.findAllElectricitySpotMarkets()) {

                // Update the load for this market. Which is market's true load
                // +/- the full interconnector capacity, based on direction of
                // the flow
                if ((first && firstImporting) || (!first && !firstImporting)) {
                    marketOutcomes.loads.put(market, globalOutcome.loads.get(market) - interconnectorCapacity);
                } else {
                    marketOutcomes.loads.put(market, globalOutcome.loads.get(market) + interconnectorCapacity);
                }
                first = false;

            }

            // For each plant in the cost-ordered list

            clearTwoInterconnectedMarketsGivenAnInterconnectorAdjustedLoad(segment, marketOutcomes);

            // updatePowerDispatchPlansAfterTwoCountryClearingIsComplete(segment);

            for (ElectricitySpotMarket market : reps.marketRepository.findAllElectricitySpotMarkets()) {
                if (marketOutcomes.supplies.get(market) < marketOutcomes.loads.get(market)) {
                    marketOutcomes.prices.put(market, market.getValueOfLostLoad());
                }
            }

            // Only for debugging purposes
            // logger.warn("Outcomes: {}", marketOutcomes);
            // for (ElectricitySpotMarket market : markets) {
            // logger.warn(
            // "Segment " + segment.getSegmentID() +
            // ": PPD capacity: {} MW, PP capacity: {}, Peak-Query: "
            // +
            // reps.powerPlantRepository.calculatePeakCapacityOfOperationalPowerPlantsInMarket(market,
            // getCurrentTick()),
            // determineCapacityInMarketBasedOnTreemapAndDispatchPlans(marginalCostMap,
            // segment, market, markets),
            // determinePeakCapacityInMarketBasedOnTreemapAndPowerPlants(marginalCostMap,
            // segment, market, markets)
            // + "Normal Capacity: "
            // +
            // determineCapacityInMarketBasedOnTreemapAndPowerPlants(marginalCostMap,
            // market, markets)
            // + "Query Capacity: "
            // +
            // reps.powerPlantRepository.calculateCapacityOfOperationalPowerPlantsInMarket(market,
            // getCurrentTick()));
            // }
            for (ElectricitySpotMarket market : reps.marketRepository.findAllElectricitySpotMarkets()) {
                reps.clearingPointRepositoryOld.createOrUpdateSegmentClearingPoint(segment, market, marketOutcomes.prices.get(market),
                        marketOutcomes.supplies.get(market) * segment.getLengthInHours(), getCurrentTick());
                // logger.warn("Stored a market specific price for market " +
                // market + " / segment " + segment + " -- supply "
                // + marketOutcomes.supplies.get(market) + " -- demand: " +
                // marketOutcomes.loads.get(market) + " -- price: "
                // + marketOutcomes.prices.get(market));
            }

            @SuppressWarnings("unused")
            int i = 0;
        }
    }

    void clearTwoInterconnectedMarketsGivenAnInterconnectorAdjustedLoad(Segment segment, MarketSegmentClearingOutcome marketOutcomes) {

        for (PowerPlantDispatchPlan plan : reps.powerPlantDispatchPlanRepository.findSortedPowerPlantDispatchPlansForSegmentForTime(
                segment, getCurrentTick())) {

            // If it is in the right market
            PowerPlant plant = plan.getPowerPlant();

            ElectricitySpotMarket myMarket = (ElectricitySpotMarket) plan.getBiddingMarket();

            // Make it produce as long as there is load.
            double plantSupply = determineProductionOnSpotMarket(plan, marketOutcomes.supplies.get(myMarket),
                    marketOutcomes.loads.get(myMarket));
            if (plantSupply > 0) {
                // Plant is producing, store the information to
                // determine price and so on.
                marketOutcomes.supplies.put(myMarket, marketOutcomes.supplies.get(myMarket) + plantSupply);
                marketOutcomes.prices.put(myMarket, plan.getPrice());
                // logger.warn("Storing price: {} for plant {} in market " +
                // myMarket, plantCost.getValue(), plant);
            }
        }

    }

    @Override
    public Reps getReps() {
        return reps;
    }

}
