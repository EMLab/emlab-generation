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

import java.io.IOException;
import java.util.List;

import org.codehaus.groovy.syntax.ReadException;
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
 * The power plant entry parser, takes rows of a CSV table and turns it into
 * power plants in the database when the simulation starts.
 * 
 * The columns of the table need to be defined in the following order:
 * 
 * Name|TechnologyName|LocationName|Age|OwnerName|Capacity|Efficiency
 * 
 * and column headers should be given.
 * 
 * TechnologyName (of class PowerGeneratingTechnology), OwnerName (of class
 * EnergyProducer) and LocationName (of class PowerGridNode) need to correspond
 * exactly to the names defined in the scenario file.
 * 
 * The entries of the columns OwnerName, Capacity and Efficiency may be left
 * empty. In this case the owner is randomly assigned, the capacity set to the
 * standard capacity times the locational capacity factor, and the efficiency is
 * calculated from the age of the power plant and the learning curve of the
 * technology. The columns OwnerName, Capacity, and Efficiency maybe left away
 * entirely (but only if the columns to the right are also left away).
 * 
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
        String locationName = data[2];
        int age = Integer.parseInt(data[3]);
        String ownerName = "";
        if (data.length > 3)
            ownerName = data[4];

        double capacity = 0;
        if (data.length > 4 && !data[5].isEmpty())
            capacity = Double.parseDouble(data[5]);
        double efficiency = 0;
        if (data.length > 5 && !data[6].isEmpty()) {
            efficiency = Double.parseDouble(data[6]);
        }

        EnergyProducer energyProducer = null;
        if (!ownerName.isEmpty()) {
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
        if (!technologyName.isEmpty()) {
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
        if (!locationName.isEmpty()) {
            for (PowerGridNode node : powerGridNodes) {
                if (node.getName().equals(locationName)) {
                    powerGridNode = node;
                    break;
                }

            }
        } else {
            try {
                throw new ReadException("Location fields is not allowed to be empty!", new IOException());
            } catch (ReadException e) {
                e.printStackTrace();
            }
        }
        return createPowerPlant(name, pgt, energyProducer, powerGridNode, age, capacity, efficiency);
    }

    private PowerPlant createPowerPlant(String name, PowerGeneratingTechnology technology,
            EnergyProducer energyProducer,
            PowerGridNode location, int age, double capacity, double efficiency) {
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
        if (capacity == 0) {
            plant.setActualNominalCapacity(technology.getCapacity() * location.getCapacityMultiplicationFactor());
        } else {
            plant.setActualNominalCapacity(capacity);
        }

        plant.calculateAndSetActualInvestedCapital(plant.getConstructionStartTime());
        if (efficiency == 0) {
            plant.calculateAndSetActualEfficiency(plant.getConstructionStartTime());
        } else {
            plant.setActualEfficiency(efficiency);
        }
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
