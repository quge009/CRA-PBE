package ch.uzh.ifi.ce.cabne.helpers;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.math3.linear.RealMatrix;

import ch.uzh.ifi.ce.cabne.strategy.Round2Strategy;
import ch.uzh.ifi.ce.cabne.strategy.Strategy;
import ch.uzh.ifi.ce.cabne.strategy.UnivariatePWLStrategy;

public class DutchFPSBCache implements MultiRoundCache<Round2Value, Double> {
	public Map<String, List<Strategy<Double,Double>>> cache;
	
	public DutchFPSBCache() {
		this.cache=new Hashtable<>();
	}
	public String bidHash(Double[] belief, boolean won) {
		StringBuilder builder = new StringBuilder();
		for (int x=0; x<belief.length; x++) {
			builder.append(String.format("%9.6f|", belief[x]));
		}
		builder.append(won);
		return builder.toString();
	}
	
	public void cacheStrategies(List<Strategy<Round2Value,Double>> strats, Histories<Double> hist) {
	int nHist=hist.getnHistories();
	Round2Strategy s =(Round2Strategy) strats.get(0);
	if(s.getStrategyMatrixLost().getRowDimension()!=nHist) {
		throw new RuntimeException("error different number of histories");
	}
	RealMatrix lostStrat = s.getStrategyMatrixLost();
	RealMatrix wonStrat = s.getStrategyMatrixWon();
	
	double minCost = s.getMinValue().cost;
	double maxCost = s.getMaxValue().cost;

	//cache histories where won
	for (int i=0; i<nHist;i++) { 
		boolean won=true;
		Double[] belief = hist.getBelief(i,won);
		if(!cache.containsKey(bidHash(belief, won))){
			TreeMap<Double, Double> stratMap = new TreeMap<>();
			for(int k=0; k<lostStrat.getColumnDimension();k++) {
				Double cost = ((double) k)/lostStrat.getColumnDimension() * (maxCost-minCost)+minCost;		
				stratMap.put(cost,wonStrat.getEntry(i, k));
			}
			List<Strategy<Double,Double>> strategies = new ArrayList<>();
			strategies.add(new UnivariatePWLStrategy(stratMap));
			strategies.add(strategies.get(0));
			strategies.add(strategies.get(0));

			cache.put(bidHash(belief, won), strategies);	
		}
		
	}
	//cache histories where lost
	for (int i=0; i<nHist;i++) { 
		boolean won=false;
		Double[] belief = hist.getBelief(i,true);//need to use this interval for unique mapping
		if(!cache.containsKey(bidHash(belief, won))){
			TreeMap<Double, Double> stratMap = new TreeMap<>();
			for(int k=0; k<lostStrat.getColumnDimension();k++) {
				Double cost = ((double) k)/lostStrat.getColumnDimension() * (maxCost-minCost)+minCost;		
				stratMap.put(cost,lostStrat.getEntry(i, k));
			}
			List<Strategy<Double,Double>> strategies = new ArrayList<>();
			strategies.add(new UnivariatePWLStrategy(stratMap));
			strategies.add(strategies.get(0));
			strategies.add(strategies.get(0));

			cache.put(bidHash(belief, won), strategies);	
		}
		
	}
	}
	public boolean containsStrategy(Double[] belief, boolean won) {
		return (cache.containsKey(bidHash(belief, won)));
	}
	
	public List<Strategy<Double,Double>> getStrategy(Double[] belief, boolean won) {
		return cache.get(bidHash(belief,won));
	}
}
