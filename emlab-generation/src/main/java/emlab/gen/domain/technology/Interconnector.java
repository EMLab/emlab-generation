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

import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.springframework.transaction.annotation.Transactional;

import emlab.gen.trend.TimeSeriesImpl;

@NodeEntity
public class Interconnector {

    @RelatedTo(type = "INTERCONNECTIONS", elementClass = PowerGridNode.class, direction = Direction.OUTGOING)
    // TODO: Limit the set to the size of two.
    private Set<PowerGridNode> connections;

    @RelatedTo(type = "INTERCONNECTOR_CAPACITY_TREND", elementClass = TimeSeriesImpl.class, direction = Direction.OUTGOING)
    private TimeSeriesImpl interconnectorCapacityTrend;

    public Set<PowerGridNode> getConnections() {
        return connections;
    }

    public void setConnections(Set<PowerGridNode> connections) {
        this.connections = connections;
    }

    public void setInterconnectorCapacityTrend(TimeSeriesImpl interconnectorCapacityTrend) {
        this.interconnectorCapacityTrend = interconnectorCapacityTrend;
    }

    public TimeSeriesImpl getInterconnectorCapacityTrend() {
        return this.interconnectorCapacityTrend;
    }

    public double getCapacity(long time) {
        return getInterconnectorCapacityTrend().getValue(time);
    }


    public void setCapacity(long time, double capacity) {
        interconnectorCapacityTrend.setValue(time, capacity);
    }

    @Transactional
    public void updateCapacity(long time, double capacity) {
        setCapacity(time, capacity);
    }

    @Transactional
    public void setCapacity(double capacity) {
        TimeSeriesImpl interconnectorCapacityTrend = new TimeSeriesImpl().persist();
        interconnectorCapacityTrend.setStartingYear(0);
        double[] interconnectorCapacityTrendTimeSeries = new double[300];
        for (int i = 0; i < interconnectorCapacityTrendTimeSeries.length; i++) {
            interconnectorCapacityTrendTimeSeries[i] = capacity;
        }
        interconnectorCapacityTrend.setTimeSeries(interconnectorCapacityTrendTimeSeries);
        setInterconnectorCapacityTrend(interconnectorCapacityTrend);
    }

}