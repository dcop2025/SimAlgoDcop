package dcop.pdsa;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import projects.dcopsim.Network;
import dcop.Problem;
import dcop.Problem.ConstraintsMatrix;
import crypto.utils.shamir.Generater;
import crypto.utils.shamir.VectorGenerater;
import sinalgo.runtime.Global;

public class NetworkGenerator {

	public static Network gererate(Problem problem, int totalRound, double roundThreshold, long algoSeed, long cryptoSeed) {
		Network network = new Network();
		
		
		long p = getGlobalPrime();
		Global.log.logln(true, "Selected P:" + p);
				
		Iterator<ConstraintsMatrix> constraintsIter = problem.iterator();
		Map<Integer, Agent> agents = new HashMap<Integer, Agent>();
		
		while (constraintsIter.hasNext()) {
			ConstraintsMatrix constraints = constraintsIter.next();
/*
			if (constraints.zero()) {
				continue;
			}*/
			Agent agentA = agents.get(constraints.a);			
			if (agentA == null) {				
				agentA = new Agent(constraints.a, constraints.domainPowerAgentA(), roundThreshold, p, algoSeed, cryptoSeed);
				agents.put(constraints.a, agentA);
				network.agents.add(agentA);
			}
			
			Agent agentB = agents.get(constraints.b);			
			if (agentB == null) {				
				agentB = new Agent(constraints.b, constraints.domainPowerAgentB(), roundThreshold, p, algoSeed, cryptoSeed);
				agents.put(constraints.b, agentB);
				network.agents.add(agentB);
			}

			agentA.addConnectionConstraints(agentB, constraints.matrix);
			agentB.addConnectionConstraints(agentA, constraints.revMatrix());
		}

		Random random = new Random(18);
		int shamirThreshold = (int) Math.floor((double)(agents.size()+1)/2);
		int secret = 1;
		int[] secretBits = bitme(secret); 
		Generater rGen = new Generater(secret, shamirThreshold, p, random);
		VectorGenerater rBitGen = new VectorGenerater(secretBits, shamirThreshold, p, random);
		
		for (int key : agents.keySet()) {
			Agent agent = agents.get(key); 
			agent.InjectRef("r-key", rGen.generate(agent.ID), rBitGen.generate(agent.ID));
		}
		
		
		return network;
	}

	
    private static int[] bitme(int num) {
        int[] bits = new int[31]; // Array to hold the 31 bits
        for (int i = 0; i < 31; i++) {
            bits[i] = (num >> i) & 1; // Extract the i-th bit
        }
        return bits;
    }
	
	private static long getGlobalPrime() {
		return (long) Math.pow(2, 31) - 1;
		//return 97;
		//return BigInteger.valueOf((long)Math.pow(2, 31) - 1);
//		return BigInteger.valueOf(97);
		//SecureRandom sr = new SecureRandom();
		//BigInteger prime = BigInteger.probablePrime(1000, sr);
		//return prime;
	}

}



