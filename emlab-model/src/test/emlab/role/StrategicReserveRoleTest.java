package emlab.role;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import emlab.domain.gis.Zone;
import emlab.domain.market.electricity.ElectricitySpotMarket;
import emlab.domain.market.electricity.PowerPlantDispatchPlan;
import emlab.domain.market.electricity.Segment;
import emlab.repository.BidRepository;
import emlab.repository.MarketRepository;
import emlab.repository.PowerPlantDispatchPlanRepository;
import emlab.repository.Reps;
import emlab.repository.SegmentLoadRepository;
import emlab.repository.ZoneRepository;

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

	@Test
	public void ZonalTest(){

		Zone zone1 = new Zone();
		Zone zone2 = new Zone();
		zone1.persist();
		zone2.persist();

		ElectricitySpotMarket market1 = new ElectricitySpotMarket();
		market1.setZone(zone1);
		//market1.getDemandGrowthTrend().getValue(0);
		//market1.getDemandGrowthTrend().setStart(1);
		market1.persist();

		ElectricitySpotMarket market2 = new ElectricitySpotMarket();
		market2.setZone(zone2);
		//market2.getDemandGrowthTrend().setMin(0);
		//market2.getDemandGrowthTrend().setMax(1);
		//market2.getDemandGrowthTrend().setStart(1);
		market2.persist();

		Segment S1 = new Segment();
		S1.persist();

		PowerPlantDispatchPlan p1 = new PowerPlantDispatchPlan();
		p1.setAmount(1500);
		p1.setSegment(S1);
		p1.setPrice(5);
		p1.setTime(0);
		p1.setBiddingMarket(market1);
		p1.persist();

		PowerPlantDispatchPlan p11 = new PowerPlantDispatchPlan();
		p11.setAmount(1000);
		p11.setSegment(S1);
		p11.setPrice(15);
		p11.setTime(0);
		p11.setBiddingMarket(market1);
		p11.persist();

		PowerPlantDispatchPlan p111 = new PowerPlantDispatchPlan();
		p111.setAmount(1200);
		p111.setSegment(S1);
		p111.setPrice(7);
		p111.setTime(0);
		p111.setBiddingMarket(market1);
		p111.persist();

		// for Zone 2

		PowerPlantDispatchPlan p2 = new PowerPlantDispatchPlan();
		p2.setAmount(1500);
		p2.setSegment(S1);
		p2.setPrice(5);
		p2.setTime(0);
		p2.setBiddingMarket(market2);
		p2.persist();

		PowerPlantDispatchPlan p22 = new PowerPlantDispatchPlan();
		p22.setAmount(1000);
		p22.setSegment(S1);
		p22.setPrice(15);
		p22.setTime(0);
		p22.setBiddingMarket(market2);
		p22.persist();

		PowerPlantDispatchPlan p222 = new PowerPlantDispatchPlan();
		p222.setAmount(1200);
		p222.setSegment(S1);
		p222.setPrice(7);
		p222.setTime(0);
		p222.setBiddingMarket(market2);
		p222.persist();

		int count = 0;
		for (Zone curZone : zoneRepository.findAll()){
			ElectricitySpotMarket market = marketRepository.findElectricitySpotMarketForZone(curZone);
			//plantDispatchPlanRepository.DescendingListAllPowerPlantDispatchPlansbyMarketTimeSegment(market, S1, 0);
			segmentLoadRepository.peakLoadbyZoneMarketandTime(curZone, market);
			//Double PeakLoad =segmentLoadRepository.peakLoadbyZoneMarketandTime(curZone, market, 0);
			count++;
			System.out.println(count);
			//System.out.println(PeakLoad);
		}
		
	}

}
