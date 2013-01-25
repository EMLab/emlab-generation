package emlab.role.capacitymechanisms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import agentspring.role.AbstractRole;
import agentspring.role.Role;

import emlab.domain.agent.StrategicReserveOperator;
import emlab.domain.contract.CashFlow;
import emlab.domain.gis.Zone;

import emlab.domain.market.Bid;
import emlab.domain.market.electricity.ElectricitySpotMarket;
import emlab.domain.market.electricity.PowerPlantDispatchPlan;
import emlab.domain.market.electricity.Segment;

import emlab.repository.BidRepository;
import emlab.repository.MarketRepository;
import emlab.repository.PowerPlantDispatchPlanRepository;
import emlab.repository.Reps;
import emlab.repository.SegmentLoadRepository;
import emlab.repository.ZoneRepository;

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

	@Autowired
	ZoneRepository zoneRepository;

	//private Segment currentSegment;

	@Transactional
	public void act(StrategicReserveOperator strategicReserveOperator) {

		// Insert a loop for zone so that strategic reserve can be turned on and off for certain zones
		for (Zone curZone : zoneRepository.findAll()){

			//loop to see if this zone has a strategic reserve
			ElectricitySpotMarket market = marketRepository.findElectricitySpotMarketForZone(curZone);

			double peakLoadforMarketNOtrend = segmentLoadRepository.peakLoadbyZoneMarketandTime(curZone, market);
			double trend = market.getDemandGrowthTrend().getValue(getCurrentTick());
			double peakLoadforMarket = trend*peakLoadforMarketNOtrend;
			
			//multiply by whatever factor
			double segmentCounter=0;
			// Set volume to be contracted

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


			// Count all segments in the given market

			segmentCounter = reps.segmentRepository.count();
			// find all segments for the given market
			for(Segment currentSegment: reps.segmentRepository.findAll()){

				//find query for specific market
				Iterable<PowerPlantDispatchPlan> sortedListofPPDP = plantDispatchPlanRepository.findDescendingSortedPowerPlantDispatchPlansForSegmentForTime(currentSegment, getCurrentTick());

				boolean isORMarketCleared = false;
				double sumofContractedBids=0;
				double volumetobeContracted = strategicReserveOperator.getReserveVolume();
				double clearingEpsilon = 0.001;
				double dispatchPrice = strategicReserveOperator.getReservePrice();

				for (PowerPlantDispatchPlan currentPPDP: sortedListofPPDP){
					if (currentPPDP.getBiddingMarket() == market){

						if (isORMarketCleared == false){

							if (volumetobeContracted-(sumofContractedBids + currentPPDP.getAmount()) >= clearingEpsilon){
								currentPPDP.setORstatus(Bid.CONTRACTED);
								sumofContractedBids += currentPPDP.getAmount();
								currentPPDP.setOldPrice(currentPPDP.getPrice());
								currentPPDP.setPrice(dispatchPrice);
								// Pays O&M costs to the generated for the contracted capacity
								double money = ((currentPPDP.getPowerPlant().getTechnology().getFixedOperatingCost()))/segmentCounter;
								reps.nonTransactionalCreateRepository.createCashFlow(strategicReserveOperator, currentPPDP.getPowerPlant().getOwner(), money, CashFlow.STRRESPAYMENT, getCurrentTick(), currentPPDP.getPowerPlant());

							}

							else if (volumetobeContracted-(sumofContractedBids + currentPPDP.getAmount()) < clearingEpsilon){
								currentPPDP.setORstatus(Bid.PARTLY_CONTRACTED);
								sumofContractedBids += currentPPDP.getAmount();
								currentPPDP.setOldPrice(currentPPDP.getPrice());
								currentPPDP.setPrice(dispatchPrice);
								isORMarketCleared = true;
								// Pays O&M costs to the generated for the contracted capacity
								double money = ((currentPPDP.getPowerPlant().getTechnology().getFixedOperatingCost()))/segmentCounter;
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

		}

	}
}


