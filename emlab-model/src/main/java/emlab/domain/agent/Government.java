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
package emlab.domain.agent;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

import agentspring.agent.Agent;
import emlab.trend.StepTrend;

@NodeEntity
public class Government extends DecarbonizationAgent implements Agent {

    @RelatedTo(type = "CO2TAX_TREND", elementClass = StepTrend.class, direction = Direction.OUTGOING)
    private StepTrend co2TaxTrend;

    @RelatedTo(type = "CO2CAP_TREND", elementClass = StepTrend.class, direction = Direction.OUTGOING)
    private StepTrend co2CapTrend;

    @RelatedTo(type = "MINCO2PRICE_TREND", elementClass = StepTrend.class, direction = Direction.OUTGOING)
    private StepTrend minCo2PriceTrend;

    private double co2Penalty;

    public double getCO2Tax(long time) {
        return co2TaxTrend.getValue(time);
    }

    public double getMinCo2Price(long time) {
        return minCo2PriceTrend.getValue(time);
    }

    public double getCo2Cap(long time) {
        return co2CapTrend.getValue(time);
    }

    public StepTrend getCo2TaxTrend() {
        return co2TaxTrend;
    }

    public void setCo2TaxTrend(StepTrend co2TaxTrend) {
        this.co2TaxTrend = co2TaxTrend;
    }

    public StepTrend getCo2CapTrend() {
        return co2CapTrend;
    }

    public void setCo2CapTrend(StepTrend co2CapTrend) {
        this.co2CapTrend = co2CapTrend;
    }

    public StepTrend getMinCo2PriceTrend() {
        return minCo2PriceTrend;
    }

    public void setMinCo2PriceTrend(StepTrend minCo2PriceTrend) {
        this.minCo2PriceTrend = minCo2PriceTrend;
    }

    public double getCo2Penalty() {
        return co2Penalty;
    }

    public void setCo2Penalty(double co2Penalty) {
        this.co2Penalty = co2Penalty;
    }

}
