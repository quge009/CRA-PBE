package ch.uzh.ifi.ce.cabne.strategy;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import ch.uzh.ifi.ce.cabne.helpers.Round2Value;

/* Implementation of many strategies s: R --> R one for each history. It is saved in a matrix for efficiency where the first 50% are strategies if first round is won 
/* and second half if it is lost.
 */
public class Round2Strategy implements Strategy<Round2Value, Double>{
    protected RealMatrix StrategyMatrixWon;
	protected RealMatrix StrategyMatrixLost;

    protected double colInterval;
    
    private Double maxValue;
    private Double minValue;
    
    protected double[][] StrategyWon;
    protected double[][] StrategyLost;
    
    
    
    public Round2Strategy(RealMatrix StrategyMatrixWon, RealMatrix StrategyMatrixLost, double maxV, double minV) {
    	this.StrategyMatrixWon= StrategyMatrixWon;
    	this.StrategyMatrixLost=StrategyMatrixLost;
        this.colInterval = (maxV-minV) / (StrategyMatrixWon.getColumnDimension() - 1);
        this.maxValue = maxV;
        this.minValue = minV;
        this.StrategyWon = StrategyMatrixWon.getData();
        this.StrategyLost=StrategyMatrixLost.getData();
    }
    public Double getBid(Round2Value v) {
    	int rowIndex=v.histRef;
    	Double value=v.cost;
    	if(v.won) {
    		int colIndex = (int) ((value-minValue)/colInterval);
    		int upperCol = Math.min(colIndex + 1, StrategyMatrixWon.getColumnDimension() - 1);
    		double LeftStrategy = StrategyWon[rowIndex][colIndex];
    		double RightStrategy = StrategyWon[rowIndex][upperCol];
    		
    		double colDiff = value- colIndex*colInterval-minValue;
    		double xComponentStrategy=colDiff/colInterval*(RightStrategy-LeftStrategy);
    		
    		return LeftStrategy+xComponentStrategy; 
    	} else {
    		int colIndex = (int) ((value-minValue)/colInterval);
    		int upperCol = Math.min(colIndex + 1, StrategyMatrixLost.getColumnDimension() - 1);
    		double LeftStrategy = StrategyLost[rowIndex][colIndex];
    		double RightStrategy = StrategyLost[rowIndex][upperCol];
    		
    		double colDiff = value- colIndex*colInterval-minValue;
    		double xComponentStrategy=colDiff/colInterval*(RightStrategy-LeftStrategy);
    		
    		return LeftStrategy+xComponentStrategy;
    	}
		
    }
    public static Strategy<Round2Value,Double> makeTruthful(Double minValue, Double maxValue, Double efficiency, int nHistories) {
    	
    	RealMatrix strategyMatrixWon = new Array2DRowRealMatrix(nHistories,31);
    	RealMatrix strategyMatrixLost = new Array2DRowRealMatrix(nHistories,31);
//    	RealMatrix strategyMatrixWon = new Array2DRowRealMatrix(nHistories,2);
//    	RealMatrix strategyMatrixLost = new Array2DRowRealMatrix(nHistories,2);

		
		double[] strategyWon=new double[nHistories];
    	double[] strategyLost=new double[nHistories];
    	
//    	for(int i=0;i<nHistories;i++) {
//    		strategyWon[i]=1*(1-efficiency);
//    		strategyLost[i]=0.3;
//    	}
//    	strategyMatrixWon.setColumn(0,strategyWon);
//    	strategyMatrixLost.setColumn(0,strategyLost);
//    	for(int i=0;i<nHistories;i++) {
//    		strategyWon[i]=2*(1-efficiency);
//    		strategyLost[i]=0.4;
//    	}
//    	strategyMatrixWon.setColumn(1,strategyWon);
//    	strategyMatrixLost.setColumn(1,strategyLost);

    			
    	for(int j=0;j<=30;j++) {
    		Double v = minValue + (maxValue-minValue) * ((double) j) / (30);
    		for(int i=0;i<nHistories;i++) {
    			
        		strategyWon[i]=v*(1-efficiency);
        		strategyLost[i]=v*(efficiency);
    			
    			
    					
        	}
    		strategyMatrixWon.setColumn(j, strategyWon);
    		strategyMatrixLost.setColumn(j, strategyLost);
    	}
    	
    	
		return  new Round2Strategy(strategyMatrixWon, strategyMatrixLost, maxValue, minValue);
    }
    public static Strategy<Round2Value,Double> makeZero(Double minValue, Double maxValue, Double efficiency, int nHistories) {
    	
    	RealMatrix strategyMatrixWon = new Array2DRowRealMatrix(nHistories,31);
    	RealMatrix strategyMatrixLost = new Array2DRowRealMatrix(nHistories,31);


		
		double[] strategyWon=new double[nHistories];
    	double[] strategyLost=new double[nHistories];
    			
    	for(int j=0;j<=30;j++) {
    		Double v = minValue + (maxValue-minValue) * ((double) j) / (30);
    		for(int i=0;i<nHistories;i++) {
    			
    			strategyWon[i]=0.0;
    			strategyLost[i]=0.0;		
        	}
    		strategyMatrixWon.setColumn(j, strategyWon);
    		strategyMatrixLost.setColumn(j, strategyLost);
    	}
		return  new Round2Strategy(strategyMatrixWon, strategyMatrixLost, maxValue, minValue);
    }    
	@Override
	public Round2Value getMaxValue() {
		return new Round2Value(0,false,maxValue);
	}
	@Override
	public Round2Value getMinValue() {
		return new Round2Value(0,false,minValue);
	}
    public RealMatrix getStrategyMatrixWon() {
		return StrategyMatrixWon;
	}
	public RealMatrix getStrategyMatrixLost() {
		return StrategyMatrixLost;
	}
}
