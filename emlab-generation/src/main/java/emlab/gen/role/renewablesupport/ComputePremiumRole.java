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

import java.util.LinkedList;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Transient;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.transaction.annotation.Transactional;

import agentspring.role.Role;
import emlab.gen.domain.agent.DecarbonizationModel;
import emlab.gen.domain.agent.EnergyProducer;
import emlab.gen.domain.agent.Regulator;
import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.domain.market.electricity.PowerPlantDispatchPlan;
import emlab.gen.domain.market.electricity.SegmentLoad;
import emlab.gen.domain.policy.renewablesupport.BaseCostFip;
import emlab.gen.domain.policy.renewablesupport.RenewableSupportFipScheme;
import emlab.gen.domain.technology.PowerGeneratingTechnology;
import emlab.gen.domain.technology.PowerGridNode;
import emlab.gen.domain.technology.PowerPlant;
import emlab.gen.repository.Reps;
import emlab.gen.role.AbstractEnergyProducerRole;

/**
 * @author Kaveri3012 This role computes LCOE per technology per node (where
 *         applicable)
 *
 */
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

        long timePoint = getCurrentTick();

        ElectricitySpotMarket eMarket = reps.marketRepository.findElectricitySpotMarketForZone(regulator.getZone());

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
                possibleInstallationNodes = reps.powerGridNodeRepository
                        .findAllPowerGridNodesByZone(regulator.getZone());
            else {
                possibleInstallationNodes = new LinkedList<PowerGridNode>();
                ((LinkedList<PowerGridNode>) possibleInstallationNodes).add(reps.powerGridNodeRepository
                        .findAllPowerGridNodesByZone(regulator.getZone()).iterator().next());
            }
            // logger.warn("Calculating for " + technology.getName() +
            // ", for Nodes: "
            // + possibleInstallationNodes.toString());

            for (PowerGridNode node : possibleInstallationNodes) {

                // find one random power plant built recently
                PowerPlant plant = reps.powerPlantRepository
                        .findPowerPlantsOperationalSinceTwoYearsByPowerGridNodeAndTechnology(node, technology,
                                getCurrentTick())
                        .iterator().next();

                double mc = 0d;
                double annualMarginalCost = 0d;
                double totalGenerationinMWh = 0d;
                double lcoe = 0d;

                mc = calculateMarginalCostExclCO2MarketCost(plant, getCurrentTick());

                for (SegmentLoad segmentLoad : eMarket.getLoadDurationCurve()) {

                    PowerPlantDispatchPlan ppdp = reps.powerPlantDispatchPlanRepository
                            .findOnePowerPlantDispatchPlanForPowerPlantForSegmentForTime(plant,
                                    segmentLoad.getSegment(), getCurrentTick(), false);
                    if (ppdp.getStatus() < 0) {
                        annualMarginalCost = 0d;
                    } else if (ppdp.getStatus() >= 2) {
                        double hours = segmentLoad.getSegment().getLengthInHours();
                        annualMarginalCost = annualMarginalCost + mc * hours * ppdp.getAcceptedAmount();
                        totalGenerationinMWh = hours * ppdp.getAcceptedAmount();
                    }

                }

                double fixedOMCost = calculateFixedOperatingCost(plant, getCurrentTick());
                double operatingCost = fixedOMCost + annualMarginalCost;

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
                double discountedOpCost = npv(discountedProjectCashOutflow, wacc);
                lcoe = (discountedCapitalCosts + discountedOpCost)
                        / (totalGenerationinMWh * scheme.getSupportSchemeDuration());

                BaseCostFip baseCostFip = new BaseCostFip();

                baseCostFip.setCostPerMWh(lcoe);
                baseCostFip.setStartTime(getCurrentTick());
                baseCostFip.setNode(node);
                baseCostFip.setTechnology(technology);
                baseCostFip.setEndTime(getCurrentTick() + scheme.getSupportSchemeDuration());

            }
        }

    }

    private TreeMap<Integer, Double> calculateSimplePowerPlantInvestmentCashFlow(int depriacationTime, int buildingTime,
            double totalInvestment, double operatingProfit) {
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
