package emlab.role.capacitymechanisms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import agentspring.role.AbstractRole;
import agentspring.role.Role;

import emlab.domain.agent.StrategicReserveOperator;
import emlab.domain.contract.CashFlow;

import emlab.domain.market.Bid;
import emlab.domain.market.electricity.ElectricitySpotMarket;
import emlab.domain.market.electricity.PowerPlantDispatchPlan;
import emlab.domain.market.electricity.Segment;

import emlab.repository.BidRepository;
import emlab.repository.MarketRepository;
import emlab.repository.PowerPlantDispatchPlanRepository;
import emlab.repository.Reps;
import emlab.repository.SegmentLoadRepository;

/**
 * 
 * @author pbhagwat
 *
 */

public abstract class StrategicReserveOperatorRole extends AbstractRole<StrategicReserveOperator> implements Role<StrategicReserveOperator>{

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

	@Transactional
	public void act(StrategicReserveOperator strategicReserveOperator) {

		Iterable<ElectricitySpotMarket> market = marketRepository.findAllElectricitySpotMarkets();


		for (ElectricitySpotMarket currentMarket: market){

			double peakLoadforMarket = segmentLoadRepository.calculatePeakLoadbyMarketandTime(currentMarket, getCurrentTick());
			double segmentCounter=0;
			// Calculates volume to be contracted

			strategicReserveOperator.setReserveVolume(peakLoadforMarket*strategicReserveOperator.getReserveVolumePercent());

			// Sets Dispatch price set by operator

			strategicReserveOperator.getReservePrice();

			// Contract Powers Power Plants

			// Iterable<Bid> sortedListofBidPairs = bidRepository.findOffersDescendingForMarketForTime(currentMarket, getCurrentTick());

			//finds List of all segments
			//List<Segment> segments = Utils.asList(reps.segmentRepository.findAll());
			//for(Segment currentSegment: reps.segmentRepository.findAll()){
			//segmentCounter += 1;
			//}
			
			segmentCounter = reps.segmentRepository.count();

			for(Segment currentSegment: reps.segmentRepository.findAll()){


				Iterable<PowerPlantDispatchPlan> sortedListofPPDP = plantDispatchPlanRepository.findDescendingSortedPowerPlantDispatchPlansForSegmentForTime(currentSegment, getCurrentTick());

				boolean isORMarketCleared = false;
				double sumofContractedBids=0;
				double volumetobeContracted = strategicReserveOperator.getReserveVolume();
				double clearingEpsilon = 0.001;
				double dispatchPrice = strategicReserveOperator.getReservePrice();

				for (PowerPlantDispatchPlan currentPPDP: sortedListofPPDP){

					if (isORMarketCleared == false){

						if (volumetobeContracted-(sumofContractedBids + currentPPDP.getAmount()) >= clearingEpsilon){
							currentPPDP.setORstatus(Bid.CONTRACTED);
							sumofContractedBids += currentPPDP.getAmount();
							currentPPDP.setOldPrice(currentPPDP.getPrice());
							currentPPDP.setPrice(dispatchPrice);
							// Pays O&M costs to the generated for the contracted capacity
							double money = ((currentPPDP.getPowerPlant().getTechnology().getFixedOperatingCost())*currentPPDP.getAmount())/segmentCounter;
							reps.nonTransactionalCreateRepository.createCashFlow(strategicReserveOperator, currentPPDP.getPowerPlant().getOwner(), money, CashFlow.STRRESPAYMENT, getCurrentTick(), currentPPDP.getPowerPlant());

						}

						else if (volumetobeContracted-(sumofContractedBids + currentPPDP.getAmount()) < clearingEpsilon){
							currentPPDP.setORstatus(Bid.PARTLY_CONTRACTED);
							sumofContractedBids += currentPPDP.getAmount();
							currentPPDP.setOldPrice(currentPPDP.getPrice());
							currentPPDP.setPrice(dispatchPrice);
							isORMarketCleared = true;
							// Pays O&M costs to the generated for the contracted capacity
							double money = ((currentPPDP.getPowerPlant().getTechnology().getFixedOperatingCost())*currentPPDP.getAmount())/segmentCounter;
							reps.nonTransactionalCreateRepository.createCashFlow(strategicReserveOperator, currentPPDP.getPowerPlant().getOwner(), money, CashFlow.STRRESPAYMENT, getCurrentTick(), currentPPDP.getPowerPlant());
						}

					}
					else {
						currentPPDP.setORstatus(Bid.NOT_CONTRACTED);
					}

					if (volumetobeContracted-sumofContractedBids < clearingEpsilon){
						isORMarketCleared = true;
					}
				}
			}

		}

		// Update cashflows
		
		// Receives revenue if capacity is deployed


	}
}


