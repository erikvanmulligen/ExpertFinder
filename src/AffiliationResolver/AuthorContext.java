package AffiliationResolver;

import gov.nih.nlm.ncbi.www.soap.eutils.EFetchPubmedServiceStub.AuthorType;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AuthorContext {
	private String authorName = null;
	private List<String> pubMedIds = new ArrayList<String>();
	private Map<String,Author> authors = new TreeMap<String,Author>();
	private List<String> affiliations = new ArrayList<String>();
	private String email = null;
	private Author author = null;
	private String fullName = null;
	
	public List<String> getPubMedIds() {
		return pubMedIds;
	}
	
	public void setPubMedIds(List<String> pubMedIds) {
		this.pubMedIds = pubMedIds;
	}
	
	public void addPubMedId( String pubMedId ){
		this.pubMedIds.add(pubMedId);
	}
	
	public Map<String, Author> getAuthors() {
		return authors;
	}
	
	public void addAuthor(AuthorType author){
		try{
			String authorLastName = this.getAuthorName().split(",")[0].trim();
			
			String lastName = author.getAuthorTypeChoice_type0().getAuthorTypeSequence_type0().getLastName();
			
			if ( ! lastName.equalsIgnoreCase(authorLastName)){
				String initials = author.getAuthorTypeChoice_type0().getAuthorTypeSequence_type0().getInitials();
				String key =  ( lastName != null ? lastName : "" ) + ' ' + ( initials != null ? initials : "" );
				if ( this.authors.containsKey(key) ){
					Author newAuthor = this.authors.get(key);
					newAuthor.increaseFrequency();
					this.authors.put( key, newAuthor );
				}
				else{
					Author newAuthor = new Author( author.getAuthorTypeChoice_type0().getAuthorTypeSequence_type0().getLastName(), 
												   author.getAuthorTypeChoice_type0().getAuthorTypeSequence_type0().getInitials(), 
												   author.getAuthorTypeChoice_type0().getAuthorTypeSequence_type0().getForeName() );
					newAuthor.increaseFrequency();
					if ( this.authorName.equalsIgnoreCase(key)){
						this.fullName = newAuthor.getFullname();
						this.author = newAuthor;
					}
					this.authors.put( key, newAuthor );			
				}
			}
			else{
				System.out.println( "one of co-authors of " + authorLastName + " is author");
			}
		}
		catch(Exception e){
		}
	}

	public List<String> getAffiliations() {
		return affiliations;
	}

	public void setAffiliations(List<String> affiliations) {
		this.affiliations = affiliations;
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
	
	public void addAffiliation( String affiliation ){
		double distance = 0.0;
		
		if ( affiliation != null ){
			if ( this.email == null ){
				String email = this.extractEmail(affiliation);
				List<String> authorList = new ArrayList<String>();
				for ( String key : this.authors.keySet() ){
					authorList.add( this.authors.get(key).getFullname() );
				}
				
				if ( email != null ){
					if ( authorList.size() == 1 ){
						this.email = email;
					}
					else{
						String emailName = email.split("@")[0];
						String result = LetterPairSimilarity.getClosestDistance(emailName, authorList );
						if ( result != null ){
							distance = LetterPairSimilarity.compareStrings(emailName, result);
							if ( distance > 0.25 && result.equalsIgnoreCase(this.fullName)){
								this.email = email;
							}
						}
					}
				}
			}
			this.affiliations.add(affiliation);
		}
	}

	public void write( PrintWriter out ) {
		String finalAffiliation = null;
		out.append( this.authorName + "\t" );
		//System.out.println("  based on " + this.pubMedIds.size() + " publications" );
		if ( this.email != null ){
			out.append( ( ( this.email != null ) ? this.email : "" ) + "\t" );			
		}
		
		for ( int i = 0 ; i < this.affiliations.size() ; i++ ){
			if ( ( this.email != null ) && this.affiliations.get(i).contains(this.email)){
				finalAffiliation = this.affiliations.get(i).replace(this.email, "");
				break;
			}
		}
		if ( finalAffiliation != null ){
			out.append( finalAffiliation + "\t" );
		}
		else{
			out.append( ( ( this.affiliations.size() > 0 ) ? this.affiliations.get(0) : "" ) + "\t" );
		}
		
		String authors = "";
		for ( String key : this.authors.keySet() ){
			if ( authors.length() > 0 ){
				authors += ", ";
			}
			authors += this.authors.get(key).getFullname();
		}
		out.append( authors + "\n" );
	}
	
	public void check(){
		if ( this.email != null ){
			System.out.println( this.authorName + " -> " + this.email );
		}
		else{
			System.out.println( this.authorName + " no email found" );
		}
	}

	public String getAuthorName() {
		return authorName;
	}

	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}

	public String getEmail() {
		return email;
	}

}
