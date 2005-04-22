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

import java.util.HashMap;

import servletunit.struts.MockStrutsTestCase;

/**
 * Tests for the LoadQueryAction class
 *
 * @author Kim Rutherford
 */
public class LoadQueryActionTest extends MockStrutsTestCase
{
    public LoadQueryActionTest(String arg) {
        super(arg);
    }


    public void tearDown() throws Exception {
         getActionServlet().destroy();
    }

    public void testLoad() throws Exception {
        addRequestParameter("method", "load");
        addRequestParameter("name", "employeesWithOldManagers");
        //necessary to work-round struts test case not invoking our SessionListener
        getSession().setAttribute(Constants.PROFILE,
                                  new Profile(null, null, null, new HashMap(), new HashMap(), new HashMap()));

        setRequestPathInfo("/loadExampleQuery");

        actionPerform();
        verifyNoActionErrors();
        verifyForward("query");

        assertNotNull(getSession().getAttribute(Constants.QUERY));
//         assertEquals("[Employee.name, Employee.age, Employee.department.name, Employee.department.manager.age]",
//                      "" + getSession().getAttribute(Constants.VIEW));
//         assertEquals("{Employee=Employee:Employee [], Employee.department=Employee.department:Department [], Employee.department.manager=Employee.department.manager:Manager [], Employee.department.manager.age=Employee.department.manager.age:int [> 10]}", "" + getSession().getAttribute(Constants.QUERY));
    }
}
