# Web3Bench

Welcome to Web3Bench, a cutting-edge benchmark designed specifically for real-world Web3 applications and addressing the challenges of decentralized databases. This benchmark offers a unified approach by providing a cohesive data model and workload that cover both transactional (T) and analytical (A) tasks. Notable features include a novel business scenario sourced from the Web3 domain, a categorization of workloads based on latency requirements (real-time, online serving, and batch processing), and the incorporation of latency limits for real-time and online queries, along with measuring batch query throughput.

Our data model is a simplified version of Ethereum's decentralized blockchain, encompassing blocks, transactions, contracts, and token transfers. Derived from a 20GB real Ethereum dataset, Web3Bench employs a proprietary data generator to create datasets of varying scales.

The benchmark encompasses a spectrum of tasks, including transactional, analytical, and online serving scenarios. It accommodates real-time and online activities with high query rates, while also executing batch tasks once during testing. Using a specialized driver, Web3Bench assesses both latency and throughput. As a proof of concept, we've successfully demonstrated its application on the [TiDB](https://github.com/pingcap/tidb) platform.

## Environment Setup
- Install Java (v1.7 or newer) and Apache Ant.
- Deploy TiDB cluster following the [TiDB documentation](https://docs.pingcap.com/tidb/stable/quick-start-with-tidb).
    - If you want to test Web3Bench on TiDB, you can use the following commands to set or unlimit the memory quota for queries and server according to your environment.
        ```sql
        SET GLOBAL tidb_mem_quota_query=0;
        SET GLOBAL tidb_server_memory_limit=0;
        ```
- Install required Python packages (tested on Python 3.8.10 & 3.8.18)
    ```bash
    pip3 install -r requirements.txt
    ```

## Quick Start Guide
Below are the steps to promptly initiate Web3Bench with a scale factor of 3 on TiDB. The provided instructions cover a scenario with a database containing 3000 blocks, 240000 transactions, 2100 contracts, and 54000 token transfers, resulting in a database size of approximately 240MB. The total testing process is configured to last around 5 minutes.

```bash
cd [Web3Bench.dir]/script
# Load the data (including executing config.sh, building the project, generating data and loading data)
# config.sh: modify the configuration files according to the values in config.sh to match your environment and then create the database
./loaddata.sh

# Execute Web3Bench
./runbench.sh

# Parse the results
python3 parse.py
# The outcomes will be saved in script/summary.csv by default
# Or if you want to import the results into the database, you can use the following command
python3 parse.py --exportdb sum
# The sum results will be stored in script/summary.csv and the sum_table table in the database you specified in the script parse.py
# Or if you want to import the all results (including the original data and the sum of the data) into the database, you can use the following command
python3 parse.py --exportdb all
# The sum results will be stored in script/summary.csv, the res_table table (storing the original data) and sum_table table in the database you specified in the script parse.py
```

### Troubleshooting

If you encounter issues during the process, refer to the following troubleshooting guide for assistance.

- Issue: `error: unmappable character for encoding ASCII` when running `ant build` in `./loaddata.sh`
    - Solution: set the environment variable `JAVA_TOOL_OPTIONS` to `-Dfile.encoding=UTF8`
        ```bash
        export JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF8
        ```


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

### Quick Configuration

- script/config.sh
    - The script for modifying the configuration files and creating the database
    - To adjust database settings such as name, access credentials, scaling factor, test duration, etc., edit the top section of the script:
        ```shell
        ###########################################################
        # Database type: mysql, tidb, or sdb (singlestoredb)
        dbtype=tidb
        ###########################################################
        # IP address of the database server
        new_ip='127.0.0.1'
        new_port=4000
        new_dbname=web3bench
        # Notice: add \ before & in the jdbc url
        new_dburl="jdbc:mysql://$new_ip:$new_port/$new_dbname?useSSL=false\&amp;characterEncoding=utf-8"
        new_username=root
        new_password=
        new_nodeid="main"
        new_scalefactor=6000
        # Test time in minutes
        new_time=60
        # terminals and rate for runthread1.xml
        new_terminals_thread1=5
        new_rate_thread1=300
        # terminals and rate for runR2*.xml
        new_terminals_R2x=1
        new_rate_R2x=1
        ###########################################################
        ```
    - Usage:
        ```bash
        cd script
        ./config.sh
        ```
### Configuration files
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
Web3Bench utilizes a configuration file to facilitate the execution of a designated benchmark. 
The workload descriptor (or configuration file) serves to furnish essential details for database access, such as the driver, URL, credentials, and other pertinent information. Additionally, it accommodates benchmark-specific preferences and, notably, the composition of the workload mixture.

When running a multi-phase experiment with varying a workload, it is imperative to include multiple sections within the configuration file. Each section should encompass details about the duration, rate, and weight of each transactions.

> Note: weights have to sum up to 100%. The transactions are listed in the benchmark specific section labeled "transactiontypes". The order in which the transactions are declared is the same as their respective weights.

- **DBUrl**: the URL to access the database
- **username**: the username to access the database
- **password**: the password to access the database
- **scalefactor**: the scale factor for loading data. When saclefactor = 1, the data size is around 80MB.
- **time**: the duration of the workload in minutes
- **rate**: the sending rate of the workload in transactions per minute
- **weights**: the weights of the transactions in the workload. The sum of the weights should be 100.
- **transactiontypes**: the list of transactions in the workload. The order of the transactions should be the same as the order of the weights.

```xml
<?xml version="1.0"?>
<parameters>
    <!-- Connection details -->
    <dbtype>mysql</dbtype>
    <driver>com.mysql.cj.jdbc.Driver</driver>
    <DBUrl>jdbc:mysql://127.0.0.1:4000/web3bench?useSSL=false&amp;characterEncoding=utf-8</DBUrl>
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
            <time>5</time>
            <rate>1</rate>
            <weights>100</weights>
            <arrival>REGULAR</arrival>
        </work>
    </works>

    <transactiontypes>
        <transactiontype>
            <name>R21</name>
        </transactiontype>
    </transactiontypes>
</parameters>
```
## Run Web3Bench

### Load data
```bash
cd script; nohup ./loaddata.sh &
```

### Execute Web3Bench
```bash
cd script; nohup ./runbench.sh &
```

If you want to dump all the query statements executed by Web3Bench, you can add the option `--trace` to the command line. And then all the query statements, results and affected rows (of inserts, updates and deletes) will be recorded in `./log/benchmark.log`.
```bash
./runbench.sh --trace
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
- script/parse.py
    - Usage:
        ```bash
        $ python parse.py -h
        usage: parse.py [-h] [--datadir DATADIR] [--exportcsv EXPORTCSV] [--exportdb EXPORTDB]
                        [--testtime TESTTIME]

        optional arguments:
        -h, --help            show this help message and exit
        --datadir DATADIR     The directory of the original csv data files. If it is empty, the script
                                will parse all the csv files in the results directory. If it is not empty,
                                the script will parse all the csv files in ../results/<datadir>
        --exportcsv EXPORTCSV
                                The name of the exported csv file, default is summary. The name of the
                                exported csv file is <exportcsv>.csv, and it will be exported to the
                                current directory
        --exportdb EXPORTDB   If it is empty, the script will not export the data to the MySQL database.
                                If it is not empty, the script will export the data to the MySQL database.
                                The value of --exportdb can be sum or all. sum: export the sum of the data
                                to the MySQL database. all: export all the data (including the original
                                data and the sum of the data) to the MySQL database
        --testtime TESTTIME   The test time in minutes, default is 0. If it is 0, the script will get the
                                test time from the xml config file. If it is not 0, the script will use the
                                value as the test time
        ```
    - The script will parse all the csv files in the `results/` dictionary by default. 
        - If you want to parse the csv files in another directory, you can use the option `--datadir` to specify the directory.
    - The script will export the sum of the data to the summary.csv file by default. 
        - If you want to export the sum of the data to another csv file, you can use the option `--exportcsv` to specify the name of the exported csv file.
    - The script will not export the data to the MySQL database by default. 
        - If you want to export the data to the MySQL database, you can use the option `--exportdb` to specify the value of the option. The value of `--exportdb` can be `sum` or `all`. 
            - `sum`: export the sum of the data to the MySQL database. 
                - The sum results will be stored in `summary.csv` file and the `sum_table` table in the database you specified in the script `parse.py`.
                    ```sql
                    CREATE TABLE IF NOT EXISTS sum_table (
                        txn_name                    VARCHAR(10) PRIMARY KEY,
                        total_latency_s             DECIMAL(20, 6),
                        txn_count                   BIGINT,
                        average_latency_s           DECIMAL(20, 6),
                        p99_latency_s               DECIMAL(20, 6),
                        qps                         DECIMAL(20, 6),
                        tps                         DECIMAL(20, 6),
                        geometric_mean_latency_s    DECIMAL(20, 6),
                        avg_latency_limit_s         VARCHAR(10),
                        pass_fail                   VARCHAR(10)
                    );
                    ```
            - `all`: export all the data (including the original data and the sum of the data) to the MySQL database. 
                - `res_table` table (storing the original data) and `sum_table` table (storing the sum of the data) will be created in the MySQL database.
                    ```sql
                    CREATE TABLE IF NOT EXISTS res_table (
                        id              BIGINT AUTO_INCREMENT PRIMARY KEY,
                        hostname        VARCHAR(30),
                        txn_type_index  BIGINT,
                        txn_name        VARCHAR(10),
                        start_time_ms   DECIMAL(20, 6),
                        latency_ms      BIGINT,
                        worker_id       INT,
                        phase_id        INT
                    );
                    ```
            - Specify the database settings at the top of the script `parse.py`:
                ```python
                # MySQL database connection configuration
                db_config = {
                    "host": "127.0.0.1",    # Replace with your MySQL host name
                    "port": 4000,           # Replace with your MySQL port
                    "user": "root",         # Replace with your database username
                    "password": "",         # Replace with your database password
                    "database": "web3bench" # Replace with your database name
                }
                ```

## Implementation

### Tables
```sql
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
  block_number bigint,
  next_block_number bigint
);

-- Add indexes
CREATE INDEX idx_blocks_number ON blocks (number);
-- for R23
CREATE INDEX idx_fa_bn ON token_transfers (from_address, block_number);
-- for R24, R25
CREATE INDEX idx_ta_bn_nbn ON token_transfers (token_address, block_number, next_block_number);

-- Add foreign keys
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
    - R21: List of transactions excluding some black listed ones.
    ```sql
    Select count(*) 
    from transactions 
    where to_address not in (?, ?, ?)
    ```
    - R22: Constraint checking that next_block_number <= block_number in token_transfers Query result should be empty.
    ```sql
    Select count(*)
    from token_transfers 
    where next_block_number <= block_number 
    group by next_block_number
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
            values (?, ?, ?, ?, ?, ?, ?)
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
