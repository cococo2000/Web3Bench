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

import com.olxpbenchmark.api.BenchmarkModule;
import com.olxpbenchmark.api.Loader;
import com.olxpbenchmark.api.Worker;

import com.olxpbenchmark.WorkloadConfiguration;
import com.olxpbenchmark.benchmarks.web3benchmark.WEB3Config;
import com.olxpbenchmark.benchmarks.web3benchmark.WEB3Worker;
import com.olxpbenchmark.benchmarks.web3benchmark.procedures.WEB3Procedure;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WEB3Benchmark extends BenchmarkModule {
    private static final Logger LOG = Logger.getLogger(WEB3Benchmark.class);

    public WEB3Benchmark(WorkloadConfiguration workConf) {
        super("web3benchmark", workConf, true);
    }

    @Override
    protected Package getProcedurePackageImpl() {
        return (WEB3Procedure.class.getPackage());
    }

    /**
     * @param Bool
     */
    @Override
    protected List<Worker<? extends BenchmarkModule>> makeWorkersImpl(boolean verbose) throws IOException {
        ArrayList<Worker<? extends BenchmarkModule>> workers = new ArrayList<Worker<? extends BenchmarkModule>>();

        try {
            //int numTerminals = workConf.getTerminals();
            //String distri = workConf.getDistri();

            List<WEB3Worker> terminals = createTerminals();
            workers.addAll(terminals);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return workers;
    }

    @Override
    protected Loader<WEB3Benchmark> makeLoaderImpl() throws SQLException {
        return new WEB3Loader(this);
    }

    protected ArrayList<WEB3Worker> createTerminals() throws SQLException {

        WEB3Worker[] terminals = new WEB3Worker[workConf.getTerminals()];

        int numTerminals = workConf.getTerminals();

        String generator = workConf.getDistri();

        int numScale = (int) workConf.getScaleFactor();
        if (numScale <= 0) {
            numScale = 1;
        }

        int startNum = workConf.getStartNum();
        //System.out.println("startnum= " + startNum);
        if (startNum <= 0 ) {
            startNum = 1;
        }


        int workerId = 0;
            for (int terminalId = 0; terminalId < numTerminals; terminalId++) {
                //LOG.info("numScale = " + numScale);
                //LOG.info("workerID = " + workerId);
                //LOG.info("terminalID = " + terminalId);
                WEB3Worker terminal = new WEB3Worker(this, workerId++, generator, numScale, startNum, workConf);
                terminals[terminalId] = terminal;
            }
        assert terminals[terminals.length - 1] != null;

        ArrayList<WEB3Worker> ret = new ArrayList<>(Arrays.asList(terminals));
        return ret;
    }

    /**
     * Hack to support postgres-specific timestamps
     * @param time
     * @return
     */
    public Timestamp getTimestamp(long time) {
        Timestamp timestamp;

        // 2020-03-03: I am no longer aware of any DBMS that needs a specialized data type for timestamps.
        timestamp = new java.sql.Timestamp(time);

        return (timestamp);
    }

}
