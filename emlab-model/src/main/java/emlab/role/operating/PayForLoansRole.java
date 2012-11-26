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
package emlab.role.operating;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import agentspring.role.AbstractRole;
import agentspring.role.Role;
import agentspring.role.RoleComponent;
import emlab.domain.agent.EnergyProducer;
import emlab.domain.contract.CashFlow;
import emlab.domain.contract.Loan;
import emlab.domain.technology.PowerPlant;
import emlab.repository.Reps;

/**
 * {@link EnergyProducer}s repay their loans
 * 
 * @author alfredas
 * @author emile
 * 
 */
@RoleComponent
public class PayForLoansRole extends AbstractRole<EnergyProducer> implements Role<EnergyProducer> {


    @Autowired
    Reps reps;

    @Transactional
    public void act(EnergyProducer producer) {

        logger.info("Process accepted bids to cash flow now");

        // for (Loan loan : loanRepository.findLoansFromAgent(producer)) {
        for (PowerPlant plant : reps.powerPlantRepository.findPowerPlantsByOwner(producer)) {
            Loan loan = plant.getLoan();
            if (loan != null) {
                logger.info("Found a loan: {}", loan);
                if (loan.getNumberOfPaymentsDone() < loan.getTotalNumberOfPayments()) {

                    double payment = loan.getAmountPerPayment();
                    reps.nonTransactionalCreateRepository.createCashFlow(producer, loan.getTo(), payment, CashFlow.LOAN, getCurrentTick(),
                            loan.getRegardingPowerPlant());

                    loan.setNumberOfPaymentsDone(loan.getNumberOfPaymentsDone() + 1);

                    logger.info("Paying {} (euro) for loan {}", payment, loan);
                    logger.info("Number of payments done {}, total needed: {}", loan.getNumberOfPaymentsDone(),
                            loan.getTotalNumberOfPayments());
                }
            }
        }

    }
}
