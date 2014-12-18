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
package emlab.gen.domain.agent;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

import agentspring.agent.Agent;
import emlab.gen.trend.TimeSeriesImpl;

/**
 * Government class representing a government of the entire simualation (e.g. a
 * European government, or a federal governemnt). Contains two boolean
 * parameters. activelyAdjustingTheCO2Cap defines whether the government adjusts
 * the CO2 based on renewable installations. The second parameter
 * deviationFromResTargetAdjustment sets if the deviation is measured from zero,
 * or as an exceeding of the predefined target. See
 * emlab.gen.role.co2Policy.RenewableAdaptiveCO2CapRole.java for details.
 *
 * @author jcrichstein
 *
 */

@NodeEntity
public class Government extends DecarbonizationAgent implements Agent {

    @RelatedTo(type = "CO2TAX_TREND", elementClass = TimeSeriesImpl.class, direction = Direction.OUTGOING)
    private TimeSeriesImpl co2TaxTrend;

    @RelatedTo(type = "CO2CAP_TREND", elementClass = TimeSeriesImpl.class, direction = Direction.OUTGOING)
    private TimeSeriesImpl co2CapTrend;

    @RelatedTo(type = "MINCO2PRICE_TREND", elementClass = TimeSeriesImpl.class, direction = Direction.OUTGOING)
    private TimeSeriesImpl minCo2PriceTrend;

    @RelatedTo(type = "CO2_PRICECEILING_TREND", elementClass = TimeSeriesImpl.class, direction = Direction.OUTGOING)
    private TimeSeriesImpl co2PriceCeilingTrend;

    @RelatedTo(type = "CO2CAPADJUSTMENT_TIMESERIES", elementClass = TimeSeriesImpl.class, direction = Direction.OUTGOING)
    private TimeSeriesImpl co2CapAdjustmentTimeSeries;

    private boolean activelyAdjustingTheCO2Cap;

    private boolean deviationFromResTargetAdjustment;

    private double adaptiveCapCO2SavingsWeighingFactor;

    private boolean adaptiveCapAdjustmentBasedOnCapNotActualEmissions;

    private double co2Penalty;

    private double stabilityReserve;

    @RelatedTo(type = "STABILITY_RESERVE_UPPER_TRIGGER", elementClass = TimeSeriesImpl.class, direction = Direction.OUTGOING)
    private TimeSeriesImpl stabilityReserveUpperTriggerTrend;

    @RelatedTo(type = "STABILITY_RESERVE_LOWER_TRIGGER", elementClass = TimeSeriesImpl.class, direction = Direction.OUTGOING)
    private TimeSeriesImpl stabilityReserveLowerTriggerTrend;

    @RelatedTo(type = "STABILITY_RESERVE_ADDING_PERCENTAGE", elementClass = TimeSeriesImpl.class, direction = Direction.OUTGOING)
    private TimeSeriesImpl stabilityReserveAddingPercentageTrend;

    @RelatedTo(type = "STABILITY_RESERVE_ADDING_MINIMUM", elementClass = TimeSeriesImpl.class, direction = Direction.OUTGOING)
    private TimeSeriesImpl stabilityReserveAddingMinimumTrend;

    @RelatedTo(type = "STABILITY_RESERVE_RELEASE_QUANTITY", elementClass = TimeSeriesImpl.class, direction = Direction.OUTGOING)
    private TimeSeriesImpl stabilityReserveReleaseQuantityTrend;

    public double getCO2Tax(long time) {
        return co2TaxTrend.getValue(time);
    }

    public double getMinCo2Price(long time) {
        return minCo2PriceTrend.getValue(time);
    }

    public double getCo2Cap(long time) {
        return co2CapTrend.getValue(time);
    }

    public TimeSeriesImpl getCo2TaxTrend() {
        return co2TaxTrend;
    }

    public void setCo2TaxTrend(TimeSeriesImpl co2TaxTrend) {
        this.co2TaxTrend = co2TaxTrend;
    }

    public TimeSeriesImpl getCo2CapTrend() {
        return co2CapTrend;
    }

    public void setCo2CapTrend(TimeSeriesImpl co2CapTrend) {
        this.co2CapTrend = co2CapTrend;
    }

    public TimeSeriesImpl getMinCo2PriceTrend() {
        return minCo2PriceTrend;
    }

    public void setMinCo2PriceTrend(TimeSeriesImpl minCo2PriceTrend) {
        this.minCo2PriceTrend = minCo2PriceTrend;
    }

    public double getCo2Penalty(long time) {
        if (getCo2PriceCeilingTrend() != null)
            return getCo2PriceCeilingTrend().getValue(time);
        return co2Penalty;
    }

    public void setCo2Penalty(double co2Penalty) {
        this.co2Penalty = co2Penalty;
    }

    public TimeSeriesImpl getCo2PriceCeilingTrend() {
        return co2PriceCeilingTrend;
    }

    public void setCo2PriceCeilingTrend(TimeSeriesImpl co2PriceCeilingTrend) {
        this.co2PriceCeilingTrend = co2PriceCeilingTrend;
    }

    public boolean isActivelyAdjustingTheCO2Cap() {
        return activelyAdjustingTheCO2Cap;
    }

    public void setActivelyAdjustingTheCO2Cap(boolean activelyAdjustingTheCO2Cap) {
        this.activelyAdjustingTheCO2Cap = activelyAdjustingTheCO2Cap;
    }

    public double getStabilityReserve() {
        return stabilityReserve;
    }

    public void setStabilityReserve(double stabilityReserve) {
        this.stabilityReserve = stabilityReserve;
    }

    public TimeSeriesImpl getStabilityReserveUpperTriggerTrend() {
        return stabilityReserveUpperTriggerTrend;
    }

    public void setStabilityReserveUpperTriggerTrend(TimeSeriesImpl stabilityReserveUpperTriggerTrend) {
        this.stabilityReserveUpperTriggerTrend = stabilityReserveUpperTriggerTrend;
    }

    public TimeSeriesImpl getStabilityReserveLowerTriggerTrend() {
        return stabilityReserveLowerTriggerTrend;
    }

    public void setStabilityReserveLowerTriggerTrend(TimeSeriesImpl stabilityReserveLowerTriggerTrend) {
        this.stabilityReserveLowerTriggerTrend = stabilityReserveLowerTriggerTrend;
    }

    public TimeSeriesImpl getStabilityReserveAddingPercentageTrend() {
        return stabilityReserveAddingPercentageTrend;
    }

    public void setStabilityReserveAddingPercentageTrend(TimeSeriesImpl stabilityReserveAddingPercentageTrend) {
        this.stabilityReserveAddingPercentageTrend = stabilityReserveAddingPercentageTrend;
    }

    public TimeSeriesImpl getStabilityReserveAddingMinimumTrend() {
        return stabilityReserveAddingMinimumTrend;
    }

    public void setStabilityReserveAddingMinimumTrend(TimeSeriesImpl stabilityReserveAddingMinimumTrend) {
        this.stabilityReserveAddingMinimumTrend = stabilityReserveAddingMinimumTrend;
    }

    public TimeSeriesImpl getStabilityReserveReleaseQuantityTrend() {
        return stabilityReserveReleaseQuantityTrend;
    }

    public void setStabilityReserveReleaseQuantityTrend(TimeSeriesImpl stabilityReserveReleaseQuantityTrend) {
        this.stabilityReserveReleaseQuantityTrend = stabilityReserveReleaseQuantityTrend;
    }

    public boolean isDeviationFromResTargetAdjustment() {
        return deviationFromResTargetAdjustment;
    }

    public void setDeviationFromResTargetAdjustment(boolean deviationFromResTargetAdjustment) {
        this.deviationFromResTargetAdjustment = deviationFromResTargetAdjustment;
    }

    public double getAdaptiveCapCO2SavingsWeighingFactor() {
        return adaptiveCapCO2SavingsWeighingFactor;
    }

    public void setAdaptiveCapCO2SavingsWeighingFactor(double adaptiveCapCO2SavingsWeighingFactor) {
        this.adaptiveCapCO2SavingsWeighingFactor = adaptiveCapCO2SavingsWeighingFactor;
    }

    public TimeSeriesImpl getCo2CapAdjustmentTimeSeries() {
        return co2CapAdjustmentTimeSeries;
    }

    public void setCo2CapAdjustmentTimeSeries(TimeSeriesImpl co2CapAdjustmentTimeSeries) {
        this.co2CapAdjustmentTimeSeries = co2CapAdjustmentTimeSeries;
    }

    public boolean isAdaptiveCapAdjustmentBasedOnCapNotActualEmissions() {
        return adaptiveCapAdjustmentBasedOnCapNotActualEmissions;
    }

    public void setAdaptiveCapAdjustmentBasedOnCapNotActualEmissions(
            boolean adaptiveCapAdjustmentBasedOnCapNotActualEmissions) {
        this.adaptiveCapAdjustmentBasedOnCapNotActualEmissions = adaptiveCapAdjustmentBasedOnCapNotActualEmissions;
    }

}
