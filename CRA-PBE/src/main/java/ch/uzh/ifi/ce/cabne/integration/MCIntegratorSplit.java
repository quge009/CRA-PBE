package ch.uzh.ifi.ce.cabne.integration;

import java.util.Iterator;
import java.util.List;

import ch.uzh.ifi.ce.cabne.algorithm.BNESolverContext;
import ch.uzh.ifi.ce.cabne.domains.BidSampler;
import ch.uzh.ifi.ce.cabne.strategy.Strategy;


// Integrator that does Monte-Carlo sampling.
// It takes samples from context.sampler, so it implicitly uses e.g. common random numbers if context.rngs contains common
// random generators.

// a subclass of this could compute E[u] by directly sampling values and applying strategies, but this would almost always 
// be very inefficient (e.g. LLG knows that the global player is truthful, and the corresponding sampler does some
// importance sampling)

public class MCIntegratorSplit<Value, Bid> extends Integrator<Value, Bid> {

	public MCIntegratorSplit(BNESolverContext<Value, Bid> context) {
		super(context);
	}

	@Override
	public double computeExpectedUtility(int i, Value v, Bid b, List<Strategy<Value, Bid>> strats) {
		int nsamples = context.getIntParameter("mcsamples");
		double result = 0.0;
		
		Iterator<BidSampler<Value, Bid>.Sample> biditer = context.sampler.conditionalBidIterator(i, v, b, strats);
		BidSampler<Value, Bid>.Sample sample;
		
		for (int MCsample=0; MCsample<nsamples; MCsample++) {
			sample = biditer.next();
			
			// add mechanism output to total, weighted by density
			result += sample.density * context.mechanism.computeUtility(i, v, sample.bids);
			if (Double.isNaN(result)) {
				throw new RuntimeException("MC integrator could not approximate integral.");
			}
		}
		
		// multiply result by integration volume
		if (b instanceof Double[]) {
			for (int j=0; j<strats.size(); j++) {
				if (j==i) continue;
				Double maxval = (Double) strats.get(j).getMaxValue();
				Double minval = (Double) strats.get(j).getMinValue();
				result *= (maxval-minval);
			}
		} else {
			throw new RuntimeException("MCIntegrator does not support this type of Bid.");
		}

		return result / nsamples;
	}
}
