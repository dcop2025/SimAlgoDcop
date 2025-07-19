package crypto.mpc.compare;

import crypto.mpc.interfaces.*;
import crypto.utils.OModMath;
import crypto.utils.shamir.Shared;

public class OPrepLSBRequestMsg implements IMessage{
	
	
	private String contextKey;
	private String aKey;
	private String refKey;
	
	public OPrepLSBRequestMsg(String contextKey, String aKey, String refKey) {
		this.contextKey = contextKey;
		this.aKey = aKey;
		this.refKey = refKey;
		
	}

	@Override
	public void action(IShamirAgent agent, int senderID) {
		long prime = agent.prime();		
		Shared a = agent.shared(aKey);
		Shared ref = agent.shared(refKey);
		//Shared o = new Shared(agent.agentID(), OModMath.multiply(a.share(), ref.share(), prime) , OModMath.sub(a.real(), ref.real(), prime));
		Shared o = new Shared(agent.agentID(), OModMath.add(a.share(), ref.share(), prime) , OModMath.add(a.real(), ref.real(), prime));
		
		OPrepLSBRequesAcktMsg ackMsg = new OPrepLSBRequesAcktMsg(contextKey, o);
		agent.SendMsg(senderID, ackMsg);		
	}

	@Override
	public IMessage clone() {
		// TODO Auto-generated method stub
		return this;
	}

}
