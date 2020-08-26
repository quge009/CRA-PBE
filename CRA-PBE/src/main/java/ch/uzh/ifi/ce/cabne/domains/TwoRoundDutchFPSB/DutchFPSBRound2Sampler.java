package ch.uzh.ifi.ce.cabne.domains.TwoRoundDutchFPSB;

import java.util.Iterator;
import java.util.List;

import ch.uzh.ifi.ce.cabne.algorithm.BNESolverContext;
import ch.uzh.ifi.ce.cabne.domains.BidSampler;
import ch.uzh.ifi.ce.cabne.helpers.Histories;
import ch.uzh.ifi.ce.cabne.helpers.Round2Value;
import ch.uzh.ifi.ce.cabne.strategy.Strategy;


public class DutchFPSBRound2Sampler extends BidSampler<Round2Value, Double> {
	public Histories<Double> histories;
	
	public DutchFPSBRound2Sampler(BNESolverContext<Round2Value,Double> context,Histories<Double> histories) {
		super(context);
		this.histories=histories;
	}
	
	
	
	public Iterator<Sample> conditionalBidIterator(int i, Round2Value v, Double b, List<Strategy<Round2Value, Double>> s){
		//Assume 2 bidders, bidding for two awards. And due to PWC and certain beliefupdating, i's belief uniquely determine the ones of his opponent.
				
		Iterator<double[]> rngiter = context.getRng(1).nextVectorIterator();
		int opponent = (i+1) % 2;

		//get referenceNr of History of opp
		int iRef = v.histRef;
		boolean iWon =v.won;
		boolean oppWon=!iWon;
		
		//get the belief that player i has about his opponent and from which we need to sample:
		Double[] beliefAbtOpp=histories.getBelief(iRef,iWon);
		
		double oppMax = beliefAbtOpp[1];
		double oppMin = beliefAbtOpp[0];
		
		Iterator<Sample> it = new Iterator<Sample>() {
			@Override
			public boolean hasNext() {
				return true;
			}
			
			@Override
			public Sample next() {
				double[] r = rngiter.next();
				Double[] result = new Double[2];
				
				//set bids
				result[i]=b;
				double costOpp = r[0]*(oppMax-oppMin)+oppMin;
				//The infSets are of same cardinality and each infSetNr of player i corresponds to the one his opponent has given the same history
				Round2Value valOpp = new Round2Value(iRef,oppWon,costOpp);
				result[opponent]= s.get(opponent).getBid(valOpp);
				
				//density. Assuming uniform distribution
				double density =1/(oppMax-oppMin);
					//similar to importance sampling this accounts for the fact that in MC integrator we just multiply by volume of complete value space when in fact 
					//we only integrate over the smaller belief interval. 
				density*=(oppMax-oppMin)/(s.get(0).getMaxValue().cost-s.get(0).getMinValue().cost); 
				
				//return sample
				return new Sample(density, result);
			}
		};
		return it;
	}
}



