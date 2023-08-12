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

public class R33 extends WEB3Procedure {

    private static final Logger LOG = Logger.getLogger(R33.class);

    // Find the number of unique senders (from\_address) in transactions 
    public SQLStmt query_stmtSQL = new SQLStmt(
            "select "
                    +     "count(distinct from_address) "
                    + "from "
                    +     "transactions "
    );
    private PreparedStatement query_stmt = null;

    public ResultSet run(Connection conn, Random gen,  WEB3Worker w, int startNumber, int upperLimit, int numScale) throws SQLException {
        boolean trace = LOG.isTraceEnabled();
        
        // initializing prepared statements
        query_stmt = this.getPreparedStatement(conn, query_stmtSQL);
        if (trace) LOG.trace("query_stmt R33 START");
        ResultSet rs = query_stmt.executeQuery();
        if (trace) LOG.trace("query_stmt R33 END");
        
        if (trace) {
            int rs_count = 0;
            if (!rs.next()) {
                String msg = String.format("Failed to execute query_stmt R33");
                if (trace)
                    LOG.warn(msg);
                // throw new RuntimeException(msg);
            } else {
                rs_count = rs.getInt(1);
                // commit the transaction
                conn.commit();
            }
            LOG.info(query_stmt.toString());
            LOG.info("R33: count = " + rs_count);
        }

        rs.close();

        return null;
    }
}


