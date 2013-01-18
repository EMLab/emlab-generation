package emlab.role.capacitymechanisms;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.omg.CORBA.ORB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import agentspring.role.AbstractRole;
import agentspring.role.Role;
import emlab.domain.agent.EnergyConsumer;
import emlab.domain.agent.StrategicReserveOperator;
import emlab.domain.contract.LongTermContract;
import emlab.domain.market.Bid;
import emlab.domain.market.electricity.ElectricitySpotMarket;
import emlab.domain.market.electricity.Segment;
import emlab.domain.market.electricity.SegmentLoad;
import emlab.repository.BidRepository;
import emlab.repository.MarketRepository;
import emlab.repository.Reps;
import emlab.repository.SegmentLoadRepository;
import emlab.repository.StrategicReserveOperatorRepository;
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

	@Transactional
	public void act(StrategicReserveOperator strategicReserveOperator) {

		Iterable<ElectricitySpotMarket> market = marketRepository.findAllElectricitySpotMarkets();


		for (ElectricitySpotMarket currentMarket: market){

			double peakLoadforMarket = segmentLoadRepository.calculatePeakLoadbyMarketandTime(currentMarket, getCurrentTick());

			// Calculates volume to be contracted

			strategicReserveOperator.setReserveVolume(peakLoadforMarket*strategicReserveOperator.getReserveVolumePercent());

			// Sets Dispatch price set by operator

			strategicReserveOperator.getReservePrice();

			// Contract Powers Power Plants

			Iterable<Bid> sortedListofBidPairs = bidRepository.findOffersDescendingForMarketForTime(currentMarket, getCurrentTick());
			boolean isORMarketCleared = false;
			double sumofContractedBids=0;
			double volumetobeContracted = strategicReserveOperator.getReserveVolume();
			double clearingEpsilon = 0.001;
			double dispatchPrice = strategicReserveOperator.getReservePrice();

			for (Bid currentBid: sortedListofBidPairs){

				if (isORMarketCleared == false){

					if (volumetobeContracted-(sumofContractedBids + currentBid.getAmount()) >= clearingEpsilon){
						currentBid.setORstatus(Bid.CONTRACTED);
						sumofContractedBids += currentBid.getAmount();
						currentBid.setPrice(dispatchPrice);
					}
					
					else if (volumetobeContracted-(sumofContractedBids + currentBid.getAmount()) < clearingEpsilon){
						currentBid.setORstatus(Bid.PARTLY_CONTRACTED);
						sumofContractedBids += currentBid.getAmount();
						currentBid.setPrice(dispatchPrice);
						isORMarketCleared = true;
					}
						
				}
				else {
					currentBid.setORstatus(Bid.NOT_CONTRACTED);
				}
				
				if (volumetobeContracted-sumofContractedBids < clearingEpsilon){
					isORMarketCleared = true;
				}
			}


		}

		// Update cashflows
		// Pays O&M costs to the generated for the contracted capacity
		// Receives revenue if capacity is deployed


	}
}


