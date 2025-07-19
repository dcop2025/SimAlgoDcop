package crypto.mpc.compare;

import crypto.mpc.interfaces.*;
import crypto.utils.OModMath;
import crypto.utils.shamir.Shared;

public class OOneSubRequsetMsg implements IMessage{
	
	
	private String contextKey;
	private String aKey;
	private String bKey;
	
	public OOneSubRequsetMsg(String contextKey, String aKey, String bKey) {
		this.contextKey = contextKey;
		this.aKey = aKey;
		this.bKey = bKey;
		
	}

	@Override
	public void action(IShamirAgent agent, int senderID) {
		long prime = agent.prime();		
		Shared a = agent.shared(aKey);						
		Shared b = new Shared(agent.agentID(), OModMath.sub(1, a.share(), prime) , OModMath.sub(1, a.real(), prime));
		agent.storeShared(bKey, b);

		OOneSubRequsetAckMsg ackMsg = new OOneSubRequsetAckMsg(contextKey);
		agent.SendMsg(senderID, ackMsg);		
	}

	@Override
	public IMessage clone() {
		// TODO Auto-generated method stub
		return this;
	}


}
