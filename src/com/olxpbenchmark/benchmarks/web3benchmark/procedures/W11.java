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

public class W11 extends WEB3Procedure {

    private static final Logger LOG = Logger.getLogger(W11.class);

    public SQLStmt query_stmtSQL = new SQLStmt(
            "insert into "
                    +  "blocks "
                    + "values "
                    + "(?, ?, ?, ?, ?,"
                    + " ?, ?, ?, ?, ?,"
                    + " ?, ?, ?, ?, ?,"
                    + " ?, ?, ?)"
    );

    private PreparedStatement query_stmt = null;

    public ResultSet run(Connection conn, Random gen,  WEB3Worker w, int startNumber, int upperLimit, int numScale) throws SQLException {
        boolean trace = LOG.isTraceEnabled();

        // initializing all prepared statements
        query_stmt = this.getPreparedStatement(conn, query_stmtSQL);

        long number         = WEB3Util.randomNumber(numScale * WEB3Config.configBlocksCount + 1, 3 * numScale * WEB3Config.configBlocksCount, gen);
        String hash         = WEB3Util.convertToBlockHashString(number);
        String parent_hash  = WEB3Util.convertToBlockHashString(number - 1);
        String nonce        = WEB3Util.randomHexString(42);
        String sha3_uncles       = WEB3Util.randomHashString();
        String transactions_root = WEB3Util.randomHashString();
        String state_root        = WEB3Util.randomHashString();
        String receipts_root     = WEB3Util.randomHashString();
        String miner = WEB3Util.convertToAddressString(WEB3Util.randomNumber(1, WEB3Config.configAccountsCount, gen));
        double difficulty       = WEB3Config.configBlockDifficulty;
        double total_difficulty = number * WEB3Config.configBlockDifficulty;
        long size = WEB3Util.randomNumber(100, 100000, gen);
        String extra_data = WEB3Util.randomStr(WEB3Util.randomNumber(1, 1000, gen));
        long gas_limit = WEB3Util.randomNumber(WEB3Config.configGasLimitLowerBound, 100000000, gen);
        long gas_used = WEB3Util.getGasUsed(number);
        long timestamp = WEB3Util.getGasUsed(number);
        long transaction_count = WEB3Util.getTransactionCount(number);
        long base_fee_per_gas = WEB3Util.randomNumber(100, 100000, gen);

        int idx = 1;
        query_stmt.setLong(idx++, timestamp);
        query_stmt.setLong(idx++, number);
        query_stmt.setString(idx++, hash);
        query_stmt.setString(idx++, parent_hash);
        query_stmt.setString(idx++, nonce);
        query_stmt.setString(idx++, sha3_uncles);
        query_stmt.setString(idx++, transactions_root);
        query_stmt.setString(idx++, state_root);
        query_stmt.setString(idx++, receipts_root);
        query_stmt.setString(idx++, miner);
        query_stmt.setDouble(idx++, difficulty);
        query_stmt.setDouble(idx++, total_difficulty);
        query_stmt.setLong(idx++, size);
        query_stmt.setString(idx++, extra_data);
        query_stmt.setLong(idx++, gas_limit);
        query_stmt.setLong(idx++, gas_used);
        query_stmt.setLong(idx++, transaction_count);
        query_stmt.setLong(idx++, base_fee_per_gas);

        if (trace) LOG.trace("query_stmt W11 InsertBlocks START");
        query_stmt.executeUpdate();
        if (trace) LOG.trace("query_stmt W11 InsertBlocks END");
        
        // commit the transaction
        conn.commit();

        // LOG.info(query_stmt.toString());

        return null;
    }
}


