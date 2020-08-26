package ch.uzh.ifi.ce.cabne.strategy;

import java.util.SortedMap;
import java.util.TreeMap;

// single-dimensional piecewise constant strategy

public class PWCStrategy1Dto2D extends PWLStrategy1Dto2D {
	
	public PWCStrategy1Dto2D(SortedMap<Double, Double> strategySole, SortedMap<Double, Double> strategySplit) {
		super(strategySole, strategySplit);
	}

	@Override
	public Double[] getBid(Double value) {
		// binary search
		int lo = 0, hi=n+1;
		while (lo + 1 < hi) {
			int middle = (lo + hi)/2;
			if (values[middle] <= value) {
				lo = middle;
			} else {
				hi = middle;
			}
		}
		Double[] constantBids = new Double[2];
		constantBids[0]=bids[lo][0];
		constantBids[1]=bids[lo][1];
        return constantBids;
	}

	public static PWCStrategy1Dto2D makeTruthful(double lower, double upper, double efficiency) {
		SortedMap<Double, Double> strategySole = new TreeMap<>();
		SortedMap<Double, Double> strategySplit = new TreeMap<>();
		
		//need in contrast to PWL more than start and endpoints 
		for(int i=0;i<=30;i++) {
			Double v = lower + (upper-lower) * ((double) i) / (30);
			strategySole.put(v, v);
			
			strategySplit.put(v, v*efficiency);
		}
		
		
		return new PWCStrategy1Dto2D(strategySole, strategySplit);		
	}
}
