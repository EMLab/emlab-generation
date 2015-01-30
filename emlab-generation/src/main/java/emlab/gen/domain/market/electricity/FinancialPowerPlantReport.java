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
 * This class reresents a financial report per power plant. It is mainly used to
 * condense information (from CashFlows and PowerPlantDispatchPlans, which are
 * regularly deleted for performance reasons) and make it accessible to agents
 * within the information, but also to the analyst using EMLab-Generation.
 * 
 * @author JCRichstein
 * 
 */
@NodeEntity
public class FinancialPowerPlantReport {

    @RelatedTo(type = "FINANCIALREPORT_POWERPLANT", elementClass = PowerPlant.class, direction = Direction.OUTGOING)
    private PowerPlant powerPlant;

    double spotMarketRevenue;

    double longTermMarketRevenue;

    double variableCosts;

    double fixedCosts;

    double fullLoadHours;

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

}
