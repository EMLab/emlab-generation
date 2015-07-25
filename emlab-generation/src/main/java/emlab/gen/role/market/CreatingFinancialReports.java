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
package emlab.gen.role.market;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import agentspring.role.RoleComponent;
import emlab.gen.domain.agent.DecarbonizationModel;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import emlab.gen.domain.market.electricity.FinancialPowerPlantReport;
import emlab.gen.domain.technology.PowerPlant;
import emlab.gen.domain.technology.Substance;
import emlab.gen.domain.technology.SubstanceShareInFuelMix;
import emlab.gen.repository.Reps;

/**
 * Creating finanical reports for each power plant. Currently implemented for
 * use with spot markets only. Long-term contracts are ignored since costs can
 * not easily be assigned to individual power plants.
 *
 * @author Joern
 *
 */
@RoleComponent
public class CreatingFinancialReports extends AbstractClearElectricitySpotMarketRole<DecarbonizationModel> {

    @Autowired
    private Reps reps;

    @Autowired
    Neo4jTemplate template;

    @Transactional
    public void act(DecarbonizationModel model) {

        Map<Substance, Double> fuelPriceMap = new HashMap<Substance, Double>();
        for (Substance substance : template.findAll(Substance.class)) {
            fuelPriceMap.put(substance, findLastKnownPriceForSubstance(substance));
        }
	logger.warn(fuelPriceMap.toString());

        createFinancialReportsForPowerPlantsAndTick(
                reps.powerPlantRepository.findAllPowerPlantsWhichAreNotDismantledBeforeTick(getCurrentTick() - 2),
                getCurrentTick());

    }

    public void createFinancialReportsForNewInvestments(DecarbonizationModel model) {
        createFinancialReportsForPowerPlantsAndTick(
                reps.powerPlantRepository.findAllPowerPlantsWithConstructionStartTimeInTick(getCurrentTick()),
                getCurrentTick());
    }

    void createFinancialReportsForPowerPlantsAndTick(Iterable<PowerPlant> plants, long tick) {

        for (PowerPlant plant : plants) {

            FinancialPowerPlantReport financialPowerPlantReport = new FinancialPowerPlantReport();
            financialPowerPlantReport.setTime(tick);
            financialPowerPlantReport.setFullLoadHours(0);
            financialPowerPlantReport.setPowerPlant(plant);
            financialPowerPlantReport.setCommodityCosts(0);
            financialPowerPlantReport.persist();


            // Determining variable and CO2 costs in current time step.
            double totalSupply = plant.calculateElectricityOutputAtTime(tick, false);
            financialPowerPlantReport.setProduction(totalSupply);

            for (SubstanceShareInFuelMix share : plant.getFuelMix()) {

                double amount = share.getShare() * totalSupply;
                Substance substance = share.getSubstance();
                double substanceCost = findLastKnownPriceForSubstance(substance) * amount;
                financialPowerPlantReport.setCommodityCosts(financialPowerPlantReport.getCommodityCosts()
                        + substanceCost);


            }
            financialPowerPlantReport.setCo2Costs(reps.powerPlantRepository.calculateCO2CostsOfPowerPlant(plant,
                    tick));
            financialPowerPlantReport.setVariableCosts(financialPowerPlantReport.getCommodityCosts()+financialPowerPlantReport.getCo2Costs());

            //Determine fixed costs
            financialPowerPlantReport.setFixedCosts(reps.powerPlantRepository
                    .calculateFixedCostsOfPowerPlant(plant,
                            tick));

            //Calculate overall revenue
            financialPowerPlantReport.setSpotMarketRevenue(reps.powerPlantRepository
                    .calculateSpotMarketRevenueOfPowerPlant(plant, tick));

            financialPowerPlantReport.setStrategicReserveRevenue(reps.powerPlantRepository
                    .calculateStrategicReserveRevenueOfPowerPlant(plant, tick));

            financialPowerPlantReport.setCapacityMarketRevenue(reps.powerPlantRepository
                    .calculateCapacityMarketRevenueOfPowerPlant(plant, tick));

            financialPowerPlantReport.setCo2HedgingRevenue(reps.powerPlantRepository
                    .calculateCO2HedgingRevenueOfPowerPlant(plant, tick));


            financialPowerPlantReport.setOverallRevenue(financialPowerPlantReport.getCapacityMarketRevenue() + financialPowerPlantReport.getCo2HedgingRevenue() + financialPowerPlantReport.getSpotMarketRevenue() + financialPowerPlantReport
                    .getStrategicReserveRevenue());

            // Calculate Full load hours
            financialPowerPlantReport.setFullLoadHours(reps.powerPlantRepository.calculateFullLoadHoursOfPowerPlant(
                    plant, tick));

            int operationalStatus;
            if (plant.isOperational(tick))
                operationalStatus = 1;
            else if (plant.isInPipeline(tick))
                operationalStatus = 0;
            else
                operationalStatus = 2;

            financialPowerPlantReport.setPowerPlantStatus(operationalStatus);



        }

    }

}
