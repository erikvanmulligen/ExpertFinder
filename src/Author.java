
public class Author {
	private String firstName = null;
	private String lastName = null;
	private String initials = null;
	private Boolean lastAuthor = false;
	
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
	public String toString(){
		return "[\"firstName\":\"" + this.firstName + "\",\"lastName\":\"" + this.lastName + "\",\"initials\":\"" + this.initials + "\",\"lastAuthor\":" + this.lastAuthor + "]";
	}
	public Boolean getLastAuthor() {
		return lastAuthor;
	}
	public void setLastAuthor(Boolean lastAuthor) {
		this.lastAuthor = lastAuthor;
	}
}
