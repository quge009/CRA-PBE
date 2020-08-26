package ch.uzh.ifi.ce.cabne.domains.FirstPriceSplitAward;

import ch.uzh.ifi.ce.cabne.domains.Mechanism;

public class FirstPriceSplitAward implements Mechanism<Double, Double[]>{
	//there are two players with one type each for sole source award. This uniquely determines their value for split award
	// which is efficiency*v. Each submits a sole source and a split award bid. Thus bids is 2*2 array.
	double efficiency;
	
	public FirstPriceSplitAward(double efficiency) {
		// TODO Auto-generated constructor stub
		this.efficiency=efficiency;
	}

	@Override
	public double computeUtility(int i, Double v, Double[][] bids) {
		
		
		
		double SoleCosts=Math.min(bids[0][0], bids[1][0]);	
		double SplitCosts=bids[0][1]+bids[1][1];

		int CheapestBidder;
		
		//Determine who has lower type
		if (bids[0][0] < bids[1][0]) {
			CheapestBidder=0;
		} else {
			CheapestBidder=1;
		}
				
		if(SplitCosts > SoleCosts) { 
			//Award 100% to one bidder.
			if(i == CheapestBidder) {
				return bids[i][0]-v;
			}
			else {
				return 0.0;
			}
		} else {
			//Split award
			return bids[i][1]-v*efficiency; 
			
		}	
	}
}
