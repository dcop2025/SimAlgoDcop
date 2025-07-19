package crypto.mpc.compare;

import crypto.mpc.interfaces.*;
import crypto.utils.OModMath;
import crypto.utils.shamir.Shared;

public class OSubtractionRequsetMsg implements IMessage{
	
	
	private String contextKey;
	private String aKey;
	private String bKey;
	private String cKey;
	
	public OSubtractionRequsetMsg(String contextKey, String aKey, String bKey, String cKey) {
		this.contextKey = contextKey;
		this.aKey = aKey;
		this.bKey = bKey;
		this.cKey = cKey;
		
	}

	@Override
	public void action(IShamirAgent agent, int senderID) {
		long prime = agent.prime();		
		Shared a = agent.shared(aKey);
		Shared b = agent.shared(bKey);
						
		Shared c = new Shared(agent.agentID(), OModMath.sub(a.share(), b.share(), prime) , OModMath.sub(a.real(), b.real(), prime));
		agent.storeShared(cKey, c);

		OSubtractionRequsetAckMsg ackMsg = new OSubtractionRequsetAckMsg(contextKey);
		agent.SendMsg(senderID, ackMsg);		
	}

	@Override
	public IMessage clone() {
		// TODO Auto-generated method stub
		return this;
	}

}
