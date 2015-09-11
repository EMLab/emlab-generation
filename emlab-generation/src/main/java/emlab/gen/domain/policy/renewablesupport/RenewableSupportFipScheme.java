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
package emlab.gen.domain.policy.renewablesupport;

import java.util.HashMap;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

import agentspring.simulation.SimulationParameter;
import emlab.gen.domain.agent.Regulator;
import emlab.gen.domain.technology.PowerGeneratingTechnology;

/**
 * @author Kaveri3012 A generic renewable support scheme role, meant to be able
 *         to model both price based and quantity based schemes.
 */
@NodeEntity
public class RenewableSupportFipScheme {

    @RelatedTo(type = "WITH_REGULATOR", elementClass = Regulator.class, direction = Direction.OUTGOING)
    private Regulator regulator;

    @RelatedTo(type = "TECHNOLOGIES_ELIGIBLE_ARE", elementClass = PowerGeneratingTechnology.class, direction = Direction.OUTGOING)
    private Set<PowerGeneratingTechnology> powerGeneratingTechnologiesEligible;

    private boolean technologySpecificityEnabled;

    private boolean locationSpecificityEnabled;

    @SimulationParameter(label = "Support Scheme Duration", from = 0, to = 50)
    private long supportSchemeDuration;

    private String name;

    // Feed-in-Premium Related Pàrameters

    private HashMap<PowerGeneratingTechnology, Double> biasFactors;

    public HashMap<PowerGeneratingTechnology, Double> getBiasFactors() {
        return biasFactors;
    }

    public void setBiasFactors(HashMap<PowerGeneratingTechnology, Double> biasFactors) {
        this.biasFactors = biasFactors;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<PowerGeneratingTechnology> getPowerGeneratingTechnologiesEligible() {
        return powerGeneratingTechnologiesEligible;
    }

    public void setPowerGeneratingTechnologiesEligible(
            Set<PowerGeneratingTechnology> powerGeneratingTechnologiesEligible) {
        this.powerGeneratingTechnologiesEligible = powerGeneratingTechnologiesEligible;
    }

    public Regulator getRegulator() {
        return regulator;
    }

    public void setRegulator(Regulator regulator) {
        this.regulator = regulator;
    }

    public boolean isTechnologySpecificityEnabled() {
        return technologySpecificityEnabled;
    }

    public void setTechnologySpecificityEnabled(boolean technologySpecificityEnabled) {
        this.technologySpecificityEnabled = technologySpecificityEnabled;
    }

    public boolean isLocationSpecificityEnabled() {
        return locationSpecificityEnabled;
    }

    public void setLocationSpecificityEnabled(boolean locationSpecificityEnabled) {
        this.locationSpecificityEnabled = locationSpecificityEnabled;
    }

    public long getSupportSchemeDuration() {
        return supportSchemeDuration;
    }

    public void setSupportSchemeDuration(long supportSchemeDuration) {
        this.supportSchemeDuration = supportSchemeDuration;
    }

}
