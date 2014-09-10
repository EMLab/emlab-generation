#!/bin/bash

## This script can be used to call the python headless reader scripts.
## It uses the automatic extensions of bash. Thus if the files to read
## are in ~/example/run-1 and ~/example/run-2 , this script can be called
## with ./headlessTableReader.sh ~/example/run-* tableName

firstarray=( "$@" )
#echo ${firstarray[@]}
length=$(($#-1))
tableName=${firstarray[$(($#-1))]}
unset firstarray[$(($#-1))]

for par in "${firstarray[@]}"
do
    if [[ "$par" == *.csv ]]
    then
	echo "Skipping"
	continue
    fi
	echo "python asHeadlessTableReader.py "$par"/" $tableName
	python asHeadlessTableReader.py $par"/" $tableName
done