# This is an example script how to do read-in parameter sweeps
#
#
# It corresponds to the rscripts/exampleScenarioCreater.R file, which
# creates the necessary scenario files and to the 
# example-priceCapRuns-InterestSensitivity.sh which runs the scenarios
# on the HPC.
#
#Load configuration script to substitute
if [ -f scriptConfigurations.cfg ];then 
	. scriptConfigurations.cfg
	HOME=$REMOTERESULTFOLDER
else
    echo "Define scriptConfigurations.cfg, by changing the template. Exiting script."
    exit
fi


policyScenarios=(PureETS MinGB MinCWE BothMin BothMinBothMax)
fuelPriceScenarios=( DeccCentral )
resScenarios=( FRES )
interestScenarios=( mediumInterest lowInterest highInterest  )


scenarioCountMaximum=0
for resScenario  in "${resScenarios[@]}"
do
    for fuelPriceScenario  in "${fuelPriceScenarios[@]}"
    do
	for policyScenario  in "${policyScenarios[@]}"
	do
	    for interestScenario in "${interestScenarios[@]}"
	    do
		scenarioCountMaximum=$((scenarioCountMaximum+1))
      	    done
        done
    done
done

scenarioCount=0
for interestScenario in "${interestScenarios[@]}"
do
    for resScenario  in "${resScenarios[@]}"
    do
	for fuelPriceScenario  in "${fuelPriceScenarios[@]}"
	do
	    for policyScenario  in "${policyScenarios[@]}"
	    do
	    echo "python "../rscripts/asHeadlessQueryReader.py" "~/emlabGen/output/" "I-"$policyScenario"-"$interestScenario"
	    python asHeadlessQueryReader.py ~/emlabGen/output/ "I-"$policyScenario"-"$interestScenario
	    scenarioCount=$((scenarioCount+1))
	    echo $((100*scenarioCount/scenarioCountMaximum))"% of scenarios read in."
	    echo "Stopped waiting"
      	    done
	done
    done
done

