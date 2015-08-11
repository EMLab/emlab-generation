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

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Transient;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.transaction.annotation.Transactional;

import agentspring.role.AbstractRole;
import agentspring.role.RoleComponent;
import emlab.gen.domain.agent.Regulator;
import emlab.gen.domain.contract.CashFlow;
import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.domain.market.electricity.PowerPlantDispatchPlan;
import emlab.gen.domain.market.electricity.SegmentLoad;
import emlab.gen.domain.policy.renewablesupport.BaseCostFip;
import emlab.gen.domain.policy.renewablesupport.RenewableSupportFipScheme;
import emlab.gen.domain.policy.renewablesupport.SupportPriceContract;
import emlab.gen.domain.technology.PowerGeneratingTechnology;
import emlab.gen.domain.technology.PowerGridNode;
import emlab.gen.domain.technology.PowerPlant;
import emlab.gen.repository.Reps;

/**
 * @author Kaveri3012 for loop through eligible, operational power plants,
 *         create support price contract for each technology SupportPrice =
 *         electricityMarketPrice*(1+premiumFactor) for 15 years?
 * 
 *         Assumption: when the policy is implemented for a certain country, all
 *         operational, eligible plants in that zone receive the premium by
 *         default. there is no need for an energy producer agent to voluntarily
 *         apply for the scheme.
 * 
 * 
 */
@RoleComponent
public class FeedInPremiumRole extends AbstractRole<RenewableSupportFipScheme> {

    @Transient
    @Autowired
    Reps reps;

    @Transient
    @Autowired
    Neo4jTemplate template;

    @Transactional
    public void act(RenewableSupportFipScheme renewableSupportScheme) {

        Regulator regulator = new Regulator();
        regulator = renewableSupportScheme.getRegulator();

        Set<PowerGeneratingTechnology> technologySet = new HashSet<PowerGeneratingTechnology>();
        technologySet = renewableSupportScheme.getPowerGeneratingTechnologiesEligible();

        ElectricitySpotMarket eMarket = reps.marketRepository.findElectricitySpotMarketForZone(regulator.getZone());
        SupportPriceContract contract = null;

        for (PowerGeneratingTechnology technology : technologySet) {

            Iterable<PowerGridNode> possibleInstallationNodes = reps.powerGridNodeRepository
                    .findAllPowerGridNodesByZone(regulator.getZone());

            logger.warn("Calculating FEED IN PREMIUM for " + technology.getName() + ", for Nodes: "
                    + possibleInstallationNodes.toString());

            for (PowerGridNode node : possibleInstallationNodes) {

                logger.warn("Inside power grid node loop");

                Iterable<PowerPlant> plantSet;

                if (getCurrentTick() >= 1) {
                    plantSet = reps.powerPlantRepository
                            .findPowerPlantsStartingOperationThisTickByPowerGridNodeAndTechnology(node, technology,
                                    getCurrentTick());
                } else {
                    plantSet = reps.powerPlantRepository.findOperationalPowerPlantsByPowerGridNodeAndTechnology(node,
                            technology, getCurrentTick());
                }

                // query to find power plants by node and technology who have
                // finished construction this tick
                for (PowerPlant plant : plantSet) {

                    long finishedConstruction = plant.getConstructionStartTime() + plant.calculateActualPermittime()
                            + plant.calculateActualLeadtime();

                    logger.warn("Found power plants starting operation this year, Printing finished construction"
                            + finishedConstruction + "and current tick " + getCurrentTick());
                    // long timeNow = getCurrentTick();

                    logger.warn("Inside contract creation loop");
                    // create a query to get base cost.
                    BaseCostFip baseCost = reps.baseCostFipRepository.findOneBaseCostForTechnologyAndNodeAndTime(node,
                            technology, getCurrentTick());
                    contract = new SupportPriceContract();
                    contract.setStart(getCurrentTick());
                    contract.setPricePerUnit(baseCost.getCostPerMWh());
                    contract.setFinish(getCurrentTick() + renewableSupportScheme.getSupportSchemeDuration());
                    contract.setPlant(plant);
                    contract.persist();

                    logger.warn("Contract price for plant of technology " + plant.getTechnology().getName()
                            + "for node " + node.getNodeId() + " is , " + contract.getPricePerUnit());

                }

                for (PowerPlant plant : reps.powerPlantRepository
                        .findOperationalPowerPlantsByPowerGridNodeAndTechnology(node, technology, getCurrentTick())) {
                    // .findAllPowerPlantsWithConstructionStartTimeInTick(getCurrentTick())
                    // //findOperationalPowerPlantsByMarketAndTechnology(eMarket,
                    // technology, getCurrentTick())) {

                    logger.warn("Inside power plant loop for power plant" + plant.getName());

                    // existing eligible plants at the start of the simulation
                    // (tick
                    // 0) do not get contracts.

                    // if plant is new (begins operation this year), get
                    // corresponding base cost, and create supportPriceContract
                    // for it, with base cost, start tick and end tick.

                    // for all eligible plants, the support price is calculated,
                    // and
                    // payment is made.
                    contract = reps.supportPriceContractRepository.findOneContractByPowerPlant(plant);
                    if (contract != null) {
                        if (getCurrentTick() <= (contract.getStart()
                                + renewableSupportScheme.getSupportSchemeDuration())) {
                            logger.warn("Inside contract payment loop");
                            double sumEMR = 0d;
                            double electricityPrice = 0d;
                            double totalGenerationInMwh = 0d;

                            // the for loop below calculates the electricity
                            // market
                            // price the plant earned
                            // throughout the year, for its total production
                            for (SegmentLoad segmentLoad : eMarket.getLoadDurationCurve()) {
                                // logger.warn("Inside segment loop for
                                // calculating
                                // total production");
                                PowerPlantDispatchPlan ppdp = reps.powerPlantDispatchPlanRepository
                                        .findOnePowerPlantDispatchPlanForPowerPlantForSegmentForTime(plant,
                                                segmentLoad.getSegment(), getCurrentTick(), false);
                                if (ppdp.getStatus() < 0) {
                                    sumEMR = 0d;
                                } else if (ppdp.getStatus() >= 2) {
                                    electricityPrice = reps.segmentClearingPointRepository
                                            .findOneSegmentClearingPointForMarketSegmentAndTime(getCurrentTick(),
                                                    segmentLoad.getSegment(), eMarket, false)
                                            .getPrice();

                                    double hours = segmentLoad.getSegment().getLengthInHours();
                                    sumEMR = sumEMR + electricityPrice * hours * ppdp.getAcceptedAmount();
                                    totalGenerationInMwh += hours * ppdp.getAcceptedAmount();

                                }

                            }

                            double supportPrice = contract.getPricePerUnit() * totalGenerationInMwh - sumEMR;
                            // payment
                            logger.warn("Total subsidy for plant of technology " + plant.getTechnology().getName()
                                    + "for node " + node.getNodeId() + " is , " + supportPrice);
                            reps.nonTransactionalCreateRepository.createCashFlow(eMarket, plant.getOwner(),
                                    supportPrice, CashFlow.FEED_IN_PREMIUM, getCurrentTick(), plant);

                        }
                        // delete contract. not sure if necessary. contract has
                        // been mainly used to control period of payment
                        if (getCurrentTick() > (contract.getStart()
                                + renewableSupportScheme.getSupportSchemeDuration())) {
                            contract = null;
                        }

                    }

                }

            }

        }
    }
}
