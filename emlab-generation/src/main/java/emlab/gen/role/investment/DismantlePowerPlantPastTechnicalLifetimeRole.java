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
package emlab.gen.role.investment;

import org.springframework.beans.factory.annotation.Autowired;

import agentspring.role.AbstractRole;
import agentspring.role.Role;
import agentspring.role.RoleComponent;
import emlab.gen.domain.agent.EnergyProducer;
import emlab.gen.domain.technology.PowerPlant;
import emlab.gen.repository.Reps;

/**
 * {@link EnergyProducer}s dismantle {@link PowerPlant}s that are pass the
 * technical life
 * 
 * @author <a href="mailto:E.J.L.Chappin@tudelft.nl">Emile Chappin</a> @author
 *         <a href="mailto:A.Chmieliauskas@tudelft.nl">Alfredas
 *         Chmieliauskas</a>
 * 
 */

@RoleComponent
public class DismantlePowerPlantPastTechnicalLifetimeRole extends AbstractRole<EnergyProducer> implements
        Role<EnergyProducer> {

    @Autowired
    Reps reps;

    @Override
    public void act(EnergyProducer producer) {

        logger.info("Dismantling plants if passed technical lifetime");

        // dismantle plants when passed technical lifetime
        for (PowerPlant plant : reps.powerPlantRepository.findOperationalPowerPlantsByOwner(producer, getCurrentTick())) {

            int prolongYearsOfDismantlng = producer.getDismantlingProlongingYearsAfterTechnicalLifetime();
            if (!plant.isWithinTechnicalLifetime(getCurrentTick() + prolongYearsOfDismantlng)) {
                logger.info("       Dismantling power plant because the technical life time has passed: " + plant);
                // double currentnumberofplants =
                // plant.getSiteLocation().getPlantPresent();

                // plant.getSiteLocation().setPlantPresent(currentnumberofplants
                // - 1);

                plant.dismantlePowerPlant(getCurrentTick());

            }
        }
    }

}
