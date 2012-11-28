package emlab.role.enipedia;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import agentspring.role.AbstractRole;
import agentspring.role.Role;
import agentspring.role.ScriptComponent;
import emlab.domain.agent.DecarbonizationModel;
import emlab.domain.agent.EnergyProducer;
import emlab.domain.contract.Loan;
import emlab.domain.enipedia.CountryEnipedia;
import emlab.domain.enipedia.FuelEnipedia;
import emlab.domain.enipedia.PowerGridNodeEnipedia;
import emlab.domain.enipedia.PowerPlantEnipedia;
import emlab.domain.technology.PowerGeneratingTechnology;
import emlab.domain.technology.PowerPlant;
import emlab.domain.technology.Substance;
import emlab.repository.Reps;

@ScriptComponent(first = true, end = 0)
public class PowerPlantEnipediaRole extends AbstractRole<DecarbonizationModel> implements Role<DecarbonizationModel> {

    static final Logger logger = LoggerFactory.getLogger(PowerPlantEnipediaRole.class);

    @Autowired
    Reps reps;

    @Transactional
    public void act(DecarbonizationModel agent) {
        for (PowerGridNodeEnipedia nodeEnipedia : reps.genericRepository.findAll(PowerGridNodeEnipedia.class)) {
            if (nodeEnipedia.getZone() == null) {
                for (CountryEnipedia country : reps.genericRepository.findAll(CountryEnipedia.class)) {
                    if (nodeEnipedia.getName() == country.getName()) {
                        nodeEnipedia.setZone(country);
                    }
                }
            }
        }
        inferPropertiesPowerPlant();
    }

    @Transactional
    private void inferPropertiesPowerPlant() {

        for (PowerPlantEnipedia plantEnipedia : reps.powerPlantRepository.findAllPowerPlantsEnipedia()) {
            logger.warn("Found plant {} from Enipedia", plantEnipedia);

            PowerPlant plant = new PowerPlant().persist();

            // Name
            plant.setName(plantEnipedia.getName() + "-inferred");

            // Technology
            plant.setTechnology(getTechnologyForFuel(plantEnipedia.getPrimaryFuel()));

            // Owner
            plant.setOwner(getRandomProducer());

            // Location
            plant.setLocation(plantEnipedia.getCountry());

            // Year built
            plant.setConstructionStartTime(determineConstructionStartTime(plantEnipedia.getYearBuilt(), plant));

            // Standard times
            plant.setActualLeadtime(plant.getTechnology().getExpectedLeadtime());
            plant.setActualPermittime(plant.getTechnology().getExpectedPermittime());
            plant.setExpectedEndOfLife(plant.getConstructionStartTime() + plant.getActualPermittime()
                    + plant.getActualLeadtime() + plant.getTechnology().getExpectedLifetime());
            plant.calculateAndSetActualInvestedCapital(plant.getConstructionStartTime());
            plant.setDismantleTime(1000);

            // Loan
            Loan loan = new Loan().persist();
            loan.setFrom(plant.getOwner());
            loan.setTo(null);
            double amountPerPayment = determineLoanAnnuities(plant.getActualInvestedCapital()
                    * plant.getOwner().getDebtRatioOfInvestments(), plant.getTechnology().getDepreciationTime(), plant
                    .getOwner().getLoanInterestRate());
            loan.setAmountPerPayment(amountPerPayment);
            loan.setTotalNumberOfPayments(plant.getTechnology().getDepreciationTime());
            loan.setLoanStartTime(plant.getConstructionStartTime());
            loan.setNumberOfPaymentsDone(-plant.getConstructionStartTime());
            plant.setLoan(loan);
        }
    }

    private long determineConstructionStartTime(Date yearBuilt, PowerPlant plant) {
        if (yearBuilt != null && yearBuilt.getYear() != 0) {
            return yearBuilt.getYear();
        } else {
            return -(plant.getTechnology().getExpectedLeadtime() + plant.getTechnology().getExpectedPermittime() + Math
                    .round((Math.random() * plant.getTechnology().getExpectedLifetime()))) + 2;
        }
    }

    private EnergyProducer getRandomProducer() {
        return reps.genericRepository.findAllAtRandom(EnergyProducer.class).iterator().next();
    }

    private PowerGeneratingTechnology getTechnologyForFuel(FuelEnipedia fuel) {
        Substance fuelSubstance = null;
        for (Substance substance : reps.genericRepository.findAll(Substance.class)) {
            if (substance.getName() == fuel.getName()) {
                fuelSubstance = substance;
            }
        }
        for (PowerGeneratingTechnology tech : reps.powerGeneratingTechnologyRepository.findAll()) {
            if (tech.getFuels().contains(fuelSubstance)) {
                return tech;
            }
        }
        // can't find it, so just take one.
        return reps.genericRepository.findAllAtRandom(PowerGeneratingTechnology.class).iterator().next();
    }

    public double determineLoanAnnuities(double totalLoan, double payBackTime, double interestRate) {

        double q = 1 + interestRate;
        double annuity = totalLoan * (Math.pow(q, payBackTime) * (q - 1)) / (Math.pow(q, payBackTime) - 1);

        return annuity;
    }

}
