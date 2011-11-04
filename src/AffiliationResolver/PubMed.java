package AffiliationResolver;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.axis2.AxisFault;

import gov.nih.nlm.ncbi.www.soap.eutils.EFetchPubmedServiceStub;
import gov.nih.nlm.ncbi.www.soap.eutils.EUtilsServiceStub;
import gov.nih.nlm.ncbi.www.soap.eutils.EFetchPubmedServiceStub.AuthorType;
import gov.nih.nlm.ncbi.www.soap.eutils.EFetchPubmedServiceStub.PubmedArticleType;

public class PubMed {
	static EFetchPubmedServiceStub fetchService;
	static EUtilsServiceStub searchService ;

	
	PubMed(){
		try {
			fetchService = new EFetchPubmedServiceStub();
			searchService = new EUtilsServiceStub();
		} catch (AxisFault e) {
			e.printStackTrace();
		}
	}
	
	List<EFetchPubmedServiceStub.PubmedArticleType> getArticles(ArrayList<String> ids){
		List<EFetchPubmedServiceStub.PubmedArticleType> articles = new ArrayList<EFetchPubmedServiceStub.PubmedArticleType>();
		// call NCBI EFetch utility
		for ( int j = 0 ; j < ids.size() ; j++ ){
			try{
				EFetchPubmedServiceStub.EFetchRequest req = new EFetchPubmedServiceStub.EFetchRequest();
				req.setId(ids.get(j));
				EFetchPubmedServiceStub.EFetchResult res = fetchService.run_eFetch(req);
				for (int i = 0; i < res.getPubmedArticleSet().getPubmedArticleSetChoice().length; i++){
					EFetchPubmedServiceStub.PubmedArticleType art = res.getPubmedArticleSet().getPubmedArticleSetChoice()[i].getPubmedArticle();
					if(art!=null) {
						articles.add(art);
					}
				}
			}
			catch (Exception e) { 
			}
		}
		return articles;
	}
	
	String getLastName(String name){
		String[] parts = name.split(",");
		return parts[0].trim();
	}
	
	List<EFetchPubmedServiceStub.PubmedArticleType> searchArticles( String name, Integer maxArticles, Map<String,Author>authors, ArrayList<String> prevArticles ){
		List<EFetchPubmedServiceStub.PubmedArticleType> articles = new ArrayList<EFetchPubmedServiceStub.PubmedArticleType>();
		ArrayList <String> ids = new ArrayList<String>();

		EUtilsServiceStub.ESearchRequest req = new EUtilsServiceStub.ESearchRequest();
		req.setDb("pubmed");

		String q = "";
		for ( String key : authors.keySet() ){
			if ( q.length() > 0 ){
				q += " OR ";
			}
			q += " " + authors.get(key).searchName() + "[AU]";
		}
		q = getLastName(name) + "[First Author] AND (" + q + ")";
		
		System.out.println( "query = " + q );
		
		req.setTerm( q );
		req.setSort( "PublicationDate" );
		req.setRetMax( maxArticles.toString() );
		
		try {
			int N = 0;
			EUtilsServiceStub.ESearchResult res = searchService.run_eSearch(req);
			try{
				N = res.getIdList().getId().length;
			}
			catch (Exception e){
				N = 0;
			}
			
			for (int i = 0; i < N; i++){
				String id = res.getIdList().getId()[i];
				if ( ! prevArticles.contains(id) ){
					ids.add( id );
				}
			}

			System.out.println( "found additional pubs: " + ids );
			List<EFetchPubmedServiceStub.PubmedArticleType> newArticles = this.getArticles(ids);
			
			Iterator<PubmedArticleType> iterator = newArticles.iterator();
			while( iterator.hasNext() ){
				PubmedArticleType article = iterator.next();
				AuthorType[] aus = article.getMedlineCitation().getArticle().getAuthorList().getAuthor();
				int count = -1;
				for ( int a = 0 ; a < aus.length ; a++ ){
					AuthorType author = aus[a];
					try{
						String key = author.getAuthorTypeChoice_type0().getAuthorTypeSequence_type0().getLastName() + ' ' + author.getAuthorTypeChoice_type0().getAuthorTypeSequence_type0().getInitials();
						if ( authors.containsKey(key) ){
							count += 1;
						}
					}
					catch(Exception e){
						//System.out.println( "error in " + article.getMedlineCitation().getPMID().toString() );
					}
				}
				
				if ( count > 0 ){
					//System.out.println( "found an article shared with a coauthor" );
					articles.add(article);
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return articles;
	}
}
