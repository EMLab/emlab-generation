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
package emlab.gen.role.capacitymarket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.transaction.annotation.Transactional;

import agentspring.role.AbstractRole;
import agentspring.role.Role;
import agentspring.role.RoleComponent;
import emlab.gen.domain.agent.DecarbonizationModel;
import emlab.gen.domain.gis.Zone;
import emlab.gen.domain.market.capacity.CapacityMarket;
import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.domain.technology.Interconnector;
import emlab.gen.repository.Reps;

/**
 * @author Kaveri
 * 
 */

@RoleComponent
public class ExportLimiterRole extends AbstractRole<DecarbonizationModel> implements Role<DecarbonizationModel> {

    @Autowired
    Reps reps;

    @Autowired
    Neo4jTemplate template;

    @Override
    @Transactional
    public void act(DecarbonizationModel model) {

        double initialInterconnectorCapacity = 0d;

        // find all interconnectors
        Interconnector interconnector = template.findAll(Interconnector.class).iterator().next();

        // get initial interconnector capacity at tick 1 as capacity market is
        // functional only from tick 1
        if (getCurrentTick() == 1) {

            initialInterconnectorCapacity = interconnector.getCapacity(getCurrentTick());
            logger.warn("at tick 1 the interconnector capacity is" + initialInterconnectorCapacity);

        }

        // initialize interconnector capacity, irrespective of current tick, to
        // initialInterconnectorCapacity
        logger.warn("print capacity temp variable initialICcapacity" + initialInterconnectorCapacity);
        logger.warn("interconnector capacity before setting " + interconnector.getCapacity(getCurrentTick()));
        interconnector.setCapacity(initialInterconnectorCapacity);
        logger.warn("interconnector capacity after setting " + interconnector.getCapacity(getCurrentTick()));
        // loop through capacity markets and if supply < demand in any of the
        // capacity market regions, set interconnector capacity = 0
        for (CapacityMarket market : reps.capacityMarketRepository.findAll()) {
            Zone zone = market.getZone();
            logger.warn("zone" + zone);
            ElectricitySpotMarket emarket = reps.marketRepository.findElectricitySpotMarketForZone(zone);

            // double supply =
            // reps.marketRepository.findTotalSupplyInElectricitySpotMarketForZone(zone);
            double supply = reps.powerPlantRepository.calculatePeakCapacityOfOperationalPowerPlantsInMarket(emarket,
                    getCurrentTick());
            logger.warn("Supply" + supply);
            // double peakDemand =
            // reps.marketRepository.findPeakDemandInElectricitySpotMarketForZone(zone);
            double peakLoadforMarketNOtrend = reps.segmentLoadRepository.peakLoadbyZoneMarketandTime(zone, emarket);
            double demandfactor = emarket.getDemandGrowthTrend().getValue(getCurrentTick());

            double peakDemand = peakLoadforMarketNOtrend * demandfactor;
            logger.warn("demand" + peakDemand);

            if ((supply - peakDemand) < 0) {

                interconnector.setCapacity(0);
                logger.warn("interconnector capacity set to zero" + interconnector.getCapacity(getCurrentTick()));
            }

        }

    }
}
