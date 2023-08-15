#!/bin/bash

cd ..

./olxpbenchmark -b web3benchmark -c config/thread1.xml  --execute=true -o thread1  | tee log/thread1.log &
./olxpbenchmark -b web3benchmark -c config/thread2.xml  --execute=true -o thread2  | tee log/thread2.log &
./olxpbenchmark -b web3benchmark -c config/R21.xml  --execute=true -o R21  | tee log/R21.log &
./olxpbenchmark -b web3benchmark -c config/R22.xml  --execute=true -o R22  | tee log/R22.log &
./olxpbenchmark -b web3benchmark -c config/R23.xml  --execute=true -o R23  | tee log/R23.log &
./olxpbenchmark -b web3benchmark -c config/R24.xml  --execute=true -o R24  | tee log/R24.log
