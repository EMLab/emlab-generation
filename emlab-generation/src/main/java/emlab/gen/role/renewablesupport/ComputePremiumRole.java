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
package emlab.gen.role.renewablesupport;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Transient;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.transaction.annotation.Transactional;

import agentspring.role.Role;
import agentspring.role.RoleComponent;
import emlab.gen.domain.agent.DecarbonizationModel;
import emlab.gen.domain.agent.EnergyProducer;
import emlab.gen.domain.agent.Regulator;
import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.domain.market.electricity.Segment;
import emlab.gen.domain.market.electricity.SegmentLoad;
import emlab.gen.domain.policy.renewablesupport.BaseCostFip;
import emlab.gen.domain.policy.renewablesupport.RenewableSupportFipScheme;
import emlab.gen.domain.technology.PowerGeneratingTechnology;
import emlab.gen.domain.technology.PowerGridNode;
import emlab.gen.domain.technology.PowerPlant;
import emlab.gen.domain.technology.Substance;
import emlab.gen.domain.technology.SubstanceShareInFuelMix;
import emlab.gen.repository.Reps;
import emlab.gen.role.AbstractEnergyProducerRole;

/**
 * @author Kaveri3012 This role loops through eligible technologies, eligible
 *         nodes,
 * 
 *         computes LCOE per technology per node and creates an object,
 *         BaseCost, to store it.
 * 
 *         In technology neutral mode, after computing LCOE per technology, it
 *         should store LCOE per technology and create a merit order upto which
 *         a cetrain target is filled.
 * 
 */

@RoleComponent
public class ComputePremiumRole extends AbstractEnergyProducerRole<EnergyProducer>implements Role<EnergyProducer> {

    @Transient
    @Autowired
    Reps reps;

    @Transient
    @Autowired
    Neo4jTemplate template;

    @SuppressWarnings("unchecked")
    @Transactional
    public void act(RenewableSupportFipScheme scheme) {

        Regulator regulator = scheme.getRegulator();

        ElectricitySpotMarket eMarket = reps.marketRepository.findElectricitySpotMarketForZone(regulator.getZone());

        Iterable<PowerGeneratingTechnology> eligibleTechnologies = scheme.getPowerGeneratingTechnologiesEligible();

        for (PowerGeneratingTechnology technology : eligibleTechnologies) {
            // for (PowerGeneratingTechnology technology :
            // reps.powerGeneratingTechnologyRepository.findAll()) {
            DecarbonizationModel model = reps.genericRepository.findAll(DecarbonizationModel.class).iterator().next();
            if (technology.isIntermittent() && model.isNoPrivateIntermittentRESInvestment())
                continue;
            Iterable<PowerGridNode> possibleInstallationNodes;

            /*
             * For dispatchable technologies just choose a random node. For
             * intermittent evaluate all possibilities.
             */
            if (technology.isIntermittent())
                possibleInstallationNodes = reps.powerGridNodeRepository
                        .findAllPowerGridNodesByZone(regulator.getZone());
            else {
                possibleInstallationNodes = new LinkedList<PowerGridNode>();
                ((LinkedList<PowerGridNode>) possibleInstallationNodes).add(reps.powerGridNodeRepository
                        .findAllPowerGridNodesByZone(regulator.getZone()).iterator().next());
            }
            logger.warn(
                    "Calculating for " + technology.getName() + ", for Nodes: " + possibleInstallationNodes.toString());

            for (PowerGridNode node : possibleInstallationNodes) {

                // or create a new power plant if above statement returns null,
                // and assign it to a random energy producer.
                PowerPlant plant = new PowerPlant();

                EnergyProducer producer = reps.energyProducerRepository.findAll().iterator().next();

                plant.specifyNotPersist(getCurrentTick(), producer, node, technology);
                logger.warn("creating a new power plant for " + producer.getName() + ", of technology "
                        + plant.getTechnology().getName());

                Map<Substance, Double> myFuelPrices = new HashMap<Substance, Double>();
                for (Substance fuel : technology.getFuels()) {
                    myFuelPrices.put(fuel, findLastKnownPriceForSubstance(fuel, getCurrentTick()));
                }

                Set<SubstanceShareInFuelMix> fuelMix = calculateFuelMix(plant, myFuelPrices,
                        findLastKnownCO2Price(getCurrentTick()));
                plant.setFuelMix(fuelMix);

                double mc = 0d;
                double annualMarginalCost = 0d;
                double totalGenerationinMWh = 0d;
                double lcoe = 0d;
                long numberOfSegments = reps.segmentRepository.count();
                double factor = 0d;
                double fullLoadHours = 0d;

                mc = calculateMarginalCostExclCO2MarketCost(plant, getCurrentTick());
                for (SegmentLoad segmentLoad : eMarket.getLoadDurationCurve()) {
                    double hours = segmentLoad.getSegment().getLengthInHours();
                    Segment segment = segmentLoad.getSegment();

                    if (hours == 0) {

                        if (technology.isIntermittent()) {
                            factor = plant.getIntermittentTechnologyNodeLoadFactor().getLoadFactorForSegment(segment);
                        } else {
                            double segmentID = segment.getSegmentID();
                            double min = technology.getPeakSegmentDependentAvailability();
                            double max = technology.getBaseSegmentDependentAvailability();
                            double segmentPortion = (numberOfSegments - segmentID) / (numberOfSegments - 1); // start
                            // counting
                            // at
                            // 1.

                            double range = max - min;
                            factor = max - segmentPortion * range;

                        }

                        fullLoadHours += factor * segment.getLengthInHours();

                    }

                }

                totalGenerationinMWh = fullLoadHours * plant.getActualNominalCapacity();
                annualMarginalCost = totalGenerationinMWh * mc;

                logger.warn("Annual Marginal cost for technology " + plant.getTechnology().getName() + " is  "
                        + annualMarginalCost + " and total generation is  " + totalGenerationinMWh);

                double fixedOMCost = calculateFixedOperatingCost(plant, getCurrentTick());
                double operatingCost = fixedOMCost + annualMarginalCost;

                logger.warn("Fixed OM cost for technology " + plant.getTechnology().getName() + " is  " + fixedOMCost
                        + " and operatingCost is  " + operatingCost);

                TreeMap<Integer, Double> discountedProjectCapitalOutflow = calculateSimplePowerPlantInvestmentCashFlow(
                        technology.getDepreciationTime(), (int) plant.getActualLeadtime(),
                        plant.getActualInvestedCapital(), 0);

                // Creation of in cashflow during operation
                TreeMap<Integer, Double> discountedProjectCashOutflow = calculateSimplePowerPlantInvestmentCashFlow(
                        technology.getDepreciationTime(), (int) plant.getActualLeadtime(), 0, operatingCost);

                // Calculation of weighted average cost of capital,
                // based on regulator's assumption of companies debt-ratio
                double wacc = (1 - regulator.getDebtRatioOfInvestments()) * regulator.getEquityInterestRate()
                        + regulator.getDebtRatioOfInvestments() * regulator.getLoanInterestRate();

                double discountedCapitalCosts = npv(discountedProjectCapitalOutflow, wacc);
                logger.warn("discountedCapitalCosts " + discountedCapitalCosts);
                double discountedOpCost = npv(discountedProjectCashOutflow, wacc);
                logger.warn("discountedOpCost " + discountedOpCost);
                lcoe = (discountedCapitalCosts + discountedOpCost) * scheme.getFeedInPremiumBiasFactor()
                        / (totalGenerationinMWh * scheme.getSupportSchemeDuration());

                BaseCostFip baseCostFip = new BaseCostFip();

                baseCostFip.setCostPerMWh(lcoe);
                baseCostFip.setStartTime(getCurrentTick());
                baseCostFip.setNode(node);
                baseCostFip.setTechnology(technology);
                baseCostFip.setEndTime(getCurrentTick() + scheme.getSupportSchemeDuration());

                logger.warn("LCOE in per MWH for technology " + plant.getTechnology().getName() + "for node "
                        + baseCostFip.getNode().getNodeId() + " is , " + baseCostFip.getCostPerMWh());

            }
        }

    }

    private TreeMap<Integer, Double> calculateSimplePowerPlantInvestmentCashFlow(int depreciationTime, int buildingTime,
            double totalInvestment, double operatingProfit) {
        TreeMap<Integer, Double> investmentCashFlow = new TreeMap<Integer, Double>();
        double equalTotalDownPaymentInstallement = totalInvestment / buildingTime;
        for (int i = 0; i < buildingTime; i++) {
            investmentCashFlow.put(new Integer(i), equalTotalDownPaymentInstallement);
        }
        for (int i = buildingTime; i < depreciationTime + buildingTime; i++) {
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

    /*
     * (non-Javadoc)
     * 
     * @see agentspring.role.Role#act(agentspring.agent.Agent)
     */
    @Override
    public void act(EnergyProducer arg0) {
        // TODO Auto-generated method stub

    }

}
