library(rjson)
library(sqldf)
source("AgentSpringQueryReader.R")
source("rConfig.R")
#Set output directory of simulation in corresponding 
#Analysis R script.


# Python Implementation ---------------------------------------------------

getDataFrameForModelRun <- function(outPutFolder, modelRun, name, ignoredHeaders=""){
  ignoredHeaderString = ""
  for(header in ignoredHeaders){
    ignoredHeaderString<-paste(ignoredHeaderString, header)
  }
  if(!file.exists(paste(outPutFolder,modelRun,".csv", sep=""))){
    system(paste('python ',rscriptFolder,"asHeadlessQueryReader.py ", outPutFolder, " ", modelRun, ignoredHeaderString, sep=""))
  }
  df<-read.csv(paste(outPutFolder,modelRun,".csv", sep=""))
  df<-cbind(runId=df$runId, tick=df$tick, subset(df, select=-c(tick, runId)))
  df<-cbind(modelRun=rep.int(name, dim(df)[1]), df)
 return(df)
}

getDataFrameForModelRunsInFolder <- function(outPutFolder, ignoredHeaders="", onlyTakeCommonColumns=T){
  modelRuns=list.dirs(outPutFolder)
  df<-getDataFrameForModelRun(outPutFolder, modelRuns[1], modelRuns[1], ignoredHeaders)
  for(modelRun in modelRuns[-1]){
    df2<-getDataFrameForModelRun(outPutFolder, modelRun, modelRun, ignoredHeaders)
    if(onlyTakeCommonColumns==T)
      df<-rbind(df[,intersect(names(df), names(df2))], df2[,intersect(names(df), names(df2))])
    else
      df<-rbind(df,df2)
  }
  return(df)
}

getDataFrameForModelRunsInFolderWithFilePattern <- function(outPutFolder, pattern, ignoredHeaders="", onlyTakeCommonColumns=T){
  csvFiles=Sys.glob(paste(outPutFolder,pattern,sep=""))
  df<-read.csv(csvFiles[1])
  df<-cbind(runId=df$runId, tick=df$tick, subset(df, select=-c(tick, runId)))
  name=gsub(pattern=outPutFolder,"",gsub(pattern=".csv","",csvFiles[1]))
  df<-cbind(modelRun=rep.int(name, dim(df)[1]), df)
  for(csvFile in csvFiles[-1]){
    df2<-read.csv(csvFile)
    df2<-cbind(runId=df2$runId, tick=df2$tick, subset(df2, select=-c(tick, runId)))
    name=gsub(pattern=outPutFolder,"",gsub(pattern=".csv","",csvFile))
    df2<-cbind(modelRun=rep.int(name, dim(df2)[1]), df2)
    if(onlyTakeCommonColumns==T)
      df<-rbind(df[,intersect(names(df), names(df2))], df2[,intersect(names(df), names(df2))])
    else
      df<-rbind(df,df2)
  }
  return(df)
}

getTableForRunId <- function(outputFolder, modelRun, runId, tableName){
  if(!file.exists(paste(outputFolder,runId,"-",tableName,".csv", sep=""))){
    setwd(outputFolder)
    system(paste('python ',rscriptFolder,"asHeadlessTableReader.py ", outputFolder, " ", modelRun, " ", runId, " ",  tableName, sep=""))
  }
  df<-read.csv(paste(outputFolder,runId,"-",tableName,".csv", sep=""))
  return(df)
}

getTableForRunIdAndTick <- function(outputFolder, modelRun, runId, tableName, tick){
  rawData <- readRawQueryOutput(outputFolder, modelRun, runId, paste("TABLE_",tableName, sep=""))
  i=0
  for(tickElement in rawData){
    if(i==tick)
      return(tableQueryResultToDataFrame(fromJSON(tickElement)[[1]], tableName))
    i<-i+1
  }
}

readRawQueryOutput <- function(outputFolder, modelRun, runId, queryName){
  return(readLines(paste(outputFolder,modelRun,"/",runId,"-",queryName, sep="")))
}

list.dirs <- function(path=".", pattern=NULL, all.dirs=FALSE,
                      full.names=FALSE, ignore.case=FALSE) {
  
  all <- list.files(path, pattern, all.dirs,
                    full.names, recursive=FALSE, ignore.case)
  all[file.info(paste(path,all, sep=""))$isdir]
}