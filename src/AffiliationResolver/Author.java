package AffiliationResolver;

public class Author {
	private String lastName = null, initials = null, foreName = null;
	private int frequency = 0;
	
	public Author( String lastName, String initials, String foreName ){
		this.lastName = lastName;
		this.initials = initials;
		this.foreName = foreName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getInitials() {
		return initials;
	}

	public String getForeName() {
		return foreName;
	}

	public String output() {
		return this.lastName + ' ' + this.initials + ' ' + this.foreName;
	}

	public String searchName() {
		return this.lastName + ' ' + this.initials;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}
	
	public void increaseFrequency(){
		this.frequency += 1;
	}

	public String getFullname() {
		return this.foreName + ' ' + this.lastName;
	}
}
