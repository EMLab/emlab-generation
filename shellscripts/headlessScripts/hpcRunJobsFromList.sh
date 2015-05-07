USAGE="Provide name of run and number of runs"
#Load configuration script to substitute
if [ -f scriptConfigurations.cfg ];then 
	. scriptConfigurations.cfg
	HOME=$REMOTERESULTFOLDER
else
    echo "Define scriptConfigurations.cfg, by changing the template. Exiting script."
    exit
fi

# Takes a list of scenario files as an input: ./hpcRunJobsFromList.sh list.txt
# list can be created by going into the scenario folder (defined in scriptConfigruations as REMOTESCENARIOFOLDER) and saving all (ls *.xml > list.txt), or a subset of XML files (ls somePattern*secondPattern*.xml > list.txt) to a file. It is important that the file names in the list are without any folder names or starting with "./" .

FILE=$1
NUMBERROFRUNSPERSCENARIO=1
PAUSE="2s"
WALLTIME=08:00:00

numberOfRunIds=$(cat $FILE | wc -l)
echo "Running "$numberOfRunIds" runIds."
no=0

#for i in "${RUNS[@]}"
for runId in $(cat $FILE)
do
#    SCENARIO=$SCENARIONAME"-$i"
 #   $REMOTEHPCSCRIPTS/hpcArrayRun.sh $RUNNAME $RUNNAME"-$i" $SCENARIO".xml" $NUMBERROFRUNSPERSCENARIO $WALLTIME
#$RUNNAME $RUNNAME"-$i" $SCENARIO".xml" $NUMBERROFRUNSPERSCENARIO $WALLTIME 
#echo $runId
runId=$(echo $runId | sed 's/.xml$//')
#echo $runId
RUNNAME=$(echo $runId | sed 's/-[0-9]*$//')
#SCENARIONAME=$(echo $runId | sed 's/^MWC/MWB/')".xml"
SCENARIONAME=$runId".xml"
#echo $RUNNAME
#echo $SCENARIONAME
echo  "$REMOTEHPCSCRIPTS/hpcArrayRun.sh $RUNNAME $runId $SCENARIONAME $NUMBERROFRUNSPERSCENARIO $WALLTIME"
$REMOTEHPCSCRIPTS/hpcArrayRun.sh $RUNNAME $runId $SCENARIONAME $NUMBERROFRUNSPERSCENARIO $WALLTIME

sleep $PAUSE
$((no++))
echo $no

if [ $no -eq 120 ] 
then
    no=0
    echo "Taking 1min break"
    sleep 1m
fi
done

