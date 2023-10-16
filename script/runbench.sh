#!/bin/bash

# Delete old results in ../results directory after 10s
if [ -d "../results" ]; then
    echo "Deleting old results in ../results directory after 10s"
    sleep 10
    rm -rf ../results/*.*
fi

# modify the config
./config.sh

# Navigate to the project directory
cd ..

./olxpbenchmark -b web3bench -c config/runthread1.xml --execute=true -o thread1  | tee log/thread1.log &
./olxpbenchmark -b web3bench -c config/runthread2.xml --execute=true -o thread2  | tee log/thread2.log &
./olxpbenchmark -b web3bench -c config/runR21.xml --execute=true -o R21  | tee log/R21.log &
./olxpbenchmark -b web3bench -c config/runR22.xml --execute=true -o R22  | tee log/R22.log &
./olxpbenchmark -b web3bench -c config/runR23.xml --execute=true -o R23  | tee log/R23.log &
./olxpbenchmark -b web3bench -c config/runR24.xml --execute=true -o R24  | tee log/R24.log

wait
