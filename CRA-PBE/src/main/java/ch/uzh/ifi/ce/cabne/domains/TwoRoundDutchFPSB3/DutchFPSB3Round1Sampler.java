package ch.uzh.ifi.ce.cabne.domains.TwoRoundDutchFPSB3;

import java.util.Iterator;
import java.util.List;

import ch.uzh.ifi.ce.cabne.algorithm.BNESolverContext;
import ch.uzh.ifi.ce.cabne.domains.BidSampler;
import ch.uzh.ifi.ce.cabne.strategy.Strategy;

public class DutchFPSB3Round1Sampler extends BidSampler<Double, Double[]> {

	public DutchFPSB3Round1Sampler(BNESolverContext<Double,Double[]> context) {
		super(context);
	}
	@Override
	public Iterator<Sample> conditionalBidIterator(int i, Double v, Double[] b, List<Strategy<Double, Double[]>> s){
		//Assume 2 bidders, bidding for split and sole source award.
		
		Iterator<double[]> rngiter = context.getRng(2).nextVectorIterator(); 
		Strategy<Double, Double[]> sOpponent = s.get(i);
		double oppMax = sOpponent.getMaxValue();
		double oppMin = sOpponent.getMinValue();
		
		int opp1=(i+1)%3;
		int opp2=(i+2)%3;
		Iterator<Sample> it = new Iterator<Sample>() {
			@Override
			public boolean hasNext() {
				return true;
			}
			
			@Override
			public Sample next() {
				double[] r = rngiter.next();
				Double[][] result = new Double[3][2];
				
				//set bids
				result[i]=b;
				double val1 = r[0]*(oppMax-oppMin)+oppMin;
				double val2 = r[1]*(oppMax-oppMin)+oppMin;
				result[opp1]= sOpponent.getBid(val1);
				result[opp2]= sOpponent.getBid(val2);
				
				//density. Assuming uniform distribution
				double density =1/((oppMax-oppMin)*(oppMax-oppMin));
				
				//return sample
				return new Sample(density, result);
			}
		};
		return it;
	}
}







