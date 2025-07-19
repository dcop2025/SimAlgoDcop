package crypto.mpc.compare;

import crypto.mpc.interfaces.*;

public class OCalcBWCPhase2AckMsg implements IMessage{
	
	
	private String contextKey;
	
	public OCalcBWCPhase2AckMsg(String contextKey) {
		this.contextKey = contextKey;
		
	}

	@Override
	public void action(IShamirAgent agent, int senderID) {
		IContext dummy = agent.context(contextKey);
		OSecureLSBCtx ctx = (OSecureLSBCtx) dummy;
	
		ctx.collectPhase2Ack(senderID);

	}

	public IMessage clone() {
		// TODO Auto-generated method stub
		return this;
	}


}
