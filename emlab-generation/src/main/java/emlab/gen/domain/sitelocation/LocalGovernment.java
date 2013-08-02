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
package emlab.gen.domain.sitelocation;

import org.springframework.data.neo4j.annotation.NodeEntity;

/**
 * Representation of a location
 * 
 * @author jpaling
 * 
 */

@NodeEntity
public class LocalGovernment {

    private String name;

    private double weightEnvironment;

    private double weightEmployment;

    private double weightCompensation;

    private double weightPrevious;

    private double effectivenessCompensationGov;

    public double getEffectivenessCompensationGov() {
        return effectivenessCompensationGov;
    }

    public void setEffectivenessCompensationGov(double effectivenessCompensationGov) {
        this.effectivenessCompensationGov = effectivenessCompensationGov;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getWeightEnvironment() {
        return weightEnvironment;
    }

    public void setWeightEnvironment(double weightEnvironment) {
        this.weightEnvironment = weightEnvironment;
    }

    public double getWeightEmployment() {
        return weightEmployment;
    }

    public void setWeightEmployment(double weightEmployment) {
        this.weightEmployment = weightEmployment;
    }

    public double getWeightCompensation() {
        return weightCompensation;
    }

    public void setWeightCompensation(double weightCompensation) {
        this.weightCompensation = weightCompensation;
    }

    public double getWeightPrevious() {
        return weightPrevious;
    }

    public void setWeightPrevious(double weightPrevious) {
        this.weightPrevious = weightPrevious;
    }

}
