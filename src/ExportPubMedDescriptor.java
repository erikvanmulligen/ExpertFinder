
public class ExportPubMedDescriptor {
	private String description = null;
	private String filename = null;
	private Boolean BioInformatics = false;
	private Boolean MedicalInformatics = false;
	private Boolean ClinicalCare = false;
	
	public ExportPubMedDescriptor( String description, String filename, Boolean BioInformatics, Boolean MedicalInformatics, Boolean ClinicalCare){
		this.description = description;
		this.filename = filename;
		this.BioInformatics = BioInformatics;
		this.MedicalInformatics = MedicalInformatics;
		this.ClinicalCare = ClinicalCare;
	}

	public String getDescription() {
		return description;
	}

	public String getFilename() {
		return filename;
	}

	public Boolean getBioInformatics() {
		return BioInformatics;
	}

	public Boolean getMedicalInformatics() {
		return MedicalInformatics;
	}

	public Boolean getClinicalCare() {
		return ClinicalCare;
	}
	
}
