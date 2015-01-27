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

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import emlab.gen.domain.agent.EnergyProducer;
import emlab.gen.domain.agent.Regulator;
import emlab.gen.domain.contract.Loan;
import emlab.gen.domain.gis.Zone;
import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.domain.market.electricity.PowerPlantDispatchPlan;
import emlab.gen.domain.market.electricity.Segment;
import emlab.gen.domain.market.electricity.SegmentClearingPoint;
import emlab.gen.domain.market.electricity.SegmentLoad;
import emlab.gen.domain.technology.PowerGeneratingTechnology;
import emlab.gen.domain.technology.PowerPlant;
import emlab.gen.repository.BidRepository;
import emlab.gen.repository.MarketRepository;
import emlab.gen.repository.PowerPlantDispatchPlanRepository;
import emlab.gen.repository.Reps;
import emlab.gen.repository.SegmentLoadRepository;
import emlab.gen.repository.ZoneRepository;
import emlab.gen.trend.TriangularTrend;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/emlab-gen-test-context.xml" })
@Transactional
public class ForecastDemandRoleTest2 {
    Logger logger = Logger.getLogger(ForecastDemandRole.class);

    @Autowired
    Reps reps;

    @Autowired
    SegmentLoadRepository segmentLoadRepository;

    @Autowired
    MarketRepository marketRepository;

    @Autowired
    BidRepository bidRepository;

    @Autowired
    PowerPlantDispatchPlanRepository plantDispatchPlanRepository;

    @Autowired
    ZoneRepository zoneRepository;

    @Autowired
    ForecastDemandRole fDemandRole;

    @Test
    public void ZonalTest() {

        Zone zone1 = new Zone();
        // Zone zone2 = new Zone();
        zone1.setName("Zone 1");

        // zone2.setName("Zone2");

        zone1.persist();

        // zone2.persist();

        Segment S1 = new Segment();
        S1.setLengthInHours(20);
        S1.persist();

        Segment S2 = new Segment();
        S2.setLengthInHours(30);
        S2.persist();

        SegmentLoad SG1 = new SegmentLoad();
        SG1.setSegment(S2);
        SG1.setBaseLoad(2500);

        // SegmentLoad SG2 = new SegmentLoad();
        // SG2.setSegment(S2);
        // SG2.setBaseLoad(2000);

        SegmentLoad SG3 = new SegmentLoad();
        SG3.setSegment(S1);
        SG3.setBaseLoad(3700);

        // SegmentLoad SG4 = new SegmentLoad();
        // SG4.setSegment(S1);
        // SG4.setBaseLoad(4000);

        SG1.persist();
        // SG2.persist();
        SG3.persist();
        // SG4.persist();

        Set<SegmentLoad> segmentLoads1 = new HashSet<SegmentLoad>();
        segmentLoads1.add(SG1);
        segmentLoads1.add(SG3);

        // Set<SegmentLoad> segmentLoads2 = new HashSet<SegmentLoad>();
        // segmentLoads2.add(SG2);
        // segmentLoads2.add(SG4);

        TriangularTrend demandGrowthTrend = new TriangularTrend();
        demandGrowthTrend.setMax(2);
        demandGrowthTrend.setMin(0);
        demandGrowthTrend.setStart(1);
        demandGrowthTrend.setTop(1);

        demandGrowthTrend.persist();

        ElectricitySpotMarket market1 = new ElectricitySpotMarket();
        market1.setName("Market1");
        market1.setZone(zone1);
        market1.setLoadDurationCurve(segmentLoads1);
        market1.setDemandGrowthTrend(demandGrowthTrend);
        market1.persist();

        // ElectricitySpotMarket market2 = new ElectricitySpotMarket();
        // market2.setZone(zone2);
        // market2.setName("Market2");
        // market2.setLoadDurationCurve(segmentLoads2);
        // market2.setDemandGrowthTrend(demandGrowthTrend);
        // market2.persist();

        PowerGeneratingTechnology coal1 = new PowerGeneratingTechnology();

        PowerGeneratingTechnology coal2 = new PowerGeneratingTechnology();

        PowerGeneratingTechnology gas1 = new PowerGeneratingTechnology();

        PowerGeneratingTechnology gas2 = new PowerGeneratingTechnology();

        coal1.persist();
        coal2.persist();
        gas1.persist();
        gas2.persist();

        EnergyProducer e1 = new EnergyProducer();
        e1.setName("E1");
        e1.setCash(0);
        e1.setPriceMarkUp(1);

        EnergyProducer e2 = new EnergyProducer();
        e2.setCash(0);
        e2.setPriceMarkUp(1);
        e2.setName("E2");

        EnergyProducer e3 = new EnergyProducer();
        e3.setCash(0);
        e3.setPriceMarkUp(1);
        e3.setName("E3");

        e1.persist();
        e2.persist();
        e3.persist();

        Loan l1 = new Loan();
        l1.setAmountPerPayment(6000);
        l1.setNumberOfPaymentsDone(10);
        l1.setTotalNumberOfPayments(15);

        Loan l2 = new Loan();
        l2.setAmountPerPayment(5000);
        l2.setNumberOfPaymentsDone(29);
        l2.setTotalNumberOfPayments(19);

        Loan l3 = new Loan();
        l3.setAmountPerPayment(4000);
        l3.setNumberOfPaymentsDone(8);
        l3.setTotalNumberOfPayments(13);

        Loan l4 = new Loan();
        l4.setAmountPerPayment(3000);
        l4.setNumberOfPaymentsDone(7);
        l4.setTotalNumberOfPayments(12);

        Loan l5 = new Loan();
        l5.setAmountPerPayment(2000);
        l5.setNumberOfPaymentsDone(6);
        l5.setTotalNumberOfPayments(11);

        Loan l6 = new Loan();
        l6.setAmountPerPayment(1000);
        l6.setNumberOfPaymentsDone(5);
        l6.setTotalNumberOfPayments(10);

        l1.persist();
        l2.persist();
        l3.persist();
        l4.persist();
        l5.persist();
        l6.persist();

        PowerPlant pp1 = new PowerPlant();
        pp1.setTechnology(coal1);
        pp1.setOwner(e1);
        pp1.setActualFixedOperatingCost(99000);
        pp1.setLoan(l1);
        // pp1.setName("PP1");

        PowerPlant pp2 = new PowerPlant();
        pp2.setTechnology(coal2);
        pp2.setOwner(e2);
        pp2.setActualFixedOperatingCost(111000);
        pp2.setLoan(l2);
        // pp2.setName("PP2");

        PowerPlant pp3 = new PowerPlant();
        pp3.setTechnology(gas1);
        pp3.setOwner(e3);
        pp3.setActualFixedOperatingCost(56000);
        pp3.setLoan(l3);

        PowerPlant pp4 = new PowerPlant();
        pp4.setTechnology(gas2);
        pp4.setOwner(e3);
        pp4.setActualFixedOperatingCost(65000);
        pp4.setLoan(l4);

        PowerPlant pp5 = new PowerPlant();
        pp5.setTechnology(gas1);
        pp5.setOwner(e2);
        pp5.setActualFixedOperatingCost(56000);
        pp5.setLoan(l5);

        PowerPlant pp6 = new PowerPlant();
        pp6.setTechnology(gas2);
        pp6.setOwner(e1);
        pp6.setActualFixedOperatingCost(65000);
        pp6.setLoan(l6);

        pp1.persist();
        pp2.persist();
        pp3.persist();
        pp4.persist();
        pp5.persist();
        pp6.persist();

        // for Zone 1 Segment 1
        PowerPlantDispatchPlan p1 = new PowerPlantDispatchPlan();
        p1.setAmount(1500.0d);
        p1.setSegment(S1);
        p1.setPrice(5.0d);
        p1.setTime(0l);
        p1.setBiddingMarket(market1);
        p1.setPowerPlant(pp1);
        p1.setBidder(e1);
        p1.setStatus(3);
        p1.setAcceptedAmount(1500);
        p1.persist();

        PowerPlantDispatchPlan p11 = new PowerPlantDispatchPlan();
        p11.setAmount(1000.0d);
        p11.setSegment(S1);
        p11.setPrice(15.0d);
        p11.setTime(0l);
        p11.setBiddingMarket(market1);
        p11.setPowerPlant(pp2);
        p11.setBidder(e2);
        p11.setStatus(3);
        p11.setAcceptedAmount(1000);
        p11.persist();

        PowerPlantDispatchPlan p111 = new PowerPlantDispatchPlan();
        p111.setAmount(1200.0d);
        p111.setSegment(S1);
        p111.setPrice(7.0d);
        p111.setTime(0l);
        p111.setBiddingMarket(market1);
        p111.setPowerPlant(pp3);
        p111.setBidder(e3);
        p111.setStatus(3);
        p111.setAcceptedAmount(1200);
        p111.persist();

        // For Zone 1 segment 2
        PowerPlantDispatchPlan p1111 = new PowerPlantDispatchPlan();
        p1111.setAmount(1500.0d);
        p1111.setSegment(S2);
        p1111.setPrice(5.0d);
        p1111.setTime(0l);
        p1111.setBiddingMarket(market1);
        p1111.setPowerPlant(pp1);
        p1111.setBidder(e1);
        p1111.setStatus(3);
        p1111.setAcceptedAmount(1500);
        p1111.persist();

        PowerPlantDispatchPlan p11111 = new PowerPlantDispatchPlan();
        p11111.setAmount(1000.0d);
        p11111.setSegment(S2);
        p11111.setPrice(15.0d);
        p11111.setTime(0l);
        p11111.setBiddingMarket(market1);
        p11111.setPowerPlant(pp2);
        p11111.setBidder(e2);
        p11111.setStatus(-1);
        p11111.setAcceptedAmount(0);
        p11111.persist();

        PowerPlantDispatchPlan p111111 = new PowerPlantDispatchPlan();
        p111111.setAmount(1200.0d);
        p111111.setSegment(S2);
        p111111.setPrice(7.0d);
        p111111.setTime(0l);
        p111111.setBiddingMarket(market1);
        p111111.setPowerPlant(pp3);
        p111111.setBidder(e3);
        p111111.setStatus(2);
        p111111.setAcceptedAmount(1000);
        p111111.persist();

        SegmentClearingPoint clearingPoint1 = new SegmentClearingPoint();
        clearingPoint1.setSegment(S1);
        clearingPoint1.setAbstractMarket(market1);
        clearingPoint1.setPrice(25);
        clearingPoint1.setTime(0l);

        SegmentClearingPoint clearingPoint111 = new SegmentClearingPoint();
        clearingPoint111.setSegment(S2);
        clearingPoint111.setAbstractMarket(market1);
        clearingPoint111.setPrice(7);
        clearingPoint111.setTime(0l);

        clearingPoint1.persist();
        clearingPoint111.persist();

        Regulator regulator = new Regulator();
        regulator.setTargetPeriod(0);
        regulator.setReserveMargin(0.15);
        regulator.setNumberOfYearsLookingBackToForecastDemand(3);
        regulator.setZone(zone1);
        regulator.persist();

        fDemandRole.act(regulator);

        logger.warn("Target Demand for this tick: " + regulator.getDemandTarget());
    }
}