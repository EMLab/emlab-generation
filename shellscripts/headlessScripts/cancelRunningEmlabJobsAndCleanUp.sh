#!/bin/bash
SERVER=hpc07
if [ -f scriptConfigurations.cfg ];then 
	. scriptConfigurations.cfg
else
    echo "Define scriptConfigurations.cfg, by changing the template. Exiting script."
    exit
fi

if [ -z $1 ]; then
    echo -n 'What is the name of the jobarray you want to cancel? (Alternatively give name as bash argument).'
    read JOBNAME
else
    JOBNAME=$1
fi

JOBID=$(qstat | grep $JOBNAME | sed 's/\([1-9][0-9]*\).*/\1/')

qstat -n1t | grep $JOBNAME | sed 's/.*\[\([1-9][0-9]*\)].*/\1/' > failedNos
qstat -n1t | grep $JOBNAME | sed 's/.*\('$JOBNAME'-[1-9][0-9]*\).*/\1/' > failedRunIds
qstat -n1t | grep $JOBNAME | sed 's/.*\('$JOBNAME'-[1-9][0-9]*\).*/\1/' > failedRunIds

for failedNo in $(cat failedNos); do
    JOBARRAYID=''$JOBID'['$failedNo'].'$SERVER''
    NODE=$(qstat -nt1 ''$JOBID'['$failedNo'].'$SERVER'' | grep $JOBNAME |  sed 's/.*\(n07-[0-9][0-9]\)\/[1-9]*$/\1/')
    echo 'Deleting '$JOBARRAYID' on node '$NODE'!'
    qdel $JOBARRAYID
    sleep 5s
    qsub -lnodes=$NODE -v JOBNAME=$JOBNAME,ARRAYID=$failedNo $REMOTEHPCSCRIPTS/deleteD13nFilesForSingleJobId.sh&
done