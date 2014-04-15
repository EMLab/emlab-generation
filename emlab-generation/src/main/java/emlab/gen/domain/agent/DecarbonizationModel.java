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
package emlab.gen.domain.agent;

import org.springframework.data.neo4j.annotation.NodeEntity;

import agentspring.agent.AbstractAgent;
import agentspring.agent.Agent;
import agentspring.simulation.SimulationParameter;

@NodeEntity
public class DecarbonizationModel extends AbstractAgent implements Agent {
    private double absoluteStabilityCriterion;
    private double relativeStabilityCriterion;
    private double relativeBounceCriterion;

    private double iterationSpeedFactor;
    private double iterationSpeedCriterion;
    private double capDeviationCriterion;
    private String name;

    private long centralForecastBacklookingYears;

    private long centralForecastingYear;

    private double centralPrivateDiscountingRate;

    private double centralCO2BackSmoothingFactor;

    private double centralCO2TargetReversionSpeedFactor;

    private boolean co2BankingIsImplemented;

    private boolean stabilityReserveIsActive;

    private long stabilityReserveFirstYearOfOperation;


    @SimulationParameter(label = "Simulation Length", from = 0, to = 75)
    private double simulationLength;

    @SimulationParameter(label = "CO2 Trading")
    private boolean co2TradingImplemented;

    @SimulationParameter(label = "Long Term Contracts")
    private boolean longTermContractsImplemented;

    @SimulationParameter(label = "Real Renewable Data")
    private boolean realRenewableDataImplemented;

    @SimulationParameter(label = "Delete old PPDPs, Bids and Cashflows")
    private boolean deletionOldPPDPBidsAndCashFlowsEnabled;

    @SimulationParameter(label = "Exit simulation after simulation length")
    private boolean exitSimulationAfterSimulationLength;

    @SimulationParameter(label = "Deletion age")
    private long deletionAge;

    public boolean isRealRenewableDataImplemented() {
        return realRenewableDataImplemented;
    }

    public void setRealRenewableDataImplemented(boolean realRenewableDataImplemented) {
        this.realRenewableDataImplemented = realRenewableDataImplemented;
    }

    public double getIterationSpeedFactor() {
        return iterationSpeedFactor;
    }

    public void setIterationSpeedFactor(double iterationSpeedFactor) {
        this.iterationSpeedFactor = iterationSpeedFactor;
    }

    public double getIterationSpeedCriterion() {
        return iterationSpeedCriterion;
    }

    public void setIterationSpeedCriterion(double iterationSpeedCriterion) {
        this.iterationSpeedCriterion = iterationSpeedCriterion;
    }

    public double getCapDeviationCriterion() {
        return capDeviationCriterion;
    }

    public void setCapDeviationCriterion(double capDeviationCriterion) {
        this.capDeviationCriterion = capDeviationCriterion;
    }

    public double getAbsoluteStabilityCriterion() {
        return absoluteStabilityCriterion;
    }

    public void setAbsoluteStabilityCriterion(double absoluteStabilityCriterion) {
        this.absoluteStabilityCriterion = absoluteStabilityCriterion;
    }

    public double getRelativeStabilityCriterion() {
        return relativeStabilityCriterion;
    }

    public void setRelativeStabilityCriterion(double relativeStabilityCriterion) {
        this.relativeStabilityCriterion = relativeStabilityCriterion;
    }

    public boolean isCo2TradingImplemented() {
        return co2TradingImplemented;
    }

    public boolean isLongTermContractsImplemented() {
        return longTermContractsImplemented;
    }

    public void setLongTermContractsImplemented(boolean longTermContractsImplemented) {
        this.longTermContractsImplemented = longTermContractsImplemented;
    }

    public void setCo2TradingImplemented(boolean co2Market) {
        this.co2TradingImplemented = co2Market;
    }

    public double getRelativeBounceCriterion() {
        return relativeBounceCriterion;
    }

    public void setRelativeBounceCriterion(double relativeBounceCriterion) {
        this.relativeBounceCriterion = relativeBounceCriterion;
    }

    public double getSimulationLength() {
        return simulationLength;
    }

    public void setSimulationLength(double simulationLength) {
        this.simulationLength = simulationLength;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public boolean isDeletionOldPPDPBidsAndCashFlowsEnabled() {
        return deletionOldPPDPBidsAndCashFlowsEnabled;
    }

    public void setDeletionOldPPDPBidsAndCashFlowsEnabled(boolean deletionOldPPDPBidsAndCashFlowsEnabled) {
        this.deletionOldPPDPBidsAndCashFlowsEnabled = deletionOldPPDPBidsAndCashFlowsEnabled;
    }

    public long getDeletionAge() {
        return deletionAge;
    }

    public void setDeletionAge(long deletionAge) {
        this.deletionAge = deletionAge;
    }

    public boolean isExitSimulationAfterSimulationLength() {
        return exitSimulationAfterSimulationLength;
    }

    public void setExitSimulationAfterSimulationLength(boolean exitSimulationAfterSimulationLength) {
        this.exitSimulationAfterSimulationLength = exitSimulationAfterSimulationLength;
    }

    public long getCentralForecastBacklookingYears() {
        return centralForecastBacklookingYears;
    }

    public void setCentralForecastBacklookingYears(long centralForecastBacklookingYears) {
        this.centralForecastBacklookingYears = centralForecastBacklookingYears;
    }

    public long getCentralForecastingYear() {
        return centralForecastingYear;
    }

    public void setCentralForecastingYear(long centralForecastingYear) {
        this.centralForecastingYear = centralForecastingYear;
    }

    public double getCentralPrivateDiscountingRate() {
        return centralPrivateDiscountingRate;
    }

    public void setCentralPrivateDiscountingRate(double centralPrivateDiscountingRate) {
        this.centralPrivateDiscountingRate = centralPrivateDiscountingRate;
    }

    public boolean isCo2BankingIsImplemented() {
        return co2BankingIsImplemented;
    }

    public void setCo2BankingIsImplemented(boolean co2BankingIsImplemented) {
        this.co2BankingIsImplemented = co2BankingIsImplemented;
    }

    public double getCentralCO2BackSmoothingFactor() {
        return centralCO2BackSmoothingFactor;
    }

    public void setCentralCO2BackSmoothingFactor(double centralCO2BackSmoothingFactor) {
        this.centralCO2BackSmoothingFactor = centralCO2BackSmoothingFactor;
    }

    public double getCentralCO2TargetReversionSpeedFactor() {
        return centralCO2TargetReversionSpeedFactor;
    }

    public void setCentralCO2TargetReversionSpeedFactor(double centralCO2TargetReversionSpeedFactor) {
        this.centralCO2TargetReversionSpeedFactor = centralCO2TargetReversionSpeedFactor;
    }

    public boolean isStabilityReserveIsActive() {
        return stabilityReserveIsActive;
    }

    public void setStabilityReserveIsActive(boolean stabilityReserveIsActive) {
        this.stabilityReserveIsActive = stabilityReserveIsActive;
    }

    public long getStabilityReserveFirstYearOfOperation() {
        return stabilityReserveFirstYearOfOperation;
    }

    public void setStabilityReserveFirstYearOfOperation(long stabilityReserveFirstYearOfOperation) {
        this.stabilityReserveFirstYearOfOperation = stabilityReserveFirstYearOfOperation;
    }

}
