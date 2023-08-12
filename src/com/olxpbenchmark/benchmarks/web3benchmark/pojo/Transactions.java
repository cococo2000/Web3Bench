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

public class Transactions {
    public String hash;
    public long nonce;
    public String block_hash;
    public long block_number;
    public long transaction_index;
    public String from_address;
    public String to_address;
    public double value;
    public long gas;
    public long gas_price;
    public String input;

    public long receipt_cumulative_gas_used;
    public long receipt_gas_used;
    public String receipt_contract_address;
    public String receipt_root;
    public long receipt_status;

    public long block_timestamp;
    public long max_fee_per_gas;
    public long max_priority_fee_per_gas;
    public long transaction_type;

    @Override
    public String toString() {
        return ("\n**************** Transactions ******************"
                + "\n*           hash = " + hash
                + "\n*          nonce = " + nonce
                + "\n*     block_hash = " + block_hash
                + "\n*   block_number = " + block_number
                + "\n*transaction_index = " + transaction_index
                + "\n*  from_address = " + from_address
                + "\n*    to_address = " + to_address
                + "\n*         value = " + value
                + "\n*          gas = " + gas
                + "\n*    gas_price = " + gas_price
                + "\n*        input = " + input
                + "\n*receipt_cumulative_gas_used = " + receipt_cumulative_gas_used
                + "\n*receipt_gas_used = " + receipt_gas_used
                + "\n*receipt_contract_address = " + receipt_contract_address
                + "\n*receipt_root = " + receipt_root
                + "\n*receipt_status = " + receipt_status
                + "\n*block_timestamp = " + block_timestamp
                + "\n* max_fee_per_gas = " + max_fee_per_gas
                + "\n*max_priority_fee_per_gas = " + max_priority_fee_per_gas
                + "\n*transaction_type = " + transaction_type
                + "\n**********************************************");
    }
}

