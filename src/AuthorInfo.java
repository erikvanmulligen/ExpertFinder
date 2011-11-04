
public class AuthorInfo {
	private String name = null;
	private String email = null;
	private String coauthors = null;
	private String affiliation = null;
	private String pmids = null;
	private String domain = null;
	
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
		return coauthors;
	}
	public void setCoauthors(String coauthors) {
		this.coauthors = coauthors;
	}
	public String getAffiliation() {
		return affiliation;
	}
	public void setAffiliation(String affiliation) {
		this.affiliation = affiliation;
	}
	public String getPmids() {
		return pmids;
	}
	public void setPmids(String pmids) {
		this.pmids = pmids;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public String text() {
		return this.email + "\t" + this.affiliation + "\t" + this.coauthors + "\t" + this.domain + "\t" + this.pmids;
	}
}
