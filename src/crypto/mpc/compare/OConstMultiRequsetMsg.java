package crypto.mpc.compare;

import crypto.mpc.interfaces.*;
import crypto.utils.OModMath;
import crypto.utils.shamir.Shared;

public class OConstMultiRequsetMsg implements IMessage{
	
	
	private String contextKey;
	private String aKey;
	private int value;
	private String oKey;
	
	public OConstMultiRequsetMsg(String contextKey, String aKey, int value, String oKey) {
		this.contextKey = contextKey;
		this.aKey = aKey;
		this.value = value;
		this.oKey = oKey;
		
	}

	@Override
	public void action(IShamirAgent agent, int senderID) {
		long prime = agent.prime();		
		Shared a = agent.shared(aKey);						
		Shared o = new Shared(agent.agentID(), OModMath.multiply(a.share(), value, prime) , OModMath.sub(a.real(), value, prime));
		agent.storeShared(oKey, o);

		OConstMultiRequsetAckMsg ackMsg = new OConstMultiRequsetAckMsg(contextKey);
		agent.SendMsg(senderID, ackMsg);		
	}

	@Override
	public IMessage clone() {
		// TODO Auto-generated method stub
		return this;
	}

}
