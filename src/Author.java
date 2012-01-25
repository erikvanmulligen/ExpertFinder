
public class Author {
	private String firstName = null;
	private String lastName = null;
	private String initials = null;
	private Boolean lastAuthor = false;
	private Boolean firstAuthor = false;
	
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getInitials() {
		return initials;
	}
	public void setInitials(String initials) {
		this.initials = initials;
	}
	
	public String getFullname(){
		return ( this.initials.isEmpty() ? "" : this.initials.substring( 0, 1 ) + " ") + this.lastName;
	}
	
	public String toString(){
		return "[\"firstName\":\"" + this.firstName + "\",\"lastName\":\"" + this.lastName + "\",\"initials\":\"" + this.initials + "\",\"lastAuthor\":" + this.lastAuthor + "]";
	}
	public Boolean getLastAuthor() {
		return lastAuthor;
	}
	public void setLastAuthor(Boolean lastAuthor) {
		this.lastAuthor = lastAuthor;
	}
	public void setFirstAuthor(boolean firstAuthor) {
		// TODO Auto-generated method stub
		this.firstAuthor = firstAuthor;
	}
	public Boolean getFirstAuthor(){
		return this.firstAuthor;
	}

}
