/*******************************************************************************
 * Copyright 2015 the original author or authors.
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
import agentspring.simulation.SimulationParameter;
import emlab.gen.domain.gis.Zone;

/**
 * @author Kaveri
 * 
 */
@NodeEntity
public class Regulator extends DecarbonizationAgent implements Agent {

    @RelatedTo(type = "OF_ZONE", elementClass = Zone.class, direction = Direction.OUTGOING)
    private Zone zone;

    private int numberOfYearsLookingBackToForecastDemand;

    // Capacity Market Related Parameters

    @SimulationParameter(label = "Capacity Market Price Cap", from = 1000, to = 150000)
    private double capacityMarketPriceCap;

    @SimulationParameter(label = "Reserve Margin", from = 0, to = 1)
    private double reserveMargin;

    @SimulationParameter(label = "Reserve Demand Lower Margin", from = 0, to = 1)
    private double reserveDemandLowerMargin;

    @SimulationParameter(label = "Reserve Demand Upper Margin", from = 0, to = 1)
    private double reserveDemandUpperMargin;

    private double demandTarget;

    @SimulationParameter(label = "Capacity Market Target Period", from = 0, to = 10)
    private int targetPeriod; // number of years in the future that the capacity
    // is being planned for - set to zero

    // Feed-in-Premium Related Pàrameters
    @SimulationParameter(label = "FeedInPremiumFactor", from = 0, to = 1)
    private double feedInPremiumFactor;

    @SimulationParameter(label = "FeedInPremium Contract Length", from = 0, to = 1)
    private long feedInPremiumContractLength;

    public double getDemandTarget() {
        return demandTarget;
    }

    public long getFeedInPremiumContractLength() {
        return feedInPremiumContractLength;
    }

    public void setFeedInPremiumContractLength(long feedInPremiumContractLength) {
        this.feedInPremiumContractLength = feedInPremiumContractLength;
    }

    public void setDemandTarget(double demandTarget) {
        this.demandTarget = demandTarget;
    }

    public double getCapacityMarketPriceCap() {
        return capacityMarketPriceCap;
    }

    public void setCapacityMarketPriceCap(double capacityMarketPriceCap) {
        this.capacityMarketPriceCap = capacityMarketPriceCap;
    }

    public int getNumberOfYearsLookingBackToForecastDemand() {
        return numberOfYearsLookingBackToForecastDemand;
    }

    public void setNumberOfYearsLookingBackToForecastDemand(int numberOfYearsLookingBackToForecastDemand) {
        this.numberOfYearsLookingBackToForecastDemand = numberOfYearsLookingBackToForecastDemand;
    }

    public int getTargetPeriod() {
        return targetPeriod;
    }

    public double getFeedInPremiumFactor() {
        return feedInPremiumFactor;
    }

    public void setFeedInPremiumFactor(double feedInPremiumFactor) {
        this.feedInPremiumFactor = feedInPremiumFactor;
    }

    public void setTargetPeriod(int targetPeriod) {
        this.targetPeriod = targetPeriod;
    }

    public double getReserveDemandLowerMargin() {
        return reserveDemandLowerMargin;
    }

    public void setReserveDemandLowerMargin(double reserveDemandLowerMargin) {
        this.reserveDemandLowerMargin = reserveDemandLowerMargin;
    }

    public double getReserveDemandUpperMargin() {
        return reserveDemandUpperMargin;
    }

    public void setReserveDemandUpperMargin(double reserveDemandUpperMargin) {
        this.reserveDemandUpperMargin = reserveDemandUpperMargin;
    }

    public double getReserveMargin() {
        return reserveMargin;
    }

    public void setReserveMargin(double reserveMargin) {
        this.reserveMargin = reserveMargin;
    }

    public Zone getZone() {
        return zone;
    }

    public void setZone(Zone zone) {
        this.zone = zone;
    }

}
