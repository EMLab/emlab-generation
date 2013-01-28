package emlab.role.capacitymechanisms;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import agentspring.role.AbstractRole;
import agentspring.role.Role;
import agentspring.role.RoleComponent;

import emlab.domain.agent.StrategicReserveOperator;
import emlab.domain.contract.CashFlow;
import emlab.domain.gis.Zone;

import emlab.domain.market.electricity.ElectricitySpotMarket;
import emlab.domain.market.electricity.PowerPlantDispatchPlan;
import emlab.domain.market.electricity.Segment;
import emlab.domain.market.electricity.SegmentClearingPoint;

import emlab.repository.BidRepository;
import emlab.repository.MarketRepository;
import emlab.repository.PowerPlantDispatchPlanRepository;
import emlab.repository.Reps;
import emlab.repository.SegmentLoadRepository;
import emlab.repository.ZoneRepository;
import emlab.role.StrategicReserveRoleTest;

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

	Logger logger = Logger.getLogger(StrategicReserveRoleTest.class);

	@Autowired
	ZoneRepository zoneRepository;

	@Transactional
	public void act(StrategicReserveOperator strategicReserveOperator) {

		for (Zone curZone : zoneRepository.findAll()){
			//logger.warn("Entering Zone loop");
			ElectricitySpotMarket market = marketRepository.findElectricitySpotMarketForZone(curZone);
			//logger.warn(market.getName());
			for (Segment segment : reps.segmentRepository.findAll()) {
				//logger.warn("Entering Segment Loop" + segment.getLengthInHours());
				SegmentClearingPoint scp = reps.segmentClearingPointRepository.findOneSegmentClearingPointForMarketSegmentAndTime(
						getCurrentTick(), segment, market);
				//logger.warn("Clearing Price " + scp.getPrice());
				for (PowerPlantDispatchPlan plan : reps.powerPlantDispatchPlanRepository.findAllPowerPlantDispatchPlansForSegmentForTime(segment, getCurrentTick())) {
					if (plan.getBiddingMarket().getNodeId()== market.getNodeId()){
						//logger.warn("Bidding Market " + plan.getBiddingMarket().getName());
						if (plan.getStatus()>=2){
							//logger.warn("Accepted Bids " +plan.getAcceptedAmount());
							if (plan.getSRstatus() <= -10){
								//logger.warn("SR Status" + plan.getSRstatus());
								double moneyReturned = ((plan.getAcceptedAmount()*scp.getPrice()*segment.getLengthInHours())- ((plan.getAcceptedAmount()*plan.getOldPrice()*segment.getLengthInHours())/(plan.getPowerPlant().getOwner().getPriceMarkUp())));
								//logger.warn("Money Earned " +(plan.getAcceptedAmount()*scp.getPrice()*segment.getLengthInHours()));
								//logger.warn("money Returned " +moneyReturned);
								
								reps.nonTransactionalCreateRepository.createCashFlow(plan.getBidder(), strategicReserveOperator, moneyReturned, CashFlow.STRRESPAYMENT, getCurrentTick(), plan.getPowerPlant());
								//logger.warn(strategicReserveOperator.getCash());
							}
						}
					}

				}

			}}}}
