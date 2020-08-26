package ch.uzh.ifi.ce.cabne.examples;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import ch.uzh.ifi.ce.cabne.BR.BR1Dto2DCalculator;
import ch.uzh.ifi.ce.cabne.algorithm.BNEAlgorithm;
import ch.uzh.ifi.ce.cabne.algorithm.BNEAlgorithmCallback;
import ch.uzh.ifi.ce.cabne.algorithm.BNESolverContext;
import ch.uzh.ifi.ce.cabne.domains.FirstPriceSplitAward.FirstPriceSplitAward;
import ch.uzh.ifi.ce.cabne.domains.FirstPriceSplitAward.FirstPriceSplitAwardSampler;
import ch.uzh.ifi.ce.cabne.integration.MCIntegratorSplit;
import ch.uzh.ifi.ce.cabne.pointwiseBR.PatternSearch;
import ch.uzh.ifi.ce.cabne.pointwiseBR.BoxPattern2D;
import ch.uzh.ifi.ce.cabne.pointwiseBR.updateRule.MultivariateDampenedUpdateRule;
import ch.uzh.ifi.ce.cabne.randomsampling.CommonRandomGenerator;
import ch.uzh.ifi.ce.cabne.strategy.PWLStrategy1Dto2D;
import ch.uzh.ifi.ce.cabne.verification.BoundingVerifier1Dto2D;


public class FPSBSplitAward {
	//Run configuration
	//args[0] is the location of config file
	
	//this is just a single-round setting to reproduce findings from Anton & Yao
	public static void main(String[] args) throws InterruptedException, IOException{
		// TODO Auto-generated method stub
	
		// create context and read config
		BNESolverContext<Double, Double[]> context = new BNESolverContext<>();
		String configfile = args[0];
		context.parseConfig(configfile);
		
		// initialize all algorithm pieces
		context.setOptimizer(new PatternSearch<>(context, new BoxPattern2D()));
		context.setIntegrator(new MCIntegratorSplit<>(context));
		context.setRng(1, new CommonRandomGenerator(1));
		context.setUpdateRule(new MultivariateDampenedUpdateRule(0.2, 0.7, 0.5 / context.getDoubleParameter("epsilon"), true));
		context.setBRC(new BR1Dto2DCalculator(context));
		context.setOuterBRC(new BR1Dto2DCalculator(context));
		context.setVerifier(new BoundingVerifier1Dto2D(context));		
		
		// instanciate auction setting
		double efficiency=0.25; //public cost efficiency parameter C that determines cost of split award.
		context.setMechanism(new FirstPriceSplitAward(efficiency));
		context.setSampler(new FirstPriceSplitAwardSampler(context));
		
		BNEAlgorithm<Double, Double[]> bneAlgo = new BNEAlgorithm<>(2, context);
		
		// add bidders
		bneAlgo.setInitialStrategy(0, PWLStrategy1Dto2D.makeTruthful(1.0, 2.0, efficiency)); 
		bneAlgo.makeBidderSymmetric(1, 0);

		// create callback that prints out the local and global players' strategies after each iteration, and also forces the strategies to be monotone
//		SplitAwardStrategyWriter writer = new SplitAwardStrategyWriter();
		
		BNEAlgorithmCallback<Double, Double[]> callback = (iteration, type, strategies, epsilon) -> {
            // print out strategy
/*			System.out.format("iteration: %d, type %s, epsilon %7.6f\n", iteration, type, epsilon);
			String s = writer.write(strategies, iteration, epsilon);
			Path outputFile = Paths.get(args[1]).resolve(String.format("iter%03d.strats", iteration));

			try {
				Files.write(
					outputFile, s.getBytes(), 
					StandardOpenOption.CREATE, 
					StandardOpenOption.WRITE, 
					StandardOpenOption.TRUNCATE_EXISTING
				);
			} catch (IOException e) {
			}
			
*/			
            StringBuilder builder = new StringBuilder();
            builder.append(String.format("%2d", iteration));
            builder.append(String.format(" %7.6f  ", epsilon));

            int ngridpoints = 1000;
            for (int i=0; i<=ngridpoints; i++) {
            	double maxValue =strategies.get(0).getMaxValue();
            	double minValue = strategies.get(0).getMinValue();
                double v = minValue+(maxValue-minValue) * i / ngridpoints;
                builder.append(String.format("%5.4f",v));
                builder.append(" ");
                builder.append(String.format("%5.4f", strategies.get(0).getBid(v)[0]));
                builder.append(" ");
                builder.append(String.format("%5.4f", strategies.get(0).getBid(v)[1]));
                builder.append("  ");
            }

            System.out.println(builder.toString());
       };
		bneAlgo.setCallback(callback);
		
		BNEAlgorithm.Result<Double, Double[]> result;
		result = bneAlgo.run();
	}

}
