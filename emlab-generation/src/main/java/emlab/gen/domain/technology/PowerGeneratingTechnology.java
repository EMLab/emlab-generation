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
import emlab.gen.trend.TimeSeriesImpl;

@NodeEntity
public class PowerGeneratingTechnology {

    private String name;

    private double locationFailure;

    @SimulationParameter(label = "Capacity (MW)", from = 0, to = 2000)
    private double capacity;

    @RelatedTo(type = "PGT_INVESTMENTCOSTS", elementClass = TimeSeriesImpl.class, direction = Direction.OUTGOING)
    private TimeSeriesImpl investmentCostTimeSeries;

    @RelatedTo(type = "PGT_OMCOSTS", elementClass = TimeSeriesImpl.class, direction = Direction.OUTGOING)
    private TimeSeriesImpl fixedOperatingCostTimeSeries;

    @RelatedTo(type = "PGT_EFFICIENCYTS", elementClass = TimeSeriesImpl.class, direction = Direction.OUTGOING)
    private TimeSeriesImpl efficiencyTimeSeries;

    @SimulationParameter(label = "CO2 capture efficiency", from = 0, to = 1)
    private double co2CaptureEffciency;

    @SimulationParameter(label = "Depreciation time (years)", from = 0, to = 40)
    private int depreciationTime;

    @SimulationParameter(label = "Minimum running hours (hours/year)", from = 0, to = 8760)
    private double minimumRunningHours;

    private double fixedOperatingCostModifierAfterLifetime;

    @SimulationParameter(label = "Expected lifetime", from = 0, to = 40)
    private int expectedLifetime;

    @SimulationParameter(label = "Expected leadtime", from = 0, to = 10)
    private int expectedLeadtime;

    private int expectedPermittime;
    private double minimumFuelQuality;

    private int expectedLeadtime2;

    private double environmentalCosts;

    private double technologyPreference;

    private double employment;

    private double locationFailureTime;

    private int expectedPermittime2;

    private double previousPermitExperience;

    private double npv;

    private double npvDelay;

    @SimulationParameter(label = "Maximum installed capacity fraction in country", from = 0, to = 1)
    private double maximumInstalledCapacityFractionInCountry;

    @SimulationParameter(label = "Maximum installed capacity fraction per producer", from = 0, to = 1)
    private double maximumInstalledCapacityFractionPerAgent;

    private double baseSegmentDependentAvailability;

    private double peakSegmentDependentAvailability;

    private boolean applicableForLongTermContract;

    private boolean intermittent;

    private String FeedstockID;

    public double getPreviousPermitExperience() {
        return previousPermitExperience;
    }

    public void setPreviousPermitExperience(double previousPermitExperience) {
        this.previousPermitExperience = previousPermitExperience;
    }

    public double getNpv() {
        return npv;
    }

    public void setNpv(double npv) {
        this.npv = npv;
    }

    public double getNpvDelay() {
        return npvDelay;
    }

    public void setNpvDelay(double npvDelay) {
        this.npvDelay = npvDelay;
    }

    public String getFeedstockID() {
        return FeedstockID;
    }

    public void setFeedstockID(String feedstockID) {
        FeedstockID = feedstockID;
    }

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

    public double getEnvironmentalCosts() {
        return environmentalCosts;
    }

    public void setEnvironmentalCosts(double environmentalCosts) {
        this.environmentalCosts = environmentalCosts;
    }

    public double getTechnologyPreference() {
        return technologyPreference;
    }

    public void setTechnologyPreference(double technologyPreference) {
        this.technologyPreference = technologyPreference;
    }

    public double getEmployment() {
        return employment;
    }

    public void setEmployment(double employment) {
        this.employment = employment;
    }

    public double getMaximumInstalledCapacityFractionPerAgent() {
        return maximumInstalledCapacityFractionPerAgent;
    }

    public void setMaximumInstalledCapacityFractionPerAgent(double maximumInstalledCapacityFractionPerAgent) {
        this.maximumInstalledCapacityFractionPerAgent = maximumInstalledCapacityFractionPerAgent;
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

    public double getEfficiency(long time) {
        return efficiencyTimeSeries.getValue(time);
    }

    public TimeSeriesImpl getInvestmentCostTimeSeries() {
        return investmentCostTimeSeries;
    }

    public void setInvestmentCostTimeSeries(TimeSeriesImpl investmentCostTrend) {
        this.investmentCostTimeSeries = investmentCostTrend;
    }

    public TimeSeriesImpl getFixedOperatingCostTimeSeries() {
        return fixedOperatingCostTimeSeries;
    }

    public void setFixedOperatingCostTimeSeries(TimeSeriesImpl fixedOperatingCostTrend) {
        this.fixedOperatingCostTimeSeries = fixedOperatingCostTrend;
    }

    public TimeSeriesImpl getEfficiencyTimeSeries() {
        return efficiencyTimeSeries;
    }

    public void setEfficiencyTimeSeries(TimeSeriesImpl efficiencyTrend) {
        this.efficiencyTimeSeries = efficiencyTrend;
    }

    public double getCo2CaptureEffciency() {
        return co2CaptureEffciency;
    }

    public void setCo2CaptureEffciency(double co2CaptureEffciency) {
        this.co2CaptureEffciency = co2CaptureEffciency;
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

    public int getExpectedLeadtime2() {
        return expectedLeadtime2;
    }

    public void setExpectedLeadtime2(int expectedLeadtime2) {
        this.expectedLeadtime2 = expectedLeadtime2;
    }

    public int getExpectedPermittime() {
        return expectedPermittime;
    }

    public void setExpectedPermittime(int expectedPermittime) {
        this.expectedPermittime = expectedPermittime;
    }

    public int getExpectedPermittime2() {
        return expectedPermittime2;
    }

    public void setExpectedPermittime2(int expectedPermittime2) {
        this.expectedPermittime2 = expectedPermittime2;
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

    @Override
    public String toString() {
        return this.getName();
    }

    public boolean isApplicableForLongTermContract() {
        return applicableForLongTermContract;
    }

    public void setApplicableForLongTermContract(boolean applicableForLongTermContract) {
        this.applicableForLongTermContract = applicableForLongTermContract;
    }

    public double getInvestmentCost(long time) {
        return investmentCostTimeSeries.getValue(time);
    }

    public double getFixedOperatingCost(long time) {
        return fixedOperatingCostTimeSeries.getValue(time);
    }

    public boolean isIntermittent() {
        return intermittent;
    }

    public void setIntermittent(boolean intermittent) {
        this.intermittent = intermittent;
    }

    public double getLocationFailure() {
        return locationFailure;
    }

    public void setLocationFailure(double locationFailure) {
        this.locationFailure = locationFailure;
    }

    public double getLocationFailureTime() {
        return locationFailureTime;
    }

    public void setLocationFailureTime(double locationFailureTime) {
        this.locationFailureTime = locationFailureTime;
    }

}
