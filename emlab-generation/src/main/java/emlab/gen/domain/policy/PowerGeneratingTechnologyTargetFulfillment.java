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
package emlab.gen.domain.policy;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

import emlab.gen.domain.technology.PowerGeneratingTechnology;
import emlab.gen.domain.technology.PowerGridNode;
import emlab.gen.trend.TimeSeriesImpl;


/**
 * @author JCRichstein
 *
 */
@NodeEntity
public class PowerGeneratingTechnologyTargetFulfillment {

    /**
     * {@link powerGeneratingTechnology} defines the technology that the target applies to
     */
    @RelatedTo(type = "TARGETFULFILLMENT_TECHNOLOGY", elementClass = PowerGeneratingTechnology.class, direction = Direction.OUTGOING)
    PowerGeneratingTechnology powerGeneratingTechnology;

    /**
     * {@link trend} contains the target installed capacity of the technology
     * in MW.
     */
    @RelatedTo(type = "TARGETFULFILLMENT_TREND", elementClass = TimeSeriesImpl.class, direction = Direction.OUTGOING)
    TimeSeriesImpl trend;

    @RelatedTo(type = "INVESTOR_NODE", elementClass = PowerGridNode.class, direction = Direction.OUTGOING)
    private PowerGridNode specificPowerGridNode;

    public PowerGeneratingTechnology getPowerGeneratingTechnology() {
        return powerGeneratingTechnology;
    }

    public void setPowerGeneratingTechnology(
            PowerGeneratingTechnology powerGeneratingTechnology) {
        this.powerGeneratingTechnology = powerGeneratingTechnology;
    }

    public TimeSeriesImpl getTrend() {
        return trend;
    }

    public void setTrend(TimeSeriesImpl trend) {
        this.trend = trend;
    }

    public PowerGridNode getSpecificPowerGridNode() {
        return specificPowerGridNode;
    }

    public void setSpecificPowerGridNode(PowerGridNode specificPowerGridNode) {
        this.specificPowerGridNode = specificPowerGridNode;
    }

}
