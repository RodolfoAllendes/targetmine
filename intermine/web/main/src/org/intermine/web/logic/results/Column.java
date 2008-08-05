package org.intermine.web.logic.results;

/*
 * Copyright (C) 2002-2008 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import org.intermine.path.Path;
import org.intermine.util.TypeUtil;

/**
 * Configuration information for a column in a table
 *
 * @author Andrew Varley
 */
public class Column
{
    protected int index;
    protected Class type;
    protected boolean selectable = false;
    private Path path;
    protected String name;
    protected String columnId;

    /**
     * @return the columnId
     */
    public String getColumnId() {
        return columnId;
    }

    /**
     * @param columnId the columnId to set
     */
    public void setColumnId(String columnId) {
        this.columnId = columnId;
    }

    /**
     * Constructor that takes a Path object.
     *
     * @param path a Path object
     * @param name an optional, more human readable description of the Column that will be
     * returned by getName() - can be null in which case the column name (given by
     * path.toStringNoConstraints()) will be used
     * @param index the number of the column
     * @param type the type of the column (ClassDescriptor or Class)
     */
    public Column(Path path, String name, int index, Class type) {
        this.path = path;
        this.name = name;
        this.index = index;
        this.type = type;
        setColumnId(path.toString().substring(0, path.toString().lastIndexOf(".")) + "_"
                    + TypeUtil.unqualifiedName(type.getName()));
    }

    /**
     * Constructor that takes a Path object.  The human readable name for getName() will be
     * generated from the path.
     *
     * @param path a Path object
     * @param index the number of the column
     * @param type the type of the column (ClassDescriptor or Class)
     */
    public Column(Path path, int index, Class type) {
        this.path = path;
        this.name = path.toStringNoConstraints();
        this.index = index;
        this.type = type;
        setColumnId(path.toString().substring(0, path.toString().lastIndexOf(".")) + "_"
                    + TypeUtil.unqualifiedName(type.getName()));
    }

    /**
     * Constructor that takes a String name.
     *
     * @param name a column name
     * @param index the number of the column
     * @param type the type of the column (ClassDescriptor or Class)
     */
    public Column(String name, int index, Class type) {
        this.name = name;
        this.index = index;
        this.type = type;
        this.path = null;
        setColumnId(name + "_" + TypeUtil.unqualifiedName(type.getName()));
    }

    /**
     * Gets the value of selectable
     * @return a boolean
     */
    public boolean isSelectable() {
        return selectable;
    }

    /**
     * Sets the value of selectable
     * @param selectable value to assign to selectable
     */
    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    /**
     * Gets the value of index
     *
     * @return the value of index
     */
    public int getIndex()  {
        return index;
    }

    /**
     * Sets the value of index
     *
     * @param index value to assign to index
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Return the type of this Column
     * @return a Class or a FieldDescriptor
     */
    public Class getType() {
        return type;
    }

    /**
     * Get the column type unqualified as a String
     * @return the column type
     */
    public String getTypeClsString() {
        return TypeUtil.unqualifiedName(type.getName());
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object other) {
        if (other instanceof Column) {
            return getName().equals(((Column) other).getName());
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return getName().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "[Column " + getName() + "]";
    }

    /**
     * Get the Path set by setPath().
     * @return the Path
     */
    public Path getPath() {
        return path;
    }

    /**
     * Get the name (title) of the column.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }
}
