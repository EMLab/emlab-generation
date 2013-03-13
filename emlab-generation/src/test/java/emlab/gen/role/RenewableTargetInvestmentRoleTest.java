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
package emlab.gen.role;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import emlab.gen.domain.agent.EnergyProducer;
import emlab.gen.domain.agent.TargetInvestor;
import emlab.gen.domain.gis.Zone;
import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.domain.policy.PowerGeneratingTechnologyTarget;
import emlab.gen.domain.technology.PowerGeneratingTechnology;
import emlab.gen.domain.technology.PowerGridNode;
import emlab.gen.domain.technology.PowerPlant;
import emlab.gen.repository.MarketRepository;
import emlab.gen.repository.PowerGenerationTechnologyTargetRepository;
import emlab.gen.repository.PowerPlantRepository;
import emlab.gen.role.investment.TargetInvestmentRole;
import emlab.gen.trend.GeometricTrend;
import emlab.gen.trend.StepTrend;

/**
 * @author JCRichstein
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/emlab-gen-test-context.xml" })
@Transactional
public class RenewableTargetInvestmentRoleTest {

	Logger logger = Logger.getLogger(RenewableTargetInvestmentRoleTest.class);

	@Autowired
	PowerPlantRepository powerPlantRepository;
	@Autowired
	MarketRepository marketRepository;

	@Autowired
	Neo4jTemplate template;

	@Autowired
	TargetInvestmentRole targetInvestmentRole;

	@Autowired
	PowerGenerationTechnologyTargetRepository powerGenerationTechnologyTargetRepository;

	@Before
	@Transactional
	public void setUp() throws Exception {
		PowerGeneratingTechnology wind = new PowerGeneratingTechnology();
		GeometricTrend windInvestmentTrend = new GeometricTrend();
		GeometricTrend windEfficiencyTrend = new GeometricTrend();
		windEfficiencyTrend.setStart(1);
		windEfficiencyTrend.setGrowthRate(0);
		windEfficiencyTrend.persist();
		windInvestmentTrend.setStart(10000);
		windInvestmentTrend.setGrowthRate(0.97);
		windInvestmentTrend.persist();
		GeometricTrend windFixedOperatingTrend = new GeometricTrend();
		windFixedOperatingTrend.setGrowthRate(0);
		windFixedOperatingTrend.setStart(0);
		windFixedOperatingTrend.persist();
		wind.setInvestmentCostTimeSeries(windInvestmentTrend);
		wind.setEfficiencyTimeSeries(windEfficiencyTrend);
		wind.setFixedOperatingCostTimeSeries(windFixedOperatingTrend);
		wind.setCapacity(200);
		wind.setExpectedLeadtime(2);
		wind.setExpectedPermittime(1);
		wind.setIntermittent(true);
		wind.setExpectedLifetime(20);
		wind.setPeakSegmentDependentAvailability(0.4);
		wind.setBaseSegmentDependentAvailability(0.1);
		wind.setName("Wind");
		wind.persist();
		PowerGeneratingTechnology pv = new PowerGeneratingTechnology();
		pv.setName("PV");
		GeometricTrend pvInvestmentTrend = new GeometricTrend();
		pvInvestmentTrend.setStart(12000);
		pvInvestmentTrend.setGrowthRate(1);
		pvInvestmentTrend.persist();
		GeometricTrend pvEfficiencyTrend = new GeometricTrend();
		pvEfficiencyTrend.setStart(1);
		pvEfficiencyTrend.setGrowthRate(0);
		pvEfficiencyTrend.persist();
		GeometricTrend pvFixedOperatingTrend = new GeometricTrend();
		pvFixedOperatingTrend.setStart(0);
		pvFixedOperatingTrend.setGrowthRate(0);
		pvFixedOperatingTrend.persist();
		pv.setCapacity(150);
		pv.setEfficiencyTimeSeries(pvEfficiencyTrend);
		pv.setInvestmentCostTimeSeries(pvInvestmentTrend);
		pv.setFixedOperatingCostTimeSeries(pvFixedOperatingTrend);
		pv.setFixedOperatingCostModifierAfterLifetime(0);
		pv.setExpectedLeadtime(1);
		pv.setExpectedPermittime(0);
		pv.setIntermittent(true);
		pv.setExpectedLifetime(15);
		pv.setPeakSegmentDependentAvailability(0.5);
		pv.setBaseSegmentDependentAvailability(0.1);
		pv.persist();
		StepTrend windTrend = new StepTrend();
		windTrend.setStart(400);
		windTrend.setDuration(1);
		windTrend.setIncrement(100);
		windTrend.setMinValue(300);
		windTrend.persist();
		StepTrend pvTrend = new StepTrend();
		pvTrend.setStart(400);
		pvTrend.setDuration(1);
		pvTrend.setIncrement(100);
		pvTrend.setMinValue(300);
		pvTrend.persist();
		PowerGeneratingTechnologyTarget windTarget = new PowerGeneratingTechnologyTarget();
		windTarget.setPowerGeneratingTechnology(wind);
		windTarget.setTrend(windTrend);
		windTarget.persist();
		PowerGeneratingTechnologyTarget pvTarget = new PowerGeneratingTechnologyTarget();
		pvTarget.setPowerGeneratingTechnology(pv);
		pvTarget.setTrend(pvTrend);
		pvTarget.persist();

		Zone zoneA = new Zone();
		zoneA.setName("ZoneA");
		zoneA.persist();

		Zone zoneB = new Zone();
		zoneB.setName("ZoneB");
		zoneB.persist();

		PowerGridNode powerGridNodeA = new PowerGridNode();
		powerGridNodeA.setZone(zoneA);
		powerGridNodeA.setCapacityMultiplicationFactor(1.0);
		powerGridNodeA.persist();
		PowerGridNode powerGridNodeB = new PowerGridNode();
		powerGridNodeB.setZone(zoneB);
		powerGridNodeB.setCapacityMultiplicationFactor(1.0);
		powerGridNodeB.persist();

		ElectricitySpotMarket marketA = new ElectricitySpotMarket();
		marketA.setName("marketA");
		marketA.setZone(zoneA);
		marketA.persist();

		ElectricitySpotMarket marketB = new ElectricitySpotMarket();
		marketB.setName("marketB");
		marketB.setZone(zoneB);
		marketB.persist();

		EnergyProducer energyProducer1 = new EnergyProducer();
		EnergyProducer energyProducer2 = new EnergyProducer();

		PowerPlant windTurbineA1 = new PowerPlant();
		windTurbineA1.specifyAndPersist(-3, energyProducer1, powerGridNodeA,
				wind);

		PowerPlant windTurbineA2 = new PowerPlant();
		windTurbineA2.specifyAndPersist(-4, energyProducer2, powerGridNodeA,
				wind);

		PowerPlant windTurbineB1 = new PowerPlant();
		windTurbineB1.specifyAndPersist(-5, energyProducer1, powerGridNodeB,
				wind);

		PowerPlant windTurbineB2 = new PowerPlant();
		windTurbineB2.specifyAndPersist(-10, energyProducer2, powerGridNodeB,
				wind);

		PowerPlant pvA1 = new PowerPlant();
		pvA1.specifyAndPersist(-2, energyProducer1, powerGridNodeA, pv);

		PowerPlant pvA2 = new PowerPlant();
		pvA2.specifyAndPersist(-3, energyProducer2, powerGridNodeA, pv);

		PowerPlant pvB1 = new PowerPlant();
		pvB1.specifyAndPersist(-5, energyProducer1, powerGridNodeB, pv);

		PowerPlant pvB2 = new PowerPlant();
		pvB2.specifyAndPersist(-10, energyProducer2, powerGridNodeB, pv);

		TargetInvestor rti = new TargetInvestor();
		rti.getPowerGenerationTechnologyTargets().add(windTarget);
		rti.getPowerGenerationTechnologyTargets().add(pvTarget);
		rti.setInvestorMarket(marketA);
		rti.setName("RenTarInv");
		template.save(rti);
	}

	@Test
	public void testActMethod() {

		TargetInvestor rti = template.findAll(TargetInvestor.class).iterator()
				.next();
		ElectricitySpotMarket marketA = null;
		for (ElectricitySpotMarket market : marketRepository
				.findAllElectricitySpotMarkets()) {
			if (market.getName().equals("marketA"))
				marketA = market;
		}

		PowerGeneratingTechnology wind = null;
		PowerGeneratingTechnology pv = null;
		for (PowerGeneratingTechnology tech : template
				.findAll(PowerGeneratingTechnology.class)) {
			if (tech.getName().equals("Wind"))
				wind = tech;
			if (tech.getName().equals("PV"))
				pv = tech;
		}

		double windCapacity = powerPlantRepository
				.calculateCapacityOfExpectedOperationalPowerPlantsInMarketAndTechnology(
						marketA, wind, 0);
		assertEquals("Test original wind capacity in market A: ", 400,
				windCapacity, 0.01);
		assertEquals(
				"Test wind capacity after investment in year 0",
				300,
				powerPlantRepository
						.calculateCapacityOfExpectedOperationalPowerPlantsInMarketAndTechnology(
								marketA, pv, 0), 0.1);

		rti.act(targetInvestmentRole);

		assertEquals(
				"Test wind capacity after investment in year 3",
				400,
				powerPlantRepository
						.calculateCapacityOfExpectedOperationalPowerPlantsInMarketAndTechnology(
								marketA, wind, 2), 0.1);
		// Expected value: (Start + ExpectedTimeForWind*Increment: 400 + 3 * 100
		// = 700, but 800 because of plant size.
		assertEquals(
				"Test wind capacity after investment in year 3",
				700,
				powerPlantRepository
						.calculateCapacityOfExpectedOperationalPowerPlantsInMarketAndTechnology(
								marketA, wind, 3), 0.1);

		assertEquals(
				"Test wind capacity after investment in year 0",
				300,
				powerPlantRepository
						.calculateCapacityOfExpectedOperationalPowerPlantsInMarketAndTechnology(
								marketA, pv, 0), 0.1);
		// 450 is closer to the target of 500 than 600.
		assertEquals(
				"Test wind capacity after investment in year 0",
				500,
				powerPlantRepository
						.calculateCapacityOfExpectedOperationalPowerPlantsInMarketAndTechnology(
								marketA, pv, 1), 0.1);
	}

	@Test
	public void testPowerGenerationTechnologyTargetRepository() {
		PowerGeneratingTechnology wind = null;
		PowerGeneratingTechnology pv = null;
		for (PowerGeneratingTechnology tech : template
				.findAll(PowerGeneratingTechnology.class)) {
			if (tech.getName().equals("Wind"))
				wind = tech;
			if (tech.getName().equals("PV"))
				pv = tech;
		}

		ElectricitySpotMarket marketA = null;
		for (ElectricitySpotMarket market : marketRepository
				.findAllElectricitySpotMarkets()) {
			if (market.getName().equals("marketA"))
				marketA = market;
		}

		PowerGeneratingTechnologyTarget pgttWindFromDB = powerGenerationTechnologyTargetRepository
				.findOneByTechnologyAndMarket(wind, marketA);
		assertEquals("Testing targetRepository", 400, ((StepTrend) pgttWindFromDB.getTrend())
				.getStart(), 0.01);

		// Testing if new construct in for private investor works:
		double expectedInstalledCapacityOfTechnology = powerPlantRepository
				.calculateCapacityOfExpectedOperationalPowerPlantsInMarketAndTechnology(
						marketA, pv, 1);
		PowerGeneratingTechnologyTarget technologyTarget = powerGenerationTechnologyTargetRepository
				.findOneByTechnologyAndMarket(pv, marketA);
		if (technologyTarget != null) {
			double technologyTargetCapacity = technologyTarget.getTrend()
					.getValue(1);
			expectedInstalledCapacityOfTechnology = (technologyTargetCapacity > expectedInstalledCapacityOfTechnology) ? technologyTargetCapacity
					: expectedInstalledCapacityOfTechnology;
		}
		assertEquals(
				"Testing if expectedInstalledCapacityOfTechnology is adjusted",
				500, expectedInstalledCapacityOfTechnology, 0.01);

		// Testing if technologies which don't have a target, are handled
		// correctly as well.
		template.delete(technologyTarget);
		technologyTarget = powerGenerationTechnologyTargetRepository
				.findOneByTechnologyAndMarket(pv, marketA);
		expectedInstalledCapacityOfTechnology = powerPlantRepository
				.calculateCapacityOfExpectedOperationalPowerPlantsInMarketAndTechnology(
						marketA, pv, 1);
		assertTrue(technologyTarget == null);
		if (technologyTarget != null) {
			double technologyTargetCapacity = technologyTarget.getTrend()
					.getValue(1);
			expectedInstalledCapacityOfTechnology = (technologyTargetCapacity > expectedInstalledCapacityOfTechnology) ? technologyTargetCapacity
					: expectedInstalledCapacityOfTechnology;
		}
		assertEquals(
				"Testing if expectedInstalledCapacityOfTechnology is adjusted",
				300, expectedInstalledCapacityOfTechnology, 0.01);

		double peakPowerPlantCapacityinMarketA = powerPlantRepository
				.calculatePeakCapacityOfOperationalPowerPlantsInMarket(marketA,
						0);
		assertEquals("Test if peakCapacity queries work: ", 310,
				peakPowerPlantCapacityinMarketA, 0.1);
		double peakPowerPlantCapacity = powerPlantRepository
				.calculatePeakCapacityOfOperationalPowerPlants(0);
		assertEquals("Test if peakCapacity queries work: ", 620,
				peakPowerPlantCapacity, 0.1);
	}

}
