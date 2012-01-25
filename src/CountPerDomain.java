import java.io.IOException;

import org.apache.solr.client.solrj.SolrServerException;

/**
 * This program counts the authors per domain as stored in the SOLR database and looks at the intersections
 * @author mulligen
 *
 */
public class CountPerDomain {

	public static void main(String[] args) throws SolrServerException, IOException {
		SolrInterface db = new SolrInterface( false );
		db.facetSearch();
	}

}
