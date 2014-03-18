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
package emlab.gen.role.operating;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.transaction.annotation.Transactional;

import agentspring.role.Role;
import agentspring.role.RoleComponent;
import emlab.gen.domain.agent.EnergyProducer;
import emlab.gen.domain.agent.Government;
import emlab.gen.domain.market.CO2Auction;
import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.domain.technology.PowerPlant;
import emlab.gen.domain.technology.Substance;
import emlab.gen.domain.technology.SubstanceShareInFuelMix;
import emlab.gen.repository.Reps;
import emlab.gen.role.AbstractEnergyProducerRole;

/**
 * Run the business. Buy supplies, pay interest, account profits
 * 
 * @author <a href="mailto:E.J.L.Chappin@tudelft.nl">Emile Chappin</a> @author <a href="mailto:A.Chmieliauskas@tudelft.nl">Alfredas Chmieliauskas</a>
 *@author JCRichstein
 *
 */
@RoleComponent
public class DetermineFuelMixRole extends AbstractEnergyProducerRole implements Role<EnergyProducer> {

    @Autowired
    Reps reps;

    @Autowired
    Neo4jTemplate template;

    public Reps getReps() {
        return reps;
    }

    @Override
    @Transactional
    public void act(EnergyProducer producer) {

        // logger.info("Determining operation mode of power plants");
        //
        // int ops = 0;
        // for (@SuppressWarnings("unused")
        // PowerPlant pp :
        // reps.powerPlantRepository.findOperationalPowerPlantsByOwner(producer,
        // getCurrentTick())) {
        // ops++;
        // }
        // logger.info("number of operational pps: {}", ops);

        // get the co2 tax and market prices
        // CO2Auction market = reps.genericRepository.findFirst(CO2Auction.class);
        // double co2AuctionPrice = findLastKnownPriceOnMarket(market);
        HashMap<ElectricitySpotMarket, Double> expectedCO2Prices = determineExpectedCO2PriceInclTax(getCurrentTick()-1, 1, 1);
        Government government = reps.genericRepository.findFirst(Government.class);
        // double co2TaxLevel = government.getCO2Tax(getCurrentTick());
        // logger.warn("Expected CO2 price: " + expectedCO2Prices.toString());

        for (PowerPlant plant : reps.powerPlantRepository.findOperationalPowerPlantsWithFuelsGreaterZeroByOwner(producer, getCurrentTick())) {
            logger.info("Found operational power plant {} ", plant.getTechnology());

            // Fuels
            Set<Substance> possibleFuels = plant.getTechnology().getFuels();
            Map<Substance, Double> substancePriceMap = new HashMap<Substance, Double>();

            for (Substance substance : possibleFuels) {
                substancePriceMap.put(substance, findLastKnownPriceForSubstance(substance, getCurrentTick()));
            }
            Set<SubstanceShareInFuelMix> fuelMix = calculateFuelMix(plant, substancePriceMap,
                    expectedCO2Prices.get(reps.marketRepository.findElectricitySpotMarketByPowerPlant(plant)));
            plant.setFuelMix(fuelMix);

        }
    }

    @Transactional
    public void determineFuelMixForecastForYearAndFuelPriceMap(long clearingTick,
            Map<Substance, Double> substancePriceMap, Map<ElectricitySpotMarket, Double> nationalMinCo2Prices) {

        CO2Auction co2Auction = template.findAll(CO2Auction.class).iterator().next();
        double lastCO2Price;
        try {
            lastCO2Price = reps.clearingPointRepositoryOld.findClearingPointForMarketAndTime(co2Auction,
                    getCurrentTick() - 1, false).getPrice();
        } catch (NullPointerException e) {
            lastCO2Price = 0;
        }

        Government government = reps.genericRepository.findFirst(Government.class);
        // double co2TaxLevel = government.getCO2Tax(getCurrentTick());

        for (ElectricitySpotMarket market : reps.marketRepository.findAllElectricitySpotMarkets()) {
            for (PowerPlant plant : reps.powerPlantRepository.findExpectedOperationalPowerPlantsInMarket(market,
                    clearingTick)) {
                logger.info("Found operational power plant {} ", plant.getTechnology());

                double effectiveCO2Price;

                if (nationalMinCo2Prices.get(market) > lastCO2Price)
                    effectiveCO2Price = nationalMinCo2Prices.get(market);
                else
                    effectiveCO2Price = lastCO2Price;

                effectiveCO2Price += government.getCO2Tax(clearingTick);
                // Fuels
                Set<Substance> possibleFuels = plant.getTechnology().getFuels();
                Map<Substance, Double> substancePriceMap1 = new HashMap<Substance, Double>();

                for (Substance substance : possibleFuels) {
                    substancePriceMap1.put(substance, findLastKnownPriceForSubstance(substance, getCurrentTick()));
                }
                Set<SubstanceShareInFuelMix> fuelMix = calculateFuelMix(plant, substancePriceMap1,
                        effectiveCO2Price);
                plant.setFuelMix(fuelMix);

            }
        }

    }

}
