#!/bin/bash

set -e
set -x
# modify the config
./config.sh

# Build the project
./build.sh

# Navigate to the project directory
cd ..

# Start loading data
./olxpbenchmark -b web3bench -c config/loaddata.xml --load=true --create=true | tee log/loaddata.log

set +x
set +e
