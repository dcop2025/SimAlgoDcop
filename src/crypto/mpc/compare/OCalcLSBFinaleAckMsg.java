package crypto.mpc.compare;

import crypto.mpc.interfaces.*;

public class OCalcLSBFinaleAckMsg implements IMessage{
	
	
	private String contextKey;
	
	public OCalcLSBFinaleAckMsg(String contextKey) {
		this.contextKey = contextKey;
		
	}

	@Override
	public void action(IShamirAgent agent, int senderID) {
		IContext dummy = agent.context(contextKey);
		OSecureLSBCtx ctx = (OSecureLSBCtx) dummy;
	
		ctx.collectFinalAck(contextKey);

	}

	
	public IMessage clone() {
		// TODO Auto-generated method stub
		return this;
	}


}
