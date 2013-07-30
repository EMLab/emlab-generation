library(ggplot2)
plotPowerPlantDispatchForTickAndMarket <- function(tick, market, segmentID, simpleQueriesDF, PowerPlantDispatchPlans, 
                                                    SegmentClearingPoints, DemandLevels){
  country=sub(pattern=" electricity spot market",replacement="",x=market)
  effectiveCO2Price=round(max(simpleQueriesDF[simpleQueriesDF$tick==tick,]$CO2Auction+simpleQueriesDF[simpleQueriesDF$tick==tick,]$CO2Tax, simpleQueriesDF[simpleQueriesDF$tick==tick,][[paste("NationalMinCO2PriceinEURpTon_",country,sep="")]],simpleQueriesDF[simpleQueriesDF$tick==tick,]$EUCO2PriceFloor), 2)
  electricityPrice = round(SegmentClearingPoints[SegmentClearingPoints$market==market & SegmentClearingPoints$segmentID==segmentID & SegmentClearingPoints$tick==tick,]$price, 2)
  data=PowerPlantDispatchPlans[PowerPlantDispatchPlans$segmentID==segmentID & PowerPlantDispatchPlans$market==market & PowerPlantDispatchPlans$tick==tick,]
  data<-data[order(data$price, -data$status, data$technology),]
  data$status<-as.factor(data$status)
  #data$status<as.factor(data$status)
  data$volume<-cumsum(data$volume)
  data$volumePrev<-c(0,data$volume[1:length(data$volume)-1])
  xMax=round(sum(PowerPlantDispatchPlans[PowerPlantDispatchPlans$segmentID==20 & PowerPlantDispatchPlans$market==market & PowerPlantDispatchPlans$tick==tick,]$volume), -5)+50000
  yMax=max(max(data$price), 200)
  p<-ggplot(data) +
    geom_rect(aes(xmin = volumePrev, xmax = volume, ymin = -2, ymax = price, fill=technology, alpha=status))+
    scale_fill_manual("Technologies", values=c("CoalPSC" = "black", "Biomass" = "darkgreen", "Biogas"="darkolivegreen3", "Nuclear" = "purple", "Lignite" = "saddlebrown",
                                                "OCGT" = "darkred", "CCGT" = "blue", "PV" = "yellow", "Wind" = "chartreuse4",
                                                "CoalPscCCS" = "darkgray", "IGCC" = "orange", "IgccCCS"="orangered", "CcgtCCS" = "red",
                                                "WindOffshore" = "navyblue", "HydroPower" = "skyblue3"), drop=FALSE)+
    scale_alpha_manual("Status", values = c("-1" = 0.5, "2"=0.75, "3"=1), drop=FALSE, guide="none")+
    geom_rect(aes(xmin = volumePrev, xmax = volume, ymin = bidWithoutCO2-0.5, ymax = bidWithoutCO2+0.5), fill="white", alpha=0.8)+
    xlim(0,xMax)+
    ylim(-2,yMax)+
    xlab("Capacity [MW]")+
    ylab("Bid [EUR/MWh]")+
    geom_vline(xintercept = DemandLevels[DemandLevels$market==market & DemandLevels$segmentID==segmentID & DemandLevels$tick==tick,]$volume, colour="orange")+
    geom_vline(xintercept = SegmentClearingPoints[SegmentClearingPoints$market==market & SegmentClearingPoints$segmentID==segmentID & SegmentClearingPoints$tick==tick,]$volume, colour="red")+
    annotate("text", x=0.80*xMax, y=0.95*yMax, label=paste("Carbon Price:  ",effectiveCO2Price, " EUR/tCO2 \nEl. Spot Price:  ", electricityPrice, " EUR/MWh", sep=""))+
    guides(fill = guide_legend(order = 1))
  return(p)
}

printPowerPlantDispatchForAllTicksAndMarkets <- function(tick, simpleQueriesDF, PowerPlantDispatchPlans, 
                                                         SegmentClearingPoints, DemandLevels, analysisFolder, fileType){
  countries=sub(pattern=" electricity spot market",replacement="",x=unique(SegmentClearingPoints$market))
  for(market in unique(SegmentClearingPoints$market)){
    for(i in seq(1:20)){
      p<-plotPowerPlantDispatchForTickAndMarket(tick, market, i, simpleQueriesDF, PowerPlantDispatchPlans, SegmentClearingPoints, DemandLevels)
      chartName<-paste(analysisFolder,gsub("\\s","", market),"_Tick",tick,"_Segment",i,fileType, sep="")
      ggsave(filename=chartName, plot=p, width=10, height=5)
    }
  }
}

plotTimeSeriesForVariable <- function(df, variable, ylabel){
  p<-ggplot(df, aes_string(x="tick", y=variable))+ #colour=modelRun, fill=modelRun,
    geom_line()+
    theme(legend.position="none")+
    xlab("Time [a]")+
    ylab(ylabel)
  return(p)
  }

printTimeSeriesForVariable <- function(df, variable, ylabel, filetype, filename=NULL, analysisFolder=analysisFolder){
  p<-plotTimeSeriesForVariable(df, variable, ylabel)
  if(is.null(filename))
    filename = variable
  filename<-paste(paste(analysisFolder,filename,filetype, sep=""))
  ggsave(filename=filename, plot=p, width=10, height=5)
}

getSDofCO2PriceForSingleRun<-function(df){sd(df$CO2Auction)}