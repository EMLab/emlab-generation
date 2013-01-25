package emlab.role;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.kernel.impl.storemigration.CurrentDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import emlab.domain.agent.EnergyProducer;
import emlab.domain.agent.StrategicReserveOperator;
import emlab.domain.gis.Zone;
import emlab.domain.market.electricity.ElectricitySpotMarket;
import emlab.domain.market.electricity.PowerPlantDispatchPlan;
import emlab.domain.market.electricity.Segment;
import emlab.domain.market.electricity.SegmentLoad;
import emlab.domain.technology.PowerGeneratingTechnology;
import emlab.domain.technology.PowerPlant;
import emlab.repository.BidRepository;
import emlab.repository.MarketRepository;
import emlab.repository.PowerPlantDispatchPlanRepository;
import emlab.repository.RepositoryTesting;
import emlab.repository.Reps;
import emlab.repository.SegmentLoadRepository;
import emlab.repository.ZoneRepository;
import emlab.role.capacitymechanisms.StrategicReserveOperatorRole;
import emlab.trend.TriangularTrend;
import groovy.mock.interceptor.Demand;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/emlab-test-context.xml"})
@Transactional
public class StrategicReserveRoleTest {

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
	StrategicReserveOperatorRole strategicReserveOperatorRole;
	
	Logger logger = Logger.getLogger(StrategicReserveRoleTest.class);

	@Test
	public void ZonalTest(){

		Zone zone1 = new Zone();
		Zone zone2 = new Zone();
		zone1.setName("Zone 1");
		zone2.setName("Zone2");
		zone1.persist();
		zone2.persist();

		
		Segment S1 = new Segment();
		S1.persist();
		
		SegmentLoad SG1 = new SegmentLoad();
		SG1.setSegment(S1);
		SG1.setBaseLoad(1500);
				
		SegmentLoad SG2 = new SegmentLoad();
		SG2.setSegment(S1);
		SG2.setBaseLoad(1000);
				
		SegmentLoad SG3 = new SegmentLoad();
		SG3.setSegment(S1);
		SG3.setBaseLoad(2000);
		
		SegmentLoad SG4 = new SegmentLoad();
		SG4.setSegment(S1);
		SG4.setBaseLoad(500);
		
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
		market1.persist();
		
		
		ElectricitySpotMarket market2 = new ElectricitySpotMarket();
		market2.setZone(zone2);
		market2.setName("Market2");
		market2.setLoadDurationCurve(segmentLoads2);
		market2.setDemandGrowthTrend(demandGrowthTrend);
		market2.persist();
		
		PowerGeneratingTechnology coal1 = new PowerGeneratingTechnology();
		coal1.setFixedOperatingCost(990);
		coal1.setFixedOperatingCostModifierAfterLifetime(1);
		
		
		PowerGeneratingTechnology coal2 = new PowerGeneratingTechnology();
		coal2.setFixedOperatingCost(1110);
		
		PowerGeneratingTechnology gas1 = new PowerGeneratingTechnology();
		gas1.setFixedOperatingCost(560);
		
		PowerGeneratingTechnology gas2 = new PowerGeneratingTechnology();
		gas2.setFixedOperatingCost(650);
		
		coal1.persist();
		coal2.persist();
		gas1.persist();
		gas2.persist();
		
		EnergyProducer e1 = new EnergyProducer();
		e1.setCash(0);
		
		EnergyProducer e2 = new EnergyProducer();
		e2.setCash(0);
		
		EnergyProducer e3 = new EnergyProducer();
		e3.setCash(0);
		
		e1.persist();
		e2.persist();
		e3.persist();
		
		
		PowerPlant  pp1 = new PowerPlant();
		pp1.setTechnology(coal1);
		pp1.setOwner(e1);
		
		PowerPlant  pp2 = new PowerPlant();
		pp2.setTechnology(coal2);
		pp2.setOwner(e2);
		
		PowerPlant  pp3 = new PowerPlant();
		pp3.setTechnology(gas1);
		pp3.setOwner(e3);
		
		PowerPlant  pp4 = new PowerPlant();
		pp4.setTechnology(gas2);
		pp4.setOwner(e3);
		
		PowerPlant  pp5 = new PowerPlant();
		pp5.setTechnology(gas1);
		pp5.setOwner(e2);
		
		PowerPlant  pp6 = new PowerPlant();
		pp6.setTechnology(gas2);
		pp6.setOwner(e1);
		
		pp1.persist();
		pp2.persist();
		pp3.persist();
		pp4.persist();
		pp5.persist();
		pp6.persist();
		

		PowerPlantDispatchPlan p1 = new PowerPlantDispatchPlan();
		p1.setAmount(1500.0d);
		p1.setSegment(S1);
		p1.setPrice(5.0d);
		p1.setTime(0l);
		p1.setBiddingMarket(market1);
		p1.setPowerPlant(pp1);
		p1.persist();
		

		PowerPlantDispatchPlan p11 = new PowerPlantDispatchPlan();
		p11.setAmount(1000.0d);
		p11.setSegment(S1);
		p11.setPrice(15.0d);
		p11.setTime(0l);
		p11.setBiddingMarket(market1);
		p11.setPowerPlant(pp2);
		p11.persist();

		PowerPlantDispatchPlan p111 = new PowerPlantDispatchPlan();
		p111.setAmount(1200.0d);
		p111.setSegment(S1);
		p111.setPrice(7.0d);
		p111.setTime(0l);
		p111.setBiddingMarket(market1);
		p111.setPowerPlant(pp3);
		p111.persist();

		// for Zone 2

		PowerPlantDispatchPlan p2 = new PowerPlantDispatchPlan();
		p2.setAmount(1700.0d);
		p2.setSegment(S1);
		p2.setPrice(5.0d);
		p2.setTime(0l);
		p2.setBiddingMarket(market2);
		p2.setPowerPlant(pp4);
		p2.persist();

		PowerPlantDispatchPlan p22 = new PowerPlantDispatchPlan();
		p22.setAmount(2000.0d);
		p22.setSegment(S1);
		p22.setPrice(15.0d);
		p22.setTime(0l);
		p22.setBiddingMarket(market2);
		p22.setPowerPlant(pp5);
		p22.persist();

		PowerPlantDispatchPlan p222 = new PowerPlantDispatchPlan();
		p222.setAmount(300.0d);
		p222.setSegment(S1);
		p222.setPrice(7.0d);
		p222.setTime(0l);
		p222.setBiddingMarket(market2);
		p222.setPowerPlant(pp6);
		p222.persist();
		
		StrategicReserveOperator strategicReserveOperator = new StrategicReserveOperator();
		strategicReserveOperator.setReservePrice(25);
		strategicReserveOperator.setReserveVolumePercent(0.6);
		strategicReserveOperator.setCash(0);
		strategicReserveOperator.setName("SRO");
		strategicReserveOperator.persist();

		/*for (PowerPlantDispatchPlan currDispatchPlan : plantDispatchPlanRepository.findAll()){
			logger.warn("Test1 Volume: " + currDispatchPlan.getAmount());
			logger.warn("Test1 Price: " + currDispatchPlan.getPrice());
		}*/
		strategicReserveOperatorRole.act(strategicReserveOperator);
		
			//logger.warn(strategicReserveOperator.getReserveVolumePercent());
		
		
		/*for (PowerPlantDispatchPlan currDispatchPlan : plantDispatchPlanRepository.findAll()){
			logger.warn("Test2 Volume: " + currDispatchPlan.getAmount());
			logger.warn("Test2 Price: " + currDispatchPlan.getPrice());
			logger.warn("Test 2 Status: "+ currDispatchPlan.getORstatus());
		}*/
		//logger.warn(strategicReserveOperator.getReserveVolume());
		
		/*for (Zone curZone : zoneRepository.findAll()){
			//logger.warn(curZone);
			ElectricitySpotMarket market = marketRepository.findElectricitySpotMarketForZone(curZone);
			//logger.warn(market.getName());
			//plantDispatchPlanRepository.DescendingListAllPowerPlantDispatchPlansbyMarketTimeSegment(market, S1, 0);
			double peaky =segmentLoadRepository.peakLoadbyZoneMarketandTime(curZone, market);
			//logger.warn(peaky);
			Double PeakLoad =peaky*market.getDemandGrowthTrend().getValue(0);
			//logger.warn(market.getDemandGrowthTrend().getValue(0));
			//logger.warn(PeakLoad);
			
			double segmentCounter = reps.segmentRepository.count();
			//logger.warn(segmentCounter);
			*/
		/*	for(Segment currentSegment: reps.segmentRepository.findAll()){
			
			Iterable<PowerPlantDispatchPlan> sortedListofPPDP = plantDispatchPlanRepository.findDescendingSortedPowerPlantDispatchPlansForSegmentForTime(currentSegment, 0);
			for (PowerPlantDispatchPlan curDispatchPlan:sortedListofPPDP){
				logger.warn(curDispatchPlan.getAmount());
			}
			}
			//System.out.println(PeakLoad);*/
		}
		
	}

//}
