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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.olxpbenchmark.api.Procedure;
import com.olxpbenchmark.benchmarks.web3benchmark.WEB3Worker;

public abstract class WEB3Procedure extends Procedure {
    // rand and iRand
    public abstract long run(Connection conn, Random gen, WEB3Worker w, int startNumber, int upperLimit,
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

    /*
     * Get the latency(ns) from the resultSet of "explain analyze"
     */
    protected long getTimeFromRS(ResultSet resultSet) throws SQLException {
        StringBuilder resultLog = new StringBuilder();

        // Check if the result set is empty
        if (!resultSet.isBeforeFirst()) {
            resultLog.append("Result set is empty.");
            return -1;
        }

        // Get the "execution info" in the first row
        long latency_ns = -1;
        if (resultSet.next()) {
            String columnValue = resultSet.getString("execution info");
            // System.out.println(columnValue);

            String timeRegex = "time:(.*?), ";
            Pattern pattern = Pattern.compile(timeRegex);
            Matcher matcher = pattern.matcher(columnValue);

            if (matcher.find()) {
                String timeValue = matcher.group(1);
                System.out.println("Extracted time: " + timeValue);
                latency_ns = this.convertToNs(timeValue);
            } else {
                return -1;
            }
        }
        return latency_ns;
    }

    private long convertToNs(String time) {
        if (time.matches("\\d+h\\d+m\\d+\\.?\\d*s")) {
            // Convert hours, minutes, and seconds to nanoseconds
            String[] parts = time.split("[hms]");
            long hours = Long.parseLong(parts[0]);
            long minutes = Long.parseLong(parts[1]);
            double seconds = Double.parseDouble(parts[2]);
            return (hours * 3600 * 1000 + minutes * 60 * 1000 + (long) (seconds * 1000)) * 1_000_000L;
        } else if (time.matches("\\d+m\\d+\\.?\\d*s")) {
            // Convert minutes and seconds to nanoseconds
            String[] parts = time.split("[ms]");
            long minutes = Long.parseLong(parts[0]);
            double seconds = Double.parseDouble(parts[1]);
            return (minutes * 60 * 1000 + (long) (seconds * 1000)) * 1_000_000L;
        } else if (time.matches("\\d+\\.?\\d*\\w+")) {
            // Convert other formats to nanoseconds
            double value = Double.parseDouble(time.replaceAll("[^\\d.]", ""));
            if (time.contains("h")) {
                return (long) (value * 3600 * 1_000_000_000L); // hours to nanoseconds
            } else if (time.contains("ms")) {
                return (long) (value * 1_000_000L); // milliseconds to nanoseconds
            } else if (time.contains("µs")) {
                return (long) (value * 1_000L); // microseconds to nanoseconds
            } else {
                return (long) (value * 1_000_000_000L); // seconds to nanoseconds
            }
        } else if (time.contains("µs")) { // Âµs
            // Convert microseconds to nanoseconds
            double microseconds = Double.parseDouble(time.replaceAll("[^\\d.]", ""));
            return (long) (microseconds * 1_000L);
        } else {
            throw new IllegalArgumentException("Invalid time format: " + time);
        }
    }
}
