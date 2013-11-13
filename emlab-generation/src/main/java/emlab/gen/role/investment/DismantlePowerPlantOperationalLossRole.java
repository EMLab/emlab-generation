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
        if (getCurrentTick() > market.getLookback()) {

            double availableFutureCapacity = 0;

            // for (ElectricitySpotMarket market :
            // reps.marketRepository.findAllElectricitySpotMarketsAsList()) {

            // Forecast Peak demand and supply for the next year store supply
            // margin in variable.

            double peakLoadforMarketNOtrend = reps.segmentLoadRepository.peakLoadbyZoneMarketandTime(market.getZone(),
                    market);

            double trend = market.getDemandGrowthTrend().getValue(getCurrentTick());

            double peakLoadforMarket = trend * peakLoadforMarketNOtrend;

            for (PowerPlant plant : reps.powerPlantRepository.findOperationalPowerPlantsInMarket(market,
                    getCurrentTick())) {

                // Calculate AgeFraction and set it for each plant.
                double age = 0;
                double currentLiftime = 0;
                currentLiftime = ((double) plant.getActualLifetime()
                        + (double) plant.getTechnology().getExpectedLeadtime() + (double) plant.getTechnology()
                        .getExpectedPermittime());

                age = currentLiftime / ((double) (plant.getTechnology().getExpectedLifetime()));

                plant.setAgeFraction(age);

                // Calculate profitability for past n years.
                long yearIterator = 0;
                double profitability = 0;
                double cost = 0;
                double revenue = 0;
                for (yearIterator = 0; yearIterator <= market.getLookback(); yearIterator++) {

                    for (CashFlow cf : reps.cashFlowRepository.findAllCashFlowsForForTime(getCurrentTick()
                            - yearIterator)) {

                        if (cf.getRegardingPowerPlant().equals(plant)) {

                            if (cf.getType() == 3 || cf.getType() == 4 || cf.getType() == 5 || cf.getType() == 6) {
                                cost = cost + cf.getMoney();
                                // logger.warn("enters loop " + cost);
                            }

                            if (cf.getType() == 1 || cf.getType() == 10) {
                                revenue = revenue + cf.getMoney();

                            }

                        }

                    }
                    if (market.getLookback() > 0) {
                        profitability = (revenue - cost) / (double) (market.getLookback());

                    } else {
                        profitability = (revenue - cost);
                        plant.setProfitability(profitability);
                    }
                }

            }

            // Calculate availableFutureCapacity for the next year at peak
            availableFutureCapacity = 0d;
            for (PowerPlant futurePlant : reps.powerPlantRepository
                    .findExpectedOperationalPowerPlantsInMarketWithoutDismantling(market, getCurrentTick() + 1)) {

                double plantCapacity = (double) (futurePlant.getTechnology().getCapacity())
                        * (double) (futurePlant.getTechnology().getPeakSegmentDependentAvailability());

                availableFutureCapacity = availableFutureCapacity + plantCapacity;

            }

            // Sort in descending order by age fraction and dismantle by age &
            // peak availability

            for (PowerPlant plant : reps.powerPlantRepository.findOperationalPowerPlantsByDescendingAgeFactorAndMarket(
                    market, getCurrentTick())) {

                if (plant.getAgeFraction() >= 1.00 && (availableFutureCapacity - peakLoadforMarket) > 0D) {

                    plant.dismantlePowerPlant(getCurrentTick());

                    availableFutureCapacity = availableFutureCapacity
                            - ((plant.getTechnology().getCapacity()) * plant.getTechnology()
                                    .getPeakSegmentDependentAvailability());
                }

                // Dismantle by profitability until last plant is profitable or
                // capacity margin goes to zero.

            }

            if ((availableFutureCapacity - peakLoadforMarket) > 1000D) {

                for (PowerPlant plant : reps.powerPlantRepository
                        .findOperationalPowerPlantsByAscendingProfitabilityAndMarket(market, getCurrentTick())) {

                    if (plant.getProfitability() <= 0D && (availableFutureCapacity - peakLoadforMarket) > 0D) {

                        plant.dismantlePowerPlant(getCurrentTick());
                        availableFutureCapacity = availableFutureCapacity
                                - ((plant.getTechnology().getCapacity()) * plant.getTechnology()
                                        .getPeakSegmentDependentAvailability());

                    }
                }

            }

            // }
        }
    }
}