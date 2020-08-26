# CRA-PBE


This is a piece of software used for numerically computing approximate Perfect Bayesian equilibria (ε-PBEs) of two-round combinatorial reverse auctions. Our algorithm is described in detail in the thesis:

**PBE Algorithm for Multi-Round Auctions**  
Vinzenz Thoma, ETH Zürich 2020

If you use this software for academic purposes, please cite the above in your work.

## Installation

Requires Java 8 (or later). In eclipse, just create a new java project pointing towards the root folder of this repository and everything should compile correctly.

CPLEX is an optional dependency. The file cplex.jar needs to be added under Project --> Properties --> Java build path, and the location of the CPLEX native extensions needs to be provided to java at runtime (e.g. "java -Djava.library.path=cplex/bin/x86-64_linux ...")


## Example: DutchFPSB

To demonstrate how to configure our algorithm, we include an example of the Dutch-FPSB domain. Bidders have a cost type drawn from [1,2] and an efficiency parameter of C that determines the cost of 50% of the business. The auction in the first round is a reverse Dutch combinatorial auction where either 100 or 50 % of the business are procured. If only the split (50%) is awarded, then the second round is a reverse FPSB auction for the remaining split.

For both rounds, we create a "BNESolverContext" object, which will contain all the objects making up our algorithm in the first round and second round subgame, and read in a configuration file. This class, as well as almost all other classes, is instanciated with two generic parameters, corresponding to the representation of values(costs) and bids.

	BNESolverContext<Double, Double[]> contextRound1 = new BNESolverContext<>();
	String configfileRound1 = args[0];
	contextRound1.parseConfig(configfileRound1);

	BNESolverContext<Round2Value, Double> contextRound2 = new BNESolverContext<>();
	String configfileRound2 = args[1];
	contextRound2.parseConfig(configfileRound2);

Then, we add all the pieces needed to specify the algorithm, from the way best responses are computed to how the strategies are updated. Again this is done for each round separately

	contextRound1.setOptimizer(new PatternSearch<>(contextRound1, new BoxPattern2Dindep()));
	contextRound1.setIntegrator(new MCIntegrator<>(contextRound1));
	contextRound1.setRng(1, new CommonRandomGenerator(1));
	contextRound1.setUpdateRule(new MultivariateDampenedUpdateRule<>(0.2, 0.7, 0.5 / contextRound1.getDoubleParameter("epsilon"), true));
	contextRound1.setBRC(new BR1Dto2DCalculator(contextRound1));
	contextRound1.setOuterBRC(new BR1Dto2DCalculator(contextRound1));

The auction instance is loaded from the config file

	double maxV = Double.parseDouble(contextRound1.config.get("auction.maxv"));
	double minV=Double.parseDouble(contextRound1.config.get("auction.minv"));
	double efficiency=Double.parseDouble(contextRound1.config.get("auction.efficiency")); 

Finally we set the sampler for the first round and mechanism for the second round 

	contextRound2.setMechanism(new DutchFPSBRound2(efficiency,contextRound2));
	contextRound1.setSampler(new DutchFPSBRound1Sampler(contextRound1));

The sampler of the second round and the mechanism of the first round will be set during the callback as they depend on tha game's histories respectively on the utilities computed in the second round and are thus not fixed throughout fictitious play.


We instantiate the BNE algorithm for an auction with 3 players and the given context.

	BNEAlgorithm<Double, Double> bneAlgo = new BNEAlgorithm<>(3, context);

We set up the algorithms as well as the initial strategies

	BNEAlgorithm<Round2Value,Double> bneAlgoRound2 = new BNEAlgorithm<Round2Value,Double>(2,contextRound2);
	PBEAlgorithm<Double,Round2Value,Double[],Double> pbeAlgo = new PBEAlgorithm<>(2,contextRound1);
		
	// add bidders (for bne round 2 done in callback since depends on histories)
	pbeAlgo.setInitialStrategy(0, PWCStrategy1Dto2D.makeTruthful(minV,maxV, efficiency));
	pbeAlgo.makeBidderSymmetric(1, 0);
		
	//put bne round into pbe round1
	pbeAlgo.setAlg(bneAlgoRound2);

The initial strategy for the second round is again defined in the callback as it depends on the histories of the game.

To actually see what's going on during the algorithm's execution, we implement a special callback interface that gets invoked after each iteration of the second and first round.

	DutchFPSBStrategyWriterRound1 writer1 = new DutchFPSBStrategyWriterRound1();
	DutchFPSBStrategyWriterRound2 writer2 = new DutchFPSBStrategyWriterRound2();
	
	PBEAlgorithmCallback<Double, Double[]> callbackAfterIt = (iteration, type, strategies1, epsilon) -> {
	...}

	PBEAlgorithmCallback<Round2Value, Double> callbackMidIt = (iteration, type, strategies2, epsilon) -> {
	...}

			
	pbeAlgo.setCallback(callbackAfterIt);
	pbeAlgo.setCallback2(callbackMidIt);
	
Finally, we run the algorithm

	PBEAlgorithm.Result<Double,Double[]> result;
	result = pbeAlgo.run();













