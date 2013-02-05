library(ggplot2)
### SET DIR ###
#setwd("/home/ejlchappin/Documents/d13n/rscripts")
setwd("/home/joern/d13n/rscripts")
source("simulation.R")

reports <- "reports/"

getBidCurve <- function(data) {
    amounts <- c(0)
    prices <- c(0)
    amount <- 0
    for (dt in data$result) {
      for (bid in dt) {
        prices <- c(prices,bid$properties$price)
        amount <- amount + bid$properties$amount
        amounts <- c(amounts,amount)
      }
    }
    prices <- c(prices,0)
    amounts <- c(amounts,amount)
    return(as.data.frame(cbind(amounts,prices)))
}

getClearingPoint <-function(data) {
  for (dt in data$result) {
    for (point in dt) {
      return(c(point$properties$price, point$properties$volume))
    }
  }
}

getMax <- function(one, two) {
  if (max(one) > max(two)) {
    return(one)
  } else {
    return(two)
  }
}

getProducerCashFlows <- function() {
  pp <- querySimulation("EnergyProducer", "revenues = v.in('to').filter{it.money>0 && it.time == tick}.money.sum(); co2costs = v.in('from').filter{it.type==6 && it.time ==tick}.money.sum(); fuelcosts = v.in('from').filter{it.type==4 && it.time == tick}.money.sum(); loancosts = v.in('from').filter{it.type==7 && it.time == tick}.money.sum();omcosts = v.in('from').filter{it.type==3 && it.time == tick}.money.sum();[v.name, [['Revenue',revenues],['CO2 Costs',co2costs],['Fuel Costs',fuelcosts],['Capital Costs',loancosts], ['OM Costs',omcosts]]]")
  df<-NULL;
  for (dt in pp$result) {
    pname <- dt[1]
    for (ss in dt[[2]]) {
      rbind(df,c(pname, ss[[1]],ss[[2]]))->df
    }
  }
  colnames(df) <- c("producer", "cashflow", "amount")
  df <- as.data.frame(df, stringsAsFactors=TRUE)
  df$amount <- as.numeric(df$amount)
  df$producer <- unlist(df$producer)
  df$cashflow <- unlist(df$cashflow)
  return(df)
}
getProducerProfit <- function() {
  df <- getProducerCashFlows()
  revenue <- df[df$cashflow=='Revenue',]
  costs <- df[df$cashflow!='Revenue',]
  costs$amount[is.na(costs$amount)]<-0
  revenue$amount[is.na(revenue$amount)]<-0
  dd<-NULL;
  for (producer in unique(df$producer)) {
    totalCosts <- sum(costs[costs$producer==producer,]$amount)
    totalRevenue <- revenue[revenue$producer==producer,]$amount
    rbind(dd,c(producer, totalCosts,totalRevenue, round(totalRevenue-totalCosts)))->dd
  }
  colnames(dd) <- c("producer", "costs", "revenue","profit")
  dd <- as.data.frame(dd, stringsAsFactors=FALSE)
  dd$costs <- as.numeric(dd$costs)
  dd$revenue <- as.numeric(dd$revenue)
  dd$profit <- as.numeric(dd$profit)
  return(dd)
}

getPlantCashFlows <- function() {
  qq <- querySimulation("", "types = [1:'Power Revenues', 3:'OM Costs', 4:'Fuel Costs', 5:'CO2 Tax', 6:'CO2 Auction', 7:'Loan']; results = []; for (plant in n.getNodes('PowerPlant').findAll{((it.constructionStartTime + it.permittime + it.leadtime) < tick) && (it.dismantleTime > tick || it.dismantleTime == 0)}) { flows = plant.in('plant').filter{it.type>=0 && it.time == tick}.collect{it}.groupBy{it.type}.entrySet(); agg = []; flows.each{agg.add([types.get(it.key), it.value.money.sum()])};if (agg.size() > 0) results.add([plant.name + '[' + plant.id + ']', agg])}; results")
  df <- NULL
  for (dt in qq$result) {
    pname <- dt[1]
    for(dd in dt[2]) {
      for (de in dd) {
        flowname <- de[1]
        flowamount <- de[2]
        rbind(df,c(pname, flowname,flowamount))->df
      }
    }
  }
  colnames(df) <- c("plant", "cashflow", "amount")
  df <- as.data.frame(df, stringsAsFactors=TRUE)
  df$amount <- as.numeric(df$amount)
  df$plant <- unlist(df$plant)
  df$cashflow <- unlist(df$cashflow)
  return(df)
}

getPlantProfit <- function() {
  df <- getPlantCashFlows()
  revenue <- df[df$cashflow=='Power Revenues',]
  costs <- df[df$cashflow!='Power Revenues',]
  costs$amount[is.na(costs$amount)]<-0
  revenue$amount[is.na(revenue$amount)]<-0
  dd<-NULL;
  for (plant in unique(df$plant)) {
    totalCosts <- sum(costs[costs$plant==plant,]$amount)
    totalRevenue <- revenue[revenue$plant==plant,]$amount
    rbind(dd,c(plant, totalCosts,totalRevenue, round(totalRevenue-totalCosts)))->dd
  }
  colnames(dd) <- c("plant", "costs", "revenue","profit")
  dd <- as.data.frame(dd, stringsAsFactors=FALSE)
  dd$costs <- as.numeric(dd$costs)
  dd$revenue <- as.numeric(dd$revenue)
  dd$profit <- as.numeric(dd$profit)
  return(dd)
}

millionFormatter <-function(val) {
  return (val/1000000)
}

drawSupplyDemandForMarketSegment <- function(market, segment, tick) {
  chartName <- paste(reports,"drawSupplyDemandForMarketSegment",segment,"_",tick,".pdf",sep="")
  tryCatch({
    demandBids <- querySimulation(market, paste("v.in('segmentmarket').filter{it.segmentID==",segment,"}.back(2).in('market').filter{it.time==tick && it.supplyBid==false}.collect{it}.sort{it.price}.reverse()",sep=""))
    supplyBids <- querySimulation(market, paste("v.in('segmentmarket').filter{it.segmentID==",segment,"}.back(2).in('market').filter{it.time==tick && it.supplyBid==true}.collect{it}.sort{it.price}",sep=""))
    cpData<-querySimulation(market, paste("v.in('segmentmarket').filter{it.segmentID==",segment,"}.back(2).out('clearingpoint').filter{it.time==tick}",sep=""))
    cp <- getClearingPoint(cpData)
    supply <- getBidCurve(supplyBids)
    demand <- getBidCurve(demandBids)
    cpDf <- data.frame(amounts=supply$amounts,prices=supply$prices,clearedprice=cp[1],clearedvolume=cp[2])
    pdf(chartName)
    p <- ggplot()+geom_step(data=supply, aes(x=amounts, y=prices),direction = "vh", colour="blue") + geom_step(data=demand, aes(x=amounts, y=prices),direction = "vh",colour="red")
    p <- p + xlab("Amount") + ylab("Price")
    p <- p + geom_line(data=cpDf, aes(x = amounts, y = clearedprice), colour = "black",linetype = 2) + geom_line(data=cpDf, aes(x = clearedvolume, y = prices), colour = "black",linetype = 2)
    p <- p + opts(title=paste(market,"segment:",segment,"p:",round(cp[1],digits=2),"v:",round(cp[2],digits=0)))
    print(p)
    dev.off()
  }, error = function(ex) drawErrorGraph(chartName, ex))
}

drawSupplyDemandForMarketBySubstance <- function(market, substance, tick) {
  chartName <- paste(reports,"drawSupplyDemandForMarketBySubstance",gsub(" ", "_", substance),"_",tick,".pdf",sep="")
  tryCatch({
    demandBids <- querySimulation(market, paste("v.in('substancemarket').filter{it.name=='",substance,"'}.back(2).in('market').filter{it.time==tick && it.supplyBid==false}.collect{it}.sort{it.price}.reverse()",sep=""))
    supplyBids <- querySimulation(market, paste("v.in('substancemarket').filter{it.name=='",substance,"'}.back(2).in('market').filter{it.time==tick && it.supplyBid==true}.collect{it}.sort{it.price}",sep=""))
    cpData<-querySimulation(market, paste("v.in('substancemarket').filter{it.name=='",substance,"'}.back(2).out('clearingpoint').filter{it.time==tick}",sep=""))
    cp <- getClearingPoint(cpData)
    supply <- getBidCurve(supplyBids)
    demand <- getBidCurve(demandBids)
    cpDf <- data.frame(amounts=supply$amounts,prices=supply$prices,clearedprice=cp[1],clearedvolume=cp[2])
    pdf(chartName)
    p <- ggplot()+geom_step(data=supply, aes(x=amounts, y=prices),direction = "vh", colour="blue") + geom_step(data=demand, aes(x=amounts, y=prices),direction = "vh",colour="red")
    p <- p + xlab("Amount") + ylab("Price")
    p <- p + geom_line(data=cpDf, aes(x = amounts, y = clearedprice), colour = "black",linetype = 2) + geom_line(data=cpDf, aes(x = clearedvolume, y = prices), colour = "black",linetype = 2)
    p <- p + opts(title=paste(market,"substance:",substance,"p:",round(cp[1],digits=2),"v:",round(cp[2],digits=0))) + scale_x_log10() + scale_y_log10()
    print(p)
    dev.off()
  }, error = function(ex) drawErrorGraph(chartName, ex))
}
drawSupplyDemandForMarket <- function(market, tick) {
  chartName <- paste(reports,"drawSupplyDemandForMarket",market,"_",tick,".pdf",sep="")
  tryCatch({
    demandBids <- querySimulation(market, paste("v.in('market').filter{it.time==tick && it.supplyBid==false}.collect{it}.sort{it.price}.reverse()",sep=""))
    supplyBids <- querySimulation(market, paste("v.in('market').filter{it.time==tick && it.supplyBid==true}.collect{it}.sort{it.price}",sep=""))
    cpData<-querySimulation(market, paste("v.out('clearingpoint').filter{it.time==tick}",sep=""))
    cp <- getClearingPoint(cpData)
    supply <- getBidCurve(supplyBids)
    demand <- getBidCurve(demandBids)
    cpDf <- data.frame(amounts=supply$amounts,prices=supply$prices,clearedprice=cp[1],clearedvolume=cp[2])
    pdf(chartName)
    p <- ggplot()+geom_step(data=supply, aes(x=amounts, y=prices),direction = "vh", colour="blue") + geom_step(data=demand, aes(x=amounts, y=prices),direction = "vh",colour="red")
    p <- p + xlab("Amount") + ylab("Price")
    p <- p + geom_line(data=cpDf, aes(x = amounts, y = clearedprice), colour = "black",linetype = 2) + geom_line(data=cpDf, aes(x = clearedvolume, y = prices), colour = "black",linetype = 2)
    p <- p + opts(title=paste(market,"p:",round(cp[1],digits=2),"v:",round(cp[2],digits=0))) + scale_x_log10() + scale_y_log10()
    print(p)
    dev.off()
  }, error = function(ex) drawErrorGraph(chartName, ex))
}

drawCapacities <- function(tick) {
  chartName <- paste(reports,"drawCapacities","_",tick,".pdf",sep="")
  tryCatch({
    capacityByTech <- querySimulation("PowerGeneratingTechnology", "[v.name, v.in('TECHNOLOGY').filter{((it.constructionStartTime + it.actualPermittime + it.actualLeadtime) < tick) && (it.dismantleTime > tick || it.dismantleTime == 0)}.out('TECHNOLOGY').capacity.sum()]")
    capacities <- c()
    techs <- c()
    for (tech in capacityByTech$result) {
      if (!is.null(tech[[2]]) && (tech[[2]]>0)) {
        capacities <- c(capacities,tech[[2]])
        techs <- c(techs,tech[[1]])
      }
    }
    pct <- round(capacities/sum(capacities)*100)
    techs <- paste(techs, pct) # add percents to labels 
    techs <- paste(techs,"%",sep="") # ad % to labels 
    if (length(capacities) > 0 && capacities >= 0) {
      pdf(chartName)
      pie(capacities, labels = techs, main="Capacities by Technology")
      dev.off()
    } else {
      drawErrorGraph(chartName, capacities)
    }
  }, error = function(ex) drawErrorGraph(chartName, ex))
}

drawGeneration <- function(tick) {
  chartName <- paste(reports,"drawGeneration","_",tick,".pdf",sep="")
  tryCatch({
    capacityByTech <- querySimulation("PowerGeneratingTechnology", "[v.name, v.in('TECHNOLOGY').in('POWERPLANT_DISPATCHPLAN').filter{it.time==tick && it.status>=2}.acceptedAmount.sum(0)]")
    capacities <- c()
    techs <- c()
    for (tech in capacityByTech$result) {
      if (!is.null(tech[[2]]) && (tech[[2]]>0)) {
        capacities <- c(capacities,tech[[2]])
        techs <- c(techs,tech[[1]])
      }
    }
    pct <- round(capacities/sum(capacities)*100)
    techs <- paste(techs, pct) # add percents to labels 
    techs <- paste(techs,"%",sep="") # ad % to labels 
    pdf(chartName)
    pie(capacities, labels = techs, main="Generation by Technology")
    dev.off()
  }, error = function(ex) drawErrorGraph(chartName, ex))
}

drawBidPrices <- function(tick) {
  chartName <- paste(reports,"drawBidPrices","_",tick,".pdf",sep="")
  tryCatch({
    dt <- querySimulation("PowerGeneratingTechnology", "list = v.in('technology').in('plant').filter{it.time==tick && it.status>=2 && it.supplyBid==true}.price.toList(); avgPrice = list.size() > 0 ? list.sum() / list.size() : null; [v.name, avgPrice]")
    prices <- c()
    techs <- c()
    for (tech in dt$result) {
      if (!is.null(tech[[2]])) {
        prices <- c(prices,tech[[2]])
        techs <- c(techs,tech[[1]])
      }
    }
    pdf(chartName)
    barplot(prices, main="Average Bid Prices by Technology ", names.arg=techs, cex.names=0.8)
    dev.off()
  }, error = function(ex) drawErrorGraph(chartName, ex))
}

drawProducerRevenue <- function(tick) {
  chartName <- paste(reports,"drawProducerRevenue","_",tick,".pdf",sep="")
  tryCatch({
    df <- getProducerCashFlows()
    revenue <- df[df$cashflow=='Revenue',]
    revenue$amount[is.na(revenue$amount)]<-0
    pdf(chartName)
    p <- ggplot(data=revenue, aes(x=factor(""), y=millionFormatter(amount), fill=factor(cashflow))) + geom_bar(width = 1) +  xlab("") + ylab("Revenue (mEUR)")+ facet_grid(~ producer) + opts(strip.text.x = theme_text(size=8, angle=90))
    print(p)
    dev.off()
  }, error = function(ex) drawErrorGraph(chartName, ex))
}

drawProducerCosts <- function(tick) {
  chartName <- paste(reports,"drawProducerCosts","_",tick,".pdf",sep="")
  tryCatch({
    df <- getProducerCashFlows()
    costs <- df[df$cashflow!='Revenue',]
    costs$amount[is.na(costs$amount)]<-0
    pdf(chartName)
    p <- ggplot(data=costs, aes(x=factor(""), y=millionFormatter(amount), fill=factor(cashflow))) + geom_bar(width = 1) +  xlab("") + ylab("Costs (mEUR)")  + facet_grid(~ producer) + opts(strip.text.x = theme_text(size=8, angle=90))
    print(p)
    dev.off()
  }, error = function(ex) drawErrorGraph(chartName, ex))
}

drawProducerProfit <- function(tick) {
  chartName <- paste(reports,"drawProducerProfit","_",tick,".pdf",sep="")
  tryCatch({
    df <- getProducerProfit()
    pdf(chartName)
    p <- ggplot(data=df, aes(x=factor(""), y=millionFormatter(profit))) + geom_bar(width = 1) +  xlab("") + ylab("Profit (mEUR)")+ facet_grid(~ producer) + opts(strip.text.x = theme_text(size=8, angle=90))
    print(p)
    dev.off()
  }, error = function(ex) drawErrorGraph(chartName, ex))
}

drawPlantCosts <- function(tick) {
  chartName <- paste(reports,"drawPlantCosts","_",tick,".pdf",sep="")
  tryCatch({
    df <- getPlantCashFlows()
    costs <- df[df$cashflow!='Power Revenues',]
    costs$amount[is.na(costs$amount)]<-0
    pdf(chartName)
    p <- ggplot(data=costs, aes(x=factor(""), y=millionFormatter(amount), fill=factor(cashflow))) + geom_bar(width = 1) +  xlab("") + ylab("Costs (mEUR)")+ facet_grid(~ plant) + opts(strip.text.x = theme_text(size=8, angle=90))
    print(p)
    dev.off()
  }, error = function(ex) drawErrorGraph(chartName, ex))
}

drawPlantRevenue <- function(tick) {
  chartName <- paste(reports,"drawPlantRevenue","_",tick,".pdf",sep="")
  tryCatch({
    df <- getPlantCashFlows()
    revenue <- df[df$cashflow=='Power Revenues',]
    revenue$amount[is.na(revenue$amount)]<-0
    pdf(chartName)
    p <- ggplot(data=revenue, aes(x=factor(""), y=millionFormatter(amount), fill=factor(cashflow))) + geom_bar(width = 1) +  xlab("") + ylab("Revenue (mEUR)")+ facet_grid(~ plant) + opts(strip.text.x = theme_text(size=8, angle=90))
    print(p)
    dev.off()
  }, error = function(ex) drawErrorGraph(chartName, ex))
}

drawPlantProfit <- function(tick) {
  chartName <- paste(reports,"drawPlantProfit","_",tick,".pdf",sep="")
  tryCatch({
    df <- getPlantProfit()
    pdf(chartName)
    p <- ggplot(data=df, aes(x=factor(""), y=millionFormatter(profit))) + geom_bar(width = 1) +  xlab("") + ylab("Profit (mEUR)")+ facet_grid(~ plant) + opts(strip.text.x = theme_text(size=8, angle=90))
    print(p)
    dev.off()
  }, error = function(ex) drawErrorGraph(chartName, ex))
}

drawErrorGraph <- function(name,ex) {
  pdf(name)
  plot(0:1, 0:1, type= "n", xlab="", ylab="")
  text(0.5,0.5, paste("Error:",ex), cex=1)
  dev.off()
  fileConn <- file("error.log")
  writeLines(paste("Error in:",name,ex), fileConn)
  close(fileConn)
}
################## 
##################

##stacked area
#p <- ggplot(df, aes( DomAreaByCat, PopDen)) + geom_area(aes(colour = PR_Cat, fill= PR_Cat), position = 'stack')
### pie
#ggplot(data=df[df$gender=='Male',], aes(x=factor(1), y=Summary, fill = factor(response))) + geom_bar(width = 1) + coord_polar(theta="y") +
# xlab('Males') +
# ylab('') +
# labs(fill='Response')
#####################
