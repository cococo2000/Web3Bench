#!/bin/bash

cd ..

ant clean

ant build

./olxpbenchmark -b web3benchmark -c config/loadtest-scale3.xml --load=true  --create=true | tee log/loadertest-scale3.log
