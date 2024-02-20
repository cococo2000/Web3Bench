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
import com.olxpbenchmark.benchmarks.web3benchmark.WEB3Config;
import com.olxpbenchmark.benchmarks.web3benchmark.WEB3Util;
import com.olxpbenchmark.benchmarks.web3benchmark.WEB3Worker;

public class W6 extends WEB3Procedure {

    private static final Logger LOG = Logger.getLogger(W6.class);

    // Single record deletes for the transaction table.
    public SQLStmt query_stmtSQL = new SQLStmt(
            "/* W6 */ "
                    + "explain analyze "
                    + "delete from transactions "
                    + "where hash = ? ");

    private PreparedStatement query_stmt = null;

    public long run(Connection conn, Random gen, WEB3Worker w, int startNumber, int upperLimit, int numScale,
            String nodeid) throws SQLException {
        boolean trace = LOG.isTraceEnabled();

        // initializing all prepared statements
        query_stmt = this.getPreparedStatement(conn, query_stmtSQL);

        // make sure the startNumber is odd, to avoid foreign key conflicts with W14
        startNumber = startNumber - (startNumber % 2) + 1;
        String hash = WEB3Util.convertToTxnHashString(startNumber % (WEB3Config.configTransactionsCount * numScale));

        // Setting the parameters for the query
        query_stmt.setString(1, hash);
        if (LOG.isDebugEnabled()) {
            LOG.debug(queryToString(query_stmt));
        }
        if (trace)
            LOG.trace("query_stmt W6 single record deletes for the transaction table START");
        // int affectedRows = query_stmt.executeUpdate();
        ResultSet rs = query_stmt.executeQuery();
        conn.commit();
        if (trace)
            LOG.trace("query_stmt W6 single record deletes for the transaction table END");

        long latency_ns = getTimeFromRS(rs);
        rs.close();
        return latency_ns;
    }
}
