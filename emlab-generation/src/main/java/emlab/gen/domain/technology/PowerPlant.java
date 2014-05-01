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

import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.data.annotation.Transient;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.springframework.transaction.annotation.Transactional;

import emlab.gen.domain.agent.EnergyProducer;
import emlab.gen.domain.contract.Loan;
import emlab.gen.domain.market.electricity.PowerPlantDispatchPlan;
import emlab.gen.domain.market.electricity.Segment;
import emlab.gen.repository.PowerPlantDispatchPlanRepository;

/**
 * Representation of a power plant
 * 
 * @author jcrichstein
 * @author ejlchappin
 * 
 */

@Configurable
@NodeEntity
public class PowerPlant {

    @Transient
    @Autowired
    private PowerPlantDispatchPlanRepository powerPlantDispatchPlanRepository;

    @RelatedTo(type = "TECHNOLOGY", elementClass = PowerGeneratingTechnology.class, direction = Direction.OUTGOING)
    private PowerGeneratingTechnology technology;

    @RelatedTo(type = "FUEL_MIX", elementClass = SubstanceShareInFuelMix.class, direction = Direction.OUTGOING)
    private Set<SubstanceShareInFuelMix> fuelMix;

    @RelatedTo(type = "POWERPLANT_OWNER", elementClass = EnergyProducer.class, direction = Direction.OUTGOING)
    private EnergyProducer owner;

    @RelatedTo(type = "LOCATION", elementClass = PowerGridNode.class, direction = Direction.OUTGOING)
    private PowerGridNode location;

    @RelatedTo(type = "LOAN", elementClass = Loan.class, direction = Direction.OUTGOING)
    private Loan loan;

    @RelatedTo(type = "DOWNPAYMENT", elementClass = Loan.class, direction = Direction.OUTGOING)
    private Loan downpayment;

    /**
     * dismantleTime is set to 1000 as a signifier, that the powerplant is not
     * yet dismantled.
     */
    private long dismantleTime;
    private long constructionStartTime;
    private long actualLeadtime;
    private long actualPermittime;
    private long actualLifetime;
    private String name;
    private String label;
    private double actualInvestedCapital;
    private double actualFixedOperatingCost;
    private double actualEfficiency;
    private double expectedEndOfLife;
    private double actualNominalCapacity;
    public double ageFraction;
    public double profitability;

    public double getProfitability() {
        return profitability;
    }

    public void setProfitability(double profitability) {
        this.profitability = profitability;
    }

    public double getAgeFraction() {
        return ageFraction;
    }

    public void setAgeFraction(double ageFraction) {
        this.ageFraction = ageFraction;
    }

    public boolean isOperational(long currentTick) {

        double finishedConstruction = getConstructionStartTime()
                + calculateActualPermittime() + calculateActualLeadtime();

        if (finishedConstruction <= currentTick) {
            // finished construction

            if (getDismantleTime() == 1000) {
                // No dismantletime set, therefore must be not yet dismantled.
                return true;
            } else if (getDismantleTime() > currentTick) {
                // Dismantle time set, but not yet reached
                return true;
            } else if (getDismantleTime() <= currentTick) {
                // Dismantle time passed so not operational
                return false;
            }
        }
        // Construction not yet finished.
        return false;
    }

    public boolean isExpectedToBeOperational(long time) {

        double finishedConstruction = getConstructionStartTime()
                + calculateActualPermittime() + calculateActualLeadtime();

        if (finishedConstruction <= time) {
            // finished construction

            if (getExpectedEndOfLife() > time) {
                // Powerplant is not expected to be dismantled
                return true;
            }
        }
        // Construction not yet finished.
        return false;
    }

    public boolean isInPipeline(long currentTick) {

        double finishedConstruction = getConstructionStartTime()
                + calculateActualPermittime() + calculateActualLeadtime();

        if (finishedConstruction > currentTick) {
            // finished construction

            if (getDismantleTime() == 1000) {
                // No dismantletime set, therefore must be not yet dismantled.
                return true;
            } else if (getDismantleTime() > currentTick) {
                // Dismantle time set, but not yet reached
                return true;
            } else if (getDismantleTime() <= currentTick) {
                // Dismantle time passed so not operational
                return false;
            }
        }
        // Construction finished
        return false;
    }

    public double getAvailableCapacity(long currentTick, Segment segment,
            long numberOfSegments) {
        if (isOperational(currentTick)) {
            double factor = 1;
            if (segment != null) {// if no segment supplied, assume we want full
                // capacity
                double segmentID = segment.getSegmentID();
                if ((int) segmentID != 1) {

                    double min = getTechnology()
                            .getPeakSegmentDependentAvailability();
                    double max = getTechnology()
                            .getBaseSegmentDependentAvailability();
                    double segmentPortion = (numberOfSegments - segmentID)
                            / (numberOfSegments - 1); // start
                    // counting
                    // at
                    // 1.

                    double range = max - min;

                    factor = max - segmentPortion * range;
                    int i = 0;
                } else {
                    factor = getTechnology()
                            .getPeakSegmentDependentAvailability();
                }
            }
            return getActualNominalCapacity() * factor;
        } else {
            return 0;
        }
    }

    public double getExpectedAvailableCapacity(long futureTick,
            Segment segment, long numberOfSegments) {
        if (isExpectedToBeOperational(futureTick)) {
            double factor = 1;
            if (segment != null) {// if no segment supplied, assume we want full
                // capacity
                double segmentID = segment.getSegmentID();
                double min = getTechnology()
                        .getPeakSegmentDependentAvailability();
                double max = getTechnology()
                        .getBaseSegmentDependentAvailability();
                double segmentPortion = (numberOfSegments - segmentID)
                        / (numberOfSegments - 1); // start
                // counting
                // at
                // 1.

                double range = max - min;

                factor = max - segmentPortion * range;
            }
            return getActualNominalCapacity() * factor;
        } else {
            return 0;
        }
    }

    public double getAvailableCapacity(long currentTick) {
        if (isOperational(currentTick)) {
            return getActualNominalCapacity();
        } else {
            return 0;
        }
    }

    public long calculateActualLeadtime() {
        long actual;
        actual = getActualLeadtime();
        if (actual <= 0) {
            actual = getTechnology().getExpectedLeadtime();
        }
        return actual;
    }

    public long calculateActualPermittime() {
        long actual;
        actual = getActualPermittime();
        if (actual <= 0) {
            actual = getTechnology().getExpectedPermittime();
        }
        return actual;
    }

    public long calculateActualLifetime() {
        long actual;
        actual = getActualLifetime();
        if (actual <= 0) {
            actual = getTechnology().getExpectedLifetime();
        }
        return actual;
    }

    /**
     * Determines whether a plant is still in its technical lifetime. The end of
     * the technical lifetime is determined by the construction start time, the
     * permit time, the lead time and the actual lifetime.
     * 
     * @param currentTick
     * @return whether the plant is still in its technical lifetime.
     */
    public boolean isWithinTechnicalLifetime(long currentTick) {
        long endOfTechnicalLifetime = getConstructionStartTime()
                + calculateActualPermittime() + calculateActualLeadtime()
                + calculateActualLifetime();
        if (endOfTechnicalLifetime <= currentTick) {
            return false;
        }
        return true;
    }

    public PowerGridNode getLocation() {
        return location;
    }

    public void setLocation(PowerGridNode location) {
        this.location = location;
    }

    public PowerGeneratingTechnology getTechnology() {
        return technology;
    }

    public void setTechnology(PowerGeneratingTechnology technology) {
        this.technology = technology;
    }

    public long getConstructionStartTime() {
        return constructionStartTime;
    }

    public void setConstructionStartTime(long constructionStartTime) {
        this.constructionStartTime = constructionStartTime;
    }

    public EnergyProducer getOwner() {
        return owner;
    }

    public void setOwner(EnergyProducer owner) {
        this.owner = owner;
    }

    public void setActualLifetime(long actualLifetime) {
        this.actualLifetime = actualLifetime;
    }

    public long getActualLifetime() {
        return actualLifetime;
    }

    public void setActualPermittime(long actualPermittime) {
        this.actualPermittime = actualPermittime;
    }

    public long getActualPermittime() {
        return actualPermittime;
    }

    public void setActualLeadtime(long actualLeadtime) {
        this.actualLeadtime = actualLeadtime;
    }

    public long getActualLeadtime() {
        return actualLeadtime;
    }

    public long getDismantleTime() {
        return dismantleTime;
    }

    public void setDismantleTime(long dismantleTime) {
        this.dismantleTime = dismantleTime;
    }

    public String getName() {
        return label;
    }

    public void setName(String label) {
        this.name = label;
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public double getActualInvestedCapital() {
        return actualInvestedCapital;
    }

    public void setActualInvestedCapital(double actualInvestedCapital) {
        this.actualInvestedCapital = actualInvestedCapital;
    }

    public Set<SubstanceShareInFuelMix> getFuelMix() {
        return fuelMix;
    }

    public void setFuelMix(Set<SubstanceShareInFuelMix> fuelMix) {
        this.fuelMix = fuelMix;
    }

    public Loan getLoan() {
        return loan;
    }

    public void setLoan(Loan loan) {
        this.loan = loan;
    }

    public Loan getDownpayment() {
        return downpayment;
    }

    public void setDownpayment(Loan downpayment) {
        this.downpayment = downpayment;
    }

    public double getActualEfficiency() {
        return actualEfficiency;
    }

    public void setActualEfficiency(double actualEfficiency) {
        this.actualEfficiency = actualEfficiency;
    }

    @Override
    public String toString() {
        return this.getName() + " power plant";
    }

    /**
     * Sets the actual capital that is needed to build the power plant. It reads
     * the investment cost from the and automatically adjusts for the actual
     * building and permit time, as well as power plant size.
     * 
     * @param timeOfPermitorBuildingStart
     */
    public void calculateAndSetActualInvestedCapital(
            long timeOfPermitorBuildingStart) {
        setActualInvestedCapital(this.getTechnology().getInvestmentCost(
                timeOfPermitorBuildingStart + getActualLeadtime() + getActualPermittime())
                * getActualNominalCapacity());
    }

    public void calculateAndSetActualFixedOperatingCosts(long timeOfPermitorBuildingStart) {
        setActualFixedOperatingCost(this.getTechnology().getFixedOperatingCost(
                timeOfPermitorBuildingStart + getActualLeadtime() + getActualPermittime())
                * getActualNominalCapacity());
    }

    public void calculateAndSetActualEfficiency(long timeOfPermitorBuildingStart) {
        this.setActualEfficiency(this.getTechnology().getEfficiency(
                timeOfPermitorBuildingStart + getActualLeadtime() + getActualPermittime()));
    }

    public double calculateEmissionIntensity() {

        double emission = 0d;
        for (SubstanceShareInFuelMix sub : this.getFuelMix()) {
            Substance substance = sub.getSubstance();
            double fuelAmount = sub.getShare();
            double co2density = substance.getCo2Density()
                    * (1 - this.getTechnology().getCo2CaptureEffciency());

            // determine the total cost per MWh production of this plant
            double emissionForThisFuel = fuelAmount * co2density;
            emission += emissionForThisFuel;
        }

        return emission;
    }

    public double calculateElectricityOutputAtTime(long time, boolean forecast) {
        // TODO This is in MWh (so hours of segment included!!)
        double amount = 0d;
        for (PowerPlantDispatchPlan plan : powerPlantDispatchPlanRepository
                .findAllPowerPlantDispatchPlansForPowerPlantForTime(this, time, forecast)) {
            amount += plan.getSegment().getLengthInHours()
                    * (plan.getCapacityLongTermContract() + plan
                            .getAcceptedAmount());
        }
        return amount;
    }

    public double calculateCO2EmissionsAtTime(long time, boolean forecast) {
        return this.calculateEmissionIntensity()
                * calculateElectricityOutputAtTime(time, forecast);
    }

    @Transactional
    public void dismantlePowerPlant(long time) {
        this.setDismantleTime(time);
    }

    /**
     * Persists and specifies the properties of a new Power Plant (which needs
     * to be created separately before with new PowerPlant();
     * 
     * Do not forget that any change made here should be reflected in the
     * ElectricityProducerFactory!!
     * 
     * @param time
     * @param energyProducer
     * @param location
     * @param technology
     * 
     * @author J.C.Richstein
     */
    @Transactional
    public void specifyAndPersist(long time, EnergyProducer energyProducer,
            PowerGridNode location, PowerGeneratingTechnology technology) {
        specifyNotPersist(time, energyProducer, location, technology);
        this.persist();
    }

    public void specifyNotPersist(long time, EnergyProducer energyProducer,
            PowerGridNode location, PowerGeneratingTechnology technology) {
        String label = energyProducer.getName() + " - " + technology.getName();
        this.setName(label);
        this.setTechnology(technology);
        this.setOwner(energyProducer);
        this.setLocation(location);
        this.setConstructionStartTime(time);
        this.setActualLeadtime(this.technology.getExpectedLeadtime());
        this.setActualPermittime(this.technology.getExpectedPermittime());
        this.calculateAndSetActualEfficiency(time);
        this.setActualNominalCapacity(this.getTechnology().getCapacity() * location.getCapacityMultiplicationFactor());
        assert this.getActualEfficiency() <= 1 : this.getActualEfficiency();
        this.setDismantleTime(1000);
        this.calculateAndSetActualInvestedCapital(time);
        this.calculateAndSetActualFixedOperatingCosts(time);
        this.setExpectedEndOfLife(time + getActualPermittime()
                + getActualLeadtime() + getTechnology().getExpectedLifetime());
    }

    @Transactional
    public void createOrUpdateLoan(Loan loan) {
        this.setLoan(loan);
    }

    @Transactional
    public void createOrUpdateDownPayment(Loan downpayment) {
        this.setDownpayment(downpayment);
    }

    public double getExpectedEndOfLife() {
        return expectedEndOfLife;
    }

    public void setExpectedEndOfLife(double expectedEndOfLife) {
        this.expectedEndOfLife = expectedEndOfLife;
    }

    @Transactional
    public void updateFuelMix(Set<SubstanceShareInFuelMix> fuelMix) {
        this.setFuelMix(fuelMix);
    }

    /**
     * @return the actualNominalCapacity
     */
    public double getActualNominalCapacity() {
        return actualNominalCapacity;
    }

    /**
     * @param actualNominalCapacity
     *            the actualNominalCapacity to set
     */
    public void setActualNominalCapacity(double actualNominalCapacity) {
        this.actualNominalCapacity = actualNominalCapacity;
    }

    /**
     * @return the actualFixedOperatingCost
     */
    public double getActualFixedOperatingCost() {
        return actualFixedOperatingCost;
    }

    /**
     * @param actualFixedOperatingCost
     *            the actualFixedOperatingCost to set
     */
    public void setActualFixedOperatingCost(double actualFixedOperatingCost) {
        this.actualFixedOperatingCost = actualFixedOperatingCost;
    }

}
