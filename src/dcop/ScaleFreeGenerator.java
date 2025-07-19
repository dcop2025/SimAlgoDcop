package dcop;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.*;

public class ScaleFreeGenerator {

	private int n;
	private int minCost;
	private int maxCost;
	private int initClique;
	private int addition;
	
	private Random random;

	private Map<Integer, AgentConfiguration> agents;
	
	class AgentConfiguration {
		public AgentConfiguration(int range) {
			this.range = range;
			this.degree = 0;
		}
		
		public int range;
		public int degree;		
	} 
	
	public ScaleFreeGenerator(int n, int initClique, int addition, int minRange, int maxRange, int minCost, int maxCost, long seed) {
		this.n = n;
		this.initClique = initClique;
		this.addition = addition;
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
			
		List<Integer> nodePool = new ArrayList<Integer>();
		
		// Build the first clique
		for (int i = 1; i <= this.initClique; i++) {
			for (int j = i + 1; j <= this.initClique; j++) {
				Problem.ConstraintsMatrix matrix = GroomAgents(problem, i, j, true);
				problem.addConstraints(matrix);
				agents.get(i).degree++;
				agents.get(j).degree++;
				nodePool.add(i);
				nodePool.add(j);
			}
		}
		
		for (int agent = this.initClique +1; agent <= n; agent++) {
			Set<Integer> connectedNodes = new HashSet<Integer>();			
			for (int addition = 0; addition < this.addition; addition++) {
				while (true) {
					int otherAgent = nodePool.get(this.random.nextInt(nodePool.size()));
					if (connectedNodes.contains(otherAgent) || (otherAgent == agent)) {
						continue; // already connected, retry another node 
					}
					Problem.ConstraintsMatrix matrix = GroomAgents(problem, agent, otherAgent, true);
					problem.addConstraints(matrix);
					agents.get(agent).degree++;
					agents.get(otherAgent).degree++;
					nodePool.add(agent);
					nodePool.add(otherAgent);					
					connectedNodes.add(otherAgent);
					break;
				}
			}
			
			for (int otherAgent = 1; otherAgent < agent; otherAgent++) {
				if (connectedNodes.contains(otherAgent)) {
					continue; // already connected, retry another node 
				}
				Problem.ConstraintsMatrix matrix = GroomAgents(problem, agent, otherAgent, false);
				problem.addConstraints(matrix);
				
			}
			
		}
		
		// TODO enptr
		return problem;
	}
	
	private Problem.ConstraintsMatrix GroomAgents(Problem problem, int a, int b, boolean connected) {
		int n = agents.get(a).range;
		int m = agents.get(b).range;
		int factor =  connected ? 0 : 1;
		
		Problem.ConstraintsMatrix constraints = problem.new ConstraintsMatrix(a, b, n, m, connected);

	    for (int i = 0; i < n; i++) {
	    	for (int j = 0; j < m; j++) {
	    		constraints.matrix[i][j] = (this.random.nextInt(maxCost - minCost + 1) + minCost) * factor;
	    	}
	    }
	    
	    return constraints; 
	}

}
