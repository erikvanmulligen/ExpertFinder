package AffiliationResolver;

public class AffiliationMatch {
	private int distance = 0;
	private String firstName = null;
	private String initials = null;
	private String lastName = null;
	
	public AffiliationMatch( String lastName, String initials, String firstName, int distance ){
		this.lastName = lastName;
		this.initials = initials;
		this.firstName = firstName;
		this.distance = distance;
	}

	public int getDistance() {
		return distance;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getInitials() {
		return initials;
	}

	public void setInitials(String initials) {
		this.initials = initials;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
}
