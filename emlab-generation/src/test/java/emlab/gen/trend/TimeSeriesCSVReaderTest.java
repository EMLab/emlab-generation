package emlab.gen.trend;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author JCRichstein
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/emlab-gen-test-context.xml" })
@Transactional
public class TimeSeriesCSVReaderTest {

	Logger logger = Logger.getLogger(TimeSeriesCSVReaderTest.class);

	@Test
	public void testReadingOfValues() {
		TimeSeriesCSVReader tscr = new TimeSeriesCSVReader();
		tscr.setDelimiter(",");
		tscr.setVariableName("gasPrice");
		tscr.setFilename("/data/exampleMultipleTimeSeries.csv");
		logger.warn(tscr.getValue(0));

	}

}
