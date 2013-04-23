library(rjson)
library(sqldf)
source("AgentSpringQueryReader.R")
#Set output directory of simulation in corresponding 
#Analysis R script.

# Pure R Implementation ---------------------------------------------------

getDataFrameForModelRun <- function(outPutFolder, modelRun, name, ignoredHeaders){
 setwd(paste0(outPutFolder,"/",modelRun,"/"))
 #Checks if file was already read in, else reads it in and saves it.
 if(file.exists(paste0(modelRun,".csv"))){
   df<-read.table(paste0(modelRun,".csv"))
 } else{
   runIds<-runIdsFromWorkingDirectory()
   jobname<-jobNameFromRunIds(runIds)
   headers<-headersFromRunIds(runIds)
   headers<-setdiff(headers,ignoredHeaders)
   #df<-bigDataFrameForSeveralRunIdsAndQueries(runIds, headers)
   #write.table(df,file=paste0(jobname,".csv"))
   bigDataFrameForSeveralRunIdsAndQueries(runIds, headers, jobname)
   df<-read.table(paste0(jobname,".csv"))
 }
 df<-cbind(df, run=rep.int(name, dim(df)[1]))
 return(df)
}

#Given the run.id and queryName, reads to a array of characters (each line one timestep)
readRawQueryOutput <- function(runId, queryName){
 return(readLines(paste(runId,"-",queryName, sep="")))
}

#Gives number of timesteps in given raw file
numberOfTimeSteps <- function(rawQueryOutput){
  return(length(rawQueryOutput))
}

#Gives number of subvariables in given raw file
numberOfSubVariables <- function(rawQueryOutput){
  return(length(fromJSON(rawQueryOutput)))
}

#Gives back a dataframe for several queries and a single run.id
#Does not include run.id or tick within the table.
dataFrameForRunIdAndQueryNames <- function(runId, vectorOfQueryNames){
  df <- dataFrameForRunIdAndSingleQuery(runId, vectorOfQueryNames[1])
  if(length(vectorOfQueryNames)>1){
    for(query in vectorOfQueryNames[-1]){
      df2<-dataFrameForRunIdAndSingleQuery(runId, query)
      df<-cbind(df, df2)
      rm(df2)
    }
  }
  return(df)
}
                 
dataFrameForRunIdAndSingleQuery2 <- function(runId, queryName){
  rawData <- readRawQueryOutput(runId, queryName)
  noTimeSteps <- numberOfTimeSteps(rawData)
  #dataAsList<-fromJSON(rawData)
  #dfList<-list(simpleQueryResultToDataFrame(dataAsList[[1]], queryName))
  i=0
  for(tick in rawData){
    if(i==0)
      dfList = list(simpleQueryResultToDataFrame(fromJSON(tick)[[1]], queryName)
      )
    dfList<-c(dfList, list(simpleQueryResultToDataFrame(fromJSON(tick)[[1]], queryName)))
  i<-i+1
  }
  return(rbindlist(dfList))
}

dataFrameForRunIdAndSingleQuery <- function(runId, queryName){
  print(paste("Reading ", queryName, "RunId- ", runId))
  if(all.equal(queryName,"PowerPlantDispatchPlans")==TRUE){
    browser()}
  rawData <- readRawQueryOutput(runId, queryName)
  noTimeSteps <- numberOfTimeSteps(rawData)
  dataAsList<-fromJSON(rawData)
  #Filter out NULL values
  dataAsList<-dataAsList[!sapply(dataAsList, is.null)]
  dataAsMatrix<-sapply(dataAsList,as.matrix)
  #In case its double enveloped in a list, go one level deeper.
  doubleEnvelopedList=FALSE
  if(mode(dataAsMatrix[[1]])=="list"){
    dataAsMatrix<-sapply(dataAsMatrix,as.matrix)
    doubleEnvelopedList=TRUE
  }
  noSubVariable <- dim(dataAsMatrix)[2]
  #Replace null with 0
  dataAsMatrix[1,sapply(dataAsMatrix[1,], is.null)]<-"Unnamed"
  #dataAsMatrix[2,sapply(dataAsMatrix[2,], is.null)]<-0
  if(length(dataAsMatrix[1,])>1){
    names<-paste0(queryName,"-",dataAsMatrix[1,])
  } else{
    names<-queryName
  }
  values<-matrix(nrow=noTimeSteps, ncol=noSubVariable)
  i=1
  if(!doubleEnvelopedList){
    for(tick in rawData){
      dataAsList<-fromJSON(tick)
      #Filter out NULL values
      dataAsList<-dataAsList[!sapply(dataAsList, is.null)]
      dataAsMatrix<-sapply(dataAsList,as.matrix)
      dataAsMatrix[2,sapply(dataAsMatrix[2,], is.null)]<-0
      values[i,]<-sapply(dataAsMatrix[2,], as.vector)
      i = i+1
    }
  } else{
    for(tick in rawData){
      dataAsList<-fromJSON(tick)
      #Filter out NULL values
      dataAsList<-dataAsList[!sapply(dataAsList, is.null)]
      dataAsMatrix<-sapply(sapply(dataAsList ,as.matrix), as.matrix)
      dataAsMatrix[2,sapply(dataAsMatrix[2,], is.null)]<-0
      values[i,]<-sapply(dataAsMatrix[2,], as.vector)
      i = i+1
    }
  }
  colnames(values)<-names
  rm(dataAsList)
  rm(dataAsMatrix)
  rm(rawData)
  return(as.data.frame(values))
}

bigDataFrameForSeveralRunIdsAndQueries <- function(runIds, queryNames, jobname){
  if(length(runIds)<2 || length(queryNames)<2)
    stop("Need input with vectors longer than 1")
  #Create initial data frame
  df<-dataFrameForRunIdAndQueryNames(runIds[1], queryNames)
  df <- cbind(tick=c(0:(dim(df)[1]-1)), df, runId=rep.int(runIds[1],dim(df)[1]))
  write.table(df,file=paste0(jobname,".csv"))
  rm(df)
  #join the other dataframes to the query
  for(runId in runIds[-1]){
    df2<-dataFrameForRunIdAndQueryNames(runId, queryNames)
    df2 <- cbind(tick=c(0:(dim(df2)[1]-1)), df2, runId=rep.int(runId,dim(df2)[1]))
    df<-read.table(paste0(jobname,".csv"))
    #R solution
    #df<-rbind(df,df2)
    #Sqldf solution
    df3<-sqldf("select * from df union select * from df2")
    #df<-sqldf("select * from df union select * from df2, dbname = tempfile()")
    ##write.table(df2,file=paste0(jobname,".csv"), append=TRUE, col.names=FALSE)
    write.table(df3,file=paste0(jobname,".csv"))
    rm(df2)
    rm(df3)
    rm(df)
    gc()
  }
  #return(df)
}

getDataFrameForModelRunsInFolder <- function(outPutFolder, ignoredHeaders=""){
  modelRuns=list.dirs(outPutFolder)
  df<-getDataFrameForModelRun(outPutFolder, modelRuns[1], modelRuns[1], ignoredHeaders)
  for(modelRun in modelRuns[-1]){
    df<-rbind(df,getDataFrameForModelRun(outPutFolder, modelRun, modelRun, ignoredHeaders))
  }
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

list.dirs <- function(path=".", pattern=NULL, all.dirs=FALSE,
                      full.names=FALSE, ignore.case=FALSE) {
  
  all <- list.files(path, pattern, all.dirs,
                    full.names, recursive=FALSE, ignore.case)
  all[file.info(all)$isdir]
}

runIdsFromWorkingDirectory <- function(){
  return(sub("^(.*).log$","\\1",grep('.log',list.files(), value=TRUE)))
}

jobNameFromRunIds <- function(runIds){
  return(sub("(.*)-[0-9]*", "\\1", runIds[1]))
}

headersFromRunIds <- function(runIds){
  return(sub(paste0("^", paste0(runIds[1],"-"),"(.*)"),"\\1",grep(paste0(runIds[1],"-"), list.files(), value=TRUE)))
}
