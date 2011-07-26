package org.intermine.bio.postprocess;

import java.util.Collections;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.intermine.bio.util.Constants;
import org.intermine.metadata.Model;
import org.intermine.model.InterMineObject;
import org.intermine.model.bio.DataSet;
import org.intermine.model.bio.Protein;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.objectstore.ObjectStoreWriter;
import org.intermine.objectstore.intermine.ObjectStoreInterMineImpl;
import org.intermine.objectstore.query.ConstraintOp;
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

public class StitchPostProcess extends PostProcessor {

	private static final Logger LOG = Logger.getLogger(StitchPostProcess.class);
	private DataSet dataSet = null;
	private Model model;

	public StitchPostProcess(ObjectStoreWriter osw) {
		super(osw);
		model = Model.getInstanceByName("genomic");
	}

	@Override
	public void postProcess() throws ObjectStoreException {
		createProteinCompoundGroupInteractions();
	}

	private void createProteinCompoundGroupInteractions() throws ObjectStoreException {

		dataSet = (DataSet) DynamicUtil.createObject(Collections.singleton(DataSet.class));
		dataSet.setName("STITCH");
		dataSet = (DataSet) osw.getObjectByExample(dataSet, Collections.singleton("name"));
		if (dataSet == null) {
			LOG.error("Failed to find STITCH DataSet object");
			return;
		}

		Results results = findProteinCompoundGroup(osw.getObjectStore());
		osw.beginTransaction();

		Iterator<?> resIter = results.iterator();
		int i = 0;
		while (resIter.hasNext()) {
			ResultsRow<?> rr = (ResultsRow<?>) resIter.next();
			InterMineObject protein = (InterMineObject) rr.get(0);
			InterMineObject compoundGroup = (InterMineObject) rr.get(1);

			InterMineObject cgi = (InterMineObject) DynamicUtil.simpleCreateObject(model
					.getClassDescriptorByName("CompoundGroupInteraction").getType());

			cgi.setFieldValue("protein", protein);
			cgi.setFieldValue("compoundGroup", compoundGroup);
			cgi.setFieldValue("dataSet", dataSet);
			try {
				String primaryAcc = (String) protein.getFieldValue("primaryAccession");
				String inchiKey = (String) compoundGroup.getFieldValue("inchiKey");
				cgi.setFieldValue("identifier", String.format("%s_%s", primaryAcc, inchiKey));
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}

			osw.store(cgi);
			i++;
		}

		osw.commitTransaction();
		System.out.println(i + " CompoundGroupInteraction created.");
	}

	protected static Results findProteinCompoundGroup(ObjectStore os) throws ObjectStoreException {
		Query q = new Query();
		QueryClass qcProtein = new QueryClass(Protein.class);
		QueryClass qcStitchInteraction = new QueryClass(os.getModel().getClassDescriptorByName(
				"StitchInteraction").getType());
		QueryClass qcPubChemCompound = new QueryClass(os.getModel().getClassDescriptorByName(
				"PubChemCompound").getType());
		QueryClass qcCompoundGroup = new QueryClass(os.getModel().getClassDescriptorByName(
				"CompoundGroup").getType());

		q.addFrom(qcProtein);
		q.addFrom(qcStitchInteraction);
		q.addFrom(qcPubChemCompound);
		q.addFrom(qcCompoundGroup);

		q.addToSelect(qcProtein);
		q.addToSelect(qcCompoundGroup);

		ConstraintSet cs = new ConstraintSet(ConstraintOp.AND);

		// Protein.chemicalInteractions.compound.compoundGroup
		QueryCollectionReference c1 = new QueryCollectionReference(qcProtein,
				"chemicalInteractions");
		cs.addConstraint(new ContainsConstraint(c1, ConstraintOp.CONTAINS, qcStitchInteraction));

		QueryObjectReference r2 = new QueryObjectReference(qcStitchInteraction, "compound");
		cs.addConstraint(new ContainsConstraint(r2, ConstraintOp.CONTAINS, qcPubChemCompound));

		QueryObjectReference r3 = new QueryObjectReference(qcPubChemCompound,
				"compoundGroup");
		cs.addConstraint(new ContainsConstraint(r3, ConstraintOp.CONTAINS, qcCompoundGroup));

		q.setConstraint(cs);

		ObjectStoreInterMineImpl osimi = (ObjectStoreInterMineImpl) os;
		osimi.precompute(q, Constants.PRECOMPUTE_CATEGORY);
		Results res = os.execute(q);

		return res;
	}
}
