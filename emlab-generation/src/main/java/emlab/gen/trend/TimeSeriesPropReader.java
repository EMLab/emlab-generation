package emlab.gen.trend;

/**
 * Can read time series directly from XML or properties files. The time series
 * need to be comma separated.
 * 
 * @author JCRichstein
 * 
 */
public class TimeSeriesPropReader extends TimeSeriesImpl {
	
	private String timeSeriesStr;
		
//	public void setTimeSeriesStr(String timeSeriesStr) {
//		this.timeSeriesStr = timeSeriesStr;
//	}


	public String getTimeSeriesStr() {
		return timeSeriesStr;
	}
	
	public void setTimeSeriesStr(String timeSeriesStr) {
		// this.timeSeriesStr = timeSeriesStr;
        //we are going to read the values as a string array and set out integer array inside this setter
        String[] timeSeriesStrArray = timeSeriesStr.split(",");
		int i=0;
        double[] timeSeries = new double[timeSeriesStrArray.length];
        for(String s: timeSeriesStrArray){
        	timeSeries[i] = Double.parseDouble(s);
            i++;
        }
        setTimeSeries(timeSeries);
    }


}
