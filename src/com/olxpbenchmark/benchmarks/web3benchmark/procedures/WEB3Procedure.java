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

/*
 * Copyright 2021 OLxPBench
 * This work was based on the OLTPBenchmark Project

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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Random;

import com.olxpbenchmark.api.Procedure;
import com.olxpbenchmark.benchmarks.web3benchmark.WEB3Worker;

public abstract class WEB3Procedure extends Procedure {
    // rand and iRand
    public abstract ResultSet run(Connection conn, Random gen, WEB3Worker w, int startNumber, int upperLimit,
            int numScale, String nodeid) throws SQLException;

    protected String queryToString(PreparedStatement query) {
        return query.toString().split(":")[1].trim();
    }

    protected String resultSetToString(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        StringBuilder resultLog = new StringBuilder();

        // Check if the result set is empty
        if (!resultSet.isBeforeFirst()) {
            resultLog.append("Result set is empty.");
            return resultLog.toString();
        }

        // Print column names in the first row
        StringBuilder headerRow = new StringBuilder("Column Names: ");
        for (int i = 1; i <= columnCount; i++) {
            headerRow.append(metaData.getColumnName(i)).append(", ");
        }
        // Remove the trailing comma and space
        headerRow.setLength(headerRow.length() - 2);

        resultLog.append(headerRow.toString()).append("\n");

        // Print data rows
        while (resultSet.next()) {
            StringBuilder resultRow = new StringBuilder("Result: ");
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                String columnValue = resultSet.getString(i);
                resultRow.append(columnName).append(": ").append(columnValue).append(", ");
            }
            // Remove the trailing comma and space
            resultRow.setLength(resultRow.length() - 2);

            resultLog.append(resultRow.toString()).append("\n");
        }

        return resultLog.toString();
    }

}
