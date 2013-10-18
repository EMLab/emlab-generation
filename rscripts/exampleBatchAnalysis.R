setwd("~/emlab-generation/rscripts/")
library(xtable)
library(scales)
source("rConfig.R")
source("batchRunAnalysis.R")
setwd(analysisFolder)

safePlots=F
showPlots=T
scaleFactor<-1
nrowLength<-1

socialDiscountRate<-0.03
##---- Read in of Data                     ------------------

bigDF <- getDataFrameForModelRunsInFolderWithFilePattern(resultFolder,"X-*-FRES-DeccCentral.csv")
#bigDF <- getDataFrameForModelRunsInFolder(resultFolder)
filePrefix<-"FRES-DeccCentral-"
#  bigDF$modelRun<-renamer(bigDF$modelRun,"-FRES","")
bigDF$modelRun<-renamer(bigDF$modelRun,"-FRES-DeccCentral","")
bigDF$modelRun<-renamer(bigDF$modelRun,"X-","")
bigDF$modelRun<-renamer(bigDF$modelRun,"BothMinBothMax","BMinMax")
##---- Calculation of additional variables ------------------
bigDF$modelRun<-factor(bigDF$modelRun,c("PureETS","MinGB","MinCWE","BothMin","BMinMax"))
#bigDF$modelRun<-factor(bigDF$modelRun,c("PureETS-FRES-DeccCentral","MinGB-FRES-DeccCentral","MinCWE-FRES-DeccCentral","BothMin-FRES-DeccCentral","BothMinBothMax-FRES-DeccCentral",
#                                        "PureETS-FRES-DeccLow","MinGB-FRES-DeccLow","MinCWE-FRES-DeccLow","BothMin-FRES-DeccLow","BothMinBothMax-FRES-DeccLow",
#                                        "PureETS-FRES-DeccHigh","MinGB-FRES-DeccHigh","MinCWE-FRES-DeccHigh","BothMin-FRES-DeccHigh","BothMinBothMax-FRES-DeccHigh",
#                                        "PureETS-HRES-DeccCentral","MinGB-HRES-DeccCentral","MinCWE-HRES-DeccCentral","BothMin-HRES-DeccCentral","BothMinBothMax-HRES-DeccCentral",
#                                        "PureETS-HRES-DeccLow","MinGB-HRES-DeccLow","MinCWE-HRES-DeccLow","BothMin-HRES-DeccLow","BothMinBothMax-HRES-DeccLow",
#                                        "PureETS-HRES-DeccHigh","MinGB-HRES-DeccHigh","MinCWE-HRES-DeccHigh","BothMin-HRES-DeccHigh","BothMinBothMax-HRES-DeccHigh",
#                                        "PureETS-ZRES-DeccCentral","MinGB-ZRES-DeccCentral","MinCWE-ZRES-DeccCentral","BothMin-ZRES-DeccCentral","BothMinBothMax-ZRES-DeccCentral",
#                                        "PureETS-ZRES-DeccLow","MinGB-ZRES-DeccLow","MinCWE-ZRES-DeccLow","BothMin-ZRES-DeccLow","BothMinBothMax-ZRES-DeccLow",
#                                        "PureETS-ZRES-DeccHigh","MinGB-ZRES-DeccHigh","MinCWE-ZRES-DeccHigh","BothMin-ZRES-DeccHigh","BothMinBothMax-ZRES-DeccHigh"))

bigDF$stochasticId<-bigDF$runId
bigDF$stochasticId<-renamer(bigDF$stochasticId,"X-PureETS-FRES-DeccCentral-","")
bigDF$stochasticId<-renamer(bigDF$stochasticId,"X-MinGB-FRES-DeccCentral-","")
bigDF$stochasticId<-renamer(bigDF$stochasticId,"X-MinCWE-FRES-DeccCentral-","")
bigDF$stochasticId<-renamer(bigDF$stochasticId,"X-BothMinBothMax-FRES-DeccCentral-","")
bigDF$stochasticId<-renamer(bigDF$stochasticId,"X-BothMin-FRES-DeccCentral-","")
bigDF$stochasticId<-substr(x=bigDF$stochasticId,1,nchar(bigDF$stochasticId)-2)

bigDF <- addSupplyRatios(bigDF)
bigDF <- addSumOfVariablesByPrefixToDF(bigDF, "ProducerCash")

bigDF$ProducerCashInGB<-bigDF$ProducerCash_Energy.Producer.A+bigDF$ProducerCash_Energy.Producer.B+bigDF$ProducerCash_Energy.Producer.C+bigDF$ProducerCash_Energy.Producer.D
bigDF$ProducerCashInCWE<-bigDF$ProducerCash_Energy.Producer.E+ bigDF$ProducerCash_Energy.Producer.F+bigDF$ProducerCash_Energy.Producer.G+bigDF$ProducerCash_Energy.Producer.H

# Attention: need to adjust the initial differential value to the starting cash balances of producers!
bigDF<-ddply(bigDF, .(runId), diffExpenditures2, list(c("ProducerCash_Energy.Producer.A","ProducerCashFlow_Energy.Producer.A"),c("ProducerCash_Energy.Producer.B","ProducerCashFlow_Energy.Producer.B"),c("ProducerCash_Energy.Producer.C","ProducerCashFlow_Energy.Producer.C"),c("ProducerCash_Energy.Producer.D","ProducerCashFlow_Energy.Producer.D")),5000000000)
bigDF<-ddply(bigDF, .(runId), diffExpenditures2, list(c("ProducerCash_Energy.Producer.E","ProducerCashFlow_Energy.Producer.E"),c("ProducerCash_Energy.Producer.F","ProducerCashFlow_Energy.Producer.F"),c("ProducerCash_Energy.Producer.G","ProducerCashFlow_Energy.Producer.G"),c("ProducerCash_Energy.Producer.H","ProducerCashFlow_Energy.Producer.H")),20000000000)
bigDF<-ddply(bigDF, .(runId), diffExpenditures2, list(c("EUGovernmentCash","Co2PolicyIncome_EU"),c("NationalGovernmentCash_Country.A","Co2PolicyIncome_Country.A"),c("NationalGovernmentCash_Country.B","Co2PolicyIncome_Country.B"),c("SpotMarketCash_GB.electricity.spot.market","Consumer_Cost.GB"),c("SpotMarketCash_CWE.electricity.spot.market","Consumer_Cost.CWE"),c("ProducerCash_Renewable.Target.Investor.CWE","RenewableSubsidy.CWE"),c("ProducerCash_Renewable.Target.Investor.GB","RenewableSubsidy.GB")),0)

bigDF$ProducerProfit.GB<-bigDF$ProducerCashFlow_Energy.Producer.A+bigDF$ProducerCashFlow_Energy.Producer.B+bigDF$ProducerCashFlow_Energy.Producer.C+bigDF$ProducerCashFlow_Energy.Producer.D
bigDF$ProducerProfit.CWE<-bigDF$ProducerCashFlow_Energy.Producer.E+bigDF$ProducerCashFlow_Energy.Producer.F+bigDF$ProducerCashFlow_Energy.Producer.G+bigDF$ProducerCashFlow_Energy.Producer.H

bigDF$ConsumerCostinclSubs.GB<-(bigDF$Consumer_Cost.GB+bigDF$RenewableSubsidy.GB)*-1
bigDF$ConsumerCostinclSubs.CWE<-(bigDF$Consumer_Cost.CWE+bigDF$RenewableSubsidy.CWE)*-1

bigDF$SpecificConsumerCostinclSubs.GB<-bigDF$ConsumerCostinclSubs.GB/bigDF$Total_DemandinMWh_Country.B
bigDF$SpecificConsumerCostinclSubs.CWE<-bigDF$ConsumerCostinclSubs.CWE/bigDF$Total_DemandinMWh_Country.A

bigDF$SpecificConsumerCost.GB<-bigDF$Consumer_Cost.GB/bigDF$Total_DemandinMWh_Country.B*-1
bigDF$SpecificConsumerCost.CWE<-bigDF$Consumer_Cost.CWE/bigDF$Total_DemandinMWh_Country.A*-1

bigDF$SpecificRenewableSubsidy.GB<-bigDF$RenewableSubsidy.GB/bigDF$Total_DemandinMWh_Country.B*-1
bigDF$SpecificRenewableSubsidy.CWE<-bigDF$RenewableSubsidy.CWE/bigDF$Total_DemandinMWh_Country.A*-1

bigDF$OverallCost<-bigDF$ProducerProfit.GB+bigDF$ProducerProfit.CWE+bigDF$Consumer_Cost.GB+bigDF$Consumer_Cost.CWE+bigDF$Co2PolicyIncome_EU+bigDF$Co2PolicyIncome_Country.A+bigDF$Co2PolicyIncome_Country.B+bigDF$RenewableSubsidy.CWE+bigDF$RenewableSubsidy.GB

bigDF[grepl("BMinMax",bigDF$modelRun),]$CO2Auction<-pmax(bigDF[grepl("BMinMax",bigDF$modelRun),]$CO2Auction,bigDF[grepl("BMinMax",bigDF$modelRun),]$NationalMinCO2PriceinEURpTon_Country.B)
bigDF[grepl("BothMin",bigDF$modelRun),]$CO2Auction<-pmax(bigDF[grepl("BothMin",bigDF$modelRun),]$CO2Auction,bigDF[grepl("BothMin",bigDF$modelRun),]$NationalMinCO2PriceinEURpTon_Country.B)


##---- CO2 Price & Emissions Plots  -----------------
priceCeiling<-data.frame(tick=seq(0,39),priceCeiling=seq(60,138,2),modelRun=rep("BMinMax",40))

co2PricePlot<-plotTimeSeriesWithConfidenceIntervalByFacettedGroup(bigDF, "CO2Auction", expression(paste(CO[2], ' Price [EUR/ton]')), nrow=nrowLength)+
geom_line(aes(y=NationalMinCO2PriceinEURpTon_Country.A),colour="black", linetype=2)+
geom_line(aes(y=NationalMinCO2PriceinEURpTon_Country.B),colour="black", linetype=4)+
  geom_line(data=priceCeiling,aes(x=tick,y=priceCeiling),linetype=3)+
  theme_publication(base_size=9)+
  geom_rug(sides='l')+
  theme(legend.position="none")
if(showPlots) co2PricePlot
if(safePlots) ggsave(filename= paste(filePrefix, "co2PricePlot.pdf", sep=""),plot=co2PricePlot, width=16.51, height=7, units="cm", scale=scaleFactor)

co2SpaghettiPrice<-plotSpaghettiTimeSeries(df=bigDF,variable="CO2Auction",ylabel="CO2 Price [EUR/ton]", nrow=1)+
  geom_line(aes(y=NationalMinCO2PriceinEURpTon_Country.A),colour="black", linetype=2)+
  geom_line(aes(y=NationalMinCO2PriceinEURpTon_Country.B),colour="black", linetype=4)+  
  theme_tufte()+
  theme(legend.position="none", axis.line=element_line())
if(showPlots) co2SpaghettiPrice
if(safePlots) ggsave(filename= paste(filePrefix, "co2PriceSpaghetti.pdf", sep=""),plot=co2SpaghettiPrice, width=16.51, height=7.22, units="cm", scale=scaleFactor)

co2MaxPrices<-ddply(bigDF, .variables=c("runId", "modelRun"), .fun=functionOfVariablePerRunId, function(x){max(x)}, "CO2Auction")
co2MaxPricesPlot<-ggplot(co2MaxPrices,aes(x=V1))+
  geom_histogram(binwidth=10)+
  facet_wrap( ~ modelRun, nrow=1)+
  ylab("Outage years")+  
  theme_tufte()+
  geom_rug()
if(showPlots) co2MaxPricesPlot

cumCo2MaxPricesPlot<-ggplot(co2MaxPrices,aes(x=V1))+
  stat_ecdf()+
  facet_wrap( ~ modelRun, nrow=2)+
  ylab(expression(paste("Cumulative distribution")))+
  xlab(expression(paste(CO[2], " Price [EUR/",ton[CO2],"]")))+
  scale_y_continuous(label=percent)+
  theme_publication(base_size=9)
if(showPlots) cumCo2MaxPricesPlot
if(safePlots) ggsave(filename= paste(filePrefix, "cumCo2MaxPricesPlot.pdf", sep=""),plot=cumCo2MaxPricesPlot,width=7.83, height=7.83, units="cm", scale=scaleFactor)

ddply(co2MaxPrices, .variables="modelRun", .fun=applyFunToColumnInDF ,fun=quantile, probs = seq(0, 1, 0.05), column="V1")

co2MaxPriceYears<-ddply(bigDF, .variables=c("runId", "modelRun"), .fun=functionOfVariablePerRunId, function(x){sum(x>150)}, "CO2Auction")
co2MaxPriceYearsPlot<-ggplot(co2MaxPriceYears,aes(x=V1))+
  geom_bar(binwidth=1)+
  facet_wrap( ~ modelRun, nrow=1)+
  ylab("Years with prices above 200 EUR/ton")
if(showPlots) co2MaxPriceYearsPlot

cumCo2MaxPriceYearsPlot<-ggplot(co2MaxPriceYears,aes(x=V1))+
  stat_ecdf()+
  facet_wrap( ~ modelRun, nrow=1)+
  xlab(expression(paste("Years with prices above 200 EUR/",ton[CO[2]])))+
  ylab("Percentage")
if(showPlots) cumCo2MaxPriceYearsPlot
if(safePlots) ggsave(filename= paste(filePrefix, "cumCo2MaxPriceYearsPlot.pdf", sep=""),plot=cumCo2MaxPriceYearsPlot, scale=scaleFactor.5, width=16.51, height=7.22, units="cm", scale=scaleFactor)

ddply(co2MaxPriceYears, .variables="modelRun", .fun=applyFunToColumnInDF ,fun=quantile, probs = seq(0, 1, 0.05), column="V1")

co2EmissionsSpaghetti<-plotSpaghettiTimeSeries(df=bigDF,variable="CO2Emissions_inTonpA",ylabel="CO2 Emissions [t/a]")
co2EmissionsSpaghetti<-co2EmissionsSpaghetti+geom_line(aes(y=CO2CapinTonpA_CO2_cap),linetype = 2, colour="red", size=1)
if(showPlots) co2EmissionsSpaghetti
if(safePlots) ggsave(filename= paste(filePrefix, "co2EmissionsSpaghetti.pdf", sep=""),plot=co2EmissionsSpaghetti, width=15.66, height=10.44, units="cm", scale=scaleFactor)

totalCO2cap=sum(bigDF[bigDF$runId=="X-BothMinBothMax-FRES-DeccCentral-100-1",]$CO2CapinTonpA_CO2_cap)
print(xtable(ddply(ddply(bigDF, .variables=c("runId", "modelRun"), .fun=functionOfVariablePerRunId,  function(x){sum(x)/totalCO2cap}, "CO2Emissions_inTonpA"), .variables="modelRun", .fun=applyFunToColumnInDF, fun=quantile, column="V1"),caption="Emitted CO\2 expressed as percent of emission cap",label="tab:co2emissions"), bookabs=T, include.rownames=F)

accumulatedEmissions<-ddply(bigDF, .variables=c("runId", "modelRun"), .fun=functionOfVariablePerRunId,  function(x){sum(x)/totalCO2cap}, "CO2Emissions_inTonpA")
emissionsBarPlot<-ggplot(accumulatedEmissions,aes(x=V1))+
  #stat_ecdf()+
  facet_wrap( ~ modelRun, nrow=2)+
  geom_histogram(aes(y=..count../sum(..count..)*5), fill="black")+
  #geom_freqpoly(aes(y=..density..))+
  xlab(expression(paste("Emitted ",CO[2],"  as % of total ",CO[2]," cap")))+
  ylab("Distribution")+
  scale_y_continuous(labels = percent)+
  theme_publication(base_size=9)
if(showPlots) emissionsBarPlot
if(safePlots) ggsave(filename= paste(filePrefix, "emissionsBarPlot.pdf", sep=""),plot=emissionsBarPlot,width=7.83, height=7.83, units="cm", scale=scaleFactor)

co2EmissionPlot<-plotTimeSeriesWithConfidenceIntervalByFacettedGroup(bigDF, "CO2Emissions_inTonpA", "CO2 Emissions [t/a]", nrow=nrowLength)
co2EmissionPlot<-co2EmissionPlot+geom_line(aes(y=CO2CapinTonpA_CO2_cap),linetype = 2, colour="red", size=1)
if(showPlots) co2EmissionPlot

##---- Electricity & Fuel Price Plots  ------------------
avgPricePlotinCWE<-plotSpaghettiTimeSeries(bigDF, "Avg_El_PricesinEURpMWh_Country.A", "Avg. Electricity Price in Country A [EUR/MW]")
if(showPlots) avgPricePlotinCWE
if(safePlots) ggsave(filename= paste(filePrefix, "avgPricePlotinCWE.pdf", sep=""),plot=avgPricePlotinCWE, width=15.66, height=10.44, units="cm", scale=scaleFactor)

maxAvgElectrictyPricesCWE<-ddply(bigDF, .variables=c("runId", "modelRun"), .fun=functionOfVariablePerRunId, function(x){max(x)}, "Avg_El_PricesinEURpMWh_Country.A")
maxAvgElectrictyPricesPlotCWE<-ggplot(maxAvgElectrictyPricesCWE,aes(x=V1))+
  geom_histogram(binwidth=10)+
  facet_wrap( ~ modelRun)+
  ylab("Maximum Avg. Electricity Price [EUR/MWh]")
if(showPlots) maxAvgElectrictyPricesPlotCWE

cumMaxAvgElectrictyPricesPlotCWE<-ggplot(maxAvgElectrictyPricesCWE,aes(x=V1))+
  stat_ecdf()+
  facet_wrap( ~ modelRun, nrow=1)+
  ylab("Maximum Avg. Electricity Price [EUR/MWh]")
if(showPlots) cumMaxAvgElectrictyPricesPlotCWE
if(safePlots) ggsave(filename= paste(filePrefix, "cumMaxAvgElectrictyPricesPlotCWE.pdf", sep=""),plot=cumMaxAvgElectrictyPricesPlotCWE, width=15.66, height=10.44, units="cm", scale=scaleFactor)

electrictyPricesPeakYearsCWE<-ddply(bigDF, .variables=c("runId", "modelRun"), .fun=functionOfVariablePerRunId, function(x){sum(x>100)}, "Avg_El_PricesinEURpMWh_Country.A")
electrictyPricesPeakYearsPlotCWE<-ggplot(electrictyPricesPeakYearsCWE,aes(x=V1))+
  geom_bar(binwidth=1)+
  facet_wrap( ~ modelRun)+
  ylab("Years with prices above 100 EUR/ton")
if(showPlots) electrictyPricesPeakYearsPlotCWE

cumElectrictyPricesPeakYearsPlotCWE<-ggplot(electrictyPricesPeakYearsCWE,aes(x=V1))+
  stat_ecdf()+
  facet_wrap( ~ modelRun)+
  ylab("Years with prices above 100 EUR/ton")
if(showPlots) cumElectrictyPricesPeakYearsPlotCWE

avgPricePlotinB<-plotTimeSeriesWithConfidenceIntervalByFacettedGroup(bigDF, "Avg_El_PricesinEURpMWh_Country.B", "Avg. Electricity Price in Country B [EUR/MW]", nrow=nrowLength)
if(showPlots) avgPricePlotinB
if(safePlots) ggsave(filename= paste(filePrefix, "avgPricePlotinB.pdf", sep=""),plot=avgPricePlotinB, width=15.66, height=10.44, units="cm", scale=scaleFactor)

maxAvgElectrictyPricesGB<-ddply(bigDF, .variables=c("runId", "modelRun"), .fun=functionOfVariablePerRunId, function(x){max(x)}, "Avg_El_PricesinEURpMWh_Country.B")
maxAvgElectrictyPricesPlotGB<-ggplot(maxAvgElectrictyPricesGB,aes(x=V1))+
  geom_histogram(binwidth=10)+
  facet_wrap( ~ modelRun)+
  ylab("Maximum Avg. Electricity Price [EUR/MWh]")
if(showPlots) maxAvgElectrictyPricesPlotGB



electrictyPricesPeakYearsCWE<-ddply(bigDF, .variables=c("runId", "modelRun"), .fun=functionOfVariablePerRunId, function(x){sum(x>100)}, "Avg_El_PricesinEURpMWh_Country.B")
electrictyPricesPeakYearsPlotCWE<-ggplot(electrictyPricesPeakYearsCWE,aes(x=V1))+
  geom_bar(binwidth=1)+
  facet_wrap( ~ modelRun)+
  ylab("Years with prices above 100 EUR/ton")
if(showPlots) electrictyPricesPeakYearsPlotCWE

coalPrice<-plotTimeSeriesWithConfidenceIntervalGroupedInOnePlot(bigDF, "FuelPricesPerGJ_Coal", "Coal")  
if(showPlots) coalPrice
#if(safePlots) ggsave(filename= paste(filePrefix, "coalPrice.pdf", sep=""),plot=coalPrice, width=16.51, height=7.22, units="cm", scale=scaleFactor)

gasPrice<-plotTimeSeriesWithConfidenceIntervalGroupedInOnePlot(bigDF, "FuelPricesPerGJ_Natural.Gas", "Gas")  
if(showPlots) gasPrice
#if(safePlots) ggsave(filename= paste(filePrefix, "gasPrice.pdf", sep=""),plot=gasPrice, width=16.51, height=7.22, units="cm", scale=scaleFactor)

biomassPrice<-plotTimeSeriesWithConfidenceIntervalGroupedInOnePlot(bigDF, "FuelPricesPerGJ_Biomass", "Biomass")
if(showPlots) biomassPrice
#if(safePlots) ggsave(filename= paste(filePrefix, "biomassPrice.pdf", sep=""),plot=biomassPrice, width=16.51, height=7.22, units="cm", scale=scaleFactor)

##---- Power Plant Capacities & Generation Plots ------------------

supplyRatioCWE<-plotTimeSeriesWithConfidenceIntervalByFacettedGroup(bigDF, "SupplyRatio_Country.A", "Peak Capacity Supply Ratio CWE", nrow=nrowLength)
if(showPlots) supplyRatioCWE

supplyRatioGB<-plotTimeSeriesWithConfidenceIntervalByFacettedGroup(bigDF, "SupplyRatio_Country.B", "Peak Capacity Supply Ratio GB", nrow=nrowLength)
if(showPlots) supplyRatioGB

outageYearsInGB<-ddply(bigDF, .variables=c("runId", "modelRun"), .fun=functionOfVariablePerRunId, function(x){sum(x<1)}, "SupplyRatio_Country.B")
outageYearsInGBPlot<-ggplot(outageYearsInGB,aes(x=V1))+
  geom_bar(binwidth=1)+
  facet_wrap( ~ modelRun, nrow=9)+
  ylab("Outage years")
if(showPlots) outageYearsInGBPlot

outageYearsInCWE<-ddply(bigDF, .variables=c("runId", "modelRun"), .fun=functionOfVariablePerRunId, function(x){sum(x<1)}, "SupplyRatio_Country.A")
outageYearsInCWEPlot<-ggplot(outageYearsInCWE,aes(x=V1))+
  geom_bar(binwidth=1)+
  facet_wrap( ~ modelRun, nrow=9)+
  ylab("Outage years")
if(showPlots) outageYearsInCWEPlot


moltenCapacities<-meltTechnologyVariable(bigDF,"CapacityinMW_")
moltenCapacities$value<-moltenCapacities$value/1000
stackedCapacities<-plotStackedTechnologyDiagram(moltenVariable=moltenCapacities,ylabel="Capacity [GW]")
if(showPlots) stackedCapacities
if(safePlots) ggsave(filename= paste(filePrefix, "stackedCapacityDiagram.pdf", sep=""),plot=stackedCapacities, width= 15.66, height= 14, units="cm", scale=scaleFactor)

generationCapacitiesFacetted<-plotMoltenVariableFacettedByVariable(moltenCapacities, "Capacity [GW]")
if(showPlots) generationCapacitiesFacetted
if(safePlots) ggsave(filename= paste(filePrefix, "generationCapacitiesFacetted.pdf", sep=""),plot=generationCapacitiesFacetted, width=30, height=16.51, units="cm", scale=scaleFactor)

moltenGeneration<-meltTechnologyVariable(bigDF, "GenerationinMWh_")
moltenGeneration$value<-moltenGeneration$value/1000000 #To TWh
stackedGeneration<-plotStackedTechnologyDiagram(moltenVariable=moltenGeneration,ylabel="Generation [TWh]")
if(showPlots) stackedGeneration
if(safePlots) ggsave(filename= paste(filePrefix, "stackedGenerationDiagram.pdf", sep=""),plot=stackedGeneration, width=16.51, height=8.255, units="cm", scale=scaleFactor)

generationFacetted<-plotMoltenVariableFacettedByVariable(moltenGeneration, "Generation [TWh]")
if(showPlots) generationFacetted
if(safePlots) ggsave(filename= paste(filePrefix, "generationFacetted.pdf", sep=""),plot=generationFacetted, width=16.51, height=21.94, units="cm", scale=scaleFactor)

moltenCapacityinCWE<-meltTechnologyVariable(bigDF,"CapacityinMWinA_")
moltenCapacityinCWE$value<-moltenCapacityinCWE$value/1000
stackedCapacitiesinCWEMedian<-plotStackedTechnologyDiagram(moltenVariable=moltenCapacityinCWE,ylabel="Capacity [GW]", summaryFunction=median)
if(showPlots) stackedCapacitiesinCWEMedian
stackedCapacitiesinCWEMean<-plotStackedTechnologyDiagram(moltenVariable=moltenCapacityinCWE,ylabel="Capacity [GW]", summaryFunction=mean)
if(showPlots) stackedCapacitiesinCWEMean
if(safePlots) ggsave(filename= paste(filePrefix, "stackedCapacityinCWEDiagram.pdf", sep=""),plot=stackedCapacitiesinCWEMean, width= 15.66, height= 14, units="cm", scale=scaleFactor)


moltenCapacityinGB<-meltTechnologyVariable(bigDF,"CapacityinMWinB_")
moltenCapacityinGB$value<-moltenCapacityinGB$value/1000
stackedCapacitiesinGBMedian<-plotStackedTechnologyDiagram(moltenVariable=moltenCapacityinGB,ylabel="Capacity [GW]", summaryFunction=median)
if(showPlots) stackedCapacitiesinGBMedian
stackedCapacitiesinGBMean<-plotStackedTechnologyDiagram(moltenVariable=moltenCapacityinGB,ylabel="Capacity [GW]", summaryFunction=mean)
if(showPlots) stackedCapacitiesinGBMean
if(safePlots) ggsave(filename= paste(filePrefix, "stackedCapacityinGBDiagram.pdf", sep=""),plot=stackedCapacitiesinGBMean, width= 15.66, height= 14, units="cm", scale=scaleFactor)

moltenCapacityinCWE$zone<-rep("CWE",dim(moltenCapacityinCWE)[1])
moltenCapacityinGB$zone<-rep("GB",dim(moltenCapacityinCWE)[1])
allMoltenCapacities<-rbind(moltenCapacityinCWE,moltenCapacityinGB)

capacitiesinGBFaceted<-plotMoltenVariableFacettedByVariable(moltenCapacityinGB, "Capacity [GW]")
if(showPlots) capacitiesinGBFaceted
if(safePlots) ggsave(filename= paste(filePrefix, "capacitiesinGBFaceted.pdf", sep=""),plot=capacitiesinGBFaceted, width=16.51, height=21.94, units="cm", scale=scaleFactor)

capacitiesinCWEFaceted<-plotMoltenVariableFacettedByVariable(moltenCapacityinCWE, "Capacity [GW]")
if(showPlots) capacitiesinCWEFaceted
if(safePlots) ggsave(filename= paste(filePrefix, "capcaitiesinCWEFaceted.pdf", sep=""),plot=capacitiesinCWEFaceted, width=16.51, height=21.94, units="cm", scale=scaleFactor)

capacitiesinGBFaceted<-plotMoltenVariableFacettedByVariable(moltenCapacityinGB, "Capacity [GW]")
if(showPlots) capacitiesinGBFaceted


selectedMoltenCapacitiesinCWE<-moltenCapacityinCWE[moltenCapacityinCWE$variable%in%c("Nuclear","Lignite","CoalPSC","CoalPscCCS","IgccCCS","CCGT","CcgtCCS"),]

selectedcapacitiesinCWEFaceted<-plotBlackAndWhiteMoltenVariableFacettedByVariable(selectedMoltenCapacitiesinCWE, "Capacity [GW]")+
  theme_tufte(base_size=9)+
  theme(legend.position="bottom")+
  facet_wrap(~ variable, nrow=1, scales="free_y")+
  theme(legend.margin=unit(-1, "cm"), plot.margin=unit(x=c(1,2,1,1),units="mm")) 
if(showPlots) selectedcapacitiesinCWEFaceted
if(safePlots) ggsave(filename= paste(filePrefix, "selectedcapacitiesinCWEFaceted.pdf", sep=""),plot=selectedcapacitiesinCWEFaceted, width=16.51, height=5, units="cm", scale=scaleFactor)

selectedMoltenCapacitiesinGB<-moltenCapacityinGB[moltenCapacityinGB$variable%in%c("Nuclear","CoalPSC","CoalPscCCS","IGCC","IgccCCS","CCGT","CcgtCCS"),]
selectedcapacitiesinGBFaceted<-plotBlackAndWhiteMoltenVariableFacettedByVariable(selectedMoltenCapacitiesinGB, "Capacity [GW]")+
  theme_tufte(base_size=9)+
  #theme(legend.position="bottom")+
  facet_wrap(~ variable, nrow=1, scales="free_y")+
  theme(legend.margin=unit(-1, "cm"), plot.margin=unit(x=c(1,2,0,1),units="mm"), legend.position="none",axis.title.x=element_blank())
if(showPlots) selectedcapacitiesinGBFaceted
if(safePlots) ggsave(filename= paste(filePrefix, "selectedcapacitiesinGBFaceted.pdf", sep=""),plot=selectedcapacitiesinGBFaceted, width=16.51, height=4.3, units="cm", scale=scaleFactor)

selectedAllCapacities<-allMoltenCapacities[allMoltenCapacities$variable%in%c("Nuclear","CoalPSC","CoalPscCCS","IgccCCS","CCGT","CcgtCCS"),]
selectedAllCapacitiesFaceted<-plotBlackAndWhiteMoltenVariableFacettedByVariable(selectedAllCapacities, "Capacity [GW]")+
  theme_tufte(base_size=9)+
  theme(legend.position="bottom",
        panel.background = element_rect(fill = "white", colour = NA), 
        panel.border = element_rect(fill = NA, 
                                    colour = "grey50"), 
        panel.grid.major = element_line(colour = "grey90", size = 0.2), 
        panel.grid.minor = element_line(colour = "grey98", size = 0.5),
        strip.background = element_rect(fill = "grey80", colour = "grey50"), 
        strip.background = element_rect(fill = "grey80", colour = "grey50"))+                                                                                                                                                                                                                                                                             colour = "grey50"))+
  facet_grid(zone ~ variable, scales="free_y")
  #theme(legend.margin=unit(-1, "cm"), plot.margin=unit(x=c(1,2,1,1),units="mm"),axis.line=element_line(),axis.line.x=element_line(),axis.line.y=element_line())+
 
if(showPlots) selectedAllCapacitiesFaceted
if(safePlots) ggsave(filename= paste(filePrefix, "selectedAllCapacitiesFaceted_BW.pdf", sep=""),plot=selectedAllCapacitiesFaceted, width=16.51, height=10, units="cm", scale=scaleFactor)

selectedAllCapacities<-allMoltenCapacities[allMoltenCapacities$variable%in%c("Nuclear","CoalPSC","CoalPscCCS","IgccCCS","CCGT","CcgtCCS"),]
selectedAllCapacitiesFaceted<-plotMoltenVariableFacettedByVariable(selectedAllCapacities, "Capacity [GW]")+
  theme_publication(base_size=9)+
  theme(legend.position="bottom")+
  facet_grid(zone ~ variable, scales="free_y")+
  expand_limits(y=0)
if(showPlots) selectedAllCapacitiesFaceted
if(safePlots) ggsave(filename= paste(filePrefix, "selectedAllCapacitiesFaceted.pdf", sep=""),plot=selectedAllCapacitiesFaceted, width=16.51, height=10, units="cm", scale=scaleFactor)

selectedAllCapacities<-allMoltenCapacities[allMoltenCapacities$variable%in%c("Nuclear","CoalPSC","CCGT"),]
sink(file="~/Desktop/meanTechnologies.txt")
aggregate(selectedAllCapacities$value,by=list(selectedAllCapacities$modelRun,selectedAllCapacities$variable,selectedAllCapacities$tick,selectedAllCapacities$zone),mean)
sink()
##---- Demand development plots           ------------------


aggregateCash<-plotTimeSeriesWithConfidenceIntervalByFacettedGroup(bigDF, "ProducerCashSum", "Aggregated Producer Cash")
if(showPlots) aggregateCash

peakLoadA<-plotTimeSeriesWithConfidenceIntervalGroupedInOnePlot(bigDF, "PeakDemandPerZoneInMW_Country.A", "Peak A")
if(showPlots) peakLoadA


peakLoadB<-plotTimeSeriesWithConfidenceIntervalGroupedInOnePlot(bigDF, "PeakDemandPerZoneInMW_Country.B", "Peak B")
if(showPlots) peakLoadB


aggregateCashDelta<-plotTimeSeriesWithConfidenceIntervalByFacettedGroup(bigDF, "AggregateFinances_Profit", "Aggregate Delta Cash Flow")
if(showPlots) aggregateCashDelta
#if(safePlots) ggsave(filename= paste(filePrefix, "aggregateCashDelta.pdf", sep=""),plot=aggregateCashDelta, width=16.51, height=7.22, units="cm", scale=scaleFactor)

##---- Finances: Consumers --------------

gbExpenditure<-plotTimeSeriesWithConfidenceIntervalByFacettedGroup(bigDF, "SpecificConsumerCostinclSubs.GB", "Total Electricity Price GB [EUR/MWh]")+
  coord_cartesian(ylim(0,375))
if(showPlots) gbExpenditure
if(safePlots) ggsave(filename= paste(filePrefix, "gbExpenditure.pdf", sep=""),plot=gbExpenditure, width=7.83, height=7.83, units="cm", scale=scaleFactor)

cweExpenditure<-plotTimeSeriesWithConfidenceIntervalByFacettedGroup(bigDF, "SpecificConsumerCostinclSubs.CWE", "Total Electricity Price CWE [EUR/MWh]")+
  coord_cartesian(ylim(0,375))
if(showPlots) cweExpenditure
if(safePlots) ggsave(filename= paste(filePrefix, "cweExpenditure.pdf", sep=""),plot=cweExpenditure, width=7.83, height=7.83, units="cm", scale=scaleFactor)

gbSpotMarketExpenditure<-plotTimeSeriesWithConfidenceIntervalByFacettedGroup(bigDF, "SpecificConsumerCost.GB", "Total Electricity Price GB [EUR/MWh]")+
  coord_cartesian(ylim(0,375))
if(showPlots) gbSpotMarketExpenditure
if(safePlots) ggsave(filename= paste(filePrefix, "gbExpenditure.pdf", sep=""),plot=gbExpenditure, width=7.83, height=7.83, units="cm", scale=scaleFactor)

cweSpotMarketExpenditure<-plotTimeSeriesWithConfidenceIntervalByFacettedGroup(bigDF, "SpecificConsumerCost.CWE", "Total Electricity Price CWE [EUR/MWh]")+
  stat_summary(aes_string(fill="modelRun",y="SpecificConsumerCost.GB"), fun.data="median_hilow", conf.int=0, geom="line", colour="black", linetype=2)+
  coord_cartesian(ylim=c(-5,450))+
  theme_publication(base_size=9)


  
if(showPlots) cweSpotMarketExpenditure
if(safePlots) ggsave(filename= paste(filePrefix, "cweSpotMarketExpenditure.pdf", sep=""),plot=cweSpotMarketExpenditure, width=7.83, height=7.83, units="cm", scale=scaleFactor)

bigDF$SpecificConsumerCostinclSubs.GB

bigDF$DiscSpotMarketExpenditure.GB<-(bigDF$Consumer_Cost.GB)/(1+socialDiscountRate)^bigDF$tick
bigDF$DiscSpotMarketExpenditur.eCWE<-(bigDF$Consumer_Cost.CWE)/(1+socialDiscountRate)^bigDF$tick

bigDF$DiscSubsidyExpenditure.GB<-(bigDF$RenewableSubsidy.GB)/(1+socialDiscountRate)^bigDF$tick
bigDF$DiscSubsidyExpenditure.CWE<-(bigDF$RenewableSubsidy.CWE)/(1+socialDiscountRate)^bigDF$tick

bigDF$DiscTotalConsumerExpenditure.GB<-bigDF$ConsumerCostinclSubs.GB/(1+socialDiscountRate)^bigDF$tick
bigDF$DiscTotalConsumerExpenditure.CWE<-bigDF$ConsumerCostinclSubs.CWE/(1+socialDiscountRate)^bigDF$tick


TotalSubsidyExpenditure.CWE<-ddply(ddply(bigDF, .variables=c("runId", "modelRun"), .fun=functionOfVariablePerRunId, sum, "DiscSubsidyExpenditure.CWE"), .variables="modelRun", .fun=applyFunToColumnInDF ,fun=quantile, column="V1")
TotalSubsidyExpenditure.CWE


TotalExpenditure.CWE<-ddply(ddply(bigDF, .variables=c("runId", "modelRun"), .fun=functionOfVariablePerRunId, sum, "DiscTotalConsumerExpenditure.CWE"), .variables="modelRun", .fun=applyFunToColumnInDF ,fun=quantile, column="V1")
TotalExpenditure.GB<-ddply(ddply(bigDF, .variables=c("runId", "modelRun"), .fun=functionOfVariablePerRunId, sum,"DiscTotalConsumerExpenditure.GB"), .variables="modelRun", .fun=applyFunToColumnInDF ,fun=quantile, column="V1")
TotalExpenditure.CWE
TotalExpenditure.GB

print(xtable(TotalExpenditure.GB, booktabs=T, display=c("s",rep("f",6)), caption="Total Costs (incl. renewable subsidy) to consumers in GB"), include.rownames=F)
print(xtable(TotalExpenditure.CWE, booktabs=T, display=c("s",rep("e",6)), caption="Total Costs (incl. renewable subsidy) to consumers in CWE"), include.rownames=F)

SpecificTotalExpenditure.CWE<-ddply(ddply(bigDF, .variables=c("runId", "modelRun"), .fun=functionOfVariablePerRunIdSpecificPerkWhAndCountry, sum, "DiscTotalConsumerExpenditure.CWE","Total_DemandinMWh_Country.A"), .variables="modelRun", .fun=applyFunToColumnInDF ,fun=quantile, column="V1")
SpecificTotalExpenditure.GB<-ddply(ddply(bigDF, .variables=c("runId", "modelRun"), .fun=functionOfVariablePerRunIdSpecificPerkWhAndCountry, sum, "DiscTotalConsumerExpenditure.GB","Total_DemandinMWh_Country.B"), .variables="modelRun", .fun=applyFunToColumnInDF ,fun=quantile, column="V1")
SpecificTotalExpenditure.CWE
SpecificTotalExpenditure.GB
##---- Finances: Firms --------------
producerProfitPlotGB<-plotTimeSeriesWithConfidenceIntervalByFacettedGroup(bigDF, "ProducerProfit.GB", "Producer Profit in GB")
if(showPlots) producerProfitPlotGB
if(safePlots) ggsave(filename= paste(filePrefix, "producerProfitPlotGB.pdf", sep=""),plot=producerProfitPlotGB, width=16.51, height=10, units="cm", scale=scaleFactor)

producerProfitPlotCWE<-plotTimeSeriesWithConfidenceIntervalByFacettedGroup(bigDF, "ProducerProfit.CWE", "Producer Profit in CWE")
if(showPlots) producerProfitPlotCWE
if(safePlots) ggsave(filename= paste(filePrefix, "producerProfitPlotCWE.pdf", sep=""),plot=producerProfitPlotCWE, width=16.51, height=10, units="cm", scale=scaleFactor)

producerCashGB<-plotTimeSeriesWithConfidenceIntervalByFacettedGroup(bigDF, "ProducerCashInCWE", "ProducerCashInCWE")
if(showPlots) producerCashGB
producerCashCWE<-plotTimeSeriesWithConfidenceIntervalByFacettedGroup(bigDF, "ProducerCashInGB", "ProducerCashInGB")
if(showPlots) producerCashCWE

bigDF$DiscProducerProfit.GB<-bigDF$ProducerProfit.GB/(1+socialDiscountRate)^bigDF$tick
bigDF$DiscProducerProfit.CWE<-bigDF$ProducerProfit.CWE/(1+socialDiscountRate)^bigDF$tick

TotalProducerProfit.GB<-ddply(ddply(bigDF, .variables=c("runId", "modelRun"), .fun=functionOfVariablePerRunId, sum, "DiscProducerProfit.GB"), .variables="modelRun", .fun=applyFunToColumnInDF ,fun=quantile, column="V1")
TotalProducerProfit.CWE<-ddply(ddply(bigDF, .variables=c("runId", "modelRun"), .fun=functionOfVariablePerRunId, sum, "DiscProducerProfit.CWE"), .variables="modelRun", .fun=applyFunToColumnInDF ,fun=quantile, column="V1")

TotalProducerProfit.GB
TotalProducerProfit.CWE

##---- Finances: Governments ----------------
bigDF$Co2PolicyIncomeAll<-bigDF$Co2PolicyIncome_EU+bigDF$Co2PolicyIncome_Country.A+bigDF$Co2PolicyIncome_Country.B
bigDF$DiscCo2PolicyIncomeAll<-bigDF$Co2PolicyIncomeAll/(1+socialDiscountRate)^bigDF$tick
TotalCo2PolicyIncomeAll<-ddply(ddply(bigDF, .variables=c("runId", "modelRun"), .fun=functionOfVariablePerRunId, sum, "DiscCo2PolicyIncomeAll"), .variables="modelRun", .fun=applyFunToColumnInDF ,fun=quantile, column="V1")
TotalCo2PolicyIncomeAll

stateIncomePlot<-plotTimeSeriesWithConfidenceIntervalByFacettedGroup(bigDF, "Co2PolicyIncomeAll", "Co2PolicyIncomeAll")
if(showPlots) stateIncomePlot
if(safePlots) ggsave(filename= paste(filePrefix, "stateIncomePlot.pdf", sep=""),plot=stateIncomePlot, width=16.51, height=10, units="cm", scale=scaleFactor)


SpecificCo2PolicyIncomeAll<-ddply(ddply(bigDF, .variables=c("runId", "modelRun"), .fun=functionOfVariablePerRunIdSpecificPerkWh, sum, "DiscCo2PolicyIncomeAll"), .variables="modelRun", .fun=applyFunToColumnInDF ,fun=quantile, column="V1")
SpecificCo2PolicyIncomeAll

##--- Finances: Overall-Costs  ---------
TotalOverallCosts<-ddply(ddply(bigDF, .variables=c("runId", "modelRun"), .fun=functionOfVariablePerRunId, sum, "OverallCost"), .variables="modelRun", .fun=applyFunToColumnInDF ,fun=quantile, column="V1")
TotalOverallCosts

TotalSpecificOverallCosts<-ddply(ddply(bigDF, .variables=c("runId", "modelRun"), .fun=functionOfVariablePerRunIdSpecificPerkWh, sum, "OverallCost"), .variables="modelRun", .fun=applyFunToColumnInDF ,fun=quantile, column="V1")
TotalSpecificOverallCosts

bigDF$DiscOverallCosts<-bigDF$OverallCost/(1+socialDiscountRate)^bigDF$tick
DiscTotalOverallCosts<-ddply(ddply(bigDF, .variables=c("runId", "modelRun"), .fun=functionOfVariablePerRunId, sum, "DiscOverallCosts"), .variables="modelRun", .fun=applyFunToColumnInDF ,fun=mean, column="V1")
DiscTotalOverallCosts

TotalDiscSpecificOverallCosts<-ddply(ddply(bigDF, .variables=c("runId", "modelRun"), .fun=functionOfVariablePerRunIdSpecificPerkWh, sum, "DiscOverallCosts"), .variables="modelRun", .fun=applyFunToColumnInDF ,fun=quantile, column="V1")
TotalDiscSpecificOverallCosts

welfareBoxplotData<-ddply(bigDF, .variables=c("runId", "modelRun"), .fun=functionOfVariablePerRunId, sum, "OverallCost")
welfareBoxplot<-qplot(x=modelRun, y=V1, data=welfareBoxplotData, geom="hist")+
  xlab("Scenario")+
  ylab("Total Welfacre")+
  theme_grey(base_size=13)
if(showPlots) welfareBoxplot
if(safePlots) ggsave(filename= paste(filePrefix, "welfareBoxplot.pdf", sep=""),plot=welfareBoxplot, width=30, height=20, units="cm", scale=scaleFactor)

###
overallCostsPerRunId<-ddply(bigDF, .variables=c("runId", "modelRun"), .fun=functionOfVariablePerRunId, sum, "DiscOverallCosts")
overallCostsPerRunId$runId<-renamer(overallCostsPerRunId$runId,"X-PureETS-FRES-DeccCentral-","")
overallCostsPerRunId$runId<-renamer(overallCostsPerRunId$runId,"X-MinGB-FRES-DeccCentral-","")
overallCostsPerRunId$runId<-renamer(overallCostsPerRunId$runId,"X-MinCWE-FRES-DeccCentral-","")
overallCostsPerRunId$runId<-renamer(overallCostsPerRunId$runId,"X-BothMinBothMax-FRES-DeccCentral-","")
overallCostsPerRunId$runId<-renamer(overallCostsPerRunId$runId,"X-BothMin-FRES-DeccCentral-","")

for(i in paste(seq(1,120),"-1", sep="")){
  overallCostsPerRunId[overallCostsPerRunId$runId==i,]$V1<-overallCostsPerRunId[overallCostsPerRunId$runId==i,]$V1/overallCostsPerRunId[(overallCostsPerRunId$runId==i & overallCostsPerRunId$modelRun=="PureETS"),]$V1
}

overallCostsPerRunIdBoxPlot<-qplot(x=modelRun, y=V1, data=overallCostsPerRunId, geom="boxplot")+
  xlab("Scenario")+
  ylab("Total Welfacre")+
  theme_grey(base_size=13)
if(showPlots) overallCostsPerRunIdBoxPlot

ddply(overallCostsPerRunId, .variables="modelRun", .fun=applyFunToColumnInDF ,fun=mean, column="V1")$V1-1


anova(aov(V1~modelRun,data=overallCostsPerRunId)) #ANOVA significantly different
pairwise.t.test(overallCostsPerRunId$V1, overallCostsPerRunId$modelRun,, p.adj = "bonf")
TukeyHSD(aov(V1~modelRun,data=overallCostsPerRunId))


kruskal.test(V1~modelRun,data=overallCostsPerRunId) #Kruskal significantly different
pairwise.wilcox.test(overallCostsPerRunId$V1, overallCostsPerRunId$modelRun)

#--------------Volatility------------------------
bigDF$co2PriceCWE<-pmax(bigDF$NationalMinCO2PriceinEURpTon_Country.A, bigDF$CO2Auction)
bigDF$co2PriceGB<-pmax(bigDF$NationalMinCO2PriceinEURpTon_Country.B, bigDF$CO2Auction)

co2PriceVolatilityPerRunId<-function(df, co2PriceName){
sd(diff(log(df[, co2PriceName])))/sqrt(length(diff(log(df[,co2PriceName]))))
}

sdPerRunId<-function(df,co2PriceName) sd(df[,co2PriceName])
medianPerRunId<-function(df,co2PriceName) median(df[,co2PriceName])
meanPerRunId<-function(df,co2PriceName) mean(df[,co2PriceName])

sdCo2PricesCWE<-ddply(ddply(bigDF, .variables=c("runId", "modelRun"), .fun=sdPerRunId, "co2PriceCWE"), .variables="modelRun", .fun=applyFunToColumnInDF ,fun=quantile, column="V1")
sdCo2PricesGB<-ddply(ddply(bigDF, .variables=c("runId", "modelRun"), .fun=sdPerRunId, "co2PriceGB"), .variables="modelRun", .fun=applyFunToColumnInDF ,fun=quantile, column="V1")
sdCo2PricesCWE
sdCo2PricesGB

meanCo2PricesCWE<-ddply(ddply(bigDF, .variables=c("runId", "modelRun"), .fun=meanPerRunId, "co2PriceCWE"), .variables="modelRun", .fun=applyFunToColumnInDF ,fun=quantile, column="V1")
meanCo2PricesGB<-ddply(ddply(bigDF, .variables=c("runId", "modelRun"), .fun=meanPerRunId, "co2PriceGB"), .variables="modelRun", .fun=applyFunToColumnInDF ,fun=quantile, column="V1")
meanCo2PricesCWE
meanCo2PricesGB

moltenCO2Prices<-melt(data=bigDF, id.vars=c("runId", "modelRun", "tick"), measure.vars=c("co2PriceCWE", "co2PriceGB"))
#moltenCO2Prices$modelRun<-factor(moltenCO2Prices$modelRun, levels=c("No Price Floor","Price Floor in A","Price Floor in B","Common Price Floor"))
moltenCO2Prices$variable<-renamer(moltenCO2Prices$variable, "co2PriceCWE", "Country CWE")
moltenCO2Prices$variable<-renamer(moltenCO2Prices$variable, "co2PriceGB", "Country GB")
moltenCO2Prices$variable<-factor(moltenCO2Prices$variable, levels=c("Country CWE","Country GB"))
sdOfCO2Prices<-ddply(moltenCO2Prices, .variables=c("runId", "modelRun", "variable"), .fun=sdPerRunId, "value")
medianOfCO2Prices<-ddply(moltenCO2Prices, .variables=c("runId", "modelRun", "variable"), .fun=medianPerRunId, "value")
meanOfCO2Prices<-ddply(moltenCO2Prices, .variables=c("runId", "modelRun", "variable"), .fun=meanPerRunId, "value")

boxPlotSDCO2Prices<-ggplot(aes(x=modelRun, y=V1), data=sdOfCO2Prices)+facet_grid(variable ~ .)+
  xlab("Scenario")+
  ylab("Standard deviation of CO2 Prices")+
  theme_publication(base_size=9)+
  geom_boxplot(colour="black")+
  theme(axis.text.x=element_text(angle=-45, hjust = 0),panel.grid.major.x=element_blank())
if(showPlots) boxPlotSDCO2Prices
if(safePlots) ggsave(filename= paste(filePrefix, "boxPlotSDCO2Prices.pdf", sep=""),plot=boxPlotSDCO2Prices, width=7.83, height=7.83, units="cm", scale=scaleFactor)


cumSdOfCO2Prices<-ggplot(sdOfCO2Prices,aes(x=V1))+
  stat_ecdf()+
  facet_grid(variable ~ modelRun)+
  ylab("SD of CO2 Prices")
if(showPlots) cumSdOfCO2Prices

boxPlotMeanCO2Prices<-ggplot(aes(x=modelRun, y=V1), data=meanOfCO2Prices)+facet_grid(variable ~ .)+
  xlab("Scenario")+
  ylab("Mean of CO2 Prices [EUR/ton]")+
  theme_tufte(base_size=9, )+
  geom_boxplot(colour="black")+
  theme(axis.text.x=element_text(angle=-45, hjust = 0))
if(showPlots) boxPlotMeanCO2Prices
if(safePlots) ggsave(filename= paste(filePrefix, "boxPlotMeanCO2Prices.pdf", sep=""),plot=boxPlotMeanCO2Prices, width=7.83, height=7.83, units="cm", scale=scaleFactor)



sdCWEco2<-ddply(bigDF, .variables=c("stochasticId", "modelRun"), .fun=sdPerRunId, "co2PriceCWE")


for(i in paste(seq(1,120), sep="")){
  sdCWEco2[sdCWEco2$stochasticId==i,]$V1<-sdCWEco2[sdCWEco2$stochasticId==i,]$V1/sdCWEco2[(sdCWEco2$stochasticId==i & sdCWEco2$modelRun=="BMinMax"),]$V1
}

sdCWEco2BoxPlot<-qplot(x=modelRun, y=V1, data=sdCWEco2, geom="boxplot")+
  xlab("Scenario")+
  ylab("Total Welfacre")+
  theme_grey(base_size=13)
if(showPlots) sdCWEco2BoxPlot
