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
import emlab.role.StrategicReserveRoleTest;

/**
 * 
 * @author pbhagwat
 *
 */
@RoleComponent
public class StrategicReserveOperatorRole extends AbstractRole<StrategicReserveOperator> implements Role<StrategicReserveOperator>{

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

	//private Segment currentSegment;

	@Transactional
	public void act(StrategicReserveOperator strategicReserveOperator) {

		// Insert a loop for zone so that strategic reserve can be turned on and off for certain zones
		for (Zone curZone : zoneRepository.findAll()){
			//logger.warn("is SR Present " + curZone.isStrategicReserveOperatorDeployed());
			if (curZone.isStrategicReserveOperatorDeployed() == true){

				//loop to see if this zone has a strategic reserve
				ElectricitySpotMarket market = marketRepository.findElectricitySpotMarketForZone(curZone);
				//logger.warn(market.getName());
				double peakLoadforMarketNOtrend = segmentLoadRepository.peakLoadbyZoneMarketandTime(curZone, market);

				double trend = market.getDemandGrowthTrend().getValue(getCurrentTick());

				double peakLoadforMarket = trend*peakLoadforMarketNOtrend;


				//multiply by whatever factor
				double segmentCounter=0;
				// Set volume to be contracted

				strategicReserveOperator.setReserveVolume(peakLoadforMarket*curZone.getReserveVolumePercentSR());
				//logger.warn(strategicReserveOperator.setReserveVolume(peakLoadforMarket*strategicReserveOperator.getReserveVolumePercent()));

				// Sets Dispatch price set by operator

				curZone.getReservePriceSR();
				//logger.warn(strategicReserveOperator.getReservePrice());
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
					//logger.warn(currentSegment);
					//find query for specific market
					Iterable<PowerPlantDispatchPlan> sortedListofPPDP = plantDispatchPlanRepository.findDescendingSortedPowerPlantDispatchPlansForSegmentForTime(currentSegment, getCurrentTick());

					boolean isORMarketCleared = false;
					double sumofContractedBids=0;
					double volumetobeContracted = strategicReserveOperator.getReserveVolume();
					//logger.warn("volumetobeContracted " + volumetobeContracted);
					double clearingEpsilon = 0.001;
					double dispatchPrice = curZone.getReservePriceSR();
					//logger.warn("dispatchPrice " + dispatchPrice);

					for (PowerPlantDispatchPlan currentPPDP: sortedListofPPDP){

						//logger.warn("Bidding Market " + currentPPDP.getBiddingMarket().getName());
						//logger.warn("Bidding Volume" + (currentPPDP.getAmount()));
						//logger.warn("current Market" + market.getName());
						// **use querying for market**
						if (currentPPDP.getBiddingMarket().getNodeId() == market.getNodeId()){
							//logger.warn(isORMarketCleared);

							if (isORMarketCleared == false){
								//logger.warn("volume of current PPDP " + currentPPDP.getAmount());
								if (volumetobeContracted-(sumofContractedBids + currentPPDP.getAmount()) >= clearingEpsilon){

									//logger.warn("RemainingVolume" + (volumetobeContracted-(sumofContractedBids + currentPPDP.getAmount())));
									currentPPDP.setSRstatus(Bid.CONTRACTED);
									//logger.warn("SRSTATUS " +currentPPDP.getSRstatus());
									sumofContractedBids += currentPPDP.getAmount();
									currentPPDP.setOldPrice(currentPPDP.getPrice());
									currentPPDP.setPrice(dispatchPrice);
									//logger.warn("New Price" + currentPPDP.getPrice());
									// Pays O&M costs to the generated for the contracted capacity

									double money = ((currentPPDP.getPowerPlant().getTechnology().getFixedOperatingCost()))/segmentCounter;

									reps.nonTransactionalCreateRepository.createCashFlow(strategicReserveOperator, currentPPDP.getBidder(), money, CashFlow.STRRESPAYMENT, getCurrentTick(), currentPPDP.getPowerPlant());
									//logger.warn("Cash of SRO " + strategicReserveOperator.getCash());
									//logger.warn("cash of Owner " +  (currentPPDP.getPowerPlant().getOwner().getCash()));
								}

								else if (volumetobeContracted-(sumofContractedBids + currentPPDP.getAmount()) < clearingEpsilon){
									currentPPDP.setSRstatus(Bid.PARTLY_CONTRACTED);
									//logger.warn("SRSTATUS " +currentPPDP.getSRstatus());
									sumofContractedBids += currentPPDP.getAmount();
									currentPPDP.setOldPrice(currentPPDP.getPrice());
									currentPPDP.setPrice(dispatchPrice);
									isORMarketCleared = true;
									// Pays O&M costs to the generated for the contracted capacity
									double money = ((currentPPDP.getPowerPlant().getTechnology().getFixedOperatingCost()))/segmentCounter;
									reps.nonTransactionalCreateRepository.createCashFlow(strategicReserveOperator, currentPPDP.getBidder(), money, CashFlow.STRRESPAYMENT, getCurrentTick(), currentPPDP.getPowerPlant());
									//logger.warn("Cash of SRO " + strategicReserveOperator.getCash());
									//logger.warn("cash of Owner " +  (currentPPDP.getPowerPlant().getOwner().getCash()));
								}

							}
							else {
								currentPPDP.setSRstatus(Bid.NOT_CONTRACTED);
							}
							//logger.warn(volumetobeContracted-sumofContractedBids);
							if (volumetobeContracted-sumofContractedBids < clearingEpsilon){
								//logger.warn("is market clear" + isORMarketCleared);
								isORMarketCleared = true;
							}
							//logger.warn(isORMarketCleared);
						}
					}
				}

			}

		}
	}
}


