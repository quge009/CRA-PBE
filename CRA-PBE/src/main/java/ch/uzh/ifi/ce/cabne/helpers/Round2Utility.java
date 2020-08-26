package ch.uzh.ifi.ce.cabne.helpers;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import ch.uzh.ifi.ce.cabne.helpers.Round2Value;

/* Implementation of many utilities s: R --> R one for each history. It is saved in a matrix for efficiency where the first 50% are strategies if first round is won 
/* and second half if it is lost. It is very similar to the corresponding strategy object but has no dependency on bids as it stores utilities in Double form. this
 * makes the algorithm much faster when the first round falls back on the second round
 */
public class Round2Utility implements Utility<Round2Value>{
    protected RealMatrix utilityMatrixWon;
    protected RealMatrix utilityMatrixLost;

    protected double colInterval;
    
    private Double maxValue;
    private Double minValue;
    
    protected double[][] utilityWon;
    protected double[][] utilityLost;
    
    
    
    public Round2Utility(RealMatrix utilityMatrixWon, RealMatrix utilityMatrixLost, double maxV, double minV) {
    	this.utilityMatrixWon= utilityMatrixWon;
    	this.utilityMatrixLost=utilityMatrixLost;
        this.colInterval = (maxV-minV) / (utilityMatrixWon.getColumnDimension() - 1);
        this.maxValue = maxV;
        this.minValue = minV;
        this.utilityWon = utilityMatrixWon.getData();
        this.utilityLost=utilityMatrixLost.getData();
    }
    public Double getUtility(Round2Value v) {
    	int rowIndex=v.histRef;
    	Double value=v.cost;
    	
    	/*by choosing gridpoints in 2nd and 1st game to be overlapping we should never have to interpolate
    	*In fact it is not clear whether utility will just be linear between gridpoints. Thus the interpolation below
    	*is more or less there to handle numerical inaccuracies, but should not be used in theory.
    	*/
    	if(v.won) {
    		int colIndex = (int) ((value-minValue)/colInterval);
    		int upperCol = Math.min(rowIndex + 1, utilityMatrixWon.getColumnDimension() - 1);
    		double LeftUtility = utilityWon[rowIndex][colIndex];
    		double RightUtility = utilityWon[rowIndex][upperCol];
    		
    		double colDiff = value- colIndex*colInterval-minValue;
    		double xComponentUtility=colDiff/colInterval*(RightUtility-LeftUtility);
    		
    		return LeftUtility+xComponentUtility;
    	}else {
    		int colIndex = (int) ((value-minValue)/colInterval);
    		int upperCol = Math.min(colIndex + 1, utilityMatrixLost.getColumnDimension() - 1);
    		double LeftUtility = utilityLost[rowIndex][colIndex];
    		double RightUtility = utilityLost[rowIndex][upperCol];
    		
    		double colDiff = value- colIndex*colInterval-minValue;
    		double xComponentUtility=colDiff/colInterval*(RightUtility-LeftUtility);
    		
    		return LeftUtility+xComponentUtility;
    	}

    }

}
