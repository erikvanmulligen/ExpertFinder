package PubMed;
import gov.nih.nlm.ncbi.www.soap.eutils.EFetchPubmedServiceStub;
import gov.nih.nlm.ncbi.www.soap.eutils.EFetchSequenceServiceStub;
import gov.nih.nlm.ncbi.www.soap.eutils.EUtilsServiceStub;
public class PubMed{
	public static void main(String[] args) throws Exception
	{
		String[] ids = { "" };
		String fetchIds = "";
		// STEP #1: search in PubMed for "cat"
		//
		try
		{
			EUtilsServiceStub service = new EUtilsServiceStub();
			// call NCBI ESearch utility
			EUtilsServiceStub.ESearchRequest req = new EUtilsServiceStub.ESearchRequest();
			req.setDb("pubmed");
			req.setTerm("malaria+AND+africa");
			req.setSort("PublicationDate");
			req.setRetMax("5");
			EUtilsServiceStub.ESearchResult res = service.run_eSearch(req);
			// results output
			int N = res.getIdList().getId().length;
			ids[0] = "";
			for (int i = 0; i < N; i++)
			{
				if (i > 0) ids[0] += ",";
				ids[0] += res.getIdList().getId()[i];
			}
			System.out.println("Search in PubMed for \"cat\" returned " + res.getCount() + " hits");
			System.out.println("Search links in nuccore for the first "+N+" UIDs: "+ids[0]);
			System.out.println();
		}
		catch (Exception e) { System.out.println(e.toString()); }
		// STEP #2: get links in nucleotide database (nuccore)
		//
//		try
//		{
//			EUtilsServiceStub service = new EUtilsServiceStub();
//			// call NCBI ELink utility
//			EUtilsServiceStub.ELinkRequest req = new EUtilsServiceStub.ELinkRequest();
//			req.setDb("pubmed");
//			//req.setDbfrom("pubmed");
//			req.setId(ids);
//			EUtilsServiceStub.ELinkResult res = service.run_eLink(req);
//			for (int i = 0; i < res.getLinkSet()[0].getLinkSetDb()[0].getLink().length; i++)
//			{
//				if (i > 0) fetchIds += ",";
//				fetchIds += res.getLinkSet()[0].getLinkSetDb()[0].getLink()[i].getId().getString();
//			}
//			System.out.println("ELink returned the following UIDs from nuccore: " + fetchIds);
//			System.out.println();
//
//		}
//		catch (Exception e) { System.out.println(e.toString()); }

		// STEP #3: fetch records from nuccore
		//
		try
		{
			EFetchPubmedServiceStub service = new EFetchPubmedServiceStub();
			// call NCBI EFetch utility

			
			for ( int j = 0 ; j < ids.length ; j++ ){
				EFetchPubmedServiceStub.EFetchRequest req = new EFetchPubmedServiceStub.EFetchRequest();
				req.setId(ids[j]);
				EFetchPubmedServiceStub.EFetchResult res = service.run_eFetch(req);				// results output
				for (int i = 0; i < res.getPubmedArticleSet().getPubmedArticleSetChoice().length; i++){
					EFetchPubmedServiceStub.PubmedArticleType art = res.getPubmedArticleSet().getPubmedArticleSetChoice()[i].getPubmedArticle();
					EFetchPubmedServiceStub.PubmedBookArticleType book = res.getPubmedArticleSet().getPubmedArticleSetChoice()[i].getPubmedBookArticle();
					if(art!=null) {
						System.out.println("ID (article): " + art.getMedlineCitation().getPMID());
						System.out.println("Title: " + art.getMedlineCitation().getArticle().getArticleTitle());
					} else if(book!=null) {
						System.out.println("ID (book): " + book.getBookDocument().getPMID());
						System.out.println("Title: " + book.getBookDocument().getArticleTitle());
					}
					System.out.println("--------------------------\n");
				}
			}
		}
		catch (Exception e) { System.out.println(e.toString()); }
	}
}