package pmgm.contexts.messages;

import crypto.mpc.interfaces.IContext;
import crypto.mpc.interfaces.IMessage;
import crypto.mpc.interfaces.IShamirAgent;
import crypto.utils.shamir.Shared;

public class CopySharedMsg implements IMessage {
	
	private String contextKey;
	private String output;
	private String input;
		
	public CopySharedMsg(String contextKey, String output, String input) {
		this.contextKey = contextKey;
		this.output = output;
		this.input = input;
	}

	public void action(IShamirAgent me, int senderID) {
		// take the src and store it in the src
		Shared src = me.shared(input);
		me.storeShared(output, src);
		// send a ack message back
		AckMsg ackMsg = new AckMsg(contextKey);
		me.SendMsg(senderID, ackMsg);
	}

	public IMessage clone( ) {
		return this;
	}
	
	public interface ICopySharedCtxOwner {
		void CopySharedAck(int agentID);
	}

	public class AckMsg implements IMessage {
		
		private String contextKey;
		
		public AckMsg(String contextKey) {
			this.contextKey = contextKey;
		}
		
		public void action(IShamirAgent me, int senderID) {
			IContext dummy = me.context(contextKey);
			ICopySharedCtxOwner ctx = (ICopySharedCtxOwner) dummy;

			ctx.CopySharedAck(senderID);
		}

		public IMessage clone( ) {
			return this;
		}		
	}
}
