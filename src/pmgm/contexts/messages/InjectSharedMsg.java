package pmgm.contexts.messages;

import crypto.mpc.interfaces.IContext;
import crypto.mpc.interfaces.IMessage;
import crypto.mpc.interfaces.IShamirAgent;
import crypto.utils.shamir.Shared;

public class InjectSharedMsg implements IMessage {
	
	private String contextKey;
	private String sharedKey;
	private Shared shared;
		
	public InjectSharedMsg(String contextKey, String sharedKey, Shared shared) {
		this.contextKey = contextKey;
		this.sharedKey = sharedKey;
		this.shared = shared;
	}

	public void action(IShamirAgent me, int senderID) {
		// TODO code the action
		me.storeShared(sharedKey, shared);

		// send a ack message back
		AckMsg ackMsg = new AckMsg(contextKey);
		me.SendMsg(senderID, ackMsg);
	}

	public IMessage clone( ) {
		return this;
	}
	
	public interface IInjectSharedCtxOwner {
		void InjectSharedAck(int agentID);
	}

	public class AckMsg implements IMessage {
		
		private String contextKey;
		
		public AckMsg(String contextKey) {
			this.contextKey = contextKey;
		}
		
		public void action(IShamirAgent me, int senderID) {
			IContext dummy = me.context(contextKey);
			IInjectSharedCtxOwner ctx = (IInjectSharedCtxOwner) dummy;

			ctx.InjectSharedAck(senderID);
		}

		public IMessage clone( ) {
			return this;
		}		
	}
}
