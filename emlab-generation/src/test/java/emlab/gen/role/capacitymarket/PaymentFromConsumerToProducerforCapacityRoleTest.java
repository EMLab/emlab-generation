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

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import emlab.gen.domain.agent.EnergyConsumer;
import emlab.gen.domain.agent.EnergyProducer;
import emlab.gen.domain.gis.Zone;
import emlab.gen.domain.market.capacity.CapacityClearingPoint;
import emlab.gen.domain.market.capacity.CapacityDispatchPlan;
import emlab.gen.domain.market.capacity.CapacityMarket;
import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.domain.technology.PowerPlant;
import emlab.gen.repository.Reps;

/**
 * @author Kaveri
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/emlab-gen-test-context.xml" })
@Transactional
public class PaymentFromConsumerToProducerforCapacityRoleTest {

    @Autowired
    Reps reps;

    /**
     * 
     */

    @Autowired
    PaymentFromConsumerToProducerForCapacityRole paymentFromConsumerToProducerForCapacityRole;

    Logger logger = Logger.getLogger(PaymentFromConsumerToProducerForCapacityRole.class);

    @Test
    public void capacityMarketPaymentFunctionality() {

        Zone zone = new Zone();
        zone.persist();

        EnergyProducer ep1 = new EnergyProducer();
        EnergyProducer ep2 = new EnergyProducer();
        ep1.persist();
        ep2.persist();

        EnergyConsumer consumer = new EnergyConsumer();
        consumer.persist();

        CapacityMarket market = new CapacityMarket();
        market.setConsumer(consumer);
        market.setZone(zone);
        market.persist();

        ElectricitySpotMarket esm = new ElectricitySpotMarket();
        esm.setZone(zone);
        esm.persist();

        CapacityClearingPoint clearingPoint = new CapacityClearingPoint();
        clearingPoint.setCapacityMarket(market);
        clearingPoint.setPrice(1);
        clearingPoint.setTime(0l);
        clearingPoint.persist();

        PowerPlant pp1 = new PowerPlant();
        PowerPlant pp2 = new PowerPlant();
        PowerPlant pp3 = new PowerPlant();
        PowerPlant pp4 = new PowerPlant();

        pp1.persist();
        pp2.persist();
        pp3.persist();
        pp4.persist();

        CapacityDispatchPlan cdp1 = new CapacityDispatchPlan();
        cdp1.setBidder(ep1);
        cdp1.setBiddingMarket(market);
        cdp1.setTime(0l);
        cdp1.setAcceptedAmount(100);
        cdp1.setPlant(pp1);
        cdp1.setStatus(3);
        cdp1.persist();

        CapacityDispatchPlan cdp2 = new CapacityDispatchPlan();
        cdp2.setBidder(ep1);
        cdp2.setBiddingMarket(market);
        cdp2.setTime(0l);
        cdp2.setAcceptedAmount(80);
        cdp2.setPlant(pp2);
        cdp2.setStatus(2);
        cdp2.persist();

        CapacityDispatchPlan cdp3 = new CapacityDispatchPlan();
        cdp3.setBidder(ep2);
        cdp3.setBiddingMarket(market);
        cdp3.setTime(0l);
        cdp3.setAcceptedAmount(150);
        cdp3.setPlant(pp3);
        cdp3.setStatus(3);
        cdp3.persist();

        CapacityDispatchPlan cdp4 = new CapacityDispatchPlan();
        cdp4.setBidder(ep2);
        cdp4.setBiddingMarket(market);
        cdp4.setTime(0l);
        cdp4.setAcceptedAmount(100);
        cdp4.setPlant(pp4);
        cdp4.setStatus(3);
        cdp4.persist();

        logger.warn("Consumer's Cash before Payment Process " + consumer.getCash());
        logger.warn("Energy Producer1's Cash before Payment Process" + ep1.getCash());
        logger.warn("Energy Producer2's Cash before Payment Process" + ep2.getCash());

        paymentFromConsumerToProducerForCapacityRole.act(market);

        logger.warn("Consumer's Cash After Payment Process " + consumer.getCash());
        logger.warn("Energy Producer1's Cash After Payment Process" + ep1.getCash());
        logger.warn("Energy Producer2's Cash After Payment Process" + ep2.getCash());

    }

}
