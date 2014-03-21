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
package emlab.gen.domain.market.electricity;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

import emlab.gen.domain.market.ClearingPoint;

@NodeEntity
public class SegmentClearingPoint extends ClearingPoint {

    @RelatedTo(type = "SEGMENT_POINT", elementClass = Segment.class, direction = Direction.OUTGOING)
    private Segment segment;

    public Segment getSegment() {
        return segment;
    }

    public void setSegment(Segment segment) {
        this.segment = segment;
    }

    /**
     * The interconnector flow is specified as a source of electricity from the
     * point of view of the market that the segment clearing point belongs to. A
     * positive value means that the market is importing electricity, a negative
     * value mean that it is exporting it.
     */
    double interconnectorFlow;

    public double getInterconnectorFlow() {
        return interconnectorFlow;
    }

    public void setInterconnectorFlow(double interconnectorFlow) {
        this.interconnectorFlow = interconnectorFlow;
    }
}
