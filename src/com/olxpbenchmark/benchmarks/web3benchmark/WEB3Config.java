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

import java.text.SimpleDateFormat;

public final class WEB3Config {
    public final static int configHashLength = 66;
    public final static int configAddressLength = 42;

    public final static int configCommitCount = 1000; // commit every n records
    public final static int configWhseCount = 1;

    public final static int configBaseTimestamp         = 1438269988; // Jul-30-2015 03:26:13 PM +UTC
    public final static int configTimestampInterval     = 60; // 60 seconds
    public final static int configBlockDifficulty       = 3;
    public final static int configGasLimitLowerBound    = 100000;
    
    public final static int configBlocksCount           = 1000;
    public final static int configContractsCount        = (int) (0.7 * configBlocksCount);
    public final static int configToken_transfersCount  = 18 * configBlocksCount;
    public final static int configTxnPerBlock           = 80;
    public final static int configTransactionsCount     = configTxnPerBlock * configBlocksCount;
    
    public final static int configTokenCount    = 1000; // the number of tokens
    public static int configAccountsCount = 10 * configBlocksCount; // the number of accounts / addresses
}
