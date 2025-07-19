package crypto.mpc.multiply;

import crypto.mpc.interfaces.*;
import crypto.utils.shamir.Shared;

public class OMultiplyRequsetAckMsg implements IMessage{
	
	private String contextKey;
	private Shared c_2t_r;

	public OMultiplyRequsetAckMsg(String contextKey, Shared c_2t_r) {
		this.contextKey = contextKey;
		this.c_2t_r = c_2t_r;
	} 
	
	@Override
	public void action(IShamirAgent agent, int senderID) {
		// TODO: P2 shmuelg find a better avoid this casting
		IContext dummy = agent.context(contextKey);
		OSecureMultiplyCtx ctx = (OSecureMultiplyCtx) dummy;
	
		ctx.collectResult(senderID, c_2t_r);
		
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
