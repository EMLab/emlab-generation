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

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import agentspring.role.Role;
import agentspring.role.RoleComponent;
import emlab.gen.domain.agent.EnergyProducer;
import emlab.gen.domain.contract.LongTermContract;
import emlab.gen.domain.market.Bid;
import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.domain.technology.PowerPlant;
import emlab.gen.repository.Reps;
import emlab.gen.role.AbstractEnergyProducerRole;

/**
 * {@link EnergyProducer} submits offers to the {@link ElectricitySpotMarket}.
 * One {@link Bid} per {@link PowerPlant}.
 * 
 * @author <a href="mailto:A.Chmieliauskas@tudelft.nl">Alfredas
 *         Chmieliauskas</a> @author <a
 *         href="mailto:E.J.L.Chappin@tudelft.nl">Emile Chappin</a>
 * 
 */
@RoleComponent
public class ReassignPowerPlantsToLongTermElectricityContractsRole extends
		AbstractEnergyProducerRole implements Role<EnergyProducer> {

    @Autowired
    Reps reps;

	@Transactional
	public void act(EnergyProducer producer) {

		// When old power plant is dismantled, we take over existing contract by
		// new power plant
		List<LongTermContract> ltcsToBeReplaced = new ArrayList<LongTermContract>();

		for (LongTermContract ltc : reps.contractRepository
				.findLongTermContractsForEnergyProducerActiveAtTime(producer,
						getCurrentTick())) {

			// Only if the contract is still active the next tick
			if (ltc.getFinish() > getCurrentTick()) {

				// if the underlying power plant is dismantled
				if (!ltc.getUnderlyingPowerPlant().isOperational(
						getCurrentTick())) {
					logger.info(
							"Powerplant {} underlying ltc {} is dismantled, contract should be reassigned",
							ltc.getUnderlyingPowerPlant(), ltc);
					ltcsToBeReplaced.add(ltc);
				} 
			}
		}

		// TODO random reassigning now. Should check volumes better and check
		// for similar technologies to make sure the type of contract fits.
		int nrOfLtcsToBeReplaced = ltcsToBeReplaced.size();
		int index = 0;

		// Go over the power plants to find one without a contract for each
		// contract that needs replacement
		if (nrOfLtcsToBeReplaced > 0) {
			for (PowerPlant plant : reps.powerPlantRepository
					.findOperationalPowerPlantsByOwner(producer,
							getCurrentTick())) {

				// if there are still contracts to be replaced
				if (index < nrOfLtcsToBeReplaced) {
					if (reps.contractRepository
							.findLongTermContractForPowerPlantActiveAtTime(
									plant, getCurrentTick()) == null) {
						if (plant.isWithinTechnicalLifetime(getCurrentTick())) {
							logger.warn(
									"Powerplant {} underlying ltc has been replaced by plant {}",
									ltcsToBeReplaced.get(index)
											.getUnderlyingPowerPlant(), plant);
							reps.contractRepository
									.reassignLongTermContractToNewPowerPlant(
											ltcsToBeReplaced.get(index), plant);
							index++;
						}
					}
				}
			}
			logger.warn(
					"Have replaced {} long-term contracts out of {} that needed to be replaced",
					index, nrOfLtcsToBeReplaced);
		}
	}

}
