/*******************************************************************************
 * Copyright 2013 the original author or authors.
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
package emlab.gen.domain.factory;

import java.io.InputStreamReader;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.annotation.Transactional;

import com.googlecode.jcsv.CSVStrategy;
import com.googlecode.jcsv.reader.CSVReader;
import com.googlecode.jcsv.reader.internal.CSVReaderBuilder;

import emlab.gen.domain.agent.EnergyProducer;
import emlab.gen.domain.technology.PowerGeneratingTechnology;
import emlab.gen.domain.technology.PowerGridNode;
import emlab.gen.domain.technology.PowerPlant;

/**
 * @author JCRichstein
 *
 */
public class PowerPlantCSVFactory implements InitializingBean {

    String csvFile;

    static final Logger logger = LoggerFactory.getLogger(PowerPlantCSVFactory.class);

    private List<EnergyProducer> producers;

    private List<PowerGeneratingTechnology> technologies;

    private List<PowerGridNode> powerGridNodes;

    @Transactional
    @Override
    public void afterPropertiesSet() throws Exception {
        logger.warn("Reading power plant from CSV file: " + csvFile);
        InputStreamReader inputStreamReader = new InputStreamReader(this.getClass().getResourceAsStream(csvFile));

        CSVReader<PowerPlant> csvPersonReader = new CSVReaderBuilder<PowerPlant>(inputStreamReader).entryParser(
                new PowerPlantEntryParser(producers, technologies, powerGridNodes))
                .strategy(new CSVStrategy(',', '\"', '#', true, true))
                .build();
        List<PowerPlant> powerplants = csvPersonReader.readAll();
    }

    public String getCsvFile() {
        return csvFile;
    }

    public void setCsvFile(String csvFile) {
        this.csvFile = csvFile;
    }

    public List<EnergyProducer> getProducers() {
        return producers;
    }

    public void setProducers(List<EnergyProducer> producers) {
        this.producers = producers;
    }

    public List<PowerGeneratingTechnology> getTechnologies() {
        return technologies;
    }

    public void setTechnologies(List<PowerGeneratingTechnology> technologies) {
        this.technologies = technologies;
    }

    public List<PowerGridNode> getPowerGridNodes() {
        return powerGridNodes;
    }

    public void setPowerGridNodes(List<PowerGridNode> powerGridNodes) {
        this.powerGridNodes = powerGridNodes;
    }

}
