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

import java.util.ArrayList;
import java.util.List;

public class R23 extends WEB3Procedure {

    private static final Logger LOG = Logger.getLogger(R23.class);

    // top N with small N on full table scan
    public SQLStmt query_stmtSQL1 = new SQLStmt(
            "select "
                + "* "
                + "from token_transfers "
                + "where from_address = ? "
                + "order by block_number desc limit 5 "
    );
    private PreparedStatement query_stmt1 = null;

    public ResultSet run(Connection conn, Random gen,  WEB3Worker w, int startNumber, int upperLimit, int numScale) throws SQLException {
        boolean trace = LOG.isTraceEnabled();

        // initializing all prepared statements
        query_stmt1 = this.getPreparedStatement(conn, query_stmtSQL1);
        
        String from_address = WEB3Util.convertToAddressString(WEB3Util.randomNumber(1, WEB3Config.configAccountsCount, gen));
        
        query_stmt1.setString(1, from_address);
        if (trace) LOG.trace("query_stmt R23 START");
        ResultSet rs = query_stmt1.executeQuery();
        if (trace) LOG.trace("query_stmt R23 END");
        
        if (trace) {
            String rs_from_address = null;
            if (!rs.next()) {
                String msg = String.format(
                        "Failed to get token_transfers [from_address = %s] order by block_number desc limit 5",
                        from_address);
                if (trace)
                    LOG.warn(msg);
                // throw new RuntimeException(msg);
            } else {
                rs_from_address = rs.getString("from_address");
                // commit the transaction
                conn.commit();
            }

            LOG.info(query_stmt1.toString());
            LOG.info("R23: from_address = " + rs_from_address);
            rs.close();
        }

        return null;
    }
}


