package ch.uzh.ifi.ce.cabne.BR;

import java.util.List;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import ch.uzh.ifi.ce.cabne.algorithm.BNESolverContext;
import ch.uzh.ifi.ce.cabne.helpers.Round2Utility;
import ch.uzh.ifi.ce.cabne.helpers.Round2Value;
import ch.uzh.ifi.ce.cabne.helpers.Utility;
import ch.uzh.ifi.ce.cabne.helpers.UtilityHelpers;
import ch.uzh.ifi.ce.cabne.pointwiseBR.Optimizer;
import ch.uzh.ifi.ce.cabne.strategy.Round2Strategy;
import ch.uzh.ifi.ce.cabne.strategy.Strategy;

/*
 * Best response calculator which assumes that values and bids are 2-dimensional arrays of Doubles.
 * Constructs a GridStrategy2D as the best response.
 */
public class DutchFPSBRound2BR implements BRCalculator<Round2Value, Double> {
	BNESolverContext<Round2Value, Double> context;

	public DutchFPSBRound2BR(BNESolverContext<Round2Value, Double> context) {
		this.context = context;
	}

	public Result<Round2Value, Double> computeBR(int i, List<Strategy<Round2Value, Double>> s) {

		int nPoints = Integer.parseInt(context.config.get("gridsize"));
		int nHistories = context.getnHistories();
		RealMatrix strategyMatrixWon = new Array2DRowRealMatrix(nHistories,nPoints+1);
		RealMatrix strategyMatrixLost = new Array2DRowRealMatrix(nHistories,nPoints+1);
		RealMatrix strategyMatrixLostTemp = new Array2DRowRealMatrix(nHistories,nPoints+1);
		RealMatrix utilityMatrixWon = new Array2DRowRealMatrix(nHistories,nPoints+1);
		RealMatrix utilityMatrixLost = new Array2DRowRealMatrix(nHistories,nPoints+1);
		double epsilonAbs = 0.0;
		double epsilonRel = 0.0;
		
		Double maxCost = s.get(i).getMaxValue().cost;
		Double minCost = s.get(i).getMinValue().cost;
		//strategy if won
		for (int x = 0; x<=nPoints; x++) {
			for (int HistNr = 0; HistNr<nHistories; HistNr++) {
				Double cost = ((double) x)/nPoints * (maxCost-minCost)+minCost;
				Round2Value v = new Round2Value(HistNr,true,cost);
				Double oldbid = s.get(i).getBid(v);
				
				Optimizer.Result<Double> result = context.optimizer.findBR(i, v, oldbid, s);
				epsilonAbs = Math.max(epsilonAbs, UtilityHelpers.absoluteLoss(result.oldutility, result.utility));
				epsilonRel = Math.max(epsilonRel, UtilityHelpers.relativeLoss(result.oldutility, result.utility));
				Double newbid = context.updateRule.update(oldbid, result.bid, result.oldutility, result.utility);
				//update strategy and save corresponding utility
				strategyMatrixWon.setEntry(HistNr,x, newbid);

				double util=context.integrator.computeExpectedUtility(i, v, newbid, s);
				utilityMatrixWon.setEntry(HistNr,x, util);
				
				//calculate strategyMatrixLostTemp existing to do alternating best response
				Round2Value vLost = new Round2Value(HistNr,false,cost);
				Double oldbidLost = s.get(i).getBid(vLost);
				strategyMatrixLostTemp.setEntry(HistNr,x, oldbidLost);
				}
		}
		Strategy<Round2Value,Double> strategyTemp =new Round2Strategy(strategyMatrixWon,strategyMatrixLostTemp, maxCost, minCost);
		for(int k=0;k<s.size();k++) {
			s.set(k, strategyTemp);
		}
		//if lost
		for (int x = 0; x<=nPoints; x++) {
			for (int HistNr = 0; HistNr<nHistories; HistNr++) {
				Double cost = ((double) x)/nPoints * (maxCost-minCost)+minCost;
				Round2Value v = new Round2Value(HistNr,false,cost);
				Double oldbid = s.get(i).getBid(v);
				
				Optimizer.Result<Double> result = context.optimizer.findBR(i, v, oldbid, s);
				epsilonAbs = Math.max(epsilonAbs, UtilityHelpers.absoluteLoss(result.oldutility, result.utility));
				epsilonRel = Math.max(epsilonRel, UtilityHelpers.relativeLoss(result.oldutility, result.utility));
				Double newbid = context.updateRule.update(oldbid, result.bid, result.oldutility, result.utility);
				//update strategy and save corresponding utility
				strategyMatrixLost.setEntry(HistNr,x, newbid);

				double util=context.integrator.computeExpectedUtility(i, v, newbid, s);
				utilityMatrixLost.setEntry(HistNr,x, util);
				}
		}
		Strategy<Round2Value,Double> strategy =new Round2Strategy(strategyMatrixWon,strategyMatrixLost, maxCost, minCost);
		Utility<Round2Value> utility = new Round2Utility(utilityMatrixWon, utilityMatrixLost,maxCost, minCost);
		return new Result<Round2Value, Double>(strategy , utility,epsilonAbs, epsilonRel);
	}
}
