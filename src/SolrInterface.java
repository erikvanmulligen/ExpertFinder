
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
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
	private static SolrServer inbiomedvisionServer = null, researcherIndexServer = null;
	
	
	private enum Fields { affiliation, bioinformatics, clinicalcare, date, firstauthor, firstname, fullname, initials, journal, lastauthor,
						  lastname, medicalinformatics, pmid, queryterm, title };

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
		researcherIndexServer = this.createServer( serverURL + "researcherIndex/" );
		if (clear){
			inbiomedvisionServer.deleteByQuery("*:*");
			inbiomedvisionServer.commit();
		}
		researcherIndexServer.deleteByQuery("*:*");
		researcherIndexServer.commit();			
	}
	
	public void commit() {
		try {
			System.out.println("Committing...");
			inbiomedvisionServer.commit();
			researcherIndexServer.commit();
			
			System.out.println("Optimizing...");
			inbiomedvisionServer.optimize();
			researcherIndexServer.optimize();
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
	
	/**
	 * function that retrieves the list of authors from the database
	 * @return
	 * @throws SolrServerException 
	 */
	public static List<String> getAuthors( Boolean lastAuthor ) throws SolrServerException{
		List<String> result = new ArrayList<String>();
		SolrQuery q = new SolrQuery();
		q.setQuery( lastAuthor ? "lastauthor:true" : "*:*" );
		q.setFacet( true );
		q.addFacetField( "fullname" );
		q.setRows( 0 );
		q.setFacetMinCount(1);
		Integer start = 0;
		Boolean more = true;
	
		while ( more ){
			q.setParam( "facet.offset", start.toString() );
			QueryResponse r = inbiomedvisionServer.query( q );
			
			FacetField fieldFacet = r.getFacetField("fullname");
			more = false;
			if (fieldFacet != null && fieldFacet.getValueCount() > 0) {
				more = true;
				for (Count count : fieldFacet.getValues()) {
					String name = count.getName().trim();
					if ( name.length() > 0 ){
						result.add(name);
					}
					start += 1;
				}
			}
		}
		
		return result;
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
	
	public Boolean addResearcherIndex( AuthorInfo authorInfo ){
		SolrInputDocument doc = new SolrInputDocument();
		doc.addField("affiliation", authorInfo.compileAffiliation() );
		doc.addField("coauthors", authorInfo.getCoauthors() );
		doc.addField("domain", authorInfo.getDomain() );
		doc.addField("domainSearch", authorInfo.getDomain() );
		doc.addField("email", authorInfo.getEmail() );
		doc.addField("name", authorInfo.getName() );
		doc.addField("pmids", authorInfo.getPmidsAsString() );
		
		try {
			researcherIndexServer.add( doc );
		} catch (SolrServerException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public Boolean addINBIOMEDVisionRecord( DatabaseInfo databaseInfo ){
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
				doc.addField("firstauthor", databaseInfo.getAuthors().get(i).getFirstAuthor());
				doc.addField("bioinformatics", databaseInfo.getBioInformatics() );
				doc.addField("medicalinformatics", databaseInfo.getMedicalInformatics() );
				doc.addField("clinicalcare", databaseInfo.getClinicalCare() );
				
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
				//System.out.println( "skipped asian record " + databaseInfo.getPmid() );
			}
		}
		return true;
	}

	public List<String> getPmids(String authorName,Boolean first) throws SolrServerException {
		List<String> pmids = new ArrayList<String>();
		
		SolrQuery q = new SolrQuery();
		q.setQuery( "fullname:\"" + authorName + "\"" + ( first ? " AND firstauthor: true" : "" ) );
		q.setRows( 100 );
		Integer start = 0;
		Boolean more = true;
	
		while ( more ){
			q.setStart(start);
			QueryResponse r = inbiomedvisionServer.query( q );
			SolrDocumentList docs = r.getResults();
			more = docs.size() == 100;
			start += 100;
			for ( int i = 0 ; i < docs.size(); i++ ){
				pmids.add( docs.get(i).getFieldValue("pmid").toString() );
			}
		}
			
		return pmids;
	}


	public List<String> getCoauthors(String key) throws SolrServerException {
		List<String>coauthors = new ArrayList<String>();
		List<String> pmids = getPmids(key,false);
		
		//System.out.println( "pmids = " + StringUtils.join(pmids,","));
		SolrQuery q = new SolrQuery();
		q.setQuery( "pmid:(" + StringUtils.join( pmids, "," ) + ")" );
		q.setRows( 100 );
		Integer start = 0;
		Boolean more = true;
	
		while ( more ){
			q.setStart(start);
			QueryResponse r = inbiomedvisionServer.query( q );
			SolrDocumentList docs = r.getResults();
			more = docs.size() == 100;
			start += 100;
			for ( int i = 0 ; i < docs.size(); i++ ){
				coauthors.add( docs.get(i).getFieldValue("lastname").toString() );
			}
		}

		return coauthors;
	}

	public List<String> findCoauthors(String pmid) throws SolrServerException {
		List<String>coauthors = new ArrayList<String>();
		
		SolrQuery q = new SolrQuery();
		q.setQuery( "pmid:" + pmid );
		q.setRows( 100 );
		Integer start = 0;
		Boolean more = true;
	
		while ( more ){
			q.setStart(start);
			QueryResponse r = inbiomedvisionServer.query( q );
			SolrDocumentList docs = r.getResults();
			more = docs.size() == 100;
			start += 100;
			for ( int i = 0 ; i < docs.size(); i++ ){
				coauthors.add( docs.get(i).getFieldValue("fullname").toString() );
			}
		}

		return coauthors;
	}
	
	public List<String> retrieveDomains(String author) throws SolrServerException{
		List<String> result = new ArrayList<String>();
		SolrQuery q = new SolrQuery();
		q.setRows( 1 );
		q.setQuery( "fullname:\"" + author + "\" AND bioinformatics:true" );
		if ( inbiomedvisionServer.query( q ).getResults().size() == 1 ) result.add( "bioinformatics" );
		q.setQuery( "fullname:" + author + " AND medicalinformatics:true" );
		if ( inbiomedvisionServer.query( q ).getResults().size() == 1 ) result.add("medicalinformatics,");
		q.setQuery( "fullname:" + author + " AND clinicalcare:true" );
		if ( inbiomedvisionServer.query( q ).getResults().size() == 1 ) result.add("clinicalcare");
		return result;		
	}

	public InitialsLastName retrieveInitialsAndLastName( String author ) throws SolrServerException {
		InitialsLastName result = new InitialsLastName();
		
		SolrQuery q = new SolrQuery();
		q.setQuery( "fullname:\"" + author + "\"" );
		q.setRows( 1 );
		QueryResponse r = inbiomedvisionServer.query( q );
		SolrDocumentList docs = r.getResults();
		if ( docs.size() == 1 ){
			result.setInitials(docs.get(0).getFieldValue("initials").toString());
			result.setLastName(docs.get(0).getFieldValue("lastname").toString());
		}
		return result;
	}

	public String retrieveAffiliation(String author) throws SolrServerException {
		SolrQuery q = new SolrQuery();
		q.setQuery( "fullname:\"" + author + "\"");
		q.setFields( "affiliation" );
		q.setRows( 1 );
		QueryResponse r = inbiomedvisionServer.query( q );
		SolrDocumentList docs = r.getResults();
		return docs.size() == 1 ? docs.get(0).getFieldValue("affiliation").toString() : null;
	}

	public AuthorInfo getAuthorInfoFromDB(String author) throws SolrServerException {
		AuthorInfo authorInfo = new AuthorInfo();
		SolrQuery q = new SolrQuery();
		q.setQuery( "fullname:\"" + author + "\"");
		q.setRows( 100 );
		Integer start = 0;
		Boolean more = true;
	
		while ( more ){
			q.setStart(start);
			QueryResponse r = inbiomedvisionServer.query( q );
			SolrDocumentList docs = r.getResults();
			more = docs.size() == 100;
			start += 100;
			for ( int i = 0 ; i < docs.size(); i++ ){
				String pmid = null;
				for ( String fieldName : docs.get(i).getFieldNames() ){
					switch (Fields.valueOf(fieldName)){
						case affiliation:
							//authorInfo.addAffiliation(docs.get(i).getFieldValue(fieldName).toString());
						break;
						case bioinformatics:
							authorInfo.setBioInformatics(docs.get(i).getFieldValue(fieldName).toString().equalsIgnoreCase("true"));
						break;
						case clinicalcare:
							authorInfo.setClinicalCare(docs.get(i).getFieldValue(fieldName).toString().equalsIgnoreCase("true"));
						break;
						case medicalinformatics:
							authorInfo.setMedicalInformatics(docs.get(i).getFieldValue(fieldName).toString().equalsIgnoreCase("true"));
						break;
						case date:
						break;
						case firstname:
							authorInfo.setFirstName( docs.get(i).getFieldValue( fieldName ).toString() );
						break;
						case fullname:
							authorInfo.setFullName( docs.get(i).getFieldValue( fieldName ).toString() );
						break;
						case initials:
							authorInfo.setInitials( docs.get(i).getFieldValue( fieldName ).toString() );
						break;
						case journal:
						break;
						case lastname:
							authorInfo.setLastName( docs.get(i).getFieldValue( fieldName ).toString() );
						break;
						case pmid:
							pmid = docs.get(i).getFieldValue(fieldName).toString();
							if ( docs.get(i).getFieldValue("firstauthor").toString().equalsIgnoreCase("true")){
								authorInfo.addPmidFirst( pmid );
							}
							else if ( docs.get(i).getFieldValue("lastauthor").toString().equalsIgnoreCase("true")){
								authorInfo.addPmidLast( pmid );
							}
							else {
								
							}
						break;
						case queryterm:
						break;
						case title:
						break;
					}	
				}
				List<String> coauthorList = findCoauthors(pmid);
				authorInfo.addAffiliation(docs.get(i).getFieldValue("affiliation").toString(), coauthorList );
			}
		}

		return authorInfo;
	}

	
}
