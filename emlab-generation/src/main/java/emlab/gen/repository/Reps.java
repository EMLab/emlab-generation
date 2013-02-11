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
package emlab.gen.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.stereotype.Repository;

/**
 * @author JCRichstein
 * 
 */
@Repository
public class Reps {

	@Autowired
	public GenericRepository genericRepository;

	@Autowired
	public PowerPlantRepository powerPlantRepository;

	@Autowired
	public NonTransactionalCreateRepository nonTransactionalCreateRepository;

	@Autowired
	public MarketRepository marketRepository;

	@Autowired
	public ModelRepository modelRepository;

	@Autowired
	public BidRepository bidRepository;

	@Autowired
	public ClearingPointRepositoryOld clearingPointRepositoryOld;

	@Autowired
	public ClearingPointRepository clearingPointRepository;

	@Autowired
	public LoanRepository loanRepository;

	@Autowired
	public PowerPlantDispatchPlanRepository powerPlantDispatchPlanRepository;

	@Autowired
	public ContractRepository contractRepository;

	@Autowired
	public CashFlowRepository cashFlowRepository;

	@Autowired
	public SegmentLoadRepository segmentLoadRepository;

	@Autowired
	public SegmentRepository segmentRepository;

	@Autowired
	public NationalGovernmentRepository nationalGovernmentRepository;

	@Autowired
	public PowerGeneratingTechnologyRepository powerGeneratingTechnologyRepository;

	@Autowired
	public PowerGridNodeRepository powerGridNodeRepository;

	@Autowired
	public SegmentClearingPointRepository segmentClearingPointRepository;

	@Autowired
	public SubstanceRepository substanceRepository;

	@Autowired
	public TargetInvestorRepository targetInvestorRepository;

	@Autowired
	public ZoneRepository zoneRepository;

	@Autowired
	public PowerGenerationTechnologyTargetRepository powerGenerationTechnologyTargetRepository;

	@Autowired
	public StrategicReserveOperatorRepository strategicReserveOperatorRepository;

	@Autowired
	public EnergyProducerRepository energyProducerRepository;

	@Autowired
	public Neo4jTemplate template;

}
