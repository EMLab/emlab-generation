#Placeholders

# Step 1 building the scenarios: insert dataframe and read scenarioA.xml file
xmlFilePath<-"~/emlab-generation/co2priceCaps-scenarioTemplate.xml"
filestump<-'MWC-'
# Step 2 building the scenarios: make separate data vectors
fuelPriceScenarioLength=1
microScenarioLength=1

#BaseCase Scenario
priceCeiling="500"
coalPriceScenario=c("Coal.Medium","Coal.Low","Coal.High")
gasPriceScenario=c("NaturalGas.Medium","NaturalGas.Low","NaturalGas.High")
fuelPriceScenarios = c("FuelCentral")
demandGrowthScenarios = c("demandCentral")

resPolicyScenarios=list(FRES=c("#cweResPolicy"="/data/policyGoalNREAP_CF_CWE.csv","#gbResPolicy"="/data/policyGoalNREAP_CF_UK.csv"))

microScenarioNo<-seq(1,microScenarioLength)


co2PolicyScenarios=list("PureETS-S0-SL0"=c("#minCWEstart"="0","#minCWEincrement"="0",
                                  "#minGBstart"="0","#minGBincrement"="0",
                                  "#minCO2start"="0","#minCO2increment"="0",
                                  "#maxCO2start"=priceCeiling, "#maxCO2increment"="0"),
                        "MinCWE-S75-SL100"=c("#minCWEstart"="7.5","#minCWEincrement"="1",
                                             "#minGBstart"="0","#minGBincrement"="0",
                                             "#minCO2start"="0","#minCO2increment"="0",
                                             "#maxCO2start"=priceCeiling, "#maxCO2increment"="0"),
                        "MinGB-S185-SL230"=c("#minCWEstart"="0","#minCWEincrement"="0",
                                             "#minGBstart"="18.5","#minGBincrement"="2.3",
                                             "#minCO2start"="0","#minCO2increment"="0",
                                             "#maxCO2start"=priceCeiling, "#maxCO2increment"="0"),
                        "BothMin-S75-SL100"=c("#minCWEstart"="7.5","#minCWEincrement"="1",
                                              "#minGBstart"="7.5","#minGBincrement"="1",
                                              "#minCO2start"="0","#minCO2increment"="0",
                                              "#maxCO2start"=priceCeiling, "#maxCO2increment"="0"),
                        "BothMinBothMax-S75-SL100"=c("#minCWEstart"="7.5","#minCWEincrement"="1",
                                                     "#minGBstart"="7.5","#minGBincrement"="1",
                                                     "#minCO2start"="0","#minCO2increment"="0",
                                                     "#maxCO2start"="60", "#maxCO2increment"="2"))


centralPrivateDiscountingRateScenario=c("0.05")
centralCO2BackSmoothingFactorScenario=c("0")
centralPrivateDiscountingRateScenarioNames=c("D50")
centralCO2BackSmoothingFactorScenarioNames=c("B0")
centralCO2TargetReversionSpeedFactorScenario=c("3")
centralCO2TargetReversionSpeedFactorScenarioNames=c("R3")

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
        for(discountingRateId in seq(1:length(centralPrivateDiscountingRateScenario))){
          for(targetReversionId in seq(1:length(centralCO2TargetReversionSpeedFactorScenario))){
            for(backLookingId in seq(1:length(centralPrivateDiscountingRateScenario))){
             for (microId in seq(1:microScenarioLength)){
        
                xmlFileContent<-readLines(xmlFilePath, encoding = "UTF-8")
                xmlFileContent<-gsub("#fuelPricePathAndFileName", paste("/data/stochasticFuelPrices/fuelPrices-",microId,".csv", sep="") , xmlFileContent)
                xmlFileContent<-gsub("#demandPathandFilename", paste("/data/stochasticDemandCWEandGB/demand-",microId,".csv", sep="") , xmlFileContent)
                xmlFileContent<-gsub("#CoalScenario", coalPriceScenario[fuelId], xmlFileContent)
                xmlFileContent<-gsub("#GasScenario", gasPriceScenario[fuelId], xmlFileContent)
                xmlFileContent<-gsub("#centralPrivateDiscountingRate", centralPrivateDiscountingRateScenario[discountingRateId], xmlFileContent)
                xmlFileContent<-gsub("#centralCO2BackSmoothingFactor", centralCO2BackSmoothingFactorScenario[backLookingId], xmlFileContent)
                xmlFileContent<-gsub("#centralCO2TargetReversionSpeedFactor", centralCO2TargetReversionSpeedFactorScenario[targetReversionId], xmlFileContent)
                for(policyNo in seq(1,length(co2scenario))){
                  xmlFileContent<-gsub(names(co2scenario)[policyNo], co2scenario[policyNo], xmlFileContent)
                  }
                for(policyNo in seq(1,length(resScenario))){
                  xmlFileContent<-gsub(names(resScenario)[policyNo], resScenario[policyNo], xmlFileContent)
                }
                #print(paste("~/emlab-generation/emlab-generation/src/main/resources/scenarios/",filestump,names(co2PolicyScenarios)[co2scenarioNo],"-",fuelPriceScenarios[fuelId],"-",demandGrowthScenarios[demandId],"-",microId,".xml", sep=""))
                writeLines(xmlFileContent, paste("~/emlab-generation/emlab-generation/src/main/resources/scenarios/",filestump,names(co2PolicyScenarios)[co2scenarioNo],"-",names(resPolicyScenarios)[resScenarioNo],"-",fuelPriceScenarios[fuelId],"-",centralPrivateDiscountingRateScenarioNames[discountingRateId],"-",centralCO2BackSmoothingFactorScenarioNames[backLookingId],"-",centralCO2TargetReversionSpeedFactorScenarioNames[targetReversionId],"-","C",priceCeiling,"-",microId,".xml", sep=""))
              }
            }
          }
        }
      }
    }
    co2scenarioNo<-co2scenarioNo+1
  }
  resScenarioNo<-resScenarioNo+1
}



# Sensitivity Fuel scenarios -----------------------------------------------


fuelPriceScenarioLength=2
fuelPriceScenarios = c("FuelHigh","FuelLow")
# Step 3 building the scenarios: estimating the last three parameters
#${initial_propensity}
resScenarioNo<-1
for(resScenario in resPolicyScenarios){
  co2scenarioNo<-1
  for(co2scenario in co2PolicyScenarios){
    for (fuelId in seq(1:fuelPriceScenarioLength)){
      for(demandId in seq(1:length(demandGrowthScenarios))){
        for(discountingRateId in seq(1:length(centralPrivateDiscountingRateScenario))){
          for(targetReversionId in seq(1:length(centralCO2TargetReversionSpeedFactorScenario))){
            for(backLookingId in seq(1:length(centralPrivateDiscountingRateScenario))){
              for (microId in seq(1:microScenarioLength)){
                
                xmlFileContent<-readLines(xmlFilePath, encoding = "UTF-8")
                xmlFileContent<-gsub("#fuelPricePathAndFileName", paste("/data/stochasticFuelPrices/fuelPrices-",microId,".csv", sep="") , xmlFileContent)
                xmlFileContent<-gsub("#demandPathandFilename", paste("/data/stochasticDemandCWEandGB/demand-",microId,".csv", sep="") , xmlFileContent)
                xmlFileContent<-gsub("#CoalScenario", coalPriceScenario[fuelId], xmlFileContent)
                xmlFileContent<-gsub("#GasScenario", gasPriceScenario[fuelId], xmlFileContent)
                xmlFileContent<-gsub("#centralPrivateDiscountingRate", centralPrivateDiscountingRateScenario[discountingRateId], xmlFileContent)
                xmlFileContent<-gsub("#centralCO2BackSmoothingFactor", centralCO2BackSmoothingFactorScenario[backLookingId], xmlFileContent)
                xmlFileContent<-gsub("#centralCO2TargetReversionSpeedFactor", centralCO2TargetReversionSpeedFactorScenario[targetReversionId], xmlFileContent)
                for(policyNo in seq(1,length(co2scenario))){
                  xmlFileContent<-gsub(names(co2scenario)[policyNo], co2scenario[policyNo], xmlFileContent)
                }
                for(policyNo in seq(1,length(resScenario))){
                  xmlFileContent<-gsub(names(resScenario)[policyNo], resScenario[policyNo], xmlFileContent)
                }
                #print(paste("~/emlab-generation/emlab-generation/src/main/resources/scenarios/",filestump,names(co2PolicyScenarios)[co2scenarioNo],"-",fuelPriceScenarios[fuelId],"-",demandGrowthScenarios[demandId],"-",microId,".xml", sep=""))
                writeLines(xmlFileContent, paste("~/emlab-generation/emlab-generation/src/main/resources/scenarios/",filestump,names(co2PolicyScenarios)[co2scenarioNo],"-",names(resPolicyScenarios)[resScenarioNo],"-",fuelPriceScenarios[fuelId],"-",centralPrivateDiscountingRateScenarioNames[discountingRateId],"-",centralCO2BackSmoothingFactorScenarioNames[backLookingId],"-",centralCO2TargetReversionSpeedFactorScenarioNames[targetReversionId],"-","C",priceCeiling,"-",microId,".xml", sep=""))
              }
            }
          }
        }
      }
    }
    co2scenarioNo<-co2scenarioNo+1
  }
  resScenarioNo<-resScenarioNo+1
}

# Sensitivity RES Scenarios ----------------------------------------------


fuelPriceScenarioLength=1
resPolicyScenarios=list(HRES=c("#cweResPolicy"="/data/policyGoalNREAP_CF_CWE-half.csv","#gbResPolicy"="/data/policyGoalNREAP_CF_UK-half.csv"),
                        ZRES=c("#cweResPolicy"="/data/policyGoalNREAP_CF_CWE-null.csv","#gbResPolicy"="/data/policyGoalNREAP_CF_UK-null.csv"))
fuelPriceScenarios = c("FuelCentral")

# Step 3 building the scenarios: estimating the last three parameters
#${initial_propensity}
resScenarioNo<-1
for(resScenario in resPolicyScenarios){
  co2scenarioNo<-1
  for(co2scenario in co2PolicyScenarios){
    for (fuelId in seq(1:fuelPriceScenarioLength)){
      for(demandId in seq(1:length(demandGrowthScenarios))){
        for(discountingRateId in seq(1:length(centralPrivateDiscountingRateScenario))){
          for(targetReversionId in seq(1:length(centralCO2TargetReversionSpeedFactorScenario))){
            for(backLookingId in seq(1:length(centralPrivateDiscountingRateScenario))){
              for (microId in seq(1:microScenarioLength)){
                
                xmlFileContent<-readLines(xmlFilePath, encoding = "UTF-8")
                xmlFileContent<-gsub("#fuelPricePathAndFileName", paste("/data/stochasticFuelPrices/fuelPrices-",microId,".csv", sep="") , xmlFileContent)
                xmlFileContent<-gsub("#demandPathandFilename", paste("/data/stochasticDemandCWEandGB/demand-",microId,".csv", sep="") , xmlFileContent)
                xmlFileContent<-gsub("#CoalScenario", coalPriceScenario[fuelId], xmlFileContent)
                xmlFileContent<-gsub("#GasScenario", gasPriceScenario[fuelId], xmlFileContent)
                xmlFileContent<-gsub("#centralPrivateDiscountingRate", centralPrivateDiscountingRateScenario[discountingRateId], xmlFileContent)
                xmlFileContent<-gsub("#centralCO2BackSmoothingFactor", centralCO2BackSmoothingFactorScenario[backLookingId], xmlFileContent)
                xmlFileContent<-gsub("#centralCO2TargetReversionSpeedFactor", centralCO2TargetReversionSpeedFactorScenario[targetReversionId], xmlFileContent)
                for(policyNo in seq(1,length(co2scenario))){
                  xmlFileContent<-gsub(names(co2scenario)[policyNo], co2scenario[policyNo], xmlFileContent)
                }
                for(policyNo in seq(1,length(resScenario))){
                  xmlFileContent<-gsub(names(resScenario)[policyNo], resScenario[policyNo], xmlFileContent)
                }
                #print(paste("~/emlab-generation/emlab-generation/src/main/resources/scenarios/",filestump,names(co2PolicyScenarios)[co2scenarioNo],"-",fuelPriceScenarios[fuelId],"-",demandGrowthScenarios[demandId],"-",microId,".xml", sep=""))
                writeLines(xmlFileContent, paste("~/emlab-generation/emlab-generation/src/main/resources/scenarios/",filestump,names(co2PolicyScenarios)[co2scenarioNo],"-",names(resPolicyScenarios)[resScenarioNo],"-",fuelPriceScenarios[fuelId],"-",centralPrivateDiscountingRateScenarioNames[discountingRateId],"-",centralCO2BackSmoothingFactorScenarioNames[backLookingId],"-",centralCO2TargetReversionSpeedFactorScenarioNames[targetReversionId],"-","C",priceCeiling,"-",microId,".xml", sep=""))
              }
            }
          }
        }
      }
    }
    co2scenarioNo<-co2scenarioNo+1
  }
  resScenarioNo<-resScenarioNo+1
}


# Price Floor Sensitivity -------------------------------------------------


fuelPriceScenarios = c("FuelCentral")
resPolicyScenarios=list(FRES=c("#cweResPolicy"="/data/policyGoalNREAP_CF_CWE.csv","#gbResPolicy"="/data/policyGoalNREAP_CF_UK.csv"))

co2PolicyScenarios=list(
  "MinCWE-S50-SL75"=c("#minCWEstart"="5","#minCWEincrement"="0.75",
              "#minGBstart"="0","#minGBincrement"="0",
              "#minCO2start"="0","#minCO2increment"="0",
              "#maxCO2start"=priceCeiling, "#maxCO2increment"="0"),
  "MinGB-S50-SL75"=c("#minCWEstart"="0","#minCWEincrement"="0",
                "#minGBstart"="5","#minGBincrement"="0.75",
                "#minCO2start"="0","#minCO2increment"="0",
                "#maxCO2start"=priceCeiling, "#maxCO2increment"="0"),
  "BothMin-S50-SL75"=c("#minCWEstart"="5","#minCWEincrement"="0.75",
                  "#minGBstart"="5","#minGBincrement"="0.75",
                  "#minCO2start"="0","#minCO2increment"="0",
                  "#maxCO2start"=priceCeiling, "#maxCO2increment"="0"),
  "BothMinBothMax-S50-SL75"=c("#minCWEstart"="5","#minCWEincrement"="0.75",
                         "#minGBstart"="5","#minGBincrement"="0.75",
                         "#minCO2start"="0","#minCO2increment"="0",
                         "#maxCO2start"="60", "#maxCO2increment"="2"),
  "MinCWE-S75-SL100"=c("#minCWEstart"="7.5","#minCWEincrement"="1",
            "#minGBstart"="0","#minGBincrement"="0",
            "#minCO2start"="0","#minCO2increment"="0",
            "#maxCO2start"=priceCeiling, "#maxCO2increment"="0"),
  "MinGB-S75-SL100"=c("#minCWEstart"="0","#minCWEincrement"="0",
              "#minGBstart"="7.5","#minGBincrement"="1",
              "#minCO2start"="0","#minCO2increment"="0",
              "#maxCO2start"=priceCeiling, "#maxCO2increment"="0"),
  "BothMin-S75-SL100"=c("#minCWEstart"="7.5","#minCWEincrement"="1",
                "#minGBstart"="7.5","#minGBincrement"="1",
                "#minCO2start"="0","#minCO2increment"="0",
                "#maxCO2start"=priceCeiling, "#maxCO2increment"="0"),
  "BothMinBothMax-S75-SL100"=c("#minCWEstart"="7.5","#minCWEincrement"="1",
                       "#minGBstart"="7.5","#minGBincrement"="1",
                       "#minCO2start"="0","#minCO2increment"="0",
                       "#maxCO2start"="60", "#maxCO2increment"="2"),
  "MinCWE-S100-SL150"=c("#minCWEstart"="10","#minCWEincrement"="1.5",
             "#minGBstart"="0","#minGBincrement"="0",
             "#minCO2start"="0","#minCO2increment"="0",
             "#maxCO2start"=priceCeiling, "#maxCO2increment"="0"),
  "MinGB-S100-SL150"=c("#minCWEstart"="0","#minCWEincrement"="0",
               "#minGBstart"="10","#minGBincrement"="1.5",
               "#minCO2start"="0","#minCO2increment"="0",
               "#maxCO2start"=priceCeiling, "#maxCO2increment"="0"),
  "BothMin-S100-SL150"=c("#minCWEstart"="10","#minCWEincrement"="1.5",
                 "#minGBstart"="10","#minGBincrement"="1.5",
                 "#minCO2start"="0","#minCO2increment"="0",
                 "#maxCO2start"=priceCeiling, "#maxCO2increment"="0"),
  "BothMinBothMax-S100-SL150"=c("#minCWEstart"="10","#minCWEincrement"="1.5",
                        "#minGBstart"="10","#minGBincrement"="1.5",
                        "#minCO2start"="0","#minCO2increment"="0",
                        "#maxCO2start"="60", "#maxCO2increment"="2"),
  "MinCWE-S185-SL230"=c("#minCWEstart"="18.5","#minCWEincrement"="2.3",
             "#minGBstart"="0","#minGBincrement"="0",
             "#minCO2start"="0","#minCO2increment"="0",
             "#maxCO2start"=priceCeiling, "#maxCO2increment"="0"),
  "MinGB-S185-SL230"=c("#minCWEstart"="0","#minCWEincrement"="0",
               "#minGBstart"="18.5","#minGBincrement"="2.3",
               "#minCO2start"="0","#minCO2increment"="0",
               "#maxCO2start"=priceCeiling, "#maxCO2increment"="0"),
  "BothMin-S185-SL230"=c("#minCWEstart"="18.5","#minCWEincrement"="2.3",
                 "#minGBstart"="18.5","#minGBincrement"="2.3",
                 "#minCO2start"="0","#minCO2increment"="0",
                 "#maxCO2start"=priceCeiling, "#maxCO2increment"="0"),
  "BothMinBothMax-S185-SL230"=c("#minCWEstart"="18.5","#minCWEincrement"="2.3",
                        "#minGBstart"="18.5","#minGBincrement"="2.3",
                        "#minCO2start"="0","#minCO2increment"="0",
                        "#maxCO2start"="60", "#maxCO2increment"="2"))

#${initial_propensity}
resScenarioNo<-1
for(resScenario in resPolicyScenarios){
  co2scenarioNo<-1
  for(co2scenario in co2PolicyScenarios){
    for (fuelId in seq(1:fuelPriceScenarioLength)){
      for(demandId in seq(1:length(demandGrowthScenarios))){
        for(discountingRateId in seq(1:length(centralPrivateDiscountingRateScenario))){
          for(targetReversionId in seq(1:length(centralCO2TargetReversionSpeedFactorScenario))){
            for(backLookingId in seq(1:length(centralPrivateDiscountingRateScenario))){
              for (microId in seq(1:microScenarioLength)){
                
                xmlFileContent<-readLines(xmlFilePath, encoding = "UTF-8")
                xmlFileContent<-gsub("#fuelPricePathAndFileName", paste("/data/stochasticFuelPrices/fuelPrices-",microId,".csv", sep="") , xmlFileContent)
                xmlFileContent<-gsub("#demandPathandFilename", paste("/data/stochasticDemandCWEandGB/demand-",microId,".csv", sep="") , xmlFileContent)
                xmlFileContent<-gsub("#CoalScenario", coalPriceScenario[fuelId], xmlFileContent)
                xmlFileContent<-gsub("#GasScenario", gasPriceScenario[fuelId], xmlFileContent)
                xmlFileContent<-gsub("#centralPrivateDiscountingRate", centralPrivateDiscountingRateScenario[discountingRateId], xmlFileContent)
                xmlFileContent<-gsub("#centralCO2BackSmoothingFactor", centralCO2BackSmoothingFactorScenario[backLookingId], xmlFileContent)
                xmlFileContent<-gsub("#centralCO2TargetReversionSpeedFactor", centralCO2TargetReversionSpeedFactorScenario[targetReversionId], xmlFileContent)
                for(policyNo in seq(1,length(co2scenario))){
                  xmlFileContent<-gsub(names(co2scenario)[policyNo], co2scenario[policyNo], xmlFileContent)
                }
                for(policyNo in seq(1,length(resScenario))){
                  xmlFileContent<-gsub(names(resScenario)[policyNo], resScenario[policyNo], xmlFileContent)
                }
                #print(paste("~/emlab-generation/emlab-generation/src/main/resources/scenarios/",filestump,names(co2PolicyScenarios)[co2scenarioNo],"-",fuelPriceScenarios[fuelId],"-",demandGrowthScenarios[demandId],"-",microId,".xml", sep=""))
                writeLines(xmlFileContent, paste("~/emlab-generation/emlab-generation/src/main/resources/scenarios/",filestump,names(co2PolicyScenarios)[co2scenarioNo],"-",names(resPolicyScenarios)[resScenarioNo],"-",fuelPriceScenarios[fuelId],"-",centralPrivateDiscountingRateScenarioNames[discountingRateId],"-",centralCO2BackSmoothingFactorScenarioNames[backLookingId],"-",centralCO2TargetReversionSpeedFactorScenarioNames[targetReversionId],"-","C",priceCeiling,"-",microId,".xml", sep=""))
              }
            }
          }
        }
      }
    }
    co2scenarioNo<-co2scenarioNo+1
  }
  resScenarioNo<-resScenarioNo+1
}
