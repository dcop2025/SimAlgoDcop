package dcop.pdsa.contexts.interfaces;

import java.util.Random;

import crypto.mpc.interfaces.IShamirAgent;

public interface IPdsaAgent extends IShamirAgent, ITicker {
	int xIndex();
	Random cryptoRandom();
	Random algoRandom();
	int shamirThreshold();
	long prime();
	void debug(boolean flag, String format, Object... args);
	int[][] constraints(int targetID);
	double roundThreshold();
	int domainPower();
}
