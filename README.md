# Web3Bench

# Dependencies
- Java (+1.7)
- Apache Ant
- Ubuntu (+16.04)

## Build

```bash
cd [Web3Bench.dir]
ant bootstrap
ant resolve
ant build
```

## Config

### Configuration Directory
- config/*
    - Configure files for testing on the VM


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
mysql -h <hostname> -u <username> -p<password> -P <port>

# After connecting, create a new database by executing the following SQL command
CREATE DATABASE <your_database_name>;
```
Replace <your_database_name> with the desired name for your database. Make sure to replace it with a valid database name that you want to use. This command will create a new database with the specified name.


#### Check Configuration File

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
- **--create=true**: create the database schema by excuting the SQL script in ddl file(e.g., src/com/olxpbenchmark/benchmarks/web3benchmark/ddls/web3benchmark-mysql-ddl.sql)
- **--load=true**: load data into the database
- **--execute=true**: run the workload

## Source Code
- src/com/olxpbenchmark/benchmarks/web3benchmark/Web3Loader.java
    - The data loader for web3benchmark
- src/com/olxpbenchmark/benchmarks/web3benchmark/procedures/*
    - The workloads for web3bench

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
