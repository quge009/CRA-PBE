package ch.uzh.ifi.ce.cabne.domains.FirstPriceSplitAward;

import java.util.List;
import java.util.StringJoiner;


import ch.uzh.ifi.ce.cabne.strategy.PWLStrategy1Dto2D;
import ch.uzh.ifi.ce.cabne.strategy.Strategy;


public class SplitAwardStrategyWriter {
    public String write(List<Strategy<Double,Double[]>> strategies, int iteration, double epsilon)  {
    	StringBuilder builder = new StringBuilder();
        builder.append(String.format("%2d", iteration));
        builder.append(String.format(" %7.6f  ", epsilon));

        int ngridpoints = 1000;
        for (int i=0; i<=ngridpoints; i++) {
        	double maxValue =strategies.get(0).getMaxValue();
        	double minValue = strategies.get(0).getMinValue();
            double v = minValue+(maxValue-minValue) * i / ngridpoints;
            builder.append(String.format("%5.4f",v));
            builder.append(" ");
            builder.append(String.format("%5.4f", strategies.get(0).getBid(v)[0]));
            builder.append(" ");
            builder.append(String.format("%5.4f", strategies.get(0).getBid(v)[1]));
            builder.append("  ");
        }
    	return builder.toString();
    }
}