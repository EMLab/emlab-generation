package emlab.domain.enipedia;

import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;

import emlab.domain.gis.Zone;

import agentspring.lod.LODId;
import agentspring.lod.LODProperty;
import agentspring.lod.LODType;

@NodeEntity
@LODType(type = "Category:Country")
public class CountryEnipedia extends Zone {

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

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return "Country Enipedia " + name;
    }

}