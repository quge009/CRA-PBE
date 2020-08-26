package ch.uzh.ifi.ce.cabne.algorithm;

import java.util.List;

import ch.uzh.ifi.ce.cabne.strategy.Strategy;

public interface PBEAlgorithmCallback<Value, Bid> {

	public void afterIteration(int iteration, PBEAlgorithm.IterationType type, List<Strategy<Value, Bid>> strategies, double epsilon);
	
}
