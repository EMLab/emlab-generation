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
package emlab.gen.trend;

import org.springframework.data.neo4j.annotation.NodeEntity;

import agentspring.simulation.SimulationParameter;
import agentspring.trend.Trend;

@NodeEntity
public class StepTrend extends TimeSeriesImpl implements Trend {

    @SimulationParameter(label = "Time steps per step", from = 0, to = 50)
    private double duration;

    @SimulationParameter(label = "Increment per step")
    private double increment;

    private double minValue;

    @SimulationParameter(label = "Start value", from = 200e6, to =300e6)
    private double start;

    public double getDuration() {
        return duration;
    }

    public double getMinValue() {
        return minValue;
    }

    public void setMinValue(double minValue) {
        this.minValue = minValue;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public double getIncrement() {
        return increment;
    }

    public void setIncrement(double increment) {
        this.increment = increment;
    }

	@Override
    public double getValue(long time) {
        return Math.max(minValue, getStart() + Math.floor(time / duration) * increment);
    }

    public double getStart() {
        return start;
    }

    public void setStart(double start) {
        this.start = start;
    }

}
