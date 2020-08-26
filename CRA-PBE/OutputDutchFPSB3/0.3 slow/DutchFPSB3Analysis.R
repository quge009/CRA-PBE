setwd("C:/Users/Vinzenz/eclipse-workspace/OutputDutchFPSB3")
rm(list = ls())
library(plotly)
options(warn=-1)

source("setup.txt")

round1Files<-list.files(path="Round1/")
round2Files<-list.files(path="Round2/")
data<-NULL
it=0
nIter=length(round1Files)-1

#bid data

for (path in round1Files){
    temp<-scan(file=paste("Round1/",path,sep=""),what = character(),sep = "")
    sole<-as.numeric(temp[4:(length(temp)/2)])
    split<-as.numeric(temp[(length(temp)/2+4):length(temp)])
    epsilon<-as.numeric(temp[2])
    n1=length(sole)/2
    values2<-seq(1,2,(1)/(n1-1))
    dataTemp<-data.frame(values2,"sole"=sole[2*(1:n1)],"split"=split[2*(1:n1)],"it2"=it)
    data<-rbind(data,dataTemp)
  it=it+1
}
if(efficiency<0.5){
dataTemp$pred<-1/3*efficiency*(dataTemp$values2+4)

round2won<-as.data.frame(t(read.table(paste("Round2/",round2Files[nIter*3],sep=""),sep=",")))
round2lost<-as.data.frame(t(read.table(paste("Round2/",round2Files[nIter*3-1],sep=""),sep=",")))
values=seq(1,2,(1)/(nrow(round2lost)-1))
maxDev<-round(max(abs(dataTemp$pred-dataTemp$split),abs(efficiency*(1+values/2)-round2lost$V1),abs(values*(1-efficiency)-round2won$V1)),4)

fig1 <- plot_ly(data, x = data$values2, y = data$sole, frame=data$it2,text='test',name = '1st Round Sole', type = 'scatter', mode = 'lines',
                line = list(color = 'rgb(205, 12, 24)', width = 4,shape = "hv"))
fig1 <-fig1 %>% add_trace(data,x=~values2,y=~(1/3*efficiency*(values2+4)), frame~it2, name ='Prediction for split', line = list(color = 'rgb(50, 120, 20)', width = 4))
fig1 <- fig1 %>% add_trace(data,x=~values2,y = ~split, frame~it2, name = '1st Round Split', line = list(color = 'rgb(22, 96, 167)', width = 4))
# fig1 <- fig1 %>% add_trace(data,x=~values2,y = ~split*2, frame~it2, name = '1st Round Split double', line = list(color = 'rgb(80, 20, 167)',width = 4))
 fig1 <- fig1 %>% layout(title = paste("Strategies in 1st round DutchFPSB with n=3, with efficiency <br> of", efficiency, ", epsilon = ", epsilon ,", max. theor. deviation =",maxDev, "<br> found after ", nIter, " iterations and", time/1000, " seconds"),
                     xaxis = list(title = "Cost Type for Sole source award"),
                       yaxis = list (title = "1st round Bids",range=c(0,2.5)))
fig1

# round2

beliefs<-read.table(paste("Round2/",round2Files[nIter*3-2],sep=""),sep=",")


round2lost=cbind(values,round2lost)
round2won=cbind(values,round2won)
fig2<-plot_ly(round2lost,x=~values, y=~V1,name=paste("Lost Strat with belief",beliefs[1,]), type = 'scatter', mode = 'lines')

if(ncol(round2lost)>2){
for(i in 3:ncol(round2lost)){
  dfi<-data.frame(yref=round2lost[,i],values=round2lost$values)
  fig2<-add_trace(fig2,data=dfi,x=~values, y=~yref,name=paste("Lost Strat with belief",beliefs[i-1,]), type = 'scatter', mode = 'lines')
}
}
fig2<-add_trace(fig2,data=dfi,x=~values, y=~efficiency*(1+values/2),name=paste("Prediction"), type = 'scatter', mode = 'lines',
                line = list(color = 'rgb(205, 12, 24)', width = 5))

fig2 <- fig2 %>% layout(title = paste("Lost Strategies in 2nd round DutchFPSB with n=3 & efficiency of", efficiency),
                        xaxis = list(title = "Cost Type for Sole source award"),
                        yaxis = list (title = "2nd round Bids"))

fig2

fig3<-plot_ly(round2won,x=~values, y=~V1,name=paste("Won Strat with belief",beliefs[1,]), type = 'scatter', mode = 'lines')

if(ncol(round2lost)>2){
for(i in 3:ncol(round2won)){
  dfi<-data.frame(yref=round2won[,i],values=round2won$values)
  fig3<-add_trace(fig3,data=dfi,x=~values, y=~yref,name=paste("Won Strat with belief",beliefs[i-1,]), type = 'scatter', mode = 'lines')
}
}
fig3<-add_trace(fig3,data=dfi,x=~values, y=~values*(1-efficiency),name=paste("Prediction"), type = 'scatter', mode = 'lines',
                line = list(color = 'rgb(205, 12, 24)', width = 5))

fig3 <- fig3 %>% layout(title = paste("Won Strategies in 2nd round DutchFPSB with n=3 & efficiency of", efficiency),
                        xaxis = list(title = "Cost Type for Sole source award"),
                        yaxis = list (title = "2nd round Bids"))

fig3
}
if(efficiency<0.5){
  fig1
}
if(efficiency<0.5){
  fig2
}
if(efficiency<0.5){
  fig3
}

# In case of sole source award

if(efficiency>=0.5){
  data<-NULL
  it=0
  

  for (path in round1Files){
    temp<-scan(file=paste("Round1/",path,sep=""),what = character(),sep = "")
    sole<-as.numeric(temp[4:(length(temp)/2)])
    split<-as.numeric(temp[(length(temp)/2+4):length(temp)])
    epsilon<-as.numeric(temp[2])
    n1=length(sole)/2
    values2<-seq(1,1.5,(0.5)/(n1-1))
    dataTemp<-data.frame(values2,"sole"=sole[2*(1:n1)],"split"=split[2*(1:n1)],"it2"=it)
    data<-rbind(data,dataTemp)
    it=it+1
  }


  dataTemp$pred<-dataTemp$values2+1/6*(3-2*dataTemp$values2)
  maxDev<-round(max(abs(dataTemp$pred-dataTemp$sole)),4)
  
  fig1 <- plot_ly(data, x = data$values2, y = data$sole, frame=data$it2,text='test',name = '1st Round Sole', type = 'scatter', mode = 'lines',
                  line = list(color = 'rgb(205, 12, 24)', width = 4,shape = "hv"))
  fig1 <- fig1 %>% add_trace(data,x=~values2,y = ~split, frame~it2, name = '1st Round Split', line = list(color = 'rgb(22, 96, 167)', width = 4))
  # fig1 <- fig1 %>% add_trace(data,x=~values2,y = ~split*2, frame~it2, name = '1st Round Split double', line = list(color = 'rgb(80, 20, 167)',width = 4))
  fig1 <- fig1 %>% layout(title = paste("Strategies in 1st round DutchFPSB with n=3, with efficiency <br> of", efficiency, ", epsilon = ", epsilon ,", max. theor. deviation =",maxDev, "found after <br>", nIter, " iterations and", time/1000, "sec"),
                          xaxis = list(title = "Cost Type for Sole source award"),
                          yaxis = list (title = "1st round Bids",range=c(0,2.5)))
  fig1 <- fig1 %>% add_trace(data,x=~values2,y = ~values2+1/6*(3-2*values2), frame~it2, line = list(color = 'rgb(80, 20, 167)',width = 4),name = 'Prediction Sole source')
  
  fig1
  
  
}


