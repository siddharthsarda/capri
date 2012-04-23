package org.styloot.capri.solr;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import java.util.*;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.styloot.hobo.*;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.servlet.SolrRequestParsers;
import org.apache.solr.common.params.SolrParams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class StylootSolr{
  private static final Logger log = LoggerFactory.getLogger(StylootSolr.class);
  private SolrServer server = null;
  public StylootSolr() throws java.io.IOException,javax.xml.parsers.ParserConfigurationException,org.xml.sax.SAXException{
         System.setProperty("solr.solr.home", "/var/solr");
         CoreContainer.Initializer initializer = new CoreContainer.Initializer();
         CoreContainer coreContainer = initializer.initialize();
         server = new EmbeddedSolrServer(coreContainer, "");
  }
  
  public void addDocumentsToSolr(List<StylootSolrItem> items) throws org.apache.solr.client.solrj.SolrServerException, java.io.IOException
  { 
         server.deleteById("*:*");
         int startIndex = 0;
         int lastIndex = items.size();
         //Break item into chunks as otherwise if you try to commit all an OutOfHeapError
         while(startIndex!= lastIndex){
              int endIndex = (startIndex +1000) < lastIndex ? startIndex + 1000: lastIndex;
              List<StylootSolrItem> itemsSubList = items.subList(startIndex,endIndex);
              server.addBeans(itemsSubList);
              startIndex = endIndex;
         }
         server.commit();
         server.optimize();
  }

  public List<String> doSimpleTextSearch(String text,int page,int pageSize){        
         try{
         Vector<String> results = new Vector<String>();
         SolrQuery solrQuery = new SolrQuery();
         solrQuery.setQuery(text);
         solrQuery.setStart(page*pageSize);
	 solrQuery.setRows(pageSize);
         solrQuery.addSortField("qualityscore", SolrQuery.ORDER.desc);
         QueryResponse response = server.query(solrQuery);
         for(SolrDocument doc : response.getResults()){ 
                        results.add(doc.getFieldValue("id").toString()); 
         }
         return results;
         }
         catch(org.apache.solr.client.solrj.SolrServerException e){

         } 
         return new Vector<String>();
  }

  public List<String> doCombinedTextSearch(Iterator<Item> iter, String text,int page,int pageSize){
         try{
         Vector<String> results = new Vector<String>();
         int count = 0;
         Vector<String> items = new Vector<String>();
         while (iter.hasNext()) {
            items.add(iter.next().id);
            count+=1;
            if (count % 50 == 0) {
                   StringBuffer request = new StringBuffer();
                   String idsWithOR = StringUtils.join(items.toArray(), " OR ");
                   String idQuery = "id:(" + idsWithOR + ") ";
                   String textQuery = "text:"+ text;
                   String queryString = "q={!q.op=AND}" + idQuery + textQuery;
                   request.append(queryString); 
                   request.append("&start=" + 0);
		   request.append("&rows=" + pageSize);
                   //request.append("&sort=qualityscore desc");
                   SolrParams solrParams = SolrRequestParsers.parseQueryString(request.toString());
                   QueryResponse response = server.query(solrParams);
                   for(SolrDocument doc : response.getResults()){
                        results.add(doc.getFieldValue("id").toString());
                   }
                   if (results.size() > ((page+1) * pageSize)){
                        return results.subList((page*pageSize) , ((page+1)* pageSize));
                   }
                   items = new Vector<String>();
            }

         }
         }
         catch(org.apache.solr.client.solrj.SolrServerException e){

         }
         return new Vector<String>();
  }


}
