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

public class Blocks {
    public long timestamp;
    public long number;
    public String hash;
    public String parent_hash;
    public String nonce;
    public String sha3_uncles;
    public String transactions_root;
    public String state_root;
    public String receipts_root;
    public String miner;
    public double difficulty;
    public double total_difficulty;
    public long size;
    public String extra_data;
    public long gas_limit;
    public long gas_used;
    public long transaction_count;
    public long base_fee_per_gas;

    @Override
    public String toString() {
        return ("\n******************* Blocks **********************"
                + "\n*       timestamp = " + timestamp
                + "\n*           number = " + number
                + "\n*             hash = " + hash
                + "\n*       parent_hash = " + parent_hash
                + "\n*            nonce = " + nonce
                + "\n*      sha3_uncles = " + sha3_uncles
                + "\n*transactions_root = " + transactions_root
                + "\n*       state_root = " + state_root
                + "\n*     receipts_root = " + receipts_root
                + "\n*           miner = " + miner
                + "\n*      difficulty = " + difficulty
                + "\n* total_difficulty = " + total_difficulty
                + "\n*            size = " + size
                + "\n*      extra_data = " + extra_data
                + "\n*       gas_limit = " + gas_limit
                + "\n*        gas_used = " + gas_used
                + "\n* transaction_count = " + transaction_count
                + "\n* base_fee_per_gas = " + base_fee_per_gas
                + "\n**********************************************");
    }
}
