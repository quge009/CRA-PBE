package ch.uzh.ifi.ce.cabne.domains;

import java.util.List;

import ch.uzh.ifi.ce.cabne.strategy.Strategy;

public interface Mechanism<Value, Bid> {
	
	public double computeUtility(int i, Value v, Bid[] bids);
	

}
