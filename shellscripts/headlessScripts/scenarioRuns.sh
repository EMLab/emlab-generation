USAGE="Provide name of run and number of runs"
#Load configuration script to substitute
if [ -f scriptConfigurations.cfg ];then 
	. scriptConfigurations.cfg
	HOME=$REMOTERESULTFOLDER
else
    echo "Define scriptConfigurations.cfg, by changing the template. Exiting script."
    exit
fi

#RUNS=(14 21 26 32 38  72 84 108 111 120 132 156 160 162 164 168 180)

#for i in "${RUNS[@]}"
for i in {1..40}
do
    FILEENDING=".xml"
    SCENARIO="scenario"-$i
    $REMOTEHPCSCRIPTS/hpcArrayRun.sh $SCENARIO 5 $SCENARIO$FILEENDING
    sleep 15s
    #echo $SCENARIO
    #echo $SCENARIO$FILEENDING
done