#install.packages("ggplot2",dependencies=TRUE)
#install.packages("rjson",dependencies=TRUE)
#install.packages("RCurl",dependencies=TRUE)

library(ggplot2)
### SET DIR ###
#setwd("/home/ejlchappin/Documents/d13n/rscripts")
setwd("/home/joern/d13n/rscripts")
source("simulation.R")

createReport <-function(tick) {
  #for (segment in 1:5) {
  #  drawSupplyDemandForMarketSegment("ElectricitySpotMarket",segment)
  #}
  #drawSupplyDemandForMarketBySubstance("CommodityMarket","Natural Gas")
  #drawSupplyDemandForMarketBySubstance("CommodityMarket","Coal")
  #drawSupplyDemandForMarketBySubstance("CommodityMarket","Biomass")
  #drawSupplyDemandForMarket("CO2Auction")
  drawCapacities()
  #drawGeneration()
  #drawBidPrices()
  #drawProducerCosts()
  #drawProducerRevenue()
  #drawProducerProfit()
}

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

drawSupplyDemandForMarketSegment <- function(market, segment) {
  demandBids <- querySimulation(market, paste("v.in('segmentmarket').filter{it.segmentID==",segment,"}.back(2).in('market').filter{it.time==tick && it.supplyBid==false}.collect{it}.sort{it.price}.reverse()",sep=""))
  supplyBids <- querySimulation(market, paste("v.in('segmentmarket').filter{it.segmentID==",segment,"}.back(2).in('market').filter{it.time==tick && it.supplyBid==true}.collect{it}.sort{it.price}",sep=""))
  cpData<-querySimulation(market, paste("v.in('segmentmarket').filter{it.segmentID==",segment,"}.back(2).out('clearingpoint').filter{it.time==tick}",sep=""))
  cp <- getClearingPoint(cpData)
  supply <- getBidCurve(supplyBids)
  demand <- getBidCurve(demandBids)
  cpDf <- data.frame(amounts=supply$amounts,prices=supply$prices,clearedprice=cp[1],clearedvolume=cp[2])
  p <- ggplot()+geom_step(data=supply, aes(x=amounts, y=prices),direction = "vh", colour="blue") + geom_step(data=demand, aes(x=amounts, y=prices),direction = "vh",colour="red")
  p <- p + xlab("Amount") + ylab("Price")
  p <- p + geom_line(data=cpDf, aes(x = amounts, y = clearedprice), colour = "black",linetype = 2) + geom_line(data=cpDf, aes(x = clearedvolume, y = prices), colour = "black",linetype = 2)
  p <- p + opts(title=paste(market,"segment:",segment,"p:",round(cp[1],digits=2),"v:",round(cp[2],digits=0)))
  print(p)
}

drawSupplyDemandForMarketBySubstance <- function(market, substance) {
  demandBids <- querySimulation(market, paste("v.in('substancemarket').filter{it.name=='",substance,"'}.back(2).in('market').filter{it.time==tick && it.supplyBid==false}.collect{it}.sort{it.price}.reverse()",sep=""))
  supplyBids <- querySimulation(market, paste("v.in('substancemarket').filter{it.name=='",substance,"'}.back(2).in('market').filter{it.time==tick && it.supplyBid==true}.collect{it}.sort{it.price}",sep=""))
  cpData<-querySimulation(market, paste("v.in('substancemarket').filter{it.name=='",substance,"'}.back(2).out('clearingpoint').filter{it.time==tick}",sep=""))
  cp <- getClearingPoint(cpData)
  supply <- getBidCurve(supplyBids)
  demand <- getBidCurve(demandBids)
  cpDf <- data.frame(amounts=supply$amounts,prices=supply$prices,clearedprice=cp[1],clearedvolume=cp[2])
  p <- ggplot()+geom_step(data=supply, aes(x=amounts, y=prices),direction = "vh", colour="blue") + geom_step(data=demand, aes(x=amounts, y=prices),direction = "vh",colour="red")
  p <- p + xlab("Amount") + ylab("Price")
  p <- p + geom_line(data=cpDf, aes(x = amounts, y = clearedprice), colour = "black",linetype = 2) + geom_line(data=cpDf, aes(x = clearedvolume, y = prices), colour = "black",linetype = 2)
  p <- p + opts(title=paste(market,"substance:",substance,"p:",round(cp[1],digits=2),"v:",round(cp[2],digits=0))) + scale_x_log10() + scale_y_log10()
  print(p)
}
drawSupplyDemandForMarket <- function(market) {
  demandBids <- querySimulation(market, paste("v.in('market').filter{it.time==tick && it.supplyBid==false}.collect{it}.sort{it.price}.reverse()",sep=""))
  supplyBids <- querySimulation(market, paste("v.in('market').filter{it.time==tick && it.supplyBid==true}.collect{it}.sort{it.price}",sep=""))
  cpData<-querySimulation(market, paste("v.out('clearingpoint').filter{it.time==tick}",sep=""))
  cp <- getClearingPoint(cpData)
  supply <- getBidCurve(supplyBids)
  demand <- getBidCurve(demandBids)
  cpDf <- data.frame(amounts=supply$amounts,prices=supply$prices,clearedprice=cp[1],clearedvolume=cp[2])
  p <- ggplot()+geom_step(data=supply, aes(x=amounts, y=prices),direction = "vh", colour="blue") + geom_step(data=demand, aes(x=amounts, y=prices),direction = "vh",colour="red")
  p <- p + xlab("Amount") + ylab("Price")
  p <- p + geom_line(data=cpDf, aes(x = amounts, y = clearedprice), colour = "black",linetype = 2) + geom_line(data=cpDf, aes(x = clearedvolume, y = prices), colour = "black",linetype = 2)
  p <- p + opts(title=paste(market,"p:",round(cp[1],digits=2),"v:",round(cp[2],digits=0))) + scale_x_log10() + scale_y_log10()
  print(p)
}

drawCapacities <- function() {
  capacityByTech <- querySimulation("PowerGeneratingTechnology", "[v.name, v.in('TECHNOLOGY').filter{((it.constructionStartTime + it.actualPermittime + it.actualLeadtime) < tick) && (it.dismantleTime > tick || it.dismantleTime == 0)}.out('TECHNOLOGY').capacity.sum()]")
  capacities <- c()
  techs <- c()
  for (tech in capacityByTech$result) {
    if (!is.null(tech[[2]])) {
      capacities <- c(capacities,tech[[2]])
      techs <- c(techs,tech[[1]])
    }
  }
  pct <- round(capacities/sum(capacities)*100)
  techs <- paste(techs, pct) # add percents to labels 
  techs <- paste(techs,"%",sep="") # ad % to labels 
  pie(capacities, labels = techs, main="Capacities by Technology")
}

drawGeneration <- function() {
  capacityByTech <- querySimulation("PowerGeneratingTechnology", "[v.name, v.in('technology').in('plant').filter{it.time==tick && it.status>=2 && it.supplyBid==true}.acceptedAmount.sum()]")
  capacities <- c()
  techs <- c()
  for (tech in capacityByTech$result) {
    if (!is.null(tech[[2]])) {
      capacities <- c(capacities,tech[[2]])
      techs <- c(techs,tech[[1]])
    }
  }
  pct <- round(capacities/sum(capacities)*100)
  techs <- paste(techs, pct) # add percents to labels 
  techs <- paste(techs,"%",sep="") # ad % to labels 
  pie(capacities, labels = techs, main="Generation by Technology")
}

drawBidPrices <- function() {
  dt <- querySimulation("PowerGeneratingTechnology", "list = v.in('technology').in('plant').filter{it.time==tick && it.status>=2 && it.supplyBid==true}.price.toList(); avgPrice = list.size() > 0 ? list.sum() / list.size() : null; [v.name, avgPrice]")
  prices <- c()
  techs <- c()
  for (tech in dt$result) {
    if (!is.null(tech[[2]])) {
      prices <- c(prices,tech[[2]])
      techs <- c(techs,tech[[1]])
    }
  }
  barplot(prices, main="Average Bid Prices by Technology ", names.arg=techs, cex.names=0.8)
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
  dd<-NULL;
  for (producer in unique(df$producer)) {
    totalCosts <- sum(costs[costs$producer==producer,]$amount)
    totalRevenue <- revenue[revenue$producer==producer,]$amount
    rbind(dd,c(producer, totalCosts,totalRevenue, round(totalRevenue-totalCosts)))->dd
  }
  colnames(dd) <- c("producer", "costs", "revenue","profit")
  return(as.data.frame(dd))
}

drawProducerRevenue <- function() {
  df <- getProducerCashFlows()
  revenue <- df[df$cashflow=='Revenue',]
  p <- ggplot(data=revenue, aes(x=factor(""), y=amount, fill=factor(cashflow))) + geom_bar(width = 1) +  xlab("") + ylab("Revenue")+ facet_grid(~ producer)
  print(p)
}

drawProducerCosts <- function() {
  df <- getProducerCashFlows()
  costs <- df[df$cashflow!='Revenue',]
  p <- ggplot(data=costs, aes(x=factor(""), y=amount, fill=factor(cashflow))) + geom_bar(width = 1) +  xlab("") + ylab("Revenue")+ facet_grid(~ producer)
  print(p)
}
drawProducerProfit <- function() {
  df <- getProducerProfit()
  p <- ggplot(data=df, aes(x=factor(""), y=profit)) + geom_bar(width = 1) +  xlab("") + ylab("Profit")+ facet_grid(~ producer)
  print(p)
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



