/**
 * 
 */
package emlab.gen.domain.contract;

import org.springframework.data.neo4j.annotation.NodeEntity;

/**
 * @author ejlchappin
 *
 *
 */
@NodeEntity
public class LongTermContractDuration {

	private long duration;

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public String toString(){
		return "duration: " + getDuration(); 
	}
}
