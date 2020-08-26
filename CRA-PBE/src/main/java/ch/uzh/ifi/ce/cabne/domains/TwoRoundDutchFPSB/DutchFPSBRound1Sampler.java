package ch.uzh.ifi.ce.cabne.domains.TwoRoundDutchFPSB;

import java.util.Iterator;
import java.util.List;

import ch.uzh.ifi.ce.cabne.algorithm.BNESolverContext;
import ch.uzh.ifi.ce.cabne.domains.BidSampler;
import ch.uzh.ifi.ce.cabne.strategy.Strategy;

public class DutchFPSBRound1Sampler extends BidSampler<Double, Double[]> {

	public DutchFPSBRound1Sampler(BNESolverContext<Double,Double[]> context) {
		super(context);
	}
	@Override
	public Iterator<Sample> conditionalBidIterator(int i, Double v, Double[] b, List<Strategy<Double, Double[]>> s){
		//Assume 2 bidders, bidding for split and sole source award.
		
		Iterator<double[]> rngiter = context.getRng(1).nextVectorIterator();
		final int opponent = (i+1) % 2 ; 
		Strategy<Double, Double[]> sOpponent = s.get(opponent);
		double oppMax = sOpponent.getMaxValue();
		double oppMin = sOpponent.getMinValue();
		
		Iterator<Sample> it = new Iterator<Sample>() {
			@Override
			public boolean hasNext() {
				return true;
			}
			
			@Override
			public Sample next() {
				double[] r = rngiter.next();
				Double[][] result = new Double[2][2];
				
				//set bids
				result[i]=b;
				double val = r[0]*(oppMax-oppMin)+oppMin;
				result[opponent]= sOpponent.getBid(val);
				
				//density. Assuming uniform distribution
				double density =1/(oppMax-oppMin);
				
				//return sample
				return new Sample(density, result);
			}
		};
		return it;
	}
}







