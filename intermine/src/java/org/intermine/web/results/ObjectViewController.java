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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.tiles.actions.TilesAction;
import org.apache.struts.tiles.ComponentContext;

import org.intermine.metadata.Model;
import org.intermine.metadata.PrimaryKeyUtil;
import org.intermine.metadata.FieldDescriptor;
import org.intermine.model.InterMineObject;
import org.intermine.util.DynamicUtil;
import org.intermine.util.TypeUtil;
import org.intermine.web.Constants;
import org.intermine.objectstore.ObjectStore;

/**
 * Implementation of <strong>TilesAction</strong> that assembles data for displaying an object.
 *
 * @author Andrew Varley
 * @author Kim Rutherford
 */
public class ObjectViewController extends TilesAction
{
    /**
     * Assembles data for displaying an object.
     *
     * @param context The Tiles ComponentContext
     * @param mapping The ActionMapping used to select this instance
     * @param form The optional ActionForm bean for this request (if any)
     * @param request The HTTP request we are processing
     * @param response The HTTP response we are creating
     * @return an ActionForward object defining where control goes next
     *
     * @exception Exception if an error occurs
     */
    public ActionForward execute(ComponentContext context,
                                 ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
        throws Exception {
        HttpSession session = request.getSession();
        ServletContext servletContext = session.getServletContext();
        ObjectStore os = (ObjectStore) servletContext.getAttribute(Constants.OBJECTSTORE);
        Model model = (Model) os.getModel();
        Object o = request.getAttribute("object");

        if (o == null) {
            String idString = (String) request.getParameter("id");
            if (idString != null) {
                Integer id = new Integer(idString);
                String field = request.getParameter("field");
                o = os.getObjectById(id);

                if (o != null && field != null) {
                    o = TypeUtil.getFieldValue(o, field);
                }
                context.putAttribute("object", o);
            }
        }

        Set leafClds = null;
        Map primaryKeyFields = null;

        if (o instanceof InterMineObject) {
            leafClds = getLeafClds(o.getClass(), model);

            if ("summary".equals((String) request.getAttribute("viewType"))) {
                primaryKeyFields = new LinkedHashMap();
                Class c = o.getClass();
                for (Iterator i = PrimaryKeyUtil.getPrimaryKeyFields(model, c).iterator();
                     i.hasNext();) {
                    FieldDescriptor fd = (FieldDescriptor) i.next();
                    primaryKeyFields.put(fd.getName(), fd.getName());
                }
            }
        } else {
            leafClds = new HashSet();
            primaryKeyFields = new HashMap();
        }

        context.putAttribute("leafClds", leafClds);
        context.putAttribute("primaryKeyFields", primaryKeyFields);

        return null;
    }

    /**
     * Get the class descriptors for a Class in the Model
     * @param c the Class
     * @param model the Model
     * @return the Set of ClassDescriptors
     */
    public static Set getLeafClds(Class c, Model model) {
        Set leafClds = new HashSet();
        for (Iterator i = DynamicUtil.decomposeClass(c).iterator(); i.hasNext();) {
            leafClds.add(model.getClassDescriptorByName(((Class) i.next()).getName()));
        }
        return leafClds;
    }
}
