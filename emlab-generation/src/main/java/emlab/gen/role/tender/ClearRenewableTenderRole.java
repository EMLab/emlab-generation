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
public class ClearRenewableTenderRole extends AbstractRole<Regulator> implements Role<Regulator> {

    @Autowired
    Reps reps;

    @Autowired
    Neo4jTemplate template;

    // /vv
    @Override
    @Transactional
    public void act(Regulator regulator) {

        // Initialize a sorted list for tender bids
        Iterable<TenderBid> sortedListofTenderBids = null;

        // Query needs to be made for sortedTenderBidsByPrice in
        // tenderBid so I can put that in a list
        // something like this;
        // @Query(value="g.idx('__types__')[[className:'emlab.gen.domain.policy.renewablesupport.TenderBid']].filter{it.time == tick}.sort{it.price}._()",
        // type=QueryType.Gremlin)
        // Iterable<Bid> findAllSortedBidsByPrice(@Param("tick") long time);

        // Should get tenderBidByPrice and tenderQuota from
        // emlab.gen.domain.policy.renewablesupport
        sortedListofTenderBids = reps.tenderBid.sortedTenderBidsByPrice(getCurrentTick());
        double tenderQuoata = reps.RenewableSupportScheme.getTenderQuota;
        double sumOfTenderBidQuantityAccepted = 0d;
        double acceptedSubsidyPrice = 0d;
        boolean isTheTenderCleared = false;

        if (TenderQuota() == 0) {
            isTheMarketCleared = true;
            acceptedSubsidyPrice = 0;
        }

        // This epsilon is to account for rounding errors for java (only
        // relevant for exact clearing)
        double clearingEpsilon = 0.001d;

        // Goes through the list of the bids that are sorted on ascending order
        // by price
        for (TenderBid currentTenderBids : sortedListofTenderBids) {

            // if the tender is not cleared yet, it collects complete bids (line
            // 79),
            if (isTheTenderCleared == false) {
                if (tenderQuota - (sumOfTenderBidQuantityAccepted + currentTenderBids.getAmount()) >= -clearingEpsilon) {
                    acceptedSubsidyPrice = currentTenderBids.getPrice();
                    currentTenderBids.setStatus(Bid.ACCEPTED);
                    currentTenderBids.setAcceptedAmount(currentTenderBids.getAmount());
                    sumOfTenderBidQuantityAccepted = sumOfTenderBidQuantityAccepted + currentTenderBids.getAmount();
                }

                // it collects a bid partially if that bid fulfills the quota
                // partially(line 87)
                else if (tenderQuota - (sumOfTenderBidQuantityAccepted + currentTenderBids.getAmount()) < clearingEpsilon) {
                    currentTenderBids.setStatus(Bid.PARTLY_ACCEPTED);
                    currentTenderBids.setAcceptedAmount((sumOfTenderBidQuantityAccepted - tenderQuota));
                    acceptedSubsidyPrice = currentTenderBids.getPrice();
                    sumOfTenderBidQuantityAccepted = sumOfTenderBidQuantityAccepted
                            + currentTenderBids.getAcceptedAmount();
                    isTheTenderCleared = true;
                }
                // the tenderQuota is reached and the bids after that are not
                // accepted
            } else {
                currentTenderBids.setStatus(Bid.FAILED);
                currentTenderBids.setAcceptedAmount(0);
            }

            if (tenderQuota - sumOfTenderBidQuantityAccepted < clearingEpsilon)
                isTheTenderCleared = true;

        }

    }

    // This information needs to go into a query too for payments organization
    {
        if (isTheTenderCleared == true) {
            sumOfTenderBidQuantityAccepted = tenderQuota;
            ClearingPoint tenderClearingPoint = new ClearingPoint();
            tenderClearingPoint.setPrice(acceptedSubsidyPrice);
            tenderClearingPoint.setVolume(sumOfTenderBidQuantityAccepted);
            tenderClearingPoint.setTime(getCurrentTick());
            tenderClearingPoint.persist();

        } else {
            ClearingPoint tenderClearingPoint = new ClearingPoint();
            // lastAcceptedBid is a dummy here, needs to be defined better
            // The situation here is that the target is not reached, and the
            // last bid submitted bidPrice will
            // determine the subsidyPrice
            tenderClearingPoint.setPrice(lastAcceptedBid.getBidPrice);
            tenderClearingPoint.setVolume(sumOfTenderBidQuantityAccepted());
            tenderClearingPoint.setTime(getCurrentTick());
            tenderClearingPoint.persist();

        }
    }
}
