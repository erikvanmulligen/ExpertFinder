import gov.nih.nlm.ncbi.www.soap.eutils.EFetchPubmedServiceStub.AuthorType;
import gov.nih.nlm.ncbi.www.soap.eutils.EFetchPubmedServiceStub.PubmedArticleType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;

import AffiliationResolver.Author;
import AffiliationResolver.AuthorContext;
import AffiliationResolver.LetterPairSimilarity;
import AffiliationResolver.PubMed;


public class AuthorInfo {
	private String name = null;
	private String fullname = null;
	private String email = null;
	private ArrayList<Author> coauthors = new ArrayList<Author>();
	private String affiliation = null;
	private ArrayList<String> pmidFirst = new ArrayList<String>();
	private ArrayList<String> pmidLast = new ArrayList<String>();
	private ArrayList<String> pmidRest = new ArrayList<String>();
	private boolean bioinformatics = false;
	private ArrayList<String> affiliations = new ArrayList<String>();
	private boolean clinicalcare = false;
	private boolean medicalinformatics = false;
	private String lastName = null;
	private String initials = null;
	private String firstName = null;
	
	public String getFullbame() {
		return fullname;
	}
	
	public void setFullname(String fullname) {
		this.fullname = fullname;
	}	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}

	public String getCoauthors() {
		String result = "";
		for ( Author coauthor : this.coauthors ){
			result += ( result.length() > 0 ? ", " : "" ) + coauthor.getFullname();
		}
		return result;
	}
	
	public void addAuthor(Author author) {
		if ( author != null ){
			for ( Author coauthor : this.coauthors ){
				if ( coauthor.getFullname().equalsIgnoreCase( author.getFullname() ) ){
					coauthor.increaseFrequency();
					return;
				}
			}
			this.coauthors.add(author);
		}
	}
	
	public String getAffiliation() {
		return affiliation;
	}
	
	public void setAffiliation(String affiliation) {
		this.affiliation = affiliation;
	}
	
	public ArrayList<String> getPmids(){
		ArrayList<String> pmids = new ArrayList<String>();
		pmids.addAll( this.pmidFirst );
		pmids.addAll( this.pmidLast );
		pmids.addAll( this.pmidRest );
		return pmids;
	}
	
	public String getPmidsAsString() {
		ArrayList<String> pmids = getPmids();
		return StringUtils.join( pmids, "," );
	}
	
	public String getDomain() {
		String result = "";
		result += clinicalcare ? "clincicalcare" : "";
		result += ( bioinformatics ? ( result.length() > 0 ? ", " : "" ) + "bioinformatics" : "" );
		result += ( medicalinformatics ? ( result.length() > 0 ? ", " : "" ) + "medicalinformatics" : "" );
		return result;
	}

	public String getLastName() {
		return this.lastName;
	}

	public String getInitials() {
		return this.initials;
	}
	
	private boolean isValidEmailAddress(String emailAddress) {
		String expression = "^[\\w\\-]([\\.\\w])+[\\w]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
		CharSequence inputStr = emailAddress;
		Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(inputStr);
		return matcher.matches();

	}
	
	private String extractEmail(String content) {
		String email = null;
		String regex = "(\\w+)(\\.\\w+)*@(\\w+\\.)(\\w+)(\\.\\w+)*";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(content);
		while (matcher.find()) {
			email = matcher.group();

			if(!this.isValidEmailAddress(email)){
				email=null;
			}

			break;
		}
		return email;
	}
	
	public void addAffiliation( String affiliation, List<String> authorList ){
		double distance = 0.0;
		
		if ( affiliation != null ){
			if ( this.email == null ){
				String email = this.extractEmail(affiliation);
				
				if ( email != null ){
					if ( authorList.size() == 1 ){
						this.email = email;
					}
					else{
						String emailName = email.split("@")[0];
						String result = null;
						try{
							result = LetterPairSimilarity.getClosestDistance(emailName, authorList );
						}catch(Exception e){
							result = null;
						}
						if ( result != null ){
							distance = LetterPairSimilarity.compareStrings(emailName, result);
							if ( distance > 0.25 && result.equalsIgnoreCase(this.fullname)){
								this.email = email;
								this.affiliations.add(affiliation);
							}
						}
					}
				}
			}
		}
	}

	public void addAffiliationAndEmail( String affiliation ){
		if ( affiliation != null ){
			if ( this.email == null ){
				String email = this.extractEmail(affiliation);
				
				if ( email != null ){
					this.email = email;
					this.affiliations.add(affiliation);
				}
			}
		}
	}

	public void setBioInformatics(boolean bioinformatics) {
		if ( ! this.bioinformatics ){
			this.bioinformatics = bioinformatics;
		}
	}

	public void setClinicalCare(boolean clinicalcare) {
		if ( ! this.clinicalcare ){
			this.clinicalcare = clinicalcare;
		}
	}	
	
	public void setMedicalInformatics(boolean medicalinformatics) {
		if ( ! this.medicalinformatics ){
			this.medicalinformatics = medicalinformatics;
		}
	}

	public void addPmidFirst(String pmid) {
		this.pmidFirst.add( pmid );
	}		
	
	public List<String> getPmidFirst(){
		return this.pmidFirst;
	}
	
	public List<String> getPmidLast(){
		return this.pmidLast;
	}

	public List<String> getPmidRest(){
		return this.pmidRest ;
	}

	public void setFirstName(String firstName) {
		this.firstName  = firstName;
	}

	public String getFirstName() {
		return firstName;
	}

	public void addPmidLast(String pmid) {
		this.pmidLast.add( pmid );
	}

	public void addPmidRest(String pmid) {
		this.pmidRest.add( pmid );
	}

	public void setFullName(String fullName) {
		this.fullname = fullName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public void setInitials(String initials) {
		this.initials = initials;
	}
	
	public Author makeAuthor( AuthorType author ){
		if ( author != null && author.getAuthorTypeChoice_type0() != null && author.getAuthorTypeChoice_type0().getAuthorTypeSequence_type0() != null ){
			return new Author( author.getAuthorTypeChoice_type0().getAuthorTypeSequence_type0().getLastName(), 
										   author.getAuthorTypeChoice_type0().getAuthorTypeSequence_type0().getInitials(), 
										   author.getAuthorTypeChoice_type0().getAuthorTypeSequence_type0().getForeName() );
		}
		else {
			return null;
		}
	}

	
	public void findAffiliationAndEmail( List<PubmedArticleType> articles ){
		if ( articles != null ){
			Iterator<PubmedArticleType> iterator = articles.iterator();
			while( this.email == null && iterator.hasNext() ){
				PubmedArticleType article = iterator.next();
	//			addPmidFirst( article.getMedlineCitation().getPMID().toString() );
	//			affiliations.add( article.getMedlineCitation().getArticle().getAffiliation() );
				addAffiliationAndEmail(article.getMedlineCitation().getArticle().getAffiliation());		
			}
		}
	}
	
	public void findAuthors( List<PubmedArticleType> articles ){
		if ( articles != null ){
			Iterator<PubmedArticleType> iterator = articles.iterator();
			while( iterator.hasNext() ){
				PubmedArticleType article = iterator.next();
				AuthorType[] aus = article.getMedlineCitation().getArticle().getAuthorList().getAuthor();
				for ( int a = 0 ; a < aus.length ; a++ ){
					addAuthor(makeAuthor(aus[a]));
				}
			}
		}
	}

	public String compileAffiliation(){
		for ( String affiliation : affiliations ){
			return affiliation;
		}
		return this.affiliation;
	}
	
	public void getAuthorInfo( PubMed pubMed, SolrInterface solrInterface ) throws IOException, SolrServerException {
		String key = getName();
		
		List<PubmedArticleType> articles = null;
		if ( this.pmidFirst.size() < 10 ){
			try{
				articles = pubMed.searchArticles(this.lastName + " " + this.initials, 10, solrInterface.getCoauthors(key), this.pmidFirst );
			}
			catch( Exception e ){
				System.out.println( "error for author " + key + " with affiliation = " + getAffiliation() );
			}
		}
		else{
			articles = pubMed.getArticles( this.pmidFirst );
		}
		
		findAuthors( articles );
		findAffiliationAndEmail( articles );
		
		try{
			articles = pubMed.getArticles( this.getPmids() );
		}
		catch( Exception e ){
			System.out.println( "error for author " + key + " with affiliation = " + getAffiliation() );
		}
		findAuthors( articles );
		
	}	
}
