package crypto.mpc.interfaces;

public interface IMessage {

	void action(IShamirAgent agent, int senderID);

	IMessage clone();
}
