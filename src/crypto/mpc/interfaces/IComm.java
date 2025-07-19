package crypto.mpc.interfaces;


// IComm is an interface for an object that provides communication with other agents
public interface IComm {
	void SendMsg(int agnetID, IMessage msg);
	
	void BroadcastMsg(IMessage msg);
	
	int networkSize();
	
	Iterable<Integer> ids();
}
