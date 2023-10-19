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


package com.olxpbenchmark.benchmarks.web3benchmark.pojo;

public class Token_transfers {
    public String token_address;
    public String from_address;
    public String to_address;
    public double value;
    public String transaction_hash;
    public long block_number;
    public long next_block_number;

    @Override
    public String toString() {
        return ("\n**************** TokenTransfers ****************"
                + "\n*   token_address = " + token_address
                + "\n*    from_address = " + from_address
                + "\n*      to_address = " + to_address
                + "\n*           value = " + value
                + "\n* transaction_hash = " + transaction_hash
                + "\n*    block_number = " + block_number
                + "\n* next_block_number = " + next_block_number
                + "\n**********************************************");
    }
}
