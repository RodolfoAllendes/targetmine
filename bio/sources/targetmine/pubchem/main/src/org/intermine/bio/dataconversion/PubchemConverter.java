package org.intermine.bio.dataconversion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.util.FormattedTextParser;
import org.intermine.xml.full.Item;

/**
 * 
 * @author chenyian
 */
public class PubchemConverter extends BioFileConverter {
	private static final Logger LOG = Logger.getLogger(PubchemConverter.class);
	//
	private static final String DATASET_TITLE = "PubChem";
	private static final String DATA_SOURCE_NAME = "PubChem";

	private Map<String, Item> compoundGroupMap = new HashMap<String, Item>();

	/**
	 * Constructor
	 * 
	 * @param writer
	 *            the ItemWriter used to handle the resultant items
	 * @param model
	 *            the Model
	 */
	public PubchemConverter(ItemWriter writer, Model model) {
		super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
	}

	private File nameFile;

	public void setNameFile(File file) {
		this.nameFile = file;
	}

	private Map<String, String> cidNameMap = new HashMap<String, String>();

	private void getCompoundName() throws Exception {
		Iterator<String[]> iterator = FormattedTextParser
				.parseTabDelimitedReader(new BufferedReader(new FileReader(nameFile)));
		while (iterator.hasNext()) {
			String[] cols = iterator.next();
			cidNameMap.put(cols[0], cols[1]);
		}
	}

	/**
	 * 
	 * 
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {
		getCompoundName();
		Iterator<String[]> iterator = FormattedTextParser
				.parseTabDelimitedReader(new BufferedReader(reader));

		while (iterator.hasNext()) {
			String[] cols = iterator.next();
			String cid = cols[0];
			String inchiKey = cols[1];
			if (StringUtils.isEmpty(inchiKey)) {
				LOG.info("Empty InChIKey for id :" + cid);
				continue;
			}
			String compoundGroupId = inchiKey.substring(0, inchiKey.indexOf("-"));
			if (compoundGroupId.length() != 14) {
				LOG.info(String.format("Bad InChIKey value: %s, %s .", cid, cols[0]));
				continue;
			}
			Item item = createItem("PubChemCompound");
			item.setAttribute("primaryIdentifier", String.format("PubChem:%s", cid));
			item.setAttribute("secondaryIdentifier", cid);
			item.setAttribute("inchiKey", inchiKey);
			
//			setSynonyms(item, inchiKey);
			
			item.setAttribute("pubChemCid", cid);
			String name = cidNameMap.get(cid);
			// if name is not available, use identifier instead; should not happen.
			if (name == null) {
				name = String.format("CID %s", cid);
				LOG.error(String.format("Compound name not found. cid: %s", cid));
			}
			// if the length of the name is greater than 40 characters,
			// use id instead and save the long name as the synonym
			if (name.length() > 40) {
				setSynonyms(item, name);
				name = String.format("CID %s", cid);
			}
			item.setAttribute("name", name);
			item.setReference("compoundGroup", getCompoundGroup(compoundGroupId, name));
			store(item);
		}
	}

	private Map<String, String> nameMap = new HashMap<String, String>();

	private Item getCompoundGroup(String inchiKey, String name) throws ObjectStoreException {
		Item ret = compoundGroupMap.get(inchiKey);
		if (ret == null) {
			ret = createItem("CompoundGroup");
			ret.setAttribute("identifier", inchiKey);
			compoundGroupMap.put(inchiKey, ret);
		}
		// randomly pick one name
		if (nameMap.get(inchiKey) == null || !name.startsWith("CID")) {
			nameMap.put(inchiKey, name);
			ret.setAttribute("name", name);
		}
		return ret;
	}

	@Override
	public void close() throws Exception {
		store(compoundGroupMap.values());
	}
	
	private void setSynonyms(Item subject, String value) throws ObjectStoreException {
		Item syn = createItem("Synonym");
		syn.setAttribute("value", value);
		syn.setReference("subject", subject);
		store(syn);
	}


}
