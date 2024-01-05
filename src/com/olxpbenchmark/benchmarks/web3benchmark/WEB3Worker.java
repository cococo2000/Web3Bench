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

package com.olxpbenchmark.benchmarks.web3benchmark;

import com.olxpbenchmark.api.Procedure.UserAbortException;
import com.olxpbenchmark.benchmarks.web3benchmark.procedures.WEB3Procedure;
import com.olxpbenchmark.api.TransactionType;
import com.olxpbenchmark.api.Worker;
import com.olxpbenchmark.types.TransactionStatus;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.Random;

import com.olxpbenchmark.WorkloadConfiguration;
import com.olxpbenchmark.distributions.CounterGenerator;
import com.olxpbenchmark.distributions.ZipfianGenerator;
import com.olxpbenchmark.util.RandomGenerator;

public class WEB3Worker extends Worker<WEB3Benchmark> {

    private static final Logger LOG = Logger.getLogger(WEB3Worker.class);

    private final Random gen = new Random();
    private static CounterGenerator startRecord;

    String distribution;

    int numScale = 0;

    double lambda = 100.0;

    int startNumber = 0;

    int delta = 0;

    public WorkloadConfiguration workConf;

    public WEB3Worker(WEB3Benchmark benchmarkModule, int id, String distri, int numScale, int startNum,
            WorkloadConfiguration workConf)
            throws SQLException {
        super(benchmarkModule, id);
        // zipf = new ZipfianGenerator(100);
        distribution = distri;
        this.numScale = numScale;
        this.startNumber = startNum;
        startRecord = new CounterGenerator(this.startNumber);
        this.workConf = workConf;
    }

    /**
     * Executes a single TPCC transaction of type transactionType.
     */
    @Override
    protected long executeWork(TransactionType nextTransaction) throws UserAbortException, SQLException {
        long latency_ns = 0;
        try {
            if (workConf.getGapTime() != 0) {
                // The upper limit of read-only queries after inserting ops.
                long current = System.currentTimeMillis();
                long interval = current - workConf.getOriginalTime();
                long divisor = workConf.getGapTime();
                int delta = 0;
                int summary = workConf.getSummaryNumber();
                delta = (int) (interval / (divisor * 1000));
                if (delta == 1) {
                    workConf.setOriginalTime(System.currentTimeMillis());
                    workConf.setSummaryNumber(++summary);
                }
            }

            int summaryNumber = workConf.getSummaryNumber();
            int insertRatio = workConf.getInsertRatio();
            int deltaNumber = ((int) (workConf.getGapTime())) * (workConf.getRate()) * summaryNumber * insertRatio
                    / 100;
            int upperLimit = workConf.getStartNum() + deltaNumber - 1;

            int startNumber = startRecord.nextInt();
            String nodeid = workConf.getNodeId();

            WEB3Procedure proc = (WEB3Procedure) this.getProcedure(nextTransaction.getProcedureClass());
            if (distribution.equals("rand")) {
                latency_ns = proc.run(conn, gen, this, startNumber, 0, numScale, nodeid);
            }
            // else if (distribution.equals("zipf")) {
            // proc.run(conn, numScale, this, startNumber, 0);
            // }
            // else if (distribution.equals("poisson")) {
            // proc.run(conn, this, startNumber, 0, numScale, "poisson");
            // }
            else if (distribution.equals("iRand")) {
                latency_ns = proc.run(conn, gen, this, startNumber, upperLimit, numScale, nodeid);
            }
            // else if (distribution.equals("iZipf")) {
            // proc.run(conn, numScale, this, startNumber, upperLimit);
            // }
            // else if (distribution.equals("iPoisson")){
            // proc.run(conn, this, startNumber, upperLimit, numScale, "iPoisson");
            // }
            else {
                LOG.error("The INVALID distribution?!");
                System.exit(-1);
            }
        } catch (ClassCastException ex) {
            // fail gracefully
            LOG.error("We have been invoked with an INVALID transactionType?!");
            throw new RuntimeException("Bad transaction type = " + nextTransaction);
        }
        conn.commit();
        // return (TransactionStatus.SUCCESS);
        // Return the latency of the transaction and add TransactionStatus.SUCCESS(range
        // from 0 to 4) to the lowest of the number of the latency.
        return latency_ns + TransactionStatus.SUCCESS.ordinal();
    }
}
