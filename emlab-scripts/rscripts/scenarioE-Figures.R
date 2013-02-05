#install.packages(c('ggplot2', 'reshape', 'plyr'))
library(ggplot2)
library(reshape)
library(plyr)
library(grid)
#setwd("/Users/Joern/Documents/d13n/rscripts")
setwd("/home/joern/emlab/rscripts")
scriptFolder="/home/joern/emlab/rscripts"
outPutFolder="/home/joern/Desktop/outputEmlab/ministry2/"
#scriptFolder="/Users/Joern/d13n/rscripts"
#outPutFolder="/Users/joern/Desktop/outputD13n"
#modelRun="d13nM2"

source("d13nHPCQueryReader.R")

#dfNoMin<-getDataFrameForModelRun(outPutFolder, "d13nNoMinNoInt2", "No Interconnector - No Price Floor")
#dfDEMin<-getDataFrameForModelRun(outPutFolder, "d13nM4", "No Interconnector - Price Floor in B")
#dfNLMin<-getDataFrameForModelRun(outPutFolder, "d13nMinNLNoInt5", "No Interconnector - Price Floor in A")
noMinCO2<-getDataFrameForModelRun(outPutFolder, "noMinCO2", "noMinCO2",c("PowerPlantDispatchPlans"))
MinCO2<-getDataFrameForModelRun(outPutFolder, "MinCO2", "MinCO2",c("PowerPlantDispatchPlans"))
highMinCO2<-getDataFrameForModelRun(outPutFolder, "highMinCO2", "highMinCO2",c("PowerPlantDispatchPlans"))
highRes<-getDataFrameForModelRun(outPutFolder, "highRes", "highRes",c("PowerPlantDispatchPlans"))
lowRes<-getDataFrameForModelRun(outPutFolder, "lowRes", "lowRes",c("PowerPlantDispatchPlans"))
#noRes<-getDataFrameForModelRun(outPutFolder, "noRes", "noRes",c("PowerPlantDispatchPlans"))
nlMinCO2<-getDataFrameForModelRun(outPutFolder, "nlMinCO2", "nlMinCO2",c("PowerPlantDispatchPlans"))
#dfDEMinWInt<-getDataFrameForModelRun(outPutFolder, "d13nRe5DEMinWInt1", "Price Floor in B")
#dfNoMinWInt<-getDataFrameForModelRun(outPutFolder, "d13nRe5NoMinWInt1", "No Price Floor")
#dfAllMinWInt<-getDataFrameForModelRun(outPutFolder, "d13nRe5AllMinWInt1", "Common Price Floor")
#dfAllTaxNoCAT<-getDataFrameForModelRun(outPutFolder, "d13nTaxNoCAT1", "Common CO2 Tax")

#Take only first hundred
no=68
noMinCO2<-selectFirstXRunIdsFromDataFrame(noMinCO2, no)
MinCO2<-selectFirstXRunIdsFromDataFrame(MinCO2, no)
MinCO2$OldCO2Auction = MinCO2$CO2Auction
MinCO2$CO2Auction = max(MinCO2$CO2Auction, MinCO2$NationalMinCO2PriceinEURpTon_CO2_price_floor_in__Country_A_)
highMinCO2<-selectFirstXRunIdsFromDataFrame(highMinCO2, no)
highRes<-selectFirstXRunIdsFromDataFrame(highRes, no)
lowRes<-selectFirstXRunIdsFromDataFrame(lowRes, no)
#noRes<-selectFirstXRunIdsFromDataFrame(noRes, no)
nlMinCO2<-selectFirstXRunIdsFromDataFrame(nlMinCO2, no)
#test<-selectFirstXRunIdsFromDataFrameAndCheckForLength(dfNoMinWInt, 75, 41)
#rm(dfNoMinWInt)
#dfDEMinWInt100<-selectFirstXRunIdsFromDataFrameAndCheckForLength(dfDEMinWInt, 75, 41)
#rm(dfDEMinWInt)
#dfNLMinWInt100<-selectFirstXRunIdsFromDataFrameAndCheckForLength(dfNLMinWInt, 75, 41)
#rm(dfNLMinWInt)
#dfAllMinWInt100<-selectFirstXRunIdsFromDataFrameAndCheckForLength(dfAllMinWInt, 75, 41)
#rm(dfAllMinWInt)
#dfAllTaxNoCAT100<-selectFirstXRunIdsFromDataFrameAndCheckForLength(dfAllTaxNoCAT, 75, 41)
#dfEUMinWInt100<-selectFirstXRunIdsFromDataFrame(dfEUMinWInt, 100)
#rm(dfEUMinWInt)

#names(bigDF)

#setwd('/Users/Joern/Desktop/graphs/')
setwd('/home/joern/Desktop/ministryFigures2/')

#bigDF<-sqldf("select * from dfNoMin100 union select * from dfDEMin100 union select * from dfNLMin100 union select * from dfNLMinWInt100 union select * from dfDEMinWInt100 union select * from dfNoMinWInt100")  
bigDF<-sqldf("select * from noMinCO2 union select * from MinCO2 union select * from highMinCO2 union select * from highRes union select * from lowRes union select * from nlMinCO2")

#Create the right order for the runs
#bigDF <- within(bigDF, run <- factor(run, levels = c("No Price Floor","Price Floor in A","Price Floor in B","Common Price Floor", "Common CO2 Tax")))

bigDF$SupplyRatioB=bigDF$TotalOperationalCapacityPerZoneInMW_Country_B_Capacity/bigDF$PeakDemandPerZoneInMW_Country_B_Peak_Demand
bigDF$SupplyRatioA=bigDF$TotalOperationalCapacityPerZoneInMW_Country_A_Capacity/bigDF$PeakDemandPerZoneInMW_Country_A_Peak_Demand

bigDF$AggregateCash<-bigDF$ProducerCash_Energy_Producer_A+bigDF$ProducerCash_Energy_Producer_B+bigDF$ProducerCash_Energy_Producer_C+bigDF$ProducerCash_Energy_Producer_D+bigDF$ProducerCash_Energy_Producer_E

co2PricePlot<-plotTimeSeriesWithConfidenceIntervalByFacettedGroup(bigDF, "CO2Auction", "CO2 Price")

co2PricePlot<-co2PricePlot + geom_line(aes(y=NationalMinCO2PriceinEURpTon_CO2_price_floor_in__Country_A_, color="black"))+
  geom_line(aes(y=NationalMinCO2PriceinEURpTon_CO2_price_floor_in__Country_B_, colour="black"))
co2PricePlot2<-plotTimeSeriesWithConfidenceIntervalGroupedInOnePlot(bigDF, "CO2Auction", "CO2 Price")
co2PricePlot2
ggsave(filename="co2Price.pdf", plot=co2PricePlot, width=15.66, height=10.44, units="cm")

co2SpaghettiPrice<-ggplot(bigDF, aes(x=tick, y=CO2Auction))+
  geom_line(aes(group=runId, linestyle=runId), alpha=I(0.2))+
  stat_summary(aes_string(fill="run"), fun.data="median_hilow", conf.int=.5, geom="smooth") +
  #stat_summary(fun.data="median_hilow", conf.int=.95, geom="smooth")+
  facet_grid(. ~ run)+
  #ylim(0,300)+
  ylab("CO2 Price [EUR/ton]")+
  xlab("Time [a]")+
  theme_grey(base_size=8)+
  opts(legend.position="none")+
  geom_line(aes(y=NationalMinCO2PriceinEURpTon_CO2_price_floor_in__Country_A_, color="black"))+
  geom_line(aes(y=NationalMinCO2PriceinEURpTon_CO2_price_floor_in__Country_B_, colour="black"))
#co2SpaghettiPrice
ggsave(filename="co2PriceSpaghetti.pdf",plot=co2SpaghettiPrice, width=16.51, height=7.22, units="cm")

co2EmissionsSpaghetti<-ggplot(bigDF, aes(x=tick, y=CO2Emissions_inTonpA))+
  geom_line(aes(group=runId), alpha=I(0.2))+
  stat_summary(aes_string(fill="run"), fun.data="median_hilow", conf.int=.5, geom="smooth") +
  #stat_summary(fun.data="median_hilow", conf.int=.95, geom="smooth")+
  facet_wrap(~ run)+
  #ylim(0,300)+
  ylab("CO2 Emissions [t/a]")+
  xlab("Time [a]")+
  opts(legend.position="none")
a<-co2EmissionsSpaghetti+geom_line(aes(y=CO2CapinTonpA),linetype = 2, colour="red", size=1)
#a
ggsave(filename="co2EmissionsSpaghetti.pdf",plot=co2EmissionsSpaghetti, width=15.66, height=10.44, units="cm")

co2EmissionPlot<-plotTimeSeriesWithConfidenceIntervalByFacettedGroup(bigDF, "CO2Emissions_inTonpA", "CO2 Emissions [t/a]")
a<-co2EmissionPlot+geom_line(aes(y=CO2CapinTonpA),linetype = 2, colour="red", size=1)
a
avgPricePlotinA<-plotTimeSeriesWithConfidenceIntervalByFacettedGroup(bigDF, "Avg_El_PricesinEURpMWh_Country_A", "Avg. Electricity Price in Country A [EUR/MW]")
#avgPricePlotinA
ggsave(filename="avgPricePlotinA.pdf",plot=avgPricePlotinA, width=15.66, height=10.44, units="cm")

avgPricePlotinB<-plotTimeSeriesWithConfidenceIntervalByFacettedGroup(bigDF, "Avg_El_PricesinEURpMWh_Country_B", "Avg. Electricity Price in Country B [EUR/MW]")
#avgPricePlotinB
ggsave(filename="avgPricePlotinB.pdf",plot=avgPricePlotinB, width=15.66, height=10.44, units="cm")

aggregateCash<-plotTimeSeriesWithConfidenceIntervalByFacettedGroup(bigDF, "AggregateCash", "Aggregated Producer Cash")
#aggregateCash

peakLoadA<-plotTimeSeriesWithConfidenceIntervalGroupedInOnePlot(bigDF, "PeakDemandPerZoneInMW_Country_A_Peak_Demand", "Peak A")
#peakLoadA


peakLoadB<-plotTimeSeriesWithConfidenceIntervalGroupedInOnePlot(bigDF, "PeakDemandPerZoneInMW_Country_B_Peak_Demand", "Peak B")
#peakLoadB

coalPrice<-plotTimeSeriesWithConfidenceIntervalGroupedInOnePlot(bigDF, "FuelPricesPerGJ_Coal", "Coal")  
#coalPrice

gasPrice<-plotTimeSeriesWithConfidenceIntervalGroupedInOnePlot(bigDF, "FuelPricesPerGJ_Natural_Gas", "Gas")  
#gasPrice

biomassPrice<-plotTimeSeriesWithConfidenceIntervalGroupedInOnePlot(bigDF, "FuelPricesPerGJ_Biomass", "Biomass")
#biomassPrice


aggregateCashDelta<-plotTimeSeriesWithConfidenceIntervalByFacettedGroup(bigDF, "AggregateFinances_Profit", "Aggregate Delta Cash Flow")
#aggregateCashDelta

supplyRatioA<-plotTimeSeriesWithConfidenceIntervalByFacettedGroup(bigDF, "SupplyRatioA", "Peak Capacity Supply Ratio A")
#supplyRatioA

supplyRatioB<-plotTimeSeriesWithConfidenceIntervalByFacettedGroup(bigDF, "SupplyRatioB", "Peak Capacity Supply Ratio B")
#supplyRatioB

moltenCapacities<-melt(bigDF, id.vars=c("tick", "run"), measure.vars=names(bigDF)[grepl("CapacityinMW_", names(bigDF))])
moltenCapacities$variable<-renamer(moltenCapacities$variable, "CapacityinMW_", "")
moltenCapacities$variable<-renamer(moltenCapacities$variable, "CoalPulverizedCSS", "CoalPulverizedCCS")
moltenCapacities$variable<-renamer(moltenCapacities$variable, "CoalPulverizedCCS", "CoalPSC CCS")
moltenCapacities$variable<-renamer(moltenCapacities$variable, "CoalPulverizedSuperCritical", "CoalPSC")
#moltenCapacities$run<-factor(moltenCapacities$run, levels=c("No Price Floor","Price Floor in A","Price Floor in B","Common Price Floor"))
moltenCapacities$variable<-factor(moltenCapacities$variable, levels=c('Photovoltaic','WindOffshore','Wind','Biomass','IgccCCS','CoalPSC CCS','CcgtCCS','Nuclear','IGCC','CoalPSC','CCGT','OCGT'))
generationCapacitiesFacetted<-ggplot(moltenCapacities, aes_string(x="tick", y="value"))+ #colour=run, fill=run,
  stat_summary(aes_string(fill="run"), fun.data="median_hilow", conf.int=.5, geom="smooth") +
  stat_summary(fun.data="median_hilow", conf.int=.95, geom="smooth")+
  facet_grid(variable ~ run)+
  #facet_wrap(~ run)+
  xlab("Time [a]")+
  ylab("Capacity [MW]")+
  scale_fill_hue(name="Technology")+
  theme_grey(base_size=7)+
  opts(legend.position="none")
#generationCapacitiesFacetted
ggsave(filename="generationCapacitiesFacetted.pdf",plot=generationCapacitiesFacetted, width=30, height=16.51, units="cm")


stackedCapacityDiagram<-ggplot(moltenCapacities, aes(x=tick, y=value))+
    stat_summary(aes(fill=variable,order= -as.numeric(variable)), fun.y=median, geom="area", position="fill")+
    facet_wrap( ~ run)+
    xlab("Time [a]")+
    ylab("Capacity [%]")+
    #scale_fill_hue(name="Technology")+
    theme_grey(base_size=14)+
    opts(legend.position="bottom", legend.margin=unit(0.5, "cm"))+
    guides(fill=guide_legend(nrow=3, keywidth=1, keylength=1, keyheight=1))+
    scale_fill_brewer(type="qual", palette=3, name="Technology")
  #stackedCapacityDiagram
  ggsave(filename="stackedCapacityDiagram.pdf",plot=stackedCapacityDiagram, width= 15.66, height= 14, units="cm")

#moltenGeneration<-melt(bigDF, id.vars=c("tick", "run"), measure.vars= c('GenerationinMWh_Biomass', 'GenerationinMWh_CoalPulverizedSuperCritical', 'GenerationinMWh_CCGT', 'GenerationinMWh_OCGT', 'GenerationinMWh_Nuclear', 'GenerationinMWh_Wind', 'GenerationinMWh_IGCC', 'GenerationinMWh_CoalPulverizedCSS', 'GenerationinMWh_IgccCCS', 'GenerationinMWh_CcgtCCS', 'GenerationinMWh_WindOffshore', 'GenerationinMWh_Photovoltaic'))
moltenGeneration<-melt(bigDF, id.vars=c("tick", "run"), measure.vars=names(bigDF)[grepl("GenerationinMWh_", names(bigDF))])
moltenGeneration$variable<-renamer(moltenGeneration$variable, "GenerationinMWh_", "")
moltenGeneration$variable<-renamer(moltenGeneration$variable, "CoalPulverizedCSS", "CoalPulverizedCCS")
moltenGeneration$variable<-ordered(moltenGeneration$variable, levels=c('Photovoltaic','WindOffshore','Wind','Biomass','IgccCCS','CoalPulverizedCCS','CcgtCCS','Nuclear','IGCC','CoalPulverizedSuperCritical','CCGT','OCGT'))
generationFacetted<-ggplot(moltenGeneration, aes_string(x="tick", y="value", group="run", colour="run"))+ #colour=run, fill=run,
  stat_summary(aes_string(fill="run"), fun.data="median_hilow", conf.int=.5, geom="smooth") +
  #stat_summary(fun.data="median_hilow", conf.int=.95, geom="smooth")+
  #facet_grid(variable ~ .)+
  facet_wrap(~ variable, scales="free_y")+
  opts(legend.position="bottom")+
  xlab("Time [a]")+
  ylab("Generation [MWh]")+
  scale_fill_hue(name="Technology")+
  scale_fill_brewer(type="qual", palette=3, name="Technology")+
  coord_cartesian(xlim=c(0,5))
#generationFacetted
ggsave(filename="generationFacetted.pdf",plot=generationFacetted, width=16.51, height=21.94, units="cm")

stackedGenerationDiagram<-ggplot(moltenGeneration, aes(x=tick, y=value))+
  stat_summary(aes(fill=variable, order= -as.numeric(variable)), fun.y=median, geom="area", position="fill")+
  facet_grid( ~ run)+
  xlab("Time [a]")+
  ylab("Generation [MWh]")+
  scale_fill_hue(name="Technology")+
  scale_fill_brewer(type="qual", palette=3, name="Technology")
#stackedGenerationDiagram
ggsave(filename="stackedGenerationDiagram.pdf",plot=stackedGenerationDiagram, width=16.51, height=8.255, units="cm")


moltenCapacityinA<-melt(bigDF, id.vars=c("tick", "run"), measure.vars=names(bigDF)[grepl("CapacityinMWinA_", names(bigDF))])
moltenCapacityinA$variable<-renamer(moltenCapacityinA$variable, "CapacityinMWinA_", "")
moltenCapacityinA$variable<-renamer(moltenCapacityinA$variable, "CoalPulverizedCSS", "CoalPulverizedCCS")
moltenCapacityinA$variable<-renamer(moltenCapacityinA$variable, "CoalPulverizedCCS", "CoalPSC CCS")
moltenCapacityinA$variable<-renamer(moltenCapacityinA$variable, "CoalPulverizedSuperCritical", "CoalPSC")
moltenCapacityinA$variable<-ordered(moltenCapacityinA$variable, levels=c('Photovoltaic','WindOffshore','Wind','Biomass','IgccCCS','CoalPSC CCS','CcgtCCS','Nuclear','IGCC','CoalPSC','CCGT','OCGT'))
stackedCapacityinADiagram<-ggplot(moltenCapacityinA, aes(x=tick, y=value))+
  stat_summary(aes(fill=variable, order= -as.numeric(variable)), fun.y=median, geom="area", position="fill")+
  facet_grid( ~ run)+
  xlab("Time [a]")+
  ylab("Capacity in A [MW]")+
  scale_fill_hue(name="Technology")+
  scale_fill_brewer(type="qual", palette=3, name="Technology")+
  opts(legend.position="bottom", legend.margin=unit(0.5, "cm"))+
  guides(fill=guide_legend(nrow=3, keywidth=1, keylength=1, keyheight=1))
#stackedCapacityinADiagram
#ggsave(filename="stackedCapacityinADiagram.pdf",plot=stackedCapacityinADiagram, width=16.51, height=8.255, units="cm")
ggsave(filename="stackedCapacityinADiagram.pdf",plot=stackedCapacityinADiagram, width=16.8, height=19.2, units="cm")

moltenCapacityinB<-melt(bigDF, id.vars=c("tick", "run"), measure.vars=names(bigDF)[grepl("CapacityinMWinB_", names(bigDF))])
moltenCapacityinB$variable<-renamer(moltenCapacityinB$variable, "CapacityinMWinB_", "")
moltenCapacityinB$variable<-renamer(moltenCapacityinB$variable, "CoalPulverizedCSS", "CoalPulverizedCCS")
moltenCapacityinB$variable<-renamer(moltenCapacityinB$variable, "CoalPulverizedCCS", "CoalPSC CCS")
moltenCapacityinB$variable<-renamer(moltenCapacityinB$variable, "CoalPulverizedSuperCritical", "CoalPSC")
moltenCapacityinB$variable<-ordered(moltenCapacityinB$variable, levels=c('Photovoltaic','WindOffshore','Wind','Biomass','IgccCCS','CoalPSC CCS','CcgtCCS','Nuclear','IGCC','CoalPSC','CCGT','OCGT'))
stackedCapacityinBDiagram<-ggplot(moltenCapacityinB, aes(x=tick, y=value))+
  stat_summary(aes(fill=variable, order= -as.numeric(variable)), fun.y=median, geom="area", position="fill")+
  facet_grid( ~ run)+
  xlab("Time [a]")+
  ylab("Capacity in B [MW]")+
  scale_fill_hue(name="Technology")+
  scale_fill_brewer(type="qual", palette=3, name="Technology")+
  opts(legend.position="bottom", legend.margin=unit(0.5, "cm"))+
  guides(fill=guide_legend(nrow=3, keywidth=1, keylength=1, keyheight=1))
#stackedCapacityinBDiagram
#ggsave(filename="stackedCapacityinBDiagram.pdf",plot=stackedCapacityinBDiagram, width=16.51, height=8.255, units="cm")
ggsave(filename="stackedCapacityinBDiagram.pdf",plot=stackedCapacityinBDiagram, width=16.8, height=19.2, units="cm")

capcitiesinBFaceted<-ggplot(moltenCapacityinB, aes_string(x="tick", y="value", colour="run", fill="run"))+ #colour=run, fill=run,
  stat_summary(aes_string(colour="run", fill="run", group="run"), fun.data="median_hilow", conf.int=.5, geom="smooth") +
  #stat_summary(aes_string(colour="run", fill="run", group="run"), fun.data="median_hilow", conf.int=.95, geom="smooth")+
  facet_grid(variable ~ .)+
  #facet_wrap(~ run)+
  opts(legend.position="bottom")+
  xlab("Time [a]")+
  ylab("Capacity [MW]")+
  scale_fill_hue(name="Technology")+
  scale_fill_brewer(type="qual", palette=3, name="Technology")
#capcitiesinBFaceted
ggsave(filename="capcitiesinBFaceted.pdf",plot=capcitiesinBFaceted, width=16.51, height=21.94, units="cm")

#------------------Median Capacity Differences between no price floor and B -----------------------

applyFunToColumnInDF<- function(df=df, column=column, fun=fun) fun(df[,column])
medianCapacitiesInB<-ddply(.data=moltenCapacityinB, .variables=c("tick","run", "variable"), .fun=applyFunToColumnInDF, column="value", fun=median)
getDifferenceBetweenFloorBandNoPriceFloor<-function(df) df[df$run=="Price Floor in B",]$V1-df[df$run=="No Price Floor",]$V1
medianCapacityinBDifferenceBandNoPriceFloor<-ddply(.data=medianCapacitiesInB, .variables=c("variable", "tick"), .fun=getDifferenceBetweenFloorBandNoPriceFloor)
medianCapacitiesinA<-ddply(.data=moltenCapacityinA, .variables=c("tick","run", "variable"), .fun=applyFunToColumnInDF, column="value", fun=median)
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
#medianCapacitiesDifferenceFloorBandNoPriceFloorPlot
#ggsave(filename="medianCapacitiesDifferenceFloorBandNoPriceFloorPlot.pdf",plot=medianCapacitiesDifferenceFloorBandNoPriceFloorPlot, width=12.3825, height=43.88, units="cm")
ggsave(filename="medianCapacitiesDifferenceFloorBandNoPriceFloorPlot.pdf",plot=medianCapacitiesDifferenceFloorBandNoPriceFloorPlot, width=28, height=12, units="cm")

co2PriceVertical<-ggplot(subset(bigDF, run==c("No Price Floor", "Price Floor in B")), aes_string(x="tick", y="CO2Auction", group="run", linetype="run", colour="run"))+ #colour=run, fill=run,
    stat_summary(aes_string(fill="run"), fun.data="median_hilow", conf.int=.5, geom="line") +
    #stat_summary(fun.data="median_hilow", conf.int=.95, geom="line")+
    #facet_grid(. ~ run)+
    #facet_grid(run ~ .)+
    scale_color_discrete(h.start=90)+
    xlab(NULL)+
    ylab("CO2 Price [EUR/ton]")+
    theme_grey(base_size=8)+
    facet_grid(GenerationinMWh_IgccCCS ~ .)+ #just to add grid label
    opts(legend.position="bottom", strip.text.y = theme_text(size = 6, angle = 270))
  #co2PriceVertical
ggsave(filename="co2PriceVertical.pdf",plot=co2PriceVertical, width=11.46, height=7.0, units="cm")

#Adjusted sizes 7.83
ggsave(filename="medianDifferences.pdf",plot=(medianCapacitiesDifferenceFloorBandNoPriceFloorPlot), width=7.83, height=22, units="cm")
ggsave(filename="co2PriceVertical.pdf",plot=co2PriceVertical, width=7.83, height=4.782722513, units="cm")

#------------------Median Capacity Differences between no price floor and A -----------------------

applyFunToColumnInDF<- function(df=df, column=column, fun=fun) fun(df[,column])
medianCapacitiesInA<-ddply(.data=moltenCapacityinA, .variables=c("tick","run", "variable"), .fun=applyFunToColumnInDF, column="value", fun=median)
getDifferenceBetweenFloorAandNoPriceFloor<-function(df) df[df$run=="Price Floor in A",]$V1-df[df$run=="No Price Floor",]$V1
medianCapacityinBDifferenceAandNoPriceFloor<-ddply(.data=medianCapacitiesInB, .variables=c("variable", "tick"), .fun=getDifferenceBetweenFloorAandNoPriceFloor)
medianCapacitiesinA<-ddply(.data=moltenCapacityinA, .variables=c("tick","run", "variable"), .fun=applyFunToColumnInDF, column="value", fun=median)
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

co2PriceVerticalDifferenceA<-ggplot(subset(bigDF, run==c("No Price Floor", "Price Floor in A")), aes_string(x="tick", y="CO2Auction", group="run", linetype="run", colour="run"))+ #colour=run, fill=run,
  stat_summary(aes_string(fill="run"), fun.data="median_hilow", conf.int=.5, geom="line") +
  #stat_summary(fun.data="median_hilow", conf.int=.95, geom="line")+
  #facet_grid(. ~ run)+
  #facet_grid(run ~ .)+
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


#--------------Volatility------------------------
bigDF$co2PriceA<-pmax(bigDF$NationalMinCO2PriceinEURpTon_CO2_price_floor_in__Country_A_, bigDF$CO2Auction)
bigDF$co2PriceB<-pmax(bigDF$NationalMinCO2PriceinEURpTon_CO2_price_floor_in__Country_B_, bigDF$CO2Auction)

co2PriceVolatilityPerRunId<-function(df, co2PriceName){
  sd(diff(log(df[, co2PriceName])))/sqrt(length(diff(log(df[,co2PriceName]))))
}

sdPerRunId<-function(df,co2PriceName) sd(df[,co2PriceName])

ddply(ddply(bigDF, .variables=c("runId", "run"), .fun=sdPerRunId, "co2PriceA"), .variables="run", .fun=applyFunToColumnInDF ,fun=quantile, column="V1")
ddply(ddply(bigDF, .variables=c("runId", "run"), .fun=sdPerRunId, "co2PriceB"), .variables="run", .fun=applyFunToColumnInDF ,fun=quantile, column="V1")

moltenCO2Prices<-melt(data=bigDF, id.vars=c("runId", "run", "tick"), measure.vars=c("co2PriceA", "co2PriceB"))
moltenCO2Prices$run<-factor(moltenCO2Prices$run, levels=c("No Price Floor","Price Floor in A","Price Floor in B","Common Price Floor"))
moltenCO2Prices$variable<-renamer(moltenCO2Prices$variable, "co2PriceA", "Country A")
moltenCO2Prices$variable<-renamer(moltenCO2Prices$variable, "co2PriceB", "Country B")
moltenCO2Prices$variable<-factor(moltenCO2Prices$variable, levels=c("Country A","Country B"))
sdOfCO2Prices<-ddply(moltenCO2Prices, .variables=c("runId", "run", "variable"), .fun=sdPerRunId, "value")
boxPlotSDCO2Prices<-qplot(x=run, y=V1, data=sdOfCO2Prices, geom="boxplot")+facet_grid(variable ~ .)+
  xlab("Scenario")+
  ylab("Standard deviation of CO2 Prices")+
  theme_grey(base_size=13)
#boxPlotSDCO2Prices
ggsave(filename="boxPlotSDCO2Prices.pdf",plot=boxPlotSDCO2Prices, width=15.66, height=12, units="cm")

ddply(moltenCO2Prices, .variables=c("run", "variable"), .fun=function(df, columnname) quantile(df[,columnname]), "value")

#----------Median Price differences---------------------
moltenElectricityPrices<-melt(bigDF, id.vars=c("tick", "run"), measure.vars=c("Avg_El_PricesinEURpMWh_Country_A", "Avg_El_PricesinEURpMWh_Country_B"))
medianElectricityPrices<-ddply(.data=moltenElectricityPrices, .variables=c("tick","run", "variable"), .fun=applyFunToColumnInDF, column="value", fun=median)
medianElectricityPricePlot<-ggplot(data=medianElectricityPrices, aes(x=tick, y=V1, group=run, linetype=run, colour=run))+
  geom_line()+
  facet_grid(variable ~ .)
medianElectricityPricePlot

electricityPricesPlot<-ggplot(moltenElectricityPrices, aes(x=tick, y=value, group=run, colour=run, linetype=run))+ #colour=run, fill=run,
  stat_summary(aes_string(fill="run"), fun.data="median_hilow", conf.int=.5, geom="errorbar") +
  stat_summary(aes_string(fill="run"), fun.data="median_hilow", conf.int=.5, geom="line") +
  #stat_summary(aes(fill=run),fun.data="median_hilow", conf.int=.95, geom="smooth")+
  #facet_grid(. ~ run)+
  
  facet_wrap(~ variable)+
  opts(legend.position="bottom")+
  xlab("Time [a]")+
  ylab("Average Electricity Price [EUR/MWh]")+
  scale_color_brewer(type="qual", palette=2)+
  scale_fill_brewer(type="qual", palette=2)
#electricityPricesPlot

moltenElectricityPriceswithRunId<-melt(bigDF, id.vars=c("tick", "run", "runId"), measure.vars=c("Avg_El_PricesinEURpMWh_Country_A", "Avg_El_PricesinEURpMWh_Country_B"))
electricityPriceSpaghettiPlot<-ggplot(moltenElectricityPriceswithRunId, aes(x=tick, y=value))+
  geom_line(aes(group=runId, linestyle=runId), alpha=I(0.2))+
  stat_summary(fun.data="median_hilow", conf.int=0.95, geom="smooth", alpha=I(0.5))+
  stat_summary(aes_string(fill="run"), fun.data="median_hilow", conf.int=.5, geom="smooth") +
  facet_grid(run ~ variable)+
  opts(legend.position="none")
#electricityPriceSpaghettiPlot

#-----------------Median generation differences------------
