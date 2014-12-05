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
package emlab.gen.role.co2policy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Transient;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.transaction.annotation.Transactional;

import agentspring.role.AbstractRole;
import agentspring.role.RoleComponent;
import emlab.gen.domain.agent.Government;
import emlab.gen.domain.agent.TargetInvestor;
import emlab.gen.domain.market.CO2Auction;
import emlab.gen.domain.market.ClearingPoint;
import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.domain.policy.PowerGeneratingTechnologyTarget;
import emlab.gen.repository.Reps;
import emlab.gen.repository.StrategicReserveOperatorRepository;

/**
 * @author JCRichstein
 *
 */
@RoleComponent
public class RenewableAdaptiveCO2CapRole extends AbstractRole<Government> {

    @Transient
    @Autowired
    Reps reps;

    @Transient
    @Autowired
    StrategicReserveOperatorRepository strategicReserveOperatorRepository;

    @Autowired
    Neo4jTemplate template;

    @Transactional
    public void act(Government government) {

        // logger.warn("TimeSeries before: {}",
        // government.getCo2CapTrend().getTimeSeries());

        double co2Emissions = 0;

        CO2Auction co2Auction = template.findAll(CO2Auction.class).iterator().next();

        ClearingPoint lastClearingPointOfCo2Market = reps.clearingPointRepositoryOld.findClearingPointForMarketAndTime(
                co2Auction, getCurrentTick() - 1, false);
        if (lastClearingPointOfCo2Market != null) {
            co2Emissions = lastClearingPointOfCo2Market.getVolume();
        }

        double totalProduction = 0;

        for (ElectricitySpotMarket esm : reps.marketRepository.findAllElectricitySpotMarkets()) {
            for (ClearingPoint cp : reps.clearingPointRepository.findAllClearingPointsForMarketAndTimeRange(esm,
                    getCurrentTick() - 1, getCurrentTick() - 1, false)) {
                totalProduction += cp.getVolume();
            }
        }

        double averageEmissionsPerMWh = co2Emissions / totalProduction;

        double plannedProductionByRenewables = 0;
        double totalPlannedCapacity = 0;

        double totalProducedRenewableElectricity = 0;

        double totalActualInstalledCapacity = 0;
        for (TargetInvestor targetInvestor : template.findAll(TargetInvestor.class)) {
            for (PowerGeneratingTechnologyTarget target : targetInvestor.getPowerGenerationTechnologyTargets()) {
                double producedRenewableElectricityByTechnologyByTargetInvestor = reps.powerPlantDispatchPlanRepository
                        .calculateTotalProductionForEnergyProducerForTimeForTechnology(targetInvestor,
                                getCurrentTick() - 1, target.getPowerGeneratingTechnology(), false);
                totalProducedRenewableElectricity += producedRenewableElectricityByTechnologyByTargetInvestor;
                double installedCapacityByTechnology = reps.powerPlantRepository
                        .calculateCapacityOfExpectedOperationalPowerPlantsByOwnerByTechnology(getCurrentTick() - 1,
                                targetInvestor, target.getPowerGeneratingTechnology());
                totalActualInstalledCapacity += installedCapacityByTechnology;
                double plannedCapacityByTechnologyAndTargetInvestor = target.getTrend().getValue(getCurrentTick() - 1);
                totalPlannedCapacity += target.getTrend().getValue(getCurrentTick() - 1);
                double plannedProducedRenewableElectricityByTechnologyAndTargetInvestor = plannedCapacityByTechnologyAndTargetInvestor
                        / installedCapacityByTechnology * producedRenewableElectricityByTechnologyByTargetInvestor;
                plannedProductionByRenewables += plannedProducedRenewableElectricityByTechnologyAndTargetInvestor;
            }
        }

        double plannedSavedEmissionsApproximation = plannedProductionByRenewables * averageEmissionsPerMWh;
        double actualSavedEmissionsApproximation = totalProducedRenewableElectricity * averageEmissionsPerMWh;

        double capReduction = 0;

        if (!government.isDeviationFromResTargetAdjustment()) {
            capReduction = actualSavedEmissionsApproximation * government.getAdaptiveCapCO2SavingsWeighingFactor();
        } else {
            capReduction = Math.max(0, actualSavedEmissionsApproximation - plannedSavedEmissionsApproximation)
                    * government.getAdaptiveCapCO2SavingsWeighingFactor();
        }
        logger.warn("plannedSavedEmissionsApproximation: " + plannedSavedEmissionsApproximation
                + ", actualSavedEmissionsApproximation: " + actualSavedEmissionsApproximation + ", Cap reduction: "
                + capReduction);
        logger.warn("plannedCapacity: " + totalPlannedCapacity + ", actualCapacity: "
                + totalActualInstalledCapacity);
        government.getCo2CapTrend().setValue(getCurrentTick(),
                government.getCo2CapTrend().getValue(getCurrentTick()) - capReduction);
        // logger.warn("TimeSeries after: {}",
        // government.getCo2CapTrend().getTimeSeries());
    }
}
