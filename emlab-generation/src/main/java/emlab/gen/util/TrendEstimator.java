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
package emlab.gen.util;


import org.apache.commons.math.stat.regression.SimpleRegression;

public class TrendEstimator {

	
	public static double[] estimateLinearTrend(double[][] input, double[] predictionYears){
		;
		//Get logarithm of second trend
		SimpleRegression sr = new SimpleRegression();
		sr.addData(input);
		double result[] = new double[predictionYears.length];
		for(int i = 0 ; i<result.length; i++){
			result[i]=sr.predict(predictionYears[i]);
		}
		return result;
		
	}
	
	
	public static double[] estimateGeometricTrend(double[][] input, double[] predictionYears){
		//Get logarithm of second trend
		for(int i=0;i<input.length;i++){
			input[i][1]=Math.log(input[i][1]);
		}
		//input[1]=log;
		SimpleRegression sr = new SimpleRegression();
		sr.addData(input);
		double result[] = new double[predictionYears.length];
		for(int i = 0 ; i<result.length; i++){
			result[i]=Math.exp(sr.predict(predictionYears[i]));
		}
		return result;
		
	}

}
