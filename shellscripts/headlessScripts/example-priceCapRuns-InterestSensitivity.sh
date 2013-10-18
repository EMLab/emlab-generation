# This is an example script how to run parameter sweeps on the HPC.
#
#
# It corresponds to the rscripts/exampleScenarioCreater.R file, which
# creates the necessary scenario files and to the 
# example-priceCapRuns-InterestSensitivity-readIn.sh which combines
#the files into several csv files.
#
#noOfRepitions sets the number of repitions per scenario file.
#startNo and stopNo correspond to the numbered scenario files.
#
# pauseBetweenScenarios and pauseBetweenRuns are used to ensure
# that not too many runs are submitted to the HPC at the same time.
# A rule of thumb is 
# pauseBetweenScenarios = ExpectedRunTimePerScenario * (stopNo-startNo)/30

policyScenarios=(PureETS MinGB MinCWE BothMin BothMinBothMax)
fuelPriceScenarios=( DeccCentral )
resScenarios=( FRES )
interestScenarios=( mediumInterest lowInterest highInterest  )

noOfRepitions=1
startNo=1
stopNo=120
maximumRunLength=03:00:00
pauseBetweenScenarios="200m"
pauseBetweenRuns="2s"

#This part simply calculates how many scenarios there are.
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

#This part starts the simulation. Refer to scenarioRuns.sh to understand
#the exact calling syntax.
scenarioCount=0
for interestScenario in "${interestScenarios[@]}"
do
    for resScenario  in "${resScenarios[@]}"
    do
	for fuelPriceScenario  in "${fuelPriceScenarios[@]}"
	do
	    for policyScenario  in "${policyScenarios[@]}"
	    do
	    echo "./scenarioRuns.sh "I-"$policyScenario"-"$fuelPriceScenario"-"$resScenario "scenarioI-"$policyScenario"-"$resScenario"-"$fuelPriceScenario"-demandCentral-"$interestScenario $noOfRepitions $startNo $stopNo $pauseBetweenRuns $maximumRunLength"
	    ./scenarioRuns.sh "I-"$policyScenario"-"$interestScenario "scenarioI-"$policyScenario"-"$resScenario"-"$fuelPriceScenario"-demandCentral-"$interestScenario $noOfRepitions $startNo $stopNo $pauseBetweenRuns $maximumRunLength
	    scenarioCount=$((scenarioCount+1))
	    echo $((100*scenarioCount/scenarioCountMaximum))"% of scenarios started."
	    sleep $pauseBetweenScenarios
	    echo "Stopped waiting"
      	    done
	done
    done
done

#This deletes renaming files on the nodes after the simulation.
./clearTmpAndVarTmpAllNodes.sh