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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;

import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.domain.market.electricity.Segment;
import emlab.gen.domain.market.electricity.SegmentLoad;
import emlab.gen.domain.technology.PowerPlant;
import emlab.gen.domain.technology.Substance;
import emlab.gen.domain.technology.SubstanceShareInFuelMix;
import emlab.gen.repository.Reps;
import emlab.gen.util.MapValueComparator;

public class SingleElectricityMarketInformation {

    @Autowired
    Reps reps;

    Map<Segment, Double> expectedElectricityPricesPerSegment;
    double maxExpectedLoad = 0d;
    Map<PowerPlant, Double> meritOrder;
    double capacitySum;

    SingleElectricityMarketInformation(ElectricitySpotMarket market, Map<Substance, Double> fuelPrices, double co2price, long time) {
        // determine expected power prices
        expectedElectricityPricesPerSegment = new HashMap<Segment, Double>();
        Map<PowerPlant, Double> marginalCostMap = new HashMap<PowerPlant, Double>();
        capacitySum = 0d;

        // get merit order for this market
        for (PowerPlant plant : reps.powerPlantRepository.findExpectedOperationalPowerPlantsInMarket(market, time)) {

            double plantMarginalCost = determineExpectedMarginalCost(plant, fuelPrices, co2price);
            marginalCostMap.put(plant, plantMarginalCost);
			capacitySum += plant.getActualNominalCapacity();
        }

        MapValueComparator comp = new MapValueComparator(marginalCostMap);
        meritOrder = new TreeMap<PowerPlant, Double>(comp);
        meritOrder.putAll(marginalCostMap);

        long numberOfSegments = reps.segmentRepository.count();

        double demandFactor = market.getDemandGrowthTrend().getValue(time);

        // find expected prices per segment given merit order
        for (SegmentLoad segmentLoad : market.getLoadDurationCurve()) {

            double expectedSegmentLoad = segmentLoad.getBaseLoad() * demandFactor;

            if (expectedSegmentLoad > maxExpectedLoad) {
                maxExpectedLoad = expectedSegmentLoad;
            }

            double segmentSupply = 0d;
            double segmentPrice = 0d;

            for (Entry<PowerPlant, Double> plantCost : meritOrder.entrySet()) {
                PowerPlant plant = plantCost.getKey();
                double plantCapacity = 0d;
                // Determine available capacity in the future in this
                // segment
                plantCapacity = plant.getExpectedAvailableCapacity(time, segmentLoad.getSegment(), numberOfSegments);

                // logger.warn("Capacity of plant " + plant.toString() +
                // " is " +
                // plantCapacity/plant.getTechnology().getCapacity());
                if (segmentSupply < expectedSegmentLoad) {
                    segmentSupply += plantCapacity;
                    segmentPrice = plantCost.getValue();
                }

            }

            // logger.warn("Segment " +
            // segmentLoad.getSegment().getSegmentID() + " supply equals " +
            // segmentSupply + " and segment demand equals " +
            // expectedSegmentLoad);

            if (segmentSupply >= expectedSegmentLoad) {
                expectedElectricityPricesPerSegment.put(segmentLoad.getSegment(), segmentPrice);
            } else {
                expectedElectricityPricesPerSegment.put(segmentLoad.getSegment(), market.getValueOfLostLoad());
            }

        }
    }

    public double determineExpectedMarginalCost(PowerPlant plant, Map<Substance, Double> expectedFuelPrices, double expectedCO2Price) {
        double mc = determineExpectedMarginalFuelCost(plant, expectedFuelPrices);
        double co2Intensity = calculateCO2Intensity(plant);
        mc += co2Intensity * expectedCO2Price;
        return mc;
    }

    public double determineExpectedMarginalFuelCost(PowerPlant powerPlant, Map<Substance, Double> expectedFuelPrices) {
        double fc = 0d;
        for (SubstanceShareInFuelMix mix : powerPlant.getFuelMix()) {
            double amount = mix.getShare();
            double fuelPrice = expectedFuelPrices.get(mix.getSubstance());
            fc += amount * fuelPrice;
        }
        return fc;
    }

    public double calculateCO2Intensity(PowerPlant plant) {
        return calculateCO2Intensity(plant.getFuelMix()) * (1 - plant.getTechnology().getCo2CaptureEffciency());
    }

    public double calculateCO2Intensity(Set<SubstanceShareInFuelMix> fuelMix) {
        double co2Intensity = 0d;
        for (SubstanceShareInFuelMix mix : fuelMix) {
            co2Intensity += mix.getShare() * mix.getSubstance().getCo2Density();
        }
        return co2Intensity;
    }
}
