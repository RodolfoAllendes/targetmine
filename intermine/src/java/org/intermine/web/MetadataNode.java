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

import org.intermine.metadata.Model;

/**
 * Node used in displaying metadata
 * @author Mark Woodbridge
 */
public class MetadataNode extends Node
{
    String button;

    /**
     * Constructor for a root node
     * @param type the root type of this tree
     */
    public MetadataNode(String type) {
        super(type);
        button = " ";
    }

    /**
     * Constructor for a non-root node
     * @param parent the parent node of this node
     * @param fieldName the name of the field that this node represents
     * @param model the model used to resolve paths
     * @param button the button displayed next to this node's name
     */
    public MetadataNode(MetadataNode parent, String fieldName, Model model, String button) {
        super(parent, fieldName, model);
        this.button = button;
    }

    /**
     * Gets the value of button
     *
     * @return the value of button
     */
    public String getButton()  {
        return button;
    }
}