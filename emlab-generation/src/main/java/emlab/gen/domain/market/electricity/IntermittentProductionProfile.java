package emlab.gen.domain.market.electricity;

import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.springframework.transaction.annotation.Transactional;

import emlab.gen.domain.technology.IntermittentResourceProfile;

/**
 * Production profile of a intermittent power plant
 * 
 * Is linked to intermittentResourceProfile, which in turn is linked to a
 * PowerGenerationTechnology and PowerGridNode. As such, the
 * IntermittentProductionProfile represents the coupling to the segment-based
 * simulation, and is specific to a technology, a node and a time step.
 * 
 * 
 * @author jrichstein
 * 
 */
@NodeEntity
public class IntermittentProductionProfile {

	@RelatedTo(type = "INTERMITTENT_RESOURCE_PROFILE", elementClass = IntermittentResourceProfile.class, direction = Direction.OUTGOING)
	IntermittentResourceProfile intermittentResourceProfile;
	
	@RelatedTo(type = "PRODUCTION_IN_SEGMENTS", elementClass = SegmentIntermittentProduction.class, direction = Direction.OUTGOING)
	Set<SegmentIntermittentProduction> productionInSegments;
    
	private long time;

	public IntermittentResourceProfile getIntermittentResourceProfile() {
		return intermittentResourceProfile;
    }

	public void setIntermittentResourceProfile(
	        IntermittentResourceProfile intermittentResourceProfile) {
		this.intermittentResourceProfile = intermittentResourceProfile;
    }

	public Set<SegmentIntermittentProduction> getProductionInSegments() {
		return productionInSegments;
    }

	public void setProductionInSegments(
	        Set<SegmentIntermittentProduction> productionInSegments) {
		this.productionInSegments = productionInSegments;
    }

	public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Transactional
	public void specifyAndPersist(
	        IntermittentResourceProfile intermittentResourceProfile,
	        Set<SegmentIntermittentProduction> productionInSegments, long time) {
        this.persist();
		specifyNotPersist(intermittentResourceProfile,
		        productionInSegments, time);
    }

	public void specifyNotPersist(
	        IntermittentResourceProfile intermittentResourceProfile,
	        Set<SegmentIntermittentProduction> productionInSegments, long time) {
		setProductionInSegments(productionInSegments);
        setTime(time);
    }

}
