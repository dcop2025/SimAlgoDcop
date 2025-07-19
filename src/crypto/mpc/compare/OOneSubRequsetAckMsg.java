package crypto.mpc.compare;

import crypto.mpc.interfaces.*;

public class OOneSubRequsetAckMsg implements IMessage{
	
	
	public interface IOneSubCtx {
		void collectOneSubAck(String context, String subContext, int otherID);
	}
	
	private String contextKey;
	
	public OOneSubRequsetAckMsg(String contextKey) {
		this.contextKey = contextKey;
		
	}

	@Override
	public void action(IShamirAgent agent, int senderID) {
		IContext dummy = agent.context(contextKey);
		IOneSubCtx ctx = (IOneSubCtx) dummy;
		ctx.collectOneSubAck(contextKey, "", senderID);
		/*
		OSecureCompareCtx ctx = (OSecureCompareCtx) dummy;
	
		ctx.collectOneSubAck(sender.ID);
		*/

	}

	@Override
	public IMessage clone() {
		// TODO Auto-generated method stub
		return this;
	}

}
