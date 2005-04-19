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

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Collections;
import java.util.TreeMap;

import org.apache.lucene.store.Directory;

/**
 * Class to represent a user of the webapp
 *
 * @author Mark Woodbridge
 * @author Thomas Riley
 */
public class Profile
{
    protected ProfileManager manager;
    protected String username;
    protected Map savedQueries = new TreeMap();
    protected Map savedBags = new TreeMap();
    protected Map savedTemplates = new TreeMap();
    protected Map categoryTemplates;
    protected Directory templateIndex;
    
    /**
     * Construct a Profile
     * @param manager the manager for this profile
     * @param username the username for this profile
     * @param savedQueries the saved queries for this profile
     * @param savedBags the saved bags for this profile
     * @param savedTemplates the saved templates for this profile
     */
    public Profile(ProfileManager manager,
                   String username,
                   Map savedQueries,
                   Map savedBags,
                   Map savedTemplates) {
        this.manager = manager;
        this.username = username;
        this.savedQueries.putAll(savedQueries);
        this.savedBags.putAll(savedBags);
        this.savedTemplates.putAll(savedTemplates);
        buildTemplateCategories();
    }
    
    /**
     * Get the value of username
     * @return the value of username
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * Get the users saved templates
     * @return saved templates
     */
    public Map getSavedTemplates() {
        return Collections.unmodifiableMap(savedTemplates);
    }

    /**
     * Save a template
     * @param name the template name
     * @param template the template
     */
    public void saveTemplate(String name, TemplateQuery template) {
        savedTemplates.put(name, template);
        if (manager != null) {
            manager.saveProfile(this);
        }
        buildTemplateCategories();
    }
    
    /**
     * Delete a template
     * @param name the template name
     */
    public void deleteTemplate(String name) {
        savedTemplates.remove(name);
        if (manager != null) {
            manager.saveProfile(this);
        }
        buildTemplateCategories();
    }

    /**
     * Get the value of savedQueries
     * @return the value of savedQueries
     */
    public Map getSavedQueries() {
        return Collections.unmodifiableMap(savedQueries);
    }

    /**
     * Save a query
     * @param name the query name
     * @param query the query
     */
    public void saveQuery(String name, PathQuery query) {
        savedQueries.put(name, query);
        if (manager != null) {
            manager.saveProfile(this);
        }
    }

    /**
     * Delete a query
     * @param name the query name
     */
    public void deleteQuery(String name) {
        savedQueries.remove(name);
        if (manager != null) {
            manager.saveProfile(this);
        }
    }

    /**
     * Get the value of savedBags
     * @return the value of savedBags
     */
    public Map getSavedBags() {
        return Collections.unmodifiableMap(savedBags);
    }

    /**
     * Save a bag
     * @param name the bag name
     * @param bag the bag
     */
    public void saveBag(String name, InterMineBag bag) {
        savedBags.put(name, bag);
        if (manager != null) {
            manager.saveProfile(this);
        }
    }

    /**
     * Delete a bag
     * @param name the bag name
     */
    public void deleteBag(String name) {
        savedBags.remove(name);
        if (manager != null) {
            manager.saveProfile(this);
        }
    }
    
    /**
     * Create a map from category name to a list of templates contained
     * within that category.
     */
    private void buildTemplateCategories() {
        categoryTemplates = new LinkedHashMap();
        Iterator iter = savedTemplates.values().iterator();
        while (iter.hasNext()) {
            TemplateQuery template = (TemplateQuery) iter.next();
            List list = (List) categoryTemplates.get(template.getCategory());
            if (list == null) {
                list = new ArrayList();
                categoryTemplates.put(template.getCategory(), list);
            }
            list.add(template);
        }
        
        // We also take this opportunity to index the user's template queries
        templateIndex = TemplateRepository.indexTemplates(savedTemplates, "user");
    }
    
    /**
     * Get a Map from category name to list of templates.
     * @return Map from category name to List of templates
     */
    public Map getCategoryTemplates() {
        return categoryTemplates;
    }
    
    /**
     * Get the users's template index.
     * 
     * @return the user's template index
     */
    public Directory getUserTemplatesIndex() {
        return templateIndex;
    }
}