package ch.uzh.ifi.ce.cabne.strategy;

import java.util.SortedMap;
import java.util.TreeMap;

// single-dimensional piecewise linear strategy

public class PWLStrategy1Dto2D implements Strategy<Double, Double[]> {
	Double[] values;
	Double[][] bids;
//	SortedMap<Double, Double> data; not needed in first naive implementation
	int n;
	double maxValue;
	double minValue;
	SortedMap<Double,Double> strategySplit;
	SortedMap<Double,Double> strategySole;
	
	
	public PWLStrategy1Dto2D(SortedMap<Double, Double> strategySole, SortedMap<Double,Double> strategySplit) throws RuntimeException{
		// don't use a TreeMap or anything similar for looking up the intervals.
		// After construction, we prefer a fast static data structure, i.e. a good old sorted array.
		// the map used to initialize is kept around so it can be recovered
		this.strategySplit=strategySplit;
		this.strategySole=strategySole;
		if (strategySole.size() != strategySplit.size()) {
			throw new RuntimeException("Values of Split and Sole strategy not equal. Maybe use 2D instead.");
		}
		n = strategySplit.size();
		values = new Double[n+2];
		// first row is Sole source second is Split award
		bids = new Double[n+2][2];
		
		int i = 0;
		for (double key : strategySole.keySet()) {
			i++;
			values[i] = key;
			bids[i][0] = strategySole.get(key);
			bids[i][1] = strategySplit.get(key);
		}
		values[0] = Double.MIN_VALUE;
		values[n+1] = Double.MAX_VALUE; // TODO: test that this interpolates correctly. If not, need to do something about it
		
		bids[n+1][0] = bids[n][0];
		bids[0][1] = bids[1][1];
		bids[n+1][1] = bids[n][1];
		bids[0][0] = bids[1][0]; //avoid errors due to rounding errors
		
//		isAscending = true;
//		for (i=0; i<n+1; i++) {
//			if (bids[i+1] < bids[i]) {
//				isAscending = false;
//				break;
//			}
//		}
		
		maxValue = values[n];
		minValue =values[1];
	}
	
	public static PWLStrategy1Dto2D makeTruthful(double lower, double upper, double efficiency) {
		SortedMap<Double, Double> strategySole = new TreeMap<>();
		SortedMap<Double, Double> strategySplit = new TreeMap<>();
		
		strategySole.put(lower, lower);
		strategySole.put(upper, upper);
		
		strategySplit.put(lower, efficiency*lower);
		strategySplit.put(upper, efficiency*upper);
		
		return new PWLStrategy1Dto2D(strategySole, strategySplit);		
	}
	
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
				
		double floor = values[lo];
		double ceiling = values[hi];
		
//		// TODO this shouldn't be needed or should it?
//		if (n==2) return value;
        
		//interpolate 
        double weight = (value - floor) / (ceiling - floor);
        Double[] interpolatedBids = new Double[2];
        
        interpolatedBids[0] = bids[lo][0] + weight * (bids[hi][0] - bids[lo][0]); //doesn't that work in one line?
        interpolatedBids[1] = bids[lo][1] + weight * (bids[hi][1] - bids[lo][1]);
        return interpolatedBids; 
	}
	
	@Override
    public String toString() {
        return "PiecewiseLinearStrategy{" +
                "values=" + values + "Sole source bids= " + bids[0] + "Split award bids= " + bids[2] +
                '}';
    }

//	public SortedMap<Double, Double> getData() {
//		return data;
//	}

	@Override
	public Double getMaxValue() {
		return maxValue;
	}
	public Double getMinValue() {
		return minValue;
	}
	
	public Double[] getValues() {
		return values;
	}
	
	public Double[][] getBids() {
		return bids;
	}

	public SortedMap<Double,Double> getSplitMap() {
		return strategySplit;
	}
	public SortedMap<Double,Double> getSoleMap() {
		return strategySole;
	}
//	public Double[] invert(Double bid, boolean isWin) {
//		
//		// binary search
//		int lo = 0, hi=n+1;
//		while (lo + 1 < hi) {
//			int middle = (lo + hi)/2;
//			if (bids[middle][1] <= bid) {
//				lo = middle;
//			} else {
//				hi = middle;
//			}
//		}
//		if(Math.abs(bids[hi][1]-bids[lo][1])<0.001){
//			while(lo>0 && Math.abs(bids[lo-1][1]-bids[lo][1])<0.001) {
//				lo=lo-1;
//			}
//			while(hi+1<n+1 && Math.abs(bids[hi][1]-bids[hi+1][1])<0.001) {
//					hi=hi+1;
//			}
//			double floor = values[lo];
//			double ceiling = values[hi];
//			if(isWin) {
//				ceiling=maxValue;
//			}
//			return new Double[] {floor,ceiling};		
//		}else if((lo>0 && (Math.abs(bid-bids[lo][1])<0.001)&& Math.abs(bids[lo-1][1]-bids[lo][1])<0.001)||(hi<n && (Math.abs(bid-bids[hi][1])<0.001)&& Math.abs(bids[hi][1]-bids[hi+1][1])<0.001)){
//			if(lo>0 && (Math.abs(bid-bids[lo][1])<0.001)&& Math.abs(bids[lo-1][1]-bids[lo][1])<0.001) {
//				hi=lo;
//				while(lo>0 && Math.abs(bids[lo-1][1]-bids[lo][1])<0.001) {
//					lo=lo-1;
//				}
//			}else if(hi<n && (Math.abs(bid-bids[hi][1])<0.001)&& Math.abs(bids[hi][1]-bids[hi+1][1])<0.001) {
//				lo=hi;
//				while(hi+1<n+1 && Math.abs(bids[hi][1]-bids[hi+1][1])<0.001) {
//					hi=hi+1;
//			}
//			}
//			double floor = values[lo];
//			double ceiling = values[hi];
//			if(isWin) {
//				ceiling=maxValue;
//			}
//			return new Double[] {floor,ceiling};
//		} else {
//			double floor = values[lo];
//			double ceiling = values[hi];
//			
//	        double weight = (bid - floor) / (ceiling - floor);
//	        Double val= weight * values[hi] + (1 - weight) * values[lo]; 
//	        return new Double[] {val,val};
//		}
//		
//		
//	} 
}
