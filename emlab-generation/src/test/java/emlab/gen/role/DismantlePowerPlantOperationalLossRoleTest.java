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
package emlab.gen.role;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import emlab.gen.domain.agent.EnergyProducer;
import emlab.gen.domain.contract.CashFlow;
import emlab.gen.domain.gis.Zone;
import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.domain.market.electricity.Segment;
import emlab.gen.domain.market.electricity.SegmentLoad;
import emlab.gen.domain.technology.Interconnector;
import emlab.gen.domain.technology.PowerGeneratingTechnology;
import emlab.gen.domain.technology.PowerGridNode;
import emlab.gen.domain.technology.PowerPlant;
import emlab.gen.repository.Reps;
import emlab.gen.role.investment.DismantlePowerPlantOperationalLossRole;
import emlab.gen.trend.TriangularTrend;

/**
 * @author pradyumnabhagwat
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/emlab-gen-test-context.xml" })
@Transactional
public class DismantlePowerPlantOperationalLossRoleTest {

    @Autowired
    Reps reps;

    @Autowired
    DismantlePowerPlantOperationalLossRole dismantlePowerPlantOperationalLossRole;

    @Test
    public void testDismantlePowerPlantOperationalLossRole() {

        Zone zone1 = new Zone();
        Zone zone2 = new Zone();

        zone1.setName("Zone 1");
        zone2.setName("Zone2");

        zone1.persist();
        zone2.persist();

        PowerGridNode pg1 = new PowerGridNode();
        pg1.setZone(zone1);

        PowerGridNode pg2 = new PowerGridNode();
        pg2.setZone(zone2);

        pg1.persist();
        pg2.persist();

        Interconnector it = new Interconnector();
        it.setCapacity(0);
        it.persist();

        Segment S1 = new Segment();
        S1.setLengthInHours(20);
        S1.persist();

        Segment S2 = new Segment();
        S2.setLengthInHours(30);
        S2.persist();

        SegmentLoad SG1 = new SegmentLoad();
        SG1.setSegment(S2);
        SG1.setBaseLoad(2500);

        SegmentLoad SG2 = new SegmentLoad();
        SG2.setSegment(S2);
        SG2.setBaseLoad(2000);

        SegmentLoad SG3 = new SegmentLoad();
        SG3.setSegment(S1);
        SG3.setBaseLoad(3700);

        SegmentLoad SG4 = new SegmentLoad();
        SG4.setSegment(S1);
        SG4.setBaseLoad(4000);

        SG1.persist();
        SG2.persist();
        SG3.persist();
        SG4.persist();

        Set<SegmentLoad> segmentLoads1 = new HashSet<SegmentLoad>();
        segmentLoads1.add(SG1);
        segmentLoads1.add(SG3);

        Set<SegmentLoad> segmentLoads2 = new HashSet<SegmentLoad>();
        segmentLoads2.add(SG2);
        segmentLoads2.add(SG4);

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
        market1.setLookback(0);
        market1.persist();

        ElectricitySpotMarket market2 = new ElectricitySpotMarket();
        market2.setZone(zone2);
        market2.setName("Market2");
        market2.setLoadDurationCurve(segmentLoads2);
        market2.setDemandGrowthTrend(demandGrowthTrend);
        market2.setLookback(0);
        market2.persist();

        PowerGeneratingTechnology coal1 = new PowerGeneratingTechnology();
        coal1.setExpectedLifetime(35);
        coal1.setPeakSegmentDependentAvailability(1);
        coal1.setCapacity(2500);
        coal1.setExpectedLeadtime(1);
        coal1.setExpectedPermittime(1);

        PowerGeneratingTechnology coal2 = new PowerGeneratingTechnology();
        coal2.setExpectedLifetime(30);
        coal2.setPeakSegmentDependentAvailability(1);
        coal2.setCapacity(2500);
        coal2.setExpectedLeadtime(1);
        coal2.setExpectedPermittime(1);

        PowerGeneratingTechnology gas1 = new PowerGeneratingTechnology();
        gas1.setExpectedLifetime(20);
        gas1.setPeakSegmentDependentAvailability(1);
        gas1.setCapacity(2500);
        gas1.setExpectedLeadtime(1);
        gas1.setExpectedPermittime(1);

        PowerGeneratingTechnology gas2 = new PowerGeneratingTechnology();
        gas2.setExpectedLifetime(15);
        gas2.setPeakSegmentDependentAvailability(1);
        gas2.setCapacity(2500);
        gas2.setExpectedLeadtime(1);
        gas2.setExpectedPermittime(1);

        coal1.persist();
        coal2.persist();
        gas1.persist();
        gas2.persist();

        EnergyProducer e1 = new EnergyProducer();
        e1.setName("E1");
        e1.setCash(0);
        e1.setPriceMarkUp(1);
        e1.setInvestorMarket(market1);

        EnergyProducer e2 = new EnergyProducer();
        e2.setCash(0);
        e2.setPriceMarkUp(1);
        e2.setName("E2");
        e2.setInvestorMarket(market1);

        EnergyProducer e3 = new EnergyProducer();
        e3.setCash(0);
        e3.setPriceMarkUp(1);
        e3.setName("E3");
        e3.setInvestorMarket(market2);

        e1.persist();
        e2.persist();
        e3.persist();

        PowerPlant pp1 = new PowerPlant();
        pp1.setTechnology(coal1);
        pp1.setOwner(e1);
        pp1.setActualLifetime(28);
        pp1.setAgeFraction(0);
        pp1.setProfitability(0);
        pp1.setConstructionStartTime(0);
        pp1.setActualPermittime(0);
        pp1.setActualLeadtime(0);
        pp1.setDismantleTime(2);
        pp1.setLocation(pg1);

        PowerPlant pp2 = new PowerPlant();
        pp2.setTechnology(coal2);
        pp2.setOwner(e2);
        pp2.setActualLifetime(29);
        pp2.setAgeFraction(0);
        pp2.setProfitability(0);
        pp2.setConstructionStartTime(0);
        pp2.setActualPermittime(0);
        pp2.setActualLeadtime(0);
        pp2.setDismantleTime(2);
        pp2.setLocation(pg1);

        PowerPlant pp3 = new PowerPlant();
        pp3.setTechnology(gas1);
        pp3.setOwner(e3);
        pp3.setActualLifetime(15);
        pp3.setAgeFraction(0);
        pp3.setProfitability(0);
        pp3.setConstructionStartTime(0);
        pp3.setActualPermittime(0);
        pp3.setActualLeadtime(0);
        pp3.setDismantleTime(2);
        pp3.setLocation(pg2);

        PowerPlant pp4 = new PowerPlant();
        pp4.setTechnology(gas2);
        pp4.setOwner(e3);
        pp4.setActualLifetime(13);
        pp4.setAgeFraction(0);
        pp4.setProfitability(0);
        pp4.setConstructionStartTime(0);
        pp4.setActualPermittime(0);
        pp4.setActualLeadtime(0);
        pp4.setDismantleTime(2);
        pp4.setLocation(pg2);

        PowerPlant pp5 = new PowerPlant();
        pp5.setTechnology(gas1);
        pp5.setOwner(e2);
        pp5.setActualLifetime(3);
        pp5.setAgeFraction(0);
        pp5.setProfitability(0);
        pp5.setConstructionStartTime(0);
        pp5.setActualPermittime(0);
        pp5.setActualLeadtime(0);
        pp5.setDismantleTime(2);
        pp5.setLocation(pg1);

        PowerPlant pp6 = new PowerPlant();
        pp6.setTechnology(gas2);
        pp6.setOwner(e1);
        pp6.setActualLifetime(8);
        pp6.setAgeFraction(0);
        pp6.setProfitability(0);
        pp6.setConstructionStartTime(0);
        pp6.setActualPermittime(0);
        pp6.setActualLeadtime(0);
        pp6.setDismantleTime(2);
        pp6.setLocation(pg1);

        pp1.persist();
        pp2.persist();
        pp3.persist();
        pp4.persist();
        pp5.persist();
        pp6.persist();

        CashFlow cf1 = new CashFlow();
        cf1.setRegardingPowerPlant(pp1);
        cf1.setType(1);
        cf1.setMoney(60);
        cf1.setTime(0);

        CashFlow cf2 = new CashFlow();
        cf2.setRegardingPowerPlant(pp1);
        cf2.setType(3);
        cf2.setMoney(5);
        cf2.setTime(0);

        CashFlow cf3 = new CashFlow();
        cf3.setRegardingPowerPlant(pp1);
        cf3.setType(4);
        cf3.setMoney(5);
        cf3.setTime(0);

        CashFlow cf4 = new CashFlow();
        cf4.setRegardingPowerPlant(pp2);
        cf4.setType(1);
        cf4.setMoney(50);
        cf4.setTime(0);

        CashFlow cf5 = new CashFlow();
        cf5.setRegardingPowerPlant(pp2);
        cf5.setType(3);
        cf5.setMoney(10);
        cf5.setTime(0);

        CashFlow cf6 = new CashFlow();
        cf6.setRegardingPowerPlant(pp2);
        cf6.setType(4);
        cf6.setMoney(10);
        cf6.setTime(0);

        CashFlow cf7 = new CashFlow();
        cf7.setRegardingPowerPlant(pp3);
        cf7.setType(1);
        cf7.setMoney(40);
        cf7.setTime(0);

        CashFlow cf8 = new CashFlow();
        cf8.setRegardingPowerPlant(pp3);
        cf8.setType(3);
        cf8.setMoney(20);
        cf8.setTime(0);

        CashFlow cf9 = new CashFlow();
        cf9.setRegardingPowerPlant(pp3);
        cf9.setType(4);
        cf9.setMoney(15);
        cf9.setTime(0);

        CashFlow cf10 = new CashFlow();
        cf10.setRegardingPowerPlant(pp4);
        cf10.setType(1);
        cf10.setMoney(30);
        cf10.setTime(0);

        CashFlow cf11 = new CashFlow();
        cf11.setRegardingPowerPlant(pp4);
        cf11.setType(3);
        cf11.setMoney(30);
        cf11.setTime(0);

        CashFlow cf12 = new CashFlow();
        cf12.setRegardingPowerPlant(pp4);
        cf12.setType(4);
        cf12.setMoney(20);
        cf12.setTime(0);

        CashFlow cf13 = new CashFlow();
        cf13.setRegardingPowerPlant(pp5);
        cf13.setType(1);
        cf13.setMoney(20);
        cf13.setTime(0);

        CashFlow cf14 = new CashFlow();
        cf14.setRegardingPowerPlant(pp5);
        cf14.setType(3);
        cf14.setMoney(35);
        cf14.setTime(0);

        CashFlow cf15 = new CashFlow();
        cf15.setRegardingPowerPlant(pp5);
        cf15.setType(4);
        cf15.setMoney(25);
        cf15.setTime(0);

        CashFlow cf16 = new CashFlow();
        cf16.setRegardingPowerPlant(pp6);
        cf16.setType(1);
        cf16.setMoney(10);
        cf16.setTime(0);

        CashFlow cf17 = new CashFlow();
        cf17.setRegardingPowerPlant(pp6);
        cf17.setType(3);
        cf17.setMoney(40);
        cf17.setTime(0);

        CashFlow cf18 = new CashFlow();
        cf18.setRegardingPowerPlant(pp6);
        cf18.setType(4);
        cf18.setMoney(30);
        cf18.setTime(0);

        cf1.persist();
        cf2.persist();
        cf3.persist();
        cf4.persist();
        cf5.persist();
        cf6.persist();
        cf7.persist();
        cf8.persist();
        cf9.persist();
        cf10.persist();
        cf11.persist();
        cf12.persist();
        cf13.persist();
        cf14.persist();
        cf15.persist();
        cf16.persist();
        cf17.persist();
        cf18.persist();

        dismantlePowerPlantOperationalLossRole.act(market1);
        dismantlePowerPlantOperationalLossRole.act(market2);

        System.out.print(" " + pp1.getProfitability());
        // System.out.print(" " + pp2.getProfitability());
        // System.out.print(" " + pp3.getProfitability());
        // System.out.print(" " + pp4.getProfitability());
        // System.out.print(" " + pp5.getProfitability());
        // System.out.print(" " + pp6.getProfitability());

        // assertTrue(pp1.getProfitability() == 50);

    }
}
