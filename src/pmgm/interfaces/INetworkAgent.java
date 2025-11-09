package pmgm.interfaces;

import crypto.mpc.interfaces.IMessage;

public interface INetworkAgent {
	Iterable<Integer> connections();
	void SendMsg(int agnetID, IMessage msg);	
	void BroadcastMsg(IMessage msg);	
	int networkSize();
}
