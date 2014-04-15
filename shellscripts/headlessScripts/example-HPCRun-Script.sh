policyScenarios=(MinGB )
fuelPriceScenarios=(FuelCentral )
resScenarios=(FRES )
centralCO2BackSmoothingScenarios=(B0 )
centralPrivateDiscountingRateScenarios=(D50 )
co2MaximumPriceScenarios=(C500 )
co2PriceFloorScenarios=("S185-SL230" )
#co2PriceFloorScenarios=("S50-SL75" "S75-SL100" "S100-SL150" "S185-SL230" )
#co2PriceFloorScenarios=("S100-SL150" "S185-SL230" )
bankingReversionFactorScenarios=(R3 )



noOfRepitions=1
startNo=1
stopNo=120
maximumRunLength=04:00:00
pauseBetweenScenarios="5m"
pauseBetweenRuns="2s"

scenarioCountMaximum=0
for centralPrivateDiscountingRateScenario  in "${centralPrivateDiscountingRateScenarios[@]}"
do
    for centralCO2BackSmoothingScenario  in "${centralCO2BackSmoothingScenarios[@]}"
    do
	for bankingReversionFactorScenario in "${bankingReversionFactorScenarios[@]}"
	do
	    for co2MaximumPriceScenario in "${co2MaximumPriceScenarios[@]}"
	    do
		for resScenario  in "${resScenarios[@]}"
		do
		    for fuelPriceScenario  in "${fuelPriceScenarios[@]}"
		    do
			for policyScenario  in "${policyScenarios[@]}"
			do
			    for co2PriceFloorScenario in "${co2PriceFloorScenarios[@]}"
			    do
				scenarioCountMaximum=$((scenarioCountMaximum+1))
			    done
			done
		    done
		done
	    done
	done
    done
done

echo "Expected runtime $((scenarioCountMaximum*400/60))h"

scenarioCount=0
for centralPrivateDiscountingRateScenario  in "${centralPrivateDiscountingRateScenarios[@]}"
do
    for centralCO2BackSmoothingScenario  in "${centralCO2BackSmoothingScenarios[@]}"
    do
	for bankingReversionFactorScenario in "${bankingReversionFactorScenarios[@]}"
	do
	    for co2MaximumPriceScenario in "${co2MaximumPriceScenarios[@]}"
	    do
		for resScenario  in "${resScenarios[@]}"
		do
		    for fuelPriceScenario  in "${fuelPriceScenarios[@]}"
		    do
			for policyScenario  in "${policyScenarios[@]}"
			do
			    for co2PriceFloorScenario in "${co2PriceFloorScenarios[@]}"
			    do
				if [[ $policyScenario == PureETS ]];then
				    echo $policyScenario
				    co2PriceFloorScenario="S0-SL0"
				else
				    echo "Not PureETS"
				fi
				echo "./scenarioRuns.sh "MWC-"$policyScenario"-"$co2PriceFloorScenario"-"$resScenario"-"$fuelPriceScenario"-"$centralPrivateDiscountingRateScenario"-"$centralCO2BackSmoothingScenario"-"$bankingReversionFactorScenario"-"$co2MaximumPriceScenario "MWB-"$policyScenario"-"$co2PriceFloorScenario"-"$resScenario"-"$fuelPriceScenario"-"$centralPrivateDiscountingRateScenario"-"$centralCO2BackSmoothingScenario"-"$bankingReversionFactorScenario"-"$co2MaximumPriceScenario $noOfRepitions $startNo $stopNo $pauseBetweenRuns $maximumRunLength"
 ./scenarioRuns.sh "MWC-"$policyScenario"-"$co2PriceFloorScenario"-"$resScenario"-"$fuelPriceScenario"-"$centralPrivateDiscountingRateScenario"-"$centralCO2BackSmoothingScenario"-"$bankingReversionFactorScenario"-"$co2MaximumPriceScenario "MWB-"$policyScenario"-"$co2PriceFloorScenario"-"$resScenario"-"$fuelPriceScenario"-"$centralPrivateDiscountingRateScenario"-"$centralCO2BackSmoothingScenario"-"$bankingReversionFactorScenario"-"$co2MaximumPriceScenario $noOfRepitions $startNo $stopNo $pauseBetweenRuns $maximumRunLength
				scenarioCount=$((scenarioCount+1))
				echo $((100*scenarioCount/scenarioCountMaximum))"% of scenarios started."
			    sleep $pauseBetweenScenarios
				echo "Stopped waiting"
			    done
			done
		    done
		done
	    done
	done
    done
done


#Standard-scenario
policyScenarios=(MinCWE BothMin BothMinBothMax)
fuelPriceScenarios=(FuelCentral )
resScenarios=(FRES )
centralCO2BackSmoothingScenarios=(B0 )
centralPrivateDiscountingRateScenarios=(D50 )
co2MaximumPriceScenarios=(C500 )
co2PriceFloorScenarios=("S75-SL100" )
#co2PriceFloorScenarios=("S50-SL75" "S75-SL100" "S100-SL150" "S185-SL230" )
#co2PriceFloorScenarios=("S100-SL150" "S185-SL230" )
bankingReversionFactorScenarios=(R3 )



noOfRepitions=1
startNo=1
stopNo=120
maximumRunLength=04:00:00
pauseBetweenScenarios="5m"
pauseBetweenRuns="2s"

scenarioCountMaximum=0
for centralPrivateDiscountingRateScenario  in "${centralPrivateDiscountingRateScenarios[@]}"
do
    for centralCO2BackSmoothingScenario  in "${centralCO2BackSmoothingScenarios[@]}"
    do
	for bankingReversionFactorScenario in "${bankingReversionFactorScenarios[@]}"
	do
	    for co2MaximumPriceScenario in "${co2MaximumPriceScenarios[@]}"
	    do
		for resScenario  in "${resScenarios[@]}"
		do
		    for fuelPriceScenario  in "${fuelPriceScenarios[@]}"
		    do
			for policyScenario  in "${policyScenarios[@]}"
			do
			    for co2PriceFloorScenario in "${co2PriceFloorScenarios[@]}"
			    do
				scenarioCountMaximum=$((scenarioCountMaximum+1))
			    done
			done
		    done
		done
	    done
	done
    done
done

echo "Expected runtime $((scenarioCountMaximum*400/60))h"

scenarioCount=0
for centralPrivateDiscountingRateScenario  in "${centralPrivateDiscountingRateScenarios[@]}"
do
    for centralCO2BackSmoothingScenario  in "${centralCO2BackSmoothingScenarios[@]}"
    do
	for bankingReversionFactorScenario in "${bankingReversionFactorScenarios[@]}"
	do
	    for co2MaximumPriceScenario in "${co2MaximumPriceScenarios[@]}"
	    do
		for resScenario  in "${resScenarios[@]}"
		do
		    for fuelPriceScenario  in "${fuelPriceScenarios[@]}"
		    do
			for policyScenario  in "${policyScenarios[@]}"
			do
			    for co2PriceFloorScenario in "${co2PriceFloorScenarios[@]}"
			    do
				if [[ $policyScenario == PureETS ]];then
				    echo $policyScenario
				    co2PriceFloorScenario="S0-SL0"
				else
				    echo "Not PureETS"
				fi
				echo "./scenarioRuns.sh "MWC-"$policyScenario"-"$co2PriceFloorScenario"-"$resScenario"-"$fuelPriceScenario"-"$centralPrivateDiscountingRateScenario"-"$centralCO2BackSmoothingScenario"-"$bankingReversionFactorScenario"-"$co2MaximumPriceScenario "MWB-"$policyScenario"-"$co2PriceFloorScenario"-"$resScenario"-"$fuelPriceScenario"-"$centralPrivateDiscountingRateScenario"-"$centralCO2BackSmoothingScenario"-"$bankingReversionFactorScenario"-"$co2MaximumPriceScenario $noOfRepitions $startNo $stopNo $pauseBetweenRuns $maximumRunLength"
 ./scenarioRuns.sh "MWC-"$policyScenario"-"$co2PriceFloorScenario"-"$resScenario"-"$fuelPriceScenario"-"$centralPrivateDiscountingRateScenario"-"$centralCO2BackSmoothingScenario"-"$bankingReversionFactorScenario"-"$co2MaximumPriceScenario "MWB-"$policyScenario"-"$co2PriceFloorScenario"-"$resScenario"-"$fuelPriceScenario"-"$centralPrivateDiscountingRateScenario"-"$centralCO2BackSmoothingScenario"-"$bankingReversionFactorScenario"-"$co2MaximumPriceScenario $noOfRepitions $startNo $stopNo $pauseBetweenRuns $maximumRunLength
				scenarioCount=$((scenarioCount+1))
				echo $((100*scenarioCount/scenarioCountMaximum))"% of scenarios started."
			    sleep $pauseBetweenScenarios
				echo "Stopped waiting"
			    done
			done
		    done
		done
	    done
	done
    done
done

