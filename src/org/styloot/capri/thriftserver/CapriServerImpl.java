package org.styloot.capri.thriftserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.thrift.TException;
import org.styloot.capri.gen.*;
import org.styloot.capri.*;
import org.styloot.capri.solr.*;
import org.styloot.hobo.*;
import org.styloot.hobo.hoboindex.HoboIndex;

import java.util.*;

class CapriServerImpl implements Capri.Iface {
    private static final Logger log = LoggerFactory.getLogger(CapriServerImpl.class);

    public CapriServerImpl(HoboIndex idx, StylootSolr s,int ps) {
	index = idx;
	pageSize = ps;
        solr = s;
    }
    HoboIndex index;
    StylootSolr solr;
    int pageSize;

    public List<String> findWithText(String category_name, List<String> features, byte red, byte green, byte blue, double colorDist, int cost_min, int cost_max, String text,int page) throws TException {
              
        boolean hasText = (text!=null && !text.isEmpty() && !text.trim().isEmpty());
        boolean hasColor = (colorDist > 0);
        boolean hasCategory = !(category_name.equals("/"));
        boolean hasFeatures = !(features== null || features.size() == 0);
        boolean hasCost = (cost_min > 0 || cost_max < Integer.MAX_VALUE); 
        
        //If its a text only query directly call the solr class
        if (hasText && !hasColor && !hasCategory && !hasFeatures && !hasCost){
                  return solr.doSimpleTextSearch(text,page,pageSize);
        }       

        Vector<String> result = new Vector<String>(pageSize);
	int count = 0;
	Iterator<Item> iter;
	log.info("Running query with cost: " + cost_min + ", " + cost_max);
	if (colorDist > 0) {
	    iter = index.find(category_name, features, CIELabColor.CIELabFromRGB(red, green, blue), colorDist, cost_min, cost_max);
	} else {
	    iter = index.find(category_name, features, null, 0, cost_min, cost_max);
	}
 
        //Now if it has text pass the iterator to the class and return the results from there
        if(hasText){
           return solr.doCombinedTextSearch(iter,text,page,pageSize);
        }
        
	while (iter.hasNext()) {
	    Item item = iter.next();
	    if (count >= page*pageSize) {
		result.add(item.id);
	    }
	    if (count >= (page+1)*pageSize) {
		break;
	    }
	    count += 1;
	}
	return result;
    }

    public List<String> find(String category_name, List<String> features, byte red, byte green, byte blue, double colorDist, int cost_min, int cost_max, int page) throws TException {
       
           return findWithText(category_name,features,red,green,blue,colorDist,cost_min,cost_max,"",page);

    }
}
