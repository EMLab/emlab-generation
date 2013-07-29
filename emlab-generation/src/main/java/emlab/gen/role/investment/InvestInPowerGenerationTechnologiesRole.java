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
package emlab.gen.role.investment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.data.annotation.Transient;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.aspects.core.NodeBacked;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.transaction.annotation.Transactional;

import agentspring.role.Role;
import emlab.gen.domain.agent.BigBank;
import emlab.gen.domain.agent.EnergyProducer;
import emlab.gen.domain.agent.LocalGovernment;
import emlab.gen.domain.agent.PowerPlantManufacturer;
import emlab.gen.domain.agent.StrategicReserveOperator;
import emlab.gen.domain.contract.CashFlow;
import emlab.gen.domain.contract.Loan;
import emlab.gen.domain.gis.Zone;
import emlab.gen.domain.market.ClearingPoint;
import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.domain.market.electricity.Segment;
import emlab.gen.domain.market.electricity.SegmentLoad;
import emlab.gen.domain.policy.PowerGeneratingTechnologyTarget;
import emlab.gen.domain.sitelocation.Location;
import emlab.gen.domain.sitelocation.LocationLocalParties;
import emlab.gen.domain.technology.PowerGeneratingTechnology;
import emlab.gen.domain.technology.PowerGeneratingTechnologyNodeLimit;
import emlab.gen.domain.technology.PowerGridNode;
import emlab.gen.domain.technology.PowerPlant;
import emlab.gen.domain.technology.Substance;
import emlab.gen.domain.technology.SubstanceShareInFuelMix;
import emlab.gen.repository.Reps;
import emlab.gen.repository.StrategicReserveOperatorRepository;
import emlab.gen.util.GeometricTrendRegression;
import emlab.gen.util.MapValueComparator;

/**
 * {@link EnergyProducer}s decide to invest in new {@link PowerPlant}
 * 
 * @author <a href="mailto:E.J.L.Chappin@tudelft.nl">Emile Chappin</a> @author
 *         <a href="mailto:A.Chmieliauskas@tudelft.nl">Alfredas
 *         Chmieliauskas</a>
 * @author JCRichstein
 */
@Configurable
@NodeEntity
public class InvestInPowerGenerationTechnologiesRole<T extends EnergyProducer> extends GenericInvestmentRole<T>
        implements Role<T>, NodeBacked {

    @Transient
    @Autowired
    Reps reps;

    @Transient
    @Autowired
    Neo4jTemplate template;

    @Transient
    @Autowired
    StrategicReserveOperatorRepository strategicReserveOperatorRepository;

    // market expectations
    @Transient
    Map<ElectricitySpotMarket, MarketInformation> marketInfoMap = new HashMap<ElectricitySpotMarket, MarketInformation>();

    @Override
    public void act(T agent) {

        long futureTimePoint = getCurrentTick() + agent.getInvestmentFutureTimeHorizon();
        // logger.warn(agent + " is looking at timepoint " + futureTimePoint);

        // ==== Expectations ===

        Map<Substance, Double> expectedFuelPrices = predictFuelPrices(agent, futureTimePoint);

        // CO2
        Map<ElectricitySpotMarket, Double> expectedCO2Price = determineExpectedCO2PriceInclTax(futureTimePoint,
                agent.getNumberOfYearsBacklookingForForecasting());

        // logger.warn(expectedCO2Price.toString());

        // Demand
        Map<ElectricitySpotMarket, Double> expectedDemand = new HashMap<ElectricitySpotMarket, Double>();
        for (ElectricitySpotMarket elm : reps.template.findAll(ElectricitySpotMarket.class)) {
            GeometricTrendRegression gtr = new GeometricTrendRegression();
            for (long time = getCurrentTick(); time > getCurrentTick()
                    - agent.getNumberOfYearsBacklookingForForecasting()
                    && time >= 0; time = time - 1) {
                gtr.addData(time, elm.getDemandGrowthTrend().getValue(time));
            }
            expectedDemand.put(elm, gtr.predict(futureTimePoint));
        }

        // Investment decision
        // for (ElectricitySpotMarket market :
        // reps.genericRepository.findAllAtRandom(ElectricitySpotMarket.class))
        // {
        ElectricitySpotMarket market = agent.getInvestorMarket();
        MarketInformation marketInformation = new MarketInformation(market, expectedDemand, expectedFuelPrices,
                expectedCO2Price.get(market).doubleValue(), futureTimePoint);
        /*
         * if (marketInfoMap.containsKey(market) &&
         * marketInfoMap.get(market).time == futureTimePoint) {
         * marketInformation = marketInfoMap.get(market); } else {
         * marketInformation = new MarketInformation(market, expectedFuelPrices,
         * expectedCO2Price, futureTimePoint); marketInfoMap.put(market,
         * marketInformation); }
         */

        // logger.warn(agent + " is expecting a CO2 price of " +
        // expectedCO2Price.get(market) + " Euro/MWh at timepoint "
        // + futureTimePoint + " in Market " + market);

        // logger.warn("Agent {}  found the expected prices to be {}", agent,
        // marketInformation.expectedElectricityPricesPerSegment);

        // logger.warn("Agent {}  found that the installed capacity in the market {} in future to be "
        // + marketInformation.capacitySum +
        // "and expectde maximum demand to be "
        // + marketInformation.maxExpectedLoad, agent, market);

        // Total capacity of producer

        double totalCapacity = 0d;

        totalCapacity = reps.powerPlantRepository.calculateCapacityOfOperationalPowerPlantsByOwner(agent,
                getCurrentTick());

        // logger.warn("Agent {} found total capacity to be " + totalCapacity,
        // agent);

        // logger.warn("Agent {} found total capacity to be " + totalCapacity);

        double highestValue = Double.MIN_VALUE;
        PowerGeneratingTechnology bestTechnology = null;

        for (PowerGeneratingTechnology technology : reps.genericRepository.findAll(PowerGeneratingTechnology.class)) {

            PowerPlant plant = new PowerPlant();
            // How to do location!!!
            plant.specifyNotPersist(getCurrentTick(), agent, getNodeForZone(market.getZone()), technology, null);

            // if too much capacity of this technology in the pipeline (not
            // limited to the 5 years)
            double expectedInstalledCapacityOfTechnology = reps.powerPlantRepository
                    .calculateCapacityOfExpectedOperationalPowerPlantsInMarketAndTechnology(market, technology,
                            futureTimePoint);
            PowerGeneratingTechnologyTarget technologyTarget = reps.powerGenerationTechnologyTargetRepository
                    .findOneByTechnologyAndMarket(technology, market);
            if (technologyTarget != null) {
                double technologyTargetCapacity = technologyTarget.getTrend().getValue(futureTimePoint);
                expectedInstalledCapacityOfTechnology = (technologyTargetCapacity > expectedInstalledCapacityOfTechnology) ? technologyTargetCapacity
                        : expectedInstalledCapacityOfTechnology;
            }
            // create variable to check if there was a recent failure and
            // technology can be used again

            if (getCurrentTick() >= technology.getLocationFailureTime()) {
                setLocationFailureTimerPositive(technology);
            }

            double pgtNodeLimit = Double.MAX_VALUE;
            PowerGeneratingTechnologyNodeLimit pgtLimit = reps.powerGeneratingTechnologyNodeLimitRepository
                    .findOneByTechnologyAndNode(technology, plant.getLocation());
            if (pgtLimit != null) {
                pgtNodeLimit = pgtLimit.getUpperCapacityLimit(futureTimePoint);
            }
            double expectedInstalledCapacityOfTechnologyInNode = reps.powerPlantRepository
                    .calculateCapacityOfExpectedOperationalPowerPlantsByNodeAndTechnology(plant.getLocation(),
                            technology, futureTimePoint);
            double expectedOwnedTotalCapacityInMarket = reps.powerPlantRepository
                    .calculateCapacityOfExpectedOperationalPowerPlantsInMarketByOwner(market, futureTimePoint, agent);
            double expectedOwnedCapacityInMarketOfThisTechnology = reps.powerPlantRepository
                    .calculateCapacityOfExpectedOperationalPowerPlantsInMarketByOwnerAndTechnology(market, technology,
                            futureTimePoint, agent);
            // logger.warn("Agent {} own capacity in market of technology {} to be"
            // + expectedOwnedCapacityInMarketOfThisTechnology2);
            // logger.warn("Agent {} total capacity of own " +
            // expectedOwnedTotalCapacityInMarket2);

            // double expectedownercapacity = reps.powerPlantRepository
            // .calculateCapacityOfExpectedOperationalPowerPlantsInMarketByOwner(market,
            // futureTimePoint, agent);
            // double ownermarketshare =
            // reps.powerPlantRepository.owneramountofplants(agent, technology);
            // double owneramountofplants =
            // reps.powerPlantRepository.countPowerPlantsByOwner(agent);
            double capacityOfTechnologyInPipeline = reps.powerPlantRepository
                    .calculateCapacityOfPowerPlantsByTechnologyInPipeline(technology, getCurrentTick());
            double operationalCapacityOfTechnology = reps.powerPlantRepository
                    .calculateCapacityOfOperationalPowerPlantsByTechnology(technology, getCurrentTick());
            double capacityInPipelineInMarket = reps.powerPlantRepository
                    .calculateCapacityOfPowerPlantsByMarketInPipeline(market, getCurrentTick());

            if ((expectedInstalledCapacityOfTechnology + plant.getActualNominalCapacity())
                    / (marketInformation.maxExpectedLoad + plant.getActualNominalCapacity()) > technology
                        .getMaximumInstalledCapacityFractionInCountry()) {
                // logger.warn(agent +
                // " will not invest in {} technology because there's too much of this type in the market",
                // technology);
            } else if ((expectedInstalledCapacityOfTechnologyInNode + plant.getActualNominalCapacity()) > pgtNodeLimit) {

            } else if (technology.getLocationFailure() == 0) {
                logger.warn(agent
                        + "will not invest in {} technology because there was no suitable location previously",
                        technology);

            } else if (plant.getLocation().getMaximumCcsInNode() - getPlacesLeftForCCS() <= 0) {
                logger.warn(agent + "will not invest in {} technology because there is no more capacity for CCS",
                        technology);
                setLocationFailureTimer(technology);

            } else if (expectedOwnedCapacityInMarketOfThisTechnology > expectedOwnedTotalCapacityInMarket
                    * technology.getMaximumInstalledCapacityFractionPerAgent()) {
                // logger.warn(agent +
                // " will not invest in {} technology because there's too much capacity planned by him",
                // technology);
            } else if (capacityInPipelineInMarket > 0.2 * marketInformation.maxExpectedLoad) {
                // logger.warn("Not investing because more than 20% of demand in pipeline.");

            } else if ((capacityOfTechnologyInPipeline > 2.0 * operationalCapacityOfTechnology)
                    && capacityOfTechnologyInPipeline > 9000) { // TODO:
                // reflects that you cannot expand a technology out of zero.
                // logger.warn(agent +
                // " will not invest in {} technology because there's too much capacity in the pipeline",
                // technology);
            } else if (plant.getActualInvestedCapital() * (1 - agent.getDebtRatioOfInvestments()) > agent
                    .getDownpaymentFractionOfCash() * agent.getCash()) {
                // logger.warn(agent +
                // " will not invest in {} technology as he does not have enough money for downpayment",
                // technology);
            } else {

                Map<Substance, Double> myFuelPrices = new HashMap<Substance, Double>();
                for (Substance fuel : technology.getFuels()) {
                    myFuelPrices.put(fuel, expectedFuelPrices.get(fuel));
                }
                Set<SubstanceShareInFuelMix> fuelMix = calculateFuelMix(plant, myFuelPrices,
                        expectedCO2Price.get(market));
                plant.setFuelMix(fuelMix);

                double expectedMarginalCost = determineExpectedMarginalCost(plant, expectedFuelPrices,
                        expectedCO2Price.get(market));
                double runningHours = 0d;
                double expectedGrossProfit = 0d;

                long numberOfSegments = reps.segmentRepository.count();

                // TODO somehow the prices of long-term contracts could also
                // be used here to determine the expected profit. Maybe not
                // though...
                for (SegmentLoad segmentLoad : market.getLoadDurationCurve()) {
                    double expectedElectricityPrice = marketInformation.expectedElectricityPricesPerSegment
                            .get(segmentLoad.getSegment());
                    double hours = segmentLoad.getSegment().getLengthInHours();
                    if (expectedMarginalCost <= expectedElectricityPrice) {
                        runningHours += hours;
                        expectedGrossProfit += (expectedElectricityPrice - expectedMarginalCost)
                                * hours
                                * plant.getAvailableCapacity(futureTimePoint, segmentLoad.getSegment(),
                                        numberOfSegments);
                    }
                }

                // logger.warn(agent +
                // "expects technology {} to have {} running", technology,
                // runningHours);
                // expect to meet minimum running hours?
                if (runningHours < plant.getTechnology().getMinimumRunningHours()) {
                    // logger.warn(agent+
                    // " will not invest in {} technology as he expect to have {} running, which is lower then required",
                    // technology, runningHours);
                } else {

                    double fixedOMCost = calculateFixedOperatingCost(plant);// /
                    // plant.getActualNominalCapacity();

                    double operatingProfit = expectedGrossProfit - fixedOMCost;

                    // TODO Alter discount rate on the basis of the amount
                    // in long-term contracts?
                    // TODO Alter discount rate on the basis of other stuff,
                    // such as amount of money, market share, portfolio
                    // size.

                    // total amount of technology x installed and calculation of
                    // market share

                    double technologyCapacityTotalAgent = calculateTechnologyMarketShare(agent, technology,
                            getCurrentTick());

                    // logger.warn("Agent {} found capacity of {} technology to be "
                    // + TechnologyCapacityTotalAgent,
                    // agent, technology);

                    // logger.warn("Agent {} found capacity of {} technology to be "
                    // + TechnologyCapacityTotalAgent);

                    double MarketShareTechnology = technologyCapacityTotalAgent / totalCapacity;

                    // logger.warn("Agent {} found that the marketshare for technology {} to be "
                    // + MarketShareTechnology,
                    // agent, technology);

                    // logger.warn("Agent {} found that the marketshare for technology {} to be "
                    // + MarketShareTechnology);

                    // Calculation of weighted average cost of capital,
                    // based on the companies debt-ratio

                    // portfolio dependency and other impact on npv for choice
                    // of location

                    double multiFactorWacc = 1;

                    if (MarketShareTechnology == 0) {
                        multiFactorWacc = agent.getLearningEffectNegative();
                    } else if (MarketShareTechnology > 0.4) {
                        multiFactorWacc = agent.getLearningEffectPositive();
                    } else {
                        multiFactorWacc = 1;
                    }

                    double wacc = ((1 - agent.getDebtRatioOfInvestments()) * agent.getEquityInterestRate() + agent
                            .getDebtRatioOfInvestments() * agent.getLoanInterestRate())
                            * multiFactorWacc;

                    // logger.warn("Agent {} found for technology {} the factor"
                    // + multiFactorWacc, agent, technology);

                    // Creation of out cash-flow during power plant building
                    // phase (note that the cash-flow is negative!)
                    TreeMap<Integer, Double> discountedProjectCapitalOutflow = calculateSimplePowerPlantInvestmentCashFlow(
                            technology.getDepreciationTime(), (int) plant.getActualLeadtime(),
                            plant.getActualInvestedCapital(), 0);

                    // Delayed NPV

                    TreeMap<Integer, Double> discountedProjectCapitalOutflowDelay = calculateSimplePowerPlantInvestmentCashFlow(
                            technology.getDepreciationTime(), (int) plant.getActualLeadtime2(),
                            plant.getActualInvestedCapitalDelay(), 0);

                    // Creation of in cashflow during operation
                    TreeMap<Integer, Double> discountedProjectCashInflow = calculateSimplePowerPlantInvestmentCashFlow(
                            technology.getDepreciationTime(), (int) plant.getActualLeadtime(), 0, operatingProfit);

                    // Delayed NPV

                    TreeMap<Integer, Double> discountedProjectCashInflowDelay = calculateSimplePowerPlantInvestmentCashFlow(
                            technology.getDepreciationTime(), (int) plant.getActualLeadtime2(), 0, operatingProfit);

                    // logger.warn("Agent {} found that his marketshare for technology {} to be"
                    // + expectedOwnedCapacityInMarketOfThisTechnology2, agent);

                    double discountedCapitalCosts = npv(discountedProjectCapitalOutflow, wacc);

                    double discountedCapitalCostsDelay = npv(discountedProjectCapitalOutflowDelay, wacc);

                    // are
                    // defined
                    // negative!!
                    // plant.getActualNominalCapacity();

                    // logger.warn("Agent {}  found that the discounted capital for technology {} to be "
                    // + discountedCapitalCosts, agent, technology);

                    // logger.warn("Agent {} DELAY found that the discounted capital for technology {} to be "
                    // + discountedCapitalCostsDelay, agent, technology);

                    double discountedOpProfit = npv(discountedProjectCashInflow, wacc);

                    double discountedOpProfitDelay = npv(discountedProjectCashInflowDelay, wacc);

                    // logger.warn("Agent {}  found that the projected discounted inflows for technology {} to be "
                    // + discountedOpProfit, agent, technology);

                    // logger.warn("Agent {} DELAY found that the projected discounted inflows for technology {} to be "
                    // + discountedOpProfitDelay, agent, technology);

                    double projectValue = discountedOpProfit + discountedCapitalCosts;
                    double projectValueDelay = discountedOpProfitDelay + discountedCapitalCostsDelay;

                    // logger.warn(
                    // "Agent {}  found the project value for technology {} to be "
                    // + Math.round(projectValue /
                    // plant.getActualNominalCapacity())
                    // + " EUR/MW (running hours: " + runningHours + "", agent,
                    // technology);
                    // logger.warn(
                    // "Agent {}  found DELAY the project value for technology {} to be "
                    // + Math.round(projectValueDelay /
                    // plant.getActualNominalCapacity())
                    // + " EUR/MW (running hours: " + runningHours + "", agent,
                    // technology);
                    // double projectTotalValue = projectValuePerMW *
                    // plant.getActualNominalCapacity();

                    // double projectReturnOnInvestment = discountedOpProfit
                    // / (-discountedCapitalCosts);

                    /*
                     * Divide by capacity, in order not to favour large power
                     * plants (which have the single largest NPV
                     */

                    if (projectValue > 0 && projectValue / plant.getActualNominalCapacity() > highestValue) {
                        highestValue = projectValue / plant.getActualNominalCapacity();
                        technology.setNpv(projectValue);
                        technology.setNpvDelay(projectValueDelay);
                        bestTechnology = plant.getTechnology();
                    }
                }

            }
        }

        // Location site identification for technology of choice bestTechnology

        // top 3 locations initialization
        Location locationrank1 = null;
        Location locationrank2 = null;
        Location locationrank3 = null;

        // For all location check if they are suitable for a certain technology
        // and have room for a new plant if so calculate utility and rank
        // location top 3 according to utility
        for (Location siteLocation : reps.genericRepository.findAll(Location.class)) {
            if (bestTechnology != null) {
                if (bestTechnology.getName().equals("coalPulverizedSuperCritical")
                        || bestTechnology.getName().equals("lignitePGT")
                        || bestTechnology.getName().equals("coalPscCSS")) {
                    if (siteLocation.isFeedstockAvailabilityCoal() == false) {
                        logger.warn("location {} is not suitable for technology " + bestTechnology,
                                siteLocation.getName());
                    } else if (siteLocation.getPossiblePlants()
                            - calculateNumberOfPlantsAtLocation(siteLocation, getCurrentTick()) <= 0) {
                        logger.warn("location {} is not suitable for technology " + bestTechnology,
                                siteLocation.getName());
                    } else if (bestTechnology.getName().equals("coalPscCSS")) {
                        if (siteLocation.isCarbonCaptureStorageAvailability() == false) {
                            logger.warn("location {} is not suitable for technology as no CCS available"
                                    + bestTechnology, siteLocation);
                        }
                    } else {
                        siteLocation.setUtilityLocation(calculateAndSetUtilityLocation(agent, siteLocation));
                    }
                }

                if (bestTechnology.getName().equals("biomassPGT")) {
                    if (siteLocation.isFeedstockAvailabilityCoal() == false) {
                        logger.warn("location {} is not suitable for technology " + bestTechnology,
                                siteLocation.getName());
                    } else if (siteLocation.getPossiblePlants()
                            - calculateNumberOfPlantsAtLocation(siteLocation, getCurrentTick()) <= 0) {
                        logger.warn("location {} is not suitable for technology " + bestTechnology,
                                siteLocation.getName());
                    } else {
                        siteLocation.setUtilityLocation(calculateAndSetUtilityLocation(agent, siteLocation));
                    }
                }
                if (bestTechnology.getName().equals("IGCC") || bestTechnology.getName().equals("IgccCCS")
                        || bestTechnology.getName().equals("CCGT") || bestTechnology.getName().equals("CcgtCCS")
                        || bestTechnology.getName().equals("OCGT")) {
                    if (siteLocation.isFeedstockAvailabilityGas() == false) {
                        logger.warn("location {} is not suitable for technology " + bestTechnology,
                                siteLocation.getName());
                    } else if (siteLocation.getPossiblePlants()
                            - calculateNumberOfPlantsAtLocation(siteLocation, getCurrentTick()) <= 0) {
                        logger.warn("location {} is not suitable for technology " + bestTechnology,
                                siteLocation.getName());
                    } else if (bestTechnology.getName().equals("IgccCCS") || bestTechnology.getName().equals("CcgtCCS")) {
                        if (siteLocation.isCarbonCaptureStorageAvailability() == false) {
                            logger.warn(
                                    "location {} is not suitable for technology as no CCS available" + siteLocation,
                                    bestTechnology);
                        }
                    } else {
                        siteLocation.setUtilityLocation(calculateAndSetUtilityLocation(agent, siteLocation));
                    }
                }
                if (bestTechnology.getName().equals("Wind")) {
                    if (siteLocation.isFeedstockAvailabilityWind() == false) {
                        logger.warn("location {} is not suitable for technology " + bestTechnology,
                                siteLocation.getName());
                    } else if (siteLocation.getPossiblePlants()
                            - calculateNumberOfPlantsAtLocation(siteLocation, getCurrentTick()) <= 0) {
                        logger.warn("location {} is not suitable for technology " + bestTechnology,
                                siteLocation.getName());
                    } else {
                        siteLocation.setUtilityLocation(calculateAndSetUtilityLocationWind(agent, siteLocation));
                    }
                }
                if (bestTechnology.getName().equals("WindOffshore")) {
                    if (siteLocation.isOffShore() == false) {
                        logger.warn("location {} is not suitable for technology " + bestTechnology,
                                siteLocation.getName());
                    } else if (siteLocation.getPossiblePlants()
                            - calculateNumberOfPlantsAtLocation(siteLocation, getCurrentTick()) <= 0) {
                        logger.warn("location {} is not suitable for technology " + bestTechnology,
                                siteLocation.getName());
                    } else {
                        siteLocation
                                .setUtilityLocation(calculateAndSetUtilityLocationWindOffShore(agent, siteLocation));
                    }
                }
                if (bestTechnology.getName().equals("Photovoltaic")) {
                    if (siteLocation.isFeedstockAvailabilitySun() == false) {
                        logger.warn("location {} is not suitable for technology " + bestTechnology,
                                siteLocation.getName());
                    } else if (siteLocation.getPossiblePlants()
                            - calculateNumberOfPlantsAtLocation(siteLocation, getCurrentTick()) <= 0) {
                        logger.warn("location {} is not suitable for technology " + bestTechnology,
                                siteLocation.getName());
                    } else {
                        siteLocation.setUtilityLocation(calculateAndSetUtilityLocationSun(agent, siteLocation));
                    }
                }
                if (bestTechnology.getName().equals("Nuclear")) {
                    if (siteLocation.isFeedstockAvailabilityNuclear() == false) {
                        logger.warn("location {} is not suitable for technology " + bestTechnology,
                                siteLocation.getName());
                    } else if (siteLocation.getPossiblePlants()
                            - calculateNumberOfPlantsAtLocation(siteLocation, getCurrentTick()) <= 0) {
                        logger.warn("location {} is not suitable for technology " + bestTechnology,
                                siteLocation.getName());
                    } else {

                        siteLocation.setUtilityLocation(calculateAndSetUtilityLocation(agent, siteLocation));
                    }
                }
            }
            if (siteLocation.getUtilityLocation() != 0) {
                if (locationrank1 != null) {
                    if (siteLocation.getUtilityLocation() > locationrank1.getUtilityLocation()) {
                        locationrank3 = locationrank2;
                        locationrank2 = locationrank1;
                        locationrank1 = siteLocation;
                    } else if (siteLocation.getUtilityLocation() > locationrank2.getUtilityLocation()) {
                        locationrank3 = locationrank2;
                        locationrank2 = siteLocation;

                    } else if (siteLocation.getUtilityLocation() > locationrank3.getUtilityLocation()) {
                        locationrank3 = siteLocation;
                    } else {
                        logger.warn(siteLocation.getName() + " location has to low utility compared to top 3");
                    }
                } else {
                    locationrank1 = siteLocation;
                    locationrank2 = siteLocation;
                    locationrank3 = siteLocation;
                }
            }
        }

        if (locationrank1 != null) {
            logger.warn(agent + " found locations for technology and moves to permit procedure");
        } else {
            setLocationFailureTimer(bestTechnology);
        }

        // Permit procedure, opposition calculations locations and payoff using
        // nucleolus theories
        boolean LocationChosen = false;

        // TODO still to add weightfactors!!!
        // create empty chosen location

        Location ChosenLocation = null;
        if (locationrank1 != null) {
            logger.warn(agent + "started permit negotiation for technology {} at Location {}", bestTechnology,
                    locationrank1);
            while (LocationChosen = false) {
                // create variables for the utility functions
                double compensationGovernment = 0d;
                double compensationLocals = 0d;
                double compensationElectricityProducer = 0d;
                double utilityElectricityProducer = 0d;

                // empty authorized government
                LocalGovernment AuthorizedGovernment = null;

                // to add previous experience!!!
                // linking to right government
                for (LocalGovernment localgov : reps.genericRepository.findAll(LocalGovernment.class)) {
                    if (localgov.getName().equals(locationrank1.getProvince())) {
                        AuthorizedGovernment = localgov;
                    }
                }

                logger.warn(agent + "has to deal with the province of {} ", AuthorizedGovernment.getName());

                int plantsOfTechnology = getAmountofPlantsinProvinceforTechnology(AuthorizedGovernment, bestTechnology,
                        getCurrentTick());

                logger.warn(AuthorizedGovernment.getName() + "has {} plants of the technology in its area",
                        plantsOfTechnology);

                // Environmental compensation to Local government

                double UtilityGovernment = calculateAndSetUtilityGovernment(AuthorizedGovernment, bestTechnology,
                        compensationGovernment, plantsOfTechnology);

                logger.warn(AuthorizedGovernment.getName() + "Has an utility for the proposed plant of {} ",
                        UtilityGovernment);
                // compensation payments until local government is no worse off
                // than when nothing would have been build
                while (UtilityGovernment <= 0) {
                    compensationGovernment += 10000;
                    compensationElectricityProducer -= 10000;
                    UtilityGovernment = calculateAndSetUtilityGovernment(AuthorizedGovernment, bestTechnology,
                            compensationGovernment, plantsOfTechnology);
                    utilityElectricityProducer = (bestTechnology.getNpv() - compensationElectricityProducer);
                }

                logger.warn(AuthorizedGovernment.getName() + "Has received {} as compensation", compensationGovernment);

                // delete all old local parties from previous negotiations
                // (Review this with expert!)
                // reps.locationLocalPartiesRepository.deleteAll();

                // generate random number of inhabitants based on normal
                // distribution and environmental factors
                if (locationrank1.isOffShore() != false) {
                    Random rand = new Random();
                    double normalDistribution = rand.nextGaussian();
                    double SigmaNormalDistribution = (((locationrank1.getPopulationDensity() - 21) / 6110) * 3
                            + ((locationrank1.getWealth() - 10.8) / 12.4) * 3 + ((bestTechnology
                            .getTechnologyPreference() - 61) / 57) * 4);
                    double AmountOfLocals = Math.abs(Math.floor(normalDistribution * SigmaNormalDistribution));
                    // create empty list of local parties
                    ArrayList<LocationLocalParties> listLocals = new ArrayList<LocationLocalParties>();
                    // create local parties
                    logger.warn(agent + "encountered a number of local parties of {}", AmountOfLocals);

                    double investmentCost = bestTechnology.getInvestmentCost(getCurrentTick())
                            * bestTechnology.getCapacity();

                    // Utility compensation for locals will be based on s
                    // function
                    // 1/1+exp(-x) which will be forced in the interval -10, 10
                    // to
                    // be a nice s shaped function

                    for (int i = 0; i < AmountOfLocals; i++) {
                        LocationLocalParties local = new LocationLocalParties();
                        local.setName("Party" + i);
                        local.setUtilityLocalParty(calculateAndSetUtilityLocalParty(bestTechnology, locationrank1,
                                compensationLocals, investmentCost));
                        logger.warn(local.getName() + "Has Utility of {}", local.getUtilityLocalParty());
                        listLocals.add(local);
                    }
                    // reduce ArrayList to the amount of objects in there, in
                    // the
                    // case there are less than 10 spots
                    // listLocals.trimToSize(); not needed according to Emile

                    // Average utility of local parties to get impression of
                    // risk
                    // and attitude of the group
                    // TODO !!!
                    double averageUtilityLocals = 0d;
                    double averageUtility = 0d;
                    LocationLocalParties minLocalUtility = null;

                    while (utilityElectricityProducer > 0 || agent.getRiskAcceptance() <= averageUtility) {
                        for (LocationLocalParties local : listLocals) {
                            if (minLocalUtility != null) {
                                if (local.getUtilityLocalParty() <= minLocalUtility.getUtilityLocalParty()) {
                                    minLocalUtility = local;
                                    listLocals.remove(local);
                                }
                            } else {
                                minLocalUtility = local;
                                listLocals.remove(local);
                            }
                        }

                        minLocalUtility.setCompensationLocalParty(minLocalUtility.getCompensationLocalParty() + 1000);
                        compensationElectricityProducer = compensationElectricityProducer - 1000;
                        minLocalUtility.setUtilityLocalParty(calculateAndSetUtilityLocalParty(bestTechnology,
                                locationrank1, compensationLocals, investmentCost));
                        listLocals.add(minLocalUtility);

                        // update utility function electricity producer, based
                        // on
                        // npv from technology and maximum delay possible
                        utilityElectricityProducer = (((bestTechnology.getNpv() - compensationElectricityProducer)
                                - (bestTechnology.getNpv() - (averageUtilityLocals * (locationrank1.getCourtChance() * bestTechnology
                                        .getNpvDelay()))) - 0) / (bestTechnology.getNpv()));
                        // test iterator!!!
                        logger.warn(agent + "payed compensation of {} to local party to smooth the process",
                                minLocalUtility.getCompensationLocalParty());

                        // Iterator<LocationLocalParties> iteratorlocallist =
                        // listLocals.iterator();
                        // while (iteratorlocallist.hasNext()) {
                        // LocationLocalParties locals =
                        // iteratorlocallist.next();
                        // AverageUtilityLocals = AverageUtilityLocals +
                        // locals.getUtilityLocalParty();
                        //
                        // }

                        for (LocationLocalParties party : listLocals) {
                            party.getUtilityLocalParty();
                            averageUtilityLocals += party.getUtilityLocalParty();
                        }
                        averageUtility = averageUtilityLocals / listLocals.size();
                        locationrank1.setAverageUtility(averageUtility);
                        agent.setCompensationElectricityProducer(compensationElectricityProducer);

                    }
                    logger.warn(averageUtility + "is now the average utility of the locals");
                }

                if (utilityElectricityProducer > 0) {
                    LocationChosen = true;
                    ChosenLocation = locationrank1;
                } else {
                    locationrank1 = locationrank2;
                    locationrank2 = locationrank3;

                    if (locationrank2.equals(locationrank3)) {
                        locationrank3 = null;
                    }
                    if (locationrank1.equals(locationrank2)) {
                        locationrank2 = null;
                    }
                }

            }
        } else {
            logger.warn(agent + " did not invest in technology {} , because the permit negotiation failed",
                    bestTechnology);

            setLocationFailureTimer(bestTechnology);
        }

        // permit procedure should be fitted here

        if (bestTechnology != null && LocationChosen != false) {
            // logger.warn("Agent {} invested in technology {} at tick " +
            // getCurrentTick(), agent, bestTechnology);
            logger.warn(agent + "invested in technology {} at location {}", bestTechnology, ChosenLocation.getName());

            PowerPlant plant = new PowerPlant();
            plant.specifyAndPersist(getCurrentTick(), agent, getNodeForZone(market.getZone()), bestTechnology,
                    ChosenLocation);
            PowerPlantManufacturer manufacturer = reps.genericRepository.findFirst(PowerPlantManufacturer.class);
            BigBank bigbank = reps.genericRepository.findFirst(BigBank.class);

            // for (Location siteLocation :
            // reps.genericRepository.findAll(Location.class)) {
            // if (ChosenLocation.getName().equals(siteLocation.getName())) {
            // siteLocation.setPlantPresent(siteLocation.getPlantPresent() + 1);
            // }
            // }
            double chanceOnDelay = Math.random();
            if (chanceOnDelay > ((ChosenLocation.getAverageUtility() / 100) * -1)) {
                chanceOnDelay = 0;
            }

            double additionalCostPermit = chanceOnDelay * (bestTechnology.getNpv() - bestTechnology.getNpvDelay())
                    + agent.getCompensationElectricityProducer();

            double investmentCostPayedByEquity = (plant.getActualInvestedCapital() + additionalCostPermit)
                    * (1 - agent.getDebtRatioOfInvestments());
            double investmentCostPayedByDebt = (plant.getActualInvestedCapital() + additionalCostPermit)
                    * agent.getDebtRatioOfInvestments();
            double downPayment = investmentCostPayedByEquity;
            createSpreadOutDownPayments(agent, manufacturer, downPayment, plant);

            double amount = determineLoanAnnuities(investmentCostPayedByDebt, plant.getTechnology()
                    .getDepreciationTime(), agent.getLoanInterestRate());
            // logger.warn("Loan amount is: " + amount);
            Loan loan = reps.loanRepository.createLoan(agent, bigbank, amount, plant.getTechnology()
                    .getDepreciationTime(), getCurrentTick(), plant);
            // Create the loan
            plant.createOrUpdateLoan(loan);

        } else {
            // logger.warn("{} found no suitable technology anymore to invest in at tick "
            // + getCurrentTick(), agent);
            // agent will not participate in the next round of investment if
            // he does not invest now
            setNotWillingToInvest(agent);
        }
    }

    // }

    // Creates n downpayments of equal size in each of the n building years of a
    // power plant
    @Transactional
    private void createSpreadOutDownPayments(EnergyProducer agent, PowerPlantManufacturer manufacturer,
            double totalDownPayment, PowerPlant plant) {
        int buildingTime = (int) plant.getActualLeadtime();
        reps.nonTransactionalCreateRepository.createCashFlow(agent, manufacturer, totalDownPayment / buildingTime,
                CashFlow.DOWNPAYMENT, getCurrentTick(), plant);
        Loan downpayment = reps.loanRepository.createLoan(agent, manufacturer, totalDownPayment / buildingTime,
                buildingTime - 1, getCurrentTick(), plant);
        plant.createOrUpdateDownPayment(downpayment);
    }

    @Transactional
    private void setNotWillingToInvest(EnergyProducer agent) {
        agent.setWillingToInvest(false);
    }

    @Transactional
    private void setLocationFailureTimer(PowerGeneratingTechnology bestTechnology) {
        for (PowerGeneratingTechnology technology : reps.genericRepository.findAll(PowerGeneratingTechnology.class)) {
            if (bestTechnology.getName().equals(technology.getName())) {
                technology.setLocationFailure(0);
                technology.setLocationFailureTime(getCurrentTick() + 5);
            }
        }
    }

    @Transactional
    private void setLocationFailureTimerPositive(PowerGeneratingTechnology technology) {
        technology.setLocationFailure(1);
        technology.setLocationFailureTime(0);
    }

    /**
     * Predicts fuel prices for {@link futureTimePoint} using a geometric trend
     * regression forecast. Only predicts fuels that are traded on a commodity
     * market.
     * 
     * @param agent
     * @param futureTimePoint
     * @return Map<Substance, Double> of predicted prices.
     */
    public Map<Substance, Double> predictFuelPrices(EnergyProducer agent, long futureTimePoint) {
        // Fuel Prices
        Map<Substance, Double> expectedFuelPrices = new HashMap<Substance, Double>();
        for (Substance substance : reps.substanceRepository.findAllSubstancesTradedOnCommodityMarkets()) {
            // Find Clearing Points for the last 5 years (counting current year
            // as one of the last 5 years).
            Iterable<ClearingPoint> cps = reps.clearingPointRepository
                    .findAllClearingPointsForSubstanceTradedOnCommodityMarkesAndTimeRange(substance, getCurrentTick()
                            - (agent.getNumberOfYearsBacklookingForForecasting() - 1), getCurrentTick());
            // logger.warn("{}, {}",
            // getCurrentTick()-(agent.getNumberOfYearsBacklookingForForecasting()-1),
            // getCurrentTick());
            // Create regression object
            GeometricTrendRegression gtr = new GeometricTrendRegression();
            for (ClearingPoint clearingPoint : cps) {
                // logger.warn("CP {}: {} , in" + clearingPoint.getTime(),
                // substance.getName(), clearingPoint.getPrice());
                gtr.addData(clearingPoint.getTime(), clearingPoint.getPrice());
            }
            expectedFuelPrices.put(substance, gtr.predict(futureTimePoint));
            // logger.warn("Forecast {}: {}, in Step " + futureTimePoint,
            // substance, expectedFuelPrices.get(substance));
        }
        return expectedFuelPrices;
    }

    // Create a powerplant investment and operation cash-flow in the form of a
    // map. If only investment, or operation costs should be considered set
    // totalInvestment or operatingProfit to 0
    private TreeMap<Integer, Double> calculateSimplePowerPlantInvestmentCashFlow(int depriacationTime,
            int buildingTime, double totalInvestment, double operatingProfit) {
        TreeMap<Integer, Double> investmentCashFlow = new TreeMap<Integer, Double>();
        double equalTotalDownPaymentInstallement = totalInvestment / buildingTime;
        for (int i = 0; i < buildingTime; i++) {
            investmentCashFlow.put(new Integer(i), -equalTotalDownPaymentInstallement);
        }
        for (int i = buildingTime; i < depriacationTime + buildingTime; i++) {
            investmentCashFlow.put(new Integer(i), operatingProfit);
        }

        return investmentCashFlow;
    }

    private double npv(TreeMap<Integer, Double> netCashFlow, double wacc) {
        double npv = 0;
        for (Integer iterator : netCashFlow.keySet()) {
            npv += netCashFlow.get(iterator).doubleValue() / Math.pow(1 + wacc, iterator.intValue());
        }
        return npv;
    }

    public double determineExpectedMarginalCost(PowerPlant plant, Map<Substance, Double> expectedFuelPrices,
            double expectedCO2Price) {
        double mc = determineExpectedMarginalFuelCost(plant, expectedFuelPrices);
        double co2Intensity = plant.calculateEmissionIntensity();
        mc += co2Intensity * expectedCO2Price;
        return mc;
    }

    public double determineExpectedMarginalFuelCost(PowerPlant powerPlant, Map<Substance, Double> expectedFuelPrices) {
        double fc = 0d;
        for (SubstanceShareInFuelMix mix : powerPlant.getFuelMix()) {
            double amount = mix.getShare();
            double fuelPrice = expectedFuelPrices.get(mix.getSubstance());
            fc += amount * fuelPrice;
        }
        return fc;
    }

    private PowerGridNode getNodeForZone(Zone zone) {
        for (PowerGridNode node : reps.genericRepository.findAll(PowerGridNode.class)) {
            if (node.getZone().equals(zone)) {
                return node;
            }
        }
        return null;
    }

    public double getPlacesLeftForCCS() {
        double ccsPlants = 0d;

        for (PowerPlant pp : reps.genericRepository.findAll(PowerPlant.class)) {
            if (pp.getTechnology().getName().equals("IgccCCS") || pp.getTechnology().getName().equals("CcgtCCS")) {
                ccsPlants += 1;
            } else if (pp.getTechnology().getName().equals("CoalPscCSS")) {
                ccsPlants += 2;
            }
        }

        // for (PowerGeneratingTechnology tech :
        // reps.genericRepository.findAll(PowerGeneratingTechnology.class)) {

        // if (tech.getName().equals("CoalPscCSS") ||
        // tech.getName().equals("IgccCCS")
        // || tech.getName().equals("CcgtCCS")) {
        // double numberPlants =
        // reps.powerPlantRepository.countPowerPlantsBytechnology(tech);
        // if (tech.getName().equals("CoalPscCCS")) {
        // numberPlants = 2 * numberPlants;
        // }
        // ccsPlants += numberPlants;
        // }
        // }

        return ccsPlants;
    }

    public double calculateAndSetUtilityLocation(EnergyProducer agent, Location siteLocation) {
        double utilityLocationUpForValuation = (((siteLocation.getPopulationDensity() - 6131) / 6110) * -1
                * agent.getWeightFactorDensity() + (((siteLocation.getWealth() - 23.2) / 12.4)) * -1
                * agent.getWeightFactorWealth() + (((siteLocation.getDistanceGrid() - 69) / 69)) * -1
                * agent.getWeightFactorDistance() + ((siteLocation.getQualityWater() - 1) / 2)
                * agent.getWeightFactorFeedstock());
        return utilityLocationUpForValuation;
    }

    public double calculateAndSetUtilityLocationWind(EnergyProducer agent, Location siteLocation) {
        double utilityLocationUpForValuation = (((siteLocation.getPopulationDensity() - 6131) / 6110) * -1
                * agent.getWeightFactorDensity() + (((siteLocation.getWealth() - 23.2) / 12.4)) * -1
                * agent.getWeightFactorWealth() + (((siteLocation.getDistanceGrid() - 69) / 69)) * -1
                * agent.getWeightFactorDistance() + ((siteLocation.getWindPower() - 3.5) / 10.24)
                * agent.getWeightFactorFeedstock());
        return utilityLocationUpForValuation;
    }

    public double calculateAndSetUtilityLocationWindOffShore(EnergyProducer agent, Location siteLocation) {
        double utilityLocationUpForValuation = (((siteLocation.getWindPower() - 3.5) / 10.24)
                * agent.getWeightFactorFeedstock() + ((siteLocation.getDepthWater() - 38) / 21) * -1
                * agent.getWeightFactorDepthWater() + ((siteLocation.getDistanceShore() - 75) / 60) * -1
                * agent.getWeightFactorDistanceShore());
        return utilityLocationUpForValuation;
    }

    public double calculateAndSetUtilityLocationSun(EnergyProducer agent, Location siteLocation) {
        double utilityLocationUpForValuation = (((siteLocation.getPopulationDensity() - 6131) / 6110) * -1
                * agent.getWeightFactorDensity() + (((siteLocation.getWealth() - 23.2) / 12.4)) * -1
                * agent.getWeightFactorWealth() + (((siteLocation.getDistanceGrid() - 69) / 69)) * -1
                * agent.getWeightFactorDistance() + ((siteLocation.getSunHours() - 1500) / 300)
                * agent.getWeightFactorFeedstock());
        return utilityLocationUpForValuation;
    }

    public double calculateAndSetUtilityGovernment(LocalGovernment AuthorizedGovernment,
            PowerGeneratingTechnology bestTechnology, double compensationGovernment, int plantsOfTechnology) {
        double utilityGovernment = 0d;
        if (bestTechnology.getName().equals("Wind") || bestTechnology.getName().equals("WindOffshore")) {
            utilityGovernment = (((bestTechnology.getEnvironmentalCosts() - 0.16) / (24.14)) * -1
                    * AuthorizedGovernment.getWeightEnvironment() + ((bestTechnology.getEmployment() - 0.379) / 2.843)
                    * AuthorizedGovernment.getWeightEmployment() + ((plantsOfTechnology - 0) / 25) * -1
                    * AuthorizedGovernment.getWeightPrevious() + (compensationGovernment / (bestTechnology
                    .getInvestmentCost(getCurrentTick()) * bestTechnology.getCapacity() * 0.1))
                    * AuthorizedGovernment.getWeightCompensation());
        } else {
            utilityGovernment = (((bestTechnology.getEnvironmentalCosts() - 0.16) / (24.14)) * -1
                    * AuthorizedGovernment.getWeightEnvironment() + ((bestTechnology.getEmployment() - 0.379) / 2.843)
                    * AuthorizedGovernment.getWeightEmployment() + ((plantsOfTechnology - 0) / 5) * -1
                    * AuthorizedGovernment.getWeightPrevious() + (compensationGovernment / (bestTechnology
                    .getInvestmentCost(getCurrentTick()) * bestTechnology.getCapacity() * 0.1))
                    * AuthorizedGovernment.getWeightCompensation());
        }
        return utilityGovernment;
    }

    public double calculateAndSetUtilityLocalParty(PowerGeneratingTechnology tech, Location site,
            double compensationLocals, double investmentCost) {
        Random randomnumber = new Random();
        double utilityLocals = Math.abs(0.25 * randomnumber.nextGaussian() + 1)
                * (((site.getPopulationDensity() - 21) / 6110) * -1 * site.getWeightFactorDensity()
                        + ((site.getWealth() - 10.8) / 12.4) * -1 * site.getWeightFactorWealth()
                        + ((tech.getTechnologyPreference() - 61) / 57) * -1 * site.getWeightFactorTechPref() + ((100 / (1 + Math
                        .exp(-(((compensationLocals) / (investmentCost * 0.05)) * 20) - 10))) * site
                        .getWeightFactorCompensation()));
        return utilityLocals;
    }

    public int getAmountofPlantsinProvinceforTechnology(LocalGovernment AuthorizedGovernment,
            PowerGeneratingTechnology bestTechnology, long time) {
        int AmountOfPlants = 0;
        for (Location ppLocation : reps.genericRepository.findAll(Location.class)) {
            if (ppLocation.getProvince().equals(AuthorizedGovernment.getName())) {

                for (PowerPlant plant : reps.powerPlantRepository
                        .findOperationalPowerPlantsByLocation(ppLocation, time)) {

                    if (bestTechnology.getFeedstockID().equals(plant.getTechnology().getFeedstockID())) {
                        AmountOfPlants += 1;
                    } else {

                    }
                }
            }
        }

        return AmountOfPlants;
    }

    public double calculateTechnologyMarketShare(EnergyProducer producer, PowerGeneratingTechnology technology,
            long time) {

        String i = technology.getName();
        double technologyCapacity = 0d;

        for (PowerPlant plant : reps.powerPlantRepository.findOperationalPowerPlantsByOwner(producer, time)) {

            if (plant.getTechnology().getName().equals(i)) {

                technologyCapacity += plant.getActualNominalCapacity();

            } else {

            }

        }

        return technologyCapacity;
    }

    public double calculateNumberOfPlantsAtLocation(Location siteLocation, long time) {
        String i = siteLocation.getName();
        double AmountOfPlantsAtLocation = 0d;

        for (PowerPlant plant : reps.powerPlantRepository.findOperationalPowerPlantsByLocation(siteLocation, time)) {
            if (plant.getSiteLocation().getName().equals(i)) {
                AmountOfPlantsAtLocation += 1;
            } else {

            }
        }

        return AmountOfPlantsAtLocation;
    }

    private class MarketInformation {

        Map<Segment, Double> expectedElectricityPricesPerSegment;
        double maxExpectedLoad = 0d;
        Map<PowerPlant, Double> meritOrder;
        double capacitySum;

        MarketInformation(ElectricitySpotMarket market, Map<ElectricitySpotMarket, Double> expectedDemand,
                Map<Substance, Double> fuelPrices, double co2price, long time) {
            // determine expected power prices
            expectedElectricityPricesPerSegment = new HashMap<Segment, Double>();
            Map<PowerPlant, Double> marginalCostMap = new HashMap<PowerPlant, Double>();
            capacitySum = 0d;

            // get merit order for this market
            for (PowerPlant plant : reps.powerPlantRepository.findExpectedOperationalPowerPlantsInMarket(market, time)) {

                double plantMarginalCost = determineExpectedMarginalCost(plant, fuelPrices, co2price);
                marginalCostMap.put(plant, plantMarginalCost);
                capacitySum += plant.getActualNominalCapacity();
            }

            // get difference between technology target and expected operational
            // capacity

            // At this moment no Location present!!! used null
            for (PowerGeneratingTechnologyTarget pggt : reps.powerGenerationTechnologyTargetRepository
                    .findAllByMarket(market)) {
                double expectedTechnologyCapacity = reps.powerPlantRepository
                        .calculateCapacityOfExpectedOperationalPowerPlantsInMarketAndTechnology(market,
                                pggt.getPowerGeneratingTechnology(), time);
                double targetDifference = pggt.getTrend().getValue(time) - expectedTechnologyCapacity;
                if (targetDifference > 0) {
                    PowerPlant plant = new PowerPlant();
                    plant.specifyNotPersist(getCurrentTick(), new EnergyProducer(),
                            reps.powerGridNodeRepository.findFirstPowerGridNodeByElectricitySpotMarket(market),
                            pggt.getPowerGeneratingTechnology(), null);
                    plant.setActualNominalCapacity(targetDifference);
                    double plantMarginalCost = determineExpectedMarginalCost(plant, fuelPrices, co2price);
                    marginalCostMap.put(plant, plantMarginalCost);
                    capacitySum += targetDifference;
                }
            }

            MapValueComparator comp = new MapValueComparator(marginalCostMap);
            meritOrder = new TreeMap<PowerPlant, Double>(comp);
            meritOrder.putAll(marginalCostMap);

            long numberOfSegments = reps.segmentRepository.count();

            double demandFactor = expectedDemand.get(market).doubleValue();

            // find expected prices per segment given merit order
            for (SegmentLoad segmentLoad : market.getLoadDurationCurve()) {

                double expectedSegmentLoad = segmentLoad.getBaseLoad() * demandFactor;

                if (expectedSegmentLoad > maxExpectedLoad) {
                    maxExpectedLoad = expectedSegmentLoad;
                }

                double segmentSupply = 0d;
                double segmentPrice = 0d;
                double totalCapacityAvailable = 0d;

                for (Entry<PowerPlant, Double> plantCost : meritOrder.entrySet()) {
                    PowerPlant plant = plantCost.getKey();
                    double plantCapacity = 0d;
                    // Determine available capacity in the future in this
                    // segment
                    plantCapacity = plant
                            .getExpectedAvailableCapacity(time, segmentLoad.getSegment(), numberOfSegments);
                    totalCapacityAvailable += plantCapacity;
                    // logger.warn("Capacity of plant " + plant.toString() +
                    // " is " +
                    // plantCapacity/plant.getActualNominalCapacity());
                    if (segmentSupply < expectedSegmentLoad) {
                        segmentSupply += plantCapacity;
                        segmentPrice = plantCost.getValue();
                    }

                }

                // logger.warn("Segment " +
                // segmentLoad.getSegment().getSegmentID() + " supply equals " +
                // segmentSupply + " and segment demand equals " +
                // expectedSegmentLoad);

                // Find strategic reserve operator for the market.
                double reservePrice = 0;
                double reserveVolume = 0;
                for (StrategicReserveOperator operator : strategicReserveOperatorRepository.findAll()) {
                    ElectricitySpotMarket market1 = reps.marketRepository.findElectricitySpotMarketForZone(operator
                            .getZone());
                    if (market.getNodeId().intValue() == market1.getNodeId().intValue()) {
                        reservePrice = operator.getReservePriceSR();
                        reserveVolume = operator.getReserveVolume();
                    }
                }

                if (segmentSupply >= expectedSegmentLoad
                        && ((totalCapacityAvailable - expectedSegmentLoad) <= (reserveVolume))) {
                    expectedElectricityPricesPerSegment.put(segmentLoad.getSegment(), reservePrice);
                    // logger.warn("Price: "+
                    // expectedElectricityPricesPerSegment);
                } else if (segmentSupply >= expectedSegmentLoad
                        && ((totalCapacityAvailable - expectedSegmentLoad) > (reserveVolume))) {
                    expectedElectricityPricesPerSegment.put(segmentLoad.getSegment(), segmentPrice);
                    // logger.warn("Price: "+
                    // expectedElectricityPricesPerSegment);
                } else {
                    expectedElectricityPricesPerSegment.put(segmentLoad.getSegment(), market.getValueOfLostLoad());
                }

            }
        }
    }

}
