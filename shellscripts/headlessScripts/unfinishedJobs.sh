#!/bin/bash
#Dir that is checked for the simulation files.
HOME=~/outputD13n
JOBNAME=$1
NROFRUNS=$2
DIR=$HOME/$JOBNAME

rm unfinishedJobs.txt

for ((i=1; i<=NROFRUNS; i++)) ; do
    if [ ! -e $DIR/$JOBNAME-$i.log ]; then
	echo -e $i',\c' >> unfinishedJobsTmp.txt
    fi
done

sed 's/,$//' <unfinishedJobsTmp.txt >unfinishedJobs.txt
rm unfinishedJobsTmp.txt