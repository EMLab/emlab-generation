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

import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

import agentspring.agent.Agent;
import emlab.gen.domain.policy.PowerGeneratingTechnologyTarget;

@NodeEntity
public class TargetInvestor extends EnergyProducer implements Agent {

    @RelatedTo(type="INVESTOR_TARGET",elementClass=PowerGeneratingTechnologyTarget.class, direction=Direction.OUTGOING)
    private Set<PowerGeneratingTechnologyTarget> powerGeneratingTechnologyTargets;

    public Set<PowerGeneratingTechnologyTarget> getPowerGenerationTechnologyTargets() {
        return powerGeneratingTechnologyTargets;
    }

    public void setPowerGenerationTechnologyTargets(
            Set<PowerGeneratingTechnologyTarget> powerGeneratingTechnologyTargets) {
        this.powerGeneratingTechnologyTargets = powerGeneratingTechnologyTargets;
    }

}
