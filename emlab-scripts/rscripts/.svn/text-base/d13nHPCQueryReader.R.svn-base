  #setwd("/Users/Joern/Documents/d13n/rscripts")
  source("AgentSpringHPCReader.R")
  source("TimeSeriesSummariser.R")
  
  
  getDataFrameForModelRun <- function(outPutFolder, modelRun, name){
    setwd(paste0(outPutFolder,"/",modelRun,"/"))
    runIds<-runIdsFromWorkingDirectory()
    jobname<-jobNameFromRunIds(runIds)
    #Checks if file was already read in, else reads it in and saves it.
    if(file.exists(paste0(jobname,".csv"))){
      df<-read.table(paste0(jobname,".csv"))
    } else{
      headers<-headersFromRunIds(runIds)
      #df<-bigDataFrameForSeveralRunIdsAndQueries(runIds, headers)
      #write.table(df,file=paste0(jobname,".csv"))
      bigDataFrameForSeveralRunIdsAndQueries(runIds, headers, jobname)
      df<-read.table(paste0(jobname,".csv"))
    }
    df<-cbind(df, run=rep.int(name, dim(df)[1]))
    return(df)
  }
  
  plotTimeSeriesWithConfidenceIntervalByFacettedGroup <- function(df, variable, ylabel){
    g<-ggplot(df, aes_string(x="tick", y=variable))+ #colour=run, fill=run,
      stat_summary(aes_string(fill="run"), fun.data="median_hilow", conf.int=.5, geom="smooth") +
      stat_summary(fun.data="median_hilow", conf.int=.95, geom="smooth")+
      #facet_grid(. ~ run)+
      facet_wrap(~ run)+
      opts(legend.position="none")+
      xlab("Time [a]")+
      ylab(ylabel)
  }
  
  plotTimeSeriesWithConfidenceIntervalGroupedInOnePlot <- function(df, variable, ylabel){
    g<-ggplot(df, aes_string(x="tick", y=variable, group="run", colour="run", linestyle="run"))+ #colour=run, fill=run,
      stat_summary(aes_string(fill="run", linestyle="run"), fun.data="median_hilow", conf.int=.5, geom="errorbar") +
      stat_summary(aes_string(fill="run", linestyle="run"), fun.data="median_hilow", conf.int=.5, geom="line") +
      #stat_summary(fun.data="median_hilow", conf.int=.95, geom="smooth")+
      #facet_grid(. ~ run)+
      #facet_wrap(~ run)+
      opts(legend.position="bottom")+
      xlab("Time [a]")+
      ylab(ylabel)
  }
  
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
  
  getSDofCO2PriceForSingleRun<-function(df){sd(df$CO2Auction)}