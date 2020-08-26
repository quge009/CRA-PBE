package ch.uzh.ifi.ce.cabne.domains.TwoRoundDutchFPSB3;

import java.util.List;
import java.util.StringJoiner;

import ch.uzh.ifi.ce.cabne.strategy.PWCStrategy1Dto2D;
import ch.uzh.ifi.ce.cabne.strategy.PWLStrategy1Dto2D;
import ch.uzh.ifi.ce.cabne.strategy.Strategy;


public class DutchFPSB3StrategyWriterRound1 {
    public String write(List<Strategy<Double,Double[]>> strategies, int iteration, double epsilon)  {
    	PWCStrategy1Dto2D s= (PWCStrategy1Dto2D) strategies.get(0);
    	Double minValue=s.getMinValue();
    	Double maxValue=s.getMaxValue();
    	
    	StringBuilder builder = new StringBuilder();
        builder.append(String.format("%2d", iteration));
        builder.append(String.format(" %7.6f  ", epsilon));
        builder.append(" sole ");
        int ngridpoints = 1000;
        for (int i=0; i<=ngridpoints; i++) {
            double v = minValue+(maxValue-minValue) * i / ngridpoints;
            builder.append(String.format("%5.4f",v));
            builder.append("  ");
            builder.append(String.format("%5.4f", s.getBid(v)[0]));
            builder.append("  ");
        }
        
            builder.append("\n");
            builder.append(String.format("%2d", iteration));
            builder.append(String.format(" %7.6f  ", epsilon));
            builder.append(" split ");
       for(int i=0;i<=ngridpoints;i++) {
    	   double v = minValue+(maxValue-minValue) * i / ngridpoints;
            builder.append(String.format("%5.4f",v));
            builder.append("  ");
            builder.append(String.format("%5.4f", s.getBid(v)[1]));
            builder.append("  ");
        }
    	return builder.toString();
    }
}