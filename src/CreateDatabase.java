import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
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

import org.apache.solr.client.solrj.SolrServerException;
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
	static String[] BioInformatics = {"genomics.xml", "Computational Genomics.xml", "systems_biology.xml", "computational_biology.xml", "chemoinformatics.xml", "modeling and simulation.xml", "genotype_phenotype_resources.xml", "translational_research.xml"};
	static String[] MedicalInformatics = {"medical_informatics.xml", "BioMedical Imaging.xml", "neuroinformatics.xml", "computers.xml", "ontologies.xml", "text mining.xml" };
	static String[] ClinicalApplicationHealthcare = {"health care professionals.xml", "health professionals.xml", "drug industry.xml", "clinical application.xml", "clinical_information_OR_clinical_data.xml", "translational_research.xml"};
	static String DataFolder = "/Users/mulligen/Documents/EMC/Projects/INBIOMEDVision/data/";
	static XPath xPath;
	static DocumentBuilder builder;	
	static SolrInterface solrInterface = null;
	
	static List<ExportPubMedDescriptor> descriptors = new ArrayList<ExportPubMedDescriptor>();
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws SolrServerException 
	 */
	public static void main(String[] args) throws SolrServerException, IOException {
		xPath = XPathFactory.newInstance().newXPath();
		solrInterface = new SolrInterface(true);
		solrInterface.clear();
		
		descriptors.add( new ExportPubMedDescriptor(	"BioInformatics and MedicalInformatics", 
														"/Users/mulligen/Documents/EMC/Projects/INBIOMEDVision/data/MI_BI.xml",
														true, true, false ) );
		descriptors.add( new ExportPubMedDescriptor(	"BioInformatics and ClinicalCare", 
														"/Users/mulligen/Documents/EMC/Projects/INBIOMEDVision/data/BI_CC.xml",
														true, false, true ) );
		descriptors.add( new ExportPubMedDescriptor(	"MedicalInformatics and ClinicalCare", 
														"/Users/mulligen/Documents/EMC/Projects/INBIOMEDVision/data/MI_CC.xml",
														false, true, true ) );
		descriptors.add( new ExportPubMedDescriptor(	"BioInformatics and MedicalInformatics and ClinicalCare", 
														"/Users/mulligen/Documents/EMC/Projects/INBIOMEDVision/data/MI_BI_CC.xml",
														true, true, true ) );

		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		
//		for ( int i = 0 ; i < BioInformatics.length ; i++ ){
//			processFile( BioInformatics[i].replace( ".xml", "" ), DataFolder + BioInformatics[i], true, false, false );
//		}
//		for ( int i = 0 ; i < MedicalInformatics.length ; i++ ){
//			processFile( MedicalInformatics[i].replace( ".xml", "" ), DataFolder + MedicalInformatics[i], false, true, false );
//		}
//		for ( int i = 0 ; i < ClinicalApplicationHealthcare.length ; i++ ){
//			processFile( ClinicalApplicationHealthcare[i].replace( ".xml", "" ), DataFolder + ClinicalApplicationHealthcare[i], false, false, true );
//		}
		
		for ( int i = 0 ; i < descriptors.size() ; i++ ){
			processFile( descriptors.get(i).getDescription(), descriptors.get(i).getFilename(), descriptors.get(i).getBioInformatics(), descriptors.get(i).getMedicalInformatics(), descriptors.get(i).getClinicalCare() );
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
	    		author.setFirstAuthor( i == 0 );
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
		String affiliationExpression  = "/PubmedArticle/MedlineCitation/Article/Affiliation";
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
//			System.out.println( databaseInfo.getPmid() + " affiliation = " + xPath.evaluate(affiliationExpression, doc) );
//			if ( databaseInfo.getPmid().equalsIgnoreCase("21591940") ) System.exit(0);
			databaseInfo.setAffiliation( xPath.evaluate(affiliationExpression, doc) );
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
	
	private static void processFile(String description, String filename, Boolean bioInformatics, Boolean medicalInformatics, Boolean clinicalCare) throws FileNotFoundException {
		StringBuilder contents = new StringBuilder();
		Scanner scanner = new Scanner(new FileInputStream(filename), "UTF-8");
		Boolean found = false;
		while (scanner.hasNextLine()){
			String line = scanner.nextLine();
			if ( line.equalsIgnoreCase("<PubmedArticle>")){
				found = true;
			}
			if ( found ){
				contents.append(line);
				contents.append(System.getProperty("line.separator"));
			}
			if ( line.equalsIgnoreCase("</PubmedArticle>")){
				found = false;
				if ( contents.length() > 0 ){
					DatabaseInfo databaseInfo = processXML(contents.toString());
					databaseInfo.setBioInformatics(bioInformatics);
					databaseInfo.setMedicalInformatics(medicalInformatics);
					databaseInfo.setClinicalCare(clinicalCare);
					databaseInfo.setQueryTerm(description);
					solrInterface.addINBIOMEDVisionRecord(databaseInfo);
					contents.delete(0, contents.length());
				}
			}
		}
		scanner.close();
		
	}
}
