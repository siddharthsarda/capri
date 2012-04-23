package org.styloot.capri.thriftserver;

import java.io.IOException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TBinaryProtocol.Factory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.io.*;

import org.styloot.capri.thriftserver.*;
import org.styloot.capri.gen.*;
import org.styloot.capri.*;
import org.styloot.hobo.*;
import org.styloot.hobo.*;
import org.styloot.hobo.hoboindex.*;
import org.styloot.capri.solr.*;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import java.util.zip.*;


public class Server {
    private static final Logger log = LoggerFactory.getLogger(Server.class);

    public Server(HoboIndex i,StylootSolr s, int pt, int ps) {
	index = i;
	port = pt;
	pageSize = ps;
        solr = s;
    }

    private HoboIndex index;
    private int port;
    private int pageSize;
    private StylootSolr solr;

    private void start(){
        try {
            TServerSocket serverTransport = new TServerSocket(port);
            Capri.Processor processor = new Capri.Processor(new CapriServerImpl(index,solr,pageSize));
	    TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));
            server.serve();
        } catch (TTransportException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) throws Exception {
	log.info("Loading list of items from " + args[0]);
        ItemParser parser = parseItemsFromInputStream(new File(args[0]));	
	Collection<Item> items = parser.getResults();
	log.info("Loaded " + items.size() + " items.");
	log.info("Items are indexed with " + Feature.count() + " features.");
        HoboIndex index = new SimpleHoboIndex(items);
	log.info("Build SimpleHoboIndex.");
        StylootSolr solr = new StylootSolr();
        solr.addDocumentsToSolr(parser.getSolrItems());
	int port = Integer.parseInt(args[1]);
	int pageSize = Integer.parseInt(args[2]);
        Server srv = new Server(index, solr,port, pageSize);
	log.info("Starting CapriServer - listening on port " + port + ", pagesize=" + pageSize);
        srv.start();

    }

    private static ItemParser parseItemsFromInputStream(File infile) throws Exception {
                ItemParser parser = new ItemParser();
                SAXParserFactory spf = SAXParserFactory.newInstance();
	        //get a new instance of parser
		SAXParser sp = spf.newSAXParser();
                //Convert the gzip file to a GzipInputFileStream
                InputStream fis = new FileInputStream(infile);  
                GZIPInputStream gis = new GZIPInputStream(fis); 
		//parse the file and also register this class for call backs
		sp.parse(gis, parser);
                return parser;
    }
}

class ItemParser extends DefaultHandler{
       private Vector<Item> result;
       private Vector<StylootSolrItem> solrItems;
       String id;
       String categoryName;
       ArrayList<String> features_array;
       CIELabColor color;
       String qualityScore;
       String price;
       String name;
       String description;

       public  ItemParser(){
               result = new Vector<Item>(); 
               solrItems = new Vector<StylootSolrItem>();         
       }
       public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                if (qName.equalsIgnoreCase("item"))
                {
                      categoryName = "";
                      features_array = new ArrayList<String>();
                      color = null;
                      qualityScore = "";
                      price = "";
                      name = "";
                      description = "";
                      id = attributes.getValue("id");
                }
                else if (qName.equalsIgnoreCase("category"))
                {
                      categoryName = attributes.getValue("name");
                }
                else if (qName.equalsIgnoreCase("feature"))
                {
                      features_array.add(attributes.getValue("value"));
                }
                else if (qName.equalsIgnoreCase("quality-score"))
                {
                      qualityScore = attributes.getValue("value");
                }
                else if (qName.equalsIgnoreCase("price"))
                {
                      price = attributes.getValue("value");
                }
                else if (qName.equalsIgnoreCase("color"))
                {
                   String colorStr = attributes.getValue("value");
                   if (!colorStr.equals("")) {
                    String[] colorStrings = colorStr.split(",");
                    color = CIELabColor.CIELabFromRGB(Integer.parseInt(colorStrings[0]), Integer.parseInt(colorStrings[1]), Integer.parseInt(colorStrings[2]));
                   }
                }
                else if (qName.equalsIgnoreCase("name"))
                {
                      name = attributes.getValue("value");
                }
                else if (qName.equalsIgnoreCase("description"))
                {
                      description = attributes.getValue("value");
                }


                
	}

	public void characters(char[] ch, int start, int length) throws SAXException {
		return;
	}
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if(qName.equalsIgnoreCase("item")) {
                        String[] features = new String[features_array.size()];
                        features_array.toArray(features);
			result.add(new Item(id, categoryName, features, Integer.parseInt(qualityScore), color, Integer.parseInt(price)));
                        solrItems.add(new StylootSolrItem(id,name,description,Integer.parseInt(qualityScore)));
	        }
        }

        public Vector<Item> getResults()
        {
                 return result;
        }

        public Vector<StylootSolrItem> getSolrItems()
        {
                 return solrItems;
        }
}  

