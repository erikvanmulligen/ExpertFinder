package AffiliationResolver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Affiliation {


	public String extractEmail(String content) {
		String email = null;
		String regex = "(\\w+)(\\.\\w+)*@(\\w+\\.)(\\w+)(\\.\\w+)*";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(content);
		while (matcher.find()) {
			email = matcher.group();

			if(!isValidEmailAddress(email)){
				email=null;
			}

			break;
		}
		return email;
	}

	public boolean isValidEmailAddress(String emailAddress) {
		String expression = "^[\\w\\-]([\\.\\w])+[\\w]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
		CharSequence inputStr = emailAddress;
		Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(inputStr);
		return matcher.matches();

	}
	
	public List<String> find( String name ){
		
		List<String> affiliations = new ArrayList<String>();

		try {
			URL url = new URL("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pubmed&term=" + URLEncoder.encode(name + "[First Author]", "ascii") );
			HttpURLConnection connection = (HttpURLConnection) url.openConnection(); 
			connection.setDoOutput(true); 
			connection.setInstanceFollowRedirects(false); 
			connection.setRequestMethod("GET"); 
			connection.setRequestProperty("Content-Type", "application/xml"); 

			InputStream responseBodyStream = connection.getInputStream();
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(responseBodyStream);

			NodeList abstractIds = doc.getElementsByTagName("Id");
			for ( int i = 0 ; i < abstractIds.getLength() ; i++ ){
				//System.out.println( "id = " + abstractIds.item(i).getTextContent() );
				List<AffiliationMatch> affiliation = getAffiliations( name, abstractIds.item(i).getTextContent() );
				System.out.println( "affiliation = " + affiliation );
			}
			
			connection.disconnect();

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} 

		return affiliations;
	}

	/*
	 * Collect from the list of publications retrieved from PubMed per abstract the author that is closest to the 
	 * affilitation with the actual affiliation
	 */
	private List<AffiliationMatch> getAffiliations(String name, String Id) {
		List<AffiliationMatch> affiliationMatches = new ArrayList<AffiliationMatch>();
		
		try {
			URL url = new URL("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&retmode=XML&id=" + Id );
			HttpURLConnection connection = (HttpURLConnection) url.openConnection(); 
			connection.setDoOutput(true); 
			connection.setInstanceFollowRedirects(false); 
			connection.setRequestMethod("GET"); 
			connection.setRequestProperty("Content-Type", "application/xml"); 

			InputStream responseBodyStream = connection.getInputStream();
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(responseBodyStream);

			NodeList affiliations = doc.getElementsByTagName("Affiliation");
			for ( int i = 0 ; i < affiliations.getLength() ; i++ ){
				String affiliation = affiliations.item(i).getTextContent();
				String email = extractEmail(affiliation);
				StringDistance sd = new StringDistance();
				List<String> authorList = new ArrayList<String>();
				Map<String,Author> authorObjects = new TreeMap<String,Author>();
				
				if ( email != null ){
					NodeList authors = doc.getElementsByTagName("Author");
					for ( int a = 0 ; a < authors.getLength() ; a++ ){
						String foreName = null, lastName = null, initials = null;
						
						Node child = authors.item(a).getFirstChild();
						while ( child != null ){
							if ( child.getNodeName().equalsIgnoreCase("forename") ){
								foreName = child.getTextContent();
							}
							else if ( child.getNodeName().equalsIgnoreCase("lastname") ){
								lastName = child.getTextContent();
							}
							else if ( child.getNodeName().equalsIgnoreCase("initials") ){
								initials = child.getTextContent();
							}
							child = child.getNextSibling();
						}
						String searchName = lastName + " " + initials;
						System.out.println( "searchName = " + searchName );
						authorObjects.put(searchName, new Author( lastName, initials, foreName ) );
						authorList.add( searchName );
					}
					
					String result = sd.getClosestLevenshteinDistance(email, authorList );
					int distance = sd.getLevenshteinDistance(email, result);
					Author selectedAuthor = authorObjects.get(result);
					if ( name.equalsIgnoreCase(selectedAuthor.searchName())){
						System.out.println( selectedAuthor.output() + " is closest to " + email + " with distance " + distance + " for affiliation " + affiliation);
						System.out.println( "authors: " + authorList  );
					}
					//affiliationMatches.add( new AffiliationMatch( result.lastName, initials, foreName, distance, email, affiliation));
				}
			}
			
			connection.disconnect();
			
			return affiliationMatches;

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} 
		
		return null;
	}
}
