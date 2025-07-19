package crypto.mpc.testing;

import crypto.mpc.interfaces.*;

import crypto.utils.shamir.Shared;

public class InjectSecretMsg implements IMessage {
		
	private String contextKey;
	private String key;
	private Shared shared;
	
	public InjectSecretMsg(String contextKey, String key, Shared shared) {
		this.contextKey = contextKey;
		this.key = key;
		this.shared = shared;
	}
	
	@Override
	public IMessage clone() {
		return this;
	}
	
	@Override
	public void action(IShamirAgent agent, int senderID) {
		// store the shared
		agent.storeShared(this.key, this.shared);
		InjectSecretAckMsg ackMsg = new InjectSecretAckMsg(contextKey);
		agent.SendMsg(senderID, ackMsg);
	}
	

}
