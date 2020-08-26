package ch.uzh.ifi.ce.cabne.examples;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import ch.uzh.ifi.ce.cabne.BR.BR1Dto2DCalculator;
import ch.uzh.ifi.ce.cabne.BR.DutchFPSBRound2BR;
import ch.uzh.ifi.ce.cabne.BR.DutchFPSBRound2BRCache;
import ch.uzh.ifi.ce.cabne.algorithm.BNEAlgorithm;
import ch.uzh.ifi.ce.cabne.algorithm.BNEAlgorithmCallback;
import ch.uzh.ifi.ce.cabne.algorithm.BNESolverContext;
import ch.uzh.ifi.ce.cabne.algorithm.PBEAlgorithm;
import ch.uzh.ifi.ce.cabne.algorithm.PBEAlgorithmCallback;
import ch.uzh.ifi.ce.cabne.domains.TwoRoundDutchFPSB.DutchFPSBRound1;
import ch.uzh.ifi.ce.cabne.domains.TwoRoundDutchFPSB.DutchFPSBRound1Sampler;
import ch.uzh.ifi.ce.cabne.domains.TwoRoundDutchFPSB.DutchFPSBRound2;
import ch.uzh.ifi.ce.cabne.domains.TwoRoundDutchFPSB.DutchFPSBRound2Sampler;
import ch.uzh.ifi.ce.cabne.domains.TwoRoundDutchFPSB.DutchFPSBStrategyWriterRound2;
import ch.uzh.ifi.ce.cabne.domains.TwoRoundDutchFPSB3.DutchFPSB3Round1;
import ch.uzh.ifi.ce.cabne.domains.TwoRoundDutchFPSB3.DutchFPSB3Round1Sampler;
import ch.uzh.ifi.ce.cabne.domains.TwoRoundDutchFPSB3.DutchFPSB3Round2;
import ch.uzh.ifi.ce.cabne.domains.TwoRoundDutchFPSB3.DutchFPSB3Round2Sampler;
import ch.uzh.ifi.ce.cabne.domains.TwoRoundDutchFPSB3.DutchFPSB3StrategyWriterRound1;
import ch.uzh.ifi.ce.cabne.domains.TwoRoundDutchFPSB3.DutchFPSB3StrategyWriterRound2;
import ch.uzh.ifi.ce.cabne.domains.TwoRoundDutchFPSB.DutchFPSBStrategyWriterRound1;
import ch.uzh.ifi.ce.cabne.integration.MCIntegrator;
import ch.uzh.ifi.ce.cabne.pointwiseBR.PatternSearch;
import ch.uzh.ifi.ce.cabne.pointwiseBR.UnivariatePattern;
import ch.uzh.ifi.ce.cabne.pointwiseBR.BoxPattern2D;
import ch.uzh.ifi.ce.cabne.pointwiseBR.BoxPattern2Dconst;
import ch.uzh.ifi.ce.cabne.pointwiseBR.BoxPattern2Dindep;
import ch.uzh.ifi.ce.cabne.pointwiseBR.MultivariateCrossPattern;
import ch.uzh.ifi.ce.cabne.pointwiseBR.MultivariateGaussianPattern;
import ch.uzh.ifi.ce.cabne.pointwiseBR.updateRule.MultivariateDampenedUpdateRule;
import ch.uzh.ifi.ce.cabne.pointwiseBR.updateRule.UnivariateDampenedUpdateRule;
import ch.uzh.ifi.ce.cabne.randomsampling.CommonRandomGenerator;
import ch.uzh.ifi.ce.cabne.randomsampling.NaiveRandomGenerator;
import ch.uzh.ifi.ce.cabne.strategy.PWCStrategy1Dto2D;
import ch.uzh.ifi.ce.cabne.strategy.PWLStrategy1Dto2D;
import ch.uzh.ifi.ce.cabne.strategy.Round2Strategy;
import ch.uzh.ifi.ce.cabne.strategy.Strategy;
import ch.uzh.ifi.ce.cabne.helpers.DutchFPSB3Histories;
import ch.uzh.ifi.ce.cabne.helpers.DutchFPSBHistories;
import ch.uzh.ifi.ce.cabne.helpers.Round2Value;
import ch.uzh.ifi.ce.cabne.helpers.DutchFPSBCache;

public class DutchFPSB3Cache {

	public static void main(String[] args) throws InterruptedException, IOException{
		//run configurations:
//		...DutchFPSB1.config
//		...DutchFPSB2.config
//		...\OutputDutchFPSB3\Round1 <-saved round1 strats
//		...\OutputDutchFPSB3\Round2 <-saved round2 strats
//		...\OutputDutchFPSB3 <-saves setup
		
		//measure time
		long startTime = System.nanoTime();
		
		//Round1
		// create context and read config
		BNESolverContext<Double, Double[]> contextRound1 = new BNESolverContext<>();
		String configfileRound1 = args[0];
		contextRound1.parseConfig(configfileRound1);
		
		// initialize all algorithm pieces
		contextRound1.setOptimizer(new PatternSearch<>(contextRound1, new BoxPattern2Dindep()));
		contextRound1.setIntegrator(new MCIntegrator<>(contextRound1));
		contextRound1.setRng(2, new CommonRandomGenerator(2));
		contextRound1.setUpdateRule(new MultivariateDampenedUpdateRule<>(0.2, 0.7, 0.5 / contextRound1.getDoubleParameter("epsilon"), true));
		contextRound1.setBRC(new BR1Dto2DCalculator(contextRound1));
		contextRound1.setOuterBRC(new BR1Dto2DCalculator(contextRound1));
		//contextRound1.setVerifier(new BoundingVerifier1Dto2D(contextRound1));		
		
		// instanciate auction setting
		double maxV = Double.parseDouble(contextRound1.config.get("auction.maxv"));
		double minV=Double.parseDouble(contextRound1.config.get("auction.minv"));
		double efficiency=Double.parseDouble(contextRound1.config.get("auction.efficiency")); //public cost efficiency parameter C that determines cost of split award.
		
		//contextRound1.setMechanism(new DutchFPSBRound1(efficiency,contextRound1,utilities#)); // done in callbackmidit
		contextRound1.setSampler(new DutchFPSB3Round1Sampler(contextRound1));
		
		//Round2
		// create context and read config
		BNESolverContext<Round2Value, Double> contextRound2 = new BNESolverContext<>();
		String configfileRound2 = args[1];
		contextRound2.parseConfig(configfileRound2);
		
		// initialize all algorithm pieces
		contextRound2.setOptimizer(new PatternSearch<>(contextRound2, new UnivariatePattern()));
		contextRound2.setIntegrator(new MCIntegrator<>(contextRound2));
		contextRound2.setRng(2, new NaiveRandomGenerator(2));
		contextRound2.setUpdateRule(new UnivariateDampenedUpdateRule(0.2, 0.7, 0.5 / contextRound2.getDoubleParameter("epsilon"), true));
		contextRound2.setBRC(new DutchFPSBRound2BR(contextRound2));
		contextRound2.setOuterBRC(new DutchFPSBRound2BR(contextRound2));
		//contextRound2.setVerifier(new BoundingVerifier1Dto2D(contextRound2));//TODO		
		
		// instanciate auction setting
		contextRound2.setMechanism(new DutchFPSB3Round2(efficiency,contextRound2));
		//contextRound2.setSampler(new DutchFPSBRound2Sampler(contextRound2)); //done in l.174 callback, depends on history
		
		//setup cache
		DutchFPSBCache cache = new DutchFPSBCache();
		//setup the algorithms
		BNEAlgorithm<Round2Value,Double> bneAlgoRound2 = new BNEAlgorithm<Round2Value,Double>(3,contextRound2);
		PBEAlgorithm<Double,Round2Value,Double[],Double> pbeAlgo = new PBEAlgorithm<>(3,contextRound1);
		
		// add bidders (for bne round 2 done in callback since depends on histories)
		pbeAlgo.setInitialStrategy(0, PWCStrategy1Dto2D.makeTruthful(minV,maxV, efficiency));//TODO set with gridsize?
		pbeAlgo.makeBidderSymmetric(1, 0);
		pbeAlgo.makeBidderSymmetric(2, 0);
		
		//put bne round into pbe round1
		pbeAlgo.setAlg(bneAlgoRound2);
		
		//Define writers for callbacks
		DutchFPSB3StrategyWriterRound1 writer1 = new DutchFPSB3StrategyWriterRound1();
		DutchFPSB3StrategyWriterRound2 writer2 = new DutchFPSB3StrategyWriterRound2();
		//Define the different callbacks
		///Round 2 Callback (not needed, too much data produced if we write this out)
		BNEAlgorithmCallback<Round2Value, Double> callbackRound2 = (iteration, type, strategies2, epsilon) -> {
			Round2Strategy s=(Round2Strategy) strategies2.get(0);
			//make strategy monotone in all histories
			RealMatrix strategyMatrixLost = s.getStrategyMatrixLost();
			RealMatrix strategyMatrixWon = s.getStrategyMatrixWon();
			RealMatrix strategyMatrixLostNew = new Array2DRowRealMatrix(strategyMatrixLost.getRowDimension(),strategyMatrixLost.getColumnDimension());
			
			for(int hist=0; hist<strategyMatrixLost.getRowDimension();hist++) {
				double prevBid=strategyMatrixLost.getNorm();
				for(int col=strategyMatrixLost.getColumnDimension()-1; col>=0;col--) {
					Double bid = Math.min(prevBid, strategyMatrixLost.getEntry(hist, col));
					strategyMatrixLostNew.setEntry(hist, col, bid);
					prevBid=bid;
				}
			}
			double maxiV = s.getMaxValue().cost;
			double miniV = s.getMinValue().cost;
			strategies2.set(0, new Round2Strategy(strategyMatrixWon,strategyMatrixLostNew,maxiV,miniV));		
			strategies2.set(1, strategies2.get(0));
			strategies2.set(2, strategies2.get(0));
			
//			s=(Round2Strategy) strategies2.get(0);
//			//write out equilibrium strategies for second round
//				String s2win = writer2.write(s, true);
//				Path outputFile2win = Paths.get(args[2]).resolve(String.format("iter%03d won-unconv.strats", iteration));
//
//				try {
//					Files.write(
//						outputFile2win, s2win.getBytes(), 
//						StandardOpenOption.CREATE, 
//						StandardOpenOption.WRITE, 
//						StandardOpenOption.TRUNCATE_EXISTING
//					);
//				} catch (IOException e) {
//				}
//				String s2lost = writer2.write(s, false);
//				Path outputFile2lost = Paths.get(args[2]).resolve(String.format("iter%03d lost-unconv.strats", iteration));
//
//				try {
//					Files.write(
//						outputFile2lost, s2lost.getBytes(), 
//						StandardOpenOption.CREATE, 
//						StandardOpenOption.WRITE, 
//						StandardOpenOption.TRUNCATE_EXISTING
//					);
//				} catch (IOException e) {
//				}
//				System.out.println(iteration+ " " + epsilon);
		};
		
		///Round 1 Callback After Iteration (after playRound1)
		PBEAlgorithmCallback<Double, Double[]> callbackAfterIt = (iteration, type, strategies1, epsilon) -> {
			
			// make first round strategy monotone, with slight attractor to constant bids (else having constnat bids would be impossible due to numerical inaccuracy)
			for (int i=0; i<1; i++) {
                PWCStrategy1Dto2D s = (PWCStrategy1Dto2D) strategies1.get(i);
                TreeMap<Double, Double> splitMap =(TreeMap<Double, Double>) s.getSplitMap();
                SortedMap<Double,Double> soleMap =s.getSoleMap();
                SortedMap<Double, Double> newSplitMap = new TreeMap<>();
                SortedMap<Double, Double> newSoleMap = new TreeMap<>();
                
                double maxiV = s.getMaxValue();
                double secondLastBid =splitMap.lowerEntry(maxiV).getValue();
                newSplitMap.put(maxiV,secondLastBid);
                double previousBid = splitMap.lastEntry().getValue();
                for (Map.Entry<Double, Double> e : splitMap.descendingMap().entrySet()) {
                    Double v = e.getKey();
                    if(Math.abs(previousBid-e.getValue())<0.0005){
                        Double bid = previousBid;
                        newSplitMap.put(v, bid);
                        previousBid = bid;
                    }else {
                        Double bid = Math.min(previousBid, e.getValue());
                        newSplitMap.put(v, bid);
                        previousBid = bid;
                    }
                }
                //make sole just monotone
                previousBid = 0.0;
                for (Map.Entry<Double, Double> e : soleMap.entrySet()) {
                    Double v = e.getKey();
                        Double bid = Math.max(previousBid, e.getValue());
                        newSoleMap.put(v, bid);
                        previousBid = bid; 
                }
                strategies1.set(i, new PWCStrategy1Dto2D(newSoleMap,newSplitMap));
            }
            strategies1.set(1, strategies1.get(0));
            strategies1.set(2, strategies1.get(0));
            
			// Create new Histories Object, use maps as a priori length unclear... Also assumes monotonicity
			SortedMap<Integer, Double> splitPrices = new TreeMap<Integer, Double>();
			SortedMap<Integer, Double[]> BeliefsAbtOppWhoLost = new TreeMap<Integer, Double[]>();
			SortedMap<Integer, Double[]> BeliefsAbtOppWhoWon = new TreeMap<Integer, Double[]>();
			
			PWCStrategy1Dto2D s = (PWCStrategy1Dto2D) strategies1.get(0); //strategies are symmetric so we take first
			Double minValue= s.getMinValue();
			Double maxValue= s.getMaxValue();
            Double[] valuesRaw = s.getValues();
            Double[][] BidsRaw= s.getBids();
            ///resolve issue about arrays having a "telomere" on either side
            int nPoints = valuesRaw.length-2;
            Double[] values=new Double[nPoints];
            Double[] splitBids=new Double[nPoints];
            for (int j = 0; j<nPoints; j++) {
    			splitBids[j]=BidsRaw[j+1][1];
    			values[j]=valuesRaw[j+1];
            }
            
            ///iterate through bids to find all possible histories;
            Double leftBelief=minValue;
            Double rightBelief=values[1];
            
            int k=0;
            for(int j=1;j<nPoints;j++) {
            	if(j+1==nPoints) {
            		BeliefsAbtOppWhoLost.put(k,  new Double[]{leftBelief,maxValue});
                	BeliefsAbtOppWhoWon.put(k,  new Double[]{leftBelief,rightBelief});
                	splitPrices.put(k, splitBids[j-1]);
            	}else if (Math.abs(splitBids[j]-splitBids[j-1])>1.e-6){	//for numerical mistakes
            		BeliefsAbtOppWhoLost.put(k,  new Double[]{leftBelief,maxValue});
                	BeliefsAbtOppWhoWon.put(k,  new Double[]{leftBelief,rightBelief});
                	splitPrices.put(k, splitBids[j-1]);
                	k++;
                	leftBelief=values[j];
                	rightBelief=values[j+1];
            	}else {
            		rightBelief=values[j+1];
            	}
            }
            //create new histories object and set it for second round sampler and first round mechanism
			DutchFPSB3Histories histories = new DutchFPSB3Histories (splitPrices,BeliefsAbtOppWhoLost, BeliefsAbtOppWhoWon);
			contextRound1.setHistories(histories);
			contextRound2.setnHistories(histories.getnHistories());
			contextRound2.setSampler(new DutchFPSB3Round2Sampler(contextRound2,histories));
			
			//set initial strategies for second round (so that nHistories is right)
			bneAlgoRound2.setInitialStrategy(0, Round2Strategy.makeTruthful(minValue, maxValue, efficiency, histories.getnHistories()));
			bneAlgoRound2.makeBidderSymmetric(1, 0);
			bneAlgoRound2.makeBidderSymmetric(2, 0);
			
			//set up BRC 2nd round with cache and histories
			contextRound2.setBRC(new DutchFPSBRound2BRCache(contextRound2,cache,histories));
			contextRound2.setOuterBRC(new DutchFPSBRound2BRCache(contextRound2,cache,histories));

			//finally write first round strategies into console
//			StringBuilder builder = new StringBuilder();
//            builder.append(String.format("%2d", iteration));
//            builder.append(String.format(" %7.6f  ", epsilon));
//            builder.append(" sole ");
//            int ngridpoints = 1000;
//            for (int i=0; i<=ngridpoints; i++) {
//                double v = minValue+(maxValue-minValue) * i / ngridpoints;
//                builder.append(String.format("%5.4f",v));
//                builder.append("  ");
//                builder.append(String.format("%5.4f", s.getBid(v)[0]));
//                builder.append("  ");
//            }
//            
//                builder.append("\n");
//                builder.append(String.format("%2d", iteration));
//                builder.append(String.format(" %7.6f  ", epsilon));
//                builder.append(" split ");
//           for(int i=0;i<=ngridpoints;i++) {
//        	   double v = minValue+(maxValue-minValue) * i / ngridpoints;
//                builder.append(String.format("%5.4f",v));
//                builder.append("  ");
//                builder.append(String.format("%5.4f", s.getBid(v)[1]));
//                builder.append("  ");
//            }
			
			//into file
			String s1 = writer1.write(strategies1, iteration, epsilon);
			Path output = Paths.get(args[2]).resolve(String.format("iter%03d round1.strats", iteration));

			try {
				Files.write(
					output, s1.getBytes(), 
					StandardOpenOption.CREATE, 
					StandardOpenOption.WRITE, 
					StandardOpenOption.TRUNCATE_EXISTING
				);
			} catch (IOException e) {
			}
			

            System.out.println(s1.toString());
		};
		
		PBEAlgorithmCallback<Round2Value, Double> callbackMidIt = (iteration, type, strategies2, epsilon) -> {
			
			Round2Strategy s=(Round2Strategy) strategies2.get(0);
			
		//update utilities in Round1 mechanism
			contextRound1.setMechanism(new DutchFPSB3Round1(efficiency,contextRound1,contextRound2.getUtilities()));
		// cache strategies
			cache.cacheStrategies(strategies2,contextRound1.getHistories());
			
			//write out equilibrium strategies for second round
			String s2win = writer2.write(s, true);
			Path outputFile2win = Paths.get(args[3]).resolve(String.format("iter%03d won.strats", iteration));

			try {
				Files.write(
					outputFile2win, s2win.getBytes(), 
					StandardOpenOption.CREATE, 
					StandardOpenOption.WRITE, 
					StandardOpenOption.TRUNCATE_EXISTING
				);
			} catch (IOException e) {
			}
			String s2lost = writer2.write(s, false);
			Path outputFile2lost = Paths.get(args[3]).resolve(String.format("iter%03d lost.strats", iteration));

			try {
				Files.write(
					outputFile2lost, s2lost.getBytes(), 
					StandardOpenOption.CREATE, 
					StandardOpenOption.WRITE, 
					StandardOpenOption.TRUNCATE_EXISTING
				);
			} catch (IOException e) {
			}

			String s2Wonbelief = writer2.write(contextRound1.histories);
			Path outputFile2Wonbelief = Paths.get(args[3]).resolve(String.format("iter%03d BelWon.strats", iteration));

			try {
				Files.write(
						outputFile2Wonbelief, s2Wonbelief.getBytes(), 
					StandardOpenOption.CREATE, 
					StandardOpenOption.WRITE, 
					StandardOpenOption.TRUNCATE_EXISTING
				);
			} catch (IOException e) {
			}	
		};
		//set callbacks
		bneAlgoRound2.setCallback(callbackRound2);
		pbeAlgo.setCallback(callbackAfterIt);
		pbeAlgo.setCallback2(callbackMidIt);
		
		PBEAlgorithm.Result<Double,Double[]> result;
		result = pbeAlgo.run();
		
		//time measurement
		long elapsedTime = System.nanoTime() - startTime;
	     
		StringBuilder builder = new StringBuilder();
		builder.append("time =");
        builder.append(elapsedTime/1000000);
        builder.append("\n");
		builder.append("efficiency =");
		builder.append(efficiency);
        String s = builder.toString();
		Path output = Paths.get(args[4]).resolve(String.format("setup.txt"));

		try {
			Files.write(
				output, s.getBytes(), 
				StandardOpenOption.CREATE, 
				StandardOpenOption.WRITE, 
				StandardOpenOption.TRUNCATE_EXISTING
			);
		} catch (IOException e) {
		}
		

        System.out.println(s.toString());
	}

}
