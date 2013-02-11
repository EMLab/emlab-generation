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

/**
 * Implementation of SimpleRegression for geometric growth trends. Only the
 * methods addData, removeData and predict have been modified with the
 * Exponential function, all other methods must be adjusted manually!!
 * 
 * @author JCRichstein
 * 
 */
public class GeometricTrendRegression extends SimpleRegression {

	public void addData(double x, double y) {
		super.addData(x, Math.log(y));
	}

	public void removeData(double x, double y) {
		super.removeData(x, Math.log(y));
	}

	public void addData(double[][] data) {
		for (double[] d : data) {
			addData(d[0], d[1]);
		}
	}

	public void removeData(double[][] data) {
		for (int i = 0; i < data.length && super.getN() > 0; i++) {
			removeData(data[i][0], Math.log(data[i][1]));
		}
	}

	public double predict(double x) {
		return Math.exp(super.predict(x));
	}

}
