package org.styloot.capri.solr;
import org.apache.solr.client.solrj.beans.Field;

public class StylootSolrItem{
    @Field("id")
    String id;

    @Field("name")
    String name;

    @Field("description")
    String description;

    @Field("qualityscore")
    int qualityScore;

    
    public StylootSolrItem(String id, String name, String description, int qualityScore){
            this.id = id;
            this.name = name;
            this.description = description;
            this.qualityScore = qualityScore;
    }
}
