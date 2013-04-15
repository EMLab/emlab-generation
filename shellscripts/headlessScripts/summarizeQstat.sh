#!/bin/bash

qstat > qstatInfo.txt
numRunning=`cat qstatInfo.txt | grep " R " | wc -l`
numQueued=`cat qstatInfo.txt | grep " Q " | wc -l`
numErrors=`cat qstatInfo.txt | grep " E " | wc -l`
echo "num Queued"
echo $numQueued
echo "num Running"
echo $numRunning
echo "num Errors"
echo $numErrors

echo "number of jobs per user:"
sed 1,2d qstatInfo.txt | awk '{print $3}' | sort | uniq -c
