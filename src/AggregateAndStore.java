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
		//solrInterface.clearRI();
		
		List<String> authorsProcessed = SolrInterface.getAuthorsAlreadyStored();
		List<String> authorNames = SolrInterface.getAuthors(true);
		
		int count = 0;
		for ( String author : authorNames ){
			count += 1;
			System.out.println( "processing " + count + " of " + authorNames.size() );
			
			if ( authorsProcessed.contains(author) ){
				if ( ( count >= 3327 && count <= 3328 ) || ( count >= 9769 && count <= 9843 ) ){
					System.out.println( author + " has already been processed with failure." );
				}
				else{
					System.out.println( author + " has already been processed." );
					continue;
				}
			}

			try{
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
			}
			catch ( Exception e ){
				System.out.println( "error for " + author );
			}
		}
	}
}