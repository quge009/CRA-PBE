package ch.uzh.ifi.ce.cabne.domains.TwoRoundDutchFPSB3;

import java.util.List;

import ch.uzh.ifi.ce.cabne.algorithm.BNESolverContext;
import ch.uzh.ifi.ce.cabne.domains.Mechanism;
import ch.uzh.ifi.ce.cabne.helpers.Round2Value;
import ch.uzh.ifi.ce.cabne.strategy.Strategy;

public class DutchFPSB3Round2 implements Mechanism<Round2Value, Double>{
	//there are two players with one type each for sole source award. This uniquely determines their value for split award
	// which is efficiency*v. Each submits a sole source and a split award bid. Thus bids is 2*2 array.
	double efficiency;
	BNESolverContext<Round2Value, Double> context;
	
	public DutchFPSB3Round2(double efficiency, BNESolverContext<Round2Value, Double> context) {
		// TODO Auto-generated constructor stub
		this.efficiency=efficiency;
		this.context=context;
	}

	@Override
	public double computeUtility(int i, Round2Value v, Double[] bids) {
		//check if i won first game to adapt her costs to second round
		Double cost = v.cost;
		if (v.won) { //0 is won
			cost*=(1-efficiency);
		} else {
			cost*=efficiency;
		}
		
		//for tiebreaker
		int opp1=(i+1)%3;
		int opp2=(i+2)%3;
		

		int minIndex=i;
		for(int k=0;k<bids.length;k++) {
			if(bids[k]<=bids[minIndex]) {
				minIndex=k;
			}
		}

		if(v.won) {
			if(bids[i]<bids[opp1] && bids[i]<bids[opp2]) {
				return bids[i]-cost;
			}
			else {
				return 0.0;
			}
		}else { 
			//asume that opp1 is winner (see sampler)
			if(bids[i]<bids[opp1] && bids[i]<bids[opp2]) {
				return bids[i]-cost;
			}
			else if(bids[i]<=bids[opp1] && bids[i]==bids[opp2]){
				return (bids[i]-cost)/2;
			}
			else {
				return 0.0;
			}
		}
		

	}
}
