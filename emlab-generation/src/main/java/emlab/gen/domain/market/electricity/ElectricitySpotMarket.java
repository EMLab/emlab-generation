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

import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

import emlab.gen.domain.market.DecarbonizationMarket;
import emlab.gen.trend.TimeSeriesImpl;

@NodeEntity
public class ElectricitySpotMarket extends DecarbonizationMarket {

    @RelatedTo(type = "SEGMENT_LOAD", elementClass = SegmentLoad.class, direction = Direction.OUTGOING)
    private Set<SegmentLoad> loadDurationCurve;

	@RelatedTo(type = "DEMANDGROWTH_TREND", elementClass = TimeSeriesImpl.class, direction = Direction.OUTGOING)
	private TimeSeriesImpl demandGrowthTrend;

    private double valueOfLostLoad;

    public Set<SegmentLoad> getLoadDurationCurve() {
        return loadDurationCurve;
    }

    public void setLoadDurationCurve(Set<SegmentLoad> loadDurationCurve) {
        this.loadDurationCurve = loadDurationCurve;
    }

    public double getValueOfLostLoad() {
        return valueOfLostLoad;
    }

    public void setValueOfLostLoad(double valueOfLostLoad) {
        this.valueOfLostLoad = valueOfLostLoad;
    }

	public TimeSeriesImpl getDemandGrowthTrend() {
        return demandGrowthTrend;
    }

	public void setDemandGrowthTrend(TimeSeriesImpl demandGrowthTrend) {
        this.demandGrowthTrend = demandGrowthTrend;
    }

}
