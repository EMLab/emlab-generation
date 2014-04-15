#Placeholders

# Step 1 building the scenarios: insert dataframe and read the scenario file. Name parameters
# that need to be replaced with #
xmlFilePath<-"~/emlabGen/scenarios/scenarioU-pureETS-resTarget-run-template-interest-Sensitivity.xml"
filestump<-'scenarioI-'
# Step 2 building the scenarios: make separate data vectors
fuelPriceScenarioLength=1
microScenarioLength=120

coalPriceScenario=c("Coal.Medium")
gasPriceScenario=c("NaturalGas.Medium")
fuelPriceScenarios = c("DeccCentral")
demandGrowthScenarios = c("demandCentral")

microScenarioNo<-seq(1,microScenarioLength)


interestRateScenarios = list(mediumInterest=c("#equityInterest"="0.12","#debtInterest"="0.09"),
                             lowInterest=c("#equityInterest"="0.09","#debtInterest"="0.05"),
                             highInterest=c("#equityInterest"="0.15","#debtInterest"="0.12"))
#Fixed parameters:
co2PolicyScenarios=list(PureETS=c("#minCWEstart"="0","#minCWEincrement"="0",
                      "#minGBstart"="0","#minGBincrement"="0",
                      "#minCO2start"="0","#minCO2increment"="0",
                      "#maxCO2start"="500", "#maxCO2increment"="0"),
           MinCWE=c("#minCWEstart"="10","#minCWEincrement"="2",
                     "#minGBstart"="0","#minGBincrement"="0",
                     "#minCO2start"="0","#minCO2increment"="0",
                     "#maxCO2start"="500", "#maxCO2increment"="0"),
           MinGB=c("#minCWEstart"="0","#minCWEincrement"="0",
                      "#minGBstart"="10","#minGBincrement"="2",
                      "#minCO2start"="0","#minCO2increment"="0",
                      "#maxCO2start"="500", "#maxCO2increment"="0"),
           BothMin=c("#minCWEstart"="10","#minCWEincrement"="2",
                     "#minGBstart"="10","#minGBincrement"="2",
                     "#minCO2start"="0","#minCO2increment"="0",
                     "#maxCO2start"="500", "#maxCO2increment"="0"),
           BothMinBothMax=c("#minCWEstart"="10","#minCWEincrement"="2",
                       "#minGBstart"="10","#minGBincrement"="2",
                       "#minCO2start"="0","#minCO2increment"="0",
                       "#maxCO2start"="60", "#maxCO2increment"="2"))

resPolicyScenarios=list(FRES=c("#cweResPolicy"="/data/policyGoalNREAP_CF_CWE.csv","#gbResPolicy"="/data/policyGoalNREAP_CF_UK.csv"))

for(scenario in interestRateScenarios){
  #browser()
  for(policyNo in seq(1,length(scenario))){
    print(names(scenario)[policyNo])
    print(scenario[policyNo])
  }
}
# Step 3 building the scenarios: estimating the last three parameters
#${initial_propensity}
resScenarioNo<-1
resScenario<-resPolicyScenarios[1]
interestRateScenarioNo<-1
for(interestRateScenario in interestRateScenarios){
  co2scenarioNo<-1
  for(co2scenario in co2PolicyScenarios){
    for (microId in seq(1:microScenarioLength)){
      xmlFileContent<-readLines(xmlFilePath, encoding = "UTF-8")
      xmlFileContent<-gsub("#fuelPricePathAndFileName", paste("/data/stochasticFuelPrices/fuelPrices-",microId,".csv", sep="") , xmlFileContent)
      xmlFileContent<-gsub("#demandPathandFilename", paste("/data/stochasticDemandCWEandGB/demand-",microId,".csv", sep="") , xmlFileContent)
      xmlFileContent<-gsub("#CoalScenario", coalPriceScenario[1], xmlFileContent)
      xmlFileContent<-gsub("#GasScenario", gasPriceScenario[1], xmlFileContent)
      for(policyNo in seq(1,length(co2scenario))){
        xmlFileContent<-gsub(names(co2scenario)[policyNo], co2scenario[policyNo], xmlFileContent)
      }
      for(policyNo in seq(1,length(resScenario[[1]]))){
        xmlFileContent<-gsub(names(resScenario[[1]])[policyNo], resScenario[[1]][policyNo], xmlFileContent)
      }
      for(policyNo in seq(1,length(interestRateScenario))){
        xmlFileContent<-gsub(names(interestRateScenario)[policyNo], interestRateScenario[policyNo], xmlFileContent)
      }
      #print(paste("~/Desktop/emlabGen/scenario/",filestump,names(co2PolicyScenarios)[co2scenarioNo],"-",fuelPriceScenarios[1],"-",demandGrowthScenarios[1],"-",microId,".xml", sep=""))
      writeLines(xmlFileContent, paste("~/Desktop/emlabGen/scenario/",filestump,names(co2PolicyScenarios)[co2scenarioNo],"-",names(resPolicyScenarios)[resScenarioNo],"-",fuelPriceScenarios[1],"-",demandGrowthScenarios[1],"-",names(interestRateScenarios)[interestRateScenarioNo],"-",microId,".xml", sep=""))
    }
    co2scenarioNo<-co2scenarioNo+1
  }
  interestRateScenarioNo<-interestRateScenarioNo+1
}

#CoalScenario
#GasScenario
#minCWEstart
#minCWEincrement
#minGBstart
#minGBincrement
#minCO2start
#minCO2increment
#maxCO2start
#maxCO2increment
