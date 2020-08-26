package ch.uzh.ifi.ce.cabne.domains.TwoRoundDutchFPSB;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


import ch.uzh.ifi.ce.cabne.algorithm.BNESolverContext;
import ch.uzh.ifi.ce.cabne.domains.Mechanism;
import ch.uzh.ifi.ce.cabne.helpers.Round2Value;
import ch.uzh.ifi.ce.cabne.helpers.Utility;

public class DutchFPSBRound1 implements Mechanism<Double, Double[]>{
	//there are two players with one type each for sole source award. This uniquely determines their value for split award
	// which is efficiency*v. Each submits a sole source and a split award bid. Thus bids is 2*2 array.
	double efficiency;
	BNESolverContext<Double, Double[]> context;
	List<Utility<Round2Value>> utilitiesRound2;
	


	public DutchFPSBRound1(double efficiency,BNESolverContext<Double, Double[]> context,List<Utility<Round2Value>> utilitiesRound2) {
		// TODO Auto-generated constructor stub
		this.efficiency=efficiency;
		this.context=context;
		this.utilitiesRound2=utilitiesRound2;
	}
	
	
	@Override
	public double computeUtility(int i, Double v, Double[][] bids) {
		double utility1=0.0;
		double utility2=0.0;
		
		//WDP
		int opp=(i+1) %2;
		double SoleCosts=Math.min(bids[0][0], bids[1][0]);	
		double SplitCosts=Math.min(bids[0][1], bids[1][1]);
				
		if(2*SplitCosts > SoleCosts) { 
			//Award 100% to one bidder.
			if(bids[i][0]<=bids[opp][0]) {
				utility1= bids[i][0]-v;
			}
		} else {
			//split award and game has two rounds
			if(bids[i][1]<bids[opp][1]) {
				//i wins first split
				utility1= bids[i][1]-v*efficiency;
				int histRef = context.histories.getRef(bids[i][1]);
				Round2Value val=new Round2Value(histRef,true,v);
				utility2 	= utilitiesRound2.get(i).getUtility(val);
			}else if(bids[i][1]==bids[opp][1]){//random tie breaker
				Random random = new Random();
				boolean won =random.nextBoolean();
				if(won) {
					utility1= bids[i][1]-v*efficiency;
				}
				int histRef = context.histories.getRef(bids[i][1]);
				Round2Value val=new Round2Value(histRef,won,v);
				utility2 	= utilitiesRound2.get(i).getUtility(val);
			}else{ // opp wins first split
			int histRef = context.histories.getRef(bids[opp][1]);
			Round2Value val=new Round2Value(histRef,false,v);
			utility2 	= utilitiesRound2.get(i).getUtility(val);
			}
		}
		
		return utility1+utility2;
	}
}
