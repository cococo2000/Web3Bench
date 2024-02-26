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

public class W14 extends WEB3Procedure {
    private static final Logger LOG = Logger.getLogger(W14.class);

    public String classname = this.getClass().getSimpleName();
    public String classname_note = "/* " + classname + " */ ";
    public String query = ""
            + "insert into token_transfers "
            + "values "
            + "(?, ?, ?, ?, ?, ?, ?)";
    private PreparedStatement query_stmt = null;

    public long run(Connection conn, Random gen, WEB3Worker w, int startNumber, int upperLimit, int numScale,
            String nodeid, boolean isExplainAnalyze) throws SQLException {
        boolean debug = LOG.isDebugEnabled();
        boolean trace = LOG.isTraceEnabled();

        // Prepare statement
        SQLStmt query_stmtSQL = new SQLStmt(
                classname_note + (isExplainAnalyze ? SQL_EXPLAIN_ANALYZE : "") + query);
        // Create statement and set parameters
        query_stmt = this.getPreparedStatement(conn, query_stmtSQL);

        String token_address = WEB3Util
                .convertToTokenAddressString(
                        WEB3Util.randomNumber(1, WEB3Config.configTokenCount, gen));
        String from_address = WEB3Util
                .convertToAddressString(WEB3Util.randomNumber(1, WEB3Config.configAccountsCount, gen));
        String to_address = WEB3Util
                .convertToAddressString(WEB3Util.randomNumber(1, WEB3Config.configAccountsCount, gen));
        double value = (double) WEB3Util.randomNumber(0, 1000000, gen);
        int transaction_hash_number = WEB3Util.randomNumber(1, numScale * WEB3Config.configTransactionsCount,
                gen);
        // make sure the startNumber is even, to avoid foreign key conflicts with W6
        transaction_hash_number = transaction_hash_number - (transaction_hash_number % 2);
        String transaction_hash = WEB3Util.convertToTxnHashString(transaction_hash_number);
        long block_number = WEB3Util.randomNumber(1, numScale * WEB3Config.configBlocksCount, gen);
        long next_block_number = block_number + 1;

        int idx = 1;
        query_stmt.setString(idx++, token_address);
        query_stmt.setString(idx++, from_address);
        query_stmt.setString(idx++, to_address);
        query_stmt.setDouble(idx++, value);
        query_stmt.setString(idx++, transaction_hash);
        query_stmt.setLong(idx++, block_number);
        query_stmt.setLong(idx++, next_block_number);

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
