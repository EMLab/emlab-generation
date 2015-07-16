/*******************************************************************************
 * Copyright 2013 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package emlab.gen.role.tender;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.transaction.annotation.Transactional;

import agentspring.role.AbstractRole;
import agentspring.role.Role;
import emlab.gen.domain.agent.Regulator;
import emlab.gen.domain.market.Bid;
import emlab.gen.domain.market.ClearingPoint;
import emlab.gen.domain.policy.renewablesupport.TenderBid;
import emlab.gen.repository.Reps;

/**
 * @author rjjdejeu adapted from emlab.gen.role.capacitymarket
 */
public class ClearRenewableTenderRole extends AbstractRole<Regulator>implements Role<Regulator> {

    @Autowired
    Reps reps;

    @Autowired
    Neo4jTemplate template;

    //
    @Override
    @Transactional
    public void act(Regulator regulator) {

        // Initialize a sorted list for tender bids
        Iterable<TenderBid> sortedTenderBidPairsByPrice = null;

        // Query needs to be made for sortedTenderBidsByPrice in
        // tenderBid so I can put that in a list
        // something like this;
        // @Query(value="g.idx('__types__')[[className:'emlab.gen.domain.policy.renewablesupport.TenderBid']].filter{it.time == tick}.sort{it.price}._()",
        // type=QueryType.Gremlin)
        // Iterable<Bid> findAllSortedBidsByPrice(@Param("tick") long time);

        // Should get tenderBidByPrice and tenderQuota from
        // emlab.gen.domain.policy.renewablesupport

        // I am not sure how to improve line 60 and 61: I want to retreieve the tenderQuota and sorted Bids
        sortedTenderBidPairsByPrice = getSortedTenderBidsByPrice(getCurrentTick());
        double tenderQuota = regulator.getAnnualRenewableTargetInMwh();
        double sumOfTenderBidQuantityAccepted = 0d;
        double acceptedSubsidyPrice = 0d;
        boolean isTheTenderCleared = false;

        if (tenderQuota == 0) {
            isTheTenderCleared = true;
            acceptedSubsidyPrice = 0;
        }

        // This epsilon is to account for rounding errors for java (only
        // relevant for exact clearing)
        double clearingEpsilon = 0.001d;

        // Goes through the list of the bids that are sorted on ascending order
        // by price
        for (TenderBid currentTenderBid : sortedTenderBidPairsByPrice) {

            // if the tender is not cleared yet, it collects complete bids
            if (isTheTenderCleared == false) {
                if (tenderQuota - (sumOfTenderBidQuantityAccepted + currentTenderBid.getAmount()) >= -clearingEpsilon) {
                    acceptedSubsidyPrice = currentTenderBid.getPrice();
                    currentTenderBid.setStatus(Bid.ACCEPTED);
                    currentTenderBid.setAcceptedAmount(currentTenderBid.getAmount());
                    sumOfTenderBidQuantityAccepted = sumOfTenderBidQuantityAccepted + currentTenderBid.getAmount();
                }

                // it collects a bid partially if that bid fulfills the quota
                // partially
                else if (tenderQuota - (sumOfTenderBidQuantityAccepted + currentTenderBid.getAmount()) < clearingEpsilon) {
                    acceptedSubsidyPrice = currentTenderBid.getPrice();
                    currentTenderBid.setStatus(Bid.PARTLY_ACCEPTED);

                    // When I adapted this from ClearCapacityMarket, this line
                    // was
                    // reversed: sumOfTenderBidQuantityAccepted - tenderQuota
                    currentTenderBid.setAcceptedAmount((tenderQuota - sumOfTenderBidQuantityAccepted));
                    sumOfTenderBidQuantityAccepted = sumOfTenderBidQuantityAccepted
                            + currentTenderBid.getAcceptedAmount();
                    isTheTenderCleared = true;
                }
                // the tenderQuota is reached and the bids after that are not
                // accepted
            } else {
                currentTenderBid.setStatus(Bid.FAILED);
                currentTenderBid.setAcceptedAmount(0);
            }

            currentTenderBid.persist();
        }

        // This information needs to go into a query too for payments
        

        if (isTheTenderCleared == true) {
            ClearingPoint tenderClearingPoint = new ClearingPoint();
            tenderClearingPoint.setPrice(acceptedSubsidyPrice);
            tenderClearingPoint.setVolume(sumOfTenderBidQuantityAccepted);
            tenderClearingPoint.setTime(getCurrentTick());
            tenderClearingPoint.persist();

        } else {
            ClearingPoint tenderClearingPoint = new ClearingPoint();
            tenderClearingPoint.setPrice(acceptedSubsidyPrice);
            tenderClearingPoint.setVolume(sumOfTenderBidQuantityAccepted);
            tenderClearingPoint.setTime(getCurrentTick());
            tenderClearingPoint.persist();

        }
        
     // Accepted and partly accepted BIDs are updated and new parameters are defined;
    
        // defines the lag in starting time of payments, zero for now
        int paymentDelayTime = 0;  
        // defines the lag in starting time for building & permitting, zero for now
        int buildAndPermitDelayTime = 0;   
        // contractLength will be initiated from a scenario file
        int contractLength = 15;
        
        //KAVERI all of these properties are either defined in the regulator or the renewable support scheme. that;s where you have to get it from. 
        //KAVERI explain the idea of a role as a person, who only gets basically information from the object that he extends from, 
        // or the classes the higher class is related to, or the objects that it directly gets from the repository. 
        
        
        int delayTime =  paymentDelayTime +  buildAndPermitDelayTime;
        int startingTimePayments = (int) (getCurrentTick() + delayTime);
        int endTimePayments = startingTimePayments + contractLength;

        // I am not sure what the right syntax is for selecting accepted bids
        
        // Kaveri- should be done as an iterable, like everything else in EMLAb. 
        // for instance, first /get/ all the exisiting bids and then apply this if statement. Or you could use filter in the query itself. #
        //However, what is more worrying is that you are using undefined variables again.
        if (TenderBid = Bid.ACCEPTED || currentTenderBid = Bid.PARTLY_ACCEPTED ) {
            
            // KAVERI where is the plant being built
            contractPrice = tenderClearingPoint.getTenderClearingPrice;
            amount = tenderClearingPoint.getAcceptedAmount;
            startingTimePayments = (int) getCurrentTick() + delayTime;
            endTimePayments = startingTimePayments + ContractLength;
            cashflow = amount * contractPrice;

            // This should give the order to start building once the delayTime counted down
            if (delayTime = tick() - getCurrentTick()) {
             // I am not sure how to update node number with technology and capacity from here
            }
        }
        
        
    }
}
