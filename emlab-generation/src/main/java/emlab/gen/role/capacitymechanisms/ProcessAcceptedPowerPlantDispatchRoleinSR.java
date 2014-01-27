package emlab.gen.role.capacitymechanisms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import agentspring.role.AbstractRole;
import agentspring.role.Role;
import agentspring.role.RoleComponent;
import emlab.gen.domain.agent.StrategicReserveOperator;
import emlab.gen.domain.contract.CashFlow;
import emlab.gen.domain.gis.Zone;
import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.domain.market.electricity.PowerPlantDispatchPlan;
import emlab.gen.domain.market.electricity.Segment;
import emlab.gen.domain.market.electricity.SegmentClearingPoint;
import emlab.gen.repository.BidRepository;
import emlab.gen.repository.MarketRepository;
import emlab.gen.repository.PowerPlantDispatchPlanRepository;
import emlab.gen.repository.Reps;
import emlab.gen.repository.SegmentLoadRepository;
import emlab.gen.repository.ZoneRepository;

/**
 * 
 * @author pbhagwat
 *
 */
@RoleComponent
public class ProcessAcceptedPowerPlantDispatchRoleinSR extends AbstractRole<StrategicReserveOperator> implements Role<StrategicReserveOperator>{

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

    @Override
    @Transactional
    public void act(StrategicReserveOperator strategicReserveOperator) {

        Zone curZone = strategicReserveOperator.getZone();
        //logger.warn("Entering Zone loop");
        ElectricitySpotMarket market = marketRepository.findElectricitySpotMarketForZone(curZone);
        //logger.warn(market.getName());
        for (Segment segment : reps.segmentRepository.findAll()) {
            //logger.warn("Entering Segment Loop" + segment.getLengthInHours());
            SegmentClearingPoint scp = reps.segmentClearingPointRepository.findOneSegmentClearingPointForMarketSegmentAndTime(
                    getCurrentTick(), segment, market, false);
            //logger.warn("Clearing Price " + scp.getPrice());
            for (PowerPlantDispatchPlan plan : reps.powerPlantDispatchPlanRepository
                    .findAllPowerPlantDispatchPlansForSegmentForTime(segment, getCurrentTick(), false)) {
                //logger.warn("Entering PPDP LOOP Successfully" +plan.getOldPrice());
                if (plan.getBiddingMarket().getNodeId().intValue()== market.getNodeId().intValue()){
                    //logger.warn("Bidding Market LOOP entered successfully " + plan.getBiddingMarket().getName());
                    if (plan.getStatus()>=2){
                        //logger.warn("Checking Accepted Bids finding accepted bids " +plan.getStatus());
                        if (plan.getSRstatus() <= -10){
                            //logger.warn("Checking SR Status Contracted " + plan.getSRstatus());
                            double moneyReturned = ((plan.getAcceptedAmount()*scp.getPrice()*segment.getLengthInHours())- ((plan.getAcceptedAmount()*plan.getOldPrice()*segment.getLengthInHours())));
                            // Price mark up /(plan.getPowerPlant().getOwner().getPriceMarkUp())
                            //logger.warn("Money Earned " +(plan.getAcceptedAmount()*scp.getPrice()*segment.getLengthInHours()));
                            //logger.warn("Money Kept "+ (plan.getAcceptedAmount()*plan.getOldPrice()*segment.getLengthInHours()));
                            //logger.warn("money Returned " +moneyReturned);

                            //logger.warn("SRO "+ strategicReserveOperator.getName() +" CASH Before" +strategicReserveOperator.getCash());
                            //logger.warn("Owner " + plan.getBidder().getName() + "money After" +plan.getBidder().getCash());

                            reps.nonTransactionalCreateRepository.createCashFlow(plan.getBidder(), strategicReserveOperator, moneyReturned, CashFlow.STRRESPAYMENT, getCurrentTick(), plan.getPowerPlant());

                            //logger.warn("SRO's CASH After" +strategicReserveOperator.getCash());
                            //logger.warn("Owner " + plan.getBidder().getName() + " money After" +plan.getBidder().getCash());
                        }
                    }
                }

            }

        }
    }
}