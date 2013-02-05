#!/bin/bash

########################################################################
# The jobname must only consist of the characters A-Z, a-z and 0-9!!!! #
########################################################################
USAGE="Provide name of run and number of runs"
#Load configuration script to substitute
if [ -f scriptConfigurations.cfg ];then 
	. scriptConfigurations.cfg
	HOME=$REMOTERESULTFOLDER
else
    echo "Define scriptConfigurations.cfg, by changing the template. Exiting script."
    exit
fi
TEMP="/var/tmp"
RAMDISK="/tmp/ramdisk"
D13NDB="emlab-db"
#SCENARIO=scenarioE-MinCO2-resTarget.xml

JARNAME=$JARFILE
#Make sure your in the right directory
cd $HOME

if [ $# -lt 2 ]  
then
   echo "$USAGE"
   exit 0
fi 

JOBNAME=$1
NROFRUNS=$2
SCENARIO=$3
#######################
#Creating output folders
STREAMOUTPUT=$HOME/$JOBNAME/streamOutput
INPUTPARAMETERFOLDER=$HOME/$JOBNAME/inputParameter
mkdir $HOME/$JOBNAME
#mkdir $HOME/$JOBNAME/results
mkdir $STREAMOUTPUT
mkdir $INPUTPARAMETERFOLDER
cp $JARFILE $HOME/
#rm $JARFILE
######################
cp $REMOTESCENARIOFOLDER/$SCENARIO $INPUTPARAMETERFOLDER/
PARAMETERFILE=$(grep "classpath:scenarios" <$INPUTPARAMETERFOLDER/$SCENARIO | sed 's/[^:]*:scenarios\/\([^"]*\).*/\1/')
cp $REMOTESCENARIOFOLDER/$PARAMETERFILE $INPUTPARAMETERFOLDER/

######################
#Starting Simulation

MD=$(md5sum $HOME/$JARNAME | sed 's/ /_/g')

echo "$MD"

   #Start the set of jobs.
qsub -t 1-$NROFRUNS -N $JOBNAME -l nodes=1:ppn=2,mem=4000mb,walltime=04:00:01,epilogue=$REMOTEHPCSCRIPTS/epilogueHpc.sh -o $STREAMOUTPUT -e $STREAMOUTPUT -v JOBNAME=$JOBNAME,JARNAME=$JARFILE,SCENARIO=$SCENARIO,TEMP=$TEMP,RAMDISK=$RAMDISK,MD=$MD,HOME=$HOME,D13NDB=$D13NDB,INPUTPARAMETERFOLDER=$INPUTPARAMETERFOLDER,PARAMETER=$PARAMETER $REMOTEHPCSCRIPTS/startASingleArrayJobOnNode.sh
   echo "Started all jobs."


