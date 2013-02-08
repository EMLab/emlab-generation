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
package emlab.gen.domain.technology;

import java.util.HashSet;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

import agentspring.simulation.SimulationParameter;

@NodeEntity
public class PowerGeneratingTechnology {

    private String name;

    @SimulationParameter(label = "Capacity (MW)", from = 0, to = 2000)
    private double capacity;

    @SimulationParameter(label = "Efficiency", from = 0, to = 1)
    private double efficiency;

    @SimulationParameter(label = "Efficiency modifier (exogenous)", from = -1, to = 1)
    private double efficiencyModifierExogenous;

    @SimulationParameter(label = "CO2 capture efficiency", from = 0, to = 1)
    private double co2CaptureEffciency;

    @SimulationParameter(label = "Total investment cost (EUR)", from = 0, to = 1000000000)
    private double baseInvestmentCost;
    private double investmentCostModifierExogenous;

    @SimulationParameter(label = "Down payment (30% of investment)", from = 0, to = 1000000000)
    private double downPayment;

    @SimulationParameter(label = "Depreciation time (years)", from = 0, to = 40)
    private int depreciationTime;

    @SimulationParameter(label = "Minimum running hours (hours/year)", from = 0, to = 8760)
    private double minimumRunningHours;

    @SimulationParameter(label = "Fixed operating cost (euro/MW)", from = 0, to = 100000)
    private double fixedOperatingCost;
    private double fixedOperatingCostModifierAfterLifetime;

    @SimulationParameter(label = "Expected lifetime", from = 0, to = 40)
    private int expectedLifetime;

    @SimulationParameter(label = "Expected leadtime", from = 0, to = 10)
    private int expectedLeadtime;

    private int expectedPermittime;
    private double minimumFuelQuality;

    @SimulationParameter(label = "Maximum installed capacity fraction in country", from = 0, to = 1)
    private double maximumInstalledCapacityFractionInCountry;

    @SimulationParameter(label = "Maximum installed capacity fraction per producer", from = 0, to = 1)
    private double maximumInstalledCapacityFractionPerAgent;

    private double baseSegmentDependentAvailability;

    private double peakSegmentDependentAvailability;

    private boolean applicableForLongTermContract;

    private boolean intermittent;

    public double getBaseSegmentDependentAvailability() {
        return baseSegmentDependentAvailability;
    }

    public void setBaseSegmentDependentAvailability(double baseSegmentDependentAvailability) {
        this.baseSegmentDependentAvailability = baseSegmentDependentAvailability;
    }

    public double getPeakSegmentDependentAvailability() {
        return peakSegmentDependentAvailability;
    }

    public void setPeakSegmentDependentAvailability(double peakSegmentDependentAvailability) {
        this.peakSegmentDependentAvailability = peakSegmentDependentAvailability;
    }

    public double getMaximumInstalledCapacityFractionInCountry() {
        return maximumInstalledCapacityFractionInCountry;
    }

    public void setMaximumInstalledCapacityFractionInCountry(double maximumInstalledCapacityFractionInCountry) {
        this.maximumInstalledCapacityFractionInCountry = maximumInstalledCapacityFractionInCountry;
    }

    public double getMaximumInstalledCapacityFractionPerAgent() {
        return maximumInstalledCapacityFractionPerAgent;
    }

    public void setMaximumInstalledCapacityFractionPerAgent(double maximumInstalledCapacityFractionPerAgent) {
        this.maximumInstalledCapacityFractionPerAgent = maximumInstalledCapacityFractionPerAgent;
    }

    public double getDownPayment() {
        return downPayment;
    }

    public void setDownPayment(double downPayment) {
        this.downPayment = downPayment;
    }

    public int getDepreciationTime() {
        return depreciationTime;
    }

    public void setDepreciationTime(int depreciationTime) {
        this.depreciationTime = depreciationTime;
    }

    public double getMinimumRunningHours() {
        return minimumRunningHours;
    }

    public void setMinimumRunningHours(double minimumRunningHours) {
        this.minimumRunningHours = minimumRunningHours;
    }

    @RelatedTo(type = "FUEL", elementClass = Substance.class, direction = Direction.OUTGOING)
    private Set<Substance> fuels;

    public String getName() {
        return name;
    }

    public void setName(String label) {
        this.name = label;
    }

    /*
     * assumption: the first is the main fuel
     */
    public Substance getMainFuel() {
        if (getFuels().size() > 0) {
            return getFuels().iterator().next();
        } else {
            return null;
        }
    }

    public Set<Substance> getCoCombustionFuels() {
        Set<Substance> coFuels = new HashSet<Substance>(getFuels());
        coFuels.remove(getMainFuel());
        return coFuels;
    }

    public double getCapacity() {
        return capacity;
    }

    public void setCapacity(double capacity) {
        this.capacity = capacity;
    }

    public double getEfficiency() {
        return efficiency;
    }

    public void setEfficiency(double efficiency) {
        this.efficiency = efficiency;
    }

    public double getEfficiencyModifierExogenous() {
        return efficiencyModifierExogenous;
    }

    public void setEfficiencyModifierExogenous(double efficiencyModifierExogenous) {
        this.efficiencyModifierExogenous = efficiencyModifierExogenous;
    }

    public double getCo2CaptureEffciency() {
        return co2CaptureEffciency;
    }

    public void setCo2CaptureEffciency(double co2CaptureEffciency) {
        this.co2CaptureEffciency = co2CaptureEffciency;
    }

    public double getInvestmentCostModifierExogenous() {
        return investmentCostModifierExogenous;
    }

    public void setInvestmentCostModifierExogenous(double investmentCostModifierExogenous) {
        this.investmentCostModifierExogenous = investmentCostModifierExogenous;
    }

    public double getFixedOperatingCost() {
        return fixedOperatingCost;
    }

    public void setFixedOperatingCost(double fixedOperatingCost) {
        this.fixedOperatingCost = fixedOperatingCost;
    }

    public double getFixedOperatingCostModifierAfterLifetime() {
        return fixedOperatingCostModifierAfterLifetime;
    }

    public void setFixedOperatingCostModifierAfterLifetime(double fixedOperatingCostModifierAfterLifetime) {
        this.fixedOperatingCostModifierAfterLifetime = fixedOperatingCostModifierAfterLifetime;
    }

    public int getExpectedLifetime() {
        return expectedLifetime;
    }

    public void setExpectedLifetime(int expectedLifetime) {
        this.expectedLifetime = expectedLifetime;
    }

    public int getExpectedLeadtime() {
        return expectedLeadtime;
    }

    public void setExpectedLeadtime(int expectedLeadtime) {
        this.expectedLeadtime = expectedLeadtime;
    }

    public int getExpectedPermittime() {
        return expectedPermittime;
    }

    public void setExpectedPermittime(int expectedPermittime) {
        this.expectedPermittime = expectedPermittime;
    }

    public double getMinimumFuelQuality() {
        return minimumFuelQuality;
    }

    public void setMinimumFuelQuality(double minimumFuelQuality) {
        this.minimumFuelQuality = minimumFuelQuality;
    }

    public Set<Substance> getFuels() {
        return fuels;
    }

    public void setFuels(Set<Substance> fuels) {
        this.fuels = fuels;
    }

    public String toString() {
        return this.getName();
    }

    public boolean isApplicableForLongTermContract() {
        return applicableForLongTermContract;
    }

    public void setApplicableForLongTermContract(boolean applicableForLongTermContract) {
        this.applicableForLongTermContract = applicableForLongTermContract;
    }

    public double getBaseInvestmentCost() {
        return baseInvestmentCost;
    }

    public void setBaseInvestmentCost(double baseInvestmentCost) {
        this.baseInvestmentCost = baseInvestmentCost;
    }

    public boolean isIntermittent() {
        return intermittent;
    }

    public void setIntermittent(boolean intermittent) {
        this.intermittent = intermittent;
    }

}
