package AffiliationResolver;

import gov.nih.nlm.ncbi.www.soap.eutils.EFetchPubmedServiceStub.AuthorType;
import gov.nih.nlm.ncbi.www.soap.eutils.EFetchPubmedServiceStub.PubmedArticleType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class findPublications {
	static PubMed pubMed = new PubMed();
	static String fileName = "/Users/mulligen/Documents/EMC/Projects/INBIOMEDVision/data/members.csv";
		
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

	
	public static void main(String[] args) throws IOException {
		Map<String, ArrayList<String>> members = getContent(new File(fileName));
		Map<String,Author> authors = new TreeMap<String,Author>();
		for ( String key : members.keySet()){
			List<PubmedArticleType> articles = pubMed.getArticles( members.get(key) );
			for ( int a = 0 ; a < articles.size() ; a++ ){
				AuthorType[] pubmedAuthors = articles.get(a).getMedlineCitation().getArticle().getAuthorList().getAuthor();
				for ( int j = 0 ; j < pubmedAuthors.length ; j++ ){
					authors.put(key, new Author( pubmedAuthors[j].getAuthorTypeChoice_type0().getAuthorTypeSequence_type0().getLastName(),
							pubmedAuthors[j].getAuthorTypeChoice_type0().getAuthorTypeSequence_type0().getInitials(),
							pubmedAuthors[j].getAuthorTypeChoice_type0().getAuthorTypeSequence_type0().getForeName() ) );
				}
			}
			
			if ( members.get(key).size() < 5 ){
				/*
				 * augment the list of publications; first seek publications with the member as first author
				 */
				//pubMed.searchArticles(key, 5, authors, prevArticles)
			}
			break;
		}
		
		for ( int i = 0 ; i < authors.size() ; i++ ){
			System.out.println( authors.get(i).getFullname() );
		}
	}
}
