import java.util.ArrayList;
import java.util.List;


public class DatabaseInfo {
	private String pmid = null;
	private List<Author> authors = new ArrayList<Author>();
	private String queryTerm = null;
	private String title = null;
	private String journal = null;
	private String date = null;
	
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
	public void setDate(String date) {
		this.date = date;
	}
}
