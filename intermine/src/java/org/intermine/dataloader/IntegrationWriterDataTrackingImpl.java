package org.intermine.dataloader;

/*
 * Copyright (C) 2002-2004 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.intermine.metadata.CollectionDescriptor;
import org.intermine.metadata.FieldDescriptor;
import org.intermine.model.InterMineObject;
import org.intermine.model.datatracking.Source;
import org.intermine.objectstore.ObjectStoreWriter;
import org.intermine.objectstore.ObjectStoreWriterFactory;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.objectstore.intermine.ObjectStoreWriterInterMineImpl;
import org.intermine.objectstore.proxy.ProxyReference;
import org.intermine.sql.Database;
import org.intermine.util.DynamicUtil;

import org.apache.log4j.Logger;

/**
 * Priority-based implementation of IntegrationWriter. Allows field values to be chosen according
 * to the relative priorities of the data sources that originated them.
 *
 * @author Matthew Wakeling
 * @author Andrew Varley
 */
public class IntegrationWriterDataTrackingImpl extends IntegrationWriterAbstractImpl
{
    private static final Logger LOG = Logger.getLogger(IntegrationWriterDataTrackingImpl.class);
    protected DataTracker dataTracker;

    /**
     * Creates a new instance of this class, given the properties defining it.
     *
     * @param props the Properties
     * @return an instance of this class
     * @throws ObjectStoreException sometimes
     */
    public static IntegrationWriterDataTrackingImpl getInstance(Properties props)
            throws ObjectStoreException {
        String writerAlias = props.getProperty("osw");
        if (writerAlias == null) {
            throw new ObjectStoreException(props.getProperty("alias") + " does not have an osw"
                    + " alias specified (check properties file)");
        }

        String trackerMaxSizeString = props.getProperty("datatrackerMaxSize");
        String trackerCommitSizeString = props.getProperty("datatrackerCommitSize");
        if (trackerMaxSizeString == null) {
            throw new ObjectStoreException(props.getProperty("alias") + " does not have a"
                    + " datatracker maximum size specified (check properties file)");
        }
        if (trackerCommitSizeString == null) {
            throw new ObjectStoreException(props.getProperty("alias") + " does not have a"
                    + " datatracker commit size specified (check properties file)");
        }

        ObjectStoreWriter writer = ObjectStoreWriterFactory.getObjectStoreWriter(writerAlias);
        try {
            int maxSize = Integer.parseInt(trackerMaxSizeString);
            int commitSize = Integer.parseInt(trackerCommitSizeString);
            Database db = ((ObjectStoreWriterInterMineImpl) writer).getDatabase();
            DataTracker newDataTracker = new DataTracker(db, maxSize, commitSize);

            return new IntegrationWriterDataTrackingImpl(writer, newDataTracker);
        } catch (Exception e) {
            IllegalArgumentException e2 = new IllegalArgumentException("Problem instantiating"
                    + " IntegrationWriterDataTrackingImpl " + props.getProperty("alias"));
            e2.initCause(e);
            throw e2;
        }
    }

    /**
     * Constructs a new instance of IntegrationWriterDataTrackingImpl.
     *
     * @param osw an instance of an ObjectStoreWriter, which we can use to access the database
     * @param dataTracker an instance of DataTracker, which we can use to store data tracking
     * information
     */
    public IntegrationWriterDataTrackingImpl(ObjectStoreWriter osw, DataTracker dataTracker) {
        super(osw);
        this.dataTracker = dataTracker;
    }

    /**
     * @see IntegrationWriter#getMainSource
     */
    public Source getMainSource(String name) throws ObjectStoreException {
        return dataTracker.stringToSource(name);
    }

    /**
     * @see IntegrationWriter#getSkeletonSource
     */
    public Source getSkeletonSource(String name) throws ObjectStoreException {
        return dataTracker.stringToSource("skel_" + name);
    }

    /**
     * Returns the data tracker being used.
     *
     * @return dataTracker
     */
    protected DataTracker getDataTracker() {
        return dataTracker;
    }

    /**
     * @see IntegrationWriterAbstractImpl#store(InterMineObject, Source, Source, int)
     */
    protected InterMineObject store(InterMineObject o, Source source, Source skelSource,
            int type) throws ObjectStoreException {
        if (o == null) {
            return null;
        }
        //String oText = o.getClass().getName() + ":" + o.getId().toString();
        //int oTextLength = oText.length();
        //oText = oText.substring(oTextLength > 60 ? 60 : oTextLength);
        //LOG.debug("store() called on " + oText);
        Set equivalentObjects = getEquivalentObjects(o, source);
        if ((equivalentObjects.size() == 1) && (type == SKELETON)) {
            InterMineObject onlyEquivalent = (InterMineObject)
                equivalentObjects.iterator().next();
            if (onlyEquivalent instanceof ProxyReference) {
                //LOG.debug("store() finished trivially for object " + oText);
                return onlyEquivalent;
            }
        }
        Integer newId = null;
        Iterator equivalentIter = equivalentObjects.iterator();
        if (equivalentIter.hasNext()) {
            newId = ((InterMineObject) equivalentIter.next()).getId();
        }
        Set classes = new HashSet();
        classes.addAll(DynamicUtil.decomposeClass(o.getClass()));
        Iterator objIter = equivalentObjects.iterator();
        while (objIter.hasNext()) {
            InterMineObject obj = (InterMineObject) objIter.next();
            if (obj instanceof ProxyReference) {
                obj = ((ProxyReference) obj).getObject();
            }
            try {
                classes.addAll(DynamicUtil.decomposeClass(obj.getClass()));
            } catch (Exception e) {
                LOG.error("Broken with: " + DynamicUtil.decomposeClass(o.getClass()));
                throw new ObjectStoreException(e);
            }
        }
        InterMineObject newObj = (InterMineObject) DynamicUtil.createObject(classes);
        newObj.setId(newId);

        Map trackingMap = new HashMap();
        try {
            Map fieldToEquivalentObjects = new HashMap();
            Map fieldDescriptors = getModel().getFieldDescriptorsForClass(newObj.getClass());
            Iterator fieldIter = fieldDescriptors.entrySet().iterator();
            while (fieldIter.hasNext()) {
                FieldDescriptor field = (FieldDescriptor) ((Map.Entry) fieldIter.next()).getValue();
                String fieldName = field.getName();
                if (!"id".equals(fieldName)) {
                    Set sortedEquivalentObjects;

                    if (field instanceof CollectionDescriptor) {
                        sortedEquivalentObjects = new HashSet();
                    } else {
                        Comparator compare = new SourcePriorityComparator(dataTracker, field,
                                (type == SOURCE ? source : skelSource), o, dbIdsStored);
                        sortedEquivalentObjects = new TreeSet(compare);
                    }

                    if (getModel().getFieldDescriptorsForClass(o.getClass())
                            .containsKey(fieldName)) {
                        sortedEquivalentObjects.add(o);
                    }
                    objIter = equivalentObjects.iterator();
                    while (objIter.hasNext()) {
                        InterMineObject obj = (InterMineObject) objIter.next();
                        Source fieldSource = dataTracker.getSource(obj.getId(), fieldName);
                        if ((equivalentObjects.size() == 1) && (fieldSource != null)
                                && (fieldSource.equals(source) || (fieldSource.equals(skelSource)
                                        && (type != SOURCE)))) {
                            if (type == SOURCE) {
                                String errMessage = "Unequivalent objects have the same"
                                    + " non-skeleton Source; o1 = \"" + o
                                    + "\" (from source), o2 = \"" + obj + "\"("
                                    + (dbIdsStored.contains(obj.getId()) ? "stored in this run"
                                            : "from database") + "), source1 = \"" + source
                                    + "\", source2 = \"" + fieldSource + "\" for field \""
                                    + field.getName() + "\"";
                                if (!ignoreDuplicates) {
                                    LOG.error(errMessage);
                                    throw new IllegalArgumentException(errMessage);
                                }
                            }
                            if (type != FROM_DB) {
                                assignMapping(o.getId(), obj.getId());
                            }
                            //LOG.debug("store() finished simply for object " + oText);
                            return obj;
                        }
                        // materialise proxies before searching for this field
                        if (obj instanceof ProxyReference) {
                             obj = ((ProxyReference) obj).getObject();
                        }
                        if (getModel().getFieldDescriptorsForClass(obj.getClass())
                                .containsKey(fieldName)) {
                            sortedEquivalentObjects.add(obj);
                        }
                    }
                    fieldToEquivalentObjects.put(field, sortedEquivalentObjects);
                }
            }

            Iterator fieldToEquivIter = fieldToEquivalentObjects.entrySet().iterator();
            while (fieldToEquivIter.hasNext()) {
                Source lastSource = null;
                Map.Entry fieldToEquivEntry = (Map.Entry) fieldToEquivIter.next();
                FieldDescriptor field = (FieldDescriptor) fieldToEquivEntry.getKey();
                Set sortedEquivalentObjects = (Set) fieldToEquivEntry.getValue();
                String fieldName = field.getName();

                objIter = sortedEquivalentObjects.iterator();
                while (objIter.hasNext()) {
                    InterMineObject obj = (InterMineObject) objIter.next();
                    if (obj == o) {
                        copyField(obj, newObj, source, skelSource, field, type);
                        lastSource = (type == SOURCE ? source : skelSource);
                    } else {
                        Source fieldSource = dataTracker.getSource(obj.getId(), fieldName);
                        copyField(obj, newObj, fieldSource, fieldSource, field, FROM_DB);
                        lastSource = fieldSource;
                    }
                }
                trackingMap.put(fieldName, lastSource);
            }
        } catch (IllegalAccessException e) {
            throw new ObjectStoreException(e);
        }
        store(newObj);

        // We have called store() on an object, and we are about to write all of its data tracking
        // data. We should tell the data tracker, ONLY IF THE ID OF THE OBJECT IS NEW, so that
        // the data tracker can cache the writes without having to ask the db if records for that
        // objectid already exist - we know there aren't.
        if (newId == null) {
            dataTracker.clearObj(newObj.getId());
        }

        Iterator trackIter = trackingMap.entrySet().iterator();
        while (trackIter.hasNext()) {
            Map.Entry trackEntry = (Map.Entry) trackIter.next();
            String fieldName = (String) trackEntry.getKey();
            Source lastSource = (Source) trackEntry.getValue();
            dataTracker.setSource(newObj.getId(), fieldName, lastSource);
        }

        while (equivalentIter.hasNext()) {
            InterMineObject objToDelete = (InterMineObject) equivalentIter.next();
            delete(objToDelete);
        }

        if (type != FROM_DB) {
            assignMapping(o.getId(), newObj.getId());
        }
        //LOG.debug("store() finished normally for object " + oText);
        return newObj;
    }

    /**
     * @see IntegrationWriterAbstractImpl#commitTransaction
    public void commitTransaction() throws ObjectStoreException {
        osw.commitTransaction();
        dataTracker.flush();
    }
     */

    /**
     * @see IntegrationWriterAbstractImpl#close
     */
    public void close() {
        osw.close();
        dataTracker.flush();
    }
}
