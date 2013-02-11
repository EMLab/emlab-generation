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

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.template.Neo4jOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import emlab.gen.trend.TriangularTrend;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/emlab-gen-test-context.xml" })
@Transactional
public class TriangularTrendTest {

	Logger logger = Logger.getLogger(GeometricTrendRegressionTest.class);

	@Autowired
	Neo4jOperations template;

	@Test
	public void testTriangularTrendGeneration() {
		TriangularTrend tt = new TriangularTrend();
		tt.setStart(1);
		tt.setMax(1.03);
		tt.setMin(1.01);
		tt.setTop(1.02);
		tt.persist();
		double[][] triangularTrendAndForecast = new double[2][20];
		for (int i = 0; i < 20; i++) {
			triangularTrendAndForecast[0][i] = i;
			triangularTrendAndForecast[1][i] = tt.getValue(i);
		}
		for (int i = 19; i >= 0; i--) {
			assertEquals(triangularTrendAndForecast[1][i], tt.getValue(i), 0.0);
		}
	}

	@Test
	public void compareTrendToGeometricForecasting() {
		TriangularTrend tt = new TriangularTrend();
		tt.setStart(1);
		tt.setMax(1.00);
		tt.setMin(1.00);
		tt.setTop(1.00);
		tt.persist();
		double[][] triangularTrendAndForecast = new double[3][50];
		for (int i = 0; i < 50; i++) {
			triangularTrendAndForecast[0][i] = i;
			triangularTrendAndForecast[1][i] = tt.getValue(i);
		}
		int futureHorizon = 7;
		for (int futureTime = 2 + futureHorizon; futureTime < 50; futureTime++) {
			GeometricTrendRegression gtr = new GeometricTrendRegression();
			for (long time = futureTime - 5; time > futureTime - 10
					&& time >= 0; time = time - 1) {
				gtr.addData(time, tt.getValue(time));
				// logger.warn(time + "\t" + tt.getValue(time));
			}
			triangularTrendAndForecast[2][futureTime] = gtr.predict(futureTime);
		}

		for (int i = 9; i < 50; i++) {
			// logger.warn(triangularTrendAndForecast[0][i] + "\t" +
			// (triangularTrendAndForecast[2][i]-triangularTrendAndForecast[1][i])/triangularTrendAndForecast[1][i]);
			assertEquals(triangularTrendAndForecast[2][i]
					- triangularTrendAndForecast[1][i], 0.0, 0.00);
		}

	}

}
