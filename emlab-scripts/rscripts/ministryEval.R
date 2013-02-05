########## Installing & loading packages ############
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


############ Reading in data ######################

noMinCO2<-getDataFrameForModelRun(outPutFolder, "noMinCO2", "Base Case",c("PowerPlantDispatchPlans","ProducerCosts"))
MinCO2<-getDataFrameForModelRun(outPutFolder, "MinCO2", "MinCO2, RES:Base",c("PowerPlantDispatchPlans","ProducerCosts"))
highMinCO2<-getDataFrameForModelRun(outPutFolder, "highMinCO2", "High MinCO2, RES:Base",c("PowerPlantDispatchPlans","ProducerCosts"))
highRes<-getDataFrameForModelRun(outPutFolder, "highRes", "CO2:Base, RES: +30%",c("PowerPlantDispatchPlans","ProducerCosts"))
lowRes<-getDataFrameForModelRun(outPutFolder, "lowRes", "CO2:Base, RES: -30%",c("PowerPlantDispatchPlans","ProducerCosts"))
nlMinCO2<-getDataFrameForModelRun(outPutFolder, "nlMinCO2", "MinCO2 Nl, RES:Base",c("PowerPlantDispatchPlans","ProducerCosts"))
noRes<-getDataFrameForModelRun(outPutFolder, "noRes", "noRes",c("PowerPlantDispatchPlans","ProducerCosts"))

#Take only first hundred
no=68
noMinCO2<-selectFirstXRunIdsFromDataFrame(noMinCO2, no)
MinCO2<-selectFirstXRunIdsFromDataFrame(MinCO2, no)
MinCO2$CO2Auction = pmax(MinCO2$CO2Auction, MinCO2$NationalMinCO2PriceinEURpTon_CO2_price_floor_in__Country_A_)
highMinCO2<-selectFirstXRunIdsFromDataFrame(highMinCO2, no)
highMinCO2$CO2Auction = pmax(highMinCO2$CO2Auction, highMinCO2$NationalMinCO2PriceinEURpTon_CO2_price_floor_in__Country_A_)
highRes<-selectFirstXRunIdsFromDataFrame(highRes, no)
lowRes<-selectFirstXRunIdsFromDataFrame(lowRes, no)
noRes<-selectFirstXRunIdsFromDataFrame(noRes, no)
nlMinCO2<-selectFirstXRunIdsFromDataFrame(nlMinCO2, no)

#bigDF<-sqldf("select * from dfNoMin100 union select * from dfDEMin100 union select * from dfNLMin100 union select * from dfNLMinWInt100 union select * from dfDEMinWInt100 union select * from dfNoMinWInt100")  
bigDF<-sqldf("select * from noMinCO2 union select * from MinCO2 union select * from highMinCO2 union select * from highRes union select * from lowRes union select * from nlMinCO2 union select * from noRes")


######### Creating additional variables #############

bigDF$SupplyRatioB=bigDF$TotalOperationalCapacityPerZoneInMW_Country_B_Capacity/bigDF$PeakDemandPerZoneInMW_Country_B_Peak_Demand
bigDF$SupplyRatioA=bigDF$TotalOperationalCapacityPerZoneInMW_Country_A_Capacity/bigDF$PeakDemandPerZoneInMW_Country_A_Peak_Demand
bigDF$CO2Auction_Revenues = bigDF$CO2Auction*bigDF$CO2Emissions_inTonpA
bigDF$AggregateCash<-bigDF$ProducerCash_Energy_Producer_A+bigDF$ProducerCash_Energy_Producer_B+bigDF$ProducerCash_Energy_Producer_C+bigDF$ProducerCash_Energy_Producer_D+bigDF$ProducerCash_Energy_Producer_E


#Create the right order for the runs
bigDF <- within(bigDF, run <- factor(run, levels = c("Base Case","MinCO2, RES:Base","High MinCO2, RES:Base","CO2:Base, RES: +30%", "CO2:Base, RES: -30%","MinCO2 Nl, RES:Base")))

################ Figure Settings ####################
format = ".png"
scale = 1
standardWidth=15.66
unit = "cm"
#Set output directory
setwd(paste0(outPutFolder,"/figures/"))

############# Evaluation Figures ######################

# CO2 Price ---------------------------------------------------------------


co2PricePlot<-plotTimeSeriesWithConfidenceIntervalByFacettedGroup(bigDF, "CO2Auction", "CO2 Price")

co2PricePlot<-co2PricePlot + geom_line(aes(y=NationalMinCO2PriceinEURpTon_CO2_price_floor_in__Country_A_, color="black"))+
  geom_line(aes(y=NationalMinCO2PriceinEURpTon_CO2_price_floor_in__Country_B_, colour="black"))
ggsave(filename=paste0("co2Price",format), plot=co2PricePlot, width=standardWidth*scale, height=2/3*standardWidth*scale, units=unit)

co2SpaghettiPrice<-ggplot(bigDF, aes(x=tick, y=CO2Auction))+
  geom_line(aes(group=runId, linestyle=runId), alpha=I(0.2))+
  stat_summary(aes_string(fill="run"), fun.data="median_hilow", conf.int=.5, geom="smooth") +
  #stat_summary(fun.data="median_hilow", conf.int=.95, geom="smooth")+
  facet_grid(. ~ run)+
  xlim(0,20)+
  ylab("CO2 Price [EUR/ton]")+
  xlab("Time [a]")+
  theme_grey(base_size=8)+
  opts(legend.position="none")+
  geom_line(aes(y=NationalMinCO2PriceinEURpTon_CO2_price_floor_in__Country_A_, color="black"))+
  geom_line(aes(y=NationalMinCO2PriceinEURpTon_CO2_price_floor_in__Country_B_, colour="black"))
co2SpaghettiPrice
#ggsave(filename=paste0("co2PriceSpaghetti",format),plot=co2SpaghettiPrice, width=1.054278416*standardWidth*scale, height=0.461*standardWidth*scale, units=unit)

CO2RevenuePlot<-plotTimeSeriesWithConfidenceIntervalByFacettedGroup(bigDF,"CO2Auction_Revenues", "CO2Auction_Revenues")
CO2RevenuePlot
# CO2 Emissions -----------------------------------------------------------

co2EmissionsSpaghetti<-ggplot(bigDF, aes(x=tick, y=CO2Emissions_inTonpA))+
  geom_line(aes(group=runId), alpha=I(0.2))+
  stat_summary(aes_string(fill="run"), fun.data="median_hilow", conf.int=.5, geom="smooth") +
  #stat_summary(fun.data="median_hilow", conf.int=.95, geom="smooth")+
  facet_wrap(~ run)+
  #ylim(0,300)+
  ylab("CO2 Emissions [t/a]")+
  xlab("Time [a]")+
  opts(legend.position="none")
co2EmissionsSpaghetti<-co2EmissionsSpaghetti+geom_line(aes(y=CO2CapinTonpA),linetype = 2, colour="red", size=1)
ggsave(filename=paste0("co2EmissionsSpaghetti",format),plot=co2EmissionsSpaghetti, width=standardWidth*scale, height=2/3*standardWidth*scale, units=unit)

co2EmissionPlot<-plotTimeSeriesWithConfidenceIntervalByFacettedGroup(bigDF, "CO2Emissions_inTonpA", "CO2 Emissions [t/a]")
co2EmissionPlot<-co2EmissionPlot+geom_line(aes(y=CO2CapinTonpA),linetype = 2, colour="red", size=1)
co2EmissionPlot
ggsave(filename=paste0("co2Emissions",format),plot=co2EmissionPlot, width=standardWidth*scale, height=2/3*standardWidth*scale, units=unit)

# Electricity Prices ------------------------------------------------------
avgPricePlotinA<-plotTimeSeriesWithConfidenceIntervalByFacettedGroup(bigDF, "Avg_El_PricesinEURpMWh_Country_A", "Avg. Electricity Price in Country A [EUR/MW]")
#avgPricePlotinA
ggsave(filename=paste0("avgPricePlotinA",format),plot=avgPricePlotinA, width=standardWidth*scale, height=2/3*standardWidth*scale, units=unit)

avgPricePlotinB<-plotTimeSeriesWithConfidenceIntervalByFacettedGroup(bigDF, "Avg_El_PricesinEURpMWh_Country_B", "Avg. Electricity Price in Country B [EUR/MW]")
#avgPricePlotinB
ggsave(filename=paste0("avgPricePlotinB",format),plot=avgPricePlotinB, width=standardWidth*scale, height=2/3*standardWidth*scale, units=unit)



# Producer Finances -------------------------------------------------------

aggregateCash<-plotTimeSeriesWithConfidenceIntervalByFacettedGroup(bigDF, "AggregateCash", "Aggregated Producer Cash")
aggregateCash

profits<-plotTimeSeriesWithConfidenceIntervalByFacettedGroup(bigDF, "AggregateCash", "Aggregated Producer Cash")

aggregateCashDelta<-plotTimeSeriesWithConfidenceIntervalByFacettedGroup(bigDF, "AggregateFinances_Profit", "Aggregate Delta Cash Flow")
aggregateCashDelta

# Load & VOLL hours -------------------------------------------------------

peakLoadA<-plotTimeSeriesWithConfidenceIntervalGroupedInOnePlot(bigDF, "PeakDemandPerZoneInMW_Country_A_Peak_Demand", "Peak A")
peakLoadA

peakLoadB<-plotTimeSeriesWithConfidenceIntervalGroupedInOnePlot(bigDF, "PeakDemandPerZoneInMW_Country_B_Peak_Demand", "Peak B")
#peakLoadB

bigDF$PercentServed = (1+bigDF$ShortagesInHoursUnserved/8660)
vollHours<-plotTimeSeriesWithConfidenceIntervalByFacettedGroup(bigDF, "PercentServed", "Percentage of Demand Served")
vollHours
ggsave(filename=paste0("vollHours",format),plot=vollHours, width=standardWidth*scale, height=2/3*standardWidth*scale, units=unit)

supplyRatioA<-plotTimeSeriesWithConfidenceIntervalByFacettedGroup(bigDF, "SupplyRatioA", "Supply Ratio A")
ggsave(filename=paste0("supplyRatioA",format),plot=supplyRatioA, width=standardWidth*scale, height=2/3*standardWidth*scale, units=unit)

supplyRatioBPlot<-plotTimeSeriesWithConfidenceIntervalByFacettedGroup(bigDF, "SupplyRatioB", "Supply Ratio B")
ggsave(filename=paste0("supplyRatioB",format),plot=supplyRatioBPlot, width=standardWidth*scale, height=2/3*standardWidth*scale, units=unit)

supplyRatioBSpaghetti<-ggplot(bigDF, aes(x=tick, y=SupplyRatioB))+
  geom_line(aes(group=runId, linestyle=runId), alpha=I(0.2))+
  stat_summary(aes_string(fill="run"), fun.data="median_hilow", conf.int=.5, geom="smooth") +
  #stat_summary(fun.data="median_hilow", conf.int=.95, geom="smooth")+
  facet_grid(. ~ run)+
  xlim(0,20)+
  ylab("Peak Available Capacity [MW]")+
  xlab("Time [a]")+
  theme_grey(base_size=8)+
  opts(legend.position="none")
supplyRatioBSpaghetti




# Fuel Prices -------------------------------------------------------------

coalPrice<-plotTimeSeriesWithConfidenceIntervalGroupedInOnePlot(bigDF, "FuelPricesPerGJ_Coal", "Coal")  
ggsave(filename=paste0("coalPrice",format),plot=coalPrice, width=standardWidth*scale, height=2/3*standardWidth*scale, units=unit)

gasPrice<-plotTimeSeriesWithConfidenceIntervalGroupedInOnePlot(bigDF, "FuelPricesPerGJ_Natural_Gas", "Gas")  
ggsave(filename=paste0("gasPrice",format),plot=gasPrice, width=standardWidth*scale, height=2/3*standardWidth*scale, units=unit)

biomassPrice<-plotTimeSeriesWithConfidenceIntervalGroupedInOnePlot(bigDF, "FuelPricesPerGJ_Biomass", "Biomass")
ggsave(filename=paste0("biomassPrice",format),plot=biomassPrice, width=standardWidth*scale, height=2/3*standardWidth*scale, units=unit)

peakDemandA<-plotTimeSeriesWithConfidenceIntervalByFacettedGroup(bigDF, "PeakDemandPerZoneInMW_Country_A_Peak_Demand", "Peak A")
peakDemandA

peakDemandB<-plotTimeSeriesWithConfidenceIntervalByFacettedGroup(bigDF, "PeakDemandPerZoneInMW_Country_B_Peak_Demand", "Peak B")
peakDemandB

# Capacities & Generation -------------------------------------------------

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
ggsave(filename=paste0("generationCapacitiesFacetted",format),plot=generationCapacitiesFacetted, width=30, height=16.51, units=unit)


stackedCapacityDiagram<-ggplot(moltenCapacities, aes(x=tick, y=value))+
  stat_summary(aes(fill=variable,order= -as.numeric(variable)), fun.y=median, geom="area", position="stack")+
  facet_wrap( ~ run)+
  xlab("Time [a]")+
  ylab("Capacity [%]")+
  #scale_fill_hue(name="Technology")+
  theme_grey(base_size=14)+
  opts(legend.position="bottom", legend.margin=unit(0.5, unit))+
  guides(fill=guide_legend(nrow=3, keywidth=1, keylength=1, keyheight=1))+
  scale_fill_brewer(type="qual", palette=3, name="Technology")
stackedCapacityDiagram
ggsave(filename=paste0("stackedCapacityDiagram",format),plot=stackedCapacityDiagram, width= standardWidth*scale, height= 0.893997446*standardWidth*scale, units=unit)

#moltenGeneration<-melt(bigDF, id.vars=c("tick", "run"), measure.vars= c('GenerationinMWh_Biomass', 'GenerationinMWh_CoalPulverizedSuperCritical', 'GenerationinMWh_CCGT', 'GenerationinMWh_OCGT', 'GenerationinMWh_Nuclear', 'GenerationinMWh_Wind', 'GenerationinMWh_IGCC', 'GenerationinMWh_CoalPulverizedCSS', 'GenerationinMWh_IgccCCS', 'GenerationinMWh_CcgtCCS', 'GenerationinMWh_WindOffshore', 'GenerationinMWh_Photovoltaic'))
moltenGeneration<-melt(bigDF, id.vars=c("tick", "run"), measure.vars=names(bigDF)[grepl("GenerationinMWh_", names(bigDF))])
moltenGeneration$variable<-renamer(moltenGeneration$variable, "GenerationinMWh_", "")
moltenGeneration$variable<-renamer(moltenGeneration$variable, "CoalPulverizedCSS", "CoalPulverizedCCS")
moltenGeneration$variable<-renamer(moltenGeneration$variable, "CoalPulverizedCSS", "CoalPulverizedCCS")
moltenGeneration$variable<-renamer(moltenGeneration$variable, "CoalPulverizedCCS", "CoalPSC CCS")
moltenGeneration$variable<-renamer(moltenGeneration$variable, "CoalPulverizedSuperCritical", "CoalPSC")
moltenGeneration$variable<-ordered(moltenGeneration$variable, levels=c('Photovoltaic','WindOffshore','Wind','Biomass','IgccCCS','CoalPSC CCS','CcgtCCS','Nuclear','IGCC','CoalPSC','CCGT','OCGT'))
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
ggsave(filename=paste0("generationFacetted",format),plot=generationFacetted, width=1.054278416*standardWidth*scale, height=1.372286079*standardWidth*scale, units=unit)

stackedGenerationDiagram<-ggplot(moltenGeneration, aes(x=tick, y=value))+
  stat_summary(aes(fill=variable, order= -as.numeric(variable)), fun.y=median, geom="area", position="stack")+
  facet_wrap( ~ run)+
  xlab("Time [a]")+
  ylab("Generation [MWh]")+
  theme_grey(base_size=14)+
  opts(legend.position="bottom", legend.margin=unit(0.5, unit))+
  guides(fill=guide_legend(nrow=3, keywidth=1, keylength=1, keyheight=1))+
  scale_fill_brewer(type="qual", palette=3, name="Technology")
#stackedGenerationDiagram
ggsave(filename=paste0("stackedGenerationDiagram",format),plot=stackedGenerationDiagram, width=16.8, height=19.2, units=unit)


moltenCapacityinA<-melt(bigDF, id.vars=c("tick", "run"), measure.vars=names(bigDF)[grepl("CapacityinMWinA_", names(bigDF))])
moltenCapacityinA$variable<-renamer(moltenCapacityinA$variable, "CapacityinMWinA_", "")
moltenCapacityinA$variable<-renamer(moltenCapacityinA$variable, "CoalPulverizedCSS", "CoalPulverizedCCS")
moltenCapacityinA$variable<-renamer(moltenCapacityinA$variable, "CoalPulverizedCCS", "CoalPSC CCS")
moltenCapacityinA$variable<-renamer(moltenCapacityinA$variable, "CoalPulverizedSuperCritical", "CoalPSC")
moltenCapacityinA$variable<-ordered(moltenCapacityinA$variable, levels=c('Photovoltaic','WindOffshore','Wind','Biomass','IgccCCS','CoalPSC CCS','CcgtCCS','Nuclear','IGCC','CoalPSC','CCGT','OCGT'))
stackedCapacityinADiagram<-ggplot(moltenCapacityinA, aes(x=tick, y=value))+
  stat_summary(aes(fill=variable, order= -as.numeric(variable)), fun.y=median, geom="area", position="stack")+
  facet_wrap( ~ run)+
  xlab("Time [a]")+
  ylab("Capacity in A [MW]")+
  scale_fill_hue(name="Technology")+
  scale_fill_brewer(type="qual", palette=3, name="Technology")+
  opts(legend.position="bottom", legend.margin=unit(0.5, unit))+
  guides(fill=guide_legend(nrow=3, keywidth=1, keylength=1, keyheight=1))
#stackedCapacityinADiagram
#ggsave(filename=paste0("stackedCapacityinADiagram",format),plot=stackedCapacityinADiagram, width=1.054278416*standardWidth*scale, height=0.527139208*standardWidth*scale, units=unit)
ggsave(filename=paste0("stackedCapacityinADiagram",format),plot=stackedCapacityinADiagram, width=16.8, height=19.2, units=unit)

moltenCapacityinB<-melt(bigDF, id.vars=c("tick", "run"), measure.vars=names(bigDF)[grepl("CapacityinMWinB_", names(bigDF))])
moltenCapacityinB$variable<-renamer(moltenCapacityinB$variable, "CapacityinMWinB_", "")
moltenCapacityinB$variable<-renamer(moltenCapacityinB$variable, "CoalPulverizedCSS", "CoalPulverizedCCS")
moltenCapacityinB$variable<-renamer(moltenCapacityinB$variable, "CoalPulverizedCCS", "CoalPSC CCS")
moltenCapacityinB$variable<-renamer(moltenCapacityinB$variable, "CoalPulverizedSuperCritical", "CoalPSC")
moltenCapacityinB$variable<-ordered(moltenCapacityinB$variable, levels=c('Photovoltaic','WindOffshore','Wind','Biomass','IgccCCS','CoalPSC CCS','CcgtCCS','Nuclear','IGCC','CoalPSC','CCGT','OCGT'))
stackedCapacityinBDiagram<-ggplot(moltenCapacityinB, aes(x=tick, y=value))+
  stat_summary(aes(fill=variable, order= -as.numeric(variable)), fun.y=median, geom="area", position="stack")+
  facet_wrap( ~ run)+
  xlab("Time [a]")+
  ylab("Capacity in B [MW]")+
  scale_fill_hue(name="Technology")+
  scale_fill_brewer(type="qual", palette=3, name="Technology")+
  opts(legend.position="bottom", legend.margin=unit(0.5, unit))+
  guides(fill=guide_legend(nrow=3, keywidth=1, keylength=1, keyheight=1))
#stackedCapacityinBDiagram
#ggsave(filename=paste0("stackedCapacityinBDiagram",format),plot=stackedCapacityinBDiagram, width=1.054278416*standardWidth*scale, height=0.527139208*standardWidth*scale, units=unit)
ggsave(filename=paste0("stackedCapacityinBDiagram",format),plot=stackedCapacityinBDiagram, width=16.8, height=19.2, units=unit)

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
ggsave(filename=paste0("capcitiesinBFaceted",format),plot=capcitiesinBFaceted, width=1.054278416*standardWidth*scale, height=1.372286079*standardWidth*scale, units=unit)

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
#ggsave(filename=paste0("medianCapacitiesDifferenceFloorBandNoPriceFloorPlot",format),plot=medianCapacitiesDifferenceFloorBandNoPriceFloorPlot, width=12.3825, height=43.88, units=unit)
ggsave(filename=paste0("medianCapacitiesDifferenceFloorBandNoPriceFloorPlot",format),plot=medianCapacitiesDifferenceFloorBandNoPriceFloorPlot, width=28, height=12, units=unit)

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
ggsave(filename=paste0("co2PriceVertical",format),plot=co2PriceVertical, width=11.46, height=7.0, units=unit)

#Adjusted sizes 7.83
ggsave(filename=paste0("medianDifferences",format),plot=(medianCapacitiesDifferenceFloorBandNoPriceFloorPlot), width=7.83, height=22, units=unit)
ggsave(filename=paste0("co2PriceVertical",format),plot=co2PriceVertical, width=7.83, height=4.782722513, units=unit)

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
ggsave(filename=paste0("medianCapacitiesDifferenceFloorAandNoPriceFloorPlot",format),plot=medianCapacitiesDifferenceFloorBandNoPriceFloorPlot, width=12.3825, height=43.88, units=unit)

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
ggsave(filename=paste0("co2PriceVerticalDifferenceA",format),plot=co2PriceVerticalDifferenceA, width=11.46, height=7.0, units=unit)

#Adjusted sizes 7.83
ggsave(filename=paste0("medianDifferences",format),plot=(medianCapacitiesDifferenceFloorBandNoPriceFloorPlot), width=7.83, height=22, units=unit)
ggsave(filename=paste0("co2PriceVertical",format),plot=co2PriceVertical, width=7.83, height=4.782722513, units=unit)


#--------------Volatility------------------------
bigDF$co2PriceA<-pmax(bigDF$NationalMinCO2PriceinEURpTon_CO2_price_floor_in__Country_A_, bigDF$CO2Auction)
bigDF$co2PriceB<-pmax(bigDF$NationalMinCO2PriceinEURpTon_CO2_price_floor_in__Country_B_, bigDF$CO2Auction)

co2PriceVolatilityPerRunId<-function(df, co2PriceName){
  sd(diff(log(df[, co2PriceName])))/sqrt(length(diff(log(df[,co2PriceName]))))
}

sdPerRunId<-function(df,co2PriceName) sd(df[,co2PriceName])

ddply(ddply(bigDF, .variables=c("runId", "run"), .fun=sdPerRunId, "co2PriceA"), .variables="run", .fun=applyFunToColumnInDF ,fun=quantile, column="V1")
ddply(ddply(bigDF, .variables=c("runId", "run"), .fun=sdPerRunId, "co2PriceB"), .variables="run", .fun=applyFunToColumnInDF ,fun=quantile, column="V1")

moltenCO2Prices<-melt(data=bigDF[(bigDF$run=="Base Case" | bigDF$run=="MinCO2, RES:Base" | bigDF$run=="High MinCO2, RES:Base" | bigDF$run=="MinCO2 Nl, RES:Base"),], id.vars=c("runId", "run", "tick"), measure.vars=c("co2PriceA", "co2PriceB"))
moltenCO2Prices$run<-factor(moltenCO2Prices$run, levels=c("Base Case","MinCO2, RES:Base","High MinCO2, RES:Base","MinCO2 Nl, RES:Base"))
moltenCO2Prices$variable<-renamer(moltenCO2Prices$variable, "co2PriceA", "Country A")
moltenCO2Prices$variable<-renamer(moltenCO2Prices$variable, "co2PriceB", "Country B")
moltenCO2Prices$variable<-factor(moltenCO2Prices$variable, levels=c("Country A","Country B"))
sdOfCO2Prices<-ddply(moltenCO2Prices, .variables=c("runId", "run", "variable"), .fun=sdPerRunId, "value")
boxPlotSDCO2Prices<-qplot(x=run, y=V1, data=sdOfCO2Prices, geom="boxplot")+facet_grid(variable ~ .)+
  xlab("Scenario")+
  ylab("Standard deviation of CO2 Prices")+
  theme_grey(base_size=13)
boxPlotSDCO2Prices
ggsave(filename=paste0("boxPlotSDCO2Prices",format),plot=boxPlotSDCO2Prices, width=standardWidth*scale*1.5, height=12, units=unit)

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