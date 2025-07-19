package crypto.mpc.testing;

import crypto.mpc.interfaces.*;
import crypto.utils.shamir.Shared;

public class OReconstructRequsetMsg implements IMessage {
	
	private String contextKey;
	private String key;

	public OReconstructRequsetMsg(String contextKey, String key) {
		this.contextKey = contextKey;
		this.key = key;
	}

	@Override
	public IMessage clone() {
		return this;
	}

	@Override
	public void action(IShamirAgent agent, int senderID) {
		// store the shared
		Shared shared = agent.shared(this.key);
		
		OReconstructRequsetAckMsg ackMsg = new OReconstructRequsetAckMsg(contextKey, key, shared);
		agent.SendMsg(senderID, ackMsg);		
	}

}
