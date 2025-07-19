package crypto.mpc.multiply;

import crypto.mpc.interfaces.*;

public class OMultiplyResultAckMsg implements IMessage{
	
	
	private String contextKey;

	public OMultiplyResultAckMsg(String contextKey) {
		this.contextKey = contextKey;
	}
	
	@Override
	public void action(IShamirAgent agent, int senderID) {
		IContext dummy = agent.context(contextKey);
		OSecureMultiplyCtx ctx = (OSecureMultiplyCtx) dummy;
	
		ctx.multiplyDoneAck();
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
