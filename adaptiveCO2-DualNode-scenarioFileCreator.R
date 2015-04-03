#Placeholders

# Step 1 building the scenarios: insert dataframe and read scenarioA.xml file
xmlFilePath<-"~/emlab-generation/adaptiveCO2-DualNode-scenarioTemplate.xml"
filestump<-'AdaptiveCO2-'
# Step 2 building the scenarios: make separate data vectors
fuelPriceScenarioLength=1
microScenarioLength=120

#BaseCase Scenario
priceCeiling="120"
coalPriceScenario=c("Coal.Medium","Coal.Low","Coal.High")
gasPriceScenario=c("NaturalGas.Medium","NaturalGas.Low","NaturalGas.High")
fuelPriceScenarios = c("FuelCentral")
demandGrowthScenarios = c("demandCentral")
stabilityReserveFirstYearOfOperation="10"

resPolicyScenarios=list(FRES=c("#cweResPolicy"="/data/policyGoalNREAP_CF_CWE.csv","#gbResPolicy"="/data/policyGoalNREAP_CF_UK.csv"))
resRealisations=c("150POvershoot"="resPolicyRealisations150POvershoot","100POvershoot"="resPolicyRealisations100POvershoot","50POvershoot"="resPolicyRealisations50POvershoot","0POvershoot"="resPolicyRealisations0POvershoot", "InvestmentSurge400P4Years"="resPolicyRealisation400P4YearsInvestmentSurge")
producerBankingScenarios=list("BaseBanking-R3"=c("#stabilityReserveBankingFirstYear"="0.8",
                                            "#stabilityReserveBankingSecondYear"="0.5",
                                            "#stabilityReserveBankingThirdYear"="0.2",
                                            "#centralPrivateDiscountingRate"="0.05",
                                            "#centralCO2BackSmoothingFactor"="0",
                                            "#centralCO2TargetReversionSpeedFactor"="3"
                                            ))

co2PolicyScenarios=list(PureETS=c("#emissionCapTimeline"="/data/emissionCapCweUk.csv",
                         "#activelyAdjustingTheCO2Cap"="false",
                         "#deviationFromResTargetAdjustment"="false",
                         "#adaptiveCapCO2SavingsWeighingFactor"="1",
                         "#originalCapOrEmissionBasedCapAdjustment"="false",
                         "#adaptiveCapAdjustmentRelativeToNonSubsidisedProduction"="false"),
               "RelativeCapAdaptionBasedOnOriginalCap"=c("#emissionCapTimeline"="/data/emissionCapCweUk.csv",
                                     "#activelyAdjustingTheCO2Cap"="true",
                                     "#deviationFromResTargetAdjustment"="true",
                                     "#adaptiveCapCO2SavingsWeighingFactor"="1",
                                     "#originalCapOrEmissionBasedCapAdjustment"="true",
                                     "#adaptiveCapAdjustmentRelativeToNonSubsidisedProduction"="false"),
               "AdaptionRelativeToNonSubsidised"=c("#emissionCapTimeline"="/data/emissionCapCweUk.csv",
                                                       "#activelyAdjustingTheCO2Cap"="true",
                                                       "#deviationFromResTargetAdjustment"="true",
                                                       "#adaptiveCapCO2SavingsWeighingFactor"="1",
                                                       "#originalCapOrEmissionBasedCapAdjustment"="true",
                                                   "#adaptiveCapAdjustmentRelativeToNonSubsidisedProduction"="true"))

microScenarioNo<-seq(1,microScenarioLength)

#No Backloading, smoothed backgloading, backloading
#backLoadingName=c("NBL","SBL","BL")
#backLoadingValue=c("/data/emissionCapCweUk.csv","/data/emissionCapCweUk_unfccc_backloading_smoothed.csv","/data/emissionCapCweUk_unfccc_backloading.csv")
#backLoadingName=c("NBL","SBL","BL")
#backLoadingValue=c("/data/emissionCapCweUk_citl.csv","/data/emissionCapCweUk_citl_backloading_smoothed.csv","/data/emissionCapCweUk_Citl_backloading.csv")

# Step 3 building the scenarios: estimating the last three parameters
#${initial_propensity}



resRealisationNumber<-1
for(resRealisation in resRealisations){
  co2PolicyNo<-1
  for(co2PolicyScenario in co2PolicyScenarios){
    producerBankingNo<-1
    for(producerBankingScenario in producerBankingScenarios){
      resScenarioNo<-1
      for(resScenario in resPolicyScenarios){
          for (fuelId in seq(1:fuelPriceScenarioLength)){
            for(demandId in seq(1:length(demandGrowthScenarios))){
               for (microId in seq(1:microScenarioLength)){
                  xmlFileContent<-readLines(xmlFilePath, encoding = "UTF-8")
                  xmlFileContent<-gsub("#fuelPricePathAndFileName", paste("/data/stochasticFuelPrices/fuelPrices-",microId,".csv", sep="") , xmlFileContent)
                  xmlFileContent<-gsub("#demandPathandFilename", paste("/data/stochasticDemandCWEandGB/demand-",microId,".csv", sep="") , xmlFileContent)
                  xmlFileContent<-gsub("#cweResRealisationFileName", paste("/data/",resRealisation,"/resPolicyRealisation_CWE-",microId,".csv", sep="") , xmlFileContent)
                  xmlFileContent<-gsub("#gbResRealisationFileName", paste("/data/",resRealisation,"/resPolicyRealisation_GB-",microId,".csv", sep="") , xmlFileContent)
                  xmlFileContent<-gsub("#CoalScenario", coalPriceScenario[fuelId], xmlFileContent)
                  xmlFileContent<-gsub("#GasScenario", gasPriceScenario[fuelId], xmlFileContent)
                  for(resPolicyParameterNo in seq(1,length(resScenario))){
                    xmlFileContent<-gsub(names(resScenario)[resPolicyParameterNo], resScenario[resPolicyParameterNo], xmlFileContent)
                  }
                  for(producerBankingParameterNo in seq(1,length(producerBankingScenario))){
                    xmlFileContent<-gsub(names(producerBankingScenario)[producerBankingParameterNo], producerBankingScenario[producerBankingParameterNo], xmlFileContent)
                  }
                  for(co2PolicyParameterNo in seq(1,length(co2PolicyScenario))){
                    #print(paste("Substituting:",co2PolicyScenario[co2PolicyParameterNo]))
                    #flush.console()
                    xmlFileContent<-gsub(names(co2PolicyScenario)[co2PolicyParameterNo], co2PolicyScenario[co2PolicyParameterNo], xmlFileContent)
                  }
                  #print( paste("~/Dropbox/emlabGen/scenario/",filestump,names(co2PolicyScenarios)[co2PolicyNo],"-",names(producerBankingScenarios)[producerBankingNo],"-",names(resPolicyScenarios)[resScenarioNo],"-",names(resRealisations)[resRealisationNumber],"-",fuelPriceScenarios[fuelId],"-",microId,".xml", sep=""))
                  #flush.console()
                  writeLines(xmlFileContent, paste("~/Desktop/emlabGen/scenario/",filestump,names(co2PolicyScenarios)[co2PolicyNo],"-",names(producerBankingScenarios)[producerBankingNo],"-",names(resPolicyScenarios)[resScenarioNo],"-",names(resRealisations)[resRealisationNumber],"-",fuelPriceScenarios[fuelId],"-",microId,".xml", sep=""))
            }
          }
        }
        resScenarioNo<-resScenarioNo+1
      }
    producerBankingNo<-producerBankingNo+1
    }
   co2PolicyNo<-co2PolicyNo+1
  }
  resRealisationNumber<-resRealisationNumber+1
}
