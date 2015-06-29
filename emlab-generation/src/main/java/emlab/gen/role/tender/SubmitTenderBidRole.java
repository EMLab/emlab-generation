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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Transient;
import org.springframework.data.neo4j.support.Neo4jTemplate;

import emlab.gen.domain.agent.DecarbonizationModel;
import emlab.gen.domain.agent.EnergyProducer;
import emlab.gen.domain.agent.Regulator;
import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.domain.market.electricity.SegmentLoad;
import emlab.gen.domain.technology.PowerGeneratingTechnology;
import emlab.gen.domain.technology.PowerGridNode;
import emlab.gen.domain.technology.PowerPlant;
import emlab.gen.domain.technology.Substance;
import emlab.gen.domain.technology.SubstanceShareInFuelMix;
import emlab.gen.repository.Reps;
import emlab.gen.util.GeometricTrendRegression;

/**
 * @author rjjdejeu
 * 
 */

// General structure

/*
 * 1) Producers need to know which year the Tender is aiming to
 * 
 * 2) Producers calculate NPV of RES-E power plants in the future (via parts of
 * Investment Algorithm) for specific nodes
 * 
 * 3) Producers need to select an unprofitable RES-E power plants in the best
 * node
 * 
 * 4) Producers bid a price = difference NPV and 0, stored as TenderBidPrice 5)
 * Producers bid the node = already known, stored as TenderBidNode 6) Producers
 * bid the technology = already known, stored as TenderBidTechnology 7)
 * Producers bid the capacity = to size of the node, stored as TenderBidCapacity
 * 
 * 8) This Bid information of 4,5,6,7 is stored in a domain
 * emlab.gen.domain.policy.renewablesupport under TenderBid.java
 */

// extends EnergyProducer since I need the properties/behavior of the Energy
// Producer

public class SubmitTenderBidRole extends EnergyProducer implements PowerGeneratingTechnologyNodeLimit {

    @Transient
    @Autowired
    Reps reps;

    @Transient
    @Autowired
    Neo4jTemplate template;

    @Transient
    Map<ElectricitySpotMarket, MarketInformation> marketInfoMap = new HashMap<ElectricitySpotMarket, MarketInformation>();

    private boolean biomass;
    private boolean biogas;

    public boolean isBiomass() {
        return biomass;
    }

    public void setBiomass(boolean biomass) {
        this.biomass = biomass;
    }

    public boolean isBiogas() {
        return biogas;
    }

    public void setBiogas(boolean biogas) {
        this.biogas = biogas;
    }

    // This will let the producer DO something, thus act
    public void act(EnergyProducer producer) {

        /*
         * 1) Producers need to know which year the regulator wants to execute
         * the winning bids. I created a variable TenderFuturePointInTime that
         * should be used also by CalculateRenewableTargetRoleTender This
         * variable should be a property of the regulator This variable can be
         * pre-fixed via a scenario file.
         */

        long futureTimePoint = getCurrentTick() + Regulator.getTenderFutureTimePoint();

        // 2) Producers calculate NPV of RES-E power plants only for the
        // futureTimePoint for specific nodes
        // this is based on InvestInPowerGenerationTechnologiesStandard

        // ::Rob:: market expectations, I removed everything related to CO2
        Map<Substance, Double> expectedFuelPrices = predictFuelPrices(producer, futureTimePoint);

        // Demand
        Map<ElectricitySpotMarket, Double> expectedDemand = new HashMap<ElectricitySpotMarket, Double>();
        for (ElectricitySpotMarket elm : reps.template.findAll(ElectricitySpotMarket.class)) {
            GeometricTrendRegression gtr = new GeometricTrendRegression();
            for (long time = getCurrentTick(); time > getCurrentTick()
                    - producer.getNumberOfYearsBacklookingForForecasting()
                    && time >= 0; time = time - 1) {
                gtr.addData(time, elm.getDemandGrowthTrend().getValue(time));
            }
            expectedDemand.put(elm, gtr.predict(futureTimePoint));
        }

        // ::Rob:: NPV calculation via adjusted Investment algorithm
        ElectricitySpotMarket market = producer.getInvestorMarket();
        MarketInformation marketInformation = new MarketInformation(market, expectedDemand, expectedFuelPrices,
                futureTimePoint);

        double highestValue = Double.MIN_VALUE;
        PowerGeneratingTechnology bestTechnology = null;
        PowerGridNode bestNode = null;

        // ::Rob:: Goes through all RES-E technologies
        // ::Rob:: I have an error here with findAll, not sure why this mistake
        // is here, probably my line 72 is not completely correct and missing
        // <diamond operators>
        for (PowerGeneratingTechnology technology : reps.genericRepository.findAll(PowerGeneratingTechnology.class)) {

            DecarbonizationModel model = reps.genericRepository.findAll(DecarbonizationModel.class).iterator().next();

            if (technology.isIntermittent() && model.isNoPrivateIntermittentRESInvestment())
                continue;

            Iterable<PowerGridNode> possibleInstallationNodes;

            /*
             * ::Rob:: For dispatchable technologies, which is only biomass and
             * biogas here, just choose a random node. For intermittent evaluate
             * all possibilities.
             * 
             * Line 287 - 292 of package emlab.gen.domain.technology,
             * PowerGeneratingTechnology.java, has a boolean for 'intermittent',
             * so I assume that we need something similar for the technologies
             * biomass and biogas as dispatchable RES-E source, which I put in
             * line 79 - 96 of this class
             */

            if (technology.isIntermittent())
                possibleInstallationNodes = reps.powerGridNodeRepository.findAllPowerGridNodesByZone(market.getZone());
            else if (technology.isBiogas || technology.isBiomass) {
                possibleInstallationNodes = new LinkedList<PowerGridNode>();
                ((LinkedList<PowerGridNode>) possibleInstallationNodes).add(reps.powerGridNodeRepository
                        .findAllPowerGridNodesByZone(market.getZone()).iterator().next());
            }

            // ::Rob:: This for-loop goes through all nodes with all RES-E
            // technologies
            for (PowerGridNode node : possibleInstallationNodes) {

                // ::Rob::
                // Creates object plant of a power plant type that is related to
                // a tech, node, producer
                // I am not sure why getCurrentTick() is here?
                PowerPlant plant = new PowerPlant();
                plant.specifyNotPersist(getCurrentTick(), producer, node, technology);

                // ::Rob:: Obtains the fuel prices, which are calculated and
                // stored earlier in line 121
                Map<Substance, Double> myFuelPrices = new HashMap<Substance, Double>();
                for (Substance fuel : technology.getFuels()) {
                    myFuelPrices.put(fuel, expectedFuelPrices.get(fuel));
                }
                Set<SubstanceShareInFuelMix> fuelMix = calculateFuelMix(plant, myFuelPrices);
                plant.setFuelMix(fuelMix);

                // ::Rob:: Cost calculation for the plant in the node
                double expectedMarginalCost = determineExpectedMarginalCost(plant, expectedFuelPrices);
                double runningHours = 0d;
                double expectedGrossProfit = 0d;

                long numberOfSegments = reps.segmentRepository.count();

                // ::Rob:: Checks technology in a node for its profitability
                for (SegmentLoad segmentLoad : market.getLoadDurationCurve()) {
                    double expectedElectricityPrice = marketInformation.expectedElectricityPricesPerSegment
                            .get(segmentLoad.getSegment());
                    double hours = segmentLoad.getSegment().getLengthInHours();

                    // ::Rob:: Checks if MC are smaller than Price for
                    // intermittent or biogas and biomass
                    if (expectedMarginalCost <= expectedElectricityPrice) {
                        runningHours += hours;
                        if (technology.isIntermittent())
                            expectedGrossProfit += (expectedElectricityPrice - expectedMarginalCost)
                                    * hours
                                    * plant.getActualNominalCapacity()
                                    * reps.intermittentTechnologyNodeLoadFactorRepository
                                            .findIntermittentTechnologyNodeLoadFactorForNodeAndTechnology(node,
                                                    technology).getLoadFactorForSegment(segmentLoad.getSegment());
                        else
                            expectedGrossProfit += (expectedElectricityPrice - expectedMarginalCost)
                                    * hours
                                    * plant.getAvailableCapacity(futureTimePoint, segmentLoad.getSegment(),
                                            numberOfSegments);
                    }
                }

                /*
                 * ::Rob:: Statement about the running hours requirement. What
                 * is meant by minimum running hours? Why is that important? Is
                 * it a threshold that affects profitability? Probably YES. I
                 * think I should turn this around and changed it from if
                 * (runningHours <
                 * plant.getTechnology().getMinimumRunningHours()) INTO if
                 * (runningHours >
                 * plant.getTechnology().getMinimumRunningHours()) So the
                 * investor will start calculating an unprofitable investment
                 * i.e. the 'missing money' as Jorn calls it always But I am not
                 * sure... I think I rather have to look here at the
                 * projectValue (line 286), whether this is positive or negative
                 */

                // logger.warn(agent +
                // "expects technology {} to have {} running", technology,
                // runningHours);
                // expect to meet minimum running hours?
                if (runningHours < plant.getTechnology().getMinimumRunningHours()) {
                    // logger.warn(agent+
                    // " will not invest in {} technology as he expect to have {} running, which is lower then required",
                    // technology, runningHours);
                } else {

                    // ::Rob::
                    // getCurrentTick(), should that not be futurePoint, as
                    // defined in line 115?
                    double fixedOMCost = calculateFixedOperatingCost(plant, getCurrentTick());// /
                    // plant.getActualNominalCapacity();

                    double operatingProfit = expectedGrossProfit - fixedOMCost;

                    // Calculation of weighted average cost of capital,
                    // based on the companies debt-ratio
                    double wacc = (1 - agent.getDebtRatioOfInvestments()) * agent.getEquityInterestRate()
                            + agent.getDebtRatioOfInvestments() * agent.getLoanInterestRate();

                    // Creation of out cash-flow during power plant building
                    // phase (note that the cash-flow is negative!)
                    TreeMap<Integer, Double> discountedProjectCapitalOutflow = calculateSimplePowerPlantInvestmentCashFlow(
                            technology.getDepreciationTime(), (int) plant.getActualLeadtime(),
                            plant.getActualInvestedCapital(), 0);
                    // Creation of in cashflow during operation
                    TreeMap<Integer, Double> discountedProjectCashInflow = calculateSimplePowerPlantInvestmentCashFlow(
                            technology.getDepreciationTime(), (int) plant.getActualLeadtime(), 0, operatingProfit);

                    double discountedCapitalCosts = npv(discountedProjectCapitalOutflow, wacc);// are
                    // defined
                    // negative!!
                    // plant.getActualNominalCapacity();

                    // logger.warn("Agent {}  found that the discounted capital for technology {} to be "
                    // + discountedCapitalCosts, agent,
                    // technology);

                    double discountedOpProfit = npv(discountedProjectCashInflow, wacc);

                    // logger.warn("Agent {}  found that the projected discounted inflows for technology {} to be "
                    // + discountedOpProfit,
                    // agent, technology);

                    /*
                     * ::Rob:: Now this projectValue is probably negative due to
                     * my change in the running hours statement
                     */

                    double projectValue = discountedOpProfit + discountedCapitalCosts;

                    // if (technology.isIntermittent()) {
                    // logger.warn(technology + "in " + node.getName() +
                    // ", NPV: " + projectValue
                    // + ", GrossProfit: " + expectedGrossProfit);
                    // }

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
                     * Divide by capacity, in order not to favor large power
                     * plants (which have the single largest NPV
                     */

                    /*
                     * ::Rob:: I want to find projectValue lower than 0, so I
                     * changed if (projectValue > 0) INTO if (projectValue < 0)
                     */

                    if (projectValue < 0 && projectValue / plant.getActualNominalCapacity() > highestValue) {
                        highestValue = projectValue / plant.getActualNominalCapacity();
                        bestTechnology = plant.getTechnology();
                        bestNode = node;
                    }
                }

            }

        }
        
        if (permanentUpperCapacityLimit != 0) { 
            for (all bestNodes) {
                
            }
        }
        
        
//        IF (nodeCapacity != 0) THEN {
//            FOR (all bestNodes) {
//                double bidQuantity;
//                IF (cashflow of agent >= cashflow needed to make investment to fill the  node) {
//                    THEN bidQuantity = nodeMaxCapacity;
//                    nodeCapacity = 0;
//                }
//                ELSE IF (cashflow of agent < cashflow needed to fil the node) { 
//                    THEN  cashflowAvailableFraction = cashflow available /   cashflow for   installing maxNodeCapacity;
//                    bidQuantity = cashflowAvailableFraction * nodeMaxCapacity;
//                    nodeCapacity = nodeMaxCapacity – BidQuantity;
//                }
//                Store bidQuantity in emlab.gen domain.policy.renewablesupport in     TenderBid.java;
//        ELSE IF (nodeCapacity = 0) THEN {}  
        
        
        
        
        
    }
}
