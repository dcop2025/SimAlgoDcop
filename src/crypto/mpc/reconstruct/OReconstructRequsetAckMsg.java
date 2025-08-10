package crypto.mpc.reconstruct;

import crypto.mpc.interfaces.*;


import crypto.utils.shamir.Shared;

public class OReconstructRequsetAckMsg implements IMessage {
	
	
	public interface IReconstructCtx {
		void collectReconstruct(String key, Shared s);
	}

	
	private String contextKey;
	private String key;
	private Shared shared;

	public OReconstructRequsetAckMsg(String contextKey, String key, Shared shared) {		
		this.contextKey = contextKey;
		this.key = key;
		this.shared = shared;
	}
	
	@Override
	public void action(IShamirAgent agent, int senderID) {
		IContext dummy = agent.context(contextKey);
		IReconstructCtx ctx = (IReconstructCtx) dummy;
	
		ctx.collectReconstruct(key, shared);

	}

	@Override
	public IMessage clone() {
		// TODO Auto-generated method stub
		return this;
	}


}
