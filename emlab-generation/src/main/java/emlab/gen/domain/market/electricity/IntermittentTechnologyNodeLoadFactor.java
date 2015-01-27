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

import emlab.gen.domain.technology.PowerGeneratingTechnology;
import emlab.gen.domain.technology.PowerGridNode;

/**
 * @author jrichstein
 *
 */
@NodeEntity
public class IntermittentTechnologyNodeLoadFactor {

    @RelatedTo(type = "LOADFACTOR_TECHNOLOGY", elementClass = PowerGeneratingTechnology.class, direction = Direction.OUTGOING)
    private PowerGeneratingTechnology technology;

    @RelatedTo(type = "LOADFACTOR_NODE", elementClass = PowerGridNode.class, direction = Direction.OUTGOING)
    private PowerGridNode node;

    private double[] loadFactors;

    public PowerGeneratingTechnology getTechnology() {
        return technology;
    }

    public void setTechnology(PowerGeneratingTechnology technology) {
        this.technology = technology;
    }

    public PowerGridNode getNode() {
        return node;
    }

    public void setNode(PowerGridNode node) {
        this.node = node;
    }

    public double[] getLoadFactors() {
        return loadFactors;
    }

    public void setLoadFactors(double[] loadFactors) {
        this.loadFactors = loadFactors;
    }

    public double getLoadFactorForSegmentId(int segmentId) {
        return loadFactors[segmentId - 1];
    }

    public double getLoadFactorForSegment(Segment segment) {
        return getLoadFactorForSegmentId(segment.getSegmentID());
    }

    public void setLoadFactorForSegmentId(int segmentId, double loadFactor) {
        loadFactors[segmentId - 1] = loadFactor;
    }

    public void setLoadFactorForSegment(Segment segment, double loadFactor) {
        setLoadFactorForSegmentId(segment.getSegmentID(), loadFactor);
    }

}
