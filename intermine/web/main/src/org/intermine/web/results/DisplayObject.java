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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.intermine.metadata.ClassDescriptor;
import org.intermine.metadata.CollectionDescriptor;
import org.intermine.metadata.FieldDescriptor;
import org.intermine.metadata.Model;
import org.intermine.metadata.PrimaryKeyUtil;
import org.intermine.metadata.ReferenceDescriptor;
import org.intermine.model.InterMineObject;
import org.intermine.objectstore.proxy.ProxyReference;
import org.intermine.util.TypeUtil;
import org.intermine.web.config.FieldConfig;
import org.intermine.web.config.FieldConfigHelper;
import org.intermine.web.config.WebConfig;

/**
 * Class to represent an object for display in the webapp
 * @author Mark Woodbridge
 */
public class DisplayObject
{
    InterMineObject object;
    WebConfig webConfig;
    Map webProperties;
    Model model;

    Set clds;

    Map attributes = null;
    Map references = null;
    Map collections = null;
    Map refsAndCollections = null;
    List keyAttributes = null;
    List keyReferences = null;
    private List fieldExprs = null;

    Map verbosity = new HashMap();

    /**
     * Create a new DisplayObject.
     * @param object the object to display
     * @param model the metadata for the object
     * @param webConfig the WebConfig object for this webapp
     * @param webProperties the web properties from the session
     * @throws Exception if an error occurs
     */
    public DisplayObject(InterMineObject object, Model model,
                         WebConfig webConfig, Map webProperties) throws Exception {
        this.object = object;
        this.model = model;
        this.webConfig = webConfig;
        this.webProperties = webProperties;

        clds = ObjectViewController.getLeafClds(object.getClass(), model);
    }

    /**
     * Get the real business object
     * @return the object
     */
    public InterMineObject getObject() {
        return object;
    }

    /**
     * Get the id of this object
     * @return the id
     */
    public int getId() {
        return object.getId().intValue();
    }

    /**
     * Get the class descriptors for this object
     * @return the class descriptors
     */
    public Set getClds() {
        return clds;
    }

    /**
     * Get the key attribute fields and values for this object
     * @return the key attributes
     */
    public List getKeyAttributes() {
        if (keyAttributes == null) {
            initialise();
        }
        return keyAttributes;
    }

    /**
     * Get the key reference fields and values for this object
     * @return the key references
     */
    public List getKeyReferences() {
        if (keyReferences == null) {
            initialise();
        }
        return keyReferences;
    }

    /**
     * Get the attribute fields and values for this object
     * @return the attributes
     */
    public Map getAttributes() {
        if (attributes == null) {
            initialise();
        }
        return attributes;
    }

    /**
     * Get the reference fields and values for this object
     * @return the references
     */
    public Map getReferences() {
        if (references == null) {
            initialise();
        }
        return references;
    }

    /**
     * Get the collection fields and values for this object
     * @return the collections
     */
    public Map getCollections() {
        if (collections == null) {
            initialise();
        }
        return collections;
    }

    /**
     * Get all the reference and collection fields and values for this object
     * @return the collections
     */
    public Map getRefsAndCollections() {
        if (refsAndCollections == null) {
            initialise();
        }
        return refsAndCollections;
    }

    /**
     * Return the path expressions for the fields that should be used when summarising this
     * DisplayObject.
     * @return the expressions
     */
    public List getFieldExprs() {
        if (fieldExprs == null) {
            fieldExprs = new ArrayList();

            for (Iterator i = clds.iterator(); i.hasNext();) {
                ClassDescriptor cld = (ClassDescriptor) i.next();
                List cldFieldConfigs = FieldConfigHelper.getClassFieldConfigs(webConfig, cld);
                Iterator cldFieldConfigIter = cldFieldConfigs.iterator();

                while (cldFieldConfigIter.hasNext()) {
                    FieldConfig fc = (FieldConfig) cldFieldConfigIter.next();

                    fieldExprs.add(fc.getFieldExpr());
                }
            }
        }
        return fieldExprs;
    }

    /**
     * Get the map indication whether individuals fields are to be display verbosely
     * @return the map
     */
    public Map getVerbosity() {
        return Collections.unmodifiableMap(verbosity);
    }

    /**
     * Set the verbosity for a field
     * @param fieldName the field name
     * @param verbose true or false
     */
    public void setVerbosity(String fieldName, boolean verbose) {
        verbosity.put(fieldName, verbose ? fieldName : null);
    }

    /**
     * Create the Maps and Lists returned by the getters in this class.
     */
    private void initialise() {
        attributes = new TreeMap(String.CASE_INSENSITIVE_ORDER);
        references = new TreeMap(String.CASE_INSENSITIVE_ORDER);
        collections = new TreeMap(String.CASE_INSENSITIVE_ORDER);
        refsAndCollections = new TreeMap(String.CASE_INSENSITIVE_ORDER);
        keyAttributes = new ArrayList();
        keyReferences = new ArrayList();

        try {
            for (Iterator i = clds.iterator(); i.hasNext();) {
                ClassDescriptor cld = (ClassDescriptor) i.next();
                for (Iterator j = cld.getAllFieldDescriptors().iterator(); j.hasNext();) {
                    FieldDescriptor fd = (FieldDescriptor) j.next();

                    if (fd.isAttribute() && !fd.getName().equals("id")) {
                        Object fieldValue = TypeUtil.getFieldValue(object, fd.getName());
                        if (fieldValue != null) {
                            attributes.put(fd.getName(), fieldValue);
                        }
                    } else if (fd.isReference()) {
                        ReferenceDescriptor ref = (ReferenceDescriptor) fd;
                        //check whether reference is null without dereferencing
                        ProxyReference proxy =
                            (ProxyReference) TypeUtil.getFieldProxy(object, ref.getName());
                        if (proxy != null) {
                            DisplayReference newReference =
                                new DisplayReference(proxy, ref.getReferencedClassDescriptor(),
                                                     webConfig, webProperties);
                            references.put(fd.getName(), newReference);
                        }
                    } else if (fd.isCollection()) {
                        Object fieldValue = TypeUtil.getFieldValue(object, fd.getName());
                        ClassDescriptor refCld =
                            ((CollectionDescriptor) fd).getReferencedClassDescriptor();
                        DisplayCollection newCollection =
                            new DisplayCollection((Collection) fieldValue, refCld,
                                                  webConfig, webProperties);
                        if (newCollection.getSize() > 0) {
                            collections.put(fd.getName(), newCollection);
                        }
                    }
                }
            }

            Iterator i = PrimaryKeyUtil.getPrimaryKeyFields(model, object.getClass()).iterator();

            while (i.hasNext()) {
                FieldDescriptor fd = (FieldDescriptor) i.next();
                if (TypeUtil.getFieldValue(object, fd.getName()) != null) {
                    if (fd.isAttribute() && !fd.getName().equals("id")) {
                        keyAttributes.add(fd.getName());
                    } else if (fd.isReference()) {
                        keyReferences.add(fd.getName());
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Exception while creating a DisplayObject", e);
        }

        // make a combined Map
        refsAndCollections.putAll(references);
        refsAndCollections.putAll(collections);
    }
}
