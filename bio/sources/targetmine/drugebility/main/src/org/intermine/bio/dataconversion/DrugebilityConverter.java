package org.intermine.bio.dataconversion;

import java.io.Reader;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.util.FormattedTextParser;
import org.intermine.xml.full.Item;

/**
 * Parser for ChEMBL DrugEBIlity data set Data source download site:
 * ftp://ftp.ebi.ac.uk/pub/databases/chembl/DrugEBIlity/
 * 
 * @author chenyian
 */
public class DrugebilityConverter extends BioFileConverter {
//	private static final Logger LOG = Logger.getLogger(DrugebilityConverter.class);
	//
	private static final String DATASET_TITLE = "DrugEBIlity";
	private static final String DATA_SOURCE_NAME = "ChEMBL";

	private Map<String, String> scopEntryMap = new HashMap<String, String>();
	private Map<String, String> structureMap = new HashMap<String, String>();

	/**
	 * Constructor
	 * 
	 * @param writer
	 *            the ItemWriter used to handle the resultant items
	 * @param model
	 *            the Model
	 */
	public DrugebilityConverter(ItemWriter writer, Model model) {
		super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
	}

	/**
	 * 
	 * 
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {
		
		Iterator<String[]> iterator = FormattedTextParser.parseTabDelimitedReader(reader);
		// skip the header
		iterator.next();
		while (iterator.hasNext()) {
			String[] cols = iterator.next();
			Item item = createItem("Druggability");
			// for external links to ChEMBL-DrugEBIlity
			item.setAttribute("primaryIdentifier",cols[0]);
			// the length of scop domain identifier is less than 7
			if (cols[0].length() < 7) {
				item.setReference("scopDomain", getScopClassification(cols[0]));
			}
			item.setReference("proteinStructure", getProteinStructure(cols[1]));
			DecimalFormat df = new DecimalFormat("0.00");
			String formatted = df.format(Double.valueOf(cols[5]));
			item.setAttribute("ensembl", formatted);
			item.setAttribute("tractable", cols[6].equals("0")? "false" : "true");
			item.setAttribute("druggable", cols[7].equals("0")? "false" : "true");
			
			store(item);
		}
	}

	private String getProteinStructure(String identifier) throws ObjectStoreException {
		String ret = structureMap.get(identifier);
		if (ret == null) {
			Item item = createItem("ProteinStructure");
			item.setAttribute("pdbId", identifier);
			ret = item.getIdentifier();
			store(item);
			structureMap.put(identifier, ret);
		}
		return ret;
	}

	private String getScopClassification(String identifier) throws ObjectStoreException {
		String ret = scopEntryMap.get(identifier);
		if (ret == null) {
			Item item = createItem("ScopClassification");
			item.setAttribute("sunid", identifier);
			ret = item.getIdentifier();
			store(item);
			scopEntryMap.put(identifier, ret);
		}
		return ret;
	}

}
