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
package emlab.gen.role.capacitymarket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import agentspring.role.Role;
import agentspring.role.RoleComponent;
import emlab.gen.domain.contract.CashFlow;
import emlab.gen.domain.market.ClearingPoint;
import emlab.gen.domain.market.capacity.CapacityDispatchPlan;
import emlab.gen.domain.market.capacity.CapacityMarket;
import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.repository.Reps;
import emlab.gen.role.market.AbstractMarketRole;

//import org.springframework.data.neo4j.annotation.NodeEntity;

/**
 * @author Kaveri
 * 
 */
@RoleComponent
public class PaymentFromConsumerToProducerForCapacityRole extends AbstractMarketRole<CapacityMarket> implements
        Role<CapacityMarket> {

    @Autowired
    Reps reps;

    // CashFlow cash = new CashFlow();

    @Override
    @Transactional
    public void act(CapacityMarket capacityMarket) {

        for (CapacityDispatchPlan plan : reps.capacityMarketRepository.findAllAcceptedCapacityDispatchPlansForTime(
                capacityMarket, getCurrentTick())) {

            // logger.warn("Hi");
            // logger.warn("cdp for plant" + plan.getPlant());

            ClearingPoint capacityClearingPoint = reps.capacityMarketRepository
                    .findOneClearingPointForTimeAndCapacityMarket(getCurrentTick(), capacityMarket);

            // logger.warn("capacity clearing point " +
            // capacityClearingPoint.getPrice());
            // double price = capacityClearingPoint.getPrice();
            ElectricitySpotMarket esm = reps.marketRepository
                    .findElectricitySpotMarketForZone(capacityMarket.getZone());
            // logger.warn("esmt " + esm.getName());

            reps.nonTransactionalCreateRepository.createCashFlow(esm, plan.getBidder(), plan.getAcceptedAmount()
                    * capacityClearingPoint.getPrice(), CashFlow.SIMPLE_CAPACITY_MARKET, getCurrentTick(),
                    plan.getPlant());
            // logger.warn("Cash flow from consumer {} to Producer {} of value {} "
            // + plan.getAcceptedAmount()
            // * capacityClearingPoint.getPrice(), plan.getBidder(),
            // capacityMarket.getConsumer());
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see emlab.gen.role.market.AbstractMarketRole#getReps()
     */
    @Override
    public Reps getReps() {

        return reps;

    }

}
