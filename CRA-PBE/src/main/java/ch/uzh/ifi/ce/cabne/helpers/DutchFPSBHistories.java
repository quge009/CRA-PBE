package ch.uzh.ifi.ce.cabne.helpers;

import java.util.SortedMap;

public class DutchFPSBHistories implements Histories<Double> {
	//Note that in DutchFPSB both bidders are symmetric when we differentiate between the two games in phase 2. Thus int i is ignored, as both have same
	//possible histories
	int nHistories;
	Double [] prices;
	Double[][] BeliefsAbtOppIfLost;
	Double[][] BeliefsAbtOppIfWon;
	
	
	public DutchFPSBHistories (Double[] prices, Double [][] BeliefsAbtOppIfLost, Double[][] BeliefsAbtOppIfWon) {
		if(BeliefsAbtOppIfLost.length!=BeliefsAbtOppIfWon.length) {
			throw new RuntimeException("invalid beliefs, should have same length");
		}
		this.prices=prices;
		this.BeliefsAbtOppIfLost=BeliefsAbtOppIfLost;
		this.BeliefsAbtOppIfWon=BeliefsAbtOppIfWon;
		this.nHistories=BeliefsAbtOppIfLost.length+BeliefsAbtOppIfWon.length;
	}
	public DutchFPSBHistories (SortedMap<Integer,Double> prices, SortedMap<Integer,Double[]> BeliefsAbtOppIfLost,SortedMap<Integer,Double[]> BeliefsAbtOppIfWon) {
		if(prices.size()!=BeliefsAbtOppIfLost.size() || BeliefsAbtOppIfLost.size()!=BeliefsAbtOppIfWon.size()) {
			throw new RuntimeException("invalid beliefs, should have same length");
		}
		Double [] pricesInit= new Double[prices.size()];
		Double[][] BeliefsAbtOppIfLostInit= new Double[prices.size()][2];
		Double[][] BeliefsAbtOppIfWonInit= new Double[prices.size()][2];
		for (int key : prices.keySet()) {
			pricesInit[key] = prices.get(key);
			BeliefsAbtOppIfLostInit[key] = BeliefsAbtOppIfLost.get(key);
			BeliefsAbtOppIfWonInit[key] = BeliefsAbtOppIfWon.get(key);
		}
		this.prices=pricesInit;
		this.BeliefsAbtOppIfLost=BeliefsAbtOppIfLostInit;
		this.BeliefsAbtOppIfWon=BeliefsAbtOppIfWonInit;
		this.nHistories=BeliefsAbtOppIfLostInit.length;
	}
	@Override
	public int getRef(Double price) {
		//binary search through price array
		int ref;
		
		int lo = 0, hi=nHistories;
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
		return BeliefsAbtOppIfWon[ref];
	}else {
		return BeliefsAbtOppIfLost[ref];
	}	
	}

}
