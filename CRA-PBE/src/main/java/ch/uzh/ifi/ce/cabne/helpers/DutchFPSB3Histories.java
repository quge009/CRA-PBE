package ch.uzh.ifi.ce.cabne.helpers;

import java.util.SortedMap;

public class DutchFPSB3Histories implements Histories<Double> {
	//Note that in DutchFPSB both bidders are symmetric when we differentiate between the two games in phase 2. Thus int i is ignored, as both have same
	//possible histories
	int nHistories;
	Double [] prices;
	Double[][] BeliefsAbtOppWhoLost;
	Double[][] BeliefsAbtOppWhoWon;
	
	
	public DutchFPSB3Histories (Double[] prices, Double [][] BeliefsAbtOppWhoLost, Double[][] BeliefsAbtOppWhoWon) {
		if(BeliefsAbtOppWhoLost.length!=BeliefsAbtOppWhoWon.length) {
			throw new RuntimeException("invalid beliefs, should have same length");
		}
		this.prices=prices;
		this.BeliefsAbtOppWhoLost=BeliefsAbtOppWhoLost;
		this.BeliefsAbtOppWhoWon=BeliefsAbtOppWhoWon;
		this.nHistories=BeliefsAbtOppWhoLost.length;
	}
	public DutchFPSB3Histories (SortedMap<Integer,Double> prices, SortedMap<Integer,Double[]> BeliefsAbtOppWhoLost,SortedMap<Integer,Double[]> BeliefsAbtOppWhoWon) {
		if(prices.size()!=BeliefsAbtOppWhoLost.size() || BeliefsAbtOppWhoLost.size()!=BeliefsAbtOppWhoWon.size()) {
			throw new RuntimeException("invalid beliefs, should have same length");
		}
		Double [] pricesInit= new Double[prices.size()];
		Double[][] BeliefsAbtOppWhoLostInit= new Double[prices.size()][2];
		Double[][] BeliefsAbtOppWhoWonInit= new Double[prices.size()][2];
		for (int key : prices.keySet()) {
			pricesInit[key] = prices.get(key);
			BeliefsAbtOppWhoLostInit[key] = BeliefsAbtOppWhoLost.get(key);
			BeliefsAbtOppWhoWonInit[key] = BeliefsAbtOppWhoWon.get(key);
		}
		this.prices=pricesInit;
		this.BeliefsAbtOppWhoLost=BeliefsAbtOppWhoLostInit;
		this.BeliefsAbtOppWhoWon=BeliefsAbtOppWhoWonInit;
		this.nHistories=BeliefsAbtOppWhoLostInit.length;
	}
	@Override
	public int getRef(Double price) {
		//binary search through price array
		int ref;
		
		int lo = 0, hi=nHistories-1;
		while (lo + 1 < hi) {
			int middle = (lo + hi)/2;
			if (prices[middle] <= price) {
				lo = middle;
			} else {
				hi = middle;
			}
		}
		if(price<prices[lo]+1.e-6) { //if player plays exactly the constant strategy, or plays lower than his lowest valuation would predict with some numerical inaccuary accounted for
			ref=lo;
		}else {
			ref=hi; //if player plays slightly above price we assume he is of next highest range of types his strategy predicts. This is a choice of beliefsetting PBE allows for in off the path histories
		}
		
		return ref;
	}
//	public DutchFPSBHistories (SortedMap<Integer,Double[]> BeliefsAbtOppIfLostMap, SortedMap<Integer,Double[]> BeliefsAbtOppIfWonMap) {
//		if(BeliefsAbtOppIfLostMap.size()!=BeliefsAbtOppIfWonMap.size()) {
//			throw new RuntimeException("Beliefsets should have same length");
//		}
//		this.nSets = BeliefsAbtOppIfLostMap.size();
//		BeliefsAbtOppIfLost = new Double[nSets][2];
//		BeliefsAbtOppIfWon = new Double[nSets][2];
//		
//		int i = 0;
//		for (int key : BeliefsAbtOppIfLostMap.keySet()) {
//			BeliefsAbtOppIfLost[i] = BeliefsAbtOppIfLostMap.get(key);
//			i++;
//		}
//		i = 0;
//		for (int key : BeliefsAbtOppIfWonMap.keySet()) {
//			BeliefsAbtOppIfWon[i] = BeliefsAbtOppIfLostMap.get(key);
//			i++;
//		}	
//	}
	@Override
	public int getnHistories() {
		return nHistories;
	}
	
	public Double[] getBelief(int ref, boolean won) {
	if(won) {
		return BeliefsAbtOppWhoWon[ref];
	}else {
		return BeliefsAbtOppWhoLost[ref];
	}	
	}

}
