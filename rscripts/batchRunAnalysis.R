library(ggplot2)
library(plyr)
library(reshape)
library(grid)
library(ggthemes)
source("AgentSpringHeadlessReader.R")
source("TimeSeriesSummariser.R")

technologyPalette=c("CoalPSC" = "black", "Biomass" = "darkgreen", "Biogas"="darkolivegreen3", "Nuclear" = "purple", "Lignite" = "saddlebrown",
                    "OCGT" = "darkred", "CCGT" = "blue", "PV" = "yellow", "Wind" = "chartreuse4",
                    "CoalPscCCS" = "darkgray", "IGCC" = "orange", "IgccCCS"="orangered", "CcgtCCS" = "red",
                    "WindOffshore" = "navyblue", "HydroPower" = "skyblue3")

technologyOrder=c("Nuclear","Lignite","CoalPSC","CoalPscCCS","IGCC","IgccCCS","CCGT","CcgtCCS","OCGT","HydroPower","Biomass","Biogas",
                  "Wind","WindOffshore","PV")

renamerList=list(list("CoalPscCSS","CoalPscCCS"),list("Photovoltaic","PV"))


# Generic Data Preparation -------------------------------------------------

meltPrefixVariables <- function(df, variablePrefix, renamerList=NULL, factor=TRUE, orderedFactorVector=NULL){
  ## This function can be used to melt and factor variables, in order to use them for example in stacked graphs.
  ## The variablePrefix is used to select columns of the originating dataframe, and
  ## the renamerList can be used to potentially further rename/shorten the variables.
  ## The renamerList should be double-nested and contain char: 
  ## renamerList=list(list("orig1","new1"),list("orig2","new2"))
  moltenVariable<-melt(df, id.vars=c("tick", "runId", "modelRun"), measure.vars=names(df)[grepl(variablePrefix, names(df))])
  moltenVariable$variable<-renamer(moltenVariable$variable, variablePrefix, "")
  for(renameElement in renamerList){
    moltenVariable$variable<-renamer(moltenVariable$variable, renameElement[1], renameElement[2])
  }
  if(!factor)
    return(moltenVariable)
  if(!is.null(orderedFactorVector))
    moltenVariable$variable<-factor(moltenVariable$variable, levels=orderedFactorVector, ordered=TRUE)
  else
    moltenVariable$variable<-factor(moltenVariable$variable)
  return(moltenVariable)
}

applyFunToColumnInDF<- function(df=df, column=column, fun=fun, ...) fun(df[,column], ...)
sdPerRunId<-function(df,variableName) sd(df[,variableName])

quantilesOfStandardDeviationOverModelRun <- function(df, variableName){
  quantiles <- ddply(ddply(df, .variables=c("runId", "modelRun"), .fun=sdPerRunId, variableName), .variables="modelRun", .fun=applyFunToColumnInDF ,fun=quantile, column="V1")
  return(quantiles)
}

addSumOfVariablesByVariableListToDF <- function(df, variableNames, newColumnName){
  sum<-apply(df[variableNames],1,sum)
  oldNames<-names(df)
  df<-cbind(df, sum)
  names(df)<-c(oldNames,newColumnName)
  return(df)
}

addSumOfVariablesByPrefixToDF<-function(df, prefix, newColumnName=NULL){
  sum<-apply(df[names(df)[grepl(prefix, names(df))]],1,sum)
  oldNames<-names(df)
  df<-cbind(df, sum)
  names(df)<-c(oldNames,paste(prefix,"Sum", sep=""))
  return(df)
}

diffExpenditures<-function(df, nameOfOriginalVariable, nameOfNewVariable){
  df[[nameOfNewVariable]]<-diff(c(0,df[[nameOfOriginalVariable]]))
  return(df)
}

diffExpenditures2<-function(df, list, zeroValue){
  for(pair in list){
    df[[pair[2]]]<-diff(c(zeroValue,df[[pair[1]]]))
  }
  return(df)
}

functionOfVariablePerRunId<-function(df,FUN,variableName) FUN(df[,variableName])

functionOfVariablePerRunIdSpecificPerkWh<-function(df,FUN,variableName){FUN(df[,variableName])/(sum(df[,"Total_DemandinMWh_Country.B"])+sum(df[,"Total_DemandinMWh_Country.A"]))}

functionOfVariablePerRunIdSpecificPerkWhAndCountry<-function(df,FUN,variableName,demandInCountryName){FUN(df[,variableName])/(sum(df[,demandInCountryName]))}
# Purpose-specific data preparation ---------------------------------------


meltTechnologyVariable <- function(df, variablePrefix){
  return(meltPrefixVariables(df, variablePrefix, renamerList=renamerList, orderedFactorVector=technologyOrder))
  #return(meltPrefixVariables(df, variablePrefix, renamerList=renamerList))
}

addSupplyRatios <- function(df){
  operationalCapacityVariables=names(df)[grepl("TotalOperationalCapacityPerZoneInMW_", names(df))]
  countries=strsplit(operationalCapacityVariables,"_")
  for(i in seq(1:length(countries))){
    supplyRatio <- df[[paste("TotalOperationalCapacityPerZoneInMW_",countries[[i]][2], sep="")]] / df[[paste("PeakDemandPerZoneInMW_",countries[[i]][2], sep="")]]
    oldNames<-names(df)
    df<-cbind(df, supplyRatio)
    names(df)<-c(oldNames,paste("SupplyRatio_",countries[[i]][2], sep=""))
  }
  return(df)
}

addProducerCashBalanceForAll <- function(df){
  energyProducers=names(df)[grepl("ProducerCash_Energy.", names(df))]
  producerCash<-0
  for(i in seq(1:length(energyProducers))){
    producerCash <- producerCash + df[[energyProducers]]
    oldNames<-names(df)
    df<-cbind(df, producerCash)
    names(df)<-c(oldNames,"TotalEnergyProducerCash")
  }
  return(df)
}


# Generic Plotting Functions over Time ------------------------------------

plotStackedDiagram <- function(moltenVariable, ylabel, legendName, absolute=TRUE, variable="variable", value="value", xlabel="Time [a]", manuelPalette=NULL, summaryFunction=median, nrow=NULL){
  ## This function can plot stacked diagrams of variables over time, and is set to accept the standard
  ## output of the melt (reshape) function. the absolute parameter can be used to switch between absolute
  ## and relative plotting (e.g. 20GW installed capacity vs 20% of a total of 100GW)
  ## summaryFunction defines which aggregating function is used, the standard value is median, but can be changed to mean.
  ## or other functions
  if(absolute)
    position="stack"
  else
    position="fill"
  p<-ggplot(moltenVariable, aes_string(x="tick", y=value))+
    stat_summary(aes_string(fill=variable,order="-desc(variable)"), fun.y=summaryFunction, geom="area", position=position)+
    facet_wrap( ~ modelRun, nrow=nrow)+
    xlab("Time [a]")+
    ylab(ylabel)+
    theme_grey(base_size=14)+
    theme(legend.position="bottom", legend.margin=unit(0.5, "cm"))+
    guides(fill=guide_legend(nrow=3, keywidth=1, keylength=1, keyheight=1))
  if(is.null(manuelPalette))
    p <- p + scale_fill_brewer(type="qual", palette=3, name="Technology")
  else
    p <- p + scale_fill_manual(name=legendName, values=manuelPalette)
  return(p)
}


plotTimeSeriesWithConfidenceIntervalByFacettedGroup <- function(df, variable, ylabel, fun.data="median_hilow", conf.int=0.5, conf.int2=0.90, nrow=NULL){
  g<-ggplot(df, aes_string(x="tick", y=variable))+ #colour=modelRun, fill=modelRun,
    #stat_summary(aes_string(fill="modelRun"), fun.data=fun.data, conf.int=conf.int, geom="smooth") +
    stat_summary(fun.data=fun.data, conf.int=conf.int, geom="smooth", colour="black") +
    stat_summary(fun.data=fun.data, conf.int=conf.int2, geom="smooth", colour="black")+
    #facet_grid(. ~ modelRun)+
    facet_wrap(~ modelRun, nrow=nrow)+
    theme(legend.position="none")+
    xlab("Time [a]")+
    ylab(ylabel)
}


plotTimeSeriesWithOnly50PerConfidenceIntervalByFacettedGroup <- function(df, variable, ylabel, nrow=NULL){
  g<-ggplot(df, aes_string(x="tick", y=variable))+ #colour=run, fill=run,
    stat_summary(aes_string(fill="modelRun"), fun.data="median_hilow", conf.int=.5, geom="smooth") +
    #facet_grid(. ~ run)+
    facet_wrap(~ run, nrow=nrow)+
    theme(legend.position="none")+
    xlab("Time [a]")+
    ylab(ylabel)
}


plotTimeSeriesWithConfidenceIntervalGroupedInOnePlot <- function(df, variable, ylabel){
  g<-ggplot(df, aes_string(x="tick", y=variable, group="modelRun", colour="modelRun", linestyle="modelRun"))+ #colour=run, fill=run,
    stat_summary(aes_string(fill="modelRun", linestyle="modelRun"), fun.data="median_hilow", conf.int=.5, geom="errorbar") +
    stat_summary(aes_string(fill="modelRun", linestyle="modelRun"), fun.data="median_hilow", conf.int=.5, geom="line") +
    #stat_summary(fun.data="median_hilow", conf.int=.9, geom="smooth")+
    #facet_grid(. ~ run)+
    #facet_wrap(~ run)+
    theme(legend.position="bottom")+
    xlab("Time [a]")+
    ylab(ylabel)
}


plotSpaghettiTimeSeries <- function(df, variable, ylabel, xlabel="Time [a]", ylim=NULL, basesize=8, nrow=NULL){
  p<- ggplot(df, aes_string(x="tick", y=variable))+
      geom_line(aes_string(group="runId", linestyle="runId"), alpha=I(0.2))+
      stat_summary(aes_string(fill="modelRun"), fun.data="median_hilow", conf.int=.5, geom="smooth") +
      #stat_summary(fun.data="median_hilow", conf.int=.9, geom="smooth")+
      facet_wrap( ~ modelRun, nrow=nrow)+
      ylab(ylabel)+
      xlab(xlabel)+
      theme_grey(base_size=basesize)+
      theme(legend.position="none")
  if(!is.null(ylim))
    p<- p + ylim(ylim)
  return(p)
}

plotMoltenVariableFacettedByVariable <- function(moltenDF, ylabel, facet_wrap=T){
  facet_wrap_option = facet_wrap(~ variable, scales="free_y")
  if(!facet_wrap)
    facet_wrap_option =facet_grid(variable ~ .)
  g<-ggplot(moltenDF, aes_string(x="tick", y="value", colour="modelRun", fill="modelRun"))+ #colour=modelRun, fill=modelRun,
    stat_summary(aes_string(colour="modelRun", fill="modelRun", group="modelRun"), fun.y="mean", geom="line", linetype=1)+
    stat_summary(data=moltenDF[moltenDF$tick%%5==0 | moltenDF$tick==39,],aes_string(group="modelRun",shape="modelRun"), fun.y="mean", geom="point", size=1.5)+
    #stat_summary(aes_string(colour="modelRun", fill="modelRun", group="modelRun"), fun.data="median_hilow", conf.int=.5, geom="smooth") +
    #stat_summary(aes_string(colour="modelRun", fill="modelRun", group="modelRun"), fun.data="median_hilow", conf.int=.9, geom="smooth")+
    facet_wrap_option+
    #facet_wrap(~ modelRun)+
    theme(legend.position="bottom")+
    scale_color_discrete(name="Scenario")+
    scale_fill_discrete(name="Scenario")+
    scale_shape_discrete(name="Scenario")+
    xlab("Time [a]")+
    ylab(ylabel)
  return(g)
}

plotBlackAndWhiteMoltenVariableFacettedByVariable <- function(moltenDF, ylabel, facet_wrap=T){
  facet_wrap_option = facet_wrap(~ variable, scales="free_y")
  if(!facet_wrap)
    facet_wrap_option =facet_grid(variable ~ .)
  g<-ggplot(moltenDF, aes_string(x="tick", y="value"))+ #colour=modelRun, fill=modelRun,
    stat_summary(aes_string(group="modelRun",linetype="modelRun"), fun.y="mean", geom="line")+
    stat_summary(data=moltenDF[moltenDF$tick%%5==0 | moltenDF$tick==39,],aes_string(group="modelRun",shape="modelRun"), fun.y="mean", geom="point", size=1.5)+
    facet_wrap_option+
    #facet_wrap(~ modelRun)+
    theme(legend.position="bottom")+
    scale_linetype_discrete(name="Scenario")+
    scale_shape_discrete(name="Scenario")+
    xlab("Time [a]")+
    ylab(ylabel)
  return(g)
}


# Purpose-specific Plotting Functions over Time ---------------------------


plotStackedTechnologyDiagram <- function(moltenVariable, ylabel, absolute=TRUE, ...){
  moltenVariable$variable<-factor(moltenVariable$variable, levels=technologyOrder)
  return(plotStackedDiagram(moltenVariable, ylabel, legendName="Technology", manuelPalette=technologyPalette, absolute=absolute, ...))
}




# Others ------------------------------------------------------------------

selectFirstXRunIdsFromDataFrame <- function(df, X){
  df[is.element(df$runId,as.vector(unique(df$runId))[1:X]),]
}


selectFirstXRunIdsFromDataFrameAndCheckForLength <- function(df, X, length){
  table<-ddply(df, .variables=c("runId"), .fun=correctLengthOfSubDataFrame, length)
  uniqueRunIdsWithCorrectLength<-table[table$V1,]$runId
  df[is.element(df$runId,as.vector(uniqueRunIdsWithCorrectLength)[1:X]),]
}


correctLengthOfSubDataFrame<-function(df, length){
  length(df$tick)==length
}


renamer <- function(x, pattern, replace) {
  for (i in seq_along(pattern))
    x <- gsub(pattern[i], replace[i], x)
  x
}

theme_publication<-function(base_size=11, base_family="serif", ticks=TRUE){
  ret<-theme_tufte(base_size=base_size)+
    theme(legend.margin=unit(-1, "cm"), plot.margin=unit(x=c(1,2,1,1),units="mm"),#axis.line=element_line(),axis.line.x=element_line(),axis.line.y=element_line(),
          panel.background = element_rect(fill = "white", colour = NA), 
          panel.border = element_rect(fill = NA, 
                                      colour = "black"), 
          panel.grid = element_line(colour = "grey98", size = 0.2),
          #panel.grid.major = element_line(colour = "grey98", size = 0.2),
          panel.grid.minor=element_blank(),
          #panel.grid.minor.y = element_line(colour = "grey98", size = 0.5),
          axis.ticks = element_line(colour = "black")
          #strip.background = element_rect(fill = "grey80", colour = "grey50"), 
    )
  ret
}
