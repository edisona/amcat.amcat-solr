To run Solr modifed for Amcat, use 

  java -jar start.jar

in this directory, and when Solr is started connect to 

  http://localhost:8983/solr/admin/

See also README.txt in the solr subdirectory, and check
http://wiki.apache.org/solr/SolrResources for a list of tutorials and
introductory articles.

By default, start.jar starts Solr in Jetty using the default solr home
directory of "./solr/" -- To run other example configurations, you can
speciy the solr.solr.home system property when starting jetty...

  java -Dsolr.solr.home=multicore -jar start.jar
  java -Dsolr.solr.home=example-DIH -jar start.jar


Basic Directory Structure
-------------------------

    java/
    
        Contains the custom Java code. This is an analyzer and a custom Similarity class. Compiling can be done using the command "mvn package".
        The resulting .jar file has to be moved inside the Solr .war file (at least for the analyzer at this moment, the /lib folder did not work).
        
        
    scripts/
    
        Contains some example Python scripts that query and add documents to Solr
        
        
    webapps/
    
        Contains the .war file
        
        
    work/
        
        Directory used by Jetty. If you update the .war file, you might need to clean this directory, to remove the cached files
        
    