package ch.uzh.ifi.ce.cabne.domains.TwoRoundDutchFPSB3;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


import ch.uzh.ifi.ce.cabne.algorithm.BNESolverContext;
import ch.uzh.ifi.ce.cabne.domains.Mechanism;
import ch.uzh.ifi.ce.cabne.helpers.Round2Value;
import ch.uzh.ifi.ce.cabne.helpers.Utility;

public class DutchFPSB3Round1 implements Mechanism<Double, Double[]>{
	//there are two players with one type each for sole source award. This uniquely determines their value for split award
	// which is efficiency*v. Each submits a sole source and a split award bid. Thus bids is 2*2 array.
	double efficiency;
	BNESolverContext<Double, Double[]> context;
	List<Utility<Round2Value>> utilitiesRound2;
	


	public DutchFPSB3Round1(double efficiency,BNESolverContext<Double, Double[]> context,List<Utility<Round2Value>> utilitiesRound2) {
		// TODO Auto-generated constructor stub
		this.efficiency=efficiency;
		this.context=context;
		this.utilitiesRound2=utilitiesRound2;
	}
	
	
	@Override
	public double computeUtility(int i, Double v, Double[][] bids) {
		double utility1=0.0;
		double utility2=0.0;
		
		int opp1 = (i+1)%3;
		int opp2 = (i+2)%3;
		
		//WDP
		int minIndexSole=0;
		int minIndexSplit=0;
		double minSole=bids[0][0];
		double minSplit=bids[0][1];
		
		for(int k=0; k<bids.length;k++) {
			if(bids[k][0]<bids[minIndexSole][0]) {
				minIndexSole=k;
				minSole=bids[minIndexSole][0];
			}
			if(bids[k][1]<bids[minIndexSplit][1]) {
				minIndexSplit=k;
				minSplit=bids[minIndexSplit][1];
			}
		}
		//TODO introduce tiebreaker
		if(2*minSplit > minSole) { 
			//Award 100% to one bidder.
			if(bids[i][0]<=minSole) {
				utility1= bids[i][0]-v;
			}
		} else {
			//split award and game has two rounds, random tiebreakers
			if(bids[i][1]<=minSplit) {
				if(bids[i][1]==bids[opp1][1]&& bids[i][1]==bids[opp2][1]) {
					//make tiebreaker (random double lower 1/3 means i won)
					Random random = new Random();
					double tieBreak = random.nextDouble();
					if(tieBreak<1/3) {
						utility1= (bids[i][1]-v*efficiency);
						int histRef = context.histories.getRef(bids[i][1]);
						Round2Value val=new Round2Value(histRef,true,v);
						utility2 	= utilitiesRound2.get(i).getUtility(val);
					} else {
						int histRef = context.histories.getRef(bids[i][1]);
						Round2Value val=new Round2Value(histRef,false,v);
						utility2 	= utilitiesRound2.get(i).getUtility(val);
					}
					
				}else if(bids[i][1]==bids[opp1][1] || bids[i][1]==bids[opp2][1]) {
					Random random = new Random();
					boolean won =random.nextBoolean();
					if(won) {
						utility1= bids[i][1]-v*efficiency;
					}
					int histRef = context.histories.getRef(bids[i][1]);
					Round2Value val=new Round2Value(histRef,won,v);
					utility2 	= utilitiesRound2.get(i).getUtility(val);
				}else {
					//i wins first split
					utility1= bids[i][1]-v*efficiency;
					int histRef = context.histories.getRef(bids[i][1]);
					Round2Value val=new Round2Value(histRef,true,v);
					utility2 	= utilitiesRound2.get(i).getUtility(val);
				}
			}else{ // opp wins first split
			int histRef = context.histories.getRef(bids[minIndexSplit][1]);
			Round2Value val=new Round2Value(histRef,false,v);
			utility2 	= utilitiesRound2.get(i).getUtility(val);
			}
		}
		
		return utility1+utility2;
	}
}
