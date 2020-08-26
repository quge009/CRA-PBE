package ch.uzh.ifi.ce.cabne.verification;

import java.util.List;
import java.util.TreeMap;

import ch.uzh.ifi.ce.cabne.algorithm.BNESolverContext;
import ch.uzh.ifi.ce.cabne.helpers.UtilityHelpers;
import ch.uzh.ifi.ce.cabne.pointwiseBR.Optimizer;
import ch.uzh.ifi.ce.cabne.strategy.Strategy;
import ch.uzh.ifi.ce.cabne.strategy.PWCStrategy1Dto2D;

public class BoundingVerifier1Dto2D implements Verifier<Double, Double[]> {
	
	BNESolverContext<Double, Double[]> context;

	public BoundingVerifier1Dto2D(BNESolverContext<Double, Double[]> context) {
		super();
		this.context = context;
	}

	public Strategy<Double, Double[]> convertStrategy(int gridsize, Strategy<Double, Double[]> s) {
		TreeMap<Double, Double> dataSole = new TreeMap<>();
		TreeMap<Double, Double> dataSplit = new TreeMap<>();
		
		double maxValue = s.getMaxValue();
		double minValue =s.getMinValue();
		
		for (int j = 0; j<=gridsize; j++) {
			double v = minValue+ (maxValue-minValue) * ((double) j) / (gridsize);
			dataSole.put(v, s.getBid(v)[0]);
			dataSplit.put(v, s.getBid(v)[1]);
		}
		return new PWCStrategy1Dto2D(dataSole,dataSplit);
	}
	
	
	public double computeEpsilon(int gridsize, int i, Strategy<Double, Double[]> si, List<Strategy<Double, Double[]>> s) {
		// Compute epsilon bound using Theorem 1 from our IJCAI'17 paper
		double highestEpsilon = 0.0;
		Optimizer.Result<Double[]> oldresult = null;
		
		double maxValue = si.getMaxValue();
		double minValue = si.getMinValue();
		for (int j = 0; j<=gridsize; j++) {
			double v = minValue+ (maxValue-minValue) * ((double) j) / (gridsize);
			Double[] equilibriumBid = si.getBid(v);
			Optimizer.Result<Double[]> result = context.optimizer.findBR(i, v, equilibriumBid, s);

			// epsilon at control point itself
			double epsilon = UtilityHelpers.absoluteLoss(result.oldutility, result.utility);
			highestEpsilon = Math.max(highestEpsilon, epsilon);
			
			// epsilon in interval between this and previous control point
			if (j!=0) {
				highestEpsilon = Math.max(highestEpsilon, result.utility - oldresult.utility + epsilon); //TODO check if that is correct
			}
			oldresult = result;
		}
		return highestEpsilon;
	}

}
