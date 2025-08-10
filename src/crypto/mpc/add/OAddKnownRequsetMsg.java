package crypto.mpc.add;

import crypto.mpc.interfaces.*;
import crypto.utils.shamir.Shared;

public class OAddKnownRequsetMsg implements IMessage{
	
	
	private String contextKey;
	private int aValue;
	private String bKey;
	private String oKey;
	
	public OAddKnownRequsetMsg(String contextKey, int aValue, String bKey, String oKey) {
		this.contextKey = contextKey;
		this.aValue = aValue;
		this.bKey = bKey;
		this.oKey = oKey;		
	}

	@Override
	public void action(IShamirAgent me, int senderID) {
		long prime = me.prime();	
		Shared a = new Shared(me.agentID(), aValue, aValue);
		Shared b = me.shared(bKey);
								
		Shared o = a.add(b, prime);
		me.storeShared(oKey,  o);
		
		OAddRequsetAckMsg ackMsg = new OAddRequsetAckMsg(contextKey);
		me.SendMsg(senderID, ackMsg);		
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
