package org.intermine.web.results;

/*
 * Copyright (C) 2002-2005 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.Collections;

import org.intermine.metadata.ClassDescriptor;
import org.intermine.metadata.Model;
import org.intermine.model.InterMineObject;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.objectstore.proxy.ProxyReference;
import org.intermine.web.config.FieldConfig;
import org.intermine.web.config.FieldConfigHelper;
import org.intermine.web.config.WebConfig;
import org.intermine.web.Constants;

import org.apache.log4j.Logger;

/**
 * An inline table created from a Collection
 * This table has one object per row
 * @author Mark Woodbridge
 */
public class InlineResultsTable
{
    protected static final Logger LOG = Logger.getLogger(InlineResultsTable.class);

    protected List results;
    // just those objects that we will display
    protected List subList;
    protected ClassDescriptor cld;
    protected List columns = null;
    // a list of list of values for the table
    protected List tableRows = null;
    protected Model model;
    protected int size = 30;
    protected WebConfig webConfig;

    /**
     * Construct a new InlineResultsTable object
     * @param results the underlying SingletonResults object
     * @param cld the type of this collection field
     * @param webConfig the WebConfig object for this webapp
     * @param webProperties the web properties from the session
     * @throws ObjectStoreException if an error occurs
     */
    public InlineResultsTable(List results, ClassDescriptor cld,
                              WebConfig webConfig, Map webProperties)
        throws ObjectStoreException {
        this.results = results;
        this.cld = cld;
        this.webConfig = webConfig;
        this.model = cld.getModel();

        // default
        int maxInlineTableSize = 30;

        String maxInlineTableSizeString = (String) webProperties.get(Constants.INLINE_TABLE_SIZE);

        try {
            maxInlineTableSize = Integer.parseInt(maxInlineTableSizeString);
        } catch (NumberFormatException e) {
            LOG.warn("Failed to parse " + Constants.INLINE_TABLE_SIZE + " property: "
                     + maxInlineTableSizeString);
        }

        size = maxInlineTableSize;

        try {
            results.get(size);
        } catch (IndexOutOfBoundsException e) {
            size = results.size();
        }
    }

    /**
     * Return heading for the columns
     * @return the column names
     */
    public List getColumnNames() {
        if (columns == null) {
            initialise();
        }

        List columnNames = new ArrayList();

        Iterator columnIter = columns.iterator();

        while (columnIter.hasNext()) {
            columnNames.add(((FieldConfig) columnIter.next()).getFieldExpr());
        }
        return Collections.unmodifiableList(columnNames);
    }

    /**
     * Get the rows of the table
     * @return the rows of the table
     * @throws Exception if an error occurs accessing the ObjectStore
     */
    public List getRows() throws Exception {
        if (tableRows == null) {
            initialise();
        }

        return tableRows;
    }

    /**
     * Get the class descriptors of each object displayed in the table
     * @return the set of class descriptors for each row
     */
    public List getTypes() {
        List types = new ArrayList();
        for (Iterator i = results.subList(0, size).iterator(); i.hasNext();) {
            Object o = i.next();
            if (o instanceof ProxyReference) {
                // special case for ProxyReference from DisplayReference objects
                o = ((ProxyReference) o).getObject();
            }
            types.add(ObjectViewController.getLeafClds(o.getClass(), cld.getModel()));
        }
        return types;
    }

    /**
     * Get the ids of the objects in the rows
     * @return a List of ids, one per row
     */
    public List getIds() {
        List ids = new ArrayList();
        for (Iterator i = results.subList(0, size).iterator(); i.hasNext();) {
            ids.add(((InterMineObject) i.next()).getId());
        }
        return ids;
    }

    /**
     * Return the Objects that we are displaying in this table.
     * @return a List of Objects, one per row
     */
    public List getRowObjects() {
        if (subList == null) {
            initialise();
        }

        return subList;
    }

    /**
     * Create the tableRows, columnNames and subList Lists by looping over the first elements of the
     * collection.  The names of all fields to be displayed are collected in the columns List then
     * and the expressions to display are collected in the tableRows List.  The fields of the rows
     * in the tableRows List will always be in the same order as the elements of the columns List.
     */
    protected void initialise() {
        tableRows = new ArrayList();
        columns = new ArrayList();
        subList = new ArrayList();

        Iterator resultsIter = results.subList(0, size).iterator();

        while (resultsIter.hasNext()) {
            Object o = resultsIter.next();

            if (o instanceof ProxyReference) {
                // special case for ProxyReference from DisplayReference objects
                o = ((ProxyReference) o).getObject();
            }

            subList.add(o);

            List objectFieldConfigs = getRowFieldConfigs(o);
            Iterator objectFieldConfigIter = objectFieldConfigs.iterator();

            while (objectFieldConfigIter.hasNext()) {
                FieldConfig fc = (FieldConfig) objectFieldConfigIter.next();

                if (!columns.contains(fc)) {
                    columns.add(fc);
                }
            }

            // add a row that contains the fields of the current object but add a null where this
            // object doesn't have the given field
            Iterator columnIter = columns.iterator();

            List newRow = new ArrayList();

            while (columnIter.hasNext()) {
                FieldConfig fc = (FieldConfig) columnIter.next();

                if (objectFieldConfigs.contains(fc)) {
                    newRow.add(fc.getFieldExpr());
                } else {
                    newRow.add(null);
                }
            }

            tableRows.add(newRow);
        }

        // now make sure that all rows are the same length by adding nulls to the ends of the short
        // rows
        Iterator tableRowIter = tableRows.iterator();

        while (tableRowIter.hasNext()) {
            List row = (List) tableRowIter.next();

            while (row.size() < columns.size()) {
                row.add(null);
            }
        }
    }

    /**
     * Find the FieldConfig objects for the the given Object.
     * @param rowObject an Object
     * @return the FieldConfig objects for the the given Object.
     */
    protected List getRowFieldConfigs(Object rowObject) {
        List returnFieldConfigs = new ArrayList();

        Set objectClassDescriptors = ObjectViewController.getLeafClds(rowObject.getClass(), model);

        Iterator classDescriptorsIter = objectClassDescriptors.iterator();

        while (classDescriptorsIter.hasNext()) {
            ClassDescriptor thisClassDescriptor = (ClassDescriptor) classDescriptorsIter.next();

            returnFieldConfigs.addAll(getClassFieldConfigs(thisClassDescriptor));
        }

        return returnFieldConfigs;
    }

    /**
     * Find the FieldConfig objects for the the given ClassDescriptor.
     * @param cd a ClassDescriptor
     * @return the FieldConfig objects for the the given ClassDescriptor
     */
    protected List getClassFieldConfigs(ClassDescriptor cd) {
        return FieldConfigHelper.getClassFieldConfigs(webConfig, cd);
    }
}
