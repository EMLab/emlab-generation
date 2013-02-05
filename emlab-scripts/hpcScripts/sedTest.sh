INPUTPARAMETERFOLDER=~/emlab/emlab-model/src/main/resources/scenarios/
SCENARIO=scenarioE-MinCO2-resTarget.xml

PARAMETERFILE=$(grep "classpath:scenarios" <$INPUTPARAMETERFOLDER/$SCENARIO | sed 's/[^:]*:scenarios\/\([^"]*\).*/\1/')

echo $PARAMETERFILE