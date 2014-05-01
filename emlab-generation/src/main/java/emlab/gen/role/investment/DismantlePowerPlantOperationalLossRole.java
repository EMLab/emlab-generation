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
import org.springframework.transaction.annotation.Transactional;

import agentspring.role.AbstractRole;
import agentspring.role.RoleComponent;
import emlab.gen.domain.contract.CashFlow;
import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.domain.technology.PowerPlant;
import emlab.gen.repository.Reps;

/**
 * @author pradyumnabhagwat
 * 
 */

// If supply is higher than demand, for each power plant check age.
// Sort by age/expectedLife in descending order and If this value is greater
// than one, plant is past technical life dismantle it and check supply margin.
// If supply margin is still positive, calculate income difference (income -
// fuel, carbon, O&M) over the past n years and sort
// start dismantling from highest negative value and updating supplyMargin
// variable, till all plants are dismantled or zero reached

@RoleComponent
public class DismantlePowerPlantOperationalLossRole extends AbstractRole<ElectricitySpotMarket> {

    @Autowired
    Reps reps;

    public Reps getReps() {
        return reps;
    }

    @Transactional
    public void act(ElectricitySpotMarket market) {
        if (getCurrentTick() > 0) {

            for (PowerPlant plant : reps.powerPlantRepository.findOperationalPowerPlantsInMarket(market,
                    getCurrentTick())) {

                double age = 0;
                long currentLiftime = 0;
                currentLiftime = getCurrentTick() - plant.getConstructionStartTime()
                        - plant.getTechnology().getExpectedLeadtime() - plant.getTechnology().getExpectedPermittime();

                plant.setActualLifetime(currentLiftime);

                age = (double) plant.getActualLifetime() / (((double) plant.getTechnology().getExpectedLifetime()));

                plant.setAgeFraction((double) age);

                if (plant.getAgeFraction() > 1.00D) {

                    double ModifiedOM = plant.getActualFixedOperatingCost()
                            * Math.pow((1 + (plant.getTechnology().getFixedOperatingCostModifierAfterLifetime())),
                                    ((double) plant.getActualLifetime() - (((double) plant.getTechnology()
                                            .getExpectedLifetime()))));

                    plant.setActualFixedOperatingCost(ModifiedOM);
                }
                long yearIterator = 0;
                double profitability = 0;
                double cost = 0;
                double revenue = 0;
                for (yearIterator = 1; yearIterator <= market.getLookback() && yearIterator > 0; yearIterator++) {

                    for (CashFlow cf : reps.cashFlowRepository.findAllCashFlowsForPowerPlantForTime(plant,
                            getCurrentTick() - yearIterator)) {

                        if (cf.getRegardingPowerPlant() != null) {

                            if (cf.getType() == 3 || cf.getType() == 4 || cf.getType() == 5 || cf.getType() == 6) {
                                cost = cost + cf.getMoney();
                            }

                            if (cf.getType() == 1 || cf.getType() == 10) {
                                revenue = revenue + cf.getMoney();
                            }
                        }
                    }
                    if (market.getLookback() > 0) {
                        if (getCurrentTick() < market.getBacklookingForDemandForecastinginDismantling()) {
                            profitability = (revenue - cost) / (double) (getCurrentTick());
                            plant.setProfitability(profitability);
                        }
                        if (getCurrentTick() >= market.getBacklookingForDemandForecastinginDismantling())
                            profitability = (revenue - cost) / (double) (market.getLookback());
                        plant.setProfitability(profitability);
                    } else {
                        profitability = (revenue - cost);
                        plant.setProfitability(profitability);
                    }
                }
                plant.persist();
                logger.warn("22 Plant prof " + plant.getProfitability());
            }

            for (PowerPlant plant : reps.powerPlantRepository
                    .findOperationalPowerPlantsByAscendingProfitabilityAndMarket(market, getCurrentTick())) {

                if (plant.getProfitability() < 0) {

                    plant.dismantlePowerPlant(getCurrentTick());
                    // logger.warn("22 Plant dismantled " + plant.getLabel());
                }
            }
        }
    }
}