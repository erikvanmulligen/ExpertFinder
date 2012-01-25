import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class DatabaseInfo {
	private String pmid = null;
	private List<Author> authors = new ArrayList<Author>();
	private String queryTerm = null;
	private String title = null;
	private String affiliation = null;
	private String journal = null;
	private String date = null;
	private Boolean MedicalInformatics = false;
	private Boolean BioInformatics = false;
	private Boolean ClinicalCare = false;
	
	
	
	public String toString(){
		String authors = "[\n";
		for ( int i = 0 ; i < this.authors.size() ; i++ ){
			authors += this.authors.get(i).toString() + "\n";
		}
		authors += "]\n";
		return "{\npmid="+this.pmid + "\nauthors=" + authors + "\nqueryTerm=" + this.queryTerm + "\ntitle=" + this.title + "\njournal=" + this.journal + "\ndate=" + this.date + "\n}";
	}
	
	public String getPmid() {
		return pmid;
	}
	public void setPmid(String pmid) {
		this.pmid = pmid;
	}
	public List<Author> getAuthors() {
		return authors;
	}
	public void setAuthors(List<Author> authors) {
		this.authors = authors;
	}
	public String getQueryTerm() {
		return queryTerm;
	}
	public void setQueryTerm(String queryTerm) {
		this.queryTerm = queryTerm;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getJournal() {
		return journal;
	}
	public void setJournal(String journal) {
		this.journal = journal;
	}
	public String getDate() {
		return date;
	}
	
	public String getDateAsCalendar(){
		Date date = null;
		DateFormat inFormatter = new SimpleDateFormat("dd/mm/yy");
		try {
			date = (Date)inFormatter.parse(this.date);
		} catch (ParseException e) {
			inFormatter = new SimpleDateFormat("dd/MMM/yy");
			try {
				date = (Date)inFormatter.parse(this.date);
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
		}  
		
		DateFormat outFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		return outFormatter.format(date);
	}
	
	public void setDate(String date) {
		this.date = date;
	}

	public String getAffiliation() {
		return affiliation;
	}

	public void setAffiliation(String affiliation) {
		this.affiliation = affiliation;
	}

	public Boolean getMedicalInformatics() {
		return MedicalInformatics;
	}

	public void setMedicalInformatics(Boolean medicalInformatics) {
		MedicalInformatics = medicalInformatics;
	}

	public Boolean getBioInformatics() {
		return BioInformatics;
	}

	public void setBioInformatics(Boolean bioInformatics) {
		BioInformatics = bioInformatics;
	}

	public Boolean getClinicalCare() {
		return ClinicalCare;
	}

	public void setClinicalCare(Boolean clinicalCare) {
		ClinicalCare = clinicalCare;
	}
}
