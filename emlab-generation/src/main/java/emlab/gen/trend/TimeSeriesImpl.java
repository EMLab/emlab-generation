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
package emlab.gen.trend;

import org.springframework.data.neo4j.annotation.NodeEntity;

import agentspring.trend.TimeSeries;



/**
 * @author JCRichstein
 *
 */
@NodeEntity
public class TimeSeriesImpl implements TimeSeries {

    /**
     * Index of double array corresponds to the tick, unless a
     * {@link startingYear} is defined to shift the index.
     */
    private double[] timeSeries;

    /**
     * Gives the starting year of the time series (probably a negative number) ,
     * is relevant for all implementations with an array.
     */
    private double startingYear;

    @Override
    public double getValue(long time) {
        return timeSeries[(int) time - (int) startingYear];
    }

    public void setValue(long time, double value) {
        timeSeries[(int) time - (int) startingYear] = value;
    }

    public double[] getTimeSeries() {
        return timeSeries;
    }

    public void setTimeSeries(double[] timeSeries) {
        this.timeSeries = timeSeries;
    }

    public double getStartingYear() {
        return startingYear;
    }

    public void setStartingYear(double startingYear) {
        this.startingYear = startingYear;
    }



}
