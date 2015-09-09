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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.math.stat.regression.SimpleRegression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.data.annotation.Transient;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.aspects.core.NodeBacked;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.transaction.annotation.Transactional;

import agentspring.role.Role;
import emlab.gen.domain.agent.BigBank;
import emlab.gen.domain.agent.DecarbonizationModel;
import emlab.gen.domain.agent.EnergyProducer;
import emlab.gen.domain.agent.Government;
import emlab.gen.domain.agent.PowerPlantManufacturer;
import emlab.gen.domain.agent.StochasticTargetInvestor;
import emlab.gen.domain.agent.StrategicReserveOperator;
import emlab.gen.domain.agent.TargetInvestor;
import emlab.gen.domain.contract.CashFlow;
import emlab.gen.domain.contract.Loan;
import emlab.gen.domain.gis.Zone;
import emlab.gen.domain.market.CO2Auction;
import emlab.gen.domain.market.ClearingPoint;
import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.domain.market.electricity.Segment;
import emlab.gen.domain.market.electricity.SegmentLoad;
import emlab.gen.domain.policy.PowerGeneratingTechnologyTarget;
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
 * Creates investment options, calculates risk premiums, evaluates the options,
 * invests
 *
 * @author FreekGielis
 *
 */

@Configurable
@NodeEntity
public class InvestmentWithUtilityBasedRiskPremiums<T extends EnergyProducer> extends GenericInvestmentRole<T>
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

    // ===================== Market expectations ====================
    @Transient
    Map<ElectricitySpotMarket, MarketInformation> marketInfoMap = new HashMap<ElectricitySpotMarket, MarketInformation>();

    // -------------------- Start: for each agent ------------------

    @Override
    public void act(T agent) {

        long futureTimePoint = getCurrentTick() + agent.getInvestmentFutureTimeHorizon();
        // logger.warn(agent + " is looking at timepoint " + futureTimePoint);

        // Map expected fuel prices

        Map<Substance, Double> expectedFuelPrices = predictFuelPrices(agent, futureTimePoint);

        // CO2
        Map<ElectricitySpotMarket, Double> expectedCO2Price = determineExpectedCO2PriceInclTaxAndFundamentalForecast(
                futureTimePoint, agent.getNumberOfYearsBacklookingForForecasting(), 0, getCurrentTick());

        // logger.warn("{} expects CO2 prices {}", agent.getName(),
        // expectedCO2Price);

        Map<ElectricitySpotMarket, Double> expectedCO2PriceOld = determineExpectedCO2PriceInclTax(futureTimePoint,
                agent.getNumberOfYearsBacklookingForForecasting(), getCurrentTick());
        // logger.warn("{} used to expect CO2 prices {}", agent.getName(),
        // expectedCO2PriceOld);

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

        // ======================= Investment decision ====================

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

        /*
         * Here the highest values are initialized to enable evaluation of all
         * investment options
         */

        double highestValue = Double.MIN_VALUE;
        double highestValueWithoutRiskPremium = Double.MIN_VALUE;
        PowerGeneratingTechnology bestTechnology = null;
        PowerGeneratingTechnology bestTechnologyWithoutRiskPremium = null;
        PowerGridNode bestNode = null;
        PowerGridNode bestNodeWithoutRiskPremium = null;

        for (PowerGeneratingTechnology technology : reps.genericRepository.findAll(PowerGeneratingTechnology.class)) {

            DecarbonizationModel model = reps.genericRepository.findAll(DecarbonizationModel.class).iterator().next();

            if (technology.isIntermittent() && model.isNoPrivateIntermittentRESInvestment())
                continue;

            Iterable<PowerGridNode> possibleInstallationNodes;

            /*
             * For dispatchable technologies just choose a random node. For
             * intermittent evaluate all possibilities.
             */
            if (technology.isIntermittent())
                possibleInstallationNodes = reps.powerGridNodeRepository.findAllPowerGridNodesByZone(market.getZone());
            else {
                possibleInstallationNodes = new LinkedList<PowerGridNode>();
                ((LinkedList<PowerGridNode>) possibleInstallationNodes).add(reps.powerGridNodeRepository
                        .findAllPowerGridNodesByZone(market.getZone()).iterator().next());
            }

            // logger.warn("Calculating for " + technology.getName() +
            // ", for Nodes: "
            // + possibleInstallationNodes.toString());

            for (PowerGridNode node : possibleInstallationNodes) {

                // Create potential powerplant to invest in
                PowerPlant plant = new PowerPlant();
                plant.specifyNotPersist(getCurrentTick(), agent, node, technology);

                // if too much capacity of this technology in the pipeline (not
                // limited to the 5 years)

                double expectedInstalledCapacityOfTechnology = reps.powerPlantRepository
                        .calculateCapacityOfExpectedOperationalPowerPlantsInMarketAndTechnology(market, technology,
                                futureTimePoint);
                PowerGeneratingTechnologyTarget technologyTarget = reps.powerGenerationTechnologyTargetRepository
                        .findOneByTechnologyAndMarket(technology, market);

                // More than technology target in the pipeline?

                if (technologyTarget != null) {
                    double technologyTargetCapacity = technologyTarget.getTrend().getValue(futureTimePoint);
                    expectedInstalledCapacityOfTechnology = (technologyTargetCapacity > expectedInstalledCapacityOfTechnology) ? technologyTargetCapacity
                            : expectedInstalledCapacityOfTechnology;
                }
                double pgtNodeLimit = Double.MAX_VALUE;
                PowerGeneratingTechnologyNodeLimit pgtLimit = reps.powerGeneratingTechnologyNodeLimitRepository
                        .findOneByTechnologyAndNode(technology, plant.getLocation());
                if (pgtLimit != null) {
                    pgtNodeLimit = pgtLimit.getUpperCapacityLimit(futureTimePoint);
                }

                // Create expected capacity of technology in node

                double expectedInstalledCapacityOfTechnologyInNode = reps.powerPlantRepository
                        .calculateCapacityOfExpectedOperationalPowerPlantsByNodeAndTechnology(plant.getLocation(),
                                technology, futureTimePoint);

                // Create expected capacity owned by agent in the market

                double expectedOwnedTotalCapacityInMarket = reps.powerPlantRepository
                        .calculateCapacityOfExpectedOperationalPowerPlantsInMarketByOwner(market, futureTimePoint,
                                agent);

                // Create expected capacity owned of this technology

                double expectedOwnedCapacityInMarketOfThisTechnology = reps.powerPlantRepository
                        .calculateCapacityOfExpectedOperationalPowerPlantsInMarketByOwnerAndTechnology(market,
                                technology, futureTimePoint, agent);

                // Create capacity of technology in pipeline

                double capacityOfTechnologyInPipeline = reps.powerPlantRepository
                        .calculateCapacityOfPowerPlantsByTechnologyInPipeline(technology, getCurrentTick());

                // Create current operational capacity of technology

                double operationalCapacityOfTechnology = reps.powerPlantRepository
                        .calculateCapacityOfOperationalPowerPlantsByTechnology(technology, getCurrentTick());

                // Create total capacity in pipeline in the market

                double capacityInPipelineInMarket = reps.powerPlantRepository
                        .calculateCapacityOfPowerPlantsByMarketInPipeline(market, getCurrentTick());

                // If the maximum fraction of the technology with respect to the
                // max load
                // is expected to be exceeded when new plant is added, do not
                // invest

                if ((expectedInstalledCapacityOfTechnology + plant.getActualNominalCapacity())
                        / (marketInformation.maxExpectedLoad + plant.getActualNominalCapacity()) > technology
                        .getMaximumInstalledCapacityFractionInCountry()) {
                    // logger.warn(agent +
                    // " will not invest in {} technology because there's too much of this type in the market",
                    // technology);

                    // if expected installed cap with new plant exceeds node
                    // limit

                } else if ((expectedInstalledCapacityOfTechnologyInNode + plant.getActualNominalCapacity()) > pgtNodeLimit) {

                    // If the cap owned of this technology by agent exceeds the
                    // max allowed fraction per agent

                } else if (expectedOwnedCapacityInMarketOfThisTechnology > expectedOwnedTotalCapacityInMarket
                        * technology.getMaximumInstalledCapacityFractionPerAgent()) {
                    // logger.warn(agent +
                    // " will not invest in {} technology because there's too much capacity planned by him",
                    // technology);

                    // If cap in pipeline exceeds 20% of the max expected load

                } else if (capacityInPipelineInMarket > 0.2 * marketInformation.maxExpectedLoad) {
                    // logger.warn("Not investing because more than 20% of demand in pipeline.");

                    // If cap of technology in pipeline is higher than 2 times
                    // the existing
                    // capacity of technology and more than 9000 MW

                } else if ((capacityOfTechnologyInPipeline > 2.0 * operationalCapacityOfTechnology)
                        && capacityOfTechnologyInPipeline > 9000) { // TODO:
                    // reflects that you cannot expand a technology out of zero.
                    // logger.warn(agent +
                    // " will not invest in {} technology because there's too much capacity in the pipeline",
                    // technology);

                    // If agent is not able to pay the equity fraction of the
                    // investment (downpayment)
                    // from its current cash balance

                } else if (plant.getActualInvestedCapital() * (1 - agent.getDebtRatioOfInvestments()) > agent
                        .getDownpaymentFractionOfCash() * agent.getCash()) {
                    // logger.warn(agent +
                    // " will not invest in {} technology as he does not have enough money for downpayment",
                    // technology);

                    // If all above criteria passed: continue evaluation of
                    // investment option

                } else {

                    // Get relevant expected fuel prices and expected CO2 price
                    // And calculate the expected marginal cost in year
                    // futureTimepoint

                    Map<Substance, Double> myFuelPrices = new HashMap<Substance, Double>();
                    for (Substance fuel : technology.getFuels()) {
                        myFuelPrices.put(fuel, expectedFuelPrices.get(fuel));
                    }
                    Set<SubstanceShareInFuelMix> fuelMix = calculateFuelMix(plant, myFuelPrices,
                            expectedCO2Price.get(market));
                    plant.setFuelMix(fuelMix);

                    double expectedMarginalCost = determineExpectedMarginalCost(plant, expectedFuelPrices,
                            expectedCO2Price.get(market));

                    // Initialize running hours and expected gross profit
                    // Make number of segments

                    double runningHours = 0d;
                    double expectedGrossProfit = 0d;

                    long numberOfSegments = reps.segmentRepository.count();

                    // TODO somehow the prices of long-term contracts could also
                    // be used here to determine the expected profit. Maybe not
                    // though...

                    // For each segment on the load-duration curve,
                    // get expected electricity price and length in hours

                    for (SegmentLoad segmentLoad : market.getLoadDurationCurve()) {
                        double expectedElectricityPrice = marketInformation.expectedElectricityPricesPerSegment
                                .get(segmentLoad.getSegment());
                        double hours = segmentLoad.getSegment().getLengthInHours();

                        // If expected marginal costs are lower than expected
                        // electricity price
                        // add the hours to the amount of expected running hours
                        // add the difference between price and MC times the
                        // amount of hours and
                        // available capacity (gross profit from serving
                        // expected demand in that segment)

                        if (expectedMarginalCost <= expectedElectricityPrice) {
                            runningHours += hours;

                            // available capacity for intermittent is nominal
                            // times
                            // Node load factor (based on weather
                            // circumstances?)

                            if (technology.isIntermittent())
                                expectedGrossProfit += (expectedElectricityPrice - expectedMarginalCost)
                                * hours
                                * plant.getActualNominalCapacity()
                                * reps.intermittentTechnologyNodeLoadFactorRepository
                                .findIntermittentTechnologyNodeLoadFactorForNodeAndTechnology(node,
                                        technology).getLoadFactorForSegment(segmentLoad.getSegment());

                            // If not intermittent available capacity is
                            // calculated by method
                            // getAvailable Capacity

                            else
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

                        // fixed operating costs calculated

                        double fixedOMCost = calculateFixedOperatingCost(plant, getCurrentTick());// /

                        // plant.getActualNominalCapacity();

                        // Get cvar of historical gross profits
                        // Set operating profit and operating profit with cvar

                        // TODO: Make agent create cVar value of best performing
                        // power plants

                        Double lowerBoundaryConventionalTech = reps.financialPowerPlantReportRepository
                                .calculateLowerBoundaryConventionalTech(getCurrentTick() - 5, getCurrentTick(), agent,
                                        technology, agent.getHistoricalCVarAlpha());

                        Double higherBoundaryConventionalTech = reps.financialPowerPlantReportRepository
                                .calculateHigherBoundaryConventionalTech(
                                        getCurrentTick() - 5, getCurrentTick(), agent, technology,
                                        agent.getHistoricalCVarAlpha());

                        double lowerUncertaintyBoundary = 0;
                        if (lowerBoundaryConventionalTech != null) {
                            lowerUncertaintyBoundary = lowerBoundaryConventionalTech.doubleValue()
                                    * plant.getActualNominalCapacity();
                        } else {
                            lowerUncertaintyBoundary = (1 - agent.getHistoricalCVarPropensityForNewTechnologies())
                                    * expectedGrossProfit;
                        }

                        double higherUncertaintyBoundary = 0;
                        if (higherBoundaryConventionalTech != null) {
                            higherUncertaintyBoundary = higherBoundaryConventionalTech.doubleValue()
                                    * plant.getActualNominalCapacity();
                        } else {
                            higherUncertaintyBoundary = (1 + agent.getHistoricalCVarPropensityForNewTechnologies())
                                    * expectedGrossProfit;
                        }


                        double operatingProfit = expectedGrossProfit - fixedOMCost;

                        double higherBoundaryOperatingProfit = higherUncertaintyBoundary - fixedOMCost;

                        double lowerBoundaryOperatingProfit = lowerUncertaintyBoundary - fixedOMCost;

                        // TODO Alter discount rate on the basis of the amount
                        // in long-term contracts?
                        // TODO Alter discount rate on the basis of other stuff,
                        // such as amount of money, market share, portfolio
                        // size.

                        // Calculation of weighted average cost of capital,
                        // based on the companies debt-ratio and interest rates
                        // on
                        // debt and equity

                        double wacc = (1 - agent.getDebtRatioOfInvestments()) * agent.getEquityInterestRate()
                                + agent.getDebtRatioOfInvestments() * agent.getLoanInterestRate();

                        // For new technologies, increase the wacc with an
                        // historical cvar rate increase
                        if (lowerBoundaryConventionalTech == null)
                            wacc += agent.getHistoricalCVarInterestRateIncreaseForNewTechnologies();

                        // Creation of out cash-flow during power plant building
                        // phase (note that the cash-flow is negative!)

                        TreeMap<Integer, Double> projectCapitalOutflow = calculateSimplePowerPlantInvestmentCashFlow(
                                technology.getDepreciationTime(), (int) plant.getActualLeadtime(),
                                plant.getActualInvestedCapital(), 0);

                        // Creation of in cashflow during operation
                        // for operating profits without cvar and put in a
                        // treemap
                        TreeMap<Integer, Double> projectCashInflow = calculateSimplePowerPlantInvestmentCashFlow(
                                technology.getDepreciationTime(), (int) plant.getActualLeadtime(), 0, operatingProfit);

                        // for operating profits with cvar
                        // and put in a treemap

                        TreeMap<Integer, Double> projectCashInflowHigherBoundary = calculateSimplePowerPlantInvestmentCashFlow(
                                technology.getDepreciationTime(), (int) plant.getActualLeadtime(), 0,
                                higherBoundaryOperatingProfit);

                        TreeMap<Integer, Double> projectCashInflowLowerBoundary = calculateSimplePowerPlantInvestmentCashFlow(
                                technology.getDepreciationTime(), (int) plant.getActualLeadtime(), 0,
                                lowerBoundaryOperatingProfit);

                        // calculate npv of capital costs

                        double discountedCapitalCosts = npv(projectCapitalOutflow, wacc);

                        // are
                        // defined
                        // negative!!
                        // plant.getActualNominalCapacity();

                        // logger.warn("Agent {}  found that the discounted capital for technology {} to be "
                        // + discountedCapitalCosts, agent,
                        // technology);

                        // calculate npv of operational profits

                        double discountedOpProfit = npv(projectCashInflow, wacc);

                        double discountedHigherBoundaryOpProfit = npv(projectCashInflowHigherBoundary, wacc);

                        double discountedLowerBoundaryOpProfit = npv(projectCashInflowLowerBoundary, wacc);

                        // logger.warn("Agent {}  found that the projected discounted inflows for technology {} to be "
                        // + discountedOpProfit,
                        // agent, technology);

                        double projectValue = discountedOpProfit + discountedCapitalCosts;
                        double oldProjectValue = projectValue;

                        double higherBoundaryProjectValue = discountedHigherBoundaryOpProfit + discountedCapitalCosts;
                        double lowerBoundaryProjectValue = discountedLowerBoundaryOpProfit + discountedCapitalCosts;

                        // ------------------ Risk premium calculation
                        // -----------------
                        //
                        //


                        double currentCashBalance = agent.getCash();
                        double impactOnCashBalanceExpectedProjectValue = currentCashBalance + oldProjectValue;
                        double impactOnCashBalanceLowerBoundary = currentCashBalance + lowerBoundaryProjectValue;
                        double impactOnCashBalanceHigherBoundary = currentCashBalance + higherBoundaryProjectValue;

                        if (agent.getRiskAversionType() == "CARA") {

                            double riskAversionCoefficient = agent.getRiskAversionCoefficientCARA();
                            double riskPremium = calculateRiskPremiumCARA(riskAversionCoefficient, currentCashBalance,
                                    impactOnCashBalanceExpectedProjectValue, impactOnCashBalanceLowerBoundary,
                                    impactOnCashBalanceHigherBoundary);

                            projectValue += riskPremium;
                        }

                        if (agent.getRiskAversionType() == "CRRA") {

                            double riskAversionCoefficient = agent.getRiskAversionCoefficientCRRA();
                            double riskPremium = calculateRiskPremiumCRRA(riskAversionCoefficient, currentCashBalance,
                                    impactOnCashBalanceExpectedProjectValue, impactOnCashBalanceLowerBoundary,
                                    impactOnCashBalanceHigherBoundary);

                            projectValue += riskPremium;
                        }

                        if (agent.getRiskAversionType() == "neutral") {

                            projectValue += 0;

                        }

                        // if (historicalCvarProjectValue < 0) {
                        // logger.warn("Adjusting NPV!");
                        // projectValue += beta * historicalCvarProjectValue;
                        // }

                        // if (technology.isIntermittent()) {
                        // logger.warn(technology + "in " + node.getName() +
                        // ", NPV: " + projectValue
                        // + ", GrossProfit: " + expectedGrossProfit);
                        // }

                        // logger.warn(technology + "in " + node.getName() +
                        // ", NPV: " + projectValue + ", GrossProfit: "
                        // + expectedGrossProfit);
                        //
                        // logger.warn("CVar: " + historicalCvarProjectValue);
                        //
                        // logger.warn("NPV-CVAR: " + projectValue);

                        // logger.warn(
                        // "Agent {}  found the project value for technology {} to be "
                        // + Math.round(projectValue /
                        // plant.getActualNominalCapacity())
                        // + " EUR/kW (running hours: " + runningHours + "",
                        // agent, technology);

                        // double projectTotalValue = projectValuePerMW *
                        // plant.getActualNominalCapacity();

                        // double projectReturnOnInvestment = discountedOpProfit
                        // / (-discountedCapitalCosts);

                        /*
                         * Divide by capacity, in order not to favour large
                         * power plants (which have the single largest NPV
                         */
                        if (projectValue < 0 && oldProjectValue > 0) {
                            logger.warn(
                                    "Not profitable w risk premium. NPV-RP: {}, NPV: {}, Lower boundary gross profit: "
                                            + lowerUncertaintyBoundary / plant.getActualNominalCapacity()
                                            + ", Higher boundary gross profit: "
                                            + higherUncertaintyBoundary
                                            / plant.getActualNominalCapacity() + " Tech:"
                                            + technology + " in " + node.getName(),
                                            projectValue / plant.getActualNominalCapacity(),
                                            oldProjectValue / plant.getActualNominalCapacity());
                        }

                        if (projectValue > 0) {
                            logger.warn(
                                    "Is profitable w risk premium. NPV-RP: {}, NPV: {}, lower boundary gross profit: "
                                            + lowerUncertaintyBoundary / plant.getActualNominalCapacity()
                                            + ", higher boundary gross profit: " + higherUncertaintyBoundary
                                            / plant.getActualNominalCapacity() + " Tech:"
                                            + technology + " in " + node.getName(),
                                            projectValue / plant.getActualNominalCapacity(),
                                            oldProjectValue / plant.getActualNominalCapacity());
                        }

                        if (projectValue > 0 && projectValue / plant.getActualNominalCapacity() > highestValue) {
                            highestValue = projectValue / plant.getActualNominalCapacity();
                            bestTechnology = plant.getTechnology();
                            bestNode = node;
                        }

                        if (oldProjectValue > 0
                                && oldProjectValue / plant.getActualNominalCapacity() > highestValueWithoutRiskPremium) {
                            highestValueWithoutRiskPremium = oldProjectValue / plant.getActualNominalCapacity();
                            bestTechnologyWithoutRiskPremium = plant.getTechnology();
                            bestNodeWithoutRiskPremium = node;
                        }
                    }

                }

            }
        }

        if (bestTechnology != null && bestTechnologyWithoutRiskPremium != null
                && !bestTechnologyWithoutRiskPremium.equals(bestTechnology)) {
            logger.warn("Because of Risk premium investing in {}, instead of in {}", bestTechnology.getName(),
                    bestTechnologyWithoutRiskPremium.getName());
        }
        if (bestTechnology == null && bestTechnologyWithoutRiskPremium != null) {
            logger.warn("Not investing. W/o risk premium would have invested in {}",
                    bestTechnologyWithoutRiskPremium.getName());
        }

        if (bestTechnology != null) {
            // logger.warn("Agent {} invested in technology {} at tick " +
            // getCurrentTick(), agent, bestTechnology);

            PowerPlant plant = new PowerPlant();
            plant.specifyAndPersist(getCurrentTick(), agent, bestNode, bestTechnology);
            PowerPlantManufacturer manufacturer = reps.genericRepository.findFirst(PowerPlantManufacturer.class);
            BigBank bigbank = reps.genericRepository.findFirst(BigBank.class);

            double investmentCostPayedByEquity = plant.getActualInvestedCapital()
                    * (1 - agent.getDebtRatioOfInvestments());
            double investmentCostPayedByDebt = plant.getActualInvestedCapital() * agent.getDebtRatioOfInvestments();
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

    // ===================== End of investment loop =========================

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
                            - (agent.getNumberOfYearsBacklookingForForecasting() - 1), getCurrentTick(), false);
            // logger.warn("{}, {}",
            // getCurrentTick()-(agent.getNumberOfYearsBacklookingForForecasting()-1),
            // getCurrentTick());
            // Create regression object
            SimpleRegression gtr = new SimpleRegression();
            for (ClearingPoint clearingPoint : cps) {
                // logger.warn("CP {}: {} , in" + clearingPoint.getTime(),
                // substance.getName(), clearingPoint.getPrice());
                gtr.addData(clearingPoint.getTime(), clearingPoint.getPrice());
            }
            gtr.addData(getCurrentTick(), findLastKnownPriceForSubstance(substance, getCurrentTick()));
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

    private double calculateRiskPremiumCARA(double riskAversionCoefficient, double cashBalance, double expectedROI,
            double lowerROI, double higherROI) {
        double riskPremium = 0;

        double utilityLowerROI = 1 - Math.exp(-riskAversionCoefficient * lowerROI);
        double utilityHigherROI = 1 - Math.exp(-riskAversionCoefficient * higherROI);
        double slopeRiskNeutralCurve = (utilityLowerROI - utilityHigherROI) / (lowerROI - higherROI);
        double utilityCertaintyEquivalent = slopeRiskNeutralCurve * expectedROI + utilityLowerROI
                - slopeRiskNeutralCurve * lowerROI;
        double certaintyEquivalentROI = Math.log(1 - utilityCertaintyEquivalent) / (-riskAversionCoefficient);

        riskPremium += (certaintyEquivalentROI - expectedROI);

        return riskPremium;


    }

    private double calculateRiskPremiumCRRA(double riskAversionCoefficient, double cashBalance, double expectedROI,
            double lowerROI, double higherROI) {
        double riskPremium = 0;

        double utilityLowerROI = 0;
        double utilityHigherROI = 0;
        double utilityCertaintyEquivalent = 0;
        double certaintyEquivalentROI = 0;


        if (riskAversionCoefficient == 1) {
            utilityLowerROI = Math.log(lowerROI);
            utilityHigherROI = Math.log(higherROI);
        } else {
            utilityLowerROI = Math.pow(lowerROI, 1 - riskAversionCoefficient) / (1 - riskAversionCoefficient);
            utilityHigherROI = Math.pow(higherROI, 1 - riskAversionCoefficient) / (1 - riskAversionCoefficient);
        }

        double slopeRiskNeutralCurve = (utilityLowerROI - utilityHigherROI) / (lowerROI - higherROI);

        utilityCertaintyEquivalent = slopeRiskNeutralCurve * expectedROI + utilityLowerROI - slopeRiskNeutralCurve
                * lowerROI;

        if (riskAversionCoefficient == 1) {
            certaintyEquivalentROI = Math.exp(utilityCertaintyEquivalent);
        } else {
            certaintyEquivalentROI = Math.pow(utilityCertaintyEquivalent * (1 - riskAversionCoefficient),
                    1 / (1 - riskAversionCoefficient));

        }

        riskPremium += (certaintyEquivalentROI - expectedROI);

        return riskPremium;

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
            for (TargetInvestor targetInvestor : reps.targetInvestorRepository.findAllByMarket(market)) {
                if (!(targetInvestor instanceof StochasticTargetInvestor)) {
                    for (PowerGeneratingTechnologyTarget pggt : targetInvestor.getPowerGenerationTechnologyTargets()) {
                        double expectedTechnologyCapacity = reps.powerPlantRepository
                                .calculateCapacityOfExpectedOperationalPowerPlantsInMarketAndTechnology(market,
                                        pggt.getPowerGeneratingTechnology(), time);
                        double targetDifference = pggt.getTrend().getValue(time) - expectedTechnologyCapacity;
                        if (targetDifference > 0) {
                            PowerPlant plant = new PowerPlant();
                            plant.specifyNotPersist(getCurrentTick(), new EnergyProducer(),
                                    reps.powerGridNodeRepository.findFirstPowerGridNodeByElectricitySpotMarket(market),
                                    pggt.getPowerGeneratingTechnology());
                            plant.setActualNominalCapacity(targetDifference);
                            double plantMarginalCost = determineExpectedMarginalCost(plant, fuelPrices, co2price);
                            marginalCostMap.put(plant, plantMarginalCost);
                            capacitySum += targetDifference;
                        }
                    }
                } else {
                    for (PowerGeneratingTechnologyTarget pggt : targetInvestor.getPowerGenerationTechnologyTargets()) {
                        double expectedTechnologyCapacity = reps.powerPlantRepository
                                .calculateCapacityOfExpectedOperationalPowerPlantsInMarketAndTechnology(market,
                                        pggt.getPowerGeneratingTechnology(), time);
                        double expectedTechnologyAddition = 0;
                        long contructionTime = getCurrentTick()
                                + pggt.getPowerGeneratingTechnology().getExpectedLeadtime()
                                + pggt.getPowerGeneratingTechnology().getExpectedPermittime();
                        for (long investmentTimeStep = contructionTime + 1; investmentTimeStep <= time; investmentTimeStep = investmentTimeStep + 1) {
                            expectedTechnologyAddition += (pggt.getTrend().getValue(investmentTimeStep) - pggt
                                    .getTrend().getValue(investmentTimeStep - 1));
                        }
                        if (expectedTechnologyAddition > 0) {
                            PowerPlant plant = new PowerPlant();
                            plant.specifyNotPersist(getCurrentTick(), new EnergyProducer(),
                                    reps.powerGridNodeRepository.findFirstPowerGridNodeByElectricitySpotMarket(market),
                                    pggt.getPowerGeneratingTechnology());
                            plant.setActualNominalCapacity(expectedTechnologyAddition);
                            double plantMarginalCost = determineExpectedMarginalCost(plant, fuelPrices, co2price);
                            marginalCostMap.put(plant, plantMarginalCost);
                            capacitySum += expectedTechnologyAddition;
                        }
                    }
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

    /**
     * Calculates expected CO2 price based on a geometric trend estimation, of
     * the past years. The adjustmentForDetermineFuelMix needs to be set to 1,
     * if this is used in the determine fuel mix role.
     *
     * @param futureTimePoint
     *            Year the prediction is made for
     * @param yearsLookingBackForRegression
     *            How many years are used as input for the regression, incl. the
     *            current tick.
     * @return
     */
    protected HashMap<ElectricitySpotMarket, Double> determineExpectedCO2PriceInclTaxAndFundamentalForecast(
            long futureTimePoint, long yearsLookingBackForRegression, int adjustmentForDetermineFuelMix,
            long clearingTick) {
        HashMap<ElectricitySpotMarket, Double> co2Prices = new HashMap<ElectricitySpotMarket, Double>();
        CO2Auction co2Auction = reps.genericRepository.findFirst(CO2Auction.class);
        Iterable<ClearingPoint> cps = reps.clearingPointRepository.findAllClearingPointsForMarketAndTimeRange(
                co2Auction, clearingTick - yearsLookingBackForRegression + 1 - adjustmentForDetermineFuelMix,
                clearingTick - adjustmentForDetermineFuelMix, false);
        // Create regression object and calculate average
        SimpleRegression sr = new SimpleRegression();
        Government government = reps.template.findAll(Government.class).iterator().next();
        double lastPrice = 0;
        double averagePrice = 0;
        int i = 0;
        for (ClearingPoint clearingPoint : cps) {
            sr.addData(clearingPoint.getTime(), clearingPoint.getPrice());
            lastPrice = clearingPoint.getPrice();
            averagePrice += lastPrice;
            i++;
        }
        averagePrice = averagePrice / i;
        double expectedCO2Price;
        double expectedRegressionCO2Price;
        if (i > 1) {
            expectedRegressionCO2Price = sr.predict(futureTimePoint);
            expectedRegressionCO2Price = Math.max(0, expectedRegressionCO2Price);
            expectedRegressionCO2Price = Math
                    .min(expectedRegressionCO2Price, government.getCo2Penalty(futureTimePoint));
        } else {
            expectedRegressionCO2Price = lastPrice;
        }
        ClearingPoint expectedCO2ClearingPoint = reps.clearingPointRepository.findClearingPointForMarketAndTime(
                co2Auction, getCurrentTick()
                + reps.genericRepository.findFirst(DecarbonizationModel.class).getCentralForecastingYear(),
                true);
        expectedCO2Price = (expectedCO2ClearingPoint == null) ? 0 : expectedCO2ClearingPoint.getPrice();
        expectedCO2Price = (expectedCO2Price + expectedRegressionCO2Price) / 2;
        for (ElectricitySpotMarket esm : reps.marketRepository.findAllElectricitySpotMarkets()) {
            double nationalCo2MinPriceinFutureTick = reps.nationalGovernmentRepository
                    .findNationalGovernmentByElectricitySpotMarket(esm).getMinNationalCo2PriceTrend()
                    .getValue(futureTimePoint);
            double co2PriceInCountry = 0d;
            if (expectedCO2Price > nationalCo2MinPriceinFutureTick) {
                co2PriceInCountry = expectedCO2Price;
            } else {
                co2PriceInCountry = nationalCo2MinPriceinFutureTick;
            }
            co2PriceInCountry += reps.genericRepository.findFirst(Government.class).getCO2Tax(futureTimePoint);
            co2Prices.put(esm, Double.valueOf(co2PriceInCountry));
        }
        return co2Prices;
    }


}