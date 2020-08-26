package ch.uzh.ifi.ce.cabne.domains.TwoRoundDutchFPSB;

import java.util.StringJoiner;

import org.apache.commons.math3.linear.RealMatrix;

import ch.uzh.ifi.ce.cabne.helpers.DutchFPSB3Histories;
import ch.uzh.ifi.ce.cabne.helpers.DutchFPSBHistories;
import ch.uzh.ifi.ce.cabne.helpers.Histories;
import ch.uzh.ifi.ce.cabne.strategy.Round2Strategy;


public class DutchFPSBStrategyWriterRound2 {
    public String write(Round2Strategy strat, boolean isWin)  {
    	StringBuilder builder = new StringBuilder();
    	RealMatrix matrix;
    	if(isWin) {
    		matrix=strat.getStrategyMatrixWon();
    	}else {
    		matrix=strat.getStrategyMatrixLost();
    	}
    	
    	builder.append(matrixToString(matrix));
    	return builder.toString();
    }
    
    public String write(Histories<Double> hist)  {
    	DutchFPSBHistories histories = (DutchFPSBHistories) hist;
    	StringBuilder builder = new StringBuilder();
    	int beliefsNr=histories.getnHistories();
    	for(int i=0;i<beliefsNr;i++) {
    		builder.append(String.format("%5.4f", histories.getBelief(i, false)[0]));	
    		builder.append(" ");
    		builder.append(String.format("%5.4f", histories.getBelief(i, false)[1]));
    		builder.append("\n");
    	}
    	
    	return builder.toString();
    }

	public String matrixToString(RealMatrix m) {
		StringBuilder builder = new StringBuilder();
		for (double[] row : m.getData()) {
			StringJoiner joiner = new StringJoiner(",", "", "\n");
			for (int i=0; i<row.length; i++) {
				joiner.add(String.format("\"%7.6f\"", row[i]));
			}
			builder.append(joiner);
		}
		return builder.toString();
	}
}