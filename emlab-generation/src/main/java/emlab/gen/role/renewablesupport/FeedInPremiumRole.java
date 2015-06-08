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
package emlab.gen.role.renewablesupport;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Transient;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.transaction.annotation.Transactional;

import agentspring.role.AbstractRole;
import emlab.gen.domain.agent.Regulator;
import emlab.gen.domain.policy.renewablesupport.RenewableSupportScheme;
import emlab.gen.domain.policy.renewablesupport.SupportPriceContract;
import emlab.gen.domain.technology.PowerGeneratingTechnology;
import emlab.gen.domain.technology.PowerPlant;
import emlab.gen.repository.Reps;

/**
 * @author Kaveri3012 for loop through eligible, operational power plants,
 *         create support price contract for each technology SupportPrice =
 *         electricityMarketPrice*(1+premiumFactor) for 15 years?
 * 
 *         Assumption: when the policy is implemented for a certain country, all
 *         operational, eligible plants in that zone receive the premium by
 *         default. there is no need for an energy producer agent to voluntarily
 *         apply for the scheme.
 * 
 * 
 */
public class FeedInPremiumRole extends AbstractRole<RenewableSupportScheme> {

    @Transient
    @Autowired
    Reps reps;

    @Transient
    @Autowired
    Neo4jTemplate template;

    @Transactional
    public void act(RenewableSupportScheme renewableSupportScheme) {

        Regulator regulator = new Regulator();
        regulator = renewableSupportScheme.getRegulator();

        Set<PowerGeneratingTechnology> technologySet = new HashSet<PowerGeneratingTechnology>();

        technologySet = renewableSupportScheme.getPowerGeneratingTechnologiesEligible();

        for (PowerGeneratingTechnology technology : technologySet) {
            for (PowerPlant plant : reps.powerPlantRepository.findOperationalPowerPlantsByTechnology(technology,
                    getCurrentTick())) {

                if (plant.isHasFeedInPremiumContract() == false) {

                    SupportPriceContract contract = new SupportPriceContract();

                    // todo: calculate electricity market price that the plant
                    // has earned this year in perUnit terms and
                    // multiply that by (1+ FeedInPremiumFactor)

                    // make feed in Premium Factor technology specific?

                    contract.setPricePerUnit(regulator.getFeedInPremiumFactor());
                    contract.setStart(getCurrentTick());
                    contract.setFinish(getCurrentTick() + regulator.getFeedInPremiumContractLength());
                    plant.setHasFeedInPremiumContract(true);
                }

                // create cash flow for plant of the subsidy. (or better do that
                // outside of this role,
                // so Rob de Jeu can use it too?
            }
        }

    }
}
