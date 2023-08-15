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

import com.olxpbenchmark.api.Loader;
import com.olxpbenchmark.benchmarks.web3benchmark.WEB3Constants;
import com.olxpbenchmark.benchmarks.web3benchmark.pojo.*;
import com.olxpbenchmark.benchmarks.web3benchmark.procedures.*;
import com.olxpbenchmark.catalog.Table;
import com.olxpbenchmark.util.SQLUtil;
import com.olxpbenchmark.util.json.Test;

import org.apache.log4j.Logger;
import org.hibernate.annotations.BatchSize;
import org.hibernate.mapping.Map;
import org.hsqldb.Tokens;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class WEB3Loader extends Loader<WEB3Benchmark> {
    private static final Logger LOG = Logger.getLogger(WEB3Loader.class);

    public WEB3Loader(WEB3Benchmark benchmark) {
        super(benchmark);
        numWarehouses = (int) Math.round(WEB3Config.configWhseCount * this.scaleFactor);
        if (numWarehouses <= 0) {
            //where would be fun in that?
            numWarehouses = 1;
        }
    }

    private int numWarehouses = 0;

    @Override
    public List<LoaderThread> createLoaderThreads() throws SQLException {
        List<LoaderThread> threads = new ArrayList<LoaderThread>();
        int numLoaders = this.workConf.getLoaderThreads();

        // blocks -> coontracts -> transactions -> token_transfers
        final CountDownLatch blocksLatch        = new CountDownLatch(numWarehouses * numLoaders);
        final CountDownLatch contractsLatch     = new CountDownLatch(numWarehouses);
        final CountDownLatch transactionsLatch  = new CountDownLatch(numWarehouses);
        // final CountDownLatch tokentransfersLatch= new CountDownLatch(numWarehouses);

        // blocks
        for (int w = 1; w <= numWarehouses; w++) {
            final int w_id = w;
            LOG.info("Starting to load " + w_id + " blocks");
            int numBlocksPerLoader = WEB3Config.configBlocksCount / numLoaders;
            for (int i = 1; i <= numLoaders; i++) {
                final int loader_id = i;
                int blocksStartInclusive = (loader_id - 1) * numBlocksPerLoader + 1;
                int blocksEndInclusive = (loader_id == numLoaders) ? WEB3Config.configBlocksCount
                        : blocksStartInclusive + numBlocksPerLoader - 1;
                LoaderThread t = new LoaderThread() {
                    @Override
                    public void load(Connection conn) throws SQLException {
                        LOG.debug("Starting to load " + w_id + " blocks" + " from " + blocksStartInclusive + " to "
                                + blocksEndInclusive + " with the " + loader_id + " loader");
                        loadBlocks(conn, w_id, blocksStartInclusive, blocksEndInclusive);
                        LOG.debug("Ending to load " + w_id + " blocks" + " from " + blocksStartInclusive + " to "
                                + blocksEndInclusive + " with the " + loader_id + " loader");
                        blocksLatch.countDown();
                    }
                };
                threads.add(t);
            }
            LOG.info("Ending to load " + w_id + " blocks");
        } // FOR

        // contracts
        for (int w = 1; w <= numWarehouses; w++) {
            final int w_id = w;
            // int numContractsPerLoader = WEB3Config.configContractsCount / numLoaders;
            // for (int i = 1; i <= numLoaders; i++) {
            //     final int loader_id = i;
            //     int contractsStartInclusive = (loader_id - 1) * numContractsPerLoader + 1;
            //     int contractsEndInclusive = (loader_id == numLoaders) ? WEB3Config.configContractsCount
            //             : contractsStartInclusive + numContractsPerLoader - 1;
                LoaderThread t = new LoaderThread() {
                    @Override
                    public void load(Connection conn) throws SQLException {
                        // Make sure that we load the blocks table first
                        try {
                            blocksLatch.await();
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                            throw new RuntimeException(ex);
                        }
                        LOG.info("Starting to load " + w_id + " contracts");
                        loadContracts(conn, w_id, WEB3Config.configContractsCount);
                        LOG.info("Ending to load " + w_id + " contracts");
                        // LOG.info("Starting to load " + w_id + " contracts" + " from " + contractsStartInclusive + " to " + contractsEndInclusive + " with the " + loader_id + " loader");
                        // loadContracts(conn, w_id, contractsStartInclusive, contractsEndInclusive);
                        // LOG.info("Ending to load " + w_id + " contracts" + " from " + contractsStartInclusive + " to " + contractsEndInclusive + " with the " + loader_id + " loader");
                        contractsLatch.countDown();
                    }
                };
                threads.add(t);
            // }
        } // FOR

        // transactions
        for (int w = 1; w <= numWarehouses; w++) {
            final int w_id = w;
            // for (int i = 1; i <= WEB3Config.configTransactionsCount;) {
            //     int numTransactionsPerLoader = WEB3Config.configTransactionsCount / numLoaders + 1;
            //     int transactionsStartInclusive = i;
            //     int transactionsEndInclusive = Math.min(WEB3Config.configTransactionsCount,
            //             transactionsStartInclusive + numTransactionsPerLoader - 1);
                LoaderThread t = new LoaderThread() {
                    @Override
                    public void load(Connection conn) throws SQLException {
                        // Make sure that we load the blocks and contracts tables first
                        try {
                            blocksLatch.await();
                            contractsLatch.await();
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                            throw new RuntimeException(ex);
                        }
                        LOG.info("Starting to load " + w_id + " transactions");
                        // LOG.info("Starting to load " + w_id + " transactions" + " from " + transactionsStartInclusive + " to " + transactionsEndInclusive);
                        loadTransactions(conn, w_id, WEB3Config.configTransactionsCount);
                        // loadTransactions(conn, w_id, transactionsStartInclusive, transactionsEndInclusive);
                        LOG.info("Ending to load " + w_id + " transactions");
                        // LOG.info("Ending to load " + w_id + " transactions" + " from " + transactionsStartInclusive + " to " + transactionsEndInclusive);
                        transactionsLatch.countDown();
                    }
                };
                threads.add(t);
            //     i = transactionsEndInclusive + 1;
            // }
        } // FOR

        // token_transfers
        for (int w = 1; w <= numWarehouses; w++) {
            final int w_id = w;
            // for (int i = 1; i <= WEB3Config.configToken_transfersCount;) {
            //     int numTTPerLoader = WEB3Config.configToken_transfersCount / numLoaders + 1;
            //     int ttStartInclusive = i;
            //     int ttEndInclusive = Math.min(WEB3Config.configToken_transfersCount,
            //             ttStartInclusive + numTTPerLoader - 1);
                LoaderThread t = new LoaderThread() {
                    @Override
                    public void load(Connection conn) throws SQLException {
                        // Make sure that we load the blocks and transactions tables first
                        try {
                            blocksLatch.await();
                            transactionsLatch.await();
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                            throw new RuntimeException(ex);
                        }
                        LOG.info("Starting to load " + w_id + " token_transfers");
                        // LOG.info("Starting to load " + w_id + " token_transfers" + " from " + ttStartInclusive + " to " + ttEndInclusive);
                        loadToken_transfers(conn, w_id, WEB3Config.configToken_transfersCount);
                        // loadToken_transfers(conn, w_id, ttStartInclusive, ttEndInclusive);
                        LOG.info("Ending to load " + w_id + " token_transfers");
                        // LOG.info("Ending to load " + w_id + " token_transfers" + " from " + ttStartInclusive + " to " + ttEndInclusive);
                        // tokentransfersLatch.countDown();
                    }
                };
                threads.add(t);
            //     i = ttEndInclusive + 1;
            // }
        } // FOR

        // List<String> constraints = new ArrayList<>();
        // constraints.add("ALTER TABLE contracts ADD FOREIGN KEY fk_bn (block_number) REFERENCES blocks (number);");
        // constraints.add("ALTER TABLE transactions ADD FOREIGN KEY fk_bn (block_number) REFERENCES blocks (number);");
        // constraints.add("ALTER TABLE transactions ADD FOREIGN KEY fk_ca (receipt_contract_address) REFERENCES contracts (address);");
        // constraints.add("ALTER TABLE token_transfers ADD FOREIGN KEY fk_bn (block_number) REFERENCES blocks (number);");
        // constraints.add("ALTER TABLE token_transfers ADD FOREIGN KEY fk_th (transaction_hash) REFERENCES transactions (hash);");
        // constraints.add("ALTER TABLE blocks ADD CONSTRAINT check_block_gas_used CHECK (gas_limit >= gas_used);");
        // constraints.add("ALTER TABLE transactions ADD CONSTRAINT check_txn_gas_used CHECK (receipt_gas_used <= gas);");
        // // excute constraints sql statements in parallel
        // for (String constraint : constraints) {
        //     LoaderThread t = new LoaderThread() {
        //         @Override
        //         public void load(Connection conn) throws SQLException {
        //             // make sure that we load the blocks, transactions, contracts and token_transfers tables first
        //             try {
        //                 blocksLatch.await();
        //                 transactionsLatch.await();
        //                 contractsLatch.await();
        //                 tokentransfersLatch.await();
        //             } catch (InterruptedException ex) {
        //                 ex.printStackTrace();
        //                 throw new RuntimeException(ex);
        //             }
        //             Statement stmt = conn.createStatement();
        //             LOG.info("Starting to execute constraint: " + constraint);
        //             stmt.execute(constraint);
        //             LOG.info("Ending to execute constraint: " + constraint);
        //             stmt.close();
        //         }
        //     };
        //     threads.add(t);
        // }

        // Prepare a temp table for the workload RangeInsertTempTable
        LoaderThread t = new LoaderThread() {
            @Override
            public void load(Connection conn) throws SQLException {
                LOG.info("Starting preparing temp table for RangeInsertTempTable");
                loadTemptable(conn, numWarehouses);
                LOG.info("Finished preparing temp table for RangeInsertTempTable");
            }
        };
        threads.add(t);

        return (threads);
    }

    private PreparedStatement getInsertStatement(Connection conn, String tableName) throws SQLException {
        Table catalog_tbl = this.benchmark.getTableCatalog(tableName);
        // LOG.info("DataLoader: Table: " + tableName + ", catalog_tbl: " + catalog_tbl);
        assert (catalog_tbl != null) : "No catalog entry for table: " + tableName;
        String sql = SQLUtil.getInsertSQL(catalog_tbl, this.getDatabaseType());
        // LOG.info("SQL: " + sql);
        PreparedStatement stmt = conn.prepareStatement(sql);
        return stmt;
    }

    protected void transRollback(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException se) {
            LOG.debug(se.getMessage());
        }
    }

    protected void transCommit(Connection conn) {
        try {
            conn.commit();
        } catch (SQLException se) {
            LOG.debug(se.getMessage());
            transRollback(conn);
        }
    }

    // protected int loadBlocks(Connection conn, int w_id, int Kount) {
    protected int loadBlocks(Connection conn, int w_id, int blocksStartInclusive, int blocksEndInclusive) {
        int k = 0;
        boolean fail = false;
        // print conn info

        try {
            Blocks blocks = new Blocks();
            PreparedStatement PrepStmt = getInsertStatement(conn, WEB3Constants.TABLENAME_BLOCKS);
            // LOG.info("DataLoader: Table: " + WEB3Constants.TABLENAME_BLOCKS + ", PrepStmt: " + PrepStmt);

            int batchSize = 0;
            // for (int i = 1; i <= Kount; i++) {
            for (int i = blocksStartInclusive; i <= blocksEndInclusive; i++) {
                blocks.number               = i + (w_id - 1) * WEB3Config.configBlocksCount;
                blocks.hash                 = WEB3Util.convertToBlockHashString(blocks.number);
                blocks.parent_hash          = WEB3Util.convertToBlockHashString(blocks.number - 1);
                blocks.nonce                = WEB3Util.randomHexString(42);
                blocks.sha3_uncles          = WEB3Util.randomHashString();
                blocks.transactions_root    = WEB3Util.randomHashString();
                blocks.state_root           = WEB3Util.randomHashString();
                blocks.receipts_root        = WEB3Util.randomHashString();
                
                blocks.miner                = WEB3Util.convertToAddressString(WEB3Util.randomNumber(1, WEB3Config.configAccountsCount, benchmark.rng()));
                
                blocks.difficulty           = WEB3Config.configBlockDifficulty;
                blocks.total_difficulty     = blocks.number * WEB3Config.configBlockDifficulty;
                
                blocks.size                 = WEB3Util.randomNumber(100, 100000, benchmark.rng());
                blocks.extra_data           = WEB3Util.randomStr(WEB3Util.randomNumber(1, 1000, benchmark.rng()));
                blocks.gas_limit            = WEB3Util.randomNumber(WEB3Config.configGasLimitLowerBound, 100000000, benchmark.rng());
                blocks.gas_used             = WEB3Util.getGasUsed(blocks.number);
                blocks.timestamp            = WEB3Util.getGasUsed(blocks.number);
                blocks.transaction_count    = WEB3Util.getTransactionCount(blocks.number);
                blocks.base_fee_per_gas     = WEB3Util.randomNumber(100, 100000, benchmark.rng());

                k++;

                int idx = 1;
                PrepStmt.setLong(idx++, blocks.timestamp);
                PrepStmt.setLong(idx++, blocks.number);
                PrepStmt.setString(idx++, blocks.hash);
                PrepStmt.setString(idx++, blocks.parent_hash);
                PrepStmt.setString(idx++, blocks.nonce);
                PrepStmt.setString(idx++, blocks.sha3_uncles);
                PrepStmt.setString(idx++, blocks.transactions_root);
                PrepStmt.setString(idx++, blocks.state_root);
                PrepStmt.setString(idx++, blocks.receipts_root);
                PrepStmt.setString(idx++, blocks.miner);
                PrepStmt.setDouble(idx++, blocks.difficulty);
                PrepStmt.setDouble(idx++, blocks.total_difficulty);
                PrepStmt.setLong(idx++, blocks.size);
                PrepStmt.setString(idx++, blocks.extra_data);
                PrepStmt.setLong(idx++, blocks.gas_limit);
                PrepStmt.setLong(idx++, blocks.gas_used);
                PrepStmt.setLong(idx++, blocks.transaction_count);
                PrepStmt.setLong(idx++, blocks.base_fee_per_gas);

                PrepStmt.addBatch();
                batchSize++;

                if (batchSize >= WEB3Config.configCommitCount) {
                    PrepStmt.executeBatch();
                    PrepStmt.clearBatch();
                    transCommit(conn);
                    batchSize = 0;
                }
            } // end for

            if (batchSize > 0)
                PrepStmt.executeBatch();
            transCommit(conn);

        } catch (BatchUpdateException ex) {
            SQLException next = ex.getNextException();
            LOG.error("Failed to load data for TPC-C", ex);
            if (next != null)
                LOG.error(ex.getClass().getSimpleName() + " Cause => " + next.getMessage());
            fail = true;
        } catch (SQLException ex) {
            SQLException next = ex.getNextException();
            LOG.error("Failed to load data for TPC-C", ex);
            if (next != null)
                LOG.error(ex.getClass().getSimpleName() + " Cause => " + next.getMessage());
            fail = true;
        } catch (Exception ex) {
            LOG.error("Failed to load data for TPC-C", ex);
            fail = true;
        } finally {
            if (fail) {
                LOG.debug("Rolling back changes from last batch");
                transRollback(conn);
            }
        }
        return (k);
    } // end loadBlocks()

    protected int loadContracts(Connection conn, int w_id, int Kount) {
    // protected int loadContracts(Connection conn, int w_id, int contractsStartInclusive, int contractsEndInclusive) {
        int k = 0;
        boolean fail = false;

        try {
            Contracts contracts = new Contracts();
            PreparedStatement PrepStmt = getInsertStatement(conn, WEB3Constants.TABLENAME_CONTRACTS);

            int batchSize = 0;
            for (int i = 1; i <= Kount; i++) {
            // for (int i = contractsStartInclusive; i <= contractsEndInclusive; i++) {
                contracts.address = WEB3Util.convertToContractAddressString(i + (w_id - 1) * WEB3Config.configContractsCount);
                contracts.bytecode = WEB3Util.randomStr(WEB3Util.randomNumber(1, 1000, benchmark.rng()));
                contracts.function_sighashes = WEB3Util.randomStr(WEB3Util.randomNumber(1, 1000, benchmark.rng()));
                contracts.is_erc20 = false; // benchmark.rng().nextBoolean();
                contracts.is_erc721 = false; // benchmark.rng().nextBoolean();
                contracts.block_number = WEB3Util.randomNumber((w_id - 1) * WEB3Config.configBlocksCount + 1, w_id * WEB3Config.configBlocksCount, benchmark.rng());

                k++;

                int idx = 1;
                PrepStmt.setString(idx++, contracts.address);
                PrepStmt.setString(idx++, contracts.bytecode);
                PrepStmt.setString(idx++, contracts.function_sighashes);
                PrepStmt.setBoolean(idx++, contracts.is_erc20);
                PrepStmt.setBoolean(idx++, contracts.is_erc721);
                PrepStmt.setLong(idx++, contracts.block_number);

                PrepStmt.addBatch();
                batchSize++;

                if (batchSize >= WEB3Config.configCommitCount) {
                    PrepStmt.executeBatch();
                    PrepStmt.clearBatch();
                    transCommit(conn);
                    batchSize = 0;
                }
            } // end for

            if (batchSize > 0)
                PrepStmt.executeBatch();
            transCommit(conn);

        } catch (BatchUpdateException ex) {
            SQLException next = ex.getNextException();
            LOG.error("Failed to load data for TPC-C", ex);
            if (next != null)
                LOG.error(ex.getClass().getSimpleName() + " Cause => " + next.getMessage());
            fail = true;
        } catch (SQLException ex) {
            SQLException next = ex.getNextException();
            LOG.error("Failed to load data for TPC-C", ex);
            if (next != null)
                LOG.error(ex.getClass().getSimpleName() + " Cause => " + next.getMessage());
            fail = true;
        } catch (Exception ex) {
            LOG.error("Failed to load data for TPC-C", ex);
            fail = true;
        } finally {
            if (fail) {
                LOG.debug("Rolling back changes from last batch");
                transRollback(conn);
            }
        }
        return (k);
    } // end loadContracts()

    protected int loadTransactions(Connection conn, int w_id, int Kount) {
    // protected int loadTransactions(Connection conn, int w_id, int transactionsStartInclusive, int transactionsEndInclusive) {
        int k = 0;
        boolean fail = false;

        try {
            Transactions transactions = new Transactions();
            PreparedStatement PrepStmt = getInsertStatement(conn, WEB3Constants.TABLENAME_TRANSACTIONS);

            int batchSize = 0;

            long contract_start = (w_id - 1) * WEB3Config.configContractsCount + 1;
            long contract_end = w_id * WEB3Config.configContractsCount;

            // three types of transactions
            // 1. Regular transactions
            // 2. Execution of a contract
            // 3. Contract deployment transactions
            boolean is_regular = false;
            boolean is_contract_execution = false;
            boolean is_contract_deployment = false;

            // for (int i = transactionsStartInclusive; i <= transactionsEndInclusive; i++) {
            for (int i = 1; i <= Kount; i++) {
                transactions.block_number = 1 + (i - 1) % WEB3Config.configBlocksCount + (w_id - 1) * WEB3Config.configBlocksCount; // block_number starting from 1
                transactions.block_hash = WEB3Util.convertToBlockHashString(transactions.block_number);
                transactions.block_timestamp = WEB3Util.getTimestamp(transactions.block_number);
                
                long transaction_count = WEB3Util.getTransactionCount(transactions.block_number);
                transactions.receipt_cumulative_gas_used = 0;
                transactions.transaction_index = (i - 1) / WEB3Config.configBlocksCount; // transaction_index starting from 0
                transactions.hash = WEB3Util.convertToTxnHashString(i + (w_id - 1) * WEB3Config.configTransactionsCount);

                double randomNumber = benchmark.rng().nextDouble();

                transactions.from_address = WEB3Util.convertToAddressString(i % WEB3Config.configAccountsCount);
                transactions.nonce = ((w_id - 1) * WEB3Config.configTransactionsCount + i - 1) / WEB3Config.configAccountsCount;

                // to_address & receipt_contract_address
                // three types of transactions
                if (randomNumber < 0.89) {
                    // 1. Regular transactions
                    is_regular = true;
                    transactions.to_address = WEB3Util.convertToAddressString(WEB3Util.randomNumber(1, WEB3Config.configAccountsCount, benchmark.rng()));
                    // transactions.receipt_contract_address = NULL;
                } else if (randomNumber < 0.99) {
                    // 2. Execution of a contract
                    is_contract_execution = true;
                    transactions.to_address = WEB3Util.convertToContractAddressString(
                            WEB3Util.randomNumber(0, contract_end, benchmark.rng()));
                    // transactions.receipt_contract_address = NULL;
                } else {
                    // 3. Contract deployment transactions
                    is_contract_deployment = true;
                    // transactions.to_address = NULL;
                    transactions.receipt_contract_address = WEB3Util.convertToContractAddressString(
                            WEB3Util.randomNumber(contract_start, contract_end - 1, benchmark.rng()));
                }

                transactions.value = (double) WEB3Util.randomNumber(0, 1000000, benchmark.rng());
                transactions.gas_price = WEB3Util.randomNumber(1000, 10000000, benchmark.rng());
                transactions.input = WEB3Util.randomStr(WEB3Util.randomNumber(1, 1000, benchmark.rng()));
                // receipt_gas_used & receipt_cumulative_gas_used
                if (transactions.transaction_index == transaction_count - 1) {
                    transactions.receipt_gas_used = WEB3Util.getGasUsed(transactions.block_number)
                    / transaction_count
                    + WEB3Util.getGasUsed(transactions.block_number) % transaction_count;
                    transactions.receipt_cumulative_gas_used = WEB3Util.getGasUsed(transactions.block_number);
                } else {
                    transactions.receipt_gas_used = WEB3Util.getGasUsed(transactions.block_number)
                    / transaction_count;
                    transactions.receipt_cumulative_gas_used = transactions.receipt_gas_used * (transactions.transaction_index + 1);
                }
                transactions.gas = WEB3Util.randomNumber(transactions.receipt_gas_used + 100, Long.MAX_VALUE, benchmark.rng());
                transactions.receipt_root = WEB3Util.randomHashString();
                transactions.receipt_status = WEB3Util.randomNumber(0, 100, benchmark.rng());
                transactions.max_fee_per_gas = WEB3Util.randomNumber(100, 10000, benchmark.rng());
                transactions.max_priority_fee_per_gas = WEB3Util.randomNumber(100, 10000, benchmark.rng());
                transactions.transaction_type = WEB3Util.randomNumber(0, 10000, benchmark.rng());

                k++;

                int idx = 1;
                PrepStmt.setString(idx++, transactions.hash);
                PrepStmt.setLong(idx++, transactions.nonce);
                PrepStmt.setString(idx++, transactions.block_hash);
                PrepStmt.setLong(idx++, transactions.block_number);
                PrepStmt.setLong(idx++, transactions.transaction_index);
                PrepStmt.setString(idx++, transactions.from_address);
                // transactions.to_address
                if (is_regular) {
                    // 1. Regular transactions
                    PrepStmt.setString(idx++, transactions.to_address);
                } else if (is_contract_execution) {
                    // 2. Execution of a contract
                    PrepStmt.setString(idx++, transactions.to_address);
                } else if (is_contract_deployment) {
                    // 3. Contract deployment transactions
                    PrepStmt.setNull(idx++, Types.VARCHAR);
                } else {
                    // error
                    System.out.println("Error: transaction type error");
                    fail = true;
                    break;
                }
                PrepStmt.setDouble(idx++, transactions.value);
                PrepStmt.setLong(idx++, transactions.gas);
                PrepStmt.setLong(idx++, transactions.gas_price);
                PrepStmt.setString(idx++, transactions.input);
                PrepStmt.setLong(idx++, transactions.receipt_cumulative_gas_used);
                PrepStmt.setLong(idx++, transactions.receipt_gas_used);
                // transactions.receipt_contract_address
                if (is_regular) {
                    // 1. Regular transactions
                    PrepStmt.setNull(idx++, Types.VARCHAR);
                    is_regular = false;
                } else if (is_contract_execution) {
                    // 2. Execution of a contract
                    PrepStmt.setString(idx++, null);
                    is_contract_execution = false;
                } else if (is_contract_deployment) {
                    // 3. Contract deployment transactions
                    PrepStmt.setString(idx++, transactions.receipt_contract_address);
                    is_contract_deployment = false;
                } else {
                    // error
                    System.out.println("Error: transaction type error");
                    fail = true;
                    break;
                }
                PrepStmt.setString(idx++, transactions.receipt_root);
                PrepStmt.setLong(idx++, transactions.receipt_status);
                PrepStmt.setLong(idx++, transactions.block_timestamp);
                PrepStmt.setLong(idx++, transactions.max_fee_per_gas);
                PrepStmt.setLong(idx++, transactions.max_priority_fee_per_gas);
                PrepStmt.setLong(idx++, transactions.transaction_type);

                PrepStmt.addBatch();
                batchSize++;

                if (batchSize >= WEB3Config.configCommitCount) {
                    PrepStmt.executeBatch();
                    PrepStmt.clearBatch();
                    transCommit(conn);
                    batchSize = 0;
                }
            } // end for

            if (batchSize > 0)
                PrepStmt.executeBatch();
            transCommit(conn);

        } catch (BatchUpdateException ex) {
            SQLException next = ex.getNextException();
            LOG.error("Failed to load data for TPC-C", ex);
            if (next != null)
                LOG.error(ex.getClass().getSimpleName() + " Cause => " + next.getMessage());
            fail = true;
        } catch (SQLException ex) {
            SQLException next = ex.getNextException();
            LOG.error("Failed to load data for TPC-C", ex);
            if (next != null)
                LOG.error(ex.getClass().getSimpleName() + " Cause => " + next.getMessage());
            fail = true;
        } catch (Exception ex) {
            LOG.error("Failed to load data for TPC-C", ex);
            fail = true;
        } finally {
            if (fail) {
                LOG.debug("Rolling back changes from last batch");
                transRollback(conn);
            }
        }
        return (k);
    } // end loadTransactions()

    protected int loadToken_transfers(Connection conn, int w_id, int Kount) {
    // protected int loadToken_transfers(Connection conn, int w_id, int ttStartInclusive, int ttEndExclusive) {
        int k = 0;
        boolean fail = false;

        try {
            // reminder: edit everytime
            Token_transfers token_transfers = new Token_transfers();
            PreparedStatement PrepStmt = getInsertStatement(conn, WEB3Constants.TABLENAME_TOKEN_TRANSFERS);

            int batchSize = 0;
            long transaction_start = (w_id - 1) * WEB3Config.configTransactionsCount + 1;
            long transaction_end = w_id * WEB3Config.configTransactionsCount;

            for (int i = 1; i <= Kount; i++) {
            // for (int i = ttStartInclusive; i <= ttEndExclusive; i++) {
                token_transfers.token_address = WEB3Util.convertToTokenAddressString(WEB3Util.randomNumber(1, WEB3Config.configTokenCount, benchmark.rng()));
                token_transfers.from_address = WEB3Util.convertToAddressString(WEB3Util.randomNumber(1, WEB3Config.configAccountsCount, benchmark.rng()));
                token_transfers.to_address = WEB3Util.convertToAddressString(WEB3Util.randomNumber(1, WEB3Config.configAccountsCount, benchmark.rng()));
                token_transfers.value = (double) WEB3Util.randomNumber(0, 1000000, benchmark.rng());
                token_transfers.transaction_hash = WEB3Util.convertToTxnHashString(
                        WEB3Util.randomNumber(transaction_start, transaction_end, benchmark.rng()));
                token_transfers.block_number = WEB3Util.randomNumber((w_id - 1) * WEB3Config.configBlocksCount + 1, w_id * WEB3Config.configBlocksCount, benchmark.rng());

                k++;

                int idx = 1;
                PrepStmt.setString(idx++, token_transfers.token_address);
                PrepStmt.setString(idx++, token_transfers.from_address);
                PrepStmt.setString(idx++, token_transfers.to_address);
                PrepStmt.setDouble(idx++, token_transfers.value);
                PrepStmt.setString(idx++, token_transfers.transaction_hash);
                PrepStmt.setLong(idx++, token_transfers.block_number);

                PrepStmt.addBatch();
                batchSize++;

                if (batchSize == WEB3Config.configCommitCount) {
                    PrepStmt.executeBatch();
                    PrepStmt.clearBatch();
                    transCommit(conn);
                    batchSize = 0;
                }
            } // end for

            if (batchSize > 0)
                PrepStmt.executeBatch();
            transCommit(conn);

        } catch (BatchUpdateException ex) {
            SQLException next = ex.getNextException();
            LOG.error("Failed to load data for TPC-C", ex);
            if (next != null)
                LOG.error(ex.getClass().getSimpleName() + " Cause => " + next.getMessage());
            fail = true;
        } catch (SQLException ex) {
            SQLException next = ex.getNextException();
            LOG.error("Failed to load data for TPC-C", ex);
            if (next != null)
                LOG.error(ex.getClass().getSimpleName() + " Cause => " + next.getMessage());
            fail = true;
        } catch (Exception ex) {
            LOG.error("Failed to load data for TPC-C", ex);
            fail = true;
        } finally {
            if (fail) {
                LOG.debug("Rolling back changes from last batch");
                transRollback(conn);
            }
        }
        return (k);
    } // end loadToken_transfers()

    public int loadTemptable(Connection conn, int numScale) {

        boolean fail = false;

        try {
            PreparedStatement query_stmt = getInsertStatement(conn, WEB3Constants.TABLENAME_TEMPTABLE);

            for (int i = 0; i < 2000; i++) {
                String hash = WEB3Util.convertToTxnHashString(4 * numScale * WEB3Config.configTransactionsCount + i + 1);
                long nonce = WEB3Util.randomNumber(0, 100, benchmark.rng());
                long block_number = WEB3Util.randomNumber(1, numScale * WEB3Config.configBlocksCount, benchmark.rng());
                String block_hash = WEB3Util.convertToBlockHashString(block_number);
                long transaction_index = WEB3Util.randomNumber(0, 10000, benchmark.rng());
                String from_address = WEB3Util.convertToAddressString(WEB3Util.randomNumber(1, WEB3Config.configAccountsCount, benchmark.rng()));
                String to_address = WEB3Util.convertToAddressString(WEB3Util.randomNumber(1, WEB3Config.configAccountsCount, benchmark.rng()));
                double value = (double) WEB3Util.randomNumber(0, 1000000, benchmark.rng());
                long gas = WEB3Util.randomNumber(100, 1000000, benchmark.rng());
                long gas_price = WEB3Util.randomNumber(1000, 10000000, benchmark.rng());
                String input = WEB3Util.randomStr(WEB3Util.randomNumber(1, 1000, benchmark.rng()));
                long receipt_cumulative_gas_used = WEB3Util.randomNumber(100, 1000000, benchmark.rng());
                long receipt_gas_used = WEB3Util.randomNumber(10, gas, benchmark.rng());
                String receipt_contract_address = WEB3Util.convertToContractAddressString(WEB3Util.randomNumber(1, numScale * WEB3Config.configContractsCount, benchmark.rng()));
                String receipt_root = WEB3Util.randomHashString();
                long receipt_status = WEB3Util.randomNumber(0, 100, benchmark.rng());
                long block_timestamp = WEB3Util.getTimestamp(block_number);
                long max_fee_per_gas = WEB3Util.randomNumber(100, 10000, benchmark.rng());
                long max_priority_fee_per_gas = WEB3Util.randomNumber(100, 10000, benchmark.rng());
                long transaction_type = WEB3Util.randomNumber(0, 100000, benchmark.rng());

                int idx = 1;
                query_stmt.setString(idx++, hash);
                query_stmt.setLong(idx++, nonce);
                query_stmt.setString(idx++, block_hash);
                query_stmt.setLong(idx++, block_number);
                query_stmt.setLong(idx++, transaction_index);
                query_stmt.setString(idx++, from_address);
                query_stmt.setString(idx++, to_address);
                query_stmt.setDouble(idx++, value);
                query_stmt.setLong(idx++, gas);
                query_stmt.setLong(idx++, gas_price);
                query_stmt.setString(idx++, input);
                query_stmt.setLong(idx++, receipt_cumulative_gas_used);
                query_stmt.setLong(idx++, receipt_gas_used);
                query_stmt.setString(idx++, receipt_contract_address);
                query_stmt.setString(idx++, receipt_root);
                query_stmt.setLong(idx++, receipt_status);
                query_stmt.setLong(idx++, block_timestamp);
                query_stmt.setLong(idx++, max_fee_per_gas);
                query_stmt.setLong(idx++, max_priority_fee_per_gas);
                query_stmt.setLong(idx++, transaction_type);

                query_stmt.addBatch();
            }

            query_stmt.executeBatch();
            conn.commit();
        } catch (BatchUpdateException ex) {
            SQLException next = ex.getNextException();
            LOG.error("Failed to load data for TPC-C", ex);
            if (next != null)
                LOG.error(ex.getClass().getSimpleName() + " Cause => " + next.getMessage());
            fail = true;
        } catch (SQLException ex) {
            SQLException next = ex.getNextException();
            LOG.error("Failed to load data for TPC-C", ex);
            if (next != null)
                LOG.error(ex.getClass().getSimpleName() + " Cause => " + next.getMessage());
            fail = true;
        } catch (Exception ex) {
            LOG.error("Failed to load data for TPC-C", ex);
            fail = true;
        } finally {
            if (fail) {
                LOG.debug("Rolling back changes from last batch");
                transRollback(conn);
            }
        }
        return 1;
    }
}
