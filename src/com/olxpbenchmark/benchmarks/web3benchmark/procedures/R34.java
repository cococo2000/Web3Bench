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

public class R34 extends WEB3Procedure {

    private static final Logger LOG = Logger.getLogger(R34.class);
    
    // Find top N senders (from\_address) by total transaction value
    public SQLStmt query_stmtSQL = new SQLStmt(
            "select "
                    + "sum(value) as totalamount, "
                    + "count(value) as transactioncount, "
                    + "from_address as fromaddress "
                    + "from "
                    + "transactions "
                    // + "where "
                    // + "to_address = ? and block_timestamp >= ? and block_timestamp <= ? and value > ? "
                    + "group by from_address "
                    + "order by sum(value) desc limit 10"
    );
    private PreparedStatement query_stmt = null;

    public ResultSet run(Connection conn, Random gen,  WEB3Worker w, int startNumber, int upperLimit, int numScale, String nodeid) throws SQLException {
        boolean trace = LOG.isTraceEnabled();
        
        // initializing prepared statements
        query_stmt = this.getPreparedStatement(conn, query_stmtSQL);

        // String to_address = WEB3Util
        //         .convertToAddressString(WEB3Util.randomNumber(1, WEB3Config.configAccountsCount, gen));
        // // Get the random block_timestamp1 and block_timestamp2
        // long min_timestamp = WEB3Util.getTimestamp(1);
        // long max_timestamp = WEB3Util.getTimestamp(numScale * WEB3Config.configBlocksCount);
        // long block_timestamp1 = WEB3Util.randomNumber(min_timestamp, max_timestamp, gen);
        // long block_timestamp2 = WEB3Util.randomNumber(min_timestamp, max_timestamp, gen);
        // // keep block_timestamp1 < block_timestamp2
        // if (block_timestamp1 > block_timestamp2) {
        //     long tmp = block_timestamp1;
        //     block_timestamp1 = block_timestamp2;
        //     block_timestamp2 = tmp;
        // }
        // double value = (double) (WEB3Util.randomNumber(10, 10000, gen) / 100.0);

        // query_stmt.setString(1, to_address);
        // query_stmt.setLong(2, block_timestamp1);
        // query_stmt.setLong(3, block_timestamp2);
        // query_stmt.setDouble(4, value);
        
        if (trace) LOG.trace("query_stmt R34 START");
        ResultSet rs = query_stmt.executeQuery();
        if (trace) LOG.trace("query_stmt R34 END");

        if (trace) {
            double rs_totalamount = 0;
            int rs_transactioncount = 0;
            String rs_fromaddress = null;
            if (!rs.next()) {
                String msg = String.format("Failed to execute query_stmt R34");
                if (trace)
                    LOG.warn(msg);
                // throw new RuntimeException(msg);
            } else {
                rs_totalamount = rs.getDouble("totalamount");
                rs_transactioncount = rs.getInt("transactioncount");
                rs_fromaddress = rs.getString("fromaddress");
                // commit the transaction
                conn.commit();
            }

            LOG.trace(query_stmt.toString());
            LOG.trace("R34.rs_totalamount = " + rs_totalamount);
            LOG.trace("R34.rs_transactioncount = " + rs_transactioncount);
            LOG.trace("R34.rs_fromaddress = " + rs_fromaddress);
        }

        rs.close();

        return null;
    }
}


