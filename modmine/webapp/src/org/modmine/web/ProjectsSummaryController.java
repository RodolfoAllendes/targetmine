package org.modmine.web;

/*
 * Copyright (C) 2002-2013 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.tiles.ComponentContext;
import org.apache.struts.tiles.actions.TilesAction;
import org.intermine.api.InterMineAPI;
import org.intermine.objectstore.ObjectStore;
import org.intermine.web.logic.session.SessionMethods;
import org.modmine.web.GBrowseParser.GBrowseTrack;

/**
 * Controller for projectSummary.jsp
 *
 * @author Sergio
 *
 */
public class ProjectsSummaryController extends TilesAction
{


    /**
     * {@inheritDoc}
     */
    public ActionForward execute(ComponentContext context,
            ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response)
        throws Exception {
        try {
            final InterMineAPI im = SessionMethods.getInterMineAPI(request.getSession());
            ObjectStore os = im.getObjectStore();

            Map<String, List<DisplayExperiment>> experiments =
                MetadataCache.getProjectExperiments(os);
            request.setAttribute("experiments", experiments);

            Map<String, List<GBrowseTrack>> tracks = MetadataCache.getExperimentGBrowseTracks(os);
            request.setAttribute("tracks", tracks);

            //            final ServletContext  = servlet.getServletContext();
            //          Map<String, List<DisplayExperiment>> categories =
            //          CategoryExperiments.getCategoryExperiments(servletContext, os);
            //      request.setAttribute("catExp", categories);


            // using the categories form experiment.category (chado)
            Map<String, List<DisplayExperiment>> categoriesNew =
                MetadataCache.getCategoryExperiments(os);

            request.setAttribute("catExp", categoriesNew);

        } catch (Exception err) {
            err.printStackTrace();
        }
        return null;
    }
}
