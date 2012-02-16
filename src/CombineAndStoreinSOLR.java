import gov.nih.nlm.ncbi.www.soap.eutils.EFetchPubmedServiceStub.AuthorType;
import gov.nih.nlm.ncbi.www.soap.eutils.EFetchPubmedServiceStub.PubmedArticleType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.common.SolrInputDocument;

import AffiliationResolver.AuthorContext;
import AffiliationResolver.PubMed;

public class CombineAndStoreinSOLR {
	static Map<String,AuthorInfo> authors = new TreeMap<String,AuthorInfo>();
    static SolrInterface solrInterface = null;
    
	static PubMed pubMed = new PubMed();

	public static void readAffiliations(String filename){
	    try {
	    	BufferedReader input =  new BufferedReader(new FileReader(filename));
	    	try {
	    		String line = null; //not declared within while loop
	    		while (( line = input.readLine()) != null){
	    			String[] pieces = line.split("\t");
	    			if ( authors.containsKey(pieces[0])){
		    			AuthorInfo authorInfo = new AuthorInfo();
		    			if ( pieces.length == 4 ){
			    			authorInfo.setName(pieces[0]);
			    			authorInfo.setEmail(pieces[1]);
			    			authorInfo.setAffiliation(pieces[2]);
			    			//authorInfo.setCoauthors(pieces[3]);
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
	    			}
	    			else{
	    				System.out.println( "didn't find author " + pieces[0] );
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
	
	
	public static void main(String[] args) throws SolrServerException, IOException {
		solrInterface = new SolrInterface(false);
		
		List<String> authorNames = solrInterface.getAuthors(true);
		
		for ( String author : authorNames ){
			AuthorInfo authorInfo = new AuthorInfo();
			//System.out.println( author );
			authorInfo.setName( author );
			authors.put( author, authorInfo);
			getAuthorInfo( authorInfo );
		}
//		readAffiliations("/Users/mulligen/Documents/EMC/Projects/INBIOMEDVision/data/authorData.tsv");
//		readPublications("/Users/mulligen/Documents/EMC/Projects/INBIOMEDVision/data/authors.last");
//		for ( String key : authors.keySet() ){
//			System.out.println( key + ": " + authors.get(key).text() );
//		}
	}

	public static void checkArticles( AuthorContext authorContext, List<PubmedArticleType> articles ){
		Iterator<PubmedArticleType> iterator = articles.iterator();
		while( iterator.hasNext() ){
			PubmedArticleType article = iterator.next();
			authorContext.addPubMedId(article.getMedlineCitation().getPMID().toString() );
			
			AuthorType[] aus = article.getMedlineCitation().getArticle().getAuthorList().getAuthor();
			for ( int a = 0 ; a < aus.length ; a++ ){
				authorContext.addAuthor(aus[a]);
			}
			authorContext.addAffiliation( article.getMedlineCitation().getArticle().getAffiliation() );
		}
	}
	
	public static void getAuthorInfo( AuthorInfo authorInfo ) throws IOException, SolrServerException {
		AuthorContext authorContext = new AuthorContext();
		
		String key = authorInfo.getName();
		System.out.println( "getAuthorInfo(): name = " + key );
		authorContext.setAuthorName(key);
		List<String>pmids = solrInterface.getPmids(key,true);
		
		if ( pmids.size() == 0 ){
			ArrayList<String> prevArticles = new ArrayList<String>();
			pubMed.searchArticles(key, 10, solrInterface.getCoauthors(key), prevArticles);
		}
		System.out.println( "PMIDS: " + pmids);
		List<PubmedArticleType> articles = pubMed.getArticles(pmids);
		
		for ( int i = 0 ; i < articles.size() ; i++ ){
			System.out.println( key + ": " + articles.get(i).getMedlineCitation().getPMID() );
		}
		
		checkArticles( authorContext, articles );
		System.out.println( "e-mail = " + authorContext.getEmail() );
//		if ( authorContext.getEmail() == null ){
//			/*
//			 * try XX other publications for author with a possible e-mail address
//			 */
//			Integer max = 20;
//			System.out.println( "before searching additional articles for " + authorContext.getAuthorName() );
//			articles = pubMed.searchArticles(authorContext.getAuthorName(), max, authorContext.getAuthors(), pmids );
//			checkArticles( authorContext, articles );
//		}
		//authorContext.check();
	}

	private static void readPublications(String filename) throws SolrServerException {
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
		    			//researcherIndexServer.add(doc);
		    			//researcherIndexServer.commit();
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
