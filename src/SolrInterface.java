
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

public class SolrInterface {

	// the remote SOLR instance that you need to connect to, could be on the same machine or a different machine
	private static final String serverURL = "http://localhost:8080/";
	private final SolrServer inbiomedvisionServer;
	private static String[] domains = {"bioinformatics","medicalinformatics","clinicalcare"};
	

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
	public SolrInterface(Boolean clear) throws SolrServerException, IOException {
		inbiomedvisionServer = this.createServer( serverURL + "inbiomedvision-pubmed/" );
		if (clear){
			inbiomedvisionServer.deleteByQuery("*:*");
			inbiomedvisionServer.commit();
		}
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
	
	public void facetSearch() throws SolrServerException{
		SolrQuery q = new SolrQuery();
		q.setQuery( "lastauthor:true" );
		q.setFacet( true );
		q.addFacetField( "fullname" );
		q.setRows( 0 );
		q.setFacetMinCount(1);
	
		Integer start = 0;

		
		while ( true ){
			q.setParam( "facet.offset", start.toString() );
			QueryResponse r = inbiomedvisionServer.query( q );
			List<FacetField> fields = r.getFacetFields();
			for ( int i = 0 ; i < fields.size() ; i++ ){
				System.out.println( fields.get(i).getName());
			}
			
			int total = 0;
			List<FacetField> fieldFacets = r.getFacetFields();
			if (fieldFacets != null && !fieldFacets.isEmpty()) {
				System.out.println("\nField Facets : ");
				for (FacetField fieldFacet : fieldFacets) {
					System.out.print("\t" + fieldFacet.getName() + " :\t");
					if (fieldFacet.getValueCount() > 0) {
						for (Count count : fieldFacet.getValues()) {
							total += 1;
							System.out.print(count.getName() + "[" + count.getCount() + "]\n");
						}
					}
					System.out.println("");
				}
			}
			System.out.println( "count = " + total );
			start += total;
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
	
	public Boolean isChineseJapanseKorean( String affiliation ){
		String lcAffiliation = affiliation.toLowerCase();
		return lcAffiliation.contains("china") || lcAffiliation.contains( "chinese") || lcAffiliation.contains("japan") || lcAffiliation.contains("korea");
	}
	
	public Boolean addINBIOMEDVisionRecord( DatabaseInfo databaseInfo, String domain ){
		System.out.println(databaseInfo.getQueryTerm() + " add: pmid = " + databaseInfo.getPmid() );
		for ( int i = 0 ; i < databaseInfo.getAuthors().size() ; i++ ){
			if ( ! isChineseJapanseKorean( databaseInfo.getAffiliation() ) ){
				SolrInputDocument doc = new SolrInputDocument();
				doc.addField("pmid", databaseInfo.getPmid() );
				doc.addField("title", databaseInfo.getTitle());
				doc.addField("affiliation", databaseInfo.getAffiliation() );
				doc.addField("queryterm", databaseInfo.getQueryTerm());
				doc.addField("date", databaseInfo.getDateAsCalendar() );
				doc.addField("journal", databaseInfo.getJournal());
				doc.addField("firstname", databaseInfo.getAuthors().get(i).getFirstName());
				doc.addField("lastname", databaseInfo.getAuthors().get(i).getLastName());
				doc.addField("fullname", databaseInfo.getAuthors().get(i).getFullname());
				doc.addField("initials", databaseInfo.getAuthors().get(i).getInitials());
				doc.addField("lastauthor", databaseInfo.getAuthors().get(i).getLastAuthor());
				doc.addField(domain, true);
				for ( int d = 0 ; d < domains.length ; d++ ){
					if ( ! domain.equalsIgnoreCase( domains[d] ) ){
						doc.addField( domains[d], false );
					}
				}
				
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
			else{
				System.out.println( "skipped asian record " + databaseInfo.getPmid() );
			}
		}
		return true;
	}

	
}
