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
import com.olxpbenchmark.benchmarks.web3benchmark.WEB3Config;
import com.olxpbenchmark.benchmarks.web3benchmark.WEB3Util;
import com.olxpbenchmark.benchmarks.web3benchmark.WEB3Worker;

public class W13 extends WEB3Procedure {
    private static final Logger LOG = LoggerFactory.getLogger(W13.class);

    public String classname = this.getClass().getSimpleName();
    public String classname_note = "/* " + classname + " */ ";
    public String query = ""
            + "insert into transactions "
            + "values "
            + "(?, ?, ?, ?, ?,"
            + " ?, ?, ?, ?, ?,"
            + " ?, ?, ?, ?, ?,"
            + " ?, ?, ?, ?, ?)";
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

        String hash = WEB3Util.convertToTxnHashString(startNumber, nodeid + "-W13");
        long nonce = WEB3Util.randomNumber(0, 100, gen);
        long block_number = WEB3Util.randomNumber(1, numScale * WEB3Config.configBlocksCount, gen);
        String block_hash = WEB3Util.convertToBlockHashString(block_number);
        long transaction_index = WEB3Util.randomNumber(0, 10000, gen);
        String from_address = WEB3Util
                .convertToAddressString(WEB3Util.randomNumber(1, WEB3Config.configAccountsCount, gen));
        String to_address = WEB3Util
                .convertToAddressString(WEB3Util.randomNumber(1, WEB3Config.configAccountsCount, gen));
        double value = (double) WEB3Util.randomNumber(0, 1000000, gen);
        long gas = WEB3Util.randomNumber(100, 1000000, gen);
        long gas_price = WEB3Util.randomNumber(1000, 10000000, gen);
        String input = WEB3Util.randomStr(WEB3Util.randomNumber(1, 1000, gen));
        long receipt_cumulative_gas_used = WEB3Util.randomNumber(100, 1000000, gen);
        long receipt_gas_used = WEB3Util.randomNumber(10, gas, gen);
        String receipt_contract_address = WEB3Util.convertToContractAddressString(
                WEB3Util.randomNumber(1, numScale * WEB3Config.configContractsCount, gen));
        String receipt_root = WEB3Util.randomHashString();
        long receipt_status = WEB3Util.randomNumber(0, 100, gen);
        long block_timestamp = WEB3Util.getTimestamp(block_number);
        long max_fee_per_gas = WEB3Util.randomNumber(100, 10000, gen);
        long max_priority_fee_per_gas = WEB3Util.randomNumber(100, 10000, gen);
        long transaction_type = WEB3Util.randomNumber(0, 100000, gen);

        int idx = 1;
        query_stmt.setString(idx++, hash);
        query_stmt.setLong(idx++, nonce);
        query_stmt.setString(idx++, block_hash);
        query_stmt.setLong(idx++, block_number);
        query_stmt.setLong(idx++, transaction_index);
        query_stmt.setString(idx++, from_address);
        query_stmt.setString(idx++, to_address);
        query_stmt.setDouble(idx++, value);
        query_stmt.setLong(idx++, gas);
        query_stmt.setLong(idx++, gas_price);
        query_stmt.setString(idx++, input);
        query_stmt.setLong(idx++, receipt_cumulative_gas_used);
        query_stmt.setLong(idx++, receipt_gas_used);
        query_stmt.setString(idx++, receipt_contract_address);
        query_stmt.setString(idx++, receipt_root);
        query_stmt.setLong(idx++, receipt_status);
        query_stmt.setLong(idx++, block_timestamp);
        query_stmt.setLong(idx++, max_fee_per_gas);
        query_stmt.setLong(idx++, max_priority_fee_per_gas);
        query_stmt.setLong(idx++, transaction_type);

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
