package ch.uzh.ifi.ce.cabne.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.uzh.ifi.ce.cabne.BR.BRCalculator;
import ch.uzh.ifi.ce.cabne.strategy.Strategy;
import ch.uzh.ifi.ce.cabne.verification.Verifier;

public class PBEAlgorithm<Value1, Value2, Bid1,Bid2> {
	public static class Result<Value1, Bid1> {
		public double epsilon;
		public List<Strategy<Value1, Bid1>> equilibriumStrategies;
		
		public Result(double epsilon, List<Strategy<Value1, Bid1>> equilibriumStrategies) {
			this.epsilon = epsilon;
			this.equilibriumStrategies = equilibriumStrategies;
		}
	}

	public enum IterationType {INNER, OUTER, VERIFICATION};
	
	private int nBidders;
	private BNESolverContext<Value1, Bid1> context;
	private BNEAlgorithm<Value2, Bid2> bneAlgRound2;
	Map<Integer, Strategy<Value1, Bid1>> initialStrategies = new HashMap<>();
	
	int[] canonicalBidders;
	boolean[] updateBidder;
	

	private PBEAlgorithmCallback<Value1, Bid1> callback; 
	private PBEAlgorithmCallback<Value2, Bid2> callback2;
	
	public PBEAlgorithm(int nBidders, BNESolverContext<Value1, Bid1> context) {
		this.nBidders = nBidders;
		canonicalBidders = new int[nBidders];
		updateBidder = new boolean[nBidders];
		for (int i=0; i<nBidders; i++) {
			canonicalBidders[i] = i;
			updateBidder[i] = true;
		}
		this.context = context;
	}

	public void setContext(BNESolverContext<Value1, Bid1> context) {
		this.context = context;
	}
	
	public void setAlg(BNEAlgorithm<Value2, Bid2> bneAlgRound2) {
		this.bneAlgRound2=bneAlgRound2;
	}
	
	public void setCallback(PBEAlgorithmCallback<Value1, Bid1> callback) {
		this.callback = callback;
	}
	public void setCallback2(PBEAlgorithmCallback<Value2, Bid2> callback) {
		this.callback2 = callback;
	}
	
	public void setInitialStrategy(int bidder, Strategy<Value1, Bid1> initialStrategy) {
		initialStrategies.put(bidder, initialStrategy);
	}
	
	public void makeBidderNonUpdating(int bidder) {
		updateBidder[bidder] = false;
	}
	
	public void makeBidderSymmetric(int bidder, int canonicalBidder) {
		if (canonicalBidders[canonicalBidder] != canonicalBidder) {
			throw new RuntimeException("Tried to add symmetric bidder but canonical bidder is not a primary bidder");
		}
		canonicalBidders[bidder] = canonicalBidder;
	}
	
	private void callbackAfterIteration(int iteration, IterationType type, List<Strategy<Value1, Bid1>> strategies, double epsilon) {
		if (callback != null) {
			callback.afterIteration(iteration, type, strategies, epsilon);
		}	
	}
	private void callbackMidIteration(int iteration, IterationType type, List<Strategy<Value2, Bid2>> strategies, double epsilon) {
		if (callback2 != null) {
			callback2.afterIteration(iteration, type, strategies, epsilon);
		}	
	}
	
	private double playRound1(List<Strategy<Value1, Bid1>> strategies, BRCalculator<Value1, Bid1> brc) {
		double highestEpsilon = 0.0;
		
		// compute best responses for players where this is needed
		Map<Integer, Strategy<Value1, Bid1>> bestResponseMap = new HashMap<>();
		for (int i=0; i<nBidders; i++) {
			if (canonicalBidders[i] == i && updateBidder[i]) {
				// this is a canonical bidder whose strategy should be updated
				BRCalculator.Result<Value1, Bid1> result = brc.computeBR(i, strategies);
				Strategy<Value1, Bid1> s = result.br;	
				highestEpsilon = Math.max(highestEpsilon, result.epsilonAbs);
				bestResponseMap.put(i, s);
			}
		}

		// update strategies in place
		for (int i=0; i<nBidders; i++) {
			if (updateBidder[i]) {
				strategies.set(i, bestResponseMap.get(canonicalBidders[i]));
			}
		}
		
		return highestEpsilon;
	}	
	private BNEAlgorithm.Result<Value2, Bid2> playRound2() {

		//Solve Round 2 of the game
		BNEAlgorithm.Result<Value2, Bid2> resultRound2 = bneAlgRound2.run();
		
		// update utilities object in 2nd round context so callback can use it to give it to first round mech
		bneAlgRound2.context.setUtilities(resultRound2.equilibriumUtilities);
		
		return resultRound2;
	}	
	private Result<Value1, Bid1> verify(List<Strategy<Value1, Bid1>> strategies, Verifier<Value1, Bid1> verifier) {
		double highestEpsilon = 0.0;
		int gridsize = context.getIntParameter("gridsize");
		
		// Convert strategies if needed (e.g. to PWC).
		// Note that if some player is non-updating (i.e. truthful), their strategy doesn't need to be converted.
		List<Strategy<Value1, Bid1>> sConverted = new ArrayList<>();
		for (int i=0; i<nBidders; i++) {
			if (updateBidder[i]) {
				sConverted.add(verifier.convertStrategy(gridsize, strategies.get(i)));
			} else {
				sConverted.add(strategies.get(i));
			}
		}
		
		// compute best responses for players where this is needed
		for (int i=0; i<nBidders; i++) {
			if (canonicalBidders[i] == i && updateBidder[i]) {
				// this is a canonical bidder whose epsilon should be computed
				double epsilon = verifier.computeEpsilon(gridsize, i, strategies.get(i), sConverted);
				highestEpsilon = Math.max(highestEpsilon, epsilon);
			}
		}
		
		return new Result<>(highestEpsilon, sConverted);
	}

	public Result<Value1, Bid1> run() {

		int maxIters = context.getIntParameter("maxiters");
		double targetEpsilon = context.getDoubleParameter("epsilon");
		double highestEpsilon = Double.POSITIVE_INFINITY;
		double epsilonRound2 = Double.POSITIVE_INFINITY;
		double epsilonRound1 = Double.POSITIVE_INFINITY;

		BRCalculator<Value1, Bid1> brc;
		
		// create list of strategies
		List<Strategy<Value1, Bid1>> strategies = new ArrayList<>();
		for (int i=0; i<nBidders; i++) {
			strategies.add(initialStrategies.get(canonicalBidders[i]));
		}
		
		context.activateConfig("innerloop"); // this allows the callback to assume some config is always active.
		callbackAfterIteration(0, IterationType.INNER, strategies, highestEpsilon);
		
		int iteration = 1;
		int lastOuterIteration = 1;
		
		while (iteration <= maxIters) {
			// This is the outer loop. First thing we do is go into the inner loop
			while (iteration <= maxIters) {
				// This is the inner loop.				
				context.activateConfig("innerloop");
				brc = context.brc;
				
				// Note that playOneRound updates the strategies in place.
				BNEAlgorithm.Result<Value2, Bid2> resultRound2 = playRound2();
				epsilonRound2=resultRound2.epsilon;
				
				callbackMidIteration(iteration, IterationType.INNER, resultRound2.equilibriumStrategies, highestEpsilon);
				
				epsilonRound1 = playRound1(strategies, brc);
				highestEpsilon=epsilonRound1+epsilonRound2;
				
				context.advanceRngs();
				
				callbackAfterIteration(iteration, IterationType.INNER, strategies, highestEpsilon);

				iteration++;
				if (highestEpsilon <= 0.8*targetEpsilon && iteration >= lastOuterIteration + 3) {
					break;
				}
			}
			
			if (iteration > maxIters) {
				return new Result<>(Double.POSITIVE_INFINITY, strategies);
			}
						
			lastOuterIteration = iteration;
			
			context.activateConfig("outerloop");
			brc = context.outerBRC;
			if (brc == null) {
				return new Result<>(Double.POSITIVE_INFINITY, strategies);
			}
			
			BNEAlgorithm.Result<Value2, Bid2> resultRound2 = playRound2();
			epsilonRound2=resultRound2.epsilon;
			
			callbackMidIteration(iteration, IterationType.OUTER, resultRound2.equilibriumStrategies, highestEpsilon);
			
			epsilonRound1 = playRound1(strategies, brc);
			highestEpsilon=epsilonRound1+epsilonRound2;
			context.advanceRngs();
			
			callbackAfterIteration(iteration, IterationType.OUTER, strategies, highestEpsilon);

			iteration++;
			if (highestEpsilon <= targetEpsilon) {
				break;
			}
		}

		context.activateConfig("verificationstep");
		boolean outerloopConverged = highestEpsilon <= targetEpsilon;
		if (context.verifier == null || !outerloopConverged) {
			return new Result<>(Double.POSITIVE_INFINITY, strategies);
		}
		Result<Value1, Bid1> result = verify(strategies, context.verifier);
		callbackAfterIteration(iteration, IterationType.VERIFICATION, result.equilibriumStrategies, result.epsilon);
		return result;
    }
}
