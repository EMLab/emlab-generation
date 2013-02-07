package emlab.domain.enipedia;

import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;

import agentspring.lod.LODId;
import agentspring.lod.LODProperty;
import agentspring.lod.LODType;

@LODType(type = "Category:Fuel")
@NodeEntity
public class FuelEnipedia {

    @LODProperty("http://www.w3.org/2000/01/rdf-schema#label")
    @Indexed
    String name;

    @LODId
    @Indexed
    String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String label) {
        this.name = label;
    }

    public String toString() {
        return this.getName();
    }
}
