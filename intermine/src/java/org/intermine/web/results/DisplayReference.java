package org.intermine.web.results;

/*
 * Copyright (C) 2002-2004 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.util.Iterator;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.intermine.metadata.PrimaryKeyUtil;
import org.intermine.metadata.FieldDescriptor;
import org.intermine.metadata.ClassDescriptor;
import org.intermine.objectstore.proxy.ProxyReference;
import org.intermine.util.TypeUtil;

/**
 * Class to represent a reference field of an object for display in the webapp
 * @author Mark Woodbridge
 */
public class DisplayReference extends DisplayField
{
    ProxyReference proxy;
    Map keyAttributes;

    /**
     * Create a new DisplayReference object.
     * @param proxy proxy for the referenced object
     * @param cld metadata for the referenced object
     * @param webconfigTypeMap the Type Map from the webconfig file
     * @param webProperties the web properties from the session
     * @throws Exception if an error occurs
     */
    public DisplayReference(ProxyReference proxy, ClassDescriptor cld,
                            Map webconfigTypeMap, Map webProperties) throws Exception {
        super(getProxyList(proxy), cld, webconfigTypeMap, webProperties);
        this.proxy = proxy;
        this.cld = cld;
    }

    /**
     * Get the id of the object
     * @return the id
     */
    public int getId() {
        return proxy.getId().intValue();
    }
    
    /**
     * Get the referenced object
     * @return the object
     */
    public Object getObject() {
        return proxy.getObject();
    }

    /**
     * Get the identifier fields and values for the object
     * @return the identifiers
     * @throws Exception if an error occurs
     */
    public Map getKeyAttributes() throws Exception {
        if (keyAttributes == null) {
            keyAttributes = new HashMap();
            Set pks = PrimaryKeyUtil.getPrimaryKeyFields(cld.getModel(),
                                                         proxy.getObject().getClass());
            for (Iterator i = pks.iterator(); i.hasNext();) {
                FieldDescriptor fd = (FieldDescriptor) i.next();
                if (fd.isAttribute()) {
                    Object fieldValue = TypeUtil.getFieldValue(proxy.getObject(), fd.getName());
                    keyAttributes.put(fd.getName(), fieldValue);
                }
            }
        }
        return keyAttributes;
    }

    /**
     * Helper method for the constructor.
     */
    private static List getProxyList(ProxyReference proxy) {
        List proxyList = new ArrayList();
        proxyList.add(proxy);
        return proxyList;
    }
}