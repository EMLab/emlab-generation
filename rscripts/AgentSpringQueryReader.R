simpleQueryResultToDataFrame <- function(queryResult, queryName){
  #Differentiate between queries of [Key, Value]-Pairs, and single results
  if(is.recursive(queryResult)){
    if(is.recursive(queryResult[[1]][[1]]))
      queryResult = queryResult[[1]]
    tmpDF<-data.frame(do.call(rbind, queryResult)[,2])
  } else{
    tmpDF<-data.frame(queryResult)
  }
  if(length(tmpDF) > 1)
    colnames(tmpDF)<-unlist(lapply(paste(queryName,"_", sep=""), paste, do.call(rbind, queryResult)[,1], sep=""))
  else
    colnames(tmpDF)<-queryName
  rm(queryResult)
  return(tmpDF)
}

tableQueryResultToDataFrame <- function(queryResult, header){
  if(length(queryResult)<2)
    queryResult<-queryResult[[1]]
  if(length(header[[1]])<3){
    df<-as.data.frame(t(sapply(queryResult[-1],rbind)), stringsAsFactors=TRUE)
    colnames(df)<-rbind(queryResult[[1]])
  } else{
    df<-as.data.frame(t(sapply(queryResult,rbind)), stringsAsFactors=TRUE)
    colnames(df)<-unlist(header)[3:length(header[[1]])]
  }
  df<-colwise(unlist)(df)
  return(df)
}