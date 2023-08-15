#!/bin/bash

cd ..

ant clean

ant build

./olxpbenchmark -b web3benchmark -c config/loaddata.xml --load=true  --create=true | tee log/loaddata.log
