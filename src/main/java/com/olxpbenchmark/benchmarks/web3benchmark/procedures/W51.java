/*
 * Copyright 2023 by Web3Bench Project
 * This work was based on the OLxPBench Project

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *  http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.

 */

package com.olxpbenchmark.benchmarks.web3benchmark.procedures;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.olxpbenchmark.api.SQLStmt;
import com.olxpbenchmark.benchmarks.web3benchmark.WEB3Util;
import com.olxpbenchmark.benchmarks.web3benchmark.WEB3Worker;

public class W51 extends WEB3Procedure {
    private static final Logger LOG = LoggerFactory.getLogger(W51.class);

    public String classname = this.getClass().getSimpleName();
    public String classname_note = "/* " + classname + " */ ";
    public String query = ""
            + "update token_transfers "
            + "set value = ? "
            + "where to_address = from_address ";
    private PreparedStatement query_stmt = null;

    public long run(Connection conn, Random gen, WEB3Worker w, int startNumber, int upperLimit, int numScale,
            String nodeid, boolean isExplainAnalyze) throws SQLException {
        boolean debug = LOG.isDebugEnabled();
        boolean trace = LOG.isTraceEnabled();

        // Prepare statement
        SQLStmt query_stmtSQL = new SQLStmt(
                classname_note + (isExplainAnalyze ? SQL_EXPLAIN_ANALYZE : "") + query);
        // Parameters
        double value = (double) WEB3Util.randomNumber(0, 1000000, gen);
        // Create statement and set parameters
        query_stmt = this.getPreparedStatement(conn, query_stmtSQL, value);

        // Log query
        if (debug) {
            LOG.debug(queryToString(query_stmt));
        }

        if (trace) {
            LOG.trace("Query" + classname + " START");
        }
        int affectedRows = 0; // Number of rows affected
        ResultSet rs = null;
        // Execute query and commit
        if (isExplainAnalyze) {
            // Use executeQuery for explain analyze
            rs = query_stmt.executeQuery();
        } else {
            // Use executeUpdate for normal query
            affectedRows = query_stmt.executeUpdate();
        }
        conn.commit();
        if (trace) {
            LOG.trace("Query" + classname + " END");
        }

        if (isExplainAnalyze) {
            // If explain analyze, then return the latency
            // Get the latency from the result set
            long latency_ns = getTimeFromRS(rs);
            rs.close();
            return latency_ns;
        } else {
            if (debug) {
                LOG.debug("Affected Rows: " + affectedRows);
            }
        }

        return 0;
    }
}
