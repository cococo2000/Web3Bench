DROP TABLE IF EXISTS token_transfers;
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS contracts;
DROP TABLE IF EXISTS blocks;

CREATE TABLE blocks (
  timestamp bigint,
  number bigint,
  hash varchar(66) PRIMARY KEY,
  parent_hash varchar(66) DEFAULT NULL,
  nonce varchar(42) DEFAULT NULL,
  sha3_uncles varchar(66) DEFAULT NULL,
  transactions_root varchar(66) DEFAULT NULL,
  state_root varchar(66) DEFAULT NULL,
  receipts_root varchar(66) DEFAULT NULL,
  miner varchar(42) DEFAULT NULL,
  difficulty decimal(38,0) DEFAULT NULL,
  total_difficulty decimal(38,0) DEFAULT NULL,
  size bigint DEFAULT NULL,
  extra_data text DEFAULT NULL,
  gas_limit bigint DEFAULT NULL,
  gas_used bigint DEFAULT NULL,
  transaction_count bigint DEFAULT NULL,
  base_fee_per_gas bigint DEFAULT NULL
);

CREATE TABLE transactions (
  hash varchar(66) PRIMARY KEY,
  nonce bigint,
  block_hash varchar(66),
  block_number bigint,
  transaction_index bigint,
  from_address varchar(42),
  to_address varchar(42),
  value decimal(38,0),
  gas bigint,
  gas_price bigint,
  input text,
  receipt_cumulative_gas_used bigint,
  receipt_gas_used bigint,
  receipt_contract_address varchar(42),
  receipt_root varchar(66),
  receipt_status bigint,
  block_timestamp bigint,
  max_fee_per_gas bigint,
  max_priority_fee_per_gas bigint,
  transaction_type bigint
);

CREATE TABLE contracts (
  address varchar(42) PRIMARY KEY,
  bytecode text,
  function_sighashes text,
  is_erc20 boolean,
  is_erc721 boolean,
  block_number bigint
);

CREATE TABLE token_transfers (
  token_address varchar(42),
  from_address varchar(42),
  to_address varchar(42),
  value decimal(38,0),
  transaction_hash varchar(66),
  block_number bigint
);

-- Add indexes
CREATE INDEX idx_blocks_number ON blocks (number);
CREATE INDEX idx_transactions_to_address ON transactions (to_address);
-- CREATE INDEX idx_tt_from_address ON token_transfers (from_address);
-- CREATE INDEX idx_tt_to_address ON token_transfers (to_address);
-- CREATE INDEX idx_tt_from_to_address ON token_transfers (from_address, to_address);

-- Add foreign keys
ALTER TABLE contracts ADD FOREIGN KEY fk_bn (block_number) REFERENCES blocks (number);
ALTER TABLE transactions ADD FOREIGN KEY fk_bn (block_number) REFERENCES blocks (number);
ALTER TABLE transactions ADD FOREIGN KEY fk_ca (receipt_contract_address) REFERENCES contracts (address);
ALTER TABLE token_transfers ADD FOREIGN KEY fk_bn (block_number) REFERENCES blocks (number);
ALTER TABLE token_transfers ADD FOREIGN KEY fk_th (transaction_hash) REFERENCES transactions (hash) ON DELETE CASCADE;

-- Add constraints
ALTER TABLE blocks ADD CONSTRAINT check_block_gas_used CHECK (gas_limit >= gas_used);
ALTER TABLE transactions ADD CONSTRAINT check_txn_gas_used CHECK (receipt_gas_used <= gas);

-- Prepare for the workload RangeInsertTempTable
DROP TABLE IF EXISTS temp_table;
CREATE TABLE temp_table (
  hash varchar(66) PRIMARY KEY,
  nonce bigint,
  block_hash varchar(66),
  block_number bigint,
  transaction_index bigint,
  from_address varchar(42),
  to_address varchar(42),
  value decimal(38,0),
  gas bigint,
  gas_price bigint,
  input text,
  receipt_cumulative_gas_used bigint,
  receipt_gas_used bigint,
  receipt_contract_address varchar(42),
  receipt_root varchar(66),
  receipt_status bigint,
  block_timestamp bigint,
  max_fee_per_gas bigint,
  max_priority_fee_per_gas bigint,
  transaction_type bigint
);

-- Add TiFlash replica
ALTER TABLE blocks          SET TIFLASH REPLICA 1;
ALTER TABLE contracts       SET TIFLASH REPLICA 1;
ALTER TABLE transactions    SET TIFLASH REPLICA 1;
ALTER TABLE token_transfers SET TIFLASH REPLICA 1;
