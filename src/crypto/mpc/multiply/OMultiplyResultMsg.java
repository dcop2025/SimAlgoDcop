package crypto.mpc.multiply;

import crypto.mpc.interfaces.*;
import crypto.utils.OModMath;
import crypto.utils.shamir.Shared;

public class OMultiplyResultMsg implements IMessage{
	
	
	private String contextKey;
	private String cKey;
	private long   c_r; 
	
	public OMultiplyResultMsg(String contextKey, String cKey, long c_r) {
		this.contextKey = contextKey;
		this.cKey = cKey;
		this.c_r = c_r;
	}

	@Override
	public void action(IShamirAgent agent, int senderID) {
		long prime = agent.prime();
		Shared r = agent.shared("r-key");
		if (r == null) {
			// ERROR HERE
			r = new Shared(agent.agentID(), 1, 1);
		}

		Shared c = new Shared(agent.agentID(), OModMath.sub(c_r, r.share(), prime), OModMath.sub(c_r, r.real(), prime));
		agent.storeShared(cKey, c);
		
		OMultiplyResultAckMsg reply = new OMultiplyResultAckMsg(contextKey);
		
		agent.SendMsg(senderID, reply);
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
