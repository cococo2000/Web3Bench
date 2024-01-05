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

import org.apache.log4j.Logger;

import com.olxpbenchmark.api.SQLStmt;
import com.olxpbenchmark.benchmarks.web3benchmark.WEB3Util;
import com.olxpbenchmark.benchmarks.web3benchmark.WEB3Worker;

public class W3 extends WEB3Procedure {

    private static final Logger LOG = Logger.getLogger(W3.class);

    // Insert 1000 rows into transactions from a temp table
    public SQLStmt query_stmtSQL = new SQLStmt(
            "explain analyze insert transactions select * from temp_table limit 1000 ");

    private PreparedStatement query_stmt = null;

    public long run(Connection conn, Random gen, WEB3Worker w, int startNumber, int upperLimit, int numScale,
            String nodeid) throws SQLException {
        boolean trace = LOG.isTraceEnabled();

        // initializing all prepared statements
        query_stmt = this.getPreparedStatement(conn, query_stmtSQL);

        if (trace)
            LOG.trace("query_stmt W3 InsertSelect START");
        // int affectedRows = query_stmt.executeUpdate();
        ResultSet rs = query_stmt.executeQuery();
        conn.commit();
        if (trace)
            LOG.trace("query_stmt W3 InsertSelect END");

        // if (LOG.isDebugEnabled()) {
        // LOG.debug(queryToString(query_stmt));
        // LOG.debug("W3 InsertSelect: " + affectedRows + " rows affected");
        // }

        long latency_ns = getTimeFromRS(rs);
        rs.close();
        return latency_ns;
    }
}
