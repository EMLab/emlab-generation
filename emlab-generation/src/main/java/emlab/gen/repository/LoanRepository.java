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
package emlab.gen.repository;


import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.pipes.Pipe;

import emlab.gen.domain.agent.DecarbonizationAgent;
import emlab.gen.domain.contract.Loan;
import emlab.gen.domain.technology.PowerPlant;


/**
 * Repository for loans
 * @author ejlchappin
 *
 */
@Repository
public class LoanRepository extends AbstractRepository<Loan> {

	/**
	 * Creates a loan
	 * @param from the seller of the loan
	 * @param to the buyer of the loan
	 * @param amount the total amount to be payed
	 * @param numberOfPayments the number of payments
	 * @param loanStartTime the time the loan starts
	 * @param plant the power plant the loan is connected to
	 * @return
	 */
    @Transactional
    public Loan createLoan(DecarbonizationAgent from, DecarbonizationAgent to, double amount, long numberOfPayments, long loanStartTime, PowerPlant plant) {
        Loan loan = new Loan().persist();
        loan.setFrom(from);
        loan.setTo(to);
        loan.setAmountPerPayment(amount);
        loan.setTotalNumberOfPayments(numberOfPayments);
        loan.setRegardingPowerPlant(plant);
        loan.setLoanStartTime(loanStartTime);
        loan.setNumberOfPaymentsDone(0);
        return loan;
    }

    /**
     * Finds all loans that the agent has been lend to by others.  
     * @param agent
     * @return the loans
     */
    public Iterable<Loan> findLoansFromAgent(DecarbonizationAgent agent) {
        Pipe<Vertex, Vertex> loansPipe = new LabeledEdgePipe("LEND_TO_AGENT", LabeledEdgePipe.Step.IN_OUT);
        return findAllByPipe(agent, loansPipe);
    }
    
    /**
     * Finds all loans that the agent has lend to others
     * @param agent 
     * @return the loans
     */
    public Iterable<Loan> findLoansToAgent(DecarbonizationAgent agent) {
        Pipe<Vertex, Vertex> loansPipe = new LabeledEdgePipe("LEND_BY_AGENT", LabeledEdgePipe.Step.IN_OUT);
        return findAllByPipe(agent, loansPipe);
    }

}
