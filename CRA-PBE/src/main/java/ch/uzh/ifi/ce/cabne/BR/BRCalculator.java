package ch.uzh.ifi.ce.cabne.BR;

import java.util.List;

import ch.uzh.ifi.ce.cabne.helpers.Utility;
import ch.uzh.ifi.ce.cabne.strategy.Strategy;

public interface BRCalculator<Value, Bid> {
	public static class Result<Value, Bid> {
		public Strategy<Value, Bid> br;
		public Utility<Value> util;
		public double epsilonAbs, epsilonRel;
		
		public Result(Strategy<Value, Bid> br, double epsilonAbs, double epsilonRel) {
			this.br = br;
			this.epsilonAbs = epsilonAbs;
			this.epsilonRel = epsilonRel;
		}
		public Result(Strategy<Value, Bid> br, Utility<Value> util, double epsilonAbs, double epsilonRel) {
			this.br = br;
			this.epsilonAbs = epsilonAbs;
			this.epsilonRel = epsilonRel;
			this.util=util;
		}
	}

	Result<Value, Bid> computeBR(int i, List<Strategy<Value, Bid>> s);
	
}