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

public class W52 extends WEB3Procedure {

    private static final Logger LOG = Logger.getLogger(W52.class);

    public SQLStmt query_stmtSQL = new SQLStmt(
            "update token_transfers "
                + "set value = value + 1 "
                + "where from_address in "
                + "(select to_address from token_transfers) "
    );

    private PreparedStatement query_stmt = null;

    public ResultSet run(Connection conn, Random gen,  WEB3Worker w, int startNumber, int upperLimit, int numScale, String nodeid) throws SQLException {
        boolean trace = LOG.isTraceEnabled();

        // initializing all prepared statements
        query_stmt = this.getPreparedStatement(conn, query_stmtSQL);

        // set autocommit to true
        conn.setAutoCommit(true);

        if (trace) LOG.trace("query_stmt UpdateQuery3 START");
        query_stmt.executeUpdate();
        if (trace) LOG.trace("query_stmt UpdateQuery3 END");
        
        // commit the transaction
        // conn.commit();

        // reset autocommit to false
        conn.setAutoCommit(false);

        // LOG.info(query_stmt.toString());

        return null;
    }
}


