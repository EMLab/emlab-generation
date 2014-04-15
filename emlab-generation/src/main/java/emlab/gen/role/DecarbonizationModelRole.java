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
package emlab.gen.role;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.transaction.annotation.Transactional;

import agentspring.role.AbstractRole;
import agentspring.role.Role;
import agentspring.role.ScriptComponent;
import cern.colt.Timer;
import emlab.gen.domain.agent.CommoditySupplier;
import emlab.gen.domain.agent.DecarbonizationModel;
import emlab.gen.domain.agent.EnergyConsumer;
import emlab.gen.domain.agent.EnergyProducer;
import emlab.gen.domain.agent.Government;
import emlab.gen.domain.agent.StrategicReserveOperator;
import emlab.gen.domain.agent.TargetInvestor;
import emlab.gen.domain.market.CommodityMarket;
import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.repository.Reps;
import emlab.gen.role.capacitymechanisms.ProcessAcceptedPowerPlantDispatchRoleinSR;
import emlab.gen.role.capacitymechanisms.StrategicReserveOperatorRole;
import emlab.gen.role.co2policy.MarketStabilityReserveRole;
import emlab.gen.role.co2policy.RenewableAdaptiveCO2CapRole;
import emlab.gen.role.investment.DismantlePowerPlantPastTechnicalLifetimeRole;
import emlab.gen.role.investment.GenericInvestmentRole;
import emlab.gen.role.market.ClearCommodityMarketRole;
import emlab.gen.role.market.ClearIterativeCO2AndElectricitySpotMarketTwoCountryRole;
import emlab.gen.role.market.ProcessAcceptedBidsRole;
import emlab.gen.role.market.ProcessAcceptedPowerPlantDispatchRole;
import emlab.gen.role.market.ReassignPowerPlantsToLongTermElectricityContractsRole;
import emlab.gen.role.market.ReceiveLongTermContractPowerRevenuesRole;
import emlab.gen.role.market.SelectLongTermElectricityContractsRole;
import emlab.gen.role.market.SubmitBidsToCommodityMarketRole;
import emlab.gen.role.market.SubmitLongTermElectricityContractsRole;
import emlab.gen.role.market.SubmitOffersToCommodityMarketRole;
import emlab.gen.role.market.SubmitOffersToElectricitySpotMarketRole;
import emlab.gen.role.operating.DetermineFuelMixRole;
import emlab.gen.role.operating.PayCO2AuctionRole;
import emlab.gen.role.operating.PayCO2TaxRole;
import emlab.gen.role.operating.PayForLoansRole;
import emlab.gen.role.operating.PayOperatingAndMaintainanceCostsRole;

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
    private GenericInvestmentRole<EnergyProducer> genericInvestmentRole;
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
    private StrategicReserveOperatorRole strategicReserveOperatorRole;
    @Autowired
    private ProcessAcceptedPowerPlantDispatchRoleinSR acceptedPowerPlantDispatchRoleinSR;
    @Autowired
    private RenewableAdaptiveCO2CapRole renewableAdaptiveCO2CapRole;
    @Autowired
    MarketStabilityReserveRole marketStabilityReserveRole;

    @Autowired
    Reps reps;

    @Autowired
    Neo4jTemplate template;

    /**
     * Main model script. Executes other roles in the right sequence.
     */
    @Override
    public void act(DecarbonizationModel model) {

        if (getCurrentTick() > model.getSimulationLength() && model.isExitSimulationAfterSimulationLength()) {
            logger.warn("Simulation is terminating!!!");
            // agentspring.simulation.Schedule.getSchedule().stop();
            System.exit(0);
        }

        if (getCurrentTick() >= model.getSimulationLength()) {
            agentspring.simulation.Schedule.getSchedule().stop();
        }

        logger.warn("***** STARTING TICK {} *****", getCurrentTick());
        Timer timer = new Timer();
        timer.start();

        logger.warn("  0. Dismantling & paying loans");
        for (EnergyProducer producer : reps.genericRepository.findAllAtRandom(EnergyProducer.class)) {
            dismantlePowerPlantRole.act(producer);
            payForLoansRole.act(producer);
            // producer.act(dismantlePowerPlantRole);
            // producer.act(payForLoansRole);
        }

        /*
         * Determine fuel mix of power plants
         */
        Timer timerMarket = new Timer();
        timerMarket.start();
        logger.warn("  1. Determining fuel mix");
        for (EnergyProducer producer : reps.genericRepository.findAllAtRandom(EnergyProducer.class)) {
            determineFuelMixRole.act(producer);
            // producer.act(determineFuelMixRole);
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
                // producer.act(submitLongTermElectricityContractsRole);
            }

            for (EnergyConsumer consumer : reps.genericRepository.findAllAtRandom(EnergyConsumer.class)) {
                selectLongTermElectricityContractsRole.act(consumer);
                // consumer.act(selectLongTermElectricityContractsRole);
            }
            timerMarket.stop();
            logger.warn("        took: {} seconds.", timerMarket.seconds());
        }

        // timerMarket.reset();
        // timerMarket.start();
        // logger.warn("  2b. Creating market forecast");
        //
        // clearIterativeCO2AndElectricitySpotMarketTwoCountryRole
        // .makeCentralElectricityMarketForecastForTimeStep(getCurrentTick() +
        // model.getCentralForecastingYear());
        //
        // logger.warn("        took: {} seconds.", timerMarket.seconds());
        //
        // timerMarket.reset();

        /*
         * Clear electricity spot and CO2 markets and determine also the
         * commitment of powerplants.
         */
        timerMarket.reset();
        timerMarket.start();
        logger.warn("  3. Submitting offers to market");
        for (EnergyProducer producer : reps.genericRepository.findAllAtRandom(EnergyProducer.class)) {
            submitOffersToElectricitySpotMarketRole.act(producer);
            // producer.act(submitOffersToElectricitySpotMarketRole);
        }
        timerMarket.stop();
        logger.warn("        took: {} seconds.", timerMarket.seconds());

        /*
         * Contract strategic reserve volume and set strategic reserve dispatch
         * price
         */
        for (StrategicReserveOperator strategicReserveOperator : reps.strategicReserveOperatorRepository.findAll()) {
            logger.warn("  3a. Contracting Strategic Reserve in " + strategicReserveOperator.getZone().getName());
            strategicReserveOperatorRole.act(strategicReserveOperator);
        }

        Government government = template.findAll(Government.class).iterator().next();
        if (getCurrentTick() > 0 && government.getCo2CapTrend() != null && government.isActivelyAdjustingTheCO2Cap()) {
            logger.warn("Lowering cap according to RES installations");
            renewableAdaptiveCO2CapRole.act(government);
        }

        if (getCurrentTick() >= 10 && model.isStabilityReserveIsActive()) {
            logger.warn("3b. CO2 Market Stability Reserve");
            marketStabilityReserveRole.act(government);
        }

        timerMarket.reset();
        timerMarket.start();
        logger.warn("  4. Clearing electricity spot and CO2 markets");
        clearIterativeCO2AndElectricitySpotMarketTwoCountryRole.act(model);
        // model.act(clearIterativeCO2AndElectricitySpotMarketTwoCountryRole);
        timerMarket.stop();
        logger.warn("        took: {} seconds.", timerMarket.seconds());

        timerMarket.reset();
        timerMarket.start();
        for (EnergyProducer producer : reps.genericRepository.findAll(EnergyProducer.class)) {
            receiveLongTermContractPowerRevenuesRole.act(producer);
            // producer.act(receiveLongTermContractPowerRevenuesRole);
        }
        for (ElectricitySpotMarket electricitySpotMarket : reps.marketRepository.findAllElectricitySpotMarkets()) {
            processAcceptedPowerPlantDispatchRole.act(electricitySpotMarket);
            // electricitySpotMarket.act(processAcceptedPowerPlantDispatchRole);
        }
        for (StrategicReserveOperator strategicReserveOperator : reps.strategicReserveOperatorRepository.findAll()) {
            acceptedPowerPlantDispatchRoleinSR.act(strategicReserveOperator);
        }
        // logger.warn(" 4. Processing Strategic Reserve Payment ");
        timerMarket.stop();
        logger.warn("        paying took: {} seconds.", timerMarket.seconds());
        /*
         * Maintenance and CO2
         */
        logger.warn("  5. Paying for maintenance & co2");
        timerMarket.reset();
        timerMarket.start();
        for (EnergyProducer producer : reps.genericRepository.findAllAtRandom(EnergyProducer.class)) {
            // do accounting
            payOperatingAndMaintainanceCostsRole.act(producer);
            // producer.act(payOperatingAndMaintainanceCostsRole);
            // pay tax
            payCO2TaxRole.act(producer);
            // producer.act(payCO2TaxRole);
            // pay for CO2 auction only if CO2 trading
            if (model.isCo2TradingImplemented()) {
                payCO2AuctionRole.act(producer);
                // producer.act(payCO2AuctionRole);
            }
        }
        timerMarket.stop();
        logger.warn("        took: {} seconds.", timerMarket.seconds());

        /*
         * COMMODITY MARKETS
         */
        logger.warn("  6. Purchasing commodities");
        timerMarket.reset();
        timerMarket.start();

        // SUPPLIER (supply for commodity markets)
        for (CommoditySupplier supplier : reps.genericRepository.findAllAtRandom(CommoditySupplier.class)) {
            // 1) first submit the offers
            submitOffersToCommodityMarketRole.act(supplier);
            // supplier.act(submitOffersToCommodityMarketRole);
        }

        // PRODUCER (demand for commodity markets)
        for (EnergyProducer producer : reps.genericRepository.findAllAtRandom(EnergyProducer.class)) {
            // 2) submit bids
            submitBidsToCommodityMarketRole.act(producer);
            // producer.act(submitBidsToCommodityMarketRole);
        }

        for (CommodityMarket market : reps.genericRepository.findAllAtRandom(CommodityMarket.class)) {
            clearCommodityMarketRole.act(market);
            processAcceptedBidsRole.act(market);
            // market.act(clearCommodityMarketRole);
            // market.act(processAcceptedBidsRole);
        }
        timerMarket.stop();
        logger.warn("        took: {} seconds.", timerMarket.seconds());

        logger.warn("  7. Investing");
        Timer timerInvest = new Timer();
        timerInvest.start();
        if (getCurrentTick() > 1) {
            boolean someOneStillWillingToInvest = true;
            while (someOneStillWillingToInvest) {
                someOneStillWillingToInvest = false;
                for (EnergyProducer producer : reps.energyProducerRepository
                        .findAllEnergyProducersExceptForRenewableTargetInvestorsAtRandom()) {
                    // invest in new plants
                    if (producer.isWillingToInvest()) {
                        genericInvestmentRole.act(producer);
                        // producer.act(investInPowerGenerationTechnologiesRole);
                        someOneStillWillingToInvest = true;
                    }
                }
            }
            resetWillingnessToInvest();
        }
        for (TargetInvestor targetInvestor : template.findAll(TargetInvestor.class)) {
            genericInvestmentRole.act(targetInvestor);
        }
        timerInvest.stop();
        logger.warn("        took: {} seconds.", timerInvest.seconds());

        if (model.isLongTermContractsImplemented()) { // if (getCurrentTick() >=
            // model.getSimulationLength())
            // {
            // agentspring.simulation.Schedule.getSchedule().stop();
            // }

            logger.warn("  7.5. Reassign LTCs");
            timerMarket.reset();
            timerMarket.start();
            for (EnergyProducer producer : reps.genericRepository.findAllAtRandom(EnergyProducer.class)) {
                reassignPowerPlantsToLongTermElectricityContractsRole.act(producer);
                // producer.act(reassignPowerPlantsToLongTermElectricityContractsRole);
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
            reps.bidRepository.delete(reps.bidRepository.findAllBidsForForTime(getCurrentTick()
                    - model.getDeletionAge()));
            reps.cashFlowRepository.delete(reps.cashFlowRepository.findAllCashFlowsForForTime(getCurrentTick()
                    - model.getDeletionAge()));
            reps.powerPlantRepository.delete(reps.powerPlantRepository
                    .findAllPowerPlantsDismantledBeforeTick(getCurrentTick() - model.getDeletionAge()));
            reps.powerPlantDispatchPlanRepository.delete(reps.powerPlantDispatchPlanRepository
                    .findAllPowerPlantDispatchPlansForTime(getCurrentTick() + model.getCentralForecastingYear() - 1,
                            true));
            timerMarket.stop();
            logger.warn("        took: {} seconds.", timerMarket.seconds());
        }

        timer.stop();
        logger.warn("Tick {} took {} seconds.", getCurrentTick(), timer.seconds());
    }

    @Transactional
    private void resetWillingnessToInvest() {
        for (EnergyProducer producer : reps.genericRepository.findAllAtRandom(EnergyProducer.class)) {
            producer.setWillingToInvest(true);
        }
    }
}
