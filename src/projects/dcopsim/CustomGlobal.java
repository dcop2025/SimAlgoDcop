/*
 Copyright (c) 2007, Distributed Computing Group (DCG)
                    ETH Zurich
                    Switzerland
                    dcg.ethz.ch

 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 - Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the
   distribution.

 - Neither the name 'Sinalgo' nor the names of its contributors may be
   used to endorse or promote products derived from this software
   without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package projects.dcopsim;


import java.util.HashMap;
import java.util.Map;


import dcop.Problem;
import dcop.ProblemGenerator;
import dcop.ScaleFreeGenerator;
import crypto.utils.OModMath;
import crypto.utils.Paillier;
import crypto.utils.shamir.Generater;
import crypto.utils.shamir.PowersStash;
import crypto.utils.shamir.ShamirSharedGen;
import crypto.utils.shamir.Shared;
import projects.defaultProject.models.distributionModels.Random;
import sinalgo.nodes.Node;
import sinalgo.runtime.AbstractCustomGlobal;
import sinalgo.runtime.Global;
import sinalgo.runtime.Runtime;
import sinalgo.tools.Tools;
import sinalgo.tools.logging.LogL;

/**
 * This class holds customized global state and methods for the framework. 
 * The only mandatory method to overwrite is 
 * <code>hasTerminated</code>
 * <br>
 * Optional methods to override are
 * <ul>
 * <li><code>customPaint</code></li>
 * <li><code>handleEmptyEventQueue</code></li>
 * <li><code>onExit</code></li>
 * <li><code>preRun</code></li>
 * <li><code>preRound</code></li>
 * <li><code>postRound</code></li>
 * <li><code>checkProjectRequirements</code></li>
 * </ul>
 * @see sinalgo.runtime.AbstractCustomGlobal for more details.
 * <br>
 * In addition, this class also provides the possibility to extend the framework with
 * custom methods that can be called either through the menu or via a button that is
 * added to the GUI. 
 */
public class CustomGlobal extends AbstractCustomGlobal{
	
	

	public void preRound() {
		// Stub
	}
		
	////////////
	// consts //
	////////////
	
	// SEEDs
	public static final long INIT_PG_SEED      = 1001L; // Seed to problem generator
	public static final long INIT_NETWORD_SEED = 2001L; // Seed to network generator
	public static final long INIT_ALGO_SEED    = 3001L; // Seed to pass for an agent for algorithm operations
	public static final long INIT_CRYPTO_SEED  = 4001L; // Seed to pass for an agent for crypto operations
	
	
	// Test configuration default values
	public static final int  INIT_TOTAL_TEST_ITERATIONS = 50; // Default value for number of iterations
	
	// test durations 
	public static final int  ONE_MIN    =  60000;
	public static final int  TWO_MINS   = 120000;
	public static final int  THREE_MINS = 180000;

	
	// AgentType tells which agent type exists in the network
	// we should strive to delete this one
	// TODO: shmuelg strive to delete this
	 enum AgentType {
		 	NONE,
		 	DSA,
		    PDSA,
		    PMAXSUM,
		    MAXSUM,
		  }
	private AgentType agentType;
	
	////////////////////////
	// Test configuration //
	////////////////////////
	
	class TestConfiguration {
		int agents;
		int domainSize;
		double nd;
		double durtion;
		int totalCost;

		public TestConfiguration(int agents, int domain, double nd, double durtion) {
			this.agents = agents;
			this.domainSize = domain;
			this.nd = nd;
			this.durtion = durtion;
			this.totalCost = 0;
		}
	}

	private TestConfiguration getTestConfiguration(int testIteration) {
		if (testIteration == 0) {
			return new TestConfiguration(10, 10 , 0.4, ONE_MIN);	
		}
		return null;
	}
	
	/////////////////////
	// Buttons Section //
	/////////////////////	

	@AbstractCustomGlobal.CustomButton(buttonText="Trigger Max Sum full flow", toolTipText="Trigger Max sum")
	public void TriggerMaxSum() {
		// Always start with debug statement
		Global.log.logln(LogL.ALWAYS, "Trigger MaxSum");
		String javeVersion = System.getProperty("java.version");
		Global.log.logln(LogL.ALWAYS, "javeVersion: " + javeVersion);
			
		startTestingMaxSum();
	}

	@AbstractCustomGlobal.CustomButton(buttonText="Trigger DSA flow", toolTipText="Trigger DSA")
	public void TriggerDSA() {
		// Always start with debug statement
		Global.log.logln(LogL.ALWAYS, "Trigger DSA");
		String javeVersion = System.getProperty("java.version");
		Global.log.logln(LogL.ALWAYS, "javeVersion: " + javeVersion);
			
		startTestingDSA();
	}

	@AbstractCustomGlobal.CustomButton(buttonText="Trigger P-DSA flow", toolTipText="Trigger DSA")
	public void TriggerPDSA() {
		// Always start with debug statement
		Global.log.logln(LogL.ALWAYS, "Trigger PDSA");
		String javeVersion = System.getProperty("java.version");
		Global.log.logln(LogL.ALWAYS, "javeVersion: " + javeVersion);
			
		startTestingPDSA();
	}

	@AbstractCustomGlobal.CustomButton(buttonText="Test MPC", toolTipText="Test MPC")
	public void TriggerMPCTest() {
		// Always start with debug statement
		Global.log.logln(LogL.ALWAYS, "Trigger MPC Testing");
		String javeVersion = System.getProperty("java.version");
		Global.log.logln(LogL.ALWAYS, "javeVersion: " + javeVersion);
			
		TriggerMPC();
	}

	/////////////////////
	// tests main loop //
	/////////////////////	

	private int testConfigIndex;
	private int totalTestIterations;
	private int testIteration;
	
	// stats
	private long startTime;
	private int accumulateRounds;
	private int accumulateCost;
	private long accumulateDuration;

	private void startTestingMaxSum() {
		// TODO: P2 shmuelg strive to remove this
		agentType = AgentType.MAXSUM;

		testConfigIndex = 0;
		initSettingForNewTestConfig();		
		runTestIteration();
	}

	private void startTestingDSA() {
		// TODO: P2 shmuelg strive to remove this
		agentType = AgentType.DSA;

		testConfigIndex = 0;
		initSettingForNewTestConfig();		
		runTestIteration();
	}

	private void startTestingPDSA() {
		// TODO: P2 shmuelg strive to remove this
		agentType = AgentType.PDSA;

		testConfigIndex = 0;
		initSettingForNewTestConfig();		
		runTestIteration();
	}

	private void initSettingForNewTestConfig() {
		// reset/init seeds
		pgSeed = INIT_PG_SEED;
		networkSeed = INIT_NETWORD_SEED;
		cryptoSeed = INIT_CRYPTO_SEED;	
		// reset/init iteration
		totalTestIterations = INIT_TOTAL_TEST_ITERATIONS;
		testIteration = 0;
		// reset/init status
		accumulateRounds = 0;
		accumulateCost = 0;
		accumulateDuration = 0;
		PowersStash.getInstance();
	}
	
	// static cost range
	int minCost = 0;
	int maxCost = 10;
	// seeds for generating problem/network
	long pgSeed = INIT_PG_SEED;
	long networkSeed = INIT_NETWORD_SEED;
	long algoSeed = INIT_ALGO_SEED;
	long cryptoSeed = INIT_CRYPTO_SEED;
	
	
	TestConfiguration currentConfiguration;
	Problem problem; 
	Network network;

	private boolean runTestIteration( ) {
		boolean moreTesting = checkNextIteration();
		if (!moreTesting) {
			return false;
		}
		buildNetwork();
		triggerRun();
		return true;
	}
	
	private boolean checkNextIteration() {
		testIteration++;		
		if (testIteration > totalTestIterations) {
			Global.log.logln(LogL.ALWAYS, String.format("Ran %d iterations, Done with this configuration, moving to the next configuration", totalTestIterations));
			testConfigIndex++;
			testIteration = 0;
			return false;
		}
		Global.log.logln(LogL.ALWAYS, String.format("Running test iteration %d/%d of configuration %s", testIteration, totalTestIterations, testConfigIndex));
				
		currentConfiguration = getTestConfiguration(testConfigIndex);
		if (currentConfiguration == null) {
			Global.log.logln(LogL.ALWAYS, String.format("no test configuration was found"));
			return false;
		}

		return true;
	}
	
	private void buildNetwork() {
		// TODO P3 shmuelg make this switchable with other problem generator 	//ScaleFreeGenerator
		pgSeed++;
		ProblemGenerator pg = new ProblemGenerator(
				currentConfiguration.agents, 
				currentConfiguration.nd, 
				currentConfiguration.domainSize, 
				currentConfiguration.domainSize, 
				minCost, maxCost, pgSeed);
		problem = pg.Generate();
		
		// TODO P1: avoid this switch case
		switch (agentType) {
	    case MAXSUM:
	    	//deployNetwork(crypto.dcop.maxsum.vanilla.NetworkGenerator.gererate2(problem, false));	    	
	        break;
	        // TODO P3 add mode agent type here
	    case DSA:
	    	//deployNetwork(crypto.dcop.dsa.vanilla.NetworkGenerator.gererate2(problem, false, algoSeed));
	    	break;
	    case PDSA:
	    	//deployNetwork(crypto.dcop.dsa.secure.NetworkGenerator.gererate2(problem, 100, 0.7, networkSeed));
	    	deployNetwork(dcop.pdsa.NetworkGenerator.gererate(problem, 100, 0.7, algoSeed, cryptoSeed));
	    	break;
	    default:
			Global.log.logln(LogL.ALWAYS, String.format("agent type is invalid %s", agentType));
			// TODO P4: shmuelg handle error here
		}		
	}
	
	
	private void deployNetwork(Network network) {
		this.network = network;

		Random random = new Random();
		for (IDcopAgent agent: network.agents) {
			// TODO P2: change IDcopAgent to DcopNode
			Node node = (Node) agent;
			node.setPosition(random.getNextPosition());
			node.finishInitializationWithDefaultModels(true);
			Runtime.addNode(node);			
		}
		Tools.repaintGUI();

		for (IDcopAgent agent: network.agents) {
			agent.logState();
		}
		
		// TODO shmuelg delete this
		//startTime = System.currentTimeMillis();
		//for (IDcopAgent agent: network.agents) {
		//	agent.trigger();
		//}
	}
	
	private void triggerRun( ) {
		startTime = System.currentTimeMillis();
		for (IDcopAgent agent: network.agents) {
			agent.trigger();
		}		
	}

	
	/* (non-Javadoc)
	 * @see runtime.AbstractCustomGlobal#hasTerminated()
	 */
	public boolean hasTerminated() {
		// in some sense this function acts as the main loop of the flow.
		// it's triggered every cycle and return if there is nothing left to do (stop running/terminated)
		
		
		// if there is no network, than there is nothing to do
		if (network == null) {
			return true;
		}
		
		// check if the test is still running
		if (isTestRunning()) {
			return false; // still running, no terminated 
		}
		
		// Test is over lets, calc some stats on it
		long endTime = System.currentTimeMillis();
        long duration = endTime - startTime; // Time in milliseconds
        System.out.println("Execution time: " + duration + " milliseconds");
		
		
		// relog for sense of progression
        // TODO what this is accDuration
        accumulateDuration = accumulateDuration + duration;
		updateStats(duration);		
		postTestCleanup();
		
		boolean moreTesting = runTestIteration();
		if( moreTesting) {
			triggerRun();
		}
		return !moreTesting;		
	}

	private boolean isTestRunning() {
		return (!testTimeout() && areAgentStillRunning());
	}
	
	private boolean testTimeout() {
		return false;
		/*
		long endTime = System.currentTimeMillis();
        long duration = endTime - startTime; // Time in milliseconds

        return (currentConfiguration.durtion < duration);
        */
	}
	
	private void updateStats(long duration) {
		Map<Integer, Integer> assigments = new HashMap<Integer, Integer>();
		for (IDcopAgent agent : network.agents) {
			// dummy agent don't have an assignment  
			if (agent.assignment() == -1) {
				continue;
			}
			// store assignment
			Global.log.logln(LogL.ALWAYS, String.format("ID: %d assignment %s", agent.agentID(), agent.assignment()));
			assigments.put(agent.agentID(), agent.assignment());

			// Ensure that all agent finish at least one round
			int around = agent.getInternal("round");
			if (around == 1) {
				Global.log.logln(LogL.ALWAYS, String.format("didn't finish running"));	
			}
			// add rounds to accumulated stats
			if (agent.agentID() == 1) {
				accumulateRounds += (around - 1); 
				Global.log.logln(LogL.ALWAYS, String.format("agent 1: round %d", around));
			}			
		}
		
		// Calc cost
		int cost = problem.solve(assigments);
		accumulateCost = accumulateCost + cost;
		Global.log.logln(LogL.ALWAYS, String.format("Total cost: %d", cost));

		double avgDuration = accumulateDuration/testIteration;
		double avgCost = (double)accumulateCost/(double)testIteration;
		double avgRounds = accumulateRounds/testIteration;
		Global.log.logln(LogL.ALWAYS, String.format("Avg cost: %.2f", avgCost));
		Global.log.logln(LogL.ALWAYS, String.format("Avg accDuration: %.2f", avgDuration));
		Global.log.logln(LogL.ALWAYS, String.format("Avg round: %.2f", avgRounds));

		
	}
	 	
	private boolean areAgentStillRunning() {
		for (IDcopAgent agent : network.agents) {
			// TODO shmuelg P1 change the interface to running
			if (!agent.doneStatus()) {
				return true;
			}
		}
		return false;
	}
		
	private void postTestCleanup() {
		currentConfiguration = null;
		network = null;
		problem = null;
				
		//algoType = AlgoType.NONE;
		Tools.removeAllNodes();
	}

	///////////////////////
	// Test flow Section //
	///////////////////////	

	
	/////////////////////////
	// MPC Testing Section //
	/////////////////////////
	
	
	private void TriggerMPC() {
		PowersStash.getInstance();
		// 1. Create a network
		agentType = AgentType.PDSA;

		testConfigIndex = 0;
		initSettingForNewTestConfig();
		currentConfiguration = getTestConfiguration(testConfigIndex);
		if (currentConfiguration == null) {
			Global.log.logln(LogL.ALWAYS, String.format("ERROR: no test configuration was found"));
			return;
		}

		buildNetwork();
		
		for (IDcopAgent agent: network.agents) {
			agent.triggerCommand("");
		}				
	}
}
 