package org.intermine.bio.postprocess;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.intermine.metadata.ConstraintOp;
import org.intermine.metadata.Model;
import org.intermine.model.InterMineObject;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.objectstore.ObjectStoreWriter;
import org.intermine.objectstore.query.ConstraintSet;
import org.intermine.objectstore.query.ContainsConstraint;
import org.intermine.objectstore.query.Query;
import org.intermine.objectstore.query.QueryClass;
import org.intermine.objectstore.query.QueryCollectionReference;
import org.intermine.objectstore.query.QueryObjectReference;
import org.intermine.objectstore.query.Results;
import org.intermine.objectstore.query.ResultsRow;
import org.intermine.postprocess.PostProcessor;
import org.intermine.util.DynamicUtil;

public class DiseaseSummaryPostprocess extends PostProcessor {

	private static final Logger LOG = Logger.getLogger(DiseaseSummaryPostprocess.class);

	// for clinvar disease title
	private static final List<String> IGNORED_DISEASE_NAMES = Arrays.asList("not specified",
			"not provided");

	private ObjectStore os;

	private Model model;

	public DiseaseSummaryPostprocess(ObjectStoreWriter osw) {
		super(osw);
		this.os = osw.getObjectStore();
		this.model = Model.getInstanceByName("genomic");
	}

	@SuppressWarnings("unchecked")
	@Override
	public void postProcess() throws ObjectStoreException {

		osw.beginTransaction();

		Iterator<?> iterator = getGwasGenes();
		int count = 0;
		while (iterator.hasNext()) {
			ResultsRow<?> rr = (ResultsRow<?>) iterator.next();
			InterMineObject gene = (InterMineObject) rr.get(0);
			try {
				String symbol = (String) gene.getFieldValue("symbol");
				Set<InterMineObject> snps = (Set<InterMineObject>) gene.getFieldValue("snps");

				Map<InterMineObject, Set<InterMineObject>> gwasMap = new HashMap<InterMineObject, Set<InterMineObject>>();
				Map<InterMineObject, Set<InterMineObject>> snpMap = new HashMap<InterMineObject, Set<InterMineObject>>();
				Map<InterMineObject, Set<InterMineObject>> publicationMap = new HashMap<InterMineObject, Set<InterMineObject>>();

				for (InterMineObject vaItem : snps) {
					InterMineObject snpItem = (InterMineObject) vaItem.getFieldValue("snp");
					Set<InterMineObject> genomeWideAssociations = (Set<InterMineObject>) snpItem
							.getFieldValue("genomeWideAssociations");
					for (InterMineObject gwasItem : genomeWideAssociations) {
						InterMineObject publicationItem = (InterMineObject) gwasItem
								.getFieldValue("publication");
						Set<InterMineObject> efoTerms = (Set<InterMineObject>) gwasItem
								.getFieldValue("efoTerms");
						for (InterMineObject efot : efoTerms) {
							if (gwasMap.get(efot) == null) {
								gwasMap.put(efot, new HashSet<InterMineObject>());
							}
							gwasMap.get(efot).add(gwasItem);
							if (snpMap.get(efot) == null) {
								snpMap.put(efot, new HashSet<InterMineObject>());
							}
							snpMap.get(efot).add(snpItem);
							if (publicationMap.get(efot) == null) {
								publicationMap.put(efot, new HashSet<InterMineObject>());
							}
							publicationMap.get(efot).add(publicationItem);
						}
					}
				}

				for (InterMineObject efot : gwasMap.keySet()) {
					String diseaseName = (String) efot.getFieldValue("name");
					InterMineObject item = (InterMineObject) DynamicUtil.simpleCreateObject(
							model.getClassDescriptorByName("GeneDiseasePair").getType());
					item.setFieldValue("title", String.format("%s > %s", symbol, diseaseName));
					item.setFieldValue("diseaseTerm", efot);
					item.setFieldValue("gene", gene);
					item.setFieldValue("gwas", gwasMap.get(efot));
					item.setFieldValue("snps", snpMap.get(efot));
					item.setFieldValue("publications", publicationMap.get(efot));

					osw.store(item);

					// LOG.info("create object: " + String.format("%s > %s", symbol, diseaseName));
				}

			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}

			count++;
		}
		LOG.info(String.format("Processed %d GWAS related genes.", count));

		HashSet<String> ignoredDiseaseNames = new HashSet<String>(IGNORED_DISEASE_NAMES);
		iterator = getClinvarGenes();
		count = 0;
		while (iterator.hasNext()) {
			ResultsRow<?> rr = (ResultsRow<?>) iterator.next();
			InterMineObject gene = (InterMineObject) rr.get(0);
			try {
				String symbol = (String) gene.getFieldValue("symbol");
				Set<InterMineObject> snps = (Set<InterMineObject>) gene.getFieldValue("snps");

				Map<InterMineObject, Set<InterMineObject>> alleleMap = new HashMap<InterMineObject, Set<InterMineObject>>();
				Map<InterMineObject, Set<InterMineObject>> snpMap = new HashMap<InterMineObject, Set<InterMineObject>>();
				Map<InterMineObject, Set<InterMineObject>> publicationMap = new HashMap<InterMineObject, Set<InterMineObject>>();

				for (InterMineObject vaItem : snps) {
					InterMineObject snpItem = (InterMineObject) vaItem.getFieldValue("snp");
					Set<InterMineObject> alleles = (Set<InterMineObject>) snpItem
							.getFieldValue("alleles");
					for (InterMineObject allele : alleles) {
						Set<InterMineObject> variations = (Set<InterMineObject>) allele
								.getFieldValue("variations");
						for (InterMineObject var : variations) {
							Set<InterMineObject> publications = (Set<InterMineObject>) var
									.getFieldValue("publications");
							Set<InterMineObject> diseaseTerms = (Set<InterMineObject>) var
									.getFieldValue("diseaseTerms");
							for (InterMineObject dt : diseaseTerms) {
								String diseaseTitle = (String) dt.getFieldValue("name");
								if (ignoredDiseaseNames.contains(diseaseTitle)) {
									continue;
								}
								if (alleleMap.get(dt) == null) {
									alleleMap.put(dt, new HashSet<InterMineObject>());
								}
								alleleMap.get(dt).add(allele);
								if (snpMap.get(dt) == null) {
									snpMap.put(dt, new HashSet<InterMineObject>());
								}
								snpMap.get(dt).add(snpItem);
								if (publicationMap.get(dt) == null) {
									publicationMap.put(dt, new HashSet<InterMineObject>());
								}
								publicationMap.get(dt).addAll(publications);
							}
						}
					}
				}

				for (InterMineObject dt : alleleMap.keySet()) {
					String diseaseName = (String) dt.getFieldValue("name");
					InterMineObject item = (InterMineObject) DynamicUtil.simpleCreateObject(
							model.getClassDescriptorByName("GeneDiseasePair").getType());
					item.setFieldValue("title", String.format("%s > %s", symbol, diseaseName));
					item.setFieldValue("diseaseTerm", dt);
					item.setFieldValue("gene", gene);
					item.setFieldValue("alleles", alleleMap.get(dt));
					item.setFieldValue("snps", snpMap.get(dt));
					item.setFieldValue("publications", publicationMap.get(dt));

					osw.store(item);

					// LOG.info("create object: " + String.format("%s > %s", symbol, diseaseName));
				}

			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}

			count++;
		}
		LOG.info(String.format("Processed %d ClinVar related genes.", count));

		osw.commitTransaction();
	}

	private Iterator<?> getGwasGenes() throws ObjectStoreException {
		Query q = new Query();
		QueryClass qcGene = new QueryClass(
				os.getModel().getClassDescriptorByName("Gene").getType());
		QueryClass qcVariationAnnotation = new QueryClass(
				os.getModel().getClassDescriptorByName("VariationAnnotation").getType());
		QueryClass qcSnp = new QueryClass(os.getModel().getClassDescriptorByName("SNP").getType());
		QueryClass qcGwas = new QueryClass(
				os.getModel().getClassDescriptorByName("GenomeWideAssociation").getType());

		q.addFrom(qcGene);
		q.addFrom(qcVariationAnnotation);
		q.addFrom(qcSnp);
		q.addFrom(qcGwas);
		q.addToSelect(qcGene);

		ConstraintSet cs = new ConstraintSet(ConstraintOp.AND);

		QueryCollectionReference qcr1 = new QueryCollectionReference(qcGene, "snps");
		cs.addConstraint(
				new ContainsConstraint(qcr1, ConstraintOp.CONTAINS, qcVariationAnnotation));
		QueryObjectReference qor1 = new QueryObjectReference(qcVariationAnnotation, "snp");
		cs.addConstraint(new ContainsConstraint(qor1, ConstraintOp.CONTAINS, qcSnp));
		QueryCollectionReference qcr2 = new QueryCollectionReference(qcSnp,
				"genomeWideAssociations");
		cs.addConstraint(new ContainsConstraint(qcr2, ConstraintOp.CONTAINS, qcGwas));

		q.setConstraint(cs);

		Results results = os.execute(q);
		return results.iterator();
	}

	private Iterator<?> getClinvarGenes() throws ObjectStoreException {
		Query q = new Query();
		QueryClass qcGene = new QueryClass(
				os.getModel().getClassDescriptorByName("Gene").getType());
		QueryClass qcVariationAnnotation = new QueryClass(
				os.getModel().getClassDescriptorByName("VariationAnnotation").getType());
		QueryClass qcSnp = new QueryClass(os.getModel().getClassDescriptorByName("SNP").getType());
		QueryClass qcAllele = new QueryClass(
				os.getModel().getClassDescriptorByName("Allele").getType());
		QueryClass qcVariation = new QueryClass(
				os.getModel().getClassDescriptorByName("Variation").getType());

		q.addFrom(qcGene);
		q.addFrom(qcVariationAnnotation);
		q.addFrom(qcSnp);
		q.addFrom(qcAllele);
		q.addFrom(qcVariation);
		q.addToSelect(qcGene);

		ConstraintSet cs = new ConstraintSet(ConstraintOp.AND);

		QueryCollectionReference qcr1 = new QueryCollectionReference(qcGene, "snps");
		cs.addConstraint(
				new ContainsConstraint(qcr1, ConstraintOp.CONTAINS, qcVariationAnnotation));
		QueryObjectReference qor1 = new QueryObjectReference(qcVariationAnnotation, "snp");
		cs.addConstraint(new ContainsConstraint(qor1, ConstraintOp.CONTAINS, qcSnp));
		QueryCollectionReference qcr2 = new QueryCollectionReference(qcSnp, "alleles");
		cs.addConstraint(new ContainsConstraint(qcr2, ConstraintOp.CONTAINS, qcAllele));
		QueryCollectionReference qcr3 = new QueryCollectionReference(qcAllele, "variations");
		cs.addConstraint(new ContainsConstraint(qcr3, ConstraintOp.CONTAINS, qcVariation));

		q.setConstraint(cs);

		Results results = os.execute(q);
		return results.iterator();
	}

}
