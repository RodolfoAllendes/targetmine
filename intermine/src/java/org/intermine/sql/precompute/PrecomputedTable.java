package org.intermine.sql.precompute;

/*
 * Copyright (C) 2002-2004 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.intermine.sql.query.AbstractTable;
import org.intermine.sql.query.AbstractValue;
import org.intermine.sql.query.Field;
import org.intermine.sql.query.Query;
import org.intermine.sql.query.SelectValue;
import org.intermine.sql.query.SQLStringable;
import org.intermine.sql.query.Table;

import org.apache.log4j.Logger;

/**
 * Represents a Precomputed table in a database. A precomputed table is a materialised SQL query.
 * Note - the query encapsulated in this PrecomputedTable should not be altered.
 *
 * @author Andrew Varley
 */
public class PrecomputedTable implements SQLStringable, Comparable
{
    private static final Logger LOG = Logger.getLogger(PrecomputedTable.class);
    protected Query q;
    protected String name;
    protected Map valueMap;
    protected String orderByField;
    protected String generationSqlString;

    /**
     * Construct a new PrecomputedTable
     *
     * @param q the Query that this PrecomputedTable materialises
     * @param name the name of this PrecomputedTable
     * @param conn a Connection to use to work out if the order by fields are compatible with a
     * unified orderby_field
     */
    public PrecomputedTable(Query q, String name, Connection conn) {
        if (q == null) {
            throw (new NullPointerException("q cannot be null"));
        }
        if (name == null) {
            throw (new NullPointerException("the name of a precomputed table cannot be null"));
        }
        this.q = q;
        this.name = name;
        // Now build the valueMap. Do not alter this Query from now on...
        valueMap = new HashMap();
        Iterator valueIter = q.getSelect().iterator();
        while (valueIter.hasNext()) {
            SelectValue value = (SelectValue) valueIter.next();
            valueMap.put(value.getValue(), value);
        }

        // Now we should work out if we can create an order by field. First, we need to make sure
        // that all the fields in the order by list are integer numbers (that is SMALLINT, INTEGER,
        // and BIGINT).
        boolean useOrderByField = (q.getOrderBy().size() > 1) && (q.getUnion().size() == 1);
        try {
            if (useOrderByField) {
                Iterator orderByIter = q.getOrderBy().iterator();
                while (orderByIter.hasNext() && useOrderByField) {
                    AbstractValue column = (AbstractValue) orderByIter.next();
                    if (valueMap.containsKey(column)) {
                        if (column instanceof Field) {
                            AbstractTable table = ((Field) column).getTable();
                            if (table instanceof Table) {
                                String tableName = ((Table) table).getName().toLowerCase();
                                String columnName = ((Field) column).getName().toLowerCase();
                                ResultSet r = conn.getMetaData().getColumns(null, null, tableName,
                                        columnName);
                                if (r.next()) {
                                    if (tableName.equals(r.getString(3))
                                            && columnName.equals(r.getString(4))) {
                                        int columnType = r.getInt(5);
                                        if (!((columnType == Types.SMALLINT)
                                                    || (columnType == Types.INTEGER)
                                                    || (columnType == Types.BIGINT))) {
                                            useOrderByField = false;
                                            LOG.info("Cannot generate order field for precomputed"
                                                    + " table - column " + column.getSQLString()
                                                    + " is type " + columnType);
                                        }
                                    } else {
                                        useOrderByField = false;
                                        LOG.error("getColumns returned wrong data for column "
                                                + column.getSQLString());
                                    }
                                } else {
                                    useOrderByField = false;
                                    LOG.error("getColumns return no data for column "
                                            + column.getSQLString() + " in table " + tableName);
                                }
                                if (r.next()) {
                                    useOrderByField = false;
                                    LOG.error("getColumns returned too much data for column "
                                            + column.getSQLString());
                                }
                            } else {
                                useOrderByField = false;
                                LOG.info("Cannot generate order field for precomputed table - column "
                                        + column.getSQLString() + " does not belong to a Table");
                            }
                        } else {
                            useOrderByField = false;
                            LOG.info("Cannot generate order field for precomputed table - column "
                                    + column.getSQLString() + " is not a Field");
                        }
                    } else {
                        useOrderByField = false;
                        LOG.info("Cannot generate order field for precomputed table - column "
                                + column.getSQLString() + " is not present in the precomputed"
                                + " table");
                    }
                }
            }
        } catch (SQLException e) {
            useOrderByField = false;
            LOG.error("Caught SQLException while examining order by fields: " + e);
        }

        if (useOrderByField) {
            orderByField = "orderby_field";
            List orderBy = q.getOrderBy();
            StringBuffer extraBuffer = new StringBuffer();
            for (int i = orderBy.size() - 1; i > 0; i--) {
                extraBuffer.append("((" + ((SQLStringable) orderBy.get(orderBy.size() - 1 - i))
                        .getSQLString() + "::numeric) * 1");
                for (int o = 0; o < i; o++) {
                    extraBuffer.append("00000000000000000000");
                }
                extraBuffer.append(") + ");
            }
            extraBuffer.append("(" + ((SQLStringable) orderBy.get(orderBy.size() - 1))
                    .getSQLString() + "::numeric) AS orderby_field");
            generationSqlString = "CREATE TABLE " + name + " AS "
                + q.getSQLStringForPrecomputedTable(extraBuffer.toString());
        } else {
            orderByField = null;
            generationSqlString = "CREATE TABLE " + name + " AS " + q.getSQLString();
        }
    }

    /**
     * Gets the Query that is materialised in this PrecomputedTable
     *
     * @return the Query that this PrecomputedTable materialises
     */
    public Query getQuery() {
        return q;
    }

    /**
     * Gets the name of this PrecomputedTable
     *
     * @return the name of the PrecomputedTable
     */
    public String getName() {
        return name;
    }

    /**
     * Gets a Map from AbstractValue to SelectValue for the Query in this PrecomputedTable.
     *
     * @return the valueMap
     */
    public Map getValueMap() {
        return valueMap;
    }

    /**
     * Get a "CREATE TABLE" SQL statement for this PrecomputedTable.
     *
     * @return this PrecomputedTable as an SQL statement
     */
    public String getSQLString() {
        return generationSqlString;
    }

    /**
     * Returns the name of the order by field, if it exists.
     *
     * @return orderByField
     */
    public String getOrderByField() {
        return orderByField;
    }

    /**
     * Overrides Object.equals().
     *
     * @param obj an Object to compare to
     * @return true if the object is equal
     */
    public boolean equals(Object obj) {
        if (obj instanceof PrecomputedTable) {
            PrecomputedTable objTable = (PrecomputedTable) obj;
            return q.equals(objTable.q) && name.equals(objTable.name);
        }
        return false;
    }

    /**
     * Overrides Object.hashCode().
     *
     * @return an arbitrary integer based on the contents of the Query and the name
     */
    public int hashCode() {
        return (3 * q.hashCode()) + (5 * name.hashCode());
    }

    /**
     * Implements Comparable's method, so we can put PrecomputedTable objects into SortedMaps.
     *
     * @param obj an Object to compare to
     * @return an integer based on the comparison
     * @throws ClassCastException if obj is not a PrecomputedTable
     */
    public int compareTo(Object obj) {
        return name.compareTo(((PrecomputedTable) obj).name);
    }
}
