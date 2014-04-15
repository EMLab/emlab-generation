/*******************************************************************************
 * Copyright 2012 the original author or authors.
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
package emlab.gen.role.market;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import agentspring.role.Role;
import agentspring.role.RoleComponent;
import emlab.gen.domain.contract.CashFlow;
import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.domain.market.electricity.PowerPlantDispatchPlan;
import emlab.gen.domain.market.electricity.Segment;
import emlab.gen.domain.market.electricity.SegmentClearingPoint;
import emlab.gen.repository.Reps;

@RoleComponent
public class ProcessAcceptedPowerPlantDispatchRole extends AbstractMarketRole<ElectricitySpotMarket> implements Role<ElectricitySpotMarket> {

    @Autowired
    Reps reps;

    @Override
    @Transactional
    public void act(ElectricitySpotMarket esm) {

        for (Segment segment : reps.segmentRepository.findAll()) {
            SegmentClearingPoint scp = reps.segmentClearingPointRepository.findOneSegmentClearingPointForMarketSegmentAndTime(
                    getCurrentTick(), segment, esm, false);
            for (PowerPlantDispatchPlan plan : reps.powerPlantDispatchPlanRepository
                    .findAllAcceptedPowerPlantDispatchPlansForMarketSegmentAndTime(esm, segment, getCurrentTick(),
                            false)) {

                reps.nonTransactionalCreateRepository.createCashFlow(esm, plan.getBidder(), plan.getAcceptedAmount() * scp.getPrice()
                        * segment.getLengthInHours(), CashFlow.ELECTRICITY_SPOT, getCurrentTick(), plan.getPowerPlant());
            }

        }

    }

    @Override
    public Reps getReps() {
        return reps;
    }

}
