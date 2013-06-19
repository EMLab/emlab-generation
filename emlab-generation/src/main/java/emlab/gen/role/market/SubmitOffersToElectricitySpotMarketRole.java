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
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import agentspring.role.Role;
import agentspring.role.RoleComponent;
import emlab.gen.domain.agent.EnergyProducer;
import emlab.gen.domain.agent.Government;
import emlab.gen.domain.market.Bid;
import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.domain.market.electricity.PowerPlantDispatchPlan;
import emlab.gen.domain.market.electricity.Segment;
import emlab.gen.domain.market.electricity.SegmentLoad;
import emlab.gen.domain.technology.PowerPlant;
import emlab.gen.domain.technology.Substance;
import emlab.gen.domain.technology.SubstanceShareInFuelMix;
import emlab.gen.repository.Reps;
import emlab.gen.role.AbstractEnergyProducerRole;

/**
 * {@link EnergyProducer} submits offers to the {@link ElectricitySpotMarket}. One {@link Bid} per {@link PowerPlant}.
 * 
 * @author <a href="mailto:A.Chmieliauskas@tudelft.nl">Alfredas Chmieliauskas</a> @author <a href="mailto:E.J.L.Chappin@tudelft.nl">Emile Chappin</a>
 * 
 */
@RoleComponent
public class SubmitOffersToElectricitySpotMarketRole extends AbstractEnergyProducerRole implements Role<EnergyProducer> {

    @Autowired
    Reps reps;

    @Override
    @Transactional
    public void act(EnergyProducer producer) {

        long numberOfSegments = reps.segmentRepository.count();
        ElectricitySpotMarket market = producer.getInvestorMarket();

        // find all my operating power plants
        for (PowerPlant plant : reps.powerPlantRepository.findOperationalPowerPlantsByOwner(producer, getCurrentTick())) {

            // get market for the plant by zone
            // ElectricitySpotMarket market =
            // reps.marketRepository.findElectricitySpotMarketForZone(plant.getLocation().getZone());

            double mc = calculateMarginalCostExclCO2MarketCost(plant);
            double price = mc * producer.getPriceMarkUp();

            logger.info("Submitting offers for {} with technology {}", plant.getName(), plant.getTechnology().getName());

            for (SegmentLoad segmentload : market.getLoadDurationCurve()) {

                Segment segment = segmentload.getSegment();
                double capacity = plant.getAvailableCapacity(getCurrentTick(), segment, numberOfSegments);
                logger.info("I bid capacity: {} and price: {}", capacity, mc);

                PowerPlantDispatchPlan plan = reps.powerPlantDispatchPlanRepository
                        .findOnePowerPlantDispatchPlanForPowerPlantForSegmentForTime(plant, segment, getCurrentTick());
                // TODO: handle exception

                // plan =
                // reps.powerPlantDispatchPlanRepository.findOnePowerPlantDispatchPlanForPowerPlantForSegmentForTime(plant,
                // segment,
                // getCurrentTick());
                // Iterable<PowerPlantDispatchPlan> plans =
                // reps.powerPlantDispatchPlanRepository
                // .findAllPowerPlantDispatchPlanForPowerPlantForSegmentForTime(plant,
                // segment, getCurrentTick());

                if (plan == null) {
                    plan = new PowerPlantDispatchPlan().persist();
                    // plan.specifyNotPersist(plant, producer, market, segment, time, price, bidWithoutCO2, spotMarketCapacity, longTermContractCapacity, status);
                    plan.specifyNotPersist(plant, producer, market, segment, getCurrentTick(), price, price, capacity, 0, Bid.SUBMITTED);
                } else {
                    // plan = plans.iterator().next();
                    plan.setBidder(producer);
                    plan.setBiddingMarket(market);
                    plan.setPrice(mc);
                    plan.setBidWithoutCO2(mc);
                    plan.setAmount(capacity);
                    plan.setCapacityLongTermContract(0d);
                    plan.setStatus(Bid.SUBMITTED);
                }

                logger.info("Submitted {} for iteration {} to electricity spot market", plan);

            }
        }
    }

    @Transactional
    void updateMarginalCostInclCO2AfterFuelMixChange(double co2Price, Map<ElectricitySpotMarket, Double> nationalMinCo2Prices) {

        int i = 0;
        int j = 0;

        Government government = reps.template.findAll(Government.class).iterator().next();
        for (PowerPlantDispatchPlan plan : reps.powerPlantDispatchPlanRepository.findAllPowerPlantDispatchPlansForTime(getCurrentTick())) {
            j++;

            double capacity = plan.getAmount();
            if (nationalMinCo2Prices.get(plan.getBiddingMarket()) > co2Price)
                co2Price = nationalMinCo2Prices.get(plan.getBiddingMarket());

            if (plan.getPowerPlant().getFuelMix().size() > 1) {

                double oldmc = plan.getBidWithoutCO2();

                // Fuels
                Set<Substance> possibleFuels = plan.getPowerPlant().getTechnology().getFuels();
                Map<Substance, Double> substancePriceMap = new HashMap<Substance, Double>();

                for (Substance substance : possibleFuels) {
                    substancePriceMap.put(substance, findLastKnownPriceForSubstance(substance));
                }
                Set<SubstanceShareInFuelMix> fuelMix = calculateFuelMix(plan.getPowerPlant(), substancePriceMap,
                        government.getCO2Tax(getCurrentTick()) + co2Price);
                plan.getPowerPlant().setFuelMix(fuelMix);
                double mc = calculateMarginalCostExclCO2MarketCost(plan.getPowerPlant());
                if (mc != oldmc) {
                    plan.setBidWithoutCO2(mc);
                    i++;
                }

            }

            plan.setPrice(plan.getBidWithoutCO2() + (co2Price * plan.getPowerPlant().calculateEmissionIntensity()));

            plan.setStatus(Bid.SUBMITTED);
            plan.setAmount(capacity);
            plan.setCapacityLongTermContract(0d);

        }

        //logger.warn("Marginal cost of {} of {} plans changed", i, j);

    }

}
