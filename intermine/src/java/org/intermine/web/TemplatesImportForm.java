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

import javax.servlet.http.HttpServletRequest;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.validator.ValidatorForm;

/**
 * Form bean representing template import form.
 *
 * @author  Thomas Riley
 */
public class TemplatesImportForm extends ValidatorForm
{
    private String xml;
    private boolean overwriting = false;
    
    /**
     * Creates a new instance of FeedbackForm.
     */
    public TemplatesImportForm() {
        reset();
    }
    
    /**
     * Get the xml.
     * @return templates in xml format
     */
    public String getXml() {
        return xml;
    }

    /**
     * Set the xml.
     * @param xml templates in xml format
     */
    public void setXml(String xml) {
        this.xml = xml;
    }
    
    /**
     * Get the overwrite flag.
     * @return  true to overwrite existing template, false to add
     */
    public boolean isOverwriting() {
        return overwriting;
    }
    
    /**
     * Set the overwriting flag.
     * @param overwriting true to overwrite existing templates, false to add
     */
    public void setOverwriting(boolean overwriting) {
        this.overwriting = overwriting;
    }
    
    /**
     * @see ActionForm#reset
     */
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        super.reset(mapping, request);
        reset();
    }
    
    /**
     * Reset the form.
     */
    protected void reset() {
        xml = "";
        overwriting = false;
    }

    /**
     * Call inherited method then check whether xml is valid.
     *
     * @see ValidatorForm#validate
     */
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = super.validate(mapping, request);
        if (errors != null && errors.size() > 0) {
            return errors;
        }
        try {
           TemplateHelper.xmlToTemplateMap(getXml());
        } catch (Exception err) {
            if (errors == null) {
                errors = new ActionErrors();
            }
            errors.add(ActionErrors.GLOBAL_MESSAGE,
                        new ActionError("errors.badtemplatexml", err.getMessage()));
        }
        return errors;
    }
}
