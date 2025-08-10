package crypto.mpc.sub;

import crypto.mpc.interfaces.*;
import crypto.utils.shamir.Shared;

public class OSubRequsetMsg implements IMessage{
	
	
	private String contextKey;
	private String aKey;
	private String bKey;
	private String oKey;
	
	public OSubRequsetMsg(String contextKey, String aKey, String bKey, String oKey) {
		this.contextKey = contextKey;
		this.aKey = aKey;
		this.bKey = bKey;
		this.oKey = oKey;		
	}

	@Override
	public void action(IShamirAgent me, int senderID) {
		long prime = me.prime();	
		Shared a = me.shared(aKey);
		Shared b = me.shared(bKey);
								
		Shared o = a.sub(b, prime);
		me.storeShared(oKey,  o);
		
		OSubRequsetAckMsg ackMsg = new OSubRequsetAckMsg(contextKey);
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
