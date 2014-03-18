/**
 * 
 */
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
    public TargetInvestorRepository targetInvestorRepository;

    @Autowired
    public PowerGenerationTechnologyTargetRepository powerGenerationTechnologyTargetRepository;

    @Autowired
    public EnergyProducerRepository energyProducerRepository;

    @Autowired
    public Neo4jTemplate template;

    @Autowired
    public SubstanceRepository substanceRepository;

    @Autowired
    public PowerGeneratingTechnologyNodeLimitRepository powerGeneratingTechnologyNodeLimitRepository;

    @Autowired
    public StrategicReserveOperatorRepository strategicReserveOperatorRepository;

    @Autowired
    public DecarbonizationAgentRepository decarbonizationAgentRepository;

}
