package crypto.mpc.multiply;

import crypto.mpc.interfaces.*;
import crypto.utils.OModMath;
import crypto.utils.shamir.Shared;

public class OMultiplyRequsetMsg implements IMessage{
	
	
	private String contextKey;
	private String aKey;
	private String bKey;
	
	public OMultiplyRequsetMsg(String contextKey, String aKey, String bKey) {
		this.contextKey = contextKey;
		this.aKey = aKey;
		this.bKey = bKey;
	}

	@Override
	public void action(IShamirAgent agent, int senderID) {
		long prime = agent.prime();		
		Shared a = agent.shared(aKey);
		Shared b = agent.shared(bKey);
		Shared r = agent.shared("r-key");
		if (r == null) {
			// ERROR HERE
			r = new Shared(agent.agentID(), 1, 1);
		}
								
		Shared c_2t = new Shared(agent.agentID(), OModMath.multiply(a.share(), b.share(), prime) , OModMath.multiply(a.real(), b.real(), prime));
		Shared c_2t_r = c_2t.add(r, prime);
		
		OMultiplyRequsetAckMsg ackMsg = new OMultiplyRequsetAckMsg(contextKey, c_2t_r);
		agent.SendMsg(senderID, ackMsg);		
	}

	public IMessage clone() {
		return this;
	}


	/*
	@Override
	public Message clone() {
		// TODO Auto-generated method stub
		return this;
	}
	*/

}
