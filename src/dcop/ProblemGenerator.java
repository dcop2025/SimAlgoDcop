package dcop;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ProblemGenerator {
	
	private int n;
	private double networkDensity;
	private int minCost;
	private int maxCost;
	
	private Random random;

	private Map<Integer, AgentConfiguration> agents;
	
	class AgentConfiguration {
		private int range;
		
		AgentConfiguration(int range) {
			this.range = range;
		}
		
		public int range() {
			return this.range;
		}
	} 
	
	public ProblemGenerator(int n, double d, int minRange, int maxRange, int minCost, int maxCost, long seed) {
		this.n = n;
		this.networkDensity = d;
		this.minCost = minCost;
		this.maxCost = maxCost;
		if (seed == 0L) {
			this.random = new Random();
		} else {
			this.random = new Random(seed);
		}
		
		
		this.agents = new HashMap<Integer, AgentConfiguration>();
		for (int i = 1; i <= n; i++) {
			int range = this.random.nextInt(maxRange - minRange + 1) + minRange;
			this.agents.put(i, new AgentConfiguration(range));
		}
	}
	
	public Problem Generate() {
		Problem problem = new Problem();
						
		// Loop over all pairs
		for (int i = 1; i <= n; i++) {
			for (int j = i + 1; j <= n; j++) {
				Problem.ConstraintsMatrix matrix = GroomAgents(problem, i, j);
				problem.addConstraints(matrix);
			}
		}
		
		return problem;
	}
	
	private Problem.ConstraintsMatrix GroomAgents(Problem problem, int a, int b) {
		int n = agents.get(a).range();
		int m = agents.get(b).range();
		boolean zero = (networkDensity < this.random.nextFloat());
		int factor =  zero ? 0 : 1;
		
		Problem.ConstraintsMatrix constraints = problem.new ConstraintsMatrix(a, b, n, m, zero);

	    for (int i = 0; i < n; i++) {
	    	for (int j = 0; j < m; j++) {
	    		constraints.matrix[i][j] = (this.random.nextInt(maxCost - minCost + 1) + minCost) * factor;
	    	}
	    }
	    
	    return constraints; 
	}

}
