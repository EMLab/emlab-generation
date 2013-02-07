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

import agentspring.role.Role;
import agentspring.role.RoleComponent;
import emlab.gen.domain.gis.Zone;
import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.domain.market.electricity.Segment;
import emlab.gen.domain.technology.PowerPlant;
import emlab.gen.repository.Reps;

/**
 * Creates and clears the {@link ElectricitySpotMarket} for two {@link Zone}s.
 * The market is divided into {@link Segment}s and cleared for each segment.
 * Also the emissions cap is adhered to and a global CO2 emissions market is
 * cleared.
 * 
 * @author <a href="mailto:E.J.L.Chappin@tudelft.nl">Emile Chappin</a>
 * 
 * @author <a href="mailto:A.Chmieliauskas@tudelft.nl">Alfredas
 *         Chmieliauskas</a>
 * 
 */
@RoleComponent
public class MarketVerificationRole extends AbstractMarketRole<ElectricitySpotMarket> implements Role<ElectricitySpotMarket> {

    @Autowired
    private Reps reps;


    public void act(ElectricitySpotMarket aRandomMarketNotToBeUsed) {

        logger.info("Validating the markets");

        // find all power plants and store the ones operational to a list.
        List<PowerPlant> powerPlants = new ArrayList<PowerPlant>();
        for (PowerPlant plant : reps.powerPlantRepository.findAll()) {
            if (plant.isOperational(getCurrentTick())) {
                powerPlants.add(plant);
            }
        }
    }

    @Override
    public Reps getReps() {
        return reps;
    }
    
}
