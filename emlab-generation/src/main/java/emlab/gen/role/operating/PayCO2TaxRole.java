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
package emlab.gen.role.operating;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import agentspring.role.Role;
import agentspring.role.RoleComponent;
import emlab.gen.domain.agent.EnergyProducer;
import emlab.gen.domain.agent.Government;
import emlab.gen.domain.contract.CashFlow;
import emlab.gen.domain.technology.PowerPlant;
import emlab.gen.repository.Reps;
import emlab.gen.role.AbstractEnergyProducerRole;

/**
 * {@link EnergyProducer}s pay CO2 taxes to the {@link Government}.
 * 
 * @author <a href="mailto:A.Chmieliauskas@tudelft.nl">Alfredas
 *         Chmieliauskas</a> @author <a
 *         href="mailto:E.J.L.Chappin@tudelft.nl">Emile Chappin</a>
 */
@RoleComponent
public class PayCO2TaxRole extends AbstractEnergyProducerRole implements Role<EnergyProducer> {

    @Autowired
    Reps reps;

    public Reps getReps() {
        return reps;
    }

    @Override
    @Transactional
    public void act(EnergyProducer producer) {
        logger.info("Pay the CO2 tax");

        Government government = reps.genericRepository.findFirst(Government.class);

        for (PowerPlant plant : reps.powerPlantRepository.findOperationalPowerPlantsByOwner(producer, getCurrentTick())) {
            double money = calculateCO2Tax(plant, false, getCurrentTick());
            CashFlow cf = reps.nonTransactionalCreateRepository.createCashFlow(producer, government, money, CashFlow.CO2TAX, getCurrentTick(), plant);
            logger.info("Cash flow created: {}", cf);
        }
    }

}
