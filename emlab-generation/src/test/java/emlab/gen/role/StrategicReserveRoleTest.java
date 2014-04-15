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
import emlab.gen.domain.agent.StrategicReserveOperator;
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
import emlab.gen.role.capacitymechanisms.ProcessAcceptedPowerPlantDispatchRoleinSR;
import emlab.gen.role.capacitymechanisms.StrategicReserveOperatorRole;
import emlab.gen.trend.TriangularTrend;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/emlab-gen-test-context.xml" })
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

    @Autowired
    ProcessAcceptedPowerPlantDispatchRoleinSR acceptedPowerPlantDispatchRoleinSR;

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
        p1.setForecast(false);
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
        p11.setForecast(false);
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
        p111.setForecast(false);
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
        p1111.setForecast(false);
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
        p11111.setForecast(false);
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
        p111111.setForecast(false);
        p111111.persist();

        // for Zone 2 Segment 1

        // PowerPlantDispatchPlan p2 = new PowerPlantDispatchPlan();
        // p2.setAmount(1700.0d);
        // p2.setSegment(S1);
        // p2.setPrice(5.0d);
        // p2.setTime(0l);
        // p2.setBiddingMarket(market2);
        // p2.setPowerPlant(pp4);
        // p2.setBidder(e3);
        // p2.setStatus(3);
        // p2.setAcceptedAmount(1700);
        // p2.persist();
        //
        // PowerPlantDispatchPlan p22 = new PowerPlantDispatchPlan();
        // p22.setAmount(2000.0d);
        // p22.setSegment(S1);
        // p22.setPrice(15.0d);
        // p22.setTime(0l);
        // p22.setBiddingMarket(market2);
        // p22.setPowerPlant(pp5);
        // p22.setBidder(e2);
        // p22.setStatus(3);
        // p22.setAcceptedAmount(2000);
        // p22.persist();
        //
        // PowerPlantDispatchPlan p222 = new PowerPlantDispatchPlan();
        // p222.setAmount(300.0d);
        // p222.setSegment(S1);
        // p222.setPrice(7.0d);
        // p222.setTime(0l);
        // p222.setBiddingMarket(market2);
        // p222.setPowerPlant(pp6);
        // p222.setBidder(e1);
        // p222.setStatus(3);
        // p222.setAcceptedAmount(300);
        // p222.persist();
        //
        // // Zone 2 segment 2
        //
        // PowerPlantDispatchPlan p21 = new PowerPlantDispatchPlan();
        // p21.setAmount(1700.0d);
        // p21.setSegment(S2);
        // p21.setPrice(5.0d);
        // p21.setTime(0l);
        // p21.setBiddingMarket(market2);
        // p21.setPowerPlant(pp4);
        // p21.setBidder(e3);
        // p21.setStatus(3);
        // p21.setAcceptedAmount(1700);
        // p21.persist();
        //
        // PowerPlantDispatchPlan p221 = new PowerPlantDispatchPlan();
        // p221.setAmount(2000.0d);
        // p221.setSegment(S2);
        // p221.setPrice(15.0d);
        // p221.setTime(0l);
        // p221.setBiddingMarket(market2);
        // p221.setPowerPlant(pp5);
        // p221.setBidder(e2);
        // p221.setStatus(-1);
        // p221.setAcceptedAmount(0);
        // p221.persist();
        //
        // PowerPlantDispatchPlan p2221 = new PowerPlantDispatchPlan();
        // p2221.setAmount(300.0d);
        // p2221.setSegment(S2);
        // p2221.setPrice(7.0d);
        // p2221.setTime(0l);
        // p2221.setBiddingMarket(market2);
        // p2221.setPowerPlant(pp6);
        // p2221.setBidder(e1);
        // p2221.setStatus(3);
        // p2221.setAcceptedAmount(300);
        // p2221.persist();

        SegmentClearingPoint clearingPoint1 = new SegmentClearingPoint();
        clearingPoint1.setSegment(S1);
        clearingPoint1.setAbstractMarket(market1);
        clearingPoint1.setPrice(25);
        clearingPoint1.setTime(0l);
        clearingPoint1.setForecast(false);

        // SegmentClearingPoint clearingPoint11 = new SegmentClearingPoint();
        // clearingPoint11.setSegment(S1);
        // clearingPoint11.setAbstractMarket(market2);
        // clearingPoint11.setPrice(30);
        // clearingPoint11.setTime(0l);

        SegmentClearingPoint clearingPoint111 = new SegmentClearingPoint();
        clearingPoint111.setSegment(S2);
        clearingPoint111.setAbstractMarket(market1);
        clearingPoint111.setPrice(7);
        clearingPoint111.setTime(0l);
        clearingPoint111.setForecast(false);

        // SegmentClearingPoint clearingPoint1111 = new SegmentClearingPoint();
        // clearingPoint1111.setSegment(S2);
        // clearingPoint1111.setAbstractMarket(market2);
        // clearingPoint1111.setPrice(7);
        // clearingPoint1111.setTime(0l);

        clearingPoint1.persist();
        // clearingPoint1111.persist();
        clearingPoint111.persist();
        // clearingPoint11.persist();

        StrategicReserveOperator strategicReserveOperator1 = new StrategicReserveOperator();

        strategicReserveOperator1.setReserveVolumePercentSR(0.3);
        strategicReserveOperator1.setReservePriceSR(25);
        strategicReserveOperator1.setCash(0);
        strategicReserveOperator1.setName("SRO1");
        strategicReserveOperator1.setZone(zone1);
        strategicReserveOperator1.persist();

        // StrategicReserveOperator strategicReserveOperator11 = new
        // StrategicReserveOperator();
        //
        // strategicReserveOperator11.setReserveVolumePercentSR(0.3);
        // strategicReserveOperator11.setReservePriceSR(30);
        // strategicReserveOperator11.setCash(0);
        // strategicReserveOperator11.setName("SRO2");
        // strategicReserveOperator11.setZone(zone2);
        // strategicReserveOperator11.persist();

        // logger.warn("P1 owner is + "
        // +p1.getPowerPlant().getOwner().getName());

        /*
         * for (PowerPlantDispatchPlan currDispatchPlan :
         * plantDispatchPlanRepository.findAll()){ logger.warn("Test1 Volume: "
         * + currDispatchPlan.getAmount()); logger.warn("Test1 Price: " +
         * currDispatchPlan.getPrice()); }
         */
        strategicReserveOperatorRole.act(strategicReserveOperator1);
        // strategicReserveOperatorRole.act(strategicReserveOperator11);

        acceptedPowerPlantDispatchRoleinSR.act(strategicReserveOperator1);
        // acceptedPowerPlantDispatchRoleinSR.act(strategicReserveOperator11);


        System.out.print("New Bidding Prices P1 " + p1.getPrice());
        System.out.print("New Bidding Prices P11 " + p11.getPrice());
        System.out.print("New Bidding Prices P111 " + p111.getPrice());
        //
        // logger.warn("New Bidding Prices P2 " + p2.getPrice());
        // logger.warn("New Bidding Prices P22 " + p22.getPrice());
        // logger.warn("New Bidding Prices P222 " + p222.getPrice());

        /*
         * logger.warn("Cash of E1 " + e1.getCash()); logger.warn("Cash of E2 "
         * + e2.getCash()); logger.warn("Cash of E3 " + e3.getCash());
         * logger.warn("Cash of SRO1 "+ strategicReserveOperator1.getCash());
         * logger.warn("Cash of SRO2 "+ strategicReserveOperator11.getCash());
         */

        // logger.warn(strategicReserveOperator.getReserveVolumePercent());

        /*
         * for (PowerPlantDispatchPlan currDispatchPlan :
         * plantDispatchPlanRepository.findAll()){ logger.warn("Test2 Volume: "
         * + currDispatchPlan.getAmount()); logger.warn("Test2 Price: " +
         * currDispatchPlan.getPrice()); logger.warn("Test 2 Status: "+
         * currDispatchPlan.getORstatus()); }
         */
        // logger.warn(strategicReserveOperator.getReserveVolume());

        /*
         * for (Zone curZone : zoneRepository.findAll()){
         * //logger.warn(curZone); ElectricitySpotMarket market =
         * marketRepository.findElectricitySpotMarketForZone(curZone);
         * //logger.warn(market.getName()); //plantDispatchPlanRepository.
         * DescendingListAllPowerPlantDispatchPlansbyMarketTimeSegment(market,
         * S1, 0); double peaky
         * =segmentLoadRepository.peakLoadbyZoneMarketandTime(curZone, market);
         * //logger.warn(peaky); Double PeakLoad
         * =peaky*market.getDemandGrowthTrend().getValue(0);
         * //logger.warn(market.getDemandGrowthTrend().getValue(0));
         * //logger.warn(PeakLoad);
         * 
         * double segmentCounter = reps.segmentRepository.count();
         * //logger.warn(segmentCounter);
         */
        /*
         * for(Segment currentSegment: reps.segmentRepository.findAll()){
         * 
         * Iterable<PowerPlantDispatchPlan> sortedListofPPDP =
         * plantDispatchPlanRepository
         * .findDescendingSortedPowerPlantDispatchPlansForSegmentForTime
         * (currentSegment, 0); for (PowerPlantDispatchPlan
         * curDispatchPlan:sortedListofPPDP){
         * logger.warn(curDispatchPlan.getAmount()); } }
         * //System.out.println(PeakLoad);
         */

    }

}

// }
