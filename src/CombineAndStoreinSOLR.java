import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.TreeMap;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.common.SolrInputDocument;

public class CombineAndStoreinSOLR {
	static Map<String,AuthorInfo> authors = new TreeMap<String,AuthorInfo>();
    static CommonsHttpSolrServer server = null; 

	public static void readAffiliations(String filename){
	    try {
	    	BufferedReader input =  new BufferedReader(new FileReader(filename));
	    	try {
	    		String line = null; //not declared within while loop
	    		while (( line = input.readLine()) != null){
	    			String[] pieces = line.split("\t");
	    			AuthorInfo authorInfo = new AuthorInfo();
	    			if ( pieces.length == 4 ){
		    			authorInfo.setName(pieces[0]);
		    			authorInfo.setEmail(pieces[1]);
		    			authorInfo.setAffiliation(pieces[2]);
		    			authorInfo.setCoauthors(pieces[3]);
	    			}
		    		else if ( pieces.length == 3 ){
		    			authorInfo.setName(pieces[0]);
		    			authorInfo.setAffiliation(pieces[1]);
		    			authorInfo.setCoauthors(pieces[2]);
	    			}
		    		else if ( pieces.length == 1 ){
		    			authorInfo.setName(pieces[0]);
	    			}
		    		else{
		    			System.err.println( "unknown number of fields" );
		    		}
		    		authors.put(pieces[0], authorInfo );
	    		}
	    	}
	    	finally {
	    		input.close();
	    	}
	    }
	    catch (IOException ex){
	    	ex.printStackTrace();
	    }
	}
	public static void main(String[] args) throws SolrServerException {
		readAffiliations("/Users/mulligen/Documents/EMC/Projects/INBIOMEDVision/data/authorData.tsv");
		readPublications("/Users/mulligen/Documents/EMC/Projects/INBIOMEDVision/data/authors.last");
		for ( String key : authors.keySet() ){
			System.out.println( key + ": " + authors.get(key).text() );
		}
	}
	private static void readPublications(String filename) throws SolrServerException {
		try {
			server = new CommonsHttpSolrServer("http://localhost:8080/researcherIndex/");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    try {
	    	BufferedReader input =  new BufferedReader(new FileReader(filename));
	    	try {
	    		String line = null; //not declared within while loop
	    		while (( line = input.readLine()) != null){
	    			String[] pieces = line.split("\t");
	    			if ( authors.containsKey(pieces[0]) ){
		    			AuthorInfo authorInfo = authors.get(pieces[0]);
		    			authorInfo.setDomain(pieces[1]);
		    			authorInfo.setPmids(pieces[2]);
		    			SolrInputDocument doc = new SolrInputDocument();
		    			doc.addField("name", authorInfo.getName());
		    			doc.addField("email", authorInfo.getEmail());
		    			doc.addField("affiliation", authorInfo.getAffiliation());
		    			doc.addField("coauthors", authorInfo.getCoauthors());
		    			doc.addField("domain", authorInfo.getDomain());
		    			doc.addField("domainSearch", authorInfo.getDomain());
		    			doc.addField("pmids", authorInfo.getPmids());
		    			server.add(doc);
		    			server.commit();
	    			}
	    			else{
	    				System.err.println( "information on author " + pieces[0] + " is missing");
	    			}
	    		}
	    	}
	    	finally {
	    		input.close();
	    	}
	    }
	    catch (IOException ex){
	    	ex.printStackTrace();
	    }
	}
}
