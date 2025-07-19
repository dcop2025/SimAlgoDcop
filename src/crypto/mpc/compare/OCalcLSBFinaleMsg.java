package crypto.mpc.compare;

import crypto.mpc.interfaces.*;
import crypto.utils.shamir.Shared;

public class OCalcLSBFinaleMsg implements IMessage{
	
	
	private String contextKey;
	private String oKey;
	
	public OCalcLSBFinaleMsg(String contextKey, String oKey) {
		this.contextKey = contextKey;
		this.oKey = oKey;
	}

	@Override
	public void action(IShamirAgent agent, int senderID) {
		long prime = agent.prime();
		
		
		Shared e = agent.shared(contextKey+"-f");
		Shared d0 = agent.shared(bitKey(contextKey+"-d", 30));		
		Shared ed0 = agent.shared(contextKey+"-ed0");
		Shared ed0x2 = ed0.constMultiply(2, prime);
		Shared x0 = e.add(d0, prime).sub(ed0x2, prime);
		
		agent.storeShared(oKey, x0);
		
		OCalcLSBFinaleAckMsg ack = new OCalcLSBFinaleAckMsg(contextKey);
		agent.SendMsg(senderID, ack);		
	}
		
	static private String bitKey(String key, int index) {
		return String.format("%s(%d)", key, index);
	}

	public IMessage clone() {
		// TODO Auto-generated method stub
		return this;
	}


}
