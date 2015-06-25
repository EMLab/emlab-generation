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
import emlab.gen.domain.policy.renewablesupport.TenderDispatchPlan;
import emlab.gen.repository.Reps;

/**
 * @author rjjdejeu
 *
 */
public class TenderClearingRole extends AbstractRole<Regulator> implements Role<Regulator> {

    @Autowired
    Reps reps;

    @Autowired
    Neo4jTemplate template;

    @Override
    @Transactional
    public void act(Regulator regulator) {

        Iterable<TenderDispatchPlan> sortedListofTenderDispatchPlan = null;

        // Query needs to be made for sortedTenderBidsByPrice in a
        // tenderReposity so I can put that in a list
        // something like this;
        // @Query(value="g.idx('__types__')[[className:'emlab.gen.domain.market.Bid']].filter{it.time == tick}.sort{it.price}._()",
        // type=QueryType.Gremlin)
        // Iterable<Bid> findAllSortedBidsByPrice(@Param("tick") long time);

        sortedListofTenderDispatchPlan = reps.tenderRepository.sortedTenderBidsByPrice(getCurrentTick());
        double relativeRenewableTarget = 0d;
        double sumOfTenderBidQuantityAccepted = 0d;
        double acceptedSubsidyPrice = 0d;
        boolean isTheTenderCleared = false;

        // This epsilon is to account for rounding errors for java (only
        // relevant for exact clearing)
        double clearingEpsilon = 0.001d;

        // Should I add line 66-73 in regulator.java (in domain.agent) ???
        // public double getRelativeRenewableTarget() {
        // return RelativeRenewableTarget;
        // }
        //
        // public void setRelativeRenewableTarget(double
        // RelativeRenewableTarget) {
        // this.RelativeRenewableTarget = RelativeRenewableTarget;

        if (regulator.getRelativeRenewableTarget() == 0) {
            isTheTenderCleared = true;
            acceptedSubsidyPrice = 0;
        }

        for (TenderDispatchPlan currentTenderDispatchPlan : sortedListofTenderDispatchPlan) {

            if (isTheTenderCleared == false) {
                if (relativeRenewableTarget - (sumOfTenderBidQuantityAccepted + currentTenderDispatchPlan.getAmount()) >= -clearingEpsilon) {
                    acceptedSubsidyPrice = currentTenderDispatchPlan.getPrice();
                    currentTenderDispatchPlan.setStatus(Bid.ACCEPTED);
                    currentTenderDispatchPlan.setAcceptedAmount(currentTenderDispatchPlan.getAmount());
                    sumOfTenderBidQuantityAccepted = sumOfTenderBidQuantityAccepted
                            + currentTenderDispatchPlan.getAmount();
                }

                else if (relativeRenewableTarget
                        - (sumOfTenderBidQuantityAccepted + currentTenderDispatchPlan.getAmount()) < clearingEpsilon) {
                    currentTenderDispatchPlan.setStatus(Bid.PARTLY_ACCEPTED);
                    currentTenderDispatchPlan
                            .setAcceptedAmount((sumOfTenderBidQuantityAccepted - relativeRenewableTarget));
                    acceptedSubsidyPrice = currentTenderDispatchPlan.getPrice();
                    sumOfTenderBidQuantityAccepted = sumOfTenderBidQuantityAccepted
                            + currentTenderDispatchPlan.getAcceptedAmount();
                    isTheTenderCleared = true;
                }

            } else {
                currentTenderDispatchPlan.setStatus(Bid.FAILED);
                currentTenderDispatchPlan.setAcceptedAmount(0);
            }

            if (relativeRenewableTarget - sumOfTenderBidQuantityAccepted < clearingEpsilon)
                isTheTenderCleared = true;

        }

    }

    // TenderClearingPoint clearingPoint = new TenderClearingPoint();
    {
        if (isTheTenderCleared == true) {
            sumOfTenderBidQuantityAccepted = relativeRenewableTarget;
            ClearingPoint clearingPoint = new ClearingPoint();
            clearingPoint.setPrice(acceptedSubsidyPrice);
            clearingPoint.setVolume(sumOfTenderBidQuantityAccepted);
            clearingPoint.setTime(getCurrentTick());
            clearingPoint.persist();

        } else {
            ClearingPoint clearingPoint = new ClearingPoint();
            clearingPoint.setPrice(lastAcceptedBid.getBidPrice);
            clearingPoint.setVolume(sumofSupplyBidsAccepted);
            clearingPoint.setTime(getCurrentTick());
            clearingPoint.persist();

        }
    }
}
