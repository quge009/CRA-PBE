package ch.uzh.ifi.ce.cabne.BR;

import java.util.List;
import java.util.TreeMap;

import ch.uzh.ifi.ce.cabne.algorithm.BNESolverContext;
import ch.uzh.ifi.ce.cabne.helpers.UtilityHelpers;
import ch.uzh.ifi.ce.cabne.pointwiseBR.Optimizer;
import ch.uzh.ifi.ce.cabne.strategy.Strategy;
import ch.uzh.ifi.ce.cabne.strategy.PWCStrategy1Dto2D;
/*
 * Best response calculator which assumes that your type is 1D but the bidding
 * space is 2D. Best response strategy is found via optimizer and given on via
 * two tree maps
 */

public class BR1Dto2DCalculator implements BRCalculator<Double, Double[]>{
	BNESolverContext<Double, Double[]> context;

	public BR1Dto2DCalculator(BNESolverContext<Double, Double[]> context) {
		this.context = context;
	}
	
	
	public Result<Double, Double[]> computeBR(int i, List<Strategy<Double, Double[]>> s){
		int nPoints = Integer.parseInt(context.config.get("gridsize"));
		
		TreeMap<Double, Double> pointwiseBRsSole = new TreeMap<>();
		TreeMap<Double, Double> pointwiseBRsSplit = new TreeMap<>();
		double epsilonAbs = 0.0;
		double epsilonRel = 0.0;
		
		double maxValue = s.get(i).getMaxValue();
		double minValue = s.get(i).getMinValue();
		
		for (int j = 0; j<=nPoints; j++) {
			Double v = minValue + (maxValue-minValue) * ((double) j) / (nPoints);
			Double[] oldbid = s.get(i).getBid(v);
			Optimizer.Result<Double[]> result = context.optimizer.findBR(i, v, oldbid, s);
			epsilonAbs = Math.max(epsilonAbs, UtilityHelpers.absoluteLoss(result.oldutility, result.utility));
			epsilonRel = Math.max(epsilonRel, UtilityHelpers.relativeLoss(result.oldutility, result.utility));
						
			Double[] newbid = context.updateRule.update(oldbid, result.bid, result.oldutility, result.utility);
			pointwiseBRsSole.put(v,  newbid[0]);
			pointwiseBRsSplit.put(v, newbid[1]);	
		}
		
		return new Result<Double, Double[]>(new PWCStrategy1Dto2D(pointwiseBRsSole,pointwiseBRsSplit),epsilonAbs, epsilonRel);
	}
}
