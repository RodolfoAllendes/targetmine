package org.intermine.web;

/*
 * Copyright (C) 2002-2005 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import java.util.HashMap;

/**
 * Action to handle button presses on the main tile
 * @author Mark Woodbridge
 */
public class LoginAction extends InterMineAction
{
    /** 
     * Method called when user has finished updating a constraint
     * @param mapping The ActionMapping used to select this instance
     * @param form The optional ActionForm bean for this request (if any)
     * @param request The HTTP request we are processing
     * @param response The HTTP response we are creating
     * @return an ActionForward object defining where control goes next
     * @exception Exception if the application business logic throws
     *  an exception
     */
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
        throws Exception {
        HttpSession session = request.getSession();
        ServletContext servletContext = session.getServletContext();
        ProfileManager pm = (ProfileManager) servletContext.getAttribute(Constants.PROFILE_MANAGER);
        String superuser = (String) servletContext.getAttribute(Constants.SUPERUSER_ACCOUNT);
        LoginForm lf = (LoginForm) form;
        
        Profile profile;
        if (pm.hasProfile(lf.getUsername())) {
            profile = pm.getProfile(lf.getUsername(), lf.getPassword());
        } else {
            profile = new Profile(pm, lf.getUsername(),
                                  new HashMap(), new HashMap(), new HashMap());
            pm.saveProfile(profile);
            pm.setPassword(lf.getUsername(), lf.getPassword());
        }
        session.setAttribute(Constants.PROFILE, profile);
        
        if (profile.getUsername().equals(superuser)) {
            session.setAttribute(Constants.IS_SUPERUSER, Boolean.TRUE);
        }

        recordMessage(new ActionMessage("login.loggedin", lf.getUsername()), request);

        return mapping.findForward("history");
    }
}
