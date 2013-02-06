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
package emlab.domain;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.template.Neo4jOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import emlab.domain.agent.EnergyProducer;
import emlab.domain.agent.TargetInvestor;
import emlab.domain.policy.PowerGeneratingTechnologyTarget;
import emlab.domain.technology.PowerGeneratingTechnology;
import emlab.repository.EnergyProducerRepository;
import emlab.trend.StepTrend;

/**
 * @author JCRichstein
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/emlab-test-context.xml"})
@Transactional
public class RenewableTargetInvestorTest {
	
	Logger logger = Logger.getLogger(RenewableTargetInvestorTest.class);
	
	@Autowired Neo4jOperations template;
	@Autowired EnergyProducerRepository energyProducerRepository;
	
    @Before
    @Transactional
    public void setUp() throws Exception {
    	
    	

    }

	@Test
	public void testBasicFunctions() {
		PowerGeneratingTechnology wind = new PowerGeneratingTechnology();
		wind.setCapacity(200);
		wind.setEfficiency(100);
		wind.setBaseInvestmentCost(10000);
		wind.setExpectedLeadtime(2);
		wind.setExpectedPermittime(1);
		wind.setIntermittent(true);
		wind.setInvestmentCostModifierExogenous(0.97);
		wind.setName("Wind");
		wind.persist();
		PowerGeneratingTechnology pv = new PowerGeneratingTechnology();
		pv.setName("PV");
		pv.persist();
		StepTrend st1 = new StepTrend();
		st1.setStart(1000);
		st1.setDuration(1);
		st1.setIncrement(200);
		st1.setMinValue(500);
		st1.persist();
		StepTrend st2 = new StepTrend();
		st2.setStart(400);
		st2.setDuration(1);
		st2.setIncrement(100);
		st2.setMinValue(300);
		st2.persist();
		PowerGeneratingTechnologyTarget windTarget = new PowerGeneratingTechnologyTarget();
		windTarget.setPowerGeneratingTechnology(wind);
		windTarget.setTrend(st1);
		windTarget.persist();
		PowerGeneratingTechnologyTarget pvTarget = new PowerGeneratingTechnologyTarget();
		pvTarget.setPowerGeneratingTechnology(pv);
		pvTarget.setTrend(st2);
		pvTarget.persist();
		
		HashSet<PowerGeneratingTechnologyTarget> testSet = new HashSet<PowerGeneratingTechnologyTarget>();
		testSet.add(windTarget);
		
		TargetInvestor rti = new TargetInvestor();
		rti.getPowerGenerationTechnologyTargets().add(windTarget);
		rti.setName("RenTarInv");
		template.save(rti);
		
		Set<PowerGeneratingTechnologyTarget> targetsFromDb= template.findAll(TargetInvestor.class).iterator().next().getPowerGenerationTechnologyTargets();
		assertTrue(targetsFromDb.equals(testSet));
		
		rti.getPowerGenerationTechnologyTargets().add(pvTarget);
		assertTrue(targetsFromDb.equals(testSet));
		
		targetsFromDb= template.findAll(TargetInvestor.class).iterator().next().getPowerGenerationTechnologyTargets();
		
		assertTrue(!targetsFromDb.equals(testSet));
		testSet.add(pvTarget);
		assertTrue(targetsFromDb.equals(testSet));
		
	}
	
	@Test
	public void testEnergyProducerAndRenewableTargetInvestorQueries(){
		
		TargetInvestor rti = new TargetInvestor();
		rti.setName("R");
		rti.persist();
		
		EnergyProducer energyProducerA = new EnergyProducer();
		energyProducerA.setName("A");
		energyProducerA.persist();
		
		EnergyProducer energyProducerB = new EnergyProducer();
		energyProducerB.setName("B");
		energyProducerB.persist();
		

		int numberProducers = 0;
		List<EnergyProducer> energyProducers = energyProducerRepository.findAllEnergyProducersExceptForRenewableTargetInvestorsAtRandom();
		for(EnergyProducer energyProducer : energyProducers){
			assertTrue(!(energyProducer instanceof TargetInvestor));
			numberProducers++;
		}
		for(EnergyProducer energyProducer : energyProducers){
			assertTrue(!(energyProducer instanceof TargetInvestor));
			numberProducers++;
		}
		assertEquals("Check number of producers", 4, numberProducers);
		
	}

}

