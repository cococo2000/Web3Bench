#!/bin/bash

set -e
set -x
# modify the config
./config.sh

# Navigate to the project directory
cd ..

ant bootstrap
ant resolve
ant clean
# Build the project
ant build
# Start loading data
./olxpbenchmark -b web3bench -c config/loaddata.xml --load=true --create=true | tee log/loaddata.log

set +x
set +e
