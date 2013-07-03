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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.googlecode.jcsv.reader.CSVEntryParser;

import emlab.gen.domain.agent.EnergyProducer;
import emlab.gen.domain.contract.Loan;
import emlab.gen.domain.technology.PowerGeneratingTechnology;
import emlab.gen.domain.technology.PowerGridNode;
import emlab.gen.domain.technology.PowerPlant;
import emlab.gen.repository.Reps;

/**
 * @author JCRichstein
 *
 */
public class PowerPlantEntryParser implements CSVEntryParser<PowerPlant> {

    private final List<EnergyProducer> producers;

    private final List<PowerGeneratingTechnology> technologies;

    private final List<PowerGridNode> powerGridNodes;

    /**
     * 
     */
    public PowerPlantEntryParser(List<EnergyProducer> producers, List<PowerGeneratingTechnology> technologies,
            List<PowerGridNode> powerGridNodes) {
        this.producers = producers;
        this.technologies = technologies;
        this.powerGridNodes = powerGridNodes;

    }

    @Autowired
    Reps reps;

    static final Logger logger = LoggerFactory.getLogger(PowerPlantEntryParser.class);

    @Override
    public PowerPlant parseEntry(String... data) {
        String name = data[0];
        String technologyName = data[1];
        String ownerName = data[2];
        String locationName = data[3];
        int age = Integer.parseInt(data[4]);
        logger.warn(data[1]);

        EnergyProducer energyProducer = null;
        if (ownerName != "") {
            for (EnergyProducer producer : producers) {
                if (producer.getName().equals(ownerName)) {
                    energyProducer = producer;
                    break;
                }

            }
        } else {
            energyProducer = getRandomProducer(producers);
        }
        PowerGeneratingTechnology pgt = null;
        if (ownerName != "") {
            for (PowerGeneratingTechnology ppTechnology : technologies) {
                if (ppTechnology.getName().equals(technologyName)) {
                    pgt = ppTechnology;
                    break;
                }

            }
        } else {
            pgt = technologies.get(0);
        }
        PowerGridNode powerGridNode = null;
        if (locationName != "") {
            for (PowerGridNode node : powerGridNodes) {
                if (node.getName().equals(locationName)) {
                    powerGridNode = node;
                    break;
                }

            }
        }
        return createPowerPlant(name, pgt, energyProducer, powerGridNode, age);
    }

    private PowerPlant createPowerPlant(String name, PowerGeneratingTechnology technology,
            EnergyProducer energyProducer,
            PowerGridNode location, int age) {
        PowerPlant plant = new PowerPlant().persist();
        plant.setName(name);
        plant.setTechnology(technology);
        plant.setOwner(energyProducer);
        plant.setLocation(location);
        plant.setConstructionStartTime(-(technology.getExpectedLeadtime() + technology.getExpectedPermittime() + age));
        plant.setActualLeadtime(plant.getTechnology().getExpectedLeadtime());
        plant.setActualPermittime(plant.getTechnology().getExpectedPermittime());
        plant.setExpectedEndOfLife(plant.getConstructionStartTime() + plant.getActualPermittime()
                + plant.getActualLeadtime() + plant.getTechnology().getExpectedLifetime());
        plant.setActualNominalCapacity(technology.getCapacity() * location.getCapacityMultiplicationFactor());
        plant.calculateAndSetActualInvestedCapital(plant.getConstructionStartTime());
        plant.calculateAndSetActualEfficiency(plant.getConstructionStartTime());
        plant.calculateAndSetActualFixedOperatingCosts(plant.getConstructionStartTime());
        plant.setDismantleTime(1000);
        Loan loan = new Loan().persist();
        loan.setFrom(energyProducer);
        loan.setTo(null);
        double amountPerPayment = determineLoanAnnuities(
                plant.getActualInvestedCapital() * energyProducer.getDebtRatioOfInvestments(), plant.getTechnology()
                .getDepreciationTime(), energyProducer.getLoanInterestRate());
        loan.setAmountPerPayment(amountPerPayment);
        loan.setTotalNumberOfPayments(plant.getTechnology().getDepreciationTime());
        loan.setLoanStartTime(plant.getConstructionStartTime());
        loan.setNumberOfPaymentsDone(-plant.getConstructionStartTime());// Some
        // payments
        // are
        // already
        // made
        plant.setLoan(loan);
        return plant;
    }

    private EnergyProducer getRandomProducer(List<EnergyProducer> producers) {
        if (producers.size() > 0) {
            int size = producers.size();
            int index = getRandomIndexFromList(size);
            return producers.get(index);
        }
        return null;
    }

    private int getRandomIndexFromList(int size) {
        return (int) Math.min(Math.floor(Math.random() * size), size - 1);
    }

    public double determineLoanAnnuities(double totalLoan, double payBackTime, double interestRate) {

        double q = 1 + interestRate;
        double annuity = totalLoan * (Math.pow(q, payBackTime) * (q - 1)) / (Math.pow(q, payBackTime) - 1);

        return annuity;
    }

}
