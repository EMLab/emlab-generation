library(rjson)
library(RCurl)
options(warn=-1)
### FUNCTIONS ###
startSimulation <- function() {
  return(fromJSON(file="http://localhost:8080/agentspring-face/engine/start"))
}
stopSimulation <- function() {
  return(fromJSON(file="http://localhost:8080/agentspring-face/engine/stop"))
}
pauseSimulation <- function() {
  return(fromJSON(file="http://localhost:8080/agentspring-face/engine/pause"))
}
resumeSimulation <- function() {
  return(fromJSON(file="http://localhost:8080/agentspring-face/engine/resume"))
}
statusSimulation <- function(){
  return(fromJSON(file="http://localhost:8080/agentspring-face/engine/status"))
}
isPausedSimulation <- function() {
  status <- fromJSON(file="http://localhost:8080/agentspring-face/engine/status")
  return(status$state == "PAUSED")
}
waitPauseSimulation <- function() {
  pauseSimulation()
  while (!isPausedSimulation()) {
    Sys.sleep(5)
  }
  return(TRUE)
}
tickSimulation <- function() {
  status <- statusSimulation()
  return(status$tick)
}
querySimulation <- function(start, query) {
  urlStr <- paste("http://localhost:8080/agentspring-face/db/query?start=",start,"&query=",curlEscape(query),sep="")
  return(fromJSON(file=urlStr))
}
runSimulation <- function(x, ticks) {
  startSimulation()
  tick <- 0 
  while (tick < ticks) {
    waitPauseSimulation()
    tick <- tickSimulation()
    x(tick)
    resumeSimulation()
    tick <- tick + 1
  }
  st <- stopSimulation()
}

### END FUNCTIONS ###


