USAGE="Provide name of run and number of runs"
#Load configuration script to substitute
if [ -f scriptConfigurations.cfg ];then 
	. scriptConfigurations.cfg
	HOME=$REMOTERESULTFOLDER
else
    echo "Define scriptConfigurations.cfg, by changing the template. Exiting script."
    exit
fi

#Alternative way to define non sequential scenario runs.
#RUNS=(14 21 26 32 38  72 84 108 111 120 132 156 160 162 164 168 180)

RUNNAME=$1
SCENARIONAME=$2
NUMBERROFRUNSPERSCENARIO=$3
START=$4
END=$5
PAUSE=$6
WALLTIME=$7

#for i in "${RUNS[@]}"
for i in $(eval echo "{$START..$END}")
do
    SCENARIO=$SCENARIONAME"-$i"
    $REMOTEHPCSCRIPTS/hpcArrayRun.sh $RUNNAME $RUNNAME"-$i" $SCENARIO".xml" $NUMBERROFRUNSPERSCENARIO $WALLTIME
    sleep $6
done