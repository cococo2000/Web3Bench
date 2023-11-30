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

import java.util.ArrayList;
import java.util.List;

public class W4 extends WEB3Procedure {

    private static final Logger LOG = Logger.getLogger(W4.class);

    public SQLStmt query_stmtSQL = new SQLStmt(
            "update transactions "
                    + "set gas_price = ? "
                    + "where hash = ? ");

    private PreparedStatement query_stmt = null;

    public ResultSet run(Connection conn, Random gen, WEB3Worker w, int startNumber, int upperLimit, int numScale,
            String nodeid) throws SQLException {
        boolean trace = LOG.isTraceEnabled();

        // initializing all prepared statements
        query_stmt = this.getPreparedStatement(conn, query_stmtSQL);

        long gas_price = WEB3Util.randomNumber(1000, 10000000, gen);
        String hash = WEB3Util
                .convertToTxnHashString(WEB3Util.randomNumber(1, WEB3Config.configTransactionsCount * numScale, gen));

        query_stmt.setLong(1, gas_price);
        query_stmt.setString(2, hash);
        if (trace)
            LOG.trace("query_stmt W4 UpdateQuery1 START");
        int affectedRows = query_stmt.executeUpdate();
        conn.commit();
        if (trace)
            LOG.trace("query_stmt W4 UpdateQuery1 END");

        if (LOG.isDebugEnabled()) {
            LOG.debug(queryToString(query_stmt));
            LOG.debug("W4 UpdateQuery1: " + affectedRows + " rows affected");
        }

        return null;
    }
}
