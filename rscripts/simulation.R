library(rjson)
library(RCurl)
library(plyr)
source("rConfig.R")
source("AgentSpringQueryReader.R")
options(warn=-1)
### FUNCTIONS ###
# start simulation
startSimulation <- function() {
  return(fromJSON(file="http://localhost:8080/agentspring-face/engine/start"))
}
# stop 
stopSimulation <- function() {
  return(fromJSON(file="http://localhost:8080/agentspring-face/engine/stop"))
}
# pause
pauseSimulation <- function() {
  return(fromJSON(file="http://localhost:8080/agentspring-face/engine/pause"))
}
# resume
resumeSimulation <- function() {
  return(fromJSON(file="http://localhost:8080/agentspring-face/engine/resume"))
}
# get status
statusSimulation <- function(){
  return(fromJSON(file="http://localhost:8080/agentspring-face/engine/status"))
}
# check if paused
isPausedSimulation <- function() {
  status <- fromJSON(file="http://localhost:8080/agentspring-face/engine/status")
  return(status$state == "PAUSED")
}

# check if stopped
isStoppedSimulation <- function() {
  status <- fromJSON(file="http://localhost:8080/agentspring-face/engine/status")
  return(status$state == "STOPPED")
}

# pause and only return when paused (simulation is only paused when the current tick is over - can take a while in some cases)
waitPauseSimulation <- function() {
  pauseSimulation()
  while (!isPausedSimulation()) {
    Sys.sleep(5)
  }
  return(TRUE)
}

# stop and only return when stopped (simulation is only stopped when the current tick is over - can take a while in some cases)
waitStopSimulation <- function() {
  stopSimulation()
  while (!isStoppedSimulation()) {
    Sys.sleep(5)
  }
  return(TRUE)
}

# get tick
tickSimulation <- function() {
  status <- statusSimulation()
  return(status$tick)
}

# query simulation - returns JSON
querySimulation <- function(start, query) {
  urlStr <- paste("http://localhost:8080/agentspring-face/db/query?start=",start,"&query=",curlEscape(query),sep="")
  return(fromJSON(file=urlStr))
}

#load scenario
loadScenario <- function(scenario) {
  curlPerform(url="http://localhost:8080/agentspring-face/engine/load", postfields=paste("scenario",scenario,sep="="), post = 1L)
}

#change the value of a parameter (object is your bean id)
changeParameter <- function(object, field, value) {
  data <- paste(paste("id",object,sep="="),paste("field",field,sep="="),paste("value",value,sep="="),sep="&")
  print(data)
  curlPerform(url="http://localhost:8080/agentspring-face/parameters/saveone", postfields=data, post = 1L)
}

readQueriesFromFile <- function(file){
  return(read.table(file, sep=",", strip.white=TRUE))
}

queryNumberToDataFrame <- function(number, queries){
  queryResult<-querySimulation(queries[number,2],queries[number,3])$result
  if(!is.null(queryResult))
    simpleQueryResultToDataFrame(queryResult, queries[number,1])
}

saveQueriesToDataFrameList <- function(tick, listOfDataFrames, queries){
  df<-data.frame(tick)
  colnames(df)<-"tick"
  for (i in seq(1,length(queries[[1]]))){
    if(grepl("^(?!TABLE).*$", queries[i,1], perl = TRUE)){
      tmpDF <- queryNumberToDataFrame(i, queries)
      if(!is.null(tmpDF))
        df<-cbind(df, tmpDF)
    } else{
      
      splitQueryName=strsplit(as.character(queries[i,1]), "_")
      if(tick>0){
        listOfDataFrames[[splitQueryName[[1]][2]]]<-rbind(listOfDataFrames[[splitQueryName[[1]][2]]],tableQueryResultToDataFrame(querySimulation(queries[i,2],queries[i,3])$result, splitQueryName))
      }else{
        listOfDataFrames[[splitQueryName[[1]][2]]]<-tableQueryResultToDataFrame(querySimulation(queries[i,2],queries[i,3])$result, splitQueryName)
      }
    }
  }
  if(tick>0){
    listOfDataFrames[["simpleQueriesDF"]]<-rbind(listOfDataFrames[["simpleQueriesDF"]],df)
  } else{
    listOfDataFrames[["simpleQueriesDF"]]<-df
  }
  return(listOfDataFrames)
}



runSimulation <- function(x, ticks, run, dryRunFile=NULL, ...) {
  ## simulation runner - runs the simulation for 'tick' number of ticks and 
  ## executes the 'x(tick, result, ...)' function after the tick is finished
  ## Can also be run with an earlier saved results file, to produce new pdf plots, 
  ## using old data. For this a "dryRunFile" in the RData format needs to be supplied
  if(is.null(dryRunFile)){
    queries<-readQueriesFromFile(queryFile)
    result<-list(run)
    names(result)<-c("RunName")
    startSimulation()
  } else{
    load(dryRunFile)
  }
  tick <- 0
  while (tick < ticks) {
    if(is.null(dryRunFile)){
      waitPauseSimulation()
      tick <- tickSimulation()
      result <- try(saveQueriesToDataFrameList(tick, result, queries));
      if(class(result) == "try-error") {
        resumeSimulation()
        waitStopSimulation()
        print(paste("error at tick",tick))
        return(result)
      }
    }
    x(tick,result,...)
    #print(paste("finished tick",tick))
    if(is.null(dryRunFile))
      resumeSimulation()
    tick <- tick + 1
  }
  if(is.null(dryRunFile)){
    st <- waitStopSimulation()
    save(result, ascii=TRUE, file=paste(run,".RData", sep=""))
  }
  return(result)
}

### END FUNCTIONS ###

