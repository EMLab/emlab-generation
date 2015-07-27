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

import agentspring.role.AbstractRole;
import agentspring.role.Role;
import emlab.gen.domain.agent.EnergyProducer;
import emlab.gen.domain.agent.Regulator;
import emlab.gen.domain.policy.renewablesupport.RenewableSupportSchemeTender;
import emlab.gen.repository.Reps;

/**
 * @author Kaveri3012
 *
 */
public class TenderMainRole extends AbstractRole<RenewableSupportSchemeTender>
        implements Role<RenewableSupportSchemeTender> {

    /*
     * (non-Javadoc)
     * 
     * @see agentspring.role.Role#act(agentspring.agent.Agent)
     */
    @Autowired
    Reps reps;

    @Autowired
    CalculateRenewableTargetForTenderRole calculateRenewableTargetForTenderRole;

    @Autowired
    SubmitTenderBidRole submitTenderBidRole;

    @Autowired
    ClearRenewableTenderRole clearRenewableTenderRole;

    @Autowired
    OrganizeRenewableTenderPaymentsRole organizeRenewableTenderPaymentsRole;

    @Override
    public void act(RenewableSupportSchemeTender scheme) {

        Regulator regulator = scheme.getRegulator();

        calculateRenewableTargetForTenderRole.act(scheme);

        for (EnergyProducer producer : reps.genericRepository.findAllAtRandom(EnergyProducer.class)) {
            submitTenderBidRole.act(producer);
        }

        clearRenewableTenderRole.act(regulator);

        organizeRenewableTenderPaymentsRole.act(scheme);
    }

}
