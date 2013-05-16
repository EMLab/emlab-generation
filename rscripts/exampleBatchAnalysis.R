setwd("~/emlab-generation/rscripts/")
source("rConfig.R")
source("batchRunAnalysis.R")
setwd(analysisFolder)

bigDF <- getDataFrameForModelRunsInFolder(resultFolder)

bigDF <- addSupplyRatios(bigDF)
bigDF <- addSumOfVariablesByPrefixToDF(bigDF, "ProducerCash")


co2PricePlot<-plotTimeSeriesWithConfidenceIntervalByFacettedGroup(bigDF, "CO2Auction_PriceInEUR", "CO2 Price")
co2PricePlot

co2SpaghettiPrice<-plotSpaghettiTimeSeries(df=bigDF,variable="CO2Auction_PriceInEUR",ylabel="CO2 Price [EUR/ton]")+
  geom_line(aes(y=NationalMinCO2PriceinEURpTon_Country.A, colour="black"))+
  geom_line(aes(y=NationalMinCO2PriceinEURpTon_Country.B, colour="black"))
co2SpaghettiPrice
ggsave(filename="co2PriceSpaghetti.pdf",plot=co2SpaghettiPrice, width=16.51, height=7.22, units="cm")

co2EmissionsSpaghetti<-plotSpaghettiTimeSeries(df=bigDF,variable="CO2Emissions_inTonpA",ylabel="CO2 Emissions [t/a]")
co2EmissionsSpaghetti<-co2EmissionsSpaghetti+geom_line(aes(y=CO2CapinTonpA_CO2_cap),linetype = 2, colour="red", size=1)
co2EmissionsSpaghetti
ggsave(filename="co2EmissionsSpaghetti.pdf",plot=co2EmissionsSpaghetti, width=15.66, height=10.44, units="cm")

co2EmissionPlot<-plotTimeSeriesWithConfidenceIntervalByFacettedGroup(bigDF, "CO2Emissions_inTonpA", "CO2 Emissions [t/a]")
co2EmissionPlot<-co2EmissionPlot+geom_line(aes(y=CO2CapinTonpA_CO2_cap),linetype = 2, colour="red", size=1)
co2EmissionPlot

avgPricePlotinA<-plotTimeSeriesWithConfidenceIntervalByFacettedGroup(bigDF, "Avg_El_PricesinEURpMWh_Country.A", "Avg. Electricity Price in Country A [EUR/MW]")
avgPricePlotinA
ggsave(filename="avgPricePlotinA.pdf",plot=avgPricePlotinA, width=15.66, height=10.44, units="cm")

avgPricePlotinB<-plotTimeSeriesWithConfidenceIntervalByFacettedGroup(bigDF, "Avg_El_PricesinEURpMWh_Country.B", "Avg. Electricity Price in Country B [EUR/MW]")
avgPricePlotinB
ggsave(filename="avgPricePlotinB.pdf",plot=avgPricePlotinB, width=15.66, height=10.44, units="cm")

aggregateCash<-plotTimeSeriesWithConfidenceIntervalByFacettedGroup(bigDF, "ProducerCashSum", "Aggregated Producer Cash")
aggregateCash

peakLoadA<-plotTimeSeriesWithConfidenceIntervalGroupedInOnePlot(bigDF, "PeakDemandPerZoneInMW_Country.A", "Peak A")
peakLoadA


peakLoadB<-plotTimeSeriesWithConfidenceIntervalGroupedInOnePlot(bigDF, "PeakDemandPerZoneInMW_Country.B", "Peak B")
peakLoadB

coalPrice<-plotTimeSeriesWithConfidenceIntervalGroupedInOnePlot(bigDF, "FuelPricesPerGJ_Coal", "Coal")  
coalPrice

gasPrice<-plotTimeSeriesWithConfidenceIntervalGroupedInOnePlot(bigDF, "FuelPricesPerGJ_Natural.Gas", "Gas")  
gasPrice

biomassPrice<-plotTimeSeriesWithConfidenceIntervalGroupedInOnePlot(bigDF, "FuelPricesPerGJ_Biomass", "Biomass")
biomassPrice


aggregateCashDelta<-plotTimeSeriesWithConfidenceIntervalByFacettedGroup(bigDF, "AggregateFinances_Profit", "Aggregate Delta Cash Flow")
aggregateCashDelta

supplyRatioA<-plotTimeSeriesWithConfidenceIntervalByFacettedGroup(bigDF, "SupplyRatio_Country.A", "Peak Capacity Supply Ratio A")
supplyRatioA

supplyRatioB<-plotTimeSeriesWithConfidenceIntervalByFacettedGroup(bigDF, "SupplyRatio_Country.B", "Peak Capacity Supply Ratio B")
supplyRatioB

moltenCapacities<-meltTechnologyVariable(bigDF,"CapacityinMW_")
moltenCapacities$value<-moltenCapacities$value/1000
stackedCapacities<-plotStackedTechnologyDiagram(moltenVariable=moltenCapacities,ylabel="Capacity [GW]")
stackedCapacities
ggsave(filename="stackedCapacityDiagram.pdf",plot=stackedCapacities, width= 15.66, height= 14, units="cm")

generationCapacitiesFacetted<-plotMoltenVariableFacettedByVariable(moltenCapacities, "Capacity [GW]")
generationCapacitiesFacetted
ggsave(filename="generationCapacitiesFacetted.pdf",plot=generationCapacitiesFacetted, width=30, height=16.51, units="cm")

moltenGeneration<-meltTechnologyVariable(bigDF, "GenerationinMWh_")
moltenGeneration$value<-moltenGeneration$value/1000000 #To TWh
stackedGeneration<-plotStackedTechnologyDiagram(moltenVariable=moltenGeneration,ylabel="Generation [TWh]")
stackedGeneration
ggsave(filename="stackedGenerationDiagram.pdf",plot=stackedGeneration, width=16.51, height=8.255, units="cm")

generationFacetted<-plotMoltenVariableFacettedByVariable(moltenGeneration, "Generation [TWh]")
generationFacetted
ggsave(filename="generationFacetted.pdf",plot=generationFacetted, width=16.51, height=21.94, units="cm")

moltenCapacityinA<-meltTechnologyVariable(bigDF,"CapacityinMWinA_")
moltenCapacityinA$value<-moltenCapacityinA$value/1000
stackedCapacitiesinAMedian<-plotStackedTechnologyDiagram(moltenVariable=moltenCapacityinA,ylabel="Capacity [GW]", summaryFunction=median)
stackedCapacitiesinAMedian
stackedCapacitiesinAMean<-plotStackedTechnologyDiagram(moltenVariable=moltenCapacityinA,ylabel="Capacity [GW]", summaryFunction=mean)
stackedCapacitiesinAMean
ggsave(filename="stackedCapacityinADiagram.pdf",plot=stackedCapacitiesinA, width= 15.66, height= 14, units="cm")


moltenCapacityinB<-meltTechnologyVariable(bigDF,"CapacityinMWinB_")
moltenCapacityinB$value<-moltenCapacityinB$value/1000
stackedCapacitiesinB<-plotStackedTechnologyDiagram(moltenVariable=moltenCapacityinB,ylabel="Capacity [GW]")
stackedCapacitiesinB
ggsave(filename="stackedCapacityinBDiagram.pdf",plot=stackedCapacitiesinA, width= 15.66, height= 14, units="cm")


capcitiesinBFaceted<-plotMoltenVariableFacettedByVariable(moltenCapacityinB, "Capacity [GW]")
capcitiesinBFaceted
ggsave(filename="capcitiesinBFaceted",plot=capcitiesinBFaceted, width=16.51, height=21.94, units="cm")

#--------------Volatility------------------------
bigDF$co2PriceA<-pmax(bigDF$NationalMinCO2PriceinEURpTon_Country.A, bigDF$CO2Auction_PriceInEUR)
bigDF$co2PriceB<-pmax(bigDF$NationalMinCO2PriceinEURpTon_Country.B, bigDF$CO2Auction_PriceInEUR)

co2PriceVolatilityPerRunId<-function(df, co2PriceName){
  sd(diff(log(df[, co2PriceName])))/sqrt(length(diff(log(df[,co2PriceName]))))
}

sdPerRunId<-function(df,co2PriceName) sd(df[,co2PriceName])
a<-ddply(ddply(bigDF, .variables=c("runId", "modelRun"), .fun=sdPerRunId, "co2PriceA"), .variables="modelRun", .fun=applyFunToColumnInDF ,fun=quantile, column="V1")

b<-ddply(ddply(bigDF, .variables=c("runId", "modelRun"), .fun=sdPerRunId, "co2PriceB"), .variables="modelRun", .fun=applyFunToColumnInDF ,fun=quantile, column="V1")

moltenCO2Prices<-melt(data=bigDF, id.vars=c("runId", "modelRun", "tick"), measure.vars=c("co2PriceA", "co2PriceB"))
moltenCO2Prices$modelRun<-factor(moltenCO2Prices$modelRun, levels=c("No Price Floor","Price Floor in A","Price Floor in B","Common Price Floor"))
moltenCO2Prices$variable<-renamer(moltenCO2Prices$variable, "co2PriceA", "Country A")
moltenCO2Prices$variable<-renamer(moltenCO2Prices$variable, "co2PriceB", "Country B")
moltenCO2Prices$variable<-factor(moltenCO2Prices$variable, levels=c("Country A","Country B"))
sdOfCO2Prices<-ddply(moltenCO2Prices, .variables=c("runId", "modelRun", "variable"), .fun=sdPerRunId, "value")
boxPlotSDCO2Prices<-qplot(x=modelRun, y=V1, data=sdOfCO2Prices, geom="boxplot")+facet_grid(variable ~ .)+
  xlab("Scenario")+
  ylab("Standard deviation of CO2 Prices")+
  theme_grey(base_size=13)
boxPlotSDCO2Prices
ggsave(filename="boxPlotSDCO2Prices.pdf",plot=boxPlotSDCO2Prices, width=15.66, height=12, units="cm")

ddply(moltenCO2Prices, .variables=c("modelRun", "variable"), .fun=function(df, columnname) quantile(df[,columnname]), "value")

#------------------Median Capacity Differences between no price floor and B -----------------------

applyFunToColumnInDF<- function(df=df, column=column, fun=fun) fun(df[,column])
medianCapacitiesInB<-ddply(.data=moltenCapacityinB, .variables=c("tick","modelRun", "variable"), .fun=applyFunToColumnInDF, column="value", fun=median)
getDifferenceBetweenFloorBandNoPriceFloor<-function(df) df[df$modelRun=="debugging1",]$V1-df[df$modelRun=="debugging2",]$V1
medianCapacityinBDifferenceBandNoPriceFloor<-ddply(.data=medianCapacitiesInB, .variables=c("variable", "tick"), .fun=getDifferenceBetweenFloorBandNoPriceFloor)
medianCapacitiesinA<-ddply(.data=moltenCapacityinA, .variables=c("tick","modelRun", "variable"), .fun=applyFunToColumnInDF, column="value", fun=median)
medianCapacityinADifferenceBandNoPriceFloor<-ddply(.data=medianCapacitiesinA, .variables=c("variable", "tick"), .fun=getDifferenceBetweenFloorBandNoPriceFloor)
names(medianCapacityinADifferenceBandNoPriceFloor)[3]<-"A"
names(medianCapacityinBDifferenceBandNoPriceFloor)[3]<-"B"
medianCapacitiesFloorB<-merge(medianCapacityinADifferenceBandNoPriceFloor,medianCapacityinBDifferenceBandNoPriceFloor)
medianCapacitiesFloorB$Both<-medianCapacitiesFloorB$A+medianCapacitiesFloorB$B
medianCapacitiesFloorB<-melt(medianCapacitiesFloorB, id.vars=c("variable", "tick"), measure.vars=c("A", "B", "Both"), variable_name="Country")
names(medianCapacitiesFloorB)[1]<-"Technology"

medianCapacitiesDifferenceFloorBandNoPriceFloorPlot<-ggplot(medianCapacitiesFloorB[medianCapacitiesFloorB$Technology!=c("Photovoltaic","IgccCCS", "CoalPulverizedCCS"),], aes(x=tick, y=value, group=Country, colour=Country, linetype=Country))+
  geom_line()+
  facet_wrap( ~ Technology  )+
  xlab("Time [a]")+
  ylab("Capacity Difference [MW]")+
  scale_color_discrete()+
  theme_grey(base_size=8)+
  opts(legend.position="bottom", strip.text.y = theme_text(size = 6, angle = 270))
medianCapacitiesDifferenceFloorBandNoPriceFloorPlot
#ggsave(filename="medianCapacitiesDifferenceFloorBandNoPriceFloorPlot.pdf",plot=medianCapacitiesDifferenceFloorBandNoPriceFloorPlot, width=12.3825, height=43.88, units="cm")
ggsave(filename="medianCapacitiesDifferenceFloorBandNoPriceFloorPlot.pdf",plot=medianCapacitiesDifferenceFloorBandNoPriceFloorPlot, width=28, height=12, units="cm")

co2PriceVertical<-ggplot(subset(bigDF, modelRun==c("debugging1", "debugging2")), aes_string(x="tick", y="CO2Auction_PriceInEUR", group="modelRun", linetype="modelRun", colour="modelRun"))+ #colour=modelRun, fill=modelRun,
    stat_summary(aes_string(fill="modelRun"), fun.data="median_hilow", conf.int=.5, geom="line") +
    #stat_summary(fun.data="median_hilow", conf.int=.95, geom="line")+
    #facet_grid(. ~ modelRun)+
    #facet_grid(modelRun ~ .)+
    scale_color_discrete(h.start=90)+
    xlab(NULL)+
    ylab("CO2 Price [EUR/ton]")+
    theme_grey(base_size=8)+
    facet_grid(GenerationinMWh_IgccCCS ~ .)+ #just to add grid label
    opts(legend.position="bottom", strip.text.y = theme_text(size = 6, angle = 270))
  co2PriceVertical
ggsave(filename="co2PriceVertical.pdf",plot=co2PriceVertical, width=11.46, height=7.0, units="cm")

#Adjusted sizes 7.83
ggsave(filename="medianDifferences.pdf",plot=(medianCapacitiesDifferenceFloorBandNoPriceFloorPlot), width=7.83, height=22, units="cm")
ggsave(filename="co2PriceVertical.pdf",plot=co2PriceVertical, width=7.83, height=4.782722513, units="cm")

#------------------Median Capacity Differences between no price floor and A -----------------------

applyFunToColumnInDF<- function(df=df, column=column, fun=fun) fun(df[,column])
medianCapacitiesInA<-ddply(.data=moltenCapacityinA, .variables=c("tick","modelRun", "variable"), .fun=applyFunToColumnInDF, column="value", fun=median)
getDifferenceBetweenFloorAandNoPriceFloor<-function(df) df[df$modelRun=="Price Floor in A",]$V1-df[df$modelRun=="No Price Floor",]$V1
medianCapacityinBDifferenceAandNoPriceFloor<-ddply(.data=medianCapacitiesInB, .variables=c("variable", "tick"), .fun=getDifferenceBetweenFloorAandNoPriceFloor)
medianCapacitiesinA<-ddply(.data=moltenCapacityinA, .variables=c("tick","modelRun", "variable"), .fun=applyFunToColumnInDF, column="value", fun=median)
medianCapacityinADifferenceAandNoPriceFloor<-ddply(.data=medianCapacitiesinA, .variables=c("variable", "tick"), .fun=getDifferenceBetweenFloorAandNoPriceFloor)
names(medianCapacityinADifferenceAandNoPriceFloor)[3]<-"A"
names(medianCapacityinBDifferenceAandNoPriceFloor)[3]<-"B"
medianCapacitiesFloorA<-merge(medianCapacityinADifferenceAandNoPriceFloor,medianCapacityinBDifferenceAandNoPriceFloor)
medianCapacitiesFloorA$Both<-medianCapacitiesFloorA$A+medianCapacitiesFloorA$B
medianCapacitiesFloorA<-melt(medianCapacitiesFloorA, id.vars=c("variable", "tick"), measure.vars=c("A", "B", "Both"), variable_name="Country")
names(medianCapacitiesFloorA)[1]<-"Technology"

medianCapacitiesDifferenceFloorAandNoPriceFloorPlot<-ggplot(medianCapacitiesFloorA[medianCapacitiesFloorA$Technology!=c("Photovoltaic","IgccCCS", "CoalPulverizedCCS"),], aes(x=tick, y=value, group=Country, colour=Country, linetype=Country))+
  geom_line()+
  facet_grid(Technology ~ .)+
  xlab("Time [a]")+
  ylab("Capacity Difference [MW]")+
  scale_color_discrete()+
  theme_grey(base_size=8)+
  opts(legend.position="bottom", strip.text.y = theme_text(size = 6, angle = 270))
#medianCapacitiesDifferenceFloorAandNoPriceFloorPlot
ggsave(filename="medianCapacitiesDifferenceFloorAandNoPriceFloorPlot.pdf",plot=medianCapacitiesDifferenceFloorBandNoPriceFloorPlot, width=12.3825, height=43.88, units="cm")

co2PriceVerticalDifferenceA<-ggplot(subset(bigDF, modelRun==c("No Price Floor", "Price Floor in A")), aes_string(x="tick", y="CO2Auction", group="modelRun", linetype="modelRun", colour="modelRun"))+ #colour=modelRun, fill=modelRun,
  stat_summary(aes_string(fill="modelRun"), fun.data="median_hilow", conf.int=.5, geom="line") +
  #stat_summary(fun.data="median_hilow", conf.int=.95, geom="line")+
  #facet_grid(. ~ modelRun)+
  #facet_grid(modelRun ~ .)+
  scale_color_discrete(h.start=90)+
  xlab(NULL)+
  ylab("CO2 Price [EUR/ton]")+
  theme_grey(base_size=8)+
  facet_grid(GenerationinMWh_IgccCCS ~ .)+ #just to add grid label
  opts(legend.position="bottom", strip.text.y = theme_text(size = 6, angle = 270))
#co2PriceVerticalDifferenceA
ggsave(filename="co2PriceVerticalDifferenceA.pdf",plot=co2PriceVerticalDifferenceA, width=11.46, height=7.0, units="cm")

#Adjusted sizes 7.83
ggsave(filename="medianDifferences.pdf",plot=(medianCapacitiesDifferenceFloorBandNoPriceFloorPlot), width=7.83, height=22, units="cm")
ggsave(filename="co2PriceVertical.pdf",plot=co2PriceVertical, width=7.83, height=4.782722513, units="cm")

#----------Median Price differences---------------------
moltenElectricityPrices<-melt(bigDF, id.vars=c("tick", "modelRun"), measure.vars=c("Avg_El_PricesinEURpMWh_Country.A", "Avg_El_PricesinEURpMWh_Country.B"))
medianElectricityPrices<-ddply(.data=moltenElectricityPrices, .variables=c("tick","modelRun", "variable"), .fun=applyFunToColumnInDF, column="value", fun=mean)
medianElectricityPricePlot<-ggplot(data=medianElectricityPrices, aes(x=tick, y=V1, group=modelRun, linetype=modelRun, colour=modelRun))+
  geom_line()+
  facet_grid(variable ~ .)
medianElectricityPricePlot

electricityPricesPlot<-ggplot(moltenElectricityPrices, aes(x=tick, y=value, group=modelRun, colour=modelRun, linetype=modelRun))+ #colour=modelRun, fill=modelRun,
  stat_summary(aes_string(fill="modelRun"), fun.data="median_hilow", conf.int=.5, geom="errorbar") +
  stat_summary(aes_string(fill="modelRun"), fun.data="median_hilow", conf.int=.5, geom="line") +
  #stat_summary(aes(fill=modelRun),fun.data="median_hilow", conf.int=.95, geom="smooth")+
  #facet_grid(. ~ modelRun)+
  
  facet_wrap(~ variable)+
  opts(legend.position="bottom")+
  xlab("Time [a]")+
  ylab("Average Electricity Price [EUR/MWh]")+
  scale_color_brewer(type="qual", palette=2)+
  scale_fill_brewer(type="qual", palette=2)
#electricityPricesPlot

moltenElectricityPriceswithRunId<-melt(bigDF, id.vars=c("tick", "modelRun", "runId"), measure.vars=c("Avg_El_PricesinEURpMWh_Country.A", "Avg_El_PricesinEURpMWh_Country.B"))
electricityPriceSpaghettiPlot<-ggplot(moltenElectricityPriceswithRunId, aes(x=tick, y=value))+
  #geom_line(aes(group=runId, linestyle=runId), alpha=I(0.2))+
  stat_summary(fun.data="median_hilow", conf.int=0.95, geom="smooth", alpha=I(0.5))+
  stat_summary(aes_string(fill="modelRun"), fun.data="median_hilow", conf.int=.5, geom="smooth") +
  facet_grid(modelRun ~ variable)+
  opts(legend.position="none")
electricityPriceSpaghettiPlot

#-----------------Median generation differences------------
