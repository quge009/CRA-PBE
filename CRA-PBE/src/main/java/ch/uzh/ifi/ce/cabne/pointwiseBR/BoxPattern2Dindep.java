package ch.uzh.ifi.ce.cabne.pointwiseBR;

import java.util.ArrayList;
import java.util.List;


// a pattern consisting of a box, i.e. 9 points on a grid
// TODO: implement for arbitrary dimensions and other amount of points


public class BoxPattern2Dindep extends Pattern<Double[]> {
	
	public BoxPattern2Dindep() {
		super(2);
	}

	@Override
	List<Double[]> getPatternPoints(Double[] center, int npoints, double scale) {
		if (npoints != 9) throw new RuntimeException();
		
		//System.out.format("%9.6f  %9.6f (center)\n", center[0], center[1]);
		int[] refX = new int[] {0,1,-1};
		int[] refY =new int[] {0,1,-1}; //choose as triebreaker always to increase bids independently
		List<Double[]> result = new ArrayList<>(npoints);
		for (int k=0; k<=2; k++) {
			for (int r=0; r<=2; r++) {
				int x=refX[k];
				int y=refY[r];
				Double[] nextPoint = new Double[]{
					Math.max(center[0] + x*scale, 0.0),
					Math.max(center[1] + y*scale, 0.0)
				};
				result.add(nextPoint);					
				//System.out.format("%9.6f  %9.6f\n", nextPoint[0], nextPoint[1]);
			}
		}
		//System.out.println();
		
		// careful: need to keep the invariant that the original center is always contained in the pattern, even if it has, e.g., a coordinate of -0.0 instead of 0.0
		result.set(getCenterIndex(npoints), center);
		return result;
	}

	@Override
	int getCenterIndex(int npoints) {
		return 0;
	}
	
	@Override
	protected String bidHash(Double[] key) {
		StringBuilder builder = new StringBuilder();
		for (int x=0; x<key.length; x++) {
			builder.append(String.format("%9.6f|", key[x]));
		}
		return builder.toString();
	}

}
