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
package emlab.gen.domain.technology;

import org.neo4j.graphdb.Direction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

import emlab.gen.domain.gis.Zone;
import emlab.gen.trend.HourlyCSVTimeSeries;

@NodeEntity
public class PowerGridNode {

    @RelatedTo(type = "REGION", elementClass = Zone.class, direction = Direction.OUTGOING)
    private Zone zone;

    @RelatedTo(type = "HOURLYDEMAND", elementClass = HourlyCSVTimeSeries.class, direction = Direction.OUTGOING)
    private HourlyCSVTimeSeries hourlyDemand;

    private double capacityMultiplicationFactor;

    private double maximumCcsInNode;

    public double getMaximumCcsInNode() {
        return maximumCcsInNode;
    }

    public void setMaximumCcsInNode(double maximumCcsInNode) {
        this.maximumCcsInNode = maximumCcsInNode;
    }

    public HourlyCSVTimeSeries getHourlyDemand() {
        return hourlyDemand;
    }

    public void setHourlyDemand(HourlyCSVTimeSeries hourlydemand) {
        this.hourlyDemand = hourlydemand;
    }

    public void setZone(Zone zone) {
        this.zone = zone;
    }

    public Zone getZone() {
        return zone;
    }

    @Value("1.0")
    public double getCapacityMultiplicationFactor() {
        return capacityMultiplicationFactor;
    }

    public void setCapacityMultiplicationFactor(double capacityMultiplicationFactor) {
        this.capacityMultiplicationFactor = capacityMultiplicationFactor;
    }

}
