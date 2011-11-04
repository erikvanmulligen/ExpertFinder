package AffiliationResolver;

import gov.nih.nlm.ncbi.www.soap.eutils.EFetchPubmedServiceStub.AuthorListType;
import gov.nih.nlm.ncbi.www.soap.eutils.EFetchPubmedServiceStub.AuthorType;
import gov.nih.nlm.ncbi.www.soap.eutils.EFetchPubmedServiceStub.PubmedArticleType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class FindContext {
	static PubMed pubMed = new PubMed();

	
	static public Map<String,ArrayList<String>> getContent(File aFile) {
		Map<String,ArrayList<String>> result = new TreeMap<String,ArrayList<String>>(); 
		
	    try {
	      BufferedReader input =  new BufferedReader(new FileReader(aFile));
	      try {
	        String line = null; //not declared within while loop
	        while (( line = input.readLine()) != null){
	        	String pieces[] = line.split("\t");
	        	result.put(pieces[0], new ArrayList<String>(Arrays.asList(pieces[2].split(","))));
	        }
	      }
	      finally {
	        input.close();
	      }
	    }
	    catch (IOException ex){
	      ex.printStackTrace();
	    }
		return result;
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
	
	public static void main(String[] args) throws IOException {
		//String fileName = "authors.last";
		//String outFilename = "authorData.tsv";
		String fileName = "members.csv";
		String outFilename = "memberData.tsv";
		Map<String, ArrayList<String>> authors = getContent(new File("/Users/mulligen/Documents/EMC/Projects/INBIOMEDVision/data/" + fileName));
		PrintWriter out = new PrintWriter(new FileWriter("/Users/mulligen/Documents/EMC/Projects/INBIOMEDVision/data/" + outFilename) );
		
		for ( String key : authors.keySet()){
			AuthorContext authorContext = new AuthorContext();
			
			authorContext.setAuthorName(key);
			System.out.println( "PMIDS: " + authors.get(key));
			List<PubmedArticleType> articles = pubMed.getArticles(authors.get(key));
			
			for ( int i = 0 ; i < articles.size() ; i++ ){
				System.out.println( key + ": " + articles.get(i).getMedlineCitation().getPMID() );
			}
			
			checkArticles( authorContext, articles );
			System.out.println( "e-mail = " + authorContext.getEmail() );
			if ( authorContext.getEmail() == null ){
				/*
				 * try XX other publications for author with a possible e-mail address
				 */
				Integer max = 20;
				System.out.println( "before searching additional articles for " + authorContext.getAuthorName() );
				articles = pubMed.searchArticles(authorContext.getAuthorName(), max, authorContext.getAuthors(), authors.get(key) );
				checkArticles( authorContext, articles );
			}
			//authorContext.check();
			authorContext.write(out);
		}
		out.close();
	}

}
