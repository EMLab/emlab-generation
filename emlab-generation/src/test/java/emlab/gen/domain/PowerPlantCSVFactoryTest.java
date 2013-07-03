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
package emlab.gen.domain;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.template.Neo4jOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import emlab.gen.domain.agent.EnergyProducer;
import emlab.gen.domain.factory.PowerPlantCSVFactory;
import emlab.gen.domain.technology.PowerGeneratingTechnology;
import emlab.gen.domain.technology.PowerGridNode;
import emlab.gen.domain.technology.PowerPlant;
import emlab.gen.repository.Reps;
import emlab.gen.trend.TimeSeriesImpl;
//import emlab.gen.domain.agent.Person;

/**
 * @author JCRichstein
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/emlab-gen-test-context.xml"})
@Transactional
public class PowerPlantCSVFactoryTest {

    Logger logger = Logger.getLogger(PowerPlantCSVFactoryTest.class);

    @Autowired Neo4jOperations template;

    @Autowired
    Reps reps;

    @Before
    @Transactional
    public void setUp() throws Exception {



    }

    @Test
    public void testCSVReader() {

        PowerGridNode nodeDE = new PowerGridNode();
        nodeDE.setName("de");
        nodeDE.setCapacityMultiplicationFactor(1.0);
        nodeDE.persist();

        PowerGridNode nodeNL = new PowerGridNode();
        nodeNL.setName("nl");
        nodeNL.setCapacityMultiplicationFactor(1.0);
        nodeNL.persist();

        EnergyProducer aon = new EnergyProducer();
        aon.setName("aon");
        aon.persist();

        EnergyProducer swe = new EnergyProducer();
        swe.setName("swe");
        swe.persist();

        PowerGeneratingTechnology coalPGT = new PowerGeneratingTechnology();
        coalPGT.setName("coalPGT");
        coalPGT.setExpectedLeadtime(1);
        coalPGT.setExpectedPermittime(1);
        coalPGT.setCapacity(500.0);
        TimeSeriesImpl investmentCostSeries = new TimeSeriesImpl();
        double[] invArray = { 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000,
                1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000,
                1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000,
                1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000 };
        investmentCostSeries.setTimeSeries(invArray);
        investmentCostSeries.setStartingYear(-30);
        investmentCostSeries.persist();
        coalPGT.setInvestmentCostTimeSeries(investmentCostSeries);
        TimeSeriesImpl efficiencyTimeSeries = new TimeSeriesImpl();
        double[] effArray = { 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30,
                0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30,
                0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30,
                0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30 };
        efficiencyTimeSeries.setTimeSeries(effArray);
        efficiencyTimeSeries.setStartingYear(-30);
        efficiencyTimeSeries.persist();
        coalPGT.setEfficiencyTimeSeries(efficiencyTimeSeries);
        TimeSeriesImpl fixedOMTimeSeries = new TimeSeriesImpl();
        double[] fixedOMTimeArr = { 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30,
                0.30,
                0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30,
                0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30,
                0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30 };
        fixedOMTimeSeries.setTimeSeries(fixedOMTimeArr);
        fixedOMTimeSeries.setStartingYear(-30);
        fixedOMTimeSeries.persist();
        coalPGT.setFixedOperatingCostTimeSeries(fixedOMTimeSeries);
        coalPGT.persist();

        PowerGeneratingTechnology gasPGT = new PowerGeneratingTechnology();
        gasPGT.setName("gasPGT");
        gasPGT.setExpectedLeadtime(1);
        gasPGT.setExpectedPermittime(1);
        gasPGT.setCapacity(300);
        gasPGT.setFixedOperatingCostTimeSeries(fixedOMTimeSeries);
        gasPGT.setEfficiencyTimeSeries(efficiencyTimeSeries);
        gasPGT.setInvestmentCostTimeSeries(investmentCostSeries);
        gasPGT.persist();

        logger.warn(reps.powerGeneratingTechnologyRepository.findAll().iterator().next());

        PowerPlantCSVFactory ppCsvFactory = new PowerPlantCSVFactory();
        ppCsvFactory.setCsvFile("/data/jUnitpowerPlantList.csv");
        EnergyProducer[] energyProducers = { aon, swe };
        PowerGeneratingTechnology[] technologies = {coalPGT, gasPGT };
        PowerGridNode[] nodes = { nodeDE, nodeNL };
        ppCsvFactory.setProducers(new ArrayList<EnergyProducer>(java.util.Arrays.asList(energyProducers)));
        ppCsvFactory.setPowerGridNodes(new ArrayList<PowerGridNode>(java.util.Arrays.asList(nodes)));
        ppCsvFactory.setTechnologies(new ArrayList<PowerGeneratingTechnology>(java.util.Arrays.asList(technologies)));
        try {
            ppCsvFactory.afterPropertiesSet();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            logger.warn("Failed.");
            e.printStackTrace();
        }
        logger.warn("All PowerPlants:");
        Iterable<PowerPlant> powerPlants = reps.powerPlantRepository.findAll();
        for(PowerPlant plant :powerPlants){
            logger.warn(plant.getName() + "," + plant.getOwner().getName() + "," + plant.getTechnology().getName()
                    + "," + plant.getLocation().getName() + "," + plant.getConstructionStartTime() + ","
                    + (plant.getConstructionStartTime() + plant.getActualLeadtime() + plant.getActualPermittime()));
        }

    }

}
