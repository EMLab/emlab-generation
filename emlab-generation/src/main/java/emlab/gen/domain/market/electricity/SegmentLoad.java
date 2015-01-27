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

@NodeEntity
public class SegmentLoad {

    @RelatedTo(type = "SEGMENTLOAD_SEGMENT", elementClass = Segment.class, direction = Direction.OUTGOING)
    private Segment segment;

    @RelatedTo(type = "SEGMENT_LOAD", elementClass = ElectricitySpotMarket.class, direction = Direction.INCOMING)
    private ElectricitySpotMarket electricitySpotMarket;

    private double baseLoad;

    private double currentLoad;

    public Segment getSegment() {
        return segment;
    }

    public void setSegment(Segment segment) {
        this.segment = segment;
    }

    public ElectricitySpotMarket getElectricitySpotMarket() {
        return electricitySpotMarket;
    }

    public void setElectricitySpotMarket(ElectricitySpotMarket electricitySpotMarket) {
        this.electricitySpotMarket = electricitySpotMarket;
    }

    public double getBaseLoad() {
        return baseLoad;
    }

    public void setBaseLoad(double baseLoad) {
        this.baseLoad = baseLoad;
    }


    public double getCurrentLoad() {
        return currentLoad;
    }

    public void setCurrentLoad(double currentLoad) {
        this.currentLoad = currentLoad;
    }

    @Override
    public String toString() {
        return "segment: " + segment + " load: " + getBaseLoad();
    }

}
