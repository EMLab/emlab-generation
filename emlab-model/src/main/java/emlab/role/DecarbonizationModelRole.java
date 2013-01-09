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
package emlab.role;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.transaction.annotation.Transactional;

import agentspring.role.AbstractRole;
import agentspring.role.Role;
import agentspring.role.ScriptComponent;
import cern.colt.Timer;
import emlab.domain.agent.CommoditySupplier;
import emlab.domain.agent.DecarbonizationModel;
import emlab.domain.agent.EnergyConsumer;
import emlab.domain.agent.EnergyProducer;
import emlab.domain.agent.TargetInvestor;
import emlab.domain.market.CommodityMarket;
import emlab.domain.market.electricity.ElectricitySpotMarket;
import emlab.repository.Reps;
import emlab.role.investment.DismantlePowerPlantPastTechnicalLifetimeRole;
import emlab.role.investment.InvestInPowerGenerationTechnologiesRole;
import emlab.role.investment.RenewableTargetInvestmentRole;
import emlab.role.market.ClearCommodityMarketRole;
import emlab.role.market.ClearIterativeCO2AndElectricitySpotMarketTwoCountryRole;
import emlab.role.market.ProcessAcceptedBidsRole;
import emlab.role.market.ProcessAcceptedPowerPlantDispatchRole;
import emlab.role.market.ReassignPowerPlantsToLongTermElectricityContractsRole;
import emlab.role.market.ReceiveLongTermContractPowerRevenuesRole;
import emlab.role.market.SelectLongTermElectricityContractsRole;
import emlab.role.market.SubmitBidsToCommodityMarketRole;
import emlab.role.market.SubmitLongTermElectricityContractsRole;
import emlab.role.market.SubmitOffersToCommodityMarketRole;
import emlab.role.market.SubmitOffersToElectricitySpotMarketRole;
import emlab.role.operating.DetermineFuelMixRole;
import emlab.role.operating.PayCO2AuctionRole;
import emlab.role.operating.PayCO2TaxRole;
import emlab.role.operating.PayForLoansRole;
import emlab.role.operating.PayOperatingAndMaintainanceCostsRole;

/**
 * Main model role.
 * 
 * @author alfredas, ejlchappin, jcrichstein
 * 
 */
@ScriptComponent
public class DecarbonizationModelRole extends AbstractRole<DecarbonizationModel> implements Role<DecarbonizationModel> {

    @Autowired
    private PayCO2TaxRole payCO2TaxRole;
    @Autowired
    private PayCO2AuctionRole payCO2AuctionRole;
    @Autowired
    private InvestInPowerGenerationTechnologiesRole investInPowerGenerationTechnologiesRole;
    @Autowired
    private SubmitOffersToElectricitySpotMarketRole submitOffersToElectricitySpotMarketRole;
    @Autowired
    private ClearCommodityMarketRole clearCommodityMarketRole;
    @Autowired
    private SubmitBidsToCommodityMarketRole submitBidsToCommodityMarketRole;
    @Autowired
    private SubmitOffersToCommodityMarketRole submitOffersToCommodityMarketRole;
    @Autowired
    private SubmitLongTermElectricityContractsRole submitLongTermElectricityContractsRole;
    @Autowired
    private SelectLongTermElectricityContractsRole selectLongTermElectricityContractsRole;
    @Autowired
    private DismantlePowerPlantPastTechnicalLifetimeRole dismantlePowerPlantRole;
    @Autowired
    private ReassignPowerPlantsToLongTermElectricityContractsRole reassignPowerPlantsToLongTermElectricityContractsRole;
    @Autowired
    private ClearIterativeCO2AndElectricitySpotMarketTwoCountryRole clearIterativeCO2AndElectricitySpotMarketTwoCountryRole;
    @Autowired
    private DetermineFuelMixRole determineFuelMixRole;
    @Autowired
    private ReceiveLongTermContractPowerRevenuesRole receiveLongTermContractPowerRevenuesRole;
    @Autowired
    private ProcessAcceptedPowerPlantDispatchRole processAcceptedPowerPlantDispatchRole;
    @Autowired
    private ProcessAcceptedBidsRole processAcceptedBidsRole;
    @Autowired
    private PayForLoansRole payForLoansRole;
    @Autowired
    private PayOperatingAndMaintainanceCostsRole payOperatingAndMaintainanceCostsRole;
    @Autowired
    private RenewableTargetInvestmentRole renewableTargetInvestmentRole;

    @Autowired
    Reps reps;
    
    @Autowired Neo4jTemplate template;

    /**
     * Main model script. Executes other roles in the right sequence.
     */
    public void act(DecarbonizationModel model) {

        if (getCurrentTick() > model.getSimulationLength()) {
            logger.warn("Simulation is terminating!!!");
            agentspring.simulation.Schedule.getSchedule().stop();
            //System.exit(0);
        }

        logger.warn("***** STARTING TICK {} *****", getCurrentTick());
        Timer timer = new Timer();
        timer.start();

        logger.warn("  0. Dismantling & paying loans");
        for (EnergyProducer producer : reps.genericRepository.findAllAtRandom(EnergyProducer.class)) {
        	dismantlePowerPlantRole.act(producer);
        	payForLoansRole.act(producer);
//            producer.act(dismantlePowerPlantRole);
//            producer.act(payForLoansRole);
        }

        /*
         * Determine fuel mix of power plants
         */
        Timer timerMarket = new Timer();
        timerMarket.start();
        logger.warn("  1. Determining fuel mix");
        for (EnergyProducer producer : reps.genericRepository.findAllAtRandom(EnergyProducer.class)) {
        	determineFuelMixRole.act(producer);
//            producer.act(determineFuelMixRole);
        }
        timerMarket.stop();
        logger.warn("        took: {} seconds.", timerMarket.seconds());

        /*
         * Submit and select long-term electricity contracts
         */

        if (model.isLongTermContractsImplemented()) {
            timerMarket.reset();
            timerMarket.start();
            logger.warn("  2. Submit and select long-term electricity contracts");
            for (EnergyProducer producer : reps.genericRepository.findAllAtRandom(EnergyProducer.class)) {
            	submitLongTermElectricityContractsRole.act(producer);
//                producer.act(submitLongTermElectricityContractsRole);
            }

            for (EnergyConsumer consumer : reps.genericRepository.findAllAtRandom(EnergyConsumer.class)) {
            	selectLongTermElectricityContractsRole.act(consumer);
//                consumer.act(selectLongTermElectricityContractsRole);
            }
            timerMarket.stop();
            logger.warn("        took: {} seconds.", timerMarket.seconds());
        }

        /*
         * Clear electricity spot and CO2 markets and determine also the commitment of powerplants.
         */
        timerMarket.reset();
        timerMarket.start();
        logger.warn("  3. Clearing electricity spot and CO2 markets");
        for (EnergyProducer producer : reps.genericRepository.findAllAtRandom(EnergyProducer.class)) {
        	submitOffersToElectricitySpotMarketRole.act(producer);
//            producer.act(submitOffersToElectricitySpotMarketRole);
        }

        clearIterativeCO2AndElectricitySpotMarketTwoCountryRole.act(model);
//        model.act(clearIterativeCO2AndElectricitySpotMarketTwoCountryRole);
        timerMarket.stop();
        logger.warn("        took: {} seconds.", timerMarket.seconds());

        timerMarket.reset();
        timerMarket.start();
        for (EnergyProducer producer : reps.genericRepository.findAll(EnergyProducer.class)) {
        	receiveLongTermContractPowerRevenuesRole.act(producer);
        	//            producer.act(receiveLongTermContractPowerRevenuesRole);
        }
        for (ElectricitySpotMarket electricitySpotMarket : reps.marketRepository.findAllElectricitySpotMarkets()) {
        	processAcceptedPowerPlantDispatchRole.act(electricitySpotMarket);
//            electricitySpotMarket.act(processAcceptedPowerPlantDispatchRole);
        }
        timerMarket.stop();
        logger.warn("        paying took: {} seconds.", timerMarket.seconds());
        /*
         * Maintenance and CO2
         */
        logger.warn("  4. Paying for maintenance & co2");
        timerMarket.reset();
        timerMarket.start();
        for (EnergyProducer producer : reps.genericRepository.findAllAtRandom(EnergyProducer.class)) {
            // do accounting
        	payOperatingAndMaintainanceCostsRole.act(producer);
//            producer.act(payOperatingAndMaintainanceCostsRole);
            // pay tax
        	payCO2TaxRole.act(producer);
//            producer.act(payCO2TaxRole);
            // pay for CO2 auction only if CO2 trading
            if (model.isCo2TradingImplemented()) {
            	payCO2AuctionRole.act(producer);
//                producer.act(payCO2AuctionRole);
            }
        }
        timerMarket.stop();
        logger.warn("        took: {} seconds.", timerMarket.seconds());

        /*
         * COMMODITY MARKETS
         */
        logger.warn("  5. Purchasing commodities");
        timerMarket.reset();
        timerMarket.start();

        // SUPPLIER (supply for commodity markets)
        for (CommoditySupplier supplier : reps.genericRepository.findAllAtRandom(CommoditySupplier.class)) {
            // 1) first submit the offers
        	submitOffersToCommodityMarketRole.act(supplier);
//            supplier.act(submitOffersToCommodityMarketRole);
        }

        // PRODUCER (demand for commodity markets)
        for (EnergyProducer producer : reps.genericRepository.findAllAtRandom(EnergyProducer.class)) {
            // 2) submit bids
        	submitBidsToCommodityMarketRole.act(producer);
//            producer.act(submitBidsToCommodityMarketRole);
        }

        for (CommodityMarket market : reps.genericRepository.findAllAtRandom(CommodityMarket.class)) {
        	clearCommodityMarketRole.act(market);
        	processAcceptedBidsRole.act(market);
//            market.act(clearCommodityMarketRole);
//            market.act(processAcceptedBidsRole);
        }
        timerMarket.stop();
        logger.warn("        took: {} seconds.", timerMarket.seconds());

        logger.warn("  6. Investing");
        Timer timerInvest = new Timer();
        timerInvest.start();
        if (getCurrentTick() > 1) {
            boolean someOneStillWillingToInvest = true;
            while (someOneStillWillingToInvest) {
                someOneStillWillingToInvest = false;
                for (EnergyProducer producer : reps.energyProducerRepository.findAllEnergyProducersExceptForRenewableTargetInvestorsAtRandom()){
                    // invest in new plants
                	if (producer.isWillingToInvest()) {
                    	investInPowerGenerationTechnologiesRole.act(producer);
//                        producer.act(investInPowerGenerationTechnologiesRole);
                        someOneStillWillingToInvest = true;
                    }
                }
            }
            resetWillingnessToInvest();
        }
        for(TargetInvestor targetInvestor : template.findAll(TargetInvestor.class)){
        	renewableTargetInvestmentRole.act(targetInvestor);
        }
        timerInvest.stop();
        logger.warn("        took: {} seconds.", timerInvest.seconds());

        if (model.isLongTermContractsImplemented()) {
            logger.warn("  7. Reassign LTCs");
            timerMarket.reset();
            timerMarket.start();
            for (EnergyProducer producer : reps.genericRepository.findAllAtRandom(EnergyProducer.class)) {
            	reassignPowerPlantsToLongTermElectricityContractsRole.act(producer);
//                producer.act(reassignPowerPlantsToLongTermElectricityContractsRole);
            }
            timerMarket.stop();
            logger.warn("        took: {} seconds.", timerMarket.seconds());
        }

        /*
         * Deletion of old nodes
         */

        if (model.isDeletionOldPPDPBidsAndCashFlowsEnabled() && (getCurrentTick() - model.getDeletionAge() >= 0)) {
            timerMarket.reset();
            timerMarket.start();
            logger.warn("  8. Delete old nodes in year {}.", (getCurrentTick() - model.getDeletionAge()));
            reps.bidRepository.delete(reps.bidRepository.findAllBidsForForTime(getCurrentTick() - model.getDeletionAge()));
            reps.cashFlowRepository.delete(reps.cashFlowRepository.findAllCashFlowsForForTime(getCurrentTick() - model.getDeletionAge()));
            reps.powerPlantRepository.delete(reps.powerPlantRepository.findAllPowerPlantsDismantledBeforeTick(getCurrentTick()
                    - model.getDeletionAge()));
            timerMarket.stop();
            logger.warn("        took: {} seconds.", timerMarket.seconds());
        }

        timer.stop();
        logger.warn("Tick {} took {} seconds.", getCurrentTick(), timer.seconds());

        // if (getCurrentTick() >= model.getSimulationLength()) {
        // agentspring.simulation.Schedule.getSchedule().stop();
        // }

    }

    @Transactional
    private void resetWillingnessToInvest() {
        for (EnergyProducer producer : reps.genericRepository.findAllAtRandom(EnergyProducer.class)) {
            producer.setWillingToInvest(true);
        }
    }
}
