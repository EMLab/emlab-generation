user<-
emlabGenerationFolder<-paste("/home/",user,"/emlab-generation/", sep="")
rscriptFolder<-paste(emlabGenerationFolder,"rscripts/", sep="")
resultFolder<-paste("/home/",user,"/Desktop/emlabGen/output/", sep="")
analysisFolder<-paste("/home/", user, "/Desktop/emlabGen/analysis/", sep="")
queryFile<-paste(emlabGenerationFolder,"emlab-generation/","queries.properties", sep="")
agentSpringReader<-paste(rscriptFolder,"AgentSpringHeadlessReader.R")
#agentSpringReader<-paste(rscriptFolder,"AgentSpringHeadlessReaderPureR.R")

setwd(rscriptFolder)