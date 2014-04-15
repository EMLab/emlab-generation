#!/bin/bash

## This script can be used to call the python headless reader scripts.
## It uses the automatic extensions of bash. Thus if the files to read
## are in ~/example/run-1 and ~/example/run-2 , this script can be called
## with ./headlessQueryReader.sh ~/example/run-*

for par in "$@"
do
echo $par"/"
./asHeadlessQueryReader.py $par"/"
done