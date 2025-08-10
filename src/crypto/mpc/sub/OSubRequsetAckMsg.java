package crypto.mpc.sub;

import crypto.mpc.interfaces.*;
import crypto.utils.shamir.Shared;

public class OSubRequsetAckMsg implements IMessage{
	
	private String contextKey;
	private Shared c_2t_r;

	public OSubRequsetAckMsg(String contextKey) {
		this.contextKey = contextKey;
	} 
	
	@Override
	public void action(IShamirAgent agent, int senderID) {
		// TODO: P2 shmuelg find a better avoid this casting
		IContext dummy = agent.context(contextKey);
		OSecureSubCtx ctx = (OSecureSubCtx) dummy;
	
		ctx.collectResult(senderID);
		
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
