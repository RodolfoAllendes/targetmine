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
import java.util.Iterator;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.tiles.actions.TilesAction;
import org.apache.struts.tiles.ComponentContext;
import org.intermine.metadata.ClassDescriptor;

import org.intermine.metadata.Model;
import org.intermine.model.InterMineObject;
import org.intermine.web.Constants;
import org.intermine.objectstore.ObjectStore;

/**
 * Controller for the object trail tile.
 *
 * @author Thomas Riley
 */
public class ObjectTrailController extends TilesAction
{
    protected static final Logger LOG = Logger.getLogger(ObjectTrailController.class);
    
    
    /**
     * Looks at the "trail" request parameter and extracts the object ids from it, then
     * looks up the actual objects and creates a list of TrailItems.
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
        String trail = request.getParameter("trail");
        String ids[] = StringUtils.split(trail.substring(1), '_');
        ArrayList elements = new ArrayList();
        String elementTrail = "";
        
        for (int i = 0; i < ids.length; i++) {
            elementTrail += "_" + ids[i];
            InterMineObject o = os.getObjectById(new Integer(ids[i]));
            if (o == null) {
                LOG.warn("failed to getObjectById " + ids[i]);
                continue;
            }
            String label = createTrailLabel(o, model);
            elements.add(new TrailElement(label, elementTrail, o.getId().intValue()));
        }
        
        request.setAttribute("trailElements", elements);
        return null;
    }
    
    /**
     * Create trail element label. Label is a list of each leaf class name.
     *
     * @param object the intermine object associated with the trail element
     * @param model the model
     * @return label for TrailElement
     */
    protected static String createTrailLabel(InterMineObject object, Model model) {
        Iterator iter = ObjectViewController.getLeafClds(object.getClass(), model).iterator();
        String label = "";
        while (iter.hasNext()) {
            label += ((ClassDescriptor) iter.next()).getUnqualifiedName() + " ";
        }
        return StringUtils.trim(label);
    }
    
    /**
     * Bean passed to JSP to represent an element in the trail.
     */
    public static class TrailElement
    {
        private String label;
        private String trail;
        private int id;
        
        private TrailElement(String label, String trail, int id) {
            this.label = label;
            this.trail = trail;
            this.id = id;
        }
        
        /**
         * Get the trail URL parameter for this trail element.
         * @return trail URL parameter for the trail element
         */
        public String getTrail() {
            return trail;
        }
        
        /**
         * Get the label for this trail element.
         * @return label for the trail element
         */
        public String getLabel() {
            return label;
        }
        
        /**
         * Get the object id for this trail element.
         * @return the object id for the trail element
         */
        public int getObjectId() {
            return id;
        }
    }
}
