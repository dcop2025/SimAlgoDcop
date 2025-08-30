package dcop.pdsa.contexts.messages;

import crypto.mpc.compare.OSecureLSBCtx;
import crypto.mpc.interfaces.IContext;
import crypto.mpc.interfaces.IMessage;
import crypto.mpc.interfaces.IShamirAgent;
import dcop.pdsa.contexts.PdsaRoundCtx;

public class ReadyToEvaluateMsg implements IMessage {
	
	private String contextKey;
	
	public ReadyToEvaluateMsg(String contextKey) {
		this.contextKey = contextKey;
	}

	
	public void action(IShamirAgent agent, int senderID) {
		IContext dummy = agent.context(contextKey);
		PdsaRoundCtx ctx = (PdsaRoundCtx) dummy;
	
		ctx.handleReadyToAssist(senderID);
	}

	public IMessage clone( ) {
		return this;
	}
}
