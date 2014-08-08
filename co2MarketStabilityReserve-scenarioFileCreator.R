#Placeholders

# Step 1 building the scenarios: insert dataframe and read scenarioA.xml file
xmlFilePath<-"~/emlab-generation/co2MarketStabilityReserve-scenarioTemplate.xml"
filestump<-'Base-'
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
producerBankingScenarios=list("BaseBanking-R3"=c("#stabilityReserveBankingFirstYear"="0.8",
                                            "#stabilityReserveBankingSecondYear"="0.5",
                                            "#stabilityReserveBankingThirdYear"="0.2",
                                            "#centralPrivateDiscountingRate"="0.05",
                                            "#centralCO2BackSmoothingFactor"="0",
                                            "#centralCO2TargetReversionSpeedFactor"="3"
                                            ))

co2PolicyScenarios=list(PureETS=c("#emissionCapTimeline"="/data/emissionCapCweUk.csv",
                         "#stabilityReserveActive"="false",
                         "#stabilityReserveFirstYearOfOperation"="100",
                         "#stabilityReserveUpperTriggerStart"="0","#stabilityReserveUpperTriggerYearlyIncrease"="0",
                         "#stabilityReserveLowerTriggerTrendStart"="0","#stabilityReserveLowerTriggerTrendIncrease"="0",
                         "#stabilityReserveReleaseQuantityTrendStart"="0","#stabilityReserveReleaseQuantityTrendIncrease"="0",
                         "#stabilityReserveAddingMinimumTrendStart"="0","#stabilityReserveAddingMinimumTrendIncrease"="0",
                         "#stabilityReserveAddingPercentageTrendStart"="0","#stabilityReserveAddingPercentageTrendIncrease"="0"),
               Backloading=c("#emissionCapTimeline"="/data/emissionCapCweUk_unfccc_backloading_smoothed.csv",
                         "#stabilityReserveActive"="false",
                         "#stabilityReserveFirstYearOfOperation"="100",
                         "#stabilityReserveUpperTriggerStart"="0","#stabilityReserveUpperTriggerYearlyIncrease"="0",
                         "#stabilityReserveLowerTriggerTrendStart"="0","#stabilityReserveLowerTriggerTrendIncrease"="0",
                         "#stabilityReserveReleaseQuantityTrendStart"="0","#stabilityReserveReleaseQuantityTrendIncrease"="0",
                         "#stabilityReserveAddingMinimumTrendStart"="0","#stabilityReserveAddingMinimumTrendIncrease"="0",
                         "#stabilityReserveAddingPercentageTrendStart"="0","#stabilityReserveAddingPercentageTrendIncrease"="0"),
               MSR=c("#emissionCapTimeline"="/data/emissionCapCweUk_unfccc_backloading_smoothed.csv",
                     "#stabilityReserveActive"="true",
                     "#stabilityReserveFirstYearOfOperation"="10",
                     "#stabilityReserveUpperTriggerStart"="289283608","#stabilityReserveUpperTriggerYearlyIncrease"="0",
                     "#stabilityReserveLowerTriggerTrendStart"="138911697","#stabilityReserveLowerTriggerTrendIncrease"="0",
                     "#stabilityReserveReleaseQuantityTrendStart"="34727924","#stabilityReserveReleaseQuantityTrendIncrease"="0",
                     "#stabilityReserveAddingMinimumTrendStart"="34727924","#stabilityReserveAddingMinimumTrendIncrease"="0",
                     "#stabilityReserveAddingPercentageTrendStart"="0.12","#stabilityReserveAddingPercentageTrendIncrease"="0"))

microScenarioNo<-seq(1,microScenarioLength)

#No Backloading, smoothed backgloading, backloading
#backLoadingName=c("NBL","SBL","BL")
#backLoadingValue=c("/data/emissionCapCweUk.csv","/data/emissionCapCweUk_unfccc_backloading_smoothed.csv","/data/emissionCapCweUk_unfccc_backloading.csv")
#backLoadingName=c("NBL","SBL","BL")
#backLoadingValue=c("/data/emissionCapCweUk_citl.csv","/data/emissionCapCweUk_citl_backloading_smoothed.csv","/data/emissionCapCweUk_Citl_backloading.csv")

# Step 3 building the scenarios: estimating the last three parameters
#${initial_propensity}



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
                #print(paste("~/Dropbox/emlabGen/scenario/",filestump,marketStabilityReservenName[msrId],"-",backLoadingName[backLoadingId],"-",names(resPolicyScenarios)[resScenarioNo],"-",fuelPriceScenarios[fuelId],"-",centralPrivateDiscountingRateScenarioNames[discountingRateId],"-",centralCO2BackSmoothingFactorScenarioNames[backLookingId],"-",centralCO2TargetReversionSpeedFactorScenarioNames[targetReversionId],"-","C",priceCeiling,"-",microId,".xml", sep=""))
                #flush.console()
                writeLines(xmlFileContent, paste("~/Dropbox/emlabGen/scenario/",filestump,names(co2PolicyScenarios)[co2PolicyNo],"-",names(producerBankingScenarios)[producerBankingNo],"-",names(resPolicyScenarios)[resScenarioNo],"-",fuelPriceScenarios[fuelId],"-",microId,".xml", sep=""))
          }
        }
      }
      resScenarioNo<-resScenarioNo+1
    }
  producerBankingNo<-producerBankingNo+1
  }
 co2PolicyNo<-co2PolicyNo+1
}



# Sensitivity Producer Banking --------------------------------------------
filestump<-'ProdBank-'

producerBankingScenarios=list("LowerBanking-R3"=c("#stabilityReserveBankingFirstYear"="0.6",
                                           "#stabilityReserveBankingSecondYear"="0.3",
                                           "#stabilityReserveBankingThirdYear"="0.1",
                                           "#centralPrivateDiscountingRate"="0.05",
                                           "#centralCO2BackSmoothingFactor"="0",
                                           "#centralCO2TargetReversionSpeedFactor"="3"),
                              "TRBanking-R3"=c("#stabilityReserveBankingFirstYear"="0.267",
                                          "#stabilityReserveBankingSecondYear"="0.167",
                                          "#stabilityReserveBankingThirdYear"="0.067",
                                          "#centralPrivateDiscountingRate"="0.05",
                                          "#centralCO2BackSmoothingFactor"="0",
                                          "#centralCO2TargetReversionSpeedFactor"="3"),
                            "BaseBanking-R2"=c("#stabilityReserveBankingFirstYear"="0.8",
                                          "#stabilityReserveBankingSecondYear"="0.5",
                                          "#stabilityReserveBankingThirdYear"="0.2",
                                          "#centralPrivateDiscountingRate"="0.05",
                                          "#centralCO2BackSmoothingFactor"="0",
                                          "#centralCO2TargetReversionSpeedFactor"="2"),
                            "LowerBanking-R2"=c("#stabilityReserveBankingFirstYear"="0.6",
                                              "#stabilityReserveBankingSecondYear"="0.3",
                                              "#stabilityReserveBankingThirdYear"="0.1",
                                              "#centralPrivateDiscountingRate"="0.05",
                                              "#centralCO2BackSmoothingFactor"="0",
                                              "#centralCO2TargetReversionSpeedFactor"="2"),
                            "TRBanking-R2"=c("#stabilityReserveBankingFirstYear"="0.267",
                                           "#stabilityReserveBankingSecondYear"="0.167",
                                           "#stabilityReserveBankingThirdYear"="0.067",
                                           "#centralPrivateDiscountingRate"="0.05",
                                           "#centralCO2BackSmoothingFactor"="0",
                                           "#centralCO2TargetReversionSpeedFactor"="2"),
                            "BaseBanking-R4"=c("#stabilityReserveBankingFirstYear"="0.8",
                                             "#stabilityReserveBankingSecondYear"="0.5",
                                             "#stabilityReserveBankingThirdYear"="0.2",
                                             "#centralPrivateDiscountingRate"="0.05",
                                             "#centralCO2BackSmoothingFactor"="0",
                                             "#centralCO2TargetReversionSpeedFactor"="4"),
                            "LowerBanking-R4"=c("#stabilityReserveBankingFirstYear"="0.6",
                                              "#stabilityReserveBankingSecondYear"="0.3",
                                              "#stabilityReserveBankingThirdYear"="0.1",
                                              "#centralPrivateDiscountingRate"="0.05",
                                              "#centralCO2BackSmoothingFactor"="0",
                                              "#centralCO2TargetReversionSpeedFactor"="4"),
                            "TRBanking-R4"=c("#stabilityReserveBankingFirstYear"="0.267",
                                           "#stabilityReserveBankingSecondYear"="0.167",
                                           "#stabilityReserveBankingThirdYear"="0.067",
                                           "#centralPrivateDiscountingRate"="0.05",
                                           "#centralCO2BackSmoothingFactor"="0",
                                           "#centralCO2TargetReversionSpeedFactor"="4"))


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
            #print(paste("~/Dropbox/emlabGen/scenario/",filestump,marketStabilityReservenName[msrId],"-",backLoadingName[backLoadingId],"-",names(resPolicyScenarios)[resScenarioNo],"-",fuelPriceScenarios[fuelId],"-",centralPrivateDiscountingRateScenarioNames[discountingRateId],"-",centralCO2BackSmoothingFactorScenarioNames[backLookingId],"-",centralCO2TargetReversionSpeedFactorScenarioNames[targetReversionId],"-","C",priceCeiling,"-",microId,".xml", sep=""))
            #flush.console()
            writeLines(xmlFileContent, paste("~/Dropbox/emlabGen/scenario/",filestump,names(co2PolicyScenarios)[co2PolicyNo],"-",names(producerBankingScenarios)[producerBankingNo],"-",names(resPolicyScenarios)[resScenarioNo],"-",fuelPriceScenarios[fuelId],"-",microId,".xml", sep=""))
          }
        }
      }
      resScenarioNo<-resScenarioNo+1
    }
    producerBankingNo<-producerBankingNo+1
  }
  co2PolicyNo<-co2PolicyNo+1
}

                              

# Other sector banking ----------------------------------------------------
# Resetting standard values
filestump<-'OtherBank-'
producerBankingScenarios=list("BaseBanking-R3"=c("#stabilityReserveBankingFirstYear"="0.8",
                                               "#stabilityReserveBankingSecondYear"="0.5",
                                               "#stabilityReserveBankingThirdYear"="0.2",
                                               "#centralPrivateDiscountingRate"="0.05",
                                               "#centralCO2BackSmoothingFactor"="0",
                                               "#centralCO2TargetReversionSpeedFactor"="3"),
                              "LowerBanking-R3"=c("#stabilityReserveBankingFirstYear"="0.6",
                                                "#stabilityReserveBankingSecondYear"="0.3",
                                                "#stabilityReserveBankingThirdYear"="0.1",
                                                "#centralPrivateDiscountingRate"="0.05",
                                                "#centralCO2BackSmoothingFactor"="0",
                                                "#centralCO2TargetReversionSpeedFactor"="3"))

co2PolicyScenarios=list(MSR_OnlyElectBank=c("#emissionCapTimeline"="/data/emissionCapCweUk_unfccc_backloading_smoothed.csv",
                              "#stabilityReserveActive"="true",
                              "#stabilityReserveFirstYearOfOperation"="10",
                              "#stabilityReserveUpperTriggerStart"="399481202","#stabilityReserveUpperTriggerYearlyIncrease"="0",
                              "#stabilityReserveLowerTriggerTrendStart"="191827708","#stabilityReserveLowerTriggerTrendIncrease"="0",
                              "#stabilityReserveReleaseQuantityTrendStart"="47956927","#stabilityReserveReleaseQuantityTrendIncrease"="0",
                              "#stabilityReserveAddingMinimumTrendStart"="47956927","#stabilityReserveAddingMinimumTrendIncrease"="0",
                              "#stabilityReserveAddingPercentageTrendStart"="0.12","#stabilityReserveAddingPercentageTrendIncrease"="0"))


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
            #print(paste("~/Dropbox/emlabGen/scenario/",filestump,marketStabilityReservenName[msrId],"-",backLoadingName[backLoadingId],"-",names(resPolicyScenarios)[resScenarioNo],"-",fuelPriceScenarios[fuelId],"-",centralPrivateDiscountingRateScenarioNames[discountingRateId],"-",centralCO2BackSmoothingFactorScenarioNames[backLookingId],"-",centralCO2TargetReversionSpeedFactorScenarioNames[targetReversionId],"-","C",priceCeiling,"-",microId,".xml", sep=""))
            #flush.console()
            writeLines(xmlFileContent, paste("~/Dropbox/emlabGen/scenario/",filestump,names(co2PolicyScenarios)[co2PolicyNo],"-",names(producerBankingScenarios)[producerBankingNo],"-",names(resPolicyScenarios)[resScenarioNo],"-",fuelPriceScenarios[fuelId],"-",microId,".xml", sep=""))
          }
        }
      }
      resScenarioNo<-resScenarioNo+1
    }
    producerBankingNo<-producerBankingNo+1
  }
  co2PolicyNo<-co2PolicyNo+1
}


# Target Corridor Sensitivity ---------------------------------------------
eutETSCap<-read.csv(file="~/emlab-generation/emlab-generation/src/main/resources/data/emissionCapCweUk_unfccc.csv")
eutETSCap<-eutETSCap$X590387193.5312
stabilityReserveBankingFirstYear=0.8
stabilityReserveBankingSecondYear=0.5
stabilityReserveBankingThirdYear=0.2

expectedBanking<-numeric(length(eutETSCap)-3)
for(year in seq(1:(length(expectedBanking)))){
  expectedBanking[year]=stabilityReserveBankingFirstYear*eutETSCap[year+1]+stabilityReserveBankingSecondYear*eutETSCap[year+2]+
    stabilityReserveBankingThirdYear*eutETSCap[year+3]
}

upperTriggerPlus30Percent<-expectedBanking*1.3
lowerTriggerMinus30Percent<-expectedBanking*0.7

triggerDF30p<-data.frame(time=seq(1:length(expectedBanking)),expectedBanking=expectedBanking,upperTrigger=upperTriggerPlus30Percent,lowerTrigger=lowerTriggerMinus30Percent, cap=eutETSCap[1:length(lowerTriggerMinus30Percent)])
triggerPlot30p<-ggplot(data=triggerDF30p,aes(x=time))+
  geom_line(aes(y=expectedBanking))+
  geom_line(aes(y=upperTrigger))+
  geom_line(aes(y=lowerTrigger))+
  geom_line(aes(y=cap))
triggerPlot30p


addingPercentage30Percent<-(upperTriggerPlus30Percent-expectedBanking)/upperTriggerPlus30Percent/2
releasingQuantity30Percent<-(expectedBanking-lowerTriggerMinus30Percent)/2
addingMinimumZero<-numeric(length(releasingQuantity))

write.table(upperTriggerPlus30Percent, file="~/emlab-generation/emlab-generation/src/main/resources/data/msr-upper-trigger-30p.csv",sep=",",row.names=F, col.names=F)
write.table(lowerTriggerMinus30Percent, file="~/emlab-generation/emlab-generation/src/main/resources/data/msr-lower-trigger-30p.csv",sep=",",row.names=F, col.names=F)
write.table(releasingQuantity30Percent, file="~/emlab-generation/emlab-generation/src/main/resources/data/msr-release-quantity-30p.csv",sep=",",row.names=F, col.names=F)
write.table(addingPercentage30Percent, file="~/emlab-generation/emlab-generation/src/main/resources/data/msr-adding-percentage-30p.csv",sep=",",row.names=F, col.names=F)
write.table(addingMinimumZero, file="~/emlab-generation/emlab-generation/src/main/resources/data/msr-adding-minimum-zero.csv",sep=",",row.names=F, col.names=F)

upperTriggerPlus20Percent<-expectedBanking*1.2
lowerTriggerMinus20Percent<-expectedBanking*0.8

triggerDF20p<-data.frame(time=seq(1:length(expectedBanking)),expectedBanking=expectedBanking,upperTrigger=upperTriggerPlus20Percent,lowerTrigger=lowerTriggerMinus20Percent, cap=eutETSCap[1:length(lowerTriggerMinus30Percent)])
triggerPlot20p<-ggplot(data=triggerDF20p,aes(x=time))+
  geom_line(aes(y=expectedBanking))+
  geom_line(aes(y=upperTrigger))+
  geom_line(aes(y=lowerTrigger))+
  geom_line(aes(y=cap))
triggerPlot20p


addingPercentage20Percent<-(upperTriggerPlus20Percent-expectedBanking)/upperTriggerPlus20Percent/2
releasingQuantity20Percent<-(expectedBanking-lowerTriggerMinus20Percent)/2
addingMinimumZero<-numeric(length(releasingQuantity))

write.table(upperTriggerPlus20Percent, file="~/emlab-generation/emlab-generation/src/main/resources/data/msr-upper-trigger-20p.csv",sep=",",row.names=F, col.names=F)
write.table(lowerTriggerMinus20Percent, file="~/emlab-generation/emlab-generation/src/main/resources/data/msr-lower-trigger-20p.csv",sep=",",row.names=F, col.names=F)
write.table(addingPercentage20Percent, file="~/emlab-generation/emlab-generation/src/main/resources/data/msr-release-quantity-20p.csv",sep=",",row.names=F, col.names=F)
write.table(releasingQuantity20Percent, file="~/emlab-generation/emlab-generation/src/main/resources/data/msr-adding-percentage-20p.csv",sep=",",row.names=F, col.names=F)


upperTriggerPlus10Percent<-expectedBanking*1.1
lowerTriggerMinus10Percent<-expectedBanking*0.9

triggerDF10p<-data.frame(time=seq(1:length(expectedBanking)),expectedBanking=expectedBanking,upperTrigger=upperTriggerPlus10Percent,lowerTrigger=lowerTriggerMinus10Percent, cap=eutETSCap[1:length(lowerTriggerMinus30Percent)])
triggerPlot10p<-ggplot(data=triggerDF10p,aes(x=time))+
  geom_line(aes(y=expectedBanking))+
  geom_line(aes(y=upperTrigger))+
  geom_line(aes(y=lowerTrigger))+
  geom_line(aes(y=cap))
triggerPlot10p


addingPercentage10Percent<-(upperTriggerPlus10Percent-expectedBanking)/upperTriggerPlus10Percent/2
releasingQuantity10Percent<-(expectedBanking-lowerTriggerMinus10Percent)/2
addingMinimumZero<-numeric(length(releasingQuantity))

write.table(upperTriggerPlus10Percent, file="~/emlab-generation/emlab-generation/src/main/resources/data/msr-upper-trigger-10p.csv",sep=",",row.names=F, col.names=F)
write.table(lowerTriggerMinus10Percent, file="~/emlab-generation/emlab-generation/src/main/resources/data/msr-lower-trigger-10p.csv",sep=",",row.names=F, col.names=F)
write.table(addingPercentage10Percent, file="~/emlab-generation/emlab-generation/src/main/resources/data/msr-release-quantity-10p.csv",sep=",",row.names=F, col.names=F)
write.table(releasingQuantity10Percent, file="~/emlab-generation/emlab-generation/src/main/resources/data/msr-adding-percentage-10p.csv",sep=",",row.names=F, col.names=F)

producerBankingScenarios=list("BaseBanking-R3"=c("#stabilityReserveBankingFirstYear"="0.8",
                                                 "#stabilityReserveBankingSecondYear"="0.5",
                                                 "#stabilityReserveBankingThirdYear"="0.2",
                                                 "#centralPrivateDiscountingRate"="0.05",
                                                 "#centralCO2BackSmoothingFactor"="0",
                                                 "#centralCO2TargetReversionSpeedFactor"="3"
))

co2PolicyScenarios=list(MSRTarget30Percent=c("#emissionCapTimeline"="/data/emissionCapCweUk_unfccc_backloading_smoothed.csv",
                              "#stabilityReserveActive"="true",
                              "#stabilityReserveFirstYearOfOperation"="10",
                              "#stabilityReserveUpperTriggerTrend"="/data/msr-upper-trigger-30p.csv",
                              "#stabilityReserveLowerTriggerTrend"="/data/msr-lower-trigger-30p.csv",
                              "#stabilityReserveReleaseQuantityTrend"="/data/msr-release-quantity-30p.csv",
                              "#stabilityReserveAddingMinimumTrend"="/data/msr-adding-minimum-zero.csv",
                              "#stabilityReserveAddingPercentageTrend"="/data/msr-adding-percentage-30p.csv"),
			      MSRTarget20Percent=c("#emissionCapTimeline"="/data/emissionCapCweUk_unfccc_backloading_smoothed.csv",
                              "#stabilityReserveActive"="true",
                              "#stabilityReserveFirstYearOfOperation"="10",
                              "#stabilityReserveUpperTriggerTrend"="/data/msr-upper-trigger-20p.csv",
                              "#stabilityReserveLowerTriggerTrend"="/data/msr-lower-trigger-20p.csv",
                              "#stabilityReserveReleaseQuantityTrend"="/data/msr-release-quantity-20p.csv",
                              "#stabilityReserveAddingMinimumTrend"="/data/msr-adding-minimum-zero.csv",
                              "#stabilityReserveAddingPercentageTrend"="/data/msr-adding-percentage-20p.csv"),
			      MSRTarget10Percent=c("#emissionCapTimeline"="/data/emissionCapCweUk_unfccc_backloading_smoothed.csv",
                              "#stabilityReserveActive"="true",
                              "#stabilityReserveFirstYearOfOperation"="10",
                              "#stabilityReserveUpperTriggerTrend"="/data/msr-upper-trigger-10p.csv",
                              "#stabilityReserveLowerTriggerTrend"="/data/msr-lower-trigger-10p.csv",
                              "#stabilityReserveReleaseQuantityTrend"="/data/msr-release-quantity-10p.csv",
                              "#stabilityReserveAddingMinimumTrend"="/data/msr-adding-minimum-zero.csv",
                              "#stabilityReserveAddingPercentageTrend"="/data/msr-adding-percentage-10p.csv"),)


# # Sensitivity Fuel scenarios -----------------------------------------------
# 
# 
# fuelPriceScenarioLength=2
# fuelPriceScenarios = c("FuelHigh","FuelLow")
# # Step 3 building the scenarios: estimating the last three parameters

# # Sensitivity RES Scenarios ----------------------------------------------
# 
# 
# fuelPriceScenarioLength=1
# resPolicyScenarios=list(HRES=c("#cweResPolicy"="/data/policyGoalNREAP_CF_CWE-half.csv","#gbResPolicy"="/data/policyGoalNREAP_CF_UK-half.csv"),
#                         ZRES=c("#cweResPolicy"="/data/policyGoalNREAP_CF_CWE-null.csv","#gbResPolicy"="/data/policyGoalNREAP_CF_UK-null.csv"))
# fuelPriceScenarios = c("FuelCentral")
# 


# Sensitivity Recession ---------------------------------------------------
#Sensitivity Analysis for a recession in year 25-32

#Placeholders

# Step 1 building the scenarios: insert dataframe and read scenarioA.xml file
xmlFilePath<-"~/emlab-generation/co2MarketStabilityReserve-scenarioTemplate.xml"
filestump<-'RecessionBase-'
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
producerBankingScenarios=list("BaseBanking-R3"=c("#stabilityReserveBankingFirstYear"="0.8",
                                                 "#stabilityReserveBankingSecondYear"="0.5",
                                                 "#stabilityReserveBankingThirdYear"="0.2",
                                                 "#centralPrivateDiscountingRate"="0.05",
                                                 "#centralCO2BackSmoothingFactor"="0",
                                                 "#centralCO2TargetReversionSpeedFactor"="3"
))

co2PolicyScenarios=list(PureETS=c("#emissionCapTimeline"="/data/emissionCapCweUk.csv",
                                  "#stabilityReserveActive"="false",
                                  "#stabilityReserveFirstYearOfOperation"="100",
                                  "#stabilityReserveUpperTriggerStart"="0","#stabilityReserveUpperTriggerYearlyIncrease"="0",
                                  "#stabilityReserveLowerTriggerTrendStart"="0","#stabilityReserveLowerTriggerTrendIncrease"="0",
                                  "#stabilityReserveReleaseQuantityTrendStart"="0","#stabilityReserveReleaseQuantityTrendIncrease"="0",
                                  "#stabilityReserveAddingMinimumTrendStart"="0","#stabilityReserveAddingMinimumTrendIncrease"="0",
                                  "#stabilityReserveAddingPercentageTrendStart"="0","#stabilityReserveAddingPercentageTrendIncrease"="0"),
                        Backloading=c("#emissionCapTimeline"="/data/emissionCapCweUk_unfccc_backloading_smoothed.csv",
                                      "#stabilityReserveActive"="false",
                                      "#stabilityReserveFirstYearOfOperation"="100",
                                      "#stabilityReserveUpperTriggerStart"="0","#stabilityReserveUpperTriggerYearlyIncrease"="0",
                                      "#stabilityReserveLowerTriggerTrendStart"="0","#stabilityReserveLowerTriggerTrendIncrease"="0",
                                      "#stabilityReserveReleaseQuantityTrendStart"="0","#stabilityReserveReleaseQuantityTrendIncrease"="0",
                                      "#stabilityReserveAddingMinimumTrendStart"="0","#stabilityReserveAddingMinimumTrendIncrease"="0",
                                      "#stabilityReserveAddingPercentageTrendStart"="0","#stabilityReserveAddingPercentageTrendIncrease"="0"),
                        MSR=c("#emissionCapTimeline"="/data/emissionCapCweUk_unfccc_backloading_smoothed.csv",
                              "#stabilityReserveActive"="true",
                              "#stabilityReserveFirstYearOfOperation"="10",
                              "#stabilityReserveUpperTriggerStart"="289283608","#stabilityReserveUpperTriggerYearlyIncrease"="0",
                              "#stabilityReserveLowerTriggerTrendStart"="138911697","#stabilityReserveLowerTriggerTrendIncrease"="0",
                              "#stabilityReserveReleaseQuantityTrendStart"="34727924","#stabilityReserveReleaseQuantityTrendIncrease"="0",
                              "#stabilityReserveAddingMinimumTrendStart"="34727924","#stabilityReserveAddingMinimumTrendIncrease"="0",
                              "#stabilityReserveAddingPercentageTrendStart"="0.12","#stabilityReserveAddingPercentageTrendIncrease"="0"))

microScenarioNo<-seq(1,microScenarioLength)

#No Backloading, smoothed backgloading, backloading
#backLoadingName=c("NBL","SBL","BL")
#backLoadingValue=c("/data/emissionCapCweUk.csv","/data/emissionCapCweUk_unfccc_backloading_smoothed.csv","/data/emissionCapCweUk_unfccc_backloading.csv")
#backLoadingName=c("NBL","SBL","BL")
#backLoadingValue=c("/data/emissionCapCweUk_citl.csv","/data/emissionCapCweUk_citl_backloading_smoothed.csv","/data/emissionCapCweUk_Citl_backloading.csv")

# Step 3 building the scenarios: estimating the last three parameters
#${initial_propensity}



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
            xmlFileContent<-gsub("#demandPathandFilename", paste("/data/stochasticDemandCWEandGBRecession/demand-",microId,".csv", sep="") , xmlFileContent)
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
            #print(paste("~/Dropbox/emlabGen/scenario/",filestump,marketStabilityReservenName[msrId],"-",backLoadingName[backLoadingId],"-",names(resPolicyScenarios)[resScenarioNo],"-",fuelPriceScenarios[fuelId],"-",centralPrivateDiscountingRateScenarioNames[discountingRateId],"-",centralCO2BackSmoothingFactorScenarioNames[backLookingId],"-",centralCO2TargetReversionSpeedFactorScenarioNames[targetReversionId],"-","C",priceCeiling,"-",microId,".xml", sep=""))
            #flush.console()
            writeLines(xmlFileContent, paste("~/Dropbox/emlabGen/scenario/",filestump,names(co2PolicyScenarios)[co2PolicyNo],"-",names(producerBankingScenarios)[producerBankingNo],"-",names(resPolicyScenarios)[resScenarioNo],"-",fuelPriceScenarios[fuelId],"-",microId,".xml", sep=""))
          }
        }
      }
      resScenarioNo<-resScenarioNo+1
    }
    producerBankingNo<-producerBankingNo+1
  }
  co2PolicyNo<-co2PolicyNo+1
}

# Sensitivity Recession ---------------------------------------------------
#Sensitivity Analysis for a recession in year 25-32


