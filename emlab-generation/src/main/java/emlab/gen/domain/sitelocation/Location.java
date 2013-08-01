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
package emlab.gen.domain.sitelocation;

import org.springframework.data.neo4j.annotation.NodeEntity;

import agentspring.simulation.SimulationParameter;

/**
 * Representation of a location
 * 
 * @author jpaling
 * 
 */

@NodeEntity
public class Location {

    private String name;

    private double depthWater;

    private double distanceShore;

    @SimulationParameter(label = "Number of Plants possible at Location", from = 0, to = 10)
    private int possiblePlants;

    private String province;

    private boolean offShore;

    private double populationDensity;

    private double utilityLocation;

    private double wealth;

    private double qualityWater;

    private double distanceGrid;

    private double capacityGrid;

    private boolean feedstockAvailabilityNuclear;

    private boolean feedstockAvailabilityCoal;

    private boolean feedstockAvailabilityGas;

    private boolean carbonCaptureStorageAvailability;

    private boolean feedstockAvailabilityWind;

    private boolean feedstockAvailabilitySun;

    private boolean feedstockAvailabilityBiomass;

    private double sunHours;

    private double windPower;

    private double utilityFunction;

    private double plantPresent;

    private double weightFactorWealth;

    private double weightFactorDensity;

    private double weightFactorTechPref;

    private double weightFactorCompensation;

    private double courtChance;

    private double effectivenessCompensation;

    private double AverageUtility;

    public double getDepthWater() {
        return depthWater;
    }

    public void setDepthWater(double depthWater) {
        this.depthWater = depthWater;
    }

    public double getDistanceShore() {
        return distanceShore;
    }

    public void setDistanceShore(double distanceShore) {
        this.distanceShore = distanceShore;
    }

    public double getAverageUtility() {
        return AverageUtility;
    }

    public void setAverageUtility(double averageUtility) {
        AverageUtility = averageUtility;
    }

    public boolean isOffShore() {
        return offShore;
    }

    public void setOffShore(boolean offShore) {
        this.offShore = offShore;
    }

    public double getEffectivenessCompensation() {
        return effectivenessCompensation;
    }

    public void setEffectivenessCompensation(double effectivenessCompensation) {
        this.effectivenessCompensation = effectivenessCompensation;
    }

    public double getCourtChance() {
        return courtChance;
    }

    public void setCourtChance(double courtChance) {
        this.courtChance = courtChance;
    }

    public double getWeightFactorWealth() {
        return weightFactorWealth;
    }

    public void setWeightFactorWealth(double weightFactorWealth) {
        this.weightFactorWealth = weightFactorWealth;
    }

    public double getWeightFactorDensity() {
        return weightFactorDensity;
    }

    public void setWeightFactorDensity(double weightFactorDensity) {
        this.weightFactorDensity = weightFactorDensity;
    }

    public double getWeightFactorTechPref() {
        return weightFactorTechPref;
    }

    public void setWeightFactorTechPref(double weightFactorTechPref) {
        this.weightFactorTechPref = weightFactorTechPref;
    }

    public double getWeightFactorCompensation() {
        return weightFactorCompensation;
    }

    public void setWeightFactorCompensation(double weightFactorCompensation) {
        this.weightFactorCompensation = weightFactorCompensation;
    }

    public double getPlantPresent() {
        return plantPresent;
    }

    public void setPlantPresent(double plantPresent) {
        this.plantPresent = plantPresent;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public boolean isFeedstockAvailabilityBiomass() {
        return feedstockAvailabilityBiomass;
    }

    public void setFeedstockAvailabilityBiomass(boolean feedstockAvailabilityBiomass) {
        this.feedstockAvailabilityBiomass = feedstockAvailabilityBiomass;
    }

    public double getUtilityFunction() {
        return utilityFunction;
    }

    public void setUtilityFunction(double utilityFunction) {
        this.utilityFunction = utilityFunction;
    }

    public boolean isFeedstockAvailabilityWind() {
        return feedstockAvailabilityWind;
    }

    public void setFeedstockAvailabilityWind(boolean feedstockAvailabilityWind) {
        this.feedstockAvailabilityWind = feedstockAvailabilityWind;
    }

    public boolean isFeedstockAvailabilitySun() {
        return feedstockAvailabilitySun;
    }

    public void setFeedstockAvailabilitySun(boolean feedstockAvailabilitySun) {
        this.feedstockAvailabilitySun = feedstockAvailabilitySun;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPossiblePlants() {
        return possiblePlants;
    }

    public void setPossiblePlants(int possiblePlants) {
        this.possiblePlants = possiblePlants;
    }

    public double getPopulationDensity() {
        return populationDensity;
    }

    public void setPopulationDensity(double populationDensity) {
        this.populationDensity = populationDensity;
    }

    public double getWealth() {
        return wealth;
    }

    public double getUtilityLocation() {
        return utilityLocation;
    }

    public void setUtilityLocation(double utilityLocation) {
        this.utilityLocation = utilityLocation;
    }

    public void setWealth(double wealth) {
        this.wealth = wealth;
    }

    public double getQualityWater() {
        return qualityWater;
    }

    public void setQualityWater(double qualityWater) {
        this.qualityWater = qualityWater;
    }

    public double getDistanceGrid() {
        return distanceGrid;
    }

    public void setDistanceGrid(double distanceGrid) {
        this.distanceGrid = distanceGrid;
    }

    public double getCapacityGrid() {
        return capacityGrid;
    }

    public void setCapacityGrid(double capacityGrid) {
        this.capacityGrid = capacityGrid;
    }

    public boolean isFeedstockAvailabilityNuclear() {
        return feedstockAvailabilityNuclear;
    }

    public void setFeedstockAvailabilityNuclear(boolean feedstockAvailabilityNuclear) {
        this.feedstockAvailabilityNuclear = feedstockAvailabilityNuclear;
    }

    public boolean isFeedstockAvailabilityCoal() {
        return feedstockAvailabilityCoal;
    }

    public void setFeedstockAvailabilityCoal(boolean feedstockAvailabilityCoal) {
        this.feedstockAvailabilityCoal = feedstockAvailabilityCoal;
    }

    public boolean isFeedstockAvailabilityGas() {
        return feedstockAvailabilityGas;
    }

    public void setFeedstockAvailabilityGas(boolean feedstockAvailabilityGas) {
        this.feedstockAvailabilityGas = feedstockAvailabilityGas;
    }

    public boolean isCarbonCaptureStorageAvailability() {
        return carbonCaptureStorageAvailability;
    }

    public void setCarbonCaptureStorageAvailability(boolean carbonCaptureStorageAvailability) {
        this.carbonCaptureStorageAvailability = carbonCaptureStorageAvailability;
    }

    public double getSunHours() {
        return sunHours;
    }

    public void setSunHours(double sunHours) {
        this.sunHours = sunHours;
    }

    public double getWindPower() {
        return windPower;
    }

    public void setWindPower(double windPower) {
        this.windPower = windPower;
    }

}
