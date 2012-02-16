import gov.nih.nlm.ncbi.www.soap.eutils.EFetchPubmedServiceStub.PubmedArticleType;

import java.io.IOException;
import java.util.List;
import org.apache.solr.client.solrj.SolrServerException;

import AffiliationResolver.PubMed;

public class AggregateAndStore {
    static SolrInterface solrInterface = null;
    
	static PubMed pubMed = new PubMed();
	
	public static void main(String[] args) throws SolrServerException, IOException {
		solrInterface = new SolrInterface(false);
		
		List<String> authorsProcessed = SolrInterface.getAuthorsAlreadyStored();
		List<String> authorNames = SolrInterface.getAuthors(true);
		
		int count = 0;
		for ( String author : authorNames ){
			count += 1;
			System.out.println( "processing " + count + " of " + authorNames.size() );
			if ( authorsProcessed.contains(author)){
				System.out.println( author + " has already been processed." );
				continue;
			}
			//if ( author.contains("G Raghava") ){
				AuthorInfo authorInfo = solrInterface.getAuthorInfoFromDB(author);
				System.out.println( author );
				authorInfo.setName( author );
	
				if ( authorInfo.getAffiliation() == null || authorInfo.getEmail() == null ){
					authorInfo.getAuthorInfo( pubMed, solrInterface );
					authorInfo.setAffiliation( solrInterface.retrieveAffiliation( author ) );
				}
				if ( authorInfo.getEmail() == null ){
					System.out.println("no e-mail found");
					List<PubmedArticleType> articles = pubMed.getArticles(authorInfo.getPmidFirst());
					authorInfo.findAffiliationAndEmail(articles);
				}
				solrInterface.addResearcherIndex( authorInfo );
			//}
		}
	}
}