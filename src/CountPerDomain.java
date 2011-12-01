import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;

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
