package crypto.mpc.compare;

import crypto.mpc.interfaces.*;

public class OCalcBWCPhase1AckMsg implements IMessage{
	
	
	private String contextKey;
	
	public OCalcBWCPhase1AckMsg(String contextKey) {
		this.contextKey = contextKey;
		
	}

	@Override
	public void action(IShamirAgent agent, int senderID) {
		IContext dummy = agent.context(contextKey);
		OSecureLSBCtx ctx = (OSecureLSBCtx) dummy;
	
		ctx.collectPhase1Ack(senderID);

	}

	public IMessage clone() {
		return this;
	}

}
