package crypto.mpc.compare;

import crypto.mpc.interfaces.*;

public class OSubtractionRequsetAckMsg implements IMessage{
	
	
	private String contextKey;
	
	public OSubtractionRequsetAckMsg(String contextKey) {
		this.contextKey = contextKey;
		
	}

	@Override
	public void action(IShamirAgent agent, int senderID) {
		IContext dummy = agent.context(contextKey);
		OSecureCompareCtx ctx = (OSecureCompareCtx) dummy;
	
		ctx.collectSubtractionAck(senderID);

	}

	@Override
	public IMessage clone() {
		// TODO Auto-generated method stub
		return this;
	}

}
