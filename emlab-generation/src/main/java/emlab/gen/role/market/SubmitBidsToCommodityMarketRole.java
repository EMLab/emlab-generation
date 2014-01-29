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

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import agentspring.role.Role;
import agentspring.role.RoleComponent;
import emlab.gen.domain.agent.EnergyProducer;
import emlab.gen.domain.market.Bid;
import emlab.gen.domain.market.CommodityMarket;
import emlab.gen.domain.technology.PowerPlant;
import emlab.gen.domain.technology.Substance;
import emlab.gen.domain.technology.SubstanceShareInFuelMix;
import emlab.gen.repository.Reps;
import emlab.gen.role.AbstractEnergyProducerRole;

/**
 * {@link EnergyProducer}s submit bids to the {@link CommodityMarket}. They buy
 * fuel needed to fuel their {@link PowerPlant}s. Interesting twist with
 * co-combustion and multiple fuels Pay attention that the fuel mix is set to
 * the current mix, that is no forecast is done between electricity and
 * commodity market clearing.
 * 
 * @author <a href="mailto:E.J.L.Chappin@tudelft.nl">Emile Chappin</a> @author
 *         <a href="mailto:A.Chmieliauskas@tudelft.nl">Alfredas
 *         Chmieliauskas</a>
 * 
 */
@RoleComponent
public class SubmitBidsToCommodityMarketRole extends AbstractEnergyProducerRole implements Role<EnergyProducer> {

    @Autowired
    Reps reps;

    @Override
    @Transactional
    public void act(EnergyProducer producer) {

        logger.info("Purchasing commodities");

        HashMap<Substance, Double> fuelAmounts = new HashMap<Substance, Double>();

        for (PowerPlant plant : reps.powerPlantRepository.findOperationalPowerPlantsByOwner(producer, getCurrentTick())) {

            double totalSupply = plant.calculateElectricityOutputAtTime(getCurrentTick(), false);

            for (SubstanceShareInFuelMix share : plant.getFuelMix()) {

                double amount = share.getShare() * totalSupply;
                Substance substance = share.getSubstance();

                // already in? Than add to total
                if (fuelAmounts.containsKey(substance)) {
                    amount += fuelAmounts.get(substance);
                }
                fuelAmounts.put(substance, amount);
            }
        }

        for (Substance substance : fuelAmounts.keySet()) {
            // find the totals and the right market. Place one bid for each
            // substance (fuel)
            if (!fuelAmounts.get(substance).isNaN() && fuelAmounts.get(substance) > 0) {
                Bid bid = reps.nonTransactionalCreateRepository.submitBidToMarket(
                        reps.marketRepository.findFirstMarketBySubstance(substance), producer, getCurrentTick(), false, Double.MAX_VALUE,
                        fuelAmounts.get(substance));
                logger.info("Submited bid " + bid);
            }
        }
    }

}
