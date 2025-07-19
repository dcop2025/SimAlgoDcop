package crypto.mpc.compare;

import crypto.mpc.interfaces.*;

public class OConstMultiRequsetAckMsg implements IMessage{
	
	
	private String contextKey;
	
	public OConstMultiRequsetAckMsg(String contextKey) {
		this.contextKey = contextKey;
		
	}

	@Override
	public void action(IShamirAgent agent, int senderID) {
		IContext dummy = agent.context(contextKey);
		OSecureCompareHalfPrimeCtx ctx = (OSecureCompareHalfPrimeCtx) dummy;
	
		ctx.collectConstMultiAck(senderID);

	}

	@Override
	public IMessage clone() {
		// TODO Auto-generated method stub
		return this;
	}

}
