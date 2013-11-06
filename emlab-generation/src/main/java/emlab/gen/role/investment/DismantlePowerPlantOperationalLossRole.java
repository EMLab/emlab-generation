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

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Transient;

import agentspring.role.Role;
import agentspring.role.RoleComponent;
import emlab.gen.domain.agent.EnergyProducer;
import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.domain.technology.PowerPlant;
import emlab.gen.repository.Reps;
import emlab.gen.role.AbstractEnergyProducerRole;

/**
 * {@link EnergyProducer}s dismantle {@link PowerPlant}s that are out of merit
 * 
 * @author <a href="mailto:E.J.L.Chappin@tudelft.nl">Emile Chappin</a> @author
 *         <a href="mailto:A.Chmieliauskas@tudelft.nl">Alfredas
 *         Chmieliauskas</a>
 * 
 */

// Forecast Peak demand and supply for the next year store supply margin in
// variable.
// If supply is higher than demand, for each power plant check age.
// Sort by age/expectedLife in descending order and If this value is greater
// than one, plant is past technical life dismantle it and check supply margin.
// If supply margin is still positive, calculate income difference (income -
// fuel, carbon, O&M) over the past n years and sort
// start dismantling from highest negative value and updating supplyMargin
// variable, till all plants are dismantled or zero reached

@RoleComponent
public class DismantlePowerPlantOperationalLossRole extends AbstractEnergyProducerRole implements Role<EnergyProducer> {

    @Autowired
    Reps reps;

    @Transient
    Map<PowerPlant, Double> ageInfoMap = new HashMap<PowerPlant, Double>();

    public Reps getReps() {
        return reps;
    }

    public void act(EnergyProducer producer) {

        double reserveMargin;

        for (ElectricitySpotMarket market : reps.marketRepository.findAllElectricitySpotMarketsAsList()) {

            double peakLoadforMarketNOtrend = reps.segmentLoadRepository.peakLoadbyZoneMarketandTime(zone, market);

            double trend = market.getDemandGrowthTrend().getValue(getCurrentTick());

            double peakLoadforMarket = trend * peakLoadforMarketNOtrend;

            for (PowerPlant plant : reps.powerPlantRepository.findOperationalPowerPlantsInMarket(market,
                    getCurrentTick())) {

                double age = plant.getActualLifetime() / plant.getExpectedEndOfLife();

                if (age <= 0) {
                    ageInfoMap.put(plant, age);
                }
            }

            MyComparator comp = new MyComparator(ageInfoMap);

            Map<PowerPlant, Double> sortedAge = new TreeMap(comp);
            sortedAge.putAll(ageInfoMap);

        }
    }

    class MyComparator implements Comparator {

        Map map;

        public MyComparator(Map map) {
            this.map = map;
        }

        public int compare(Object o1, Object o2) {

            return ((Double) map.get(o2)).compareTo((Double) map.get(o1));

        }
    }
}
// public void act(EnergyProducer producer) {

// logger.info("Dismantling plants if out of merit");

// dis-mantle plants when passed technical lifetime.
// for (PowerPlant plant :
// reps.powerPlantRepository.findOperationalPowerPlantsByOwner(producer,
// getCurrentTick())) {
// long horizon = producer.getPastTimeHorizon();

// double requiredProfit = producer.getDismantlingRequiredOperatingProfit();
// if (calculateAveragePastOperatingProfit(plant, horizon) < requiredProfit) {
// logger.info("Dismantling power plant because it has had an operating loss (incl O&M cost) on average in the last "
// + horizon + " years: " + plant);

// plant.dismantlePowerPlant(getCurrentTick());
//
// }
// }
// }

// }
