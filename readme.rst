========
Capri
========

Capri builds on Hobo(https://github.com/stucchio/Hobo) and provides text search in addition to the filtering that Hobo already provides.


Dependencies
============

Apache Thrift 0.8

sl4j for logging.

Apache Solr 1.4

When compiling, make sure you edit the build.xml file to point to the right location/version of the dependent libraries.


Installation
============

The server installation is easy::

    $ ant compile jar
    $ cp build/capri.jar SOMEPLACE_IN_CLASSPATH
Its also requires solr.

Client
------

It supports two methods: 
find(1:string category_name, 2:list<string> features, 3:byte red, 4:byte green, 5:byte blue, 6:double colorDist, 7:i32 cost_min, 8:i32 cost_max, 9:i32 page)::

findWithText(1:string category_name, 2:list<string> features, 3:byte red, 4:byte green, 5:byte blue, 6:double colorDist, 7:i32 cost_min, 8:i32 cost_max,9:string text,10:i32 page)::

Server
------

The server is a read-only indexer. It is run as follows::

    $ java org.styloot.hobo.thriftserver.Server INPUTFILE PORT PAGESIZE

The inputfile is an xml file and has the following format:
<items>
 <item id="cfb34890-08f4-11e1-b39f-0007e95c7626">
      <category name="/Jewelry/Watches"/>
      <quality-score value="4"/>
      <color value="217,185,125"/>
      <name value="Firetrap Alpha Gold Plated Chunky Link Watch"/>
      <description value="A dazzling gold plated watch by Firetrap with a gorgeous round dial;Round dial;Firetrap branded watch face;Chunky link watch;Bracelet style steel strap;Adjustable strap,Material: Stainless steel"/>
      <features>
        <feature value="8cbf7942-de82-11e0-b84a-0007e95c7626"/>
        <feature value="ad183224-429d-4d4a-b32c-f52b079b5068"/>
      </features>
      <price value="39"/>
 </item>
 <item>..
 </item>
</items>

