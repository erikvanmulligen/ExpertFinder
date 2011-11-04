import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This program will populate a SOLR database with information collected from PubMed. This will be used to create the necessary
 * list for the researcher index
 * 
 * @author mulligen
 *
 */
public class CreateDatabase {
	static String DataFolder = "/Users/mulligen/Documents/EMC/Projects/INBIOMEDVision/data/";
	static String[] files = {"text mining.xml"};
	static XPath xPath;
	static DocumentBuilder builder;	
	static SolrInterface solrInterface = null;
	
	/**
	 * @param args
	 * @throws MalformedURLException 
	 */
	public static void main(String[] args) throws MalformedURLException {
		xPath = XPathFactory.newInstance().newXPath();
		solrInterface = new SolrInterface();
		solrInterface.clear();
		
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		for ( int i = 0 ; i < files.length ; i++ ){
			processFile( files[i].replace( ".xml", "" ), DataFolder + files[i] );
		}
	}

	public static List<Author> getAuthorInfo( String expression, Document doc, String[] nodes ){
		List<Author> response = new ArrayList<Author>();
		Author author = null;
		
		try {
			NodeList nodeList = (NodeList) xPath.evaluate( expression, doc, XPathConstants.NODESET );
		    for ( int i = 0 ; i < nodeList.getLength() ; i++ ){
	    		author = new Author();
	    		author.setLastAuthor( i == ( nodeList.getLength() - 1 ) );
	    		response.add( author );
	    		for ( int n = 0 ; n < nodes.length ; n++ ){
	    			if ( nodes[n].equalsIgnoreCase("./ForeName") ){
	    				author.setFirstName( (String) xPath.evaluate( nodes[n], nodeList.item(i) ) );
	    			}
	    			else if ( nodes[n].equalsIgnoreCase("./Initials") ){
	    				author.setInitials( (String) xPath.evaluate( nodes[n], nodeList.item(i) ) );
	    			}
	    			else if ( nodes[n].equalsIgnoreCase("./LastName") ){
	    				author.setLastName( (String) xPath.evaluate( nodes[n], nodeList.item(i) ) );
	    			}
	    		}
		    }
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		
		return response;
	}
	
	public static DatabaseInfo processXML(String xml){
		DatabaseInfo databaseInfo = new DatabaseInfo();
		
		//System.out.println( xml );
		
		String pmidExpression         = "/PubmedArticle/MedlineCitation/PMID[@Version='1']";
		String authorExpression       = "/PubmedArticle/MedlineCitation/Article/AuthorList/Author";
		String journalExpression      = "/PubmedArticle/MedlineCitation/Article/Journal/Title";
		String titleExpression        = "/PubmedArticle/MedlineCitation/Article/ArticleTitle";
		String pubDateYearExpression  = "/PubmedArticle/MedlineCitation/DateCreated/Year";
		String pubDateMonthExpression = "/PubmedArticle/MedlineCitation/DateCreated/Month";
		String pubDateDayExpression   = "/PubmedArticle/MedlineCitation/DateCreated/Day";
		
		try {
			Document doc = builder.parse( new InputSource( new StringReader( xml ) ) );	

			databaseInfo.setPmid(xPath.evaluate( pmidExpression, doc ) );
			String[] nodes = {"./LastName", "./ForeName", "./Initials" };
			databaseInfo.setAuthors( getAuthorInfo(authorExpression, doc, nodes) );
			databaseInfo.setJournal(xPath.evaluate(journalExpression, doc) );
			databaseInfo.setTitle(xPath.evaluate(titleExpression,doc));
			databaseInfo.setDate(xPath.evaluate(pubDateDayExpression,doc)+"/"+xPath.evaluate(pubDateMonthExpression,doc)+"/"+xPath.evaluate(pubDateYearExpression,doc));
		} catch (XPathExpressionException e) {
			e.printStackTrace();
			System.exit(0);
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
		return databaseInfo;
	}
	
	public static void processFile(String queryTerm, String fileName){
	    StringBuilder contents = new StringBuilder();
	    Scanner scanner;
		try {
			scanner = new Scanner(new FileInputStream(fileName), "UTF-8");
		    try {
			      while (scanner.hasNextLine()){
			        String line = null; //not declared within while loop
			        Boolean found = false;
			        while (( line = scanner.nextLine()) != null){
			        	if ( line.equalsIgnoreCase("<PubmedArticle>")){
			        		found = true;
			        		if ( contents.length() > 0 ){
			        			System.out.println("before processing XML");
			        			DatabaseInfo databaseInfo = processXML(contents.toString());
			        			databaseInfo.setQueryTerm(queryTerm);
		        				solrInterface.addINBIOMEDVisionRecord(databaseInfo);
		        				System.out.println("after processing XML");
			        			contents.delete(0, contents.length());
			        			System.out.println("length contents = " + contents.length());
			        			break;
			        		}
			        	}
			        	if ( found ){
			        		contents.append(line);
			        		contents.append(System.getProperty("line.separator"));
			        	}
			        }
			        break;
			      }
			    }
			    finally{
					if ( contents.length() > 0 ){
						DatabaseInfo databaseInfo = processXML(contents.toString());
						databaseInfo.setQueryTerm(queryTerm);
	        			System.out.println(databaseInfo.toString());
						solrInterface.addINBIOMEDVisionRecord(databaseInfo);
						contents.delete(0, contents.length());
					}
					scanner.close();
			    }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
