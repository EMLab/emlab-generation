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

import agentspring.role.RoleComponent;
import emlab.gen.domain.agent.DecarbonizationModel;
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


    @Transactional
    public void act(DecarbonizationModel model) {

        logger.warn("Querying power plants.");

        for (PowerPlant plant : reps.powerPlantRepository.findAll()) {

            FinancialPowerPlantReport financialPowerPlantReport = new FinancialPowerPlantReport();
            financialPowerPlantReport.setTime(getCurrentTick());
            financialPowerPlantReport.setFullLoadHours(0);
            financialPowerPlantReport.setPowerPlant(plant);
            financialPowerPlantReport.setCommodityCosts(0);
            financialPowerPlantReport.persist();


            // Determining variable and CO2 costs in current time step.
            double totalSupply = plant.calculateElectricityOutputAtTime(getCurrentTick(), false);
            financialPowerPlantReport.setProduction(totalSupply);

            for (SubstanceShareInFuelMix share : plant.getFuelMix()) {

                double amount = share.getShare() * totalSupply;
                Substance substance = share.getSubstance();
                double substanceCost = findLastKnownPriceForSubstance(substance) * amount;
                financialPowerPlantReport.setCommodityCosts(financialPowerPlantReport.getCommodityCosts()
                        + substanceCost);


            }
            financialPowerPlantReport.setCo2Costs(reps.powerPlantRepository.calculateCO2CostsOfPowerPlant(plant,
                    getCurrentTick()));
            financialPowerPlantReport.setVariableCosts(financialPowerPlantReport.getCommodityCosts()+financialPowerPlantReport.getCo2Costs());

            //Determine fixed costs
            financialPowerPlantReport.setFixedCosts(reps.powerPlantRepository
                    .calculateFixedCostsOfPowerPlant(plant, getCurrentTick()));

            //Calculate overall revenue
            financialPowerPlantReport.setSpotMarketRevenue(reps.powerPlantRepository
                    .calculateSpotMarketRevenueOfPowerPlant(plant, getCurrentTick()));

            financialPowerPlantReport.setStrategicReserveRevenue(reps.powerPlantRepository
                    .calculateStrategicReserveRevenueOfPowerPlant(plant, getCurrentTick()));

            financialPowerPlantReport.setCapacityMarketRevenue(reps.powerPlantRepository
                    .calculateCapacityMarketRevenueOfPowerPlant(plant, getCurrentTick()));

            financialPowerPlantReport.setCo2HedgingRevenue(reps.powerPlantRepository
                    .calculateCO2HedgingRevenueOfPowerPlant(plant, getCurrentTick()));


            financialPowerPlantReport.setOverallRevenue(financialPowerPlantReport.getCapacityMarketRevenue() + financialPowerPlantReport.getCo2HedgingRevenue() + financialPowerPlantReport.getSpotMarketRevenue() + financialPowerPlantReport
                    .getStrategicReserveRevenue());

            // Calculate Full load hours
            financialPowerPlantReport.setFullLoadHours(reps.powerPlantRepository.calculateFullLoadHoursOfPowerPlant(
                    plant, getCurrentTick()));



        }


    }

}
