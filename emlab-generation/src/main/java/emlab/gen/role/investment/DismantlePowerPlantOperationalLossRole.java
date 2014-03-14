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

import org.apache.commons.math.stat.regression.SimpleRegression;
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

// TODO: If shortage in market, check for capacity in interconnected market. If
// capacity is present, check interconnector capacity and add the capacity in
// peak supply.

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

            double availableFutureCapacity = 0d;
            double peakLoadforMarket = 0d;

            // Demand

            if (getCurrentTick() == 1) {
                peakLoadforMarket = reps.segmentLoadRepository.peakLoadbyZoneMarketandTime(market.getZone(), market);
            }

            if (getCurrentTick() > 1) {
                SimpleRegression sr = new SimpleRegression();
                for (long time = getCurrentTick() - 1; time >= getCurrentTick()
                        - market.getBacklookingForDemandForecastinginDismantling()
                        && time >= 0; time = time - 1) {
                    sr.addData(time, market.getDemandGrowthTrend().getValue(time));

                }
                double peakLoadforMarketNOtrend = reps.segmentLoadRepository.peakLoadbyZoneMarketandTime(
                        market.getZone(), market);

                peakLoadforMarket = sr.predict(getCurrentTick()) * peakLoadforMarketNOtrend;

            }
            logger.warn("1 peakLoad " + peakLoadforMarket);

            availableFutureCapacity = reps.powerPlantRepository.calculatePeakCapacityOfOperationalPowerPlantsInMarket(
                    market, getCurrentTick());

            logger.warn("2 availableFutureCapacity " + availableFutureCapacity);
            // logger.warn("capacity" + (availableFutureCapacity -
            // peakLoadforMarket) + "Future" + availableFutureCapacity);

            // Check interconnector capacity in other market

            double interconnectorCapacity = reps.interconnectorRepository.findInterconnectorCapacity();
            double peakCapacityotherMarket = 0;
            double peakLoadotherMarket = 0;
            double capacityMarginOtherMarket = 0;

            if (interconnectorCapacity > 0) {
                logger.warn("3 Enters interconnection exists  " + interconnectorCapacity);
                if (getCurrentTick() > 3) {
                    for (ElectricitySpotMarket otherMarket : reps.marketRepository.findAllElectricitySpotMarkets()) {
                        logger.warn("4 Enters interconnector loop  " + otherMarket.getNodeId());
                        if (otherMarket.getNodeId().intValue() != market.getNodeId().intValue()) {
                            logger.warn("5 finds the other market " + otherMarket.getNodeId() + " My Market "
                                    + market.getNodeId());
                            SimpleRegression sr2 = new SimpleRegression();
                            for (long time = getCurrentTick() - 1; time >= getCurrentTick()
                                    - market.getBacklookingForDemandForecastinginDismantling()
                                    && time >= 0; time = time - 1) {
                                sr2.addData(time, reps.powerPlantRepository
                                        .calculatePeakCapacityOfOperationalPowerPlantsInMarket(otherMarket, time));
                            }

                            peakCapacityotherMarket = sr2.predict(getCurrentTick());

                            logger.warn("6 peakCapacity in other market" + peakCapacityotherMarket);

                            logger.warn("7 Enters Load loop" + getCurrentTick());
                            SimpleRegression sr1 = new SimpleRegression();
                            for (long time = getCurrentTick() - 1; time >= getCurrentTick()
                                    - market.getBacklookingForDemandForecastinginDismantling()
                                    && time >= 0; time = time - 1) {
                                sr1.addData(time, otherMarket.getDemandGrowthTrend().getValue(time));
                            }

                            double peakNoTrendsOtherMarket = reps.segmentLoadRepository.peakLoadbyZoneMarketandTime(
                                    otherMarket.getZone(), otherMarket);

                            peakLoadotherMarket = sr1.predict(getCurrentTick()) * peakNoTrendsOtherMarket;
                            logger.warn("8 peakLoad" + peakLoadotherMarket);

                            capacityMarginOtherMarket = peakCapacityotherMarket - peakLoadotherMarket;
                            logger.warn("9 Capacity Margin " + capacityMarginOtherMarket);
                        }
                    }

                    if (capacityMarginOtherMarket >= 0 && capacityMarginOtherMarket <= interconnectorCapacity) {
                        logger.warn("10 Capacity Margin is higher " + capacityMarginOtherMarket);
                        availableFutureCapacity = availableFutureCapacity + capacityMarginOtherMarket;
                        logger.warn("11 Higher Available Capacity " + availableFutureCapacity);
                    }

                    else {
                        availableFutureCapacity = availableFutureCapacity + interconnectorCapacity;
                        logger.warn("12 interconnector added " + availableFutureCapacity);
                    }

                    if (capacityMarginOtherMarket < 0 && ((-1) * capacityMarginOtherMarket) <= interconnectorCapacity) {
                        logger.warn("13 Capacity Margin is lower " + capacityMarginOtherMarket);
                        peakLoadforMarket = peakLoadforMarket - capacityMarginOtherMarket;
                        logger.warn("14 Higher Available Load " + peakLoadforMarket);
                    } else {
                        peakLoadforMarket = peakLoadforMarket + interconnectorCapacity;
                    }
                }
            }

            if ((availableFutureCapacity - peakLoadforMarket) > 0D) {
                for (PowerPlant plant : reps.powerPlantRepository.findOperationalPowerPlantsInMarket(market,
                        getCurrentTick())) {
                    // logger.warn("enters loop Powerplant " +
                    // plant.getLabel());
                    // Calculate AgeFraction and set it for each plant.
                    double age = 0;
                    long currentLiftime = 0;
                    currentLiftime = getCurrentTick() - plant.getConstructionStartTime()
                            - plant.getTechnology().getExpectedLeadtime()
                            - plant.getTechnology().getExpectedPermittime();
                    // .getExpectedPermittime();
                    plant.setActualLifetime(currentLiftime);
                    // logger.warn("ActualLifeTime" +
                    // plant.getActualLifetime());
                    // currentLiftime = ((double) plant.getActualLifetime()
                    // + (double) plant.getTechnology().getExpectedLeadtime() +
                    // (double) plant.getTechnology()
                    // .getExpectedPermittime());

                    age = (double) plant.getActualLifetime() / (((double) plant.getTechnology().getExpectedLifetime()));

                    plant.setAgeFraction((double) age);
                    // logger.warn("enters loop Powerplant " +
                    // plant.getAgeFraction());
                    // Calculate profitability for past n years.
                    long yearIterator = 0;
                    double profitability = 0;
                    double cost = 0;
                    double revenue = 0;
                    for (yearIterator = 1; yearIterator <= market.getLookback() && yearIterator > 0; yearIterator++) {

                        for (CashFlow cf : reps.cashFlowRepository.findAllCashFlowsForPowerPlantForTime(plant,
                                getCurrentTick() - yearIterator)) {
                            // logger.warn("enters for loop revenue" +
                            // cf.getRegardingPowerPlant().getNodeId() + " "
                            // + plant.getNodeId());
                            if (cf.getRegardingPowerPlant() != null) {

                                // logger.warn("enters power plant loop" +
                                // plant.getLabel());
                                if (cf.getType() == 3 || cf.getType() == 4 || cf.getType() == 5 || cf.getType() == 6) {
                                    cost = cost + cf.getMoney();
                                    // logger.warn("enters loop cost" +
                                    // cf.getType());
                                }
                                // logger.warn("enters loop revenue" +
                                // cf.getType());
                                if (cf.getType() == 1 || cf.getType() == 10) {
                                    revenue = revenue + cf.getMoney();

                                }
                            }
                        }
                        if (market.getLookback() > 0) {
                            profitability = (revenue - cost) / (double) (market.getLookback());
                            plant.setProfitability(profitability);
                            // logger.warn("enters loop profitability calculation"
                            // + plant.getProfitability());
                        } else {
                            profitability = (revenue - cost);
                            plant.setProfitability(profitability);
                            // logger.warn("enters loop profitability calculation"
                            // + plant.getProfitability());
                        }
                    }
                    plant.persist();
                }

                // Sort in descending order by age fraction and dismantle by age
                // &
                // peak availability
                if ((availableFutureCapacity - peakLoadforMarket) > 0D) {
                    for (PowerPlant plant : reps.powerPlantRepository
                            .findOperationalPowerPlantsByDescendingAgeFactorAndMarket(market, getCurrentTick())) {

                        double dismantledPlantCapacity = ((plant.getTechnology().getCapacity()) * plant.getTechnology()
                                .getPeakSegmentDependentAvailability());

                        if ((availableFutureCapacity - dismantledPlantCapacity) > peakLoadforMarket) {

                            if (plant.getAgeFraction() >= 1.00D) {

                                // logger.warn("enters loop age dismantle" +
                                // plant.getAgeFraction());

                                plant.dismantlePowerPlant(getCurrentTick());

                                availableFutureCapacity = (availableFutureCapacity - dismantledPlantCapacity);
                            }
                        }

                        // if (((availableFutureCapacity -
                        // dismantledPlantCapacity) - peakLoadforMarket) <= 0)
                        // break;
                    }
                }
                // logger.warn("capacity" + (availableFutureCapacity -
                // peakLoadforMarket));

                // Dismantle by profitability until last plant is
                // profitable
                // or
                // capacity margin goes to zero.

                if ((availableFutureCapacity - peakLoadforMarket) > 0D) {

                    for (PowerPlant plant : reps.powerPlantRepository
                            .findOperationalPowerPlantsByAscendingProfitabilityAndMarket(market, getCurrentTick())) {

                        double dismantledPlantCapacity = ((plant.getTechnology().getCapacity()) * plant.getTechnology()
                                .getPeakSegmentDependentAvailability());
                        // logger.warn("Shortage "
                        // + ((availableFutureCapacity -
                        // dismantledPlantCapacity) - peakLoadforMarket));

                        if ((availableFutureCapacity - dismantledPlantCapacity) > (peakLoadforMarket)) {
                            if (plant.getProfitability() < 0) {

                                // logger.warn("enters loop money dismantle" +
                                // plant.getProfitability());

                                plant.dismantlePowerPlant(getCurrentTick());

                                availableFutureCapacity = (availableFutureCapacity - dismantledPlantCapacity);
                            }
                        }

                        // if (((availableFutureCapacity -
                        // dismantledPlantCapacity) - peakLoadforMarket) <= 0)
                        // break;
                    }
                }
            }
        }
    }
}