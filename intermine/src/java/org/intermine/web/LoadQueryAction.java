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

import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.actions.DispatchAction;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * Implementation of <strong>Action</strong> that sets the current Query for
 * the session from a saved Query.
 *
 * @author Kim Rutherford
 */
public class LoadQueryAction extends DispatchAction
{
    /**
     * Load a query ie. take a query from the exampleQueries and make it the current one
     * @param mapping The ActionMapping used to select this instance
     * @param form The optional ActionForm bean for this request (if any)
     * @param request The HTTP request we are processing
     * @param response The HTTP response we are creating
     * @return an ActionForward object defining where control goes next
     * @exception Exception if the application business logic throws
     *  an exception
     */
    public ActionForward load(ActionMapping mapping,
                              ActionForm form,
                              HttpServletRequest request,
                              HttpServletResponse response)
        throws Exception {
        HttpSession session = request.getSession();
        ServletContext servletContext = session.getServletContext();
        Map exampleQueries = (Map) servletContext.getAttribute(Constants.EXAMPLE_QUERIES);
        String queryName = request.getParameter("name");

        loadQuery((PathQuery) exampleQueries.get(queryName), session);

        return mapping.findForward("query");
    }
    
    /**
     * Load a query into the session, cloning to avoid modifying the original
     * @param query the query
     * @param session the session
     */
    public static void loadQuery(PathQuery query, HttpSession session) {
        session.setAttribute(Constants.QUERY, query.clone());
        //at the moment we can only load queries that have saved using the webapp
        //this is because the webapp satisfies our (dumb) assumption that the view list is not empty
        String path = (String) query.getView().iterator().next();
        if (path.indexOf(".") != -1) {
            path = path.substring(0, path.indexOf("."));
        }
        session.setAttribute("path", path);
        session.removeAttribute("prefix");
    }
}
