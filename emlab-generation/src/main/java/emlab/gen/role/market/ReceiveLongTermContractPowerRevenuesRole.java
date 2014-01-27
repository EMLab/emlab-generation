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
import emlab.gen.domain.agent.EnergyConsumer;
import emlab.gen.domain.agent.EnergyProducer;
import emlab.gen.domain.contract.CashFlow;
import emlab.gen.domain.contract.LongTermContract;
import emlab.gen.domain.gis.Zone;
import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.domain.market.electricity.Segment;
import emlab.gen.domain.technology.Substance;
import emlab.gen.repository.Reps;
import emlab.gen.role.AbstractEnergyProducerRole;

/**
 * Creates and clears the {@link ElectricitySpotMarket} for one {@link Zone}. {@link EnergyConsumer} submit bids to purchase electricity; {@link EnergyProducer} submit ask offers to sell power. The
 * market is divided into {@link Segment}s and cleared for each segment.
 * 
 * @author <a href="mailto:E.J.L.Chappin@tudelft.nl">Emile Chappin</a>
 * 
 * @author <a href="mailto:A.Chmieliauskas@tudelft.nl">Alfredas Chmieliauskas</a>
 * 
 */
@RoleComponent
public class ReceiveLongTermContractPowerRevenuesRole extends AbstractEnergyProducerRole implements Role<EnergyProducer> {

    @Autowired
    Reps reps;

    @Override
    @Transactional
    public void act(EnergyProducer producer) {

        logger.info("Process electricity revenues");

        // Receive revenues for all long term contracts
        for (Segment segment : reps.genericRepository.findAll(Segment.class)) {
            for (LongTermContract longTermContract : reps.contractRepository.findLongTermContractsForEnergyProducerForSegmentActiveAtTime(
                    producer, segment, getCurrentTick())) {

                // Update the price with pass through factors.
                double basePrice = longTermContract.getCapacity() * longTermContract.getPricePerUnit() * segment.getLengthInHours();
                double co2PassThrough = longTermContract.getCo2PassThroughFactor();
                double co2PriceStart = longTermContract.getCo2PriceStart();
                double currentCo2Price = findLastKnownCO2Price(getCurrentTick());
                double fuelPassThrough = longTermContract.getFuelPassThroughFactor();
                double fuelPriceStart = longTermContract.getFuelPriceStart();
                double currentFuelPrice = 0d;
                Substance mainFuel = longTermContract.getMainFuel();
                if (mainFuel != null) {
                    currentFuelPrice = findLastKnownPriceForSubstance(mainFuel, getCurrentTick());
                }

                // prevent dividing by 0
                if (fuelPriceStart == 0) {
                    fuelPriceStart = 1e-8;
                }
                if (co2PriceStart == 0) {
                    co2PriceStart = 1e-8;
                }

                double updatedPrice = basePrice * (1 + fuelPassThrough * (currentFuelPrice / fuelPriceStart - 1))
                        * (1 + co2PassThrough * (currentCo2Price / co2PriceStart - 1));

                reps.nonTransactionalCreateRepository.createCashFlow(longTermContract.getTo(), longTermContract.getFrom(), updatedPrice,
                        CashFlow.ELECTRICITY_LONGTERM, getCurrentTick(), null);

                long hours = 0;
                for (Segment s : longTermContract.getLongTermContractType().getSegments()) {
                    hours += s.getLengthInHours();
                }

                double pricePerMWh = updatedPrice / (longTermContract.getCapacity() * hours);
                logger.info("Revenue from long term contract @ {} euro/MWh", pricePerMWh);
            }
        }

        // Receive revenues for all spot trade

        // for (PowerPlantDispatchPlan plan : reps.powerPlantDispatchPlanRepository
        // .findAllAcceptedPowerPlantDispatchPlansForEnergyProducerForTime(producer, getCurrentTick())) {
        // logger.info("Found dispatch plan: {}", plan);
        // if (plan.getAcceptedAmount() > 0) {
        // double price = 0d;
        // for (ClearingPoint point : reps.clearingPointRepositoryOld.findClearingPointsForSegmentAndTime(plan.getSegment(),
        // getCurrentTick())) {
        // SegmentClearingPoint cp = (SegmentClearingPoint) point;
        // if (cp.getSegment().equals(plan.getSegment())) {
        // price = cp.getPrice();
        // }
        // }
        // logger.info("Revenue from spot market @ {} euro/MWh", price);
        // reps.nonTransactionalCreateRepository.createCashFlow(plan.getBiddingMarket(), plan.getBidder(), plan.getAcceptedAmount()
        // * plan.getSegment().getLengthInHours() * price, CashFlow.ELECTRICITY_SPOT, getCurrentTick(), plan.getPowerPlant());
        // }
        // }

    }

}
