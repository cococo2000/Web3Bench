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

import com.olxpbenchmark.api.SQLStmt;
import com.olxpbenchmark.benchmarks.web3benchmark.WEB3Config;
import com.olxpbenchmark.benchmarks.web3benchmark.WEB3Constants;
import com.olxpbenchmark.benchmarks.web3benchmark.WEB3Util;
import com.olxpbenchmark.benchmarks.web3benchmark.WEB3Worker;
import com.olxpbenchmark.benchmarks.web3benchmark.procedures.WEB3Procedure;
import com.olxpbenchmark.distributions.ZipfianGenerator;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

public class R31 extends WEB3Procedure {
    private static final Logger LOG = Logger.getLogger(R31.class);

    public String classname = this.getClass().getSimpleName();
    public String classname_note = "/* " + classname + " */ ";
    // For a specific person, find transactions where this person is either a sender
    // or receiver. Limit the result by the most recent timestamp.
    public String query = ""
            + "select * "
            + "from transactions "
            + "where from_address = ? or to_address = ? "
            + "order by block_timestamp desc "
            + "limit 10";
    private PreparedStatement query_stmt = null;

    public long run(Connection conn, Random gen, WEB3Worker w, int startNumber, int upperLimit, int numScale,
            String nodeid, boolean isExplainAnalyze) throws SQLException {
        boolean debug = LOG.isDebugEnabled();
        boolean trace = LOG.isTraceEnabled();

        // Prepare statement
        SQLStmt query_stmtSQL = new SQLStmt(
                classname_note + (isExplainAnalyze ? SQL_EXPLAIN_ANALYZE : "") + query);
        // Parameters
        String from_address = WEB3Util
                .convertToAddressString(WEB3Util.randomNumber(1, WEB3Config.configAccountsCount, gen));
        String to_address = from_address; // one person
        // Create statement and set parameters
        query_stmt = this.getPreparedStatement(conn, query_stmtSQL, from_address, to_address);

        // Log query
        if (debug) {
            LOG.debug(queryToString(query_stmt));
        }

        if (trace) {
            LOG.trace("Query" + classname + " START");
        }
        // Execute query and commit
        ResultSet rs = query_stmt.executeQuery();
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
        }

        // Log result
        if (trace) {
            LOG.trace(resultSetToString(rs));
        }

        // Close result set
        rs.close();
        return 0;
    }
}
