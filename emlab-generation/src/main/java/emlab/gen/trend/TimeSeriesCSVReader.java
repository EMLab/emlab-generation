/**
 * 
 */
package emlab.gen.trend;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reads time series from a CSV file. Formatting must correspond to a format
 * where each row contains one time series and the first column contains the
 * variable names.
 * 
 * Alternatively, if no {@link variableName} is given, it reads a CSV file with
 * a single column, in which each row contains a single value.
 * 
 * @author JCRichstein
 * 
 */
@NodeEntity
public class TimeSeriesCSVReader extends TimeSeriesImpl {

    Logger logger = LoggerFactory.getLogger(TimeSeriesCSVReader.class);

    private String filename;

	private String delimiter;

	private String variableName;

    private void readSingleColumn() {

		logger.warn("Trying to read single column CSV file: " + filename);

		String data = new String();

        // Save the data in a long String
        try {

            InputStreamReader inputStreamReader = new InputStreamReader(this.getClass().getResourceAsStream(filename));
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String line;
			int lineCounter = 0;
            while ((line = bufferedReader.readLine()) != null) {
                data = data.concat(line + ",");
				lineCounter++;
            }
            bufferedReader.close();
			double[] timeSeries = new double[lineCounter];
			int i = 0;
			for (String s : data.split("[,]")) {
				timeSeries[i] = Double.parseDouble(s);
				i++;
			}
			setTimeSeries(timeSeries);

        } catch (Exception e) {
            logger.error("Couldn't read CSV file: " + filename);
            e.printStackTrace();
        }
        this.persist();
    }

	@Transactional
	private void readVariableFromCSV() {
		logger.warn("Trying to read variable " + variableName + " from CSV file: " + filename + " with delimiter "
				+ delimiter);

		// Save the data in a long String
		try {

			InputStreamReader inputStreamReader = new InputStreamReader(this.getClass().getResourceAsStream(filename));
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

			String line;
			String[] lineContentSplit = null;
			while ((line = bufferedReader.readLine()) != null) {
				if (line.startsWith(variableName)) {
					lineContentSplit = line.split(delimiter);
					break;
				}
			}
			bufferedReader.close();
			double[] timeSeries = new double[lineContentSplit.length - 1];
			int i = 0;
			for (String s : lineContentSplit) {
				if (i > 0)
					timeSeries[i - 1] = Double.parseDouble(s);
				i++;
			}
			setTimeSeries(timeSeries);
			this.persist();

		} catch (Exception e) {
			logger.error("Couldn't read CSV file: " + filename);
			e.printStackTrace();
		}

	}

	@Override
	public double getValue(long time) {
		if (getTimeSeries() == null)
			if (variableName != null)
				readVariableFromCSV();
			else
				readSingleColumn();
		return super.getValue(time);
	}

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

	public String getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	public String getVariableName() {
		return variableName;
	}

	public void setVariableName(String variable) {
		this.variableName = variable;
	}

}
