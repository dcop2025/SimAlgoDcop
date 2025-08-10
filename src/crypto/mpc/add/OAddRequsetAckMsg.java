package crypto.mpc.add;

import crypto.mpc.interfaces.*;
import crypto.utils.shamir.Shared;

public class OAddRequsetAckMsg implements IMessage{
	
	private String contextKey;
	private Shared c_2t_r;

	public OAddRequsetAckMsg(String contextKey) {
		this.contextKey = contextKey;
	} 
	
	@Override
	public void action(IShamirAgent agent, int senderID) {
		// TODO: P2 shmuelg find a better avoid this casting
		IContext dummy = agent.context(contextKey);
		OSecureAddCtx ctx = (OSecureAddCtx) dummy;
	
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
