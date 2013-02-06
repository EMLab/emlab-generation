/**
 * 
 */
package emlab.gen.trend;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.neo4j.annotation.NodeEntity;

import agentspring.trend.Trend;

/**
 * @author E.J.L. Chappin
 * 
 */
@NodeEntity
public class CSVDataTrend implements Trend {

    Logger logger = LoggerFactory.getLogger(CSVDataTrend.class);

    private String filename;

    private String data;

    private void readData() {

        logger.warn("Trying to read CSV file: " + filename);

        data = new String();

        // Save the data in a long String
        try {

            InputStreamReader inputStreamReader = new InputStreamReader(this.getClass().getResourceAsStream(filename));
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String line;

            while ((line = bufferedReader.readLine()) != null) {
                data = data.concat(line + ",");
            }
            bufferedReader.close();

        } catch (Exception e) {
            logger.error("Couldn't read CSV file: " + filename);
            e.printStackTrace();
        }
        this.persist();
    }

    @Override
    public double getValue(long time) {
        // Process the data and save it in the double array
        String data = getData();
        String[] vals = data.split(",");

        double[] doubleArrayData = new double[vals.length];
        for (int i = 0; i <= vals.length - 1; i++) {
            doubleArrayData[i] = Double.parseDouble(vals[i]);
        }
        return doubleArrayData[(int) time];
    }

    private String getData() {
        if (data != null) {
            return data;
        } else {
            readData();
            return data;
        }
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public double getStart() {
        return 0;
    }

    @Override
    public void setStart(double start) {
    }

}
