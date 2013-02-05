library("rjson")

args <- commandArgs(trailingOnly = TRUE)
names <- character(0)
valus <- character(0)
for (arg in args) {
    name <- strsplit(arg, "=")[[1]][1]
    valu <- strsplit(arg, "=")[[1]][2]
    names <- append(names, name)
    valus <- append(valus, valu)
}
parameters <- valus
names(parameters) <- names

if (is.na(parameters["file"])) {
    print("Please provide a data file name in the form file=filename")
    q()
}

# get the file
json_file <- parameters["file"]
# fix for json
json_string <- paste(readLines(json_file), collapse="")
json_string <- paste("[", substr(json_string, 1, nchar(json_string) - 1), "]")

#print(json_string)
#read json
json <- fromJSON(json_string)

# figure out number of runs (= split by tab)
line <- readLines(json_file, n=1)
no_runs <- length(strsplit(line, "\\t")[[1]])

# figure out number of series
no_series <- length(json[[1]])

# figure out names of series
names_series <- character(0)
for (serie in json[[1]]) {
    names_series <- append(names_series, serie[[1]])
}

# number of ticks
no_ticks <- length(json) / no_runs

# cap ticks at 50
no_ticks <- min(c(no_ticks, 50))

# min, max, mean X tick X serie
results <- array(0, c(no_ticks, no_series, 3))

for (s in 1:no_series) {
    for (t in 1:no_ticks) {
        # store series data
        dt <- numeric(0)
        for (r in 1:no_runs) {
            i <- ((t - 1) * no_runs) + r
            dt <- append(dt, ifelse(
                                is.na(
                                    as.numeric(json[[i]][[s]][[2]])
                                ), 
                                0, 
                                as.numeric(json[[i]][[s]][[2]])
                            )
                        )       
        }
        
        avg <- mean(dt)
        std <- sd(dt)
        mn <- avg - std
        mx <- avg + std
        if (!is.na(avg)) {
            results[t,s,1] <- avg
        }   
        if (!is.na(mn)) {
            results[t,s,2] <- mn
        }
        if (!is.na(mx)) {
            results[t,s,3] <- mx
        }
    }
}


# x axis - number of ticks
xx <- c(1:no_ticks)

# set white background
par(bg="white") 

#set color array
colors <- c("#2F4F4F","#8B7D6B", "#66CD00", "#CD5B45" ,"#00CDCD", "#8A2BE2", "#8B2323", "#8B7355", "#53868B", "#458B00", "#8B4513", "#6495ED", "#8B8878", "#DC143C", "#00008B", "#006400", "#2F4F4F", "#8B7500", "#8B864E")
#set opacity level
tr_level <- "22"

#set mode jpeg
jpeg(paste(json_file, ".jpg", sep=""), width=800, height=480, quality=100)
par(mar=c(3,4,3,15))   
plot(xx, type="l", xlab="Tick", ylab=parameters["ylab"], ylim=c(min(results), max(results))) 

for (s in 1:no_series) {
    par(lwd="2")

    # draw average
    lines(xx, results[,s,1], col=colors[s])

    par(lwd="1")
    
    # draw min / max
    lines(xx, results[,s,2], col=colors[s])
    lines(xx, results[,s,3], col=colors[s])
    tr_color <- paste(colors[s], tr_level, sep="")
    polygon(c(1:no_ticks,no_ticks:1), c(results[,s,2],rev(results[,s,3])), col=tr_color)
}

title(parameters["title"]) 
#legend("bottomright", "(x,y)", pch=15, legend=names_series, col=colors)
par(xpd=NA)
tmp.u <- par('usr')
legend(tmp.u[2] + 0.2, tmp.u[3], xjust=0, yjust=0, pch=15, legend=names_series, col=colors)

dev.off()
q()




