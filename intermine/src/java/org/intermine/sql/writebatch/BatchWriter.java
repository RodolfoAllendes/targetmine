package org.intermine.sql.writebatch;

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
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * An interface representing an object that can flush a TableBatch to an SQL server Connection.
 *
 * @author Matthew Wakeling
 */
public interface BatchWriter
{
    /**
     * Performs all the processing for flushing a few tables to the connection, and returns a List
     * of jobs to do the actual flushing.
     *
     * @param con the SQL connection
     * @param tables a Map from table name to Table
     * @return a List of jobs to run to actually do the flushing
     * @throws SQLException if there is an underlying DB problem
     */
    public List write(Connection con, Map tables) throws SQLException;
}
