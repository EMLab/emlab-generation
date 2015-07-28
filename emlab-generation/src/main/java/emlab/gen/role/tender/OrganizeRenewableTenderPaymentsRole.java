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
import org.springframework.transaction.annotation.Transactional;

import agentspring.role.AbstractRole;
import agentspring.role.Role;
import agentspring.role.RoleComponent;
import emlab.gen.domain.contract.CashFlow;
import emlab.gen.domain.market.ClearingPoint;
import emlab.gen.domain.policy.renewablesupport.RenewableSupportSchemeTender;
import emlab.gen.domain.policy.renewablesupport.TenderBid;
import emlab.gen.repository.Reps;

/**
 * @author rjjdejeu
 *
 */

@RoleComponent
public class OrganizeRenewableTenderPaymentsRole extends AbstractRole<RenewableSupportSchemeTender> implements
        Role<RenewableSupportSchemeTender> {

    @Autowired
    Reps reps;

    @Override
    @Transactional
    public void act(RenewableSupportSchemeTender scheme) {

        // what about the other (earlier or later) bids that need to be paid
        // out? - i have included them as well with my if statement.

        // the following query should return only all accepted or partially
        // accepted bids - write a query that only returns accepted bids. Look
        // up powerPlantDispatchPlanRepository for examples - there are two.
        for (TenderBid currentTenderBid : reps.tenderBidRepository.findAllTenderBidsThatShouldBePaidInTimeStep(scheme,
                getCurrentTick())) {

            ClearingPoint tenderClearingPoint = reps.tenderClearingPointRepository
                    .findOneClearingPointForTimeAndRenewableSupportSchemeTender(getCurrentTick(), scheme);
            reps.nonTransactionalCreateRepository.createCashFlow(scheme, currentTenderBid.getBidder(),
                    currentTenderBid.getAcceptedAmount() * tenderClearingPoint.getPrice(), CashFlow.TENDER_SUBSIDY,
                    getCurrentTick(), currentTenderBid.getPowerPlant());

        }

    }

}