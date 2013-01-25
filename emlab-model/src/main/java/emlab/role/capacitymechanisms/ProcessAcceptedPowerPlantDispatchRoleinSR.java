package emlab.role.capacitymechanisms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import agentspring.role.AbstractRole;
import agentspring.role.Role;
import agentspring.role.RoleComponent;
import emlab.domain.agent.StrategicReserveOperator;
import emlab.domain.contract.CashFlow;
import emlab.domain.market.electricity.ElectricitySpotMarket;
import emlab.domain.market.electricity.PowerPlantDispatchPlan;
import emlab.domain.market.electricity.Segment;
import emlab.domain.market.electricity.SegmentClearingPoint;
import emlab.repository.MarketRepository;
import emlab.repository.Reps;

/**
 * 
 * @author pbhagwat
 *
 */
@RoleComponent
public abstract class ProcessAcceptedPowerPlantDispatchRoleinSR extends AbstractRole<StrategicReserveOperator> implements Role<StrategicReserveOperator>{

	@Autowired
	Reps reps;

	@Autowired
	MarketRepository marketRepository;

	@Transactional
	public void act(StrategicReserveOperator strategicReserveOperator) {

		Iterable<ElectricitySpotMarket> market = marketRepository.findAllElectricitySpotMarkets();

		for (ElectricitySpotMarket currentMarket: market){

			for (Segment segment : reps.segmentRepository.findAll()) {

				SegmentClearingPoint scp = reps.segmentClearingPointRepository.findOneSegmentClearingPointForMarketSegmentAndTime(
						getCurrentTick(), segment, currentMarket);
				for (PowerPlantDispatchPlan plan : reps.powerPlantDispatchPlanRepository
						.findAllAcceptedPowerPlantDispatchPlansForMarketSegmentAndTime(currentMarket, segment, getCurrentTick())) {

					if (plan.getORstatus() <= -10){
						double moneyReturned = ((plan.getAcceptedAmount()*scp.getPrice()*segment.getLengthInHours()) - ((plan.getAcceptedAmount()*plan.getOldPrice()*segment.getLengthInHours()) / (plan.getPowerPlant().getOwner().getPriceMarkUp())));
						reps.nonTransactionalCreateRepository.createCashFlow(plan.getPowerPlant().getOwner(), strategicReserveOperator, moneyReturned, CashFlow.STRRESPAYMENT, getCurrentTick(), plan.getPowerPlant());
						
					}
				}
			}

		}

	}}
