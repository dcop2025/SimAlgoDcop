package crypto.mpc.compare;

import crypto.mpc.interfaces.*;
import crypto.utils.shamir.Shared;

public class OPrepLSBRequesAcktMsg implements IMessage{
	
	
	private String contextKey;
	private Shared c;
	
	public OPrepLSBRequesAcktMsg(String contextKey, Shared c) {
		this.contextKey = contextKey;
		this.c = c;
		
	}

	@Override
	public void action(IShamirAgent agent, int senderID) {
		IContext dummy = agent.context(contextKey);
		OSecureLSBCtx ctx = (OSecureLSBCtx) dummy;
	
		ctx.collectPrep(senderID, c);

	}

	@Override
	public IMessage clone() {
		// TODO Auto-generated method stub
		return this;
	}

}
