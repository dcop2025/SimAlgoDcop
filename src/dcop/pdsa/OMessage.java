package dcop.pdsa;


import crypto.mpc.interfaces.IMessage;
import crypto.mpc.interfaces.IShamirAgent;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

// OMessage is a an interface class that extends the sinalgo message
// and provide action method, to enable dependency inversion pattern
public class OMessage extends Message{ 	

	IMessage msg;
	
	public OMessage(IMessage msg) {
		this.msg = msg;
	}
    /**
     * calls a predefine action method in the Agent to handle the message
     * must be implement by every message, by calling the proper Agent handle message method 
     *
     * @param the Agent to handle the message, should be the one that calls the action method
     * @param the node that send the message 
     */	
	void action(IShamirAgent agent, Node sender) {
		msg.action(agent, sender.ID);
	}

	@Override
	public Message clone() {
		// TODO Auto-generated method stub
		return this;
	}
}
