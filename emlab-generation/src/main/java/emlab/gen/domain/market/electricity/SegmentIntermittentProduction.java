package emlab.gen.domain.market.electricity;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.springframework.transaction.annotation.Transactional;

/**
 * Is needed due the the Neo4j database structure. Is linked to the
 * intermittentProductionProfile. As such it is in total specific to the
 * segment, the PowerGenerationTechnology, the PowerGridNode and the time step.
 * 
 * 
 * @author jrichstein
 * 
 */

@NodeEntity
public class SegmentIntermittentProduction {

    @RelatedTo(type = "PRODUCTION_SEGMENT", elementClass = Segment.class, direction = Direction.OUTGOING)
    private Segment productionSegment;

    @RelatedTo(type = "PRODUCTIONPROFILE", elementClass = IntermittentProductionProfile.class, direction = Direction.BOTH)
    private IntermittentProductionProfile productionProfile;

    /**
     * In MWh.
     */
    private double production;

    /**
     * Capacity used to to achieve the production value. Is necessary so that
     * the share of revenue for individual power plants can be calculated.
     * 
     * In Megawatt (MW).
     */
    private double capacity;

    /**
     * A curtailment factor of 1 means no curtailment.
     */
    private double curtailmentFactor;

    public Segment getSegment() {
        return productionSegment;
    }

    public void setSegment(Segment segment) {
        this.productionSegment = segment;
    }

    public double getProduction() {
        return production;
    }

    public void setProduction(double production) {
        this.production = production;
    }

    @Override
    public String toString() {
        return "segment: " + productionSegment + " intermittent production: " + getProduction();
    }

    public double getCurtailmentFactor() {
        return curtailmentFactor;
    }

    public void setCurtailmentFactor(double curtailmentFactor) {
        this.curtailmentFactor = curtailmentFactor;
    }

    public double getCapacity() {
        return capacity;
    }

    public void setCapacity(double capacity) {
        this.capacity = capacity;
    }

    public void specifyNotPersist(IntermittentProductionProfile productionProfile, Segment productionSegment, double production,
            double installedCapacity, double curtailmentFactor) {
        this.productionProfile = productionProfile;
        this.productionSegment = productionSegment;
        this.production = production;
        this.curtailmentFactor = curtailmentFactor;
        this.capacity = installedCapacity;

    }

    @Transactional
    public void specifyAndPersist(IntermittentProductionProfile productionProfile, Segment productionSegment, double production,
            double installedCapacity, double curtailmentFactor) {
        this.persist();
        specifyNotPersist(productionProfile, productionSegment, production, installedCapacity, curtailmentFactor);
    }

}
