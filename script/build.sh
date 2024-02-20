#!/bin/bash

# Navigate to the project directory
cd ..

# Bootstrap the project using Ant
ant bootstrap
# Resolve the dependencies
ant resolve
# Clean the project
ant clean
# Build the project
ant build
