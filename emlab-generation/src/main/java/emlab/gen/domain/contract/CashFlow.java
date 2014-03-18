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
package emlab.gen.domain.contract;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

import emlab.gen.domain.agent.DecarbonizationAgent;
import emlab.gen.domain.technology.PowerPlant;

@NodeEntity
public class CashFlow {

    public static final int UNCLASSIFIED = 0;
    public static final int ELECTRICITY_SPOT = 1;
    public static final int ELECTRICITY_LONGTERM = 2;
    public static final int FIXEDOMCOST = 3;
    public static final int COMMODITY = 4;
    public static final int CO2TAX = 5;
    public static final int CO2AUCTION = 6;
    public static final int LOAN = 7;
    public static final int DOWNPAYMENT = 8;
    public static final int NATIONALMINCO2 = 9;
    public static final int STRRESPAYMENT = 10;
    public static final int CO2HEDGING = 12;

    @RelatedTo(type = "FROM_AGENT", elementClass = DecarbonizationAgent.class, direction = Direction.OUTGOING)
    private DecarbonizationAgent from;

    @RelatedTo(type = "TO_AGENT", elementClass = DecarbonizationAgent.class, direction = Direction.OUTGOING)
    private DecarbonizationAgent to;

    @RelatedTo(type = "REGARDING_POWERPLANT", elementClass = PowerPlant.class, direction = Direction.OUTGOING)
    private PowerPlant regardingPowerPlant;

    private int type;
    private double money;
    private long time;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public DecarbonizationAgent getFrom() {
        return from;
    }

    public void setFrom(DecarbonizationAgent from) {
        this.from = from;
    }

    public DecarbonizationAgent getTo() {
        return to;
    }

    public void setTo(DecarbonizationAgent to) {
        this.to = to;
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "from " + getFrom() + " to " + getTo() + " type " + getType() + " amount " + getMoney();
    }

    public PowerPlant getRegardingPowerPlant() {
        return regardingPowerPlant;
    }

    public void setRegardingPowerPlant(PowerPlant regardingPowerPlant) {
        this.regardingPowerPlant = regardingPowerPlant;
    }

}
