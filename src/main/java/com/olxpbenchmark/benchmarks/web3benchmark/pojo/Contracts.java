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

public class Contracts {
    public String address;
    public String bytecode;
    public String function_sighashes;
    public boolean is_erc20;
    public boolean is_erc721;
    public long block_number;

    @Override
    public String toString() {
        return ("\n****************** Contracts *******************"
                + "\n*         address = " + address
                + "\n*        bytecode = " + bytecode
                + "\n*function_sighashes = " + function_sighashes
                + "\n*       is_erc20 = " + is_erc20
                + "\n*      is_erc721 = " + is_erc721
                + "\n*   block_number = " + block_number
                + "\n**********************************************");
    }
}
