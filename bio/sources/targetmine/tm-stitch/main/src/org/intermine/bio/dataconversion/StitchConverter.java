package org.intermine.bio.dataconversion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.ConstraintOp;
import org.intermine.metadata.Model;
import org.intermine.model.bio.Protein;
import org.intermine.model.bio.Synonym;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.objectstore.ObjectStoreFactory;
import org.intermine.objectstore.query.ConstraintSet;
import org.intermine.objectstore.query.ContainsConstraint;
import org.intermine.objectstore.query.Query;
import org.intermine.objectstore.query.QueryClass;
import org.intermine.objectstore.query.QueryCollectionReference;
import org.intermine.objectstore.query.QueryField;
import org.intermine.objectstore.query.QueryValue;
import org.intermine.objectstore.query.Results;
import org.intermine.objectstore.query.ResultsRow;
import org.intermine.objectstore.query.SimpleConstraint;
import org.intermine.util.FormattedTextParser;
import org.intermine.xml.full.Item;

/**
 * 
 * @author chenyian
 * 
 */
public class StitchConverter extends BioFileConverter {
	// Threshold of evidence score (experimental) , 700 means 0.7
	// 2014/1/20 Philip request to lower the threshold to 400
	private static int THRESHOLD = 400;

	private static final Logger LOG = Logger.getLogger(StitchConverter.class);
	//
	private static final String DATASET_TITLE = "STITCH";
	private static final String DATA_SOURCE_NAME = "STITCH: Chemical-Protein Interactions";

	private Map<String, String> proteinMap = new HashMap<String, String>();
	private Map<String, String> pubChemCompoundMap = new HashMap<String, String>();

	private Set<String> interactiondSet = new HashSet<String>();

	// get id map from integrated data
	private Map<String, Set<String>> primaryIdMap;
	private String osAlias = null;

	/**
	 * Set the ObjectStore alias.
	 * 
	 * @param osAlias
	 *            The ObjectStore alias
	 */
	public void setOsAlias(String osAlias) {
		this.osAlias = osAlias;
	}

	/**
	 * Constructor
	 * 
	 * @param writer
	 *            the ItemWriter used to handle the resultant items
	 * @param model
	 *            the Model
	 */
	public StitchConverter(ItemWriter writer, Model model) {
		super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
	}

	/**
	 * 
	 * 
	 * {@inheritDoc}
	 */
	public void process(Reader reader) throws Exception {

		// should only generate once
		if (primaryIdMap == null) {
			getPrimaryIdMap();
		}
		if (primaryIdMap.isEmpty()) {
			throw new RuntimeException(
					"No primary id mapping found. Forget to load uniprot before loading stitch?");
		}

		if (cidNameMap.isEmpty()) {
			readCompoundName();
		}
		if (cidInchikeyMap.isEmpty()) {
			readCompoundInchikey();
		}

		Iterator<String[]> iterator = FormattedTextParser
				.parseTabDelimitedReader(new BufferedReader(reader));

		int countNoProtein = 0;
		int countThreshold = 0;
		int countRedundant = 0;

		while (iterator.hasNext()) {
			String[] cols = iterator.next();
			
			if (!cols[0].startsWith("CID")) {
				continue;
			}
			
			// example CID010939283 CID110928783
			// the first digit is not a part of pubchem identifier
			String cid = cols[0].substring(4).replaceAll("^0*", "");
			// String ensemblId = cols[1].substring(cols[1].indexOf(".") + 1);
			String ensemblId = StringUtils.substringAfter(cols[1], ".");
			Set<String> uniprotIds = primaryIdMap.get(ensemblId);

			if (uniprotIds != null) {
				// only experiment data will be integrated
				String evidence = "experimental";
				if (Integer.valueOf(cols[2]).intValue() < THRESHOLD) {
					countThreshold++;
					continue;
				}
				if (interactiondSet.contains(ensemblId + "-" + cid)) {
					countRedundant++;
					continue;
				}
				for (String primaryIdentifier : uniprotIds) {
					Item si = createItem("StitchInteraction");
					si.setAttribute("identifier", cols[0] + "_" + cols[1]);
					si.setReference("compound", getPubChemCompound(cid));
					si.setAttribute("score", cols[2]);
					si.setAttribute("evidence", evidence);
					si.setReference("protein", getProtein(primaryIdentifier));
					store(si);
				}

				interactiondSet.add(ensemblId + "-" + cid);
			} else {
				countNoProtein++;
				 LOG.info(String.format("Uniprot ID for '%s' was not found.", ensemblId));
			}
		}
		LOG.info(String.format("%d interactions were skipped.", (countNoProtein + countRedundant + countThreshold)));
		LOG.info(String.format("%d interactions were not able to map protein ID.", countNoProtein));
		LOG.info(String.format("%d interactions were redundant.", countRedundant));
		LOG.info(String.format("%d interactions were lower than the threshold.", countThreshold));
	}

	private String getProtein(String primaryIdentifier) throws ObjectStoreException {
		String ret = proteinMap.get(primaryIdentifier);
		if (ret == null) {
			Item item = createItem("Protein");
			item.setAttribute("primaryIdentifier", primaryIdentifier);
			store(item);
			ret = item.getIdentifier();
			proteinMap.put(primaryIdentifier, ret);
		}
		return ret;
	}

	private String getPubChemCompound(String cid) throws ObjectStoreException {
		String ret = pubChemCompoundMap.get(cid);
		if (ret == null) {
			Item item = createItem("PubChemCompound");
			item.setAttribute("originalId", cid);
			item.setAttribute("identifier", String.format("PubChem:%s", cid));
			String name = cidNameMap.get(cid);
			if (name == null || name.equals("")) {
				name = String.format("CID %s", cid);
			}
			// if the length of the name is greater than 40 characters,
			// use id instead and save the long name as the synonym
			if (name.length() > 40) {
				setSynonyms(item, name);
				name = String.format("CID %s", cid);
			}
			item.setAttribute("name", name);

			String inchiKey = cidInchikeyMap.get(cid);
			if (inchiKey != null) {
				item.setAttribute("inchiKey", inchiKey);
				// setSynonyms(item, inchiKey);
				item.setReference("compoundGroup",
						getCompoundGroup(inchiKey.substring(0, inchiKey.indexOf("-")), name));
			}

			store(item);
			ret = item.getIdentifier();
			pubChemCompoundMap.put(cid, ret);
		}
		return ret;
	}

	@SuppressWarnings("unchecked")
	private void getPrimaryIdMap() throws Exception {
		primaryIdMap = new HashMap<String, Set<String>>();

		Query q = new Query();
		QueryClass qcSynonym = new QueryClass(Synonym.class);
		QueryClass qcProtein = new QueryClass(Protein.class);
		QueryField qfValue = new QueryField(qcSynonym, "value");
		QueryField qfPrimaryId = new QueryField(qcProtein, "primaryIdentifier");
		q.addFrom(qcSynonym);
		q.addFrom(qcProtein);
		q.addToSelect(qfValue);
		q.addToSelect(qfPrimaryId);

		ConstraintSet cs = new ConstraintSet(ConstraintOp.AND);

		QueryCollectionReference synRef = new QueryCollectionReference(qcProtein, "synonyms");
		cs.addConstraint(new ContainsConstraint(synRef, ConstraintOp.CONTAINS, qcSynonym));

		cs.addConstraint(new SimpleConstraint(qfValue, ConstraintOp.MATCHES, new QueryValue("ENS%")));

		q.setConstraint(cs);

		ObjectStore os = ObjectStoreFactory.getObjectStore(osAlias);

		Results results = os.execute(q);
		Iterator<Object> iterator = results.iterator();
		while (iterator.hasNext()) {
			ResultsRow<String> rr = (ResultsRow<String>) iterator.next();
			// LOG.info(String.format("ens: %s; uni: %s...", rr.get(0), rr.get(1)));
			if (primaryIdMap.get(rr.get(0)) == null) {
				primaryIdMap.put(rr.get(0), new HashSet<String>());
			}
			primaryIdMap.get(rr.get(0)).add(rr.get(1));
		}
	}

	private File nameFile;

	public void setNameFile(File file) {
		this.nameFile = file;
	}

	private Map<String, String> cidNameMap = new HashMap<String, String>();

	private void readCompoundName() throws Exception {
		Iterator<String[]> iterator = FormattedTextParser
				.parseTabDelimitedReader(new BufferedReader(new FileReader(nameFile)));
		while (iterator.hasNext()) {
			String[] cols = iterator.next();
//			String cid = cols[0].substring(4).replaceAll("^0*", "");
			cidNameMap.put(cols[0], cols[1]);
		}
	}

	private File inchikeyFile;

	public void setInchikeyFile(File file) {
		this.inchikeyFile = file;
	}

	private Map<String, String> cidInchikeyMap = new HashMap<String, String>();

	private void readCompoundInchikey() throws Exception {
		Iterator<String[]> iterator = FormattedTextParser
				.parseTabDelimitedReader(new BufferedReader(new FileReader(inchikeyFile)));
		while (iterator.hasNext()) {
			String[] cols = iterator.next();
//			String cid = cols[0].substring(4).replaceAll("^0*", "");
			cidInchikeyMap.put(cols[0], cols[1].trim());
		}
	}

	private void setSynonyms(Item subject, String value) throws ObjectStoreException {
		Item syn = createItem("CompoundSynonym");
		syn.setAttribute("value", value);
		syn.setReference("subject", subject);
		store(syn);
	}

	private Map<String, Item> compoundGroupMap = new HashMap<String, Item>();
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

}
