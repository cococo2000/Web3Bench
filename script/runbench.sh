#!/bin/bash

dump_dml="INFO"
# Check if there is an argument == --trace
if [ $# -eq 1 ] && [ $1 == "--trace" ]; then
    dump_dml="TRACE"
fi

# Check the operating system
if [ "$(uname)" == "Darwin" ]; then
    # macOS
    SED_INPLACE_OPTION="-i ''"
else
    # Linux or other Unix-like OS
    SED_INPLACE_OPTION="-i"
fi

# Modify log4j.properties based on dump_dml value
case $dump_dml in
    INFO)
        sed $SED_INPLACE_OPTION 's/log4j.logger.com.olxpbenchmark.benchmarks.web3benchmark.procedures=.*/log4j.logger.com.olxpbenchmark.benchmarks.web3benchmark.procedures=INFO, A2/' ../log4j.properties
        ;;
    TRACE)
        sed $SED_INPLACE_OPTION 's/log4j.logger.com.olxpbenchmark.benchmarks.web3benchmark.procedures=.*/log4j.logger.com.olxpbenchmark.benchmarks.web3benchmark.procedures=TRACE, A2/' ../log4j.properties
        ;;
    *)
        echo "Unknown dump_dml value: $dump_dml"
        exit 1
        ;;
esac

echo "Dump DML: $dump_dml"
echo "log4j.properties updated"

# Delete old results in ../results directory after 10s
if [ -d "../results" ]; then
    echo "Deleting old results in ../results directory after 10s"
    sleep 10
    rm -rf ../results/*.*
fi

# modify the config
./config.sh

# Build the project
./build.sh

# Navigate to the project directory
cd ..

./olxpbenchmark -b web3bench --explain-analyze=true -c config/runthread1.xml --execute=true -o thread1  | tee log/thread1.log &
./olxpbenchmark -b web3bench --explain-analyze=true -c config/runthread2.xml --execute=true -o thread2  | tee log/thread2.log &
./olxpbenchmark -b web3bench --explain-analyze=true -c config/runR21.xml --execute=true -o R21  | tee log/R21.log &
./olxpbenchmark -b web3bench --explain-analyze=true -c config/runR22.xml --execute=true -o R22  | tee log/R22.log &
./olxpbenchmark -b web3bench --explain-analyze=true -c config/runR23.xml --execute=true -o R23  | tee log/R23.log &
./olxpbenchmark -b web3bench --explain-analyze=true -c config/runR24.xml --execute=true -o R24  | tee log/R24.log &
./olxpbenchmark -b web3bench --explain-analyze=true -c config/runR25.xml --execute=true -o R25  | tee log/R25.log

wait
