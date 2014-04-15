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

    private boolean activelyAdjustingTheCO2Cap;

    private double co2Penalty;

    private double stabilityReserve;

    private double stabilityReserveAddingThreshold;

    private double stabilityReserveReleasingThreshold;

    private double stabilityReserveAddingPercentage;

    private double stabilityReserveAddingMinimum;

    private double stabilityReserveReleaseQuantity;

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

    public double getStabilityReserveAddingThreshold() {
        return stabilityReserveAddingThreshold;
    }

    public void setStabilityReserveAddingThreshold(double stabilityReserveAddingThreshold) {
        this.stabilityReserveAddingThreshold = stabilityReserveAddingThreshold;
    }

    public double getStabilityReserveReleasingThreshold() {
        return stabilityReserveReleasingThreshold;
    }

    public void setStabilityReserveReleasingThreshold(double stabilityReserveReleasingThreshold) {
        this.stabilityReserveReleasingThreshold = stabilityReserveReleasingThreshold;
    }

    public double getStabilityReserveAddingPercentage() {
        return stabilityReserveAddingPercentage;
    }

    public void setStabilityReserveAddingPercentage(double stabilityReserveAddingPercentage) {
        this.stabilityReserveAddingPercentage = stabilityReserveAddingPercentage;
    }

    public double getStabilityReserveAddingMinimum() {
        return stabilityReserveAddingMinimum;
    }

    public void setStabilityReserveAddingMinimum(double stabilityReserveAddingMinimum) {
        this.stabilityReserveAddingMinimum = stabilityReserveAddingMinimum;
    }

    public double getStabilityReserveReleaseQuantity() {
        return stabilityReserveReleaseQuantity;
    }

    public void setStabilityReserveReleaseQuantity(double stabilityReserveReleaseQuantity) {
        this.stabilityReserveReleaseQuantity = stabilityReserveReleaseQuantity;
    }

}
