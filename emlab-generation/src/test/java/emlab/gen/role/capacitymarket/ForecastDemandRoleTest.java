/*******************************************************************************
` * Copyright 2013 the original author or authors.
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
package emlab.gen.role.capacitymarket;

import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import emlab.gen.domain.agent.Regulator;
import emlab.gen.domain.gis.Zone;
import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.domain.market.electricity.Segment;
import emlab.gen.domain.market.electricity.SegmentLoad;
import emlab.gen.repository.MarketRepository;
import emlab.gen.repository.Reps;
import emlab.gen.trend.TriangularTrend;

/**
 * @author Kaveri
 * 
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/emlab-gen-test-context.xml" })
@Transactional
public class ForecastDemandRoleTest {
    Logger logger = Logger.getLogger(ForecastDemandRole.class);

    @Autowired
    Reps reps;

    @Autowired
    MarketRepository marketRepository;

    @Autowired
    ForecastDemandRole fDemandRole;

    @Test
    public void checkForecastDemandFunctionality() {

        Zone zone = new Zone();
        zone.persist();
        Regulator regulator = new Regulator();
        regulator.setTargetPeriod(0);
        regulator.setReserveMargin(0.15);
        regulator.setNumberOfYearsLookingBackToForecastDemand(3);
        regulator.setZone(zone);
        regulator.persist();

        Segment S1 = new Segment();
        S1.setLengthInHours(20);
        S1.persist();

        Segment S2 = new Segment();
        S2.setLengthInHours(30);
        S2.persist();

        SegmentLoad SG1 = new SegmentLoad();
        SG1.setSegment(S2);
        SG1.setBaseLoad(2500);
        SG1.persist();
        // SegmentLoad SG2 = new SegmentLoad();
        // SG2.setSegment(S2);
        // SG2.setBaseLoad(2000);

        SegmentLoad SG3 = new SegmentLoad();
        SG3.setSegment(S1);
        SG3.setBaseLoad(3700);

        // SegmentLoad SG4 = new SegmentLoad();
        // SG4.setSegment(S1);
        // SG4.setBaseLoad(4000);

        // SG2.persist();
        SG3.persist();
        // SG4.persist();

        Set<SegmentLoad> segmentLoads1 = new HashSet<SegmentLoad>();
        segmentLoads1.add(SG1);
        segmentLoads1.add(SG3);
        //
        // TimeSeriesImpl demandGrowthTrend = new TimeSeriesImpl();
        // int lengthOfSeries = 50;
        // double[] timeSeries = new double[lengthOfSeries];
        // timeSeries[0] = 1;
        // for (int i = 1; i <= lengthOfSeries; i++) {
        // timeSeries[i] = timeSeries[i - 1] * 1.02;
        // }

        TriangularTrend demandGrowthTrend = new TriangularTrend();
        demandGrowthTrend.setMax(2);
        demandGrowthTrend.setMin(1);
        demandGrowthTrend.setStart(1);
        demandGrowthTrend.setTop(1);

        // demandGrowthTrend.setTimeSeries(timeSeries);
        // demandGrowthTrend.setStartingYear(0);
        demandGrowthTrend.persist();

        ElectricitySpotMarket market1 = new ElectricitySpotMarket();
        market1.setName("Market1");
        market1.setLoadDurationCurve(segmentLoads1);
        market1.setDemandGrowthTrend(demandGrowthTrend);
        market1.setZone(zone);
        market1.persist();

        fDemandRole.act(regulator);

        logger.warn("Target Demand for this tick: " + regulator.getRelativeRenewableTarget());
        assertTrue(regulator.getRelativeRenewableTarget() == 4255);
        // logger.warn("Target Demand for this tick: " +
        // fDemandRole.expectedDemandFactor);

    }
}
