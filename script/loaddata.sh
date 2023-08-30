#!/bin/bash

# Navigate to the project directory
cd ..

ant bootstrap
ant resolve
ant clean
# Build the project
ant build
# Start loading data
./olxpbenchmark -b web3benchmark -c config/loaddata.xml --load=true  --create=true | tee log/loaddata.log
