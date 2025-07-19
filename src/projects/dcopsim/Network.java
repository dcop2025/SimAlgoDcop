package projects.dcopsim;

import java.util.Vector;

public class Network {
	public Vector<IDcopAgent> agents;
	
	public Network() {
		agents = new Vector<IDcopAgent>();
	}

	
	public void ConnectAgent(int leftAgent, int rightAgent) {
		IDcopAgent agentA = agents.get(leftAgent);
		IDcopAgent agentB = agents.get(rightAgent);
		
		agentA.connect(agentB);
	}

}
