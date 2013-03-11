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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.transaction.annotation.Transactional;

import agentspring.simulation.SimulationParameter;
import agentspring.trend.Trend;
import cern.jet.random.Distributions;
import cern.jet.random.engine.RandomEngine;

@NodeEntity
public class TriangularTrend extends TimeSeriesImpl implements Trend {

    static final Logger logger = LoggerFactory.getLogger(TriangularTrend.class);

    @SimulationParameter(label = "Minimum growth factor per time step")
    private double min;

    @SimulationParameter(label = "Maximum growth factor per time step")
    private double max;

    @SimulationParameter(label = "Expected growth factor per time step")
    private double top;

    private String previousValues;
    private double start;

	@Override
    @Transactional
    public double getValue(long time) {

        int timeToCheck = (int) time;
        // If previous values not existing, make it and put starting value in.
        if (previousValues == null) {
            // Empty
            previousValues = "";
            previousValues += getStart();
            this.persist();
        }

        // Map existing
        String[] vals = previousValues.split(",");
        int lastFilled = vals.length - 1;
        double[] values = null;

        // Check what is bigger: what we already have or what we have to
        // generate?
        if (timeToCheck < lastFilled) {
            values = new double[lastFilled + 1];
        } else {
            values = new double[timeToCheck + 1];
        }

        for (int i = 0; i <= lastFilled; i++) {
            values[i] = Double.parseDouble(vals[i]);
        }

        // If value is not already existing
        if (timeToCheck >= lastFilled) {

            // Add new values
            for (int i = lastFilled + 1; i <= timeToCheck; i++) {
                double lastValue = 0;
                // don't try for element -1...
                if (i > 0) {
                    lastValue = values[i - 1];
                }
                double randomValue = Distributions.nextTriangular(RandomEngine.makeDefault());
                double translatedValue = 0d;
                if (randomValue < 0) {
                    translatedValue = top + (randomValue * (top - min));
                } else {
                    translatedValue = top + (randomValue * (max - top));
                }
                double newValue = lastValue * translatedValue;
                values[i] = newValue;
                previousValues += "," + newValue;
            }
            this.persist();
        }
        return values[timeToCheck];
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getTop() {
        return top;
    }

    public void setTop(double top) {
        this.top = top;
    }

    public double getStart() {
        return start;
    }

    public void setStart(double start) {
        this.start = start;
    }

}
