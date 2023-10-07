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

public class R35 extends WEB3Procedure {

    private static final Logger LOG = Logger.getLogger(R35.class);
    
    // Total count of token transfers for a specific sender and token transfers for recipients who are also senders in other transactions. 
    public SQLStmt query_stmtSQL = new SQLStmt(
            "select "
                    +   "count(*) as count "
                    + "from "
                    + "( "
                    +   "select * "
                    +   "from "
                    +       "token_transfers t "
                    +   "where "
                    +       "from_address = ? "
                    +   "union all "
                    +   "select t2.* "
                    +   "from "
                    +       "token_transfers t2 "
                    + "inner join token_transfers t on t2.from_address = t.to_address "
                    + "and t.value < t2.value "
                    + ") as temp "
    );
    private PreparedStatement query_stmt = null;

    public ResultSet run(Connection conn, Random gen,  WEB3Worker w, int startNumber, int upperLimit, int numScale, String nodeid) throws SQLException {
        boolean trace = LOG.isTraceEnabled();
        // R35:
        // initializing prepared statements
        query_stmt = this.getPreparedStatement(conn, query_stmtSQL);

        String from_address = WEB3Util.convertToAddressString(WEB3Util.randomNumber(1, WEB3Config.configAccountsCount, gen));

        query_stmt.setString(1, from_address);

        if (trace)
            LOG.trace("query_stmt R35 START");
        ResultSet rs = query_stmt.executeQuery();
        if (trace)
            LOG.trace("query_stmt R35 END");

        if (trace) {
            int rs_count = 0;
            if (!rs.next()) {
                String msg = String.format("Failed to execute query_stmt R35");
                if (trace)
                    LOG.warn(msg);
                // throw new RuntimeException(msg);
            } else {
                rs_count = rs.getInt(1);
                // commit the transaction
                conn.commit();
            }

            LOG.info(query_stmt.toString());
            LOG.info("R35: count = " + rs_count);
        }
        
        rs.close();

        return null;
    }
}


