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
package emlab.gen.domain.factory;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.annotation.Transactional;

import emlab.gen.domain.agent.EnergyProducer;
import emlab.gen.domain.contract.Loan;
import emlab.gen.domain.gis.Zone;
import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.domain.market.electricity.SegmentLoad;
import emlab.gen.domain.technology.PowerGeneratingTechnology;
import emlab.gen.domain.technology.PowerGridNode;
import emlab.gen.domain.technology.PowerPlant;

public class ElectricityProducerFactory implements InitializingBean {

    private double capacityMargin;

    private Map<PowerGeneratingTechnology, Double> portfolioShares = null;

    private Set<PowerGridNode> nodes;

    private ElectricitySpotMarket market;

    private List<EnergyProducer> producers;

    static final Logger logger = LoggerFactory.getLogger(ElectricityProducerFactory.class);

    @Override
    public void afterPropertiesSet() throws Exception {
        createPowerPlantsForMarket(market);
    }

    @Transactional
    private PowerPlant createPowerPlant(PowerGeneratingTechnology technology, EnergyProducer energyProducer, PowerGridNode location) {
        PowerPlant plant = new PowerPlant().persist();
        String label = energyProducer.getName() + " - " + technology.getName();
        plant.setName(label);
        plant.setTechnology(technology);
        plant.setOwner(energyProducer);
        plant.setLocation(location);
        plant.setConstructionStartTime(-(technology.getExpectedLeadtime() + technology.getExpectedPermittime() + Math.round((Math.random() * technology
                .getExpectedLifetime()))) + 2); // TODO: Why include expected lead
                                                // time and permit time? Wouldn't it
                                                // be realistic to have some PP in
                                                // the pipeline at the start?
        plant.setActualLeadtime(plant.getTechnology().getExpectedLeadtime());
        plant.setActualPermittime(plant.getTechnology().getExpectedPermittime());
        plant.setExpectedEndOfLife(plant.getConstructionStartTime() + plant.getActualPermittime() + plant.getActualLeadTime()
                + plant.getTechnology().getExpectedLifetime());
		plant.setActualNominalCapacity(technology.getCapacity() * location.getCapacityMultiplicationFactor());
        plant.calculateAndSetActualInvestedCapital(plant.getConstructionStartTime());
        plant.calculateAndSetActualEfficiency(plant.getConstructionStartTime());
		plant.calculateAndSetActualFixedOperatingCosts(plant.getConstructionStartTime());
		plant.setDismantleTime(1000);
        Loan loan = new Loan().persist();
        loan.setFrom(energyProducer);
        loan.setTo(null);
        double amountPerPayment = determineLoanAnnuities(plant.getActualInvestedCapital() * energyProducer.getDebtRatioOfInvestments(),
                plant.getTechnology().getDepreciationTime(), energyProducer.getLoanInterestRate());
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

    private void createPowerPlantsForMarket(ElectricitySpotMarket market) {

        double maxLoad = Double.MIN_NORMAL;
        // get max load
        for (SegmentLoad segmentLoad : market.getLoadDurationCurve()) {

            if (maxLoad < segmentLoad.getBaseLoad()) {
                maxLoad = segmentLoad.getBaseLoad();
            }
        }
        double requiredCapacity = maxLoad * (1 + capacityMargin);
        logger.info("required capacity for market {} is {}", market, requiredCapacity);
        for (PowerGeneratingTechnology technology : portfolioShares.keySet()) {
            double pctValue = portfolioShares.get(technology);
            double requiredCapacityForTechnology = pctValue * requiredCapacity;
            logger.info("required capacity within this market for technology {} is {}", technology, requiredCapacityForTechnology);
            // logger.info("required capacity: {} for technology {} before creating",
            // requiredCapacityForTechnology, technology);
            while (requiredCapacityForTechnology > 0) {
                EnergyProducer energyProducer = getRandomProducer(producers);
                PowerPlant plant = createPowerPlant(technology, energyProducer, getNodeForZone(market.getZone()));
                requiredCapacityForTechnology -= plant.getAvailableCapacity(0);
            }
            // logger.info("required capacity: {} for technology {} after creating",
            // requiredCapacityForTechnology, technology);
        }

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

    private PowerGridNode getNodeForZone(Zone zone) {
        for (PowerGridNode node : nodes) {
            if (node.getZone().equals(zone)) {
                return node;
            }
        }
        return null;
    }

    public double getCapacityMargin() {
        return capacityMargin;
    }

    public void setCapacityMargin(double capacityMargin) {
        this.capacityMargin = capacityMargin;
    }

    public Map<PowerGeneratingTechnology, Double> getPortfolioShares() {
        return portfolioShares;
    }

    public void setPortfolioShares(Map<PowerGeneratingTechnology, Double> portfolioShares) {
        this.portfolioShares = portfolioShares;
    }

    public Set<PowerGridNode> getNodes() {
        return nodes;
    }

    public void setNodes(Set<PowerGridNode> nodes) {
        this.nodes = nodes;
    }

    public ElectricitySpotMarket getMarket() {
        return market;
    }

    public void setMarket(ElectricitySpotMarket market) {
        logger.info("setting market {}", market);
        this.market = market;
    }

    public List<EnergyProducer> getProducers() {
        return producers;
    }

    public void setProducers(List<EnergyProducer> producers) {
        this.producers = producers;
    }

    public double determineLoanAnnuities(double totalLoan, double payBackTime, double interestRate) {

        double q = 1 + interestRate;
        double annuity = totalLoan * (Math.pow(q, payBackTime) * (q - 1)) / (Math.pow(q, payBackTime) - 1);

        return annuity;
    }

}
