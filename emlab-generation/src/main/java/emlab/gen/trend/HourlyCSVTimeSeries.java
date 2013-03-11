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

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author J.C. Richstein
 * 
 */
@NodeEntity
public class HourlyCSVTimeSeries implements HourlyTimeSeries {

	Logger logger = LoggerFactory.getLogger(HourlyCSVTimeSeries.class);

    private String filename;

    private double[] hourlyArray;

    @Transactional
    private void readData() {

        this.persist();
        logger.warn("Trying to read CSV file: " + filename);

        String data = new String();

        // Save the data in a long String
        try {

            InputStreamReader inputStreamReader = new InputStreamReader(this.getClass().getResourceAsStream(filename));
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String line;

            while ((line = bufferedReader.readLine()) != null) {
                data = data.concat(line + ",");
            }
            bufferedReader.close();

            String[] vals = data.split(",");
            setHourlyArray(parseString(vals), 0);

        } catch (Exception e) {
            logger.error("Couldn't read CSV file: " + filename);
            e.printStackTrace();
        }

    }

    /**
     * Parameter time specifies year of the hourly array. Currently not
     * implemented to make a difference.
     * 
     * @param time
     * @return
     */
    public double[] getHourlyArray(long time) {
        if (hourlyArray != null)
            try {
                return hourlyArray;
            } catch (Exception e) {
                logger.error("CSV File has wrong length (!= 8760 hours");
                e.printStackTrace();
            }
        else {
            readData();
            return hourlyArray;
        }
        return null;

    }

    public void setHourlyArray(double[] hourlyArray, long time) {
        this.hourlyArray = hourlyArray;
    }

    private double[] parseString(String[] vals) throws Exception {

        if (vals.length == 8760) {
            double[] doubleArrayData = new double[vals.length];
            for (int i = 0; i <= vals.length - 1; i++) {
                doubleArrayData[i] = Double.parseDouble(vals[i]);
            }
            return doubleArrayData;
        } else {
            throw new Exception();
        }
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

}
