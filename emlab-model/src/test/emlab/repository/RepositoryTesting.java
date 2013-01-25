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
package emlab.repository;

import static org.junit.Assert.*;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hamcrest.core.IsEqual;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import scala.annotation.target.setter;

import emlab.domain.gis.Zone;
import emlab.domain.market.ClearingPoint;
import emlab.domain.market.CommodityMarket;
import emlab.domain.market.electricity.ElectricitySpotMarket;
import emlab.domain.market.electricity.PowerPlantDispatchPlan;
import emlab.domain.market.electricity.Segment;
import emlab.domain.market.electricity.SegmentLoad;
import emlab.domain.technology.Substance;
import emlab.util.TrendEstimator;
import groovy.mock.interceptor.Demand;

/**
 * @author JCRichstein
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/emlab-test-context.xml"})
@Transactional
public class RepositoryTesting {


	Logger logger = Logger.getLogger(RepositoryTesting.class);


	//------- clearingPointRepository ---------
	@Autowired ClearingPointRepository clearingPointRepository;

	@Test
	public void testfindAllClearingPointsForSubstanceTradedOnCommodityMarkesAndTimeRange(){
		double[][] input = {{0,1},{1,1.1},{2,1.21},{3,1.331},{4,1.4641}};
		Substance substance = new Substance();
		substance.persist();
		CommodityMarket market = new CommodityMarket();
		market.setSubstance(substance);
		market.persist();
		Map<Integer, Double> inputMap = new HashMap();
		for (double[] d : input) {
			ClearingPoint cp = new ClearingPoint();
			cp.setTime((long) d[0]);
			cp.setPrice(d[1]);
			cp.setAbstractMarket(market);
			cp.persist();
			inputMap.put(new Integer((int) d[0]), d[1]);
		}

		//Testing selection of only first one, starting with negative value
		Iterable<ClearingPoint> cps = clearingPointRepository.findAllClearingPointsForSubstanceTradedOnCommodityMarkesAndTimeRange(substance, -2l, 0l);
		assertTrue(cps.iterator().next().getPrice() == 1);

		cps = clearingPointRepository.findAllClearingPointsForSubstanceTradedOnCommodityMarkesAndTimeRange(substance, -2l, 4l);
		for(ClearingPoint cp : cps){
			assertTrue(cp.getPrice() == inputMap.get(new Integer((int) cp.getTime())));
			//logger.warn(new Double(cp.getPrice()).toString() + "==" + inputMap.get(new Integer((int) cp.getTime())).toString());
		}
	}

	//SubstanceRepository
	@Autowired SubstanceRepository substanceRepository;

	@Test
	public void testfindAllSubstancesTradedOnCommodityMarkets(){
		Substance coal = new Substance();
		coal.setName("Coal");
		coal.persist();
		Substance co2 = new Substance();
		co2.persist();
		CommodityMarket market = new CommodityMarket();
		market.setSubstance(coal);
		market.persist();

		Iterable<Substance> substancesInDB = substanceRepository.findAllSubstancesTradedOnCommodityMarkets();
		int count = 0;
		for(Substance substance : substancesInDB){
			count++;
			assertTrue(substance.getName().equals("Coal"));
		}
		assertTrue(count == 1);
	}

	@Autowired
	SegmentLoadRepository segmentLoadRepository;

	@Autowired
	ZoneRepository zoneRepository;

	@Autowired 
	Reps reps;

	@Autowired
	PowerPlantDispatchPlanRepository planRepository;

	@Autowired
	MarketRepository marketRepository;

	@Test
	public void findDescendingSortedPowerPlantDispatchPlansForSegmentForTime(){
		ElectricitySpotMarket market = new ElectricitySpotMarket();
		market.persist();
		Zone Z1 =new Zone();
		Zone Z2 = new Zone();
		Z1.persist();
		Z2.persist();
		Segment S11 = new Segment();

		PowerPlantDispatchPlan p1 = new PowerPlantDispatchPlan();
		p1.setAmount(1500);
		p1.setSegment(S11);
		p1.setPrice(5);
		p1.setTime(0);
		p1.persist();

		PowerPlantDispatchPlan p11 = new PowerPlantDispatchPlan();
		p11.setAmount(1000);
		p11.setSegment(S11);
		p11.setPrice(15);
		p11.setTime(0);
		p11.persist();

		PowerPlantDispatchPlan p111 = new PowerPlantDispatchPlan();
		p111.setAmount(1200);
		p111.setSegment(S11);
		p111.setPrice(7);
		p111.setTime(0);
		p111.persist();


		/*ElectricitySpotMarket market = new ElectricitySpotMarket();
		market.persist();
		logger.warn(marketRepository.findAllElectricitySpotMarkets());*/

		S11.persist();
		double sum = 0;
		for (Zone currentZone:zoneRepository.findAll()){
			logger.warn(currentZone.getName());
			//logger.warn(reps.segmentRepository.findAll());
		}
		//Iterable<PowerPlantDispatchPlan> ppdp = planRepository.findSortedPowerPlantDispatchPlansForSegmentForTime(S11, 0);

		for (PowerPlantDispatchPlan currDispatchPlan: planRepository.DescendingListAllPowerPlantDispatchPlansbyMarketTimeSegment(market, S11, 0)){
			logger.warn(currDispatchPlan.getPrice());
			//sum += currDispatchPlan.getPrice();
		}
		//logger.warn(sum);
		//assertTrue(sum == 27);
	}



	@Test
	public void calculatePeakLoadbyMarketandTime(){

		ElectricitySpotMarket market = new ElectricitySpotMarket();
		market.persist();
		Zone Z1 = new Zone();

		Segment S11 = new Segment();
		S11.setSegmentID(1);
		S11.persist();

		SegmentLoad S1 = new  SegmentLoad();
		S1.setBaseLoad(20);
		S1.setSegment(S11);
		S1.persist();


		SegmentLoad S2 = new  SegmentLoad();
		S2.setBaseLoad(30);
		S2.setSegment(S11);
		S2.persist();


		SegmentLoad S3 = new  SegmentLoad();
		S3.setBaseLoad(40);
		S3.setSegment(S11);
		S3.persist();

		SegmentLoad S4 = new SegmentLoad();
		S4.setBaseLoad(50);
		S4.setSegment(S11);
		S4.persist();

		Set<SegmentLoad> segmentLoad = new HashSet<SegmentLoad>();
		market.setLoadDurationCurve(segmentLoad);
		market.persist();

		double Test = segmentLoadRepository.peakLoadbyZoneMarketandTime(Z1, market);

	}


}

