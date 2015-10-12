/*******************************************************************************
 * Copyright 2013 the original author or authors.
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
package emlab.gen.domain.market.electricity;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

import emlab.gen.domain.technology.PowerPlant;

/**
 * This class represents a financial report per power plant per year. It is
 * mainly used to condense information (from CashFlows and
 * PowerPlantDispatchPlans, which are regularly deleted for performance reasons)
 * and make it accessible to agents within the information, but also to the
 * analyst using EMLab-Generation.
 *
 * @author JCRichstein
 *
 */
@NodeEntity
public class FinancialPowerPlantReport {

    @RelatedTo(type = "FINANCIALREPORT_POWERPLANT", elementClass = PowerPlant.class, direction = Direction.OUTGOING)
    private PowerPlant powerPlant;

    long time;

    double spotMarketRevenue;

    double longTermMarketRevenue;

    double capacityMarketRevenue;

    double strategicReserveRevenue;

    double co2HedgingRevenue;

    double overallRevenue;

    double commodityCosts;

    double co2Costs;

    double variableCosts;

    double fixedCosts;

    double fullLoadHours;

    double production;

    int powerPlantStatus;

    public static final int UNDERCONSTRUCTION = 0;
    public static final int OPERATIONAL = 1;
    public static final int DISMANTLED = 2;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public PowerPlant getPowerPlant() {
        return powerPlant;
    }

    public void setPowerPlant(PowerPlant powerPlant) {
        this.powerPlant = powerPlant;
    }

    public double getSpotMarketRevenue() {
        return spotMarketRevenue;
    }

    public void setSpotMarketRevenue(double spotMarketRevenue) {
        this.spotMarketRevenue = spotMarketRevenue;
    }

    public double getLongTermMarketRevenue() {
        return longTermMarketRevenue;
    }

    public void setLongTermMarketRevenue(double longTermMarketRevenue) {
        this.longTermMarketRevenue = longTermMarketRevenue;
    }

    public double getCapacityMarketRevenue() {
        return capacityMarketRevenue;
    }

    public void setCapacityMarketRevenue(double capacityMarketRevenue) {
        this.capacityMarketRevenue = capacityMarketRevenue;
    }

    public double getStrategicReserveRevenue() {
        return strategicReserveRevenue;
    }

    public void setStrategicReserveRevenue(double strategicReserveRevenue) {
        this.strategicReserveRevenue = strategicReserveRevenue;
    }

    public double getCo2HedgingRevenue() {
        return co2HedgingRevenue;
    }

    public void setCo2HedgingRevenue(double co2HedgingRevenue) {
        this.co2HedgingRevenue = co2HedgingRevenue;
    }

    public double getOverallRevenue() {
        return this.overallRevenue;
    }

    public void setOverallRevenue(double overallRevenue) {
        this.overallRevenue = overallRevenue;

    }

    public double getVariableCosts() {
        return variableCosts;
    }

    public void setVariableCosts(double variableCosts) {
        this.variableCosts = variableCosts;
    }

    public double getFixedCosts() {
        return fixedCosts;
    }

    public void setFixedCosts(double fixedCosts) {
        this.fixedCosts = fixedCosts;
    }

    public double getFullLoadHours() {
        return fullLoadHours;
    }

    public void setFullLoadHours(double fullLoadHours) {
        this.fullLoadHours = fullLoadHours;
    }

    public double getProduction() {
        return production;
    }

    public void setProduction(double production) {
        this.production = production;
    }

    public double getCommodityCosts() {
        return commodityCosts;
    }

    public void setCommodityCosts(double commodityCosts) {
        this.commodityCosts = commodityCosts;
    }

    public double getCo2Costs() {
        return co2Costs;
    }

    public void setCo2Costs(double co2Costs) {
        this.co2Costs = co2Costs;
    }

    public int getPowerPlantStatus() {
        return powerPlantStatus;
    }

    public void setPowerPlantStatus(int powerPlantStatus) {
        this.powerPlantStatus = powerPlantStatus;
    }

}
