package org.intermine.web;

/*
 * Copyright (C) 2002-2004 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMessage;

import org.apache.log4j.Logger;

import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.objectstore.ObjectStoreQueryDurationException;
import org.intermine.objectstore.query.iql.IqlQuery;
import org.intermine.objectstore.query.Query;
import org.intermine.web.results.TableHelper;

/**
 * Implementation of <strong>Action</strong> that runs a Query
 *
 * @author Andrew Varley
 */
public class IqlQueryAction extends InterMineLookupDispatchAction
{
    private static final Logger LOG = Logger.getLogger(IqlQueryAction.class);

    /**
     * Process the specified HTTP request, and create the corresponding HTTP
     * response (or forward to another web component that will create it).
     * Return an <code>ActionForward</code> instance describing where and how
     * control should be forwarded, or <code>null</code> if the response has
     * already been completed.
     *
     * @param mapping The ActionMapping used to select this instance
     * @param form The optional ActionForm bean for this request (if any)
     * @param request The HTTP request we are processing
     * @param response The HTTP response we are creating
     * @return an ActionForward object defining where control goes next
     *
     * @exception Exception if the application business logic throws
     *  an exception
     */
    public ActionForward run(ActionMapping mapping,
                             ActionForm form,
                             HttpServletRequest request,
                             HttpServletResponse response)
        throws Exception {
        HttpSession session = request.getSession();
        ServletContext servletContext = session.getServletContext();
        ObjectStore os = (ObjectStore) servletContext.getAttribute(Constants.OBJECTSTORE);
        Model model = (Model) os.getModel();

        IqlQueryForm queryform = (IqlQueryForm) form;

        try {
            Query q = new IqlQuery(queryform.getQuerystring(),
                                   ((Model) os.getModel()).getPackageName()).toQuery();
            session.setAttribute(Constants.RESULTS_TABLE, TableHelper.makeTable(os, q));
            return mapping.findForward("results");
        } catch (java.lang.IllegalArgumentException e) {
            recordError(new ActionMessage("errors.iqlquery.illegalargument",
                                          e.getMessage()), request, e, LOG);
            return mapping.findForward("iqlQuery");
        } catch (ObjectStoreException e) {
            ActionErrors errors = new ActionErrors();
            if (e instanceof ObjectStoreQueryDurationException) {
                recordError(new ActionMessage("errors.query.estimatetimetoolong"), request, e, LOG);
            } else {
                recordError(new ActionMessage("errors.query.objectstoreerror"), request, e, LOG);
            }

            return mapping.findForward("iqlQuery");
        }
    }

    /**
     * Distributes the actions to the necessary methods, by providing a Map from action to
     * the name of a method.
     *
     * @return a Map
     */
    protected Map getKeyMethodMap() {
        Map map = new HashMap();
        map.put("button.run", "run");
        return map;
    }
}
