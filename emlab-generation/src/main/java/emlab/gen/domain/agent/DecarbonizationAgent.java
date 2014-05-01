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

import org.springframework.data.neo4j.annotation.NodeEntity;

import agentspring.agent.AbstractAgent;

@NodeEntity
public class DecarbonizationAgent extends AbstractAgent {

    private double cash;
    private double co2Allowances;
    private String name;
    private double lastYearsCo2Allowances;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public double getCash() {
        return cash;
    }

    public void setCash(double cash) {
        this.cash = cash;
    }

    public double getCo2Allowances() {
        return co2Allowances;
    }

    public void setCo2Allowances(double co2Allowances) {
        this.co2Allowances = co2Allowances;
    }

    public double getLastYearsCo2Allowances() {
        return lastYearsCo2Allowances;
    }

    public void setLastYearsCo2Allowances(double lastYearsCo2Allowances) {
        this.lastYearsCo2Allowances = lastYearsCo2Allowances;
    }

    @Override
    public String toString() {
        return getName();
    }
}
