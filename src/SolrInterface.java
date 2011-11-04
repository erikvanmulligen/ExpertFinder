
import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

public class SolrInterface {

	// the remote SOLR instance that you need to connect to, could be on the same machine or a different machine
	private static final String serverURL = "http://localhost:8080/";
	private final SolrServer inbiomedvisionServer;

	private SolrServer createServer( String URL ){
		SolrServer server = null;
		try {
			server = new CommonsHttpSolrServer(URL);
		} catch(MalformedURLException e) {
			System.err.println("Failed to create SolrServer for "+URL);
			e.printStackTrace();
		}
		return server;
	}
	
	// Configuration options for the connector
	public SolrInterface() throws MalformedURLException {
		inbiomedvisionServer = this.createServer( serverURL + "inbiomedvision-pubmed/" );
	}
	
	public void commit() {
		try {
			System.out.println("Committing...");
			inbiomedvisionServer.commit();
			
			System.out.println("Optimizing...");
			inbiomedvisionServer.optimize();
		} catch (IOException e) {
			System.err.println("An general IO error occured:");
			e.printStackTrace();
		} catch (SolrServerException e) {
			System.err.println("A solr specific error occured:");
			e.printStackTrace();
		}
	}
	
	public void clear(){
		try {
			inbiomedvisionServer.deleteByQuery("*:*");
		} catch (SolrServerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public SolrDocumentList query(String table, String query) throws SolrServerException {
		SolrServer server = null;
		if ( table.equalsIgnoreCase("inbiomedvision")){
			server = inbiomedvisionServer;
		}
		
		SolrQuery solrQuery = new SolrQuery().setQuery(query).setStart(0).setRows(120000);
		QueryResponse queryResponse = server.query(solrQuery);
		return queryResponse.getResults();
	}
	
	public Boolean addINBIOMEDVisionRecord( DatabaseInfo databaseInfo ){
		SolrInputDocument doc = new SolrInputDocument();
		for ( int i = 0 ; i < databaseInfo.getAuthors().size() ; i++ ){
			System.out.println("addRecord(): pmid = " + databaseInfo.getPmid() );
			doc.addField("pmid", databaseInfo.getPmid() );
			doc.addField("title", databaseInfo.getTitle());
			doc.addField("queryterm", databaseInfo.getQueryTerm());
			doc.addField("date", databaseInfo.getDate());
			doc.addField("journal", databaseInfo.getJournal());
			doc.addField("firstname", databaseInfo.getAuthors().get(i).getFirstName());
			doc.addField("lastname", databaseInfo.getAuthors().get(i).getLastName());
			doc.addField("initials", databaseInfo.getAuthors().get(i).getInitials());
			doc.addField("lastauthor", databaseInfo.getAuthors().get(i).getLastAuthor());
			try {
				inbiomedvisionServer.add( doc );
			} catch (SolrServerException e) {
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	
}
