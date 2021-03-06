# 
# This plot shows for 4x4 Hex the results for different alpha. No big differences.
# For EvalMode=10 (EvalT), alpha=0.2 is slightly better
# 
library(ggplot2)
library(grid)
source("summarySE.R")

path <- "../../agents/Hex/04/csv/"; limits=c(0.0,1.0); errWidth=3000;

# 25 runs, epsilon 1.3 --> 0, lambda=0, 20 5-tuples. "-01" means 'Choose Start 01' checked.
# 'Learn from RM' not checked. Eval_Q=0, Eval_T=10.
# Files w/o "_3P" are with VER_3P=false
filenames=c( "multiTrain-noLearnFromRM-01-al099.csv"
            ,"multiTrain-noLearnFromRM-01-al050.csv"
            ,"multiTrain-noLearnFromRM-01-al050_3P-OLD.csv"
            ,"multiTrain-noLearnFromRM-01-al020.csv"
            ) 
PLOTALLLINES=F    # if =T: make a plot for each filename, with one line for each run
  
dfBoth = data.frame()
for (k in 1:length(filenames)) {
  filename <- paste0(path,filenames[k])
  df <- read.csv(file=filename, na.string="-1", dec=".",skip=2)
  df$run <- as.factor(df$run)
  # we remove here two columns since the older multiTrain files do not have it:
  df <- df[,setdiff(names(df),c("actionNum","trnMoves"))]
  
  if (PLOTALLLINES) {
    q <- ggplot(df,aes(x=gameNum,y=evalQ))
    q <- q+geom_line(aes(colour=run),size=1.0) + 
      scale_y_continuous(limits=c(-1,0)) #+
    #facet_grid(.~THETA, labeller = label_both)
    plot(q)
  }
  
  alphaCol = switch(k
                    ,rep("0.99",nrow(df))
                    ,rep("0.5",nrow(df))
                    ,rep("0.5 3P-OLD",nrow(df))
                    ,rep("0.2",nrow(df))
                    )
  dfBoth <- rbind(dfBoth,cbind(df,alpha=alphaCol))
                  
}

tgc <- data.frame()
# summarySE is a very useful script from www.cookbook-r.com/Graphs/Plotting_means_and_error_bars_(ggplot2)
# It summarizes a dataset, by grouping measurevar according to groupvars and calculating
# its mean, sd, se (standard dev of the mean), ci (conf.interval) and count N.
tgc1 <- summarySE(dfBoth, measurevar="evalQ", groupvars=c("gameNum","alpha"))
tgc1 <- cbind(tgc1,evalMode=rep(0,nrow(tgc1)))
names(tgc1)[4] <- "eval"  # rename "evalQ"
tgc2 <- summarySE(dfBoth, measurevar="evalT", groupvars=c("gameNum","alpha"))
tgc2 <- cbind(tgc2,evalMode=rep(10,nrow(tgc1)))
names(tgc2)[4] <- "eval"  # rename "evalT"
#tgc <- rbind(tgc1,tgc2)
tgc <- tgc2
tgc$alpha <- as.factor(tgc$alpha)
tgc$evalMode <- as.factor(tgc$evalMode)

# The errorbars may overlap, so use position_dodge to move them horizontally
pd <- position_dodge(1000) # move them 300 to the left and right

q <- ggplot(tgc,aes(x=gameNum,y=eval,colour=alpha,linetype=evalMode))
q <- q+geom_errorbar(aes(ymin=eval-se, ymax=eval+se), width=errWidth, position=pd)
q <- q+geom_line(position=pd,size=1.0) + geom_point(position=pd,size=2.0) 
q <- q+scale_y_continuous(limits=limits) 
#q <- q+guides(colour = guide_legend(reverse = TRUE))
plot(q)

