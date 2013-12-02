## This file was used to creater the scenario file for the paper
## "Cross-border electricity market effects due to price caps in an emission trading system: An agent-based approach"


##------ Standard scenarios: 3x Fuel Scenarios, 5x policy scenarios, 3x renewable scenarios, 120 Monte-Carlo runs ----

# Step 1 building the scenarios: insert dataframe and read input scenario file.
xmlFilePath<-"~/emlab-generation/co2priceCaps-scenarioTemplate.xml"
filestump<-'scenarioX-'
# Step 2 building the scenarios: make separate data vectors
fuelPriceScenarioLength=3
microScenarioLength=120

coalPriceScenario=c("Coal.Medium","Coal.Low","Coal.High")
gasPriceScenario=c("NaturalGas.Medium","NaturalGas.Low","NaturalGas.High")
fuelPriceScenarios = c("DeccCentral","DeccLow","DeccHigh")
demandGrowthScenarios = c("demandCentral")
microScenarioNo<-seq(1,microScenarioLength)

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

resPolicyScenarios=list(FRES=c("#cweResPolicy"="/data/policyGoalNREAP_CF_CWE.csv","#gbResPolicy"="/data/policyGoalNREAP_CF_UK.csv"),
                        HRES=c("#cweResPolicy"="/data/policyGoalNREAP_CF_CWE-half.csv","#gbResPolicy"="/data/policyGoalNREAP_CF_UK-half.csv"),
                        ZRES=c("#cweResPolicy"="/data/policyGoalNREAP_CF_CWE-null.csv","#gbResPolicy"="/data/policyGoalNREAP_CF_UK-null.csv"))

for(scenario in co2PolicyScenarios){
  #browser()
  for(policyNo in seq(1,length(scenario))){
    print(names(scenario)[policyNo])
    print(scenario[policyNo])
  }
}
# Step 3 building the scenarios: estimating the last three parameters
#${initial_propensity}
resScenarioNo<-1
for(resScenario in resPolicyScenarios){
  co2scenarioNo<-1
  for(co2scenario in co2PolicyScenarios){
    for (fuelId in seq(1:fuelPriceScenarioLength)){
      for(demandId in seq(1:length(demandGrowthScenarios))){
      
        for (microId in seq(1:microScenarioLength)){
        
          xmlFileContent<-readLines(xmlFilePath, encoding = "UTF-8")
          xmlFileContent<-gsub("#fuelPricePathAndFileName", paste("/data/stochasticFuelPrices/fuelPrices-",microId,".csv", sep="") , xmlFileContent)
          xmlFileContent<-gsub("#demandPathandFilename", paste("/data/stochasticDemandCWEandGB/demand-",microId,".csv", sep="") , xmlFileContent)
          xmlFileContent<-gsub("#CoalScenario", coalPriceScenario[fuelId], xmlFileContent)
          xmlFileContent<-gsub("#GasScenario", gasPriceScenario[fuelId], xmlFileContent)
          for(policyNo in seq(1,length(co2scenario))){
            xmlFileContent<-gsub(names(co2scenario)[policyNo], co2scenario[policyNo], xmlFileContent)
            }
          for(policyNo in seq(1,length(resScenario))){
            xmlFileContent<-gsub(names(resScenario)[policyNo], resScenario[policyNo], xmlFileContent)
          }
          #print(paste("~/Desktop/emlabGen/scenario/",filestump,names(co2PolicyScenarios)[co2scenarioNo],"-",fuelPriceScenarios[fuelId],"-",demandGrowthScenarios[demandId],"-",microId,".xml", sep=""))
          writeLines(xmlFileContent, paste("~/emlab-generation/emlab-generation/src/main/resources/scenarios/",filestump,names(co2PolicyScenarios)[co2scenarioNo],"-",names(resPolicyScenarios)[resScenarioNo],"-",fuelPriceScenarios[fuelId],"-",demandGrowthScenarios[demandId],"-",microId,".xml", sep=""))
        }
      }
    }
    co2scenarioNo<-co2scenarioNo+1
  }
  resScenarioNo<-resScenarioNo+1
}


##------ CO2 price floor sensitivity: 1x Fuel Scenarios, 5x4 policy scenarios, 1x renewable scenarios, 120 Monte-Carlo runs ----

filestump<-'scenarioS-'

co2PolicyScenarios=list(
                        MinCWE075=c("#minCWEstart"="5","#minCWEincrement"="0.75",
                                 "#minGBstart"="0","#minGBincrement"="0",
                                 "#minCO2start"="0","#minCO2increment"="0",
                                 "#maxCO2start"="500", "#maxCO2increment"="0"),
                        MinGBLow075=c("#minCWEstart"="0","#minCWEincrement"="0",
                                "#minGBstart"="5","#minGBincrement"="0.75",
                                "#minCO2start"="0","#minCO2increment"="0",
                                "#maxCO2start"="500", "#maxCO2increment"="0"),
                        BothMinLow075=c("#minCWEstart"="5","#minCWEincrement"="0.75",
                                  "#minGBstart"="5","#minGBincrement"="0.75",
                                  "#minCO2start"="0","#minCO2increment"="0",
                                  "#maxCO2start"="500", "#maxCO2increment"="0"),
                        BothMinBothMaxLow075=c("#minCWEstart"="5","#minCWEincrement"="0.75",
                                         "#minGBstart"="5","#minGBincrement"="0.75",
                                         "#minCO2start"="0","#minCO2increment"="0",
                                         "#maxCO2start"="60", "#maxCO2increment"="2"),
                        MinCWE1=c("#minCWEstart"="7.5","#minCWEincrement"="1",
                                     "#minGBstart"="0","#minGBincrement"="0",
                                     "#minCO2start"="0","#minCO2increment"="0",
                                     "#maxCO2start"="500", "#maxCO2increment"="0"),
                        MinGBLow1=c("#minCWEstart"="0","#minCWEincrement"="0",
                                       "#minGBstart"="7.5","#minGBincrement"="1",
                                       "#minCO2start"="0","#minCO2increment"="0",
                                       "#maxCO2start"="500", "#maxCO2increment"="0"),
                        BothMinLow1=c("#minCWEstart"="7.5","#minCWEincrement"="1",
                                         "#minGBstart"="7.5","#minGBincrement"="1",
                                         "#minCO2start"="0","#minCO2increment"="0",
                                         "#maxCO2start"="500", "#maxCO2increment"="0"),
                        BothMinBothMaxLow1=c("#minCWEstart"="7.5","#minCWEincrement"="1",
                                                "#minGBstart"="7.5","#minGBincrement"="1",
                                                "#minCO2start"="0","#minCO2increment"="0",
                                                "#maxCO2start"="60", "#maxCO2increment"="2"),
                        MinCWE15=c("#minCWEstart"="10","#minCWEincrement"="1.5",
                                  "#minGBstart"="0","#minGBincrement"="0",
                                  "#minCO2start"="0","#minCO2increment"="0",
                                  "#maxCO2start"="500", "#maxCO2increment"="0"),
                        MinGBLow15=c("#minCWEstart"="0","#minCWEincrement"="0",
                                    "#minGBstart"="10","#minGBincrement"="1.5",
                                    "#minCO2start"="0","#minCO2increment"="0",
                                    "#maxCO2start"="500", "#maxCO2increment"="0"),
                        BothMinLow15=c("#minCWEstart"="10","#minCWEincrement"="1.5",
                                      "#minGBstart"="10","#minGBincrement"="1.5",
                                      "#minCO2start"="0","#minCO2increment"="0",
                                      "#maxCO2start"="500", "#maxCO2increment"="0"),
                        BothMinBothMaxLow15=c("#minCWEstart"="10","#minCWEincrement"="1.5",
                                             "#minGBstart"="10","#minGBincrement"="1.5",
                                             "#minCO2start"="0","#minCO2increment"="0",
                                             "#maxCO2start"="60", "#maxCO2increment"="2"),
                        MinCWE23=c("#minCWEstart"="18.5","#minCWEincrement"="2.3",
                                    "#minGBstart"="0","#minGBincrement"="0",
                                    "#minCO2start"="0","#minCO2increment"="0",
                                    "#maxCO2start"="500", "#maxCO2increment"="0"),
                        MinGBLow23=c("#minCWEstart"="0","#minCWEincrement"="0",
                                      "#minGBstart"="18.5","#minGBincrement"="2.3",
                                      "#minCO2start"="0","#minCO2increment"="0",
                                      "#maxCO2start"="500", "#maxCO2increment"="0"),
                        BothMinLow23=c("#minCWEstart"="18.5","#minCWEincrement"="2.3",
                                        "#minGBstart"="18.5","#minGBincrement"="2.3",
                                        "#minCO2start"="0","#minCO2increment"="0",
                                        "#maxCO2start"="500", "#maxCO2increment"="0"),
                        BothMinBothMaxLow23=c("#minCWEstart"="18.5","#minCWEincrement"="2.3",
                                               "#minGBstart"="18.5","#minGBincrement"="2.3",
                                               "#minCO2start"="0","#minCO2increment"="0",
                                               "#maxCO2start"="60", "#maxCO2increment"="2"))
co2scenarioNo<-1
resScenarioNo<-1
resScenario<-resPolicyScenarios[1]
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
    writeLines(xmlFileContent, paste("~/emlab-generation/emlab-generation/src/main/resources/scenarios/",filestump,names(co2PolicyScenarios)[co2scenarioNo],"-",names(resPolicyScenarios)[resScenarioNo],"-",fuelPriceScenarios[1],"-",demandGrowthScenarios[1],"-",microId,".xml", sep=""))
  }
  co2scenarioNo<-co2scenarioNo+1
}