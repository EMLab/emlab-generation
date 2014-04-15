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
EMLABDB="emlab.gen-db"

chmod go+x $REMOTEHPCSCRIPTS
chmod go+x $REMOTEHPCSCRIPTS/epilogueHpc.sh

#Make sure your in the right directory
cd $HOME

if [ $# -lt 2 ]  
then
   echo "$USAGE"
   exit 0
fi 

RUNNAME=$1
JOBNAME=$2
SCENARIO=$3
NROFRUNS=$4
WALLTIME=$5
#######################
#Creating output folders
STREAMOUTPUT=$HOME/$RUNNAME/streamOutput
INPUTPARAMETERFOLDER=$HOME/$RUNNAME/inputParameter
if [ ! -e $HOME/$RUNNAME ];then
mkdir $HOME/$RUNNAME
#mkdir $HOME/$JOBNAME/results
mkdir $STREAMOUTPUT
mkdir $INPUTPARAMETERFOLDER
#cp $REMOTED13N/target/$JARFILE $HOME/
#rm $JARFILE
fi
######################
cp $REMOTESCENARIOFOLDER/$SCENARIO $INPUTPARAMETERFOLDER/
PARAMETERFILE=$(grep "classpath:scenarios" <$INPUTPARAMETERFOLDER/$SCENARIO | sed 's/[^:]*:scenarios\/\([^"]*\).*/\1/')
cp $REMOTESCENARIOFOLDER/$PARAMETERFILE $INPUTPARAMETERFOLDER/

######################
#Starting Simulation

MD=$(md5sum $HOME/$JARNAME | sed 's/\([a-z0-9]*\) .*/\1/g')

#echo "$MD"

   #Start the set of jobs.
if [ $NROFRUNS -gt 1 ]
then
qsub -t 1-$NROFRUNS -N $JOBNAME -l nodes=1:ppn=2,mem=3000mb,walltime=$WALLTIME,epilogue=$REMOTEHPCSCRIPTS/epilogueHpc.sh -o $STREAMOUTPUT -e $STREAMOUTPUT -v JOBNAME=$JOBNAME,RUNNAME=$RUNNAME,JARNAME=$JARNAME,SCENARIO=$SCENARIO,TEMP=$TEMP,RAMDISK=$RAMDISK,EMLABDB=$EMLABDB,MD=$MD,HOME=$HOME,INPUTPARAMETERFOLDER=$INPUTPARAMETERFOLDER,PARAMETER=$PARAMETER $REMOTEHPCSCRIPTS/startASingleArrayJobOnNode.sh
   echo "Started all job "$JOBNAME"."
else
qsub -N $JOBNAME -l nodes=1:ppn=2,mem=3000mb,walltime=$WALLTIME,epilogue=$REMOTEHPCSCRIPTS/epilogueHpc.sh -o $STREAMOUTPUT -e $STREAMOUTPUT -v JOBNAME=$JOBNAME,RUNNAME=$RUNNAME,JARNAME=$JARNAME,SCENARIO=$SCENARIO,TEMP=$TEMP,RAMDISK=$RAMDISK,EMLABDB=$EMLABDB,MD=$MD,HOME=$HOME,INPUTPARAMETERFOLDER=$INPUTPARAMETERFOLDER,PARAMETER=$PARAMETER $REMOTEHPCSCRIPTS/startASingleArrayJobOnNode.sh
   echo "Started all job "$JOBNAME"."
fi
