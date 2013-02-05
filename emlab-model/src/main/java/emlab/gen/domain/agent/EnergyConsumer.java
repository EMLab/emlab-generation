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

import agentspring.agent.Agent;
import agentspring.simulation.SimulationParameter;

@NodeEntity
public class EnergyConsumer extends DecarbonizationAgent implements Agent {

    @SimulationParameter(label = "Maximum coverage fraction of long-term contracts", from = 0, to = 0.25)
    private double ltcMaximumCoverageFraction;

    @SimulationParameter(label = "Contract duration preference factor", from = 0, to = 1)
    private double contractDurationPreferenceFactor;

    @SimulationParameter(label = "Contract willingness to pay factor", from = 1, to = 2)
    private double contractWillingnessToPayFactor;

    public double getLtcMaximumCoverageFraction() {
        return ltcMaximumCoverageFraction;
    }

    public void setLtcMaximumCoverageFraction(double ltcMaximumCoverageFraction) {
        this.ltcMaximumCoverageFraction = ltcMaximumCoverageFraction;
    }

    public double getContractDurationPreferenceFactor() {
        return contractDurationPreferenceFactor;
    }

    public void setContractDurationPreferenceFactor(double contractDurationPreferenceFactor) {
        this.contractDurationPreferenceFactor = contractDurationPreferenceFactor;
    }

    public double getContractWillingnessToPayFactor() {
        return contractWillingnessToPayFactor;
    }

    public void setContractWillingnessToPayFactor(double contractWillingnessToPayFactor) {
        this.contractWillingnessToPayFactor = contractWillingnessToPayFactor;
    }
}
