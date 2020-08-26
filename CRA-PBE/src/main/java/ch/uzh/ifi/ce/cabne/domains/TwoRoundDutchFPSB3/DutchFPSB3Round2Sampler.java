package ch.uzh.ifi.ce.cabne.domains.TwoRoundDutchFPSB3;

import java.util.Iterator;
import java.util.List;

import ch.uzh.ifi.ce.cabne.algorithm.BNESolverContext;
import ch.uzh.ifi.ce.cabne.domains.BidSampler;
import ch.uzh.ifi.ce.cabne.helpers.Histories;
import ch.uzh.ifi.ce.cabne.helpers.Round2Value;
import ch.uzh.ifi.ce.cabne.strategy.Strategy;


public class DutchFPSB3Round2Sampler extends BidSampler<Round2Value, Double> {
	public Histories<Double> histories;
	
	public DutchFPSB3Round2Sampler(BNESolverContext<Round2Value,Double> context,Histories<Double> histories) {
		super(context);
		this.histories=histories;
	}
	
	
	
	public Iterator<Sample> conditionalBidIterator(int i, Round2Value v, Double b, List<Strategy<Round2Value, Double>> s){
		//Assume 2 bidders, bidding for two awards. And due to PWC and certain beliefupdating, i's belief uniquely determine the ones of his opponent.
				
		Iterator<double[]> rngiter = context.getRng(2).nextVectorIterator();
		int opp1 = (i+1) % 3;
		int opp2 = (i+2) % 3;

		//get referenceNr of History of opp
		int iRef = v.histRef;
		boolean iWon =v.won;
		Double[] beliefAbtOpp1;
		Double[] beliefAbtOpp2;
		boolean opp1won;
		boolean opp2won;
		
		//get the belief that player i has about his opponent and from which we need to sample:
		if(iWon) {
			opp1won=false;
			opp2won=false;
			beliefAbtOpp1=histories.getBelief(iRef,opp1won);
			beliefAbtOpp2=histories.getBelief(iRef,opp2won);
		}else {
			opp1won=true;
			opp2won=false;
			beliefAbtOpp1=histories.getBelief(iRef,opp1won);
			beliefAbtOpp2=histories.getBelief(iRef,opp2won);

		}
		
		
		double opp1Max = beliefAbtOpp1[1];
		double opp1Min = beliefAbtOpp1[0];
		double opp2Max = beliefAbtOpp2[1];
		double opp2Min = beliefAbtOpp2[0];

		
		Iterator<Sample> it = new Iterator<Sample>() {
			@Override
			public boolean hasNext() {
				return true;
			}
			
			@Override
			public Sample next() {
				double[] r = rngiter.next();
				Double[] result = new Double[3];
				
				//set bids
				result[i]=b;
				double costOpp1 = r[0]*(opp1Max-opp1Min)+opp1Min;
				double costOpp2 = r[1]*(opp2Max-opp2Min)+opp2Min;
				//The infSets are of same cardinality and each infSetNr of player i corresponds to the one his opponent has given the same history
				Round2Value valOpp1 =new Round2Value(iRef,opp1won,costOpp1);
				Round2Value valOpp2 = new Round2Value(iRef,opp2won,costOpp2);
				result[opp1]= s.get(opp1).getBid(valOpp1);
				result[opp2]=s.get(opp2).getBid(valOpp2);
				
				//density. Assuming uniform distribution
				double density =1/(opp1Max-opp1Min)/(opp2Max-opp2Min);
					//similar to importance sampling this accounts for the fact that in MC integrator we just multiply by volume of complete value space when in fact 
					//we only integrate over the smaller belief interval. 
				density*=(opp1Max-opp1Min)*(opp2Max-opp2Min)/(s.get(0).getMaxValue().cost-s.get(0).getMinValue().cost)/(s.get(0).getMaxValue().cost-s.get(0).getMinValue().cost); 
				
				//return sample
				return new Sample(density, result);
			}
		};
		return it;
	}
}



