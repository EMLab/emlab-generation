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

    @Autowired
    ZoneRepository zoneRepository;

    //private Segment currentSegment;

    @Override
    @Transactional
    public void act(StrategicReserveOperator strategicReserveOperator) {

        // Insert a loop for zone so that strategic reserve can be turned on and off for certain zones
        Zone curZone = strategicReserveOperator.getZone();
        //logger.warn("is SR Present " + curZone.getNodeId());
        //if (curZone.isStrategicReserveOperatorDeployed() == true){

        //loop to see if this zone has a strategic reserve
        ElectricitySpotMarket market = marketRepository.findElectricitySpotMarketForZone(curZone);
        //logger.warn(market.getName());
        double peakLoadforMarketNOtrend = segmentLoadRepository.peakLoadbyZoneMarketandTime(curZone, market);

        double trend = market.getDemandGrowthTrend().getValue(getCurrentTick());

        double peakLoadforMarket = trend*peakLoadforMarketNOtrend;



        //multiply by whatever factor
        long segmentCounter=0;
        // Set volume to be contracted

        strategicReserveOperator.setReserveVolume(peakLoadforMarket*strategicReserveOperator.getReserveVolumePercentSR());
        //logger.warn(strategicReserveOperator.setReserveVolume(peakLoadforMarket*strategicReserveOperator.getReserveVolumePercent()));

        //Find peak supply in the market
        // double peakCapacityforMarket = reps.powerPlantRepository.calculatePeakCapacityOfOperationalPowerPlantsInMarket(market,
        //         getCurrentTick());

        // Find difference between peakload and peak capacity





        // Sets Dispatch price set by operator

        strategicReserveOperator.getReservePriceSR();
        //logger.warn(" Reserve Price " + strategicReserveOperator.getReservePriceSR());
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
            //logger.warn("Current segment is" + currentSegment);
            //find query for specific market


            boolean isORMarketCleared = false;
            double sumofContractedBids=0;
            double volumetobeContracted = strategicReserveOperator.getReserveVolume();
            //logger.warn("volumetobeContracted " + volumetobeContracted);
            double clearingEpsilon = 0.001;
            double dispatchPrice = strategicReserveOperator.getReservePriceSR();
            //logger.warn("dispatchPrice " + dispatchPrice);
            //double reserveMargin = peakCapacityforMarket-peakLoadforMarket;

            Iterable<PowerPlantDispatchPlan> sortedListofPPDP = plantDispatchPlanRepository
                    .findDescendingSortedPowerPlantDispatchPlansForSegmentForTime(currentSegment, getCurrentTick(),
                            false);

            for (PowerPlantDispatchPlan currentPPDP: sortedListofPPDP){

                //logger.warn("Bidding Market " + currentPPDP.getBiddingMarket().getNodeId().intValue());
                //logger.warn("Bidding Volume" + (currentPPDP.getAmount()));
                //logger.warn("current Market" + market.getNodeId().intValue());
                // **use querying for market**
                if (currentPPDP.getBiddingMarket().getNodeId().intValue() == market.getNodeId().intValue()){
                    //logger.warn("isOR market cleared" + isORMarketCleared);

                    // Check the size of margin

                    /* if (strategicReserveOperator.getReserveVolume()>reserveMargin){
                        if (reserveMargin-currentPPDP.getAmount()>strategicReserveOperator.getReserveVolume()){
                            currentPPDP.setSRstatus(PowerPlantDispatchPlan.NOT_CONTRACTED);
                            reserveMargin -= currentPPDP.getAmount();
                        }

                    }*/

                    if (volumetobeContracted==0){
                        isORMarketCleared=true;
                    }
                    else if (isORMarketCleared == false){
                        //logger.warn("volume of current PPDP " + currentPPDP.getAmount());
                        if (volumetobeContracted-(sumofContractedBids + currentPPDP.getAmount()) >= clearingEpsilon){

                            // check if already not contracted
                            //if(currentPPDP.getSRstatus()!=PowerPlantDispatchPlan.NOT_CONTRACTED){
                            //logger.warn("RemainingVolume" + (volumetobeContracted-(sumofContractedBids + currentPPDP.getAmount())));
                            currentPPDP.setSRstatus(PowerPlantDispatchPlan.CONTRACTED);
                            //logger.warn("SRSTATUS " +currentPPDP.getSRstatus());
                            sumofContractedBids += currentPPDP.getAmount();
                            currentPPDP.setOldPrice(currentPPDP.getPrice());
                            //logger.warn("Old Price" + currentPPDP.getOldPrice());
                            currentPPDP.setPrice(dispatchPrice);

                            //logger.warn("New Price" + currentPPDP.getPrice());
                            // Pays O&M costs to the generated for the contracted capacity

                            double Loan = 0;
                            if ((currentPPDP.getPowerPlant().getLoan().getTotalNumberOfPayments() - currentPPDP
                                    .getPowerPlant().getLoan().getNumberOfPaymentsDone()) > 0d) {
                                Loan = (currentPPDP.getPowerPlant().getLoan().getAmountPerPayment());
                            }

                            double money = ((currentPPDP.getPowerPlant().getActualFixedOperatingCost()) + Loan)
                                    / segmentCounter;
                            //logger.warn("Annual FOC "+ currentPPDP.getPowerPlant().getTechnology().getFixedOperatingCost());
                            //logger.warn("No of Segments " +segmentCounter);
                            //logger.warn("Money Paid " +money);

                            //logger.warn("SRO "+ strategicReserveOperator.getName() +" CASH Before" +strategicReserveOperator.getCash());
                            //logger.warn("Owner " + currentPPDP.getBidder().getName() + "money Before" +currentPPDP.getBidder().getCash());


                            reps.nonTransactionalCreateRepository.createCashFlow(strategicReserveOperator, currentPPDP.getBidder(), money, CashFlow.STRRESPAYMENT, getCurrentTick(), currentPPDP.getPowerPlant());

                            //logger.warn("SRO's CASH After" +strategicReserveOperator.getCash());
                            //logger.warn("Owner " + currentPPDP.getBidder().getName() + " money After" +currentPPDP.getBidder().getCash());
                            // }
                        }

                        else if (volumetobeContracted-(sumofContractedBids + currentPPDP.getAmount()) < clearingEpsilon){

                            // if(currentPPDP.getSRstatus()!=PowerPlantDispatchPlan.NOT_CONTRACTED){

                            currentPPDP.setSRstatus(PowerPlantDispatchPlan.PARTLY_CONTRACTED);
                            //logger.warn("SRSTATUS " +currentPPDP.getSRstatus());
                            sumofContractedBids += currentPPDP.getAmount();
                            currentPPDP.setOldPrice(currentPPDP.getPrice());
                            //logger.warn("Old Price" + currentPPDP.getOldPrice());
                            currentPPDP.setPrice(dispatchPrice);

                            //logger.warn("New Price" + currentPPDP.getPrice());
                            isORMarketCleared = true;
                            // Pays O&M costs and outstanding loans to the
                            // generated for the contracted capacity
                            double Loan = 0;
                            if ((currentPPDP.getPowerPlant().getLoan().getTotalNumberOfPayments() - currentPPDP
                                    .getPowerPlant().getLoan().getNumberOfPaymentsDone()) > 0d) {
                                Loan = (currentPPDP.getPowerPlant().getLoan().getAmountPerPayment());
                            }


                            double money = ((currentPPDP.getPowerPlant().getActualFixedOperatingCost()) + Loan)
                                    / segmentCounter;
                            //logger.warn("Annual FOC "+ currentPPDP.getPowerPlant().getTechnology().getFixedOperatingCost());
                            //logger.warn("No of Segments " +segmentCounter);
                            //logger.warn("Money Paid " +money);

                            //logger.warn("SRO "+ strategicReserveOperator.getName() +" CASH Before" +strategicReserveOperator.getCash());
                            //logger.warn("Owner " + currentPPDP.getBidder().getName() + "money Before" +currentPPDP.getBidder().getCash());

                            reps.nonTransactionalCreateRepository.createCashFlow(strategicReserveOperator, currentPPDP.getBidder(), money, CashFlow.STRRESPAYMENT, getCurrentTick(), currentPPDP.getPowerPlant());

                            //logger.warn("SRO's CASH After" +strategicReserveOperator.getCash());
                            //logger.warn("Owner " + currentPPDP.getBidder().getName() + " money After" +currentPPDP.getBidder().getCash());
                            //}
                        }

                    }
                    else {
                        currentPPDP.setSRstatus(PowerPlantDispatchPlan.NOT_CONTRACTED);

                    }
                    //logger.warn(volumetobeContracted-sumofContractedBids);
                    if (volumetobeContracted-sumofContractedBids < clearingEpsilon){
                        //logger.warn("is market clear" + isORMarketCleared);
                        isORMarketCleared = true;
                    }
                    //logger.warn(" iS OR CLEARED "+isORMarketCleared);
                    //logger.warn("Price is "+currentPPDP.getPrice());
                    currentPPDP.persist();
                }
            }
        }
        //logger.warn("cash of SR " +strategicReserveOperator.getCash());
    }

}