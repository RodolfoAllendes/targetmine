package org.intermine.bio.dataconversion;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.xml.full.Item;

/**
 * Since the so called drugcard format was deprecated by DrugBank themselves, the old parser which
 * was developed by Ishikawa-san was replaced with this one.
 * 
 * This parser will parse DrugBank data from xml file.
 * 
 * @author chenyian
 * 
 * @since 2012/8/15
 */
public class DrugBankXmlConverter extends BioFileConverter {

	private static Logger LOG = Logger.getLogger(DrugBankXmlConverter.class);

	private static final String DATASET_TITLE = "DrugBank";
	private static final String DATA_SOURCE_NAME = "DrugBank";

	private static final String NAMESPACE_URI = "http://drugbank.ca";

	private Map<String, String> proteinMap = new HashMap<String, String>();
	private Map<String, String> publicationMap = new HashMap<String, String>();
	private Map<String, String> drugTypeMap = new HashMap<String, String>();
	private Map<String, String> compoundGroupMap = new HashMap<String, String>();

	public DrugBankXmlConverter(ItemWriter writer, Model model) {
		super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
	}

	@Override
	public void process(Reader reader) throws Exception {
		Builder parser = new Builder();
		Document doc = parser.build(reader);

		Map<String, String> idMap = new HashMap<String, String>();
		Elements partnerElements = doc.getRootElement().getChildElements("partners", NAMESPACE_URI)
				.get(0).getChildElements();
		for (int i = 0; i < partnerElements.size(); i++) {
			Element part = partnerElements.get(i);
			String refId = part.getAttribute("id").getValue();
			String uniprotId = null;
			Elements extIds = part.getFirstChildElement("external-identifiers", NAMESPACE_URI)
					.getChildElements("external-identifier", NAMESPACE_URI);
			for (int j = 0; j < extIds.size(); j++) {
				Element e = extIds.get(j);
				if (e.getFirstChildElement("resource", NAMESPACE_URI).getValue().toLowerCase()
						.equals("uniprotkb")) {
					uniprotId = e.getFirstChildElement("identifier", NAMESPACE_URI).getValue();
				}
			}
			if (uniprotId != null) {
				idMap.put(refId, uniprotId);
			}
		}

		Elements drugElements = doc.getRootElement().getChildElements("drug", NAMESPACE_URI);
		for (int i = 0; i < drugElements.size(); i++) {
			Element drug = drugElements.get(i);
			Item drugItem = createItem("DrugCompound");
			String drugBankId = drug.getFirstChildElement("drugbank-id", NAMESPACE_URI).getValue();
			drugItem.setAttribute("drugBankId", drugBankId);
			drugItem.setAttribute("primaryIdentifier", String.format("DrugBank: %s", drugBankId));
			drugItem.setAttribute("secondaryIdentifier", drugBankId);
			String name = drug.getFirstChildElement("name", NAMESPACE_URI).getValue();
			// if the length of the name is greater than 40 characters,
			// use id instead and save the long name as the synonym
			if (name.length() > 40) {
				setSynonyms(drugItem, name);
				name = drugBankId;
			}
			drugItem.setAttribute("name", name);
			drugItem.setAttribute("genericName", name);
			String casReg = drug.getFirstChildElement("cas-number", NAMESPACE_URI).getValue();
			if (!StringUtils.isEmpty(casReg)) {
				drugItem.setAttribute("casRegistryNumber", casReg);
			}
			String desc = drug.getFirstChildElement("description", NAMESPACE_URI).getValue().trim();
			if (!StringUtils.isEmpty(desc)) {
				drugItem.setAttribute("description", desc);
			}

			// inchikey
			Element cpNode = drug.getFirstChildElement("calculated-properties", NAMESPACE_URI);
			if (cpNode != null) {
				Elements properties = cpNode.getChildElements("property", NAMESPACE_URI);
				for (int j = 0; j < properties.size(); j++) {
					Element p = properties.get(j);
					if (p.getFirstChildElement("kind", NAMESPACE_URI).getValue().toLowerCase()
							.equals("inchikey")) {
						String inchiKey = p.getFirstChildElement("value", NAMESPACE_URI).getValue();
						inchiKey = inchiKey.substring(inchiKey.indexOf("=") + 1);
						drugItem.setAttribute("inchiKey", inchiKey);
						drugItem.setReference(
								"compoundGroup",
								getCompoundGroup(inchiKey.substring(0, inchiKey.indexOf("-")), name));

						// assign inchikey as synonym
						setSynonyms(drugItem, inchiKey);

					}
				}
			}
			// 4 types
			List<String> proteinTypes = Arrays.asList("target", "enzyme", "transporter", "carrier");
			for (String proteinType : proteinTypes) {
				Elements targets = drug.getFirstChildElement(proteinType + "s", NAMESPACE_URI)
						.getChildElements(proteinType, NAMESPACE_URI);
				for (int j = 0; j < targets.size(); j++) {
					Element t = targets.get(j);

					// retrieve actions
					Elements actions = t.getFirstChildElement("actions", NAMESPACE_URI)
							.getChildElements("action", NAMESPACE_URI);
					List<String> actionValues = new ArrayList<String>();
					for (int k = 0; k < actions.size(); k++) {
						String action = actions.get(k).getValue();
						actionValues.add(action);
					}

					String id = t.getAttribute("partner").getValue();
					if (idMap.get(id) != null) {
						Item interaction = createItem("DrugBankInteraction");
						interaction.setReference("protein", getProtein(idMap.get(id)));
						interaction.setReference("compound", drugItem);
						String ref = t.getFirstChildElement("references", NAMESPACE_URI).getValue();
						Pattern pattern = Pattern
								.compile("\"Pubmed\":http://www.ncbi.nlm.nih.gov/pubmed/(\\d+)");
						Matcher matcher = pattern.matcher(ref);
						while (matcher.find()) {
							interaction.addToCollection("publications",
									getPublication(matcher.group(1)));
						}
						if (actionValues.size() > 0) {
							for (String action : actionValues) {
								interaction.addToCollection("actions", getDrugAction(action.trim()
										.toLowerCase()));
							}
						} else {
							interaction.addToCollection("actions", getDrugAction("unknown"));
						}
						interaction.setAttribute("proteinType", proteinType);
						store(interaction);
					}
				}

			}

			// get brand names
			Elements brands = drug.getFirstChildElement("brands", NAMESPACE_URI).getChildElements(
					"brand", NAMESPACE_URI);
			for (int j = 0; j < brands.size(); j++) {
				String brandName = brands.get(j).getValue();
				setSynonyms(drugItem, brandName);
			}
			// get synonyms
			Elements synonyms = drug.getFirstChildElement("synonyms", NAMESPACE_URI)
					.getChildElements("synonym", NAMESPACE_URI);
			for (int j = 0; j < synonyms.size(); j++) {
				String synonym = synonyms.get(j).getValue();
				setSynonyms(drugItem, synonym);
			}

			drugItem.addToCollection("drugTypes", getDrugType(drug.getAttribute("type").getValue()));
			// get groups (for DrugType)
			Elements groups = drug.getFirstChildElement("groups", NAMESPACE_URI).getChildElements(
					"group", NAMESPACE_URI);
			for (int j = 0; j < groups.size(); j++) {
				String group = groups.get(j).getValue();
				drugItem.addToCollection("drugTypes", getDrugType(group));
			}

			// TODO get ATC codes
			Elements atcCodes = drug.getFirstChildElement("atc-codes", NAMESPACE_URI).getChildElements(
					"atc-code", NAMESPACE_URI);
			for (int j = 0; j < atcCodes.size(); j++) {
				String atcCode = atcCodes.get(j).getValue();
				if (atcCode.length() != 7) {
					LOG.error(String.format("Invalid atc code, id: %s, code: %s", drugBankId, atcCode));
					continue;
				}
				drugItem.addToCollection("atcCodes", getAtcClassification(atcCode, name));
			}

			// get uiprot id if the drug is a protein
			Elements extIds = drug.getFirstChildElement("external-identifiers", NAMESPACE_URI)
					.getChildElements("external-identifier", NAMESPACE_URI);
			for (int j = 0; j < extIds.size(); j++) {
				Element e = extIds.get(j);
				if (e.getFirstChildElement("resource", NAMESPACE_URI).getValue().toLowerCase()
						.equals("uniprotkb")) {
					drugItem.setReference("protein",
							getProtein(e.getFirstChildElement("identifier", NAMESPACE_URI)
									.getValue()));
				}
			}
			store(drugItem);

		}

	}

	private Map<String, String> atcMap = new HashMap<String, String>();

	private String getAtcClassification(String atcCode, String name) throws ObjectStoreException {
		String ret = atcMap.get(atcCode);
		if (ret == null) {
			Item item = createItem("AtcClassification");
			item.setAttribute("atcCode", atcCode);
			item.setAttribute("name", name);
			// TODO add parent
			String parentCode = atcCode.substring(0,5);
			item.setReference("parent", getParent(parentCode));
			
			store(item);
			ret = item.getIdentifier();
			atcMap.put(atcCode, ret);
		}
		return ret;
	}

	private String getParent(String parentCode) throws ObjectStoreException {
		String ret = atcMap.get(parentCode);
		if (ret == null) {
			Item item = createItem("AtcClassification");
			item.setAttribute("atcCode", parentCode);
			store(item);
			ret = item.getIdentifier();
			atcMap.put(parentCode, ret);
		}
		return ret;
	}

	private Map<String, String> actionMap = new HashMap<String, String>();

	private String getDrugAction(String action) throws ObjectStoreException {
		String ret = actionMap.get(action);
		if (ret == null) {
			Item item = createItem("DrugAction");
			item.setAttribute("type", action);
			store(item);
			ret = item.getIdentifier();
			actionMap.put(action, ret);
		}
		return ret;
	}

	private String getProtein(String uniprotId) throws ObjectStoreException {
		String ret = proteinMap.get(uniprotId);
		if (ret == null) {
			Item item = createItem("Protein");
			item.setAttribute("primaryAccession", uniprotId);
			store(item);
			ret = item.getIdentifier();
			proteinMap.put(uniprotId, ret);
		}
		return ret;
	}

	private String getPublication(String pubMedId) throws ObjectStoreException {
		String ret = publicationMap.get(pubMedId);
		if (ret == null) {
			Item item = createItem("Publication");
			item.setAttribute("pubMedId", pubMedId);
			store(item);
			ret = item.getIdentifier();
			publicationMap.put(pubMedId, ret);
		}
		return ret;
	}

	private String getDrugType(String name) throws ObjectStoreException {
		String ret = drugTypeMap.get(name);
		if (ret == null) {
			Item item = createItem("DrugType");
			item.setAttribute("name", name);
			store(item);
			ret = item.getIdentifier();
			drugTypeMap.put(name, ret);
		}
		return ret;
	}

	private String getCompoundGroup(String inchiKey, String name) throws ObjectStoreException {
		String ret = compoundGroupMap.get(inchiKey);
		if (ret == null) {
			Item item = createItem("CompoundGroup");
			item.setAttribute("identifier", inchiKey);
			item.setAttribute("name", name);
			store(item);
			ret = item.getIdentifier();
			compoundGroupMap.put(inchiKey, ret);
		}
		return ret;
	}

	private void setSynonyms(Item subject, String value) throws ObjectStoreException {
		Item syn = createItem("Synonym");
		syn.setAttribute("value", value);
		syn.setReference("subject", subject);
		store(syn);
	}

}
