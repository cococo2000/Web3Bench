# Web3Bench

Welcome to Web3Bench, a cutting-edge benchmark designed specifically for real-world Web3 applications and addressing the challenges of decentralized databases. This benchmark offers a unified approach by providing a cohesive data model and workload that cover both transactional (T) and analytical (A) tasks. Notable features include a novel business scenario sourced from the Web3 domain, a categorization of workloads based on latency requirements (real-time, online serving, and batch processing), and the incorporation of latency limits for real-time and online queries, along with measuring batch query throughput.

Our data model is a simplified version of Ethereum's decentralized blockchain, encompassing blocks, transactions, contracts, and token transfers. Derived from a 20GB real Ethereum dataset, Web3Bench employs a proprietary data generator to create datasets of varying scales.

The benchmark encompasses a spectrum of tasks, including transactional, analytical, and online serving scenarios. It accommodates real-time and online activities with high query rates, while also executing batch tasks once during testing. Using a specialized driver, Web3Bench assesses both latency and throughput. As a proof of concept, we've successfully demonstrated its application on the [TiDB](https://github.com/pingcap/tidb) platform.

## Environment Setup
- Install Java (v1.7 or newer) and Apache Ant.
- Deploy TiDB cluster following the [TiDB documentation](https://docs.pingcap.com/tidb/stable/quick-start-with-tidb).

## Build Process
1. Navigate to the project directory:
    ```bash
    cd [Web3Bench.dir]
    ```
2. Bootstrap the project using Ant
    ```bash
    ant bootstrap
    ```
3. Resolve project dependencies
    ```bash
    ant resolve
    ```
4. Build the project
    ```
    ant build
    ```

## Config

### Configuration Directory
- config/*
    - This directory contains various configuration files used for testing on the VM. Each configuration file is designed to facilitate specific testing scenarios and workloads.
        - `loaddata.xml`: the configuration file for loading data into the database
        - `runthread1.xml`: the configuration file for running point query workloads (R1, W1*, W4 and W6) at a rate of 300 requests per minute.
        - `runthread2.xml`: the configuration file for running complex query workloads once in serial covering R3*, W2, W3 and W5*.
        - `runR21.xml`: the configuration file for running R21 at a rate of 1 request per minute.
        - `runR22.xml`: the configuration file for running R22 at a rate of 1 request per minute.
        - `runR23.xml`: the configuration file for running R23 at a rate of 1 request per minute.
        - `runR24.xml`: the configuration file for running R24 at a rate of 1 request per minute.


### Workload Descriptor
Web3Bench uses a configuration file to drive a given benchmark. 
The workload descriptor (or configuration file) provides the general information to access the database (driver, URL, credential .. etc), benchmark specific options and most importantly, the workload mix.
When running a multi-phase experiment with varying a workload, one should provide multiple <work> sections with their duration, rate, and the weight of each transaction. 

> Note: weights have to sum up to 100%. The transactions are listed in the benchmark specific section <transactiontypes>. The order in which the transactions are declared is the same as their respective weights.

```xml
<?xml version="1.0"?>
<parameters>
    <!-- Connection details -->
    <dbtype>mysql</dbtype>
    <driver>com.mysql.jdbc.Driver</driver>
    <DBUrl>jdbc:mysql://127.0.0.1:3500/ethereum3?useSSL=false&amp;characterEncoding=utf-8</DBUrl>
    <username>root</username>
    <password></password>
    <isolation>TRANSACTION_SERIALIZABLE</isolation>
    <distribution>rand</distribution>
    <uploadUrl></uploadUrl>

    <scalefactor>3</scalefactor>

    <!-- The workload -->
    <terminals>1</terminals>
    <works>
        <work>
            <warmup>0</warmup>
            <time>60</time>
            <rate>300</rate>
            <weights>80,3,3,4,4,3,3</weights>
            <arrival>REGULAR</arrival>
        </work>
    </works>

    <transactiontypes>
        <transactiontype>
            <name>R1</name>
        </transactiontype>
        <transactiontype>
            <name>W11</name>
        </transactiontype>
        <transactiontype>
            <name>W12</name>
        </transactiontype>
        <transactiontype>
            <name>W13</name>
        </transactiontype>
        <transactiontype>
            <name>W14</name>
        </transactiontype>
        <transactiontype>
            <name>W4</name>
        </transactiontype>
        <transactiontype>
            <name>W6</name>
        </transactiontype>
    </transactiontypes>
</parameters>
```

- **DBUrl**: the URL to access the database
- **username**: the username to access the database
- **password**: the password to access the database
- **scalefactor**: the scale factor for loading data. When saclefactor = 1, the data size is around 25GB.
- **time**: the duration of the workload in minutes
- **rate**: the sending rate of the workload in transactions per minute
- **weights**: the weights of the transactions in the workload. The sum of the weights should be 100.
- **transactiontypes**: the list of transactions in the workload. The order of the transactions should be the same as the order of the weights.

## Run Web3Bench

### Before Running Web3Bench

#### Create Database
```bash
# Connect to mysql or tidb server
mysql -h <hostname> -u <username> -p <password> -P <port>

# After connecting, create a new database by executing the following SQL command
CREATE DATABASE <your_database_name>;
```
Replace <your_database_name> with the desired name for your database. Make sure to replace it with a valid database name that you want to use. This command will create a new database with the specified name.

If you are running Web3Bench on TiDB, you need to execute the following SQL command to disable the isolation level check. Otherwise, you will get the error `The isolation level 'SERIALIZABLE' is not supported`.
```sql
set global tidb_skip_isolation_level_check=1;
```

#### Check Configuration Files

- Check **DBUrl** to make sure that the database name is the same as the one you created in the previous step.
- Check **username** and **password** to make sure that they are the same as the ones you used to connect to the database.
- Check **scalefactor** to make sure that it is the same as the one you used to load data.
    - Note: The value of the 'scalefactor' in both the data generation and running configuration files should be the same.
    - To quick modify the value of 'scalefactor' in the configuration files, you can use the following command:
        ```bash
        sed -i 's/<scalefactor>.*<\/scalefactor>/<scalefactor>4<\/scalefactor>/g' config/*.xml
        ```
        Replace 4 with the desired value of 'scalefactor'.


### Example
Examples for loading data and run web3benchmark with scale factor = 3

- Load data with scale factor = 3
  ```bash
  cd [Web3Bench.dir]
  # modify the value of 'scalefactor' in the configuration files
  sed -i 's/<scalefactor>.*<\/scalefactor>/<scalefactor>3<\/scalefactor>/g' config/*.xml
  
  # load data
  # method 1
  ./olxpbenchmark -b web3benchmark -c config/loaddata.xml --load=true  --create=true | tee log/loaddata.log
  # method 2
  cd script; nohup ./loaddata.sh &
  ```
- Run web3benchmark with scale factor = 3
  ```bash
  cd [Web3Bench.dir]
  # modify the value of 'scalefactor' in the configuration files
  sed -i 's/<scalefactor>.*<\/scalefactor>/<scalefactor>3<\/scalefactor>/g' config/*.xml

  # run web3benchmark
  cd script; nohup ./runbench.sh &
  ```

### Command Line Options
```bash
$ ./olxpbenchmark --help
usage: olxpbenchmark
 -b,--bench <arg>               [required] Benchmark class. Currently
                                supported: [web3benchmark]
 -c,--config <arg>              [required] Workload configuration file
    --clear <arg>               Clear all records in the database for this
                                benchmark
    --create <arg>              Initialize the database for this benchmark
 -d,--directory <arg>           Base directory for the result files,
                                default is current directory
    --dialects-export <arg>     Export benchmark SQL to a dialects file
    --execute <arg>             Execute the benchmark workload
 -h,--help                      Print this help
    --histograms                Print txn histograms
 -im,--interval-monitor <arg>   Throughput Monitoring Interval in
                                milliseconds
 -jh,--json-histograms <arg>    Export histograms to JSON file
    --load <arg>                Load data using the benchmark's data
                                loader
 -o,--output <arg>              Output file (default System.out)
    --output-raw <arg>          Output raw data
    --output-samples <arg>      Output sample data
    --runscript <arg>           Run an SQL script
 -s,--sample <arg>              Sampling window
 -ss                            Verbose Sampling per Transaction
 -t,--timestamp                 Each result file is prepended with a
                                timestamp for the beginning of the
                                experiment
 -ts,--tracescript <arg>        Script of transactions to execute
    --upload <arg>              Upload the result
    --uploadHash <arg>          git hash to be associated with the upload
 -v,--verbose                   Display Messages
```

- **-b,--bench**: the benchmark class. Currently, only web3benchmark is supported.
- **--create=true**: create the database schema by excuting the SQL script in ddl files(e.g., src/com/olxpbenchmark/benchmarks/web3benchmark/ddls/web3benchmark-mysql-ddl.sql)
- **--load=true**: load data into the database
- **--execute=true**: run the workload

## Results

### Output Directory
- results/*
    - The result files for the benchmark

### Parsing results
- script/parse-res.py
    - The script for parsing the result files and generating the summary file in csv format
    - Edit the head of the script to change the path of the results you want to parse
        - data = "": the path will be "results/"
        - data = "???" (not empty): the path will be "results/???"
    - Usage:
        ```bash
        cd script
        python3 parse-res.py
        ```

## Implementation

### Tables
```sql
CREATE TABLE blocks (
  timestamp bigint,
  number bigint PRIMARY KEY,
  hash varchar(66),
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

--  Add foreign keys
ALTER TABLE contracts ADD FOREIGN KEY fk_bn (block_number) REFERENCES blocks (number);
ALTER TABLE transactions ADD FOREIGN KEY fk_bn (block_number) REFERENCES blocks (number);
ALTER TABLE transactions ADD FOREIGN KEY fk_ca (receipt_contract_address) REFERENCES contracts (address);
ALTER TABLE token_transfers ADD FOREIGN KEY fk_bn (block_number) REFERENCES blocks (number);
ALTER TABLE token_transfers ADD FOREIGN KEY fk_th (transaction_hash) REFERENCES transactions (hash) ON DELETE CASCADE;

-- Add constraints
ALTER TABLE blocks ADD CONSTRAINT check_block_gas_used CHECK (gas_limit >= gas_used);
ALTER TABLE transactions ADD CONSTRAINT check_txn_gas_used CHECK (receipt_gas_used <= gas);
```

### Workloads
- R1: T queries
    ```sql
    Select to_address, from_address 
    from transactions 
    where hash = ?
    ```

- R2: O queries
    - R21: 
    ```sql
    Select * 
    from transactions 
    where to_address in (?, ?, ?)
    ```
    - R22: 
    ```sql
    Select *  
    from transactions 
    where hash in (?, ?, ?, ?) and to_adress <> from_address 
    ```
    - R23: top N with small N on full table scan. 
    ```sql
    SELECT * 
    FROM token_transfers 
    WHERE from_address = ? 
    ORDER BY block_number DESC 
    LIMIT 5
    ```
    - R24: Aggregation with no group by on a small range
    ```sql
    Select count(*) 
    from token_transfers 
    where token_address = ?
    ```
- R3: A queries
    - R31: For a specific person, find transactions where this person is either a sender or receiver. Limit the result by the most recent timestamp.
        - Notice: one person,  from_address and to_address have the same value
    ```sql
    SELECT * FROM transactions
    WHERE from_address = ? OR to_address = ? 
    ORDER BY block_timestamp DESC 
    LIMIT 10
    ```
    - R32: Top N transactions based on block timestamp. 
    ```sql
    SELECT * 
    FROM transactions
    ORDER BY block_timestamp DESC 
    LIMIT 10
    ```
    - R33: Find the number of unique senders (from\_address) in transactions 
    ```sql
    SELECT count(DISTINCT from_address) 
    FROM transactions
    ```
    - R34: Find top N senders (from\_address) by total transaction value
    ```sql
    SELECT 
        sum (value) AS totalamount, 
        count (value) AS transactioncount, 
        from_address AS fromaddress
    FROM transactions
    WHERE to_address = ? AND block_timestamp >= ? AND block_timestamp <= ? AND value > ? 
    GROUP BY from_address
    ORDER BY sum (value) DESC
    LIMIT 10
    ```
    - R35: Total count of token transfers for a specific sender and token transfers for recipients who are also senders in other transactions. 
    ```sql
    SELECT
        count(*)
    FROM ( 
        SELECT * 
        FROM token_transfers t
        WHERE from_address = ?
        UNION ALL
        SELECT t2.* 
        FROM token_transfers t2  
        INNER JOIN token_transfers t ON t2.from_address = t.to_address
        AND t.value < t2.value
    ) as temp
    ```
- Inserts
    - W1: single point inserts
        - W11: Insert blocks
        ```sql
        insert into blocks
            values (?, ?, ?, ?, ?,
                ?, ?, ?, ?, ?,
                ?, ?, ?, ?, ?,
                ?, ?, ?)
        ```
        - W12: Insert contracts
        ```sql
        insert into contracts
            values (?, ?, ?, ?, ?, ?)
        ```
        - W13: Insert transactions
        ```sql
        insert into transactions 
            values (?, ?, ?, ?, ?,
                ?, ?, ?, ?, ?,
                ?, ?, ?, ?, ?,
                ?, ?, ?, ?, ?)
        ```
        - W14: Insert token_transfers
        ```sql
        insert into token_transfers
            values (?, ?, ?, ?, ?, ?)
        ```
    - W2: Range inserts
        - Small batch inserts (100 rows) for the transaction table.
    - W3: Insert select
        - Insert 1000 rows into transactions from a temp table 
        ```sql
        insert transactions select * from temp_table limit 1000;
        ```
- Updates
    - W4: Point update
    ```sql
    update transactions 
    set gas_price = ? 
    where hash = ?
    ```
- W5: Join updates
    - W51: Join update 1
    ```sql
    UPDATE token_transfers
    SET value = ?
    WHERE to_address = from_address
    ```
    - W52: Join update 2
    ```sql
    UPDATE token_transfers
    SET value = value + 1
    WHERE from_address IN (SELECT to_address FROM token_transfers)
    ```
- Deletes
    - W6: point delete
    ```sql
    DELETE FROM transactions where hash = ?
    ```
