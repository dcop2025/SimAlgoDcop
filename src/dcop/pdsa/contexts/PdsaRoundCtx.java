package dcop.pdsa.contexts;

import java.util.Arrays;

import crypto.mpc.add.OSecureAddCtx;
import crypto.mpc.compare.OSecureCompareCtx;
import crypto.mpc.interfaces.IContext;
import crypto.mpc.interfaces.IShamirAgent;
import crypto.mpc.multiply.OSecureMultiplyCtx;
import crypto.mpc.reconstruct.OReconstructCtx;
import crypto.mpc.sub.OSecureKnownSubCtx;
import crypto.mpc.sub.OSecureSubCtx;
import crypto.utils.shamir.Shared;
import crypto.utils.shamir.VectorGenerater;
import dcop.pdsa.contexts.interfaces.IPdsaAgent;
import dcop.pdsa.contexts.messages.ConstrainsRowSharingMsg;

public class PdsaRoundCtx implements 
			IContext, 
			crypto.mpc.compare.OSecureCompareCtx.IContextOwner,
			crypto.mpc.multiply.OSecureMultiplyCtx.IContextOwner,
			crypto.mpc.sub.OSecureSubCtx.IContextOwner,
			crypto.mpc.sub.OSecureKnownSubCtx.IContextOwner,
			crypto.mpc.add.OSecureAddCtx.IContextOwner,
			crypto.mpc.reconstruct.OReconstructCtx.IContextOwner {
	
	static final String wb_i_key = "wb";
	static final String ki_key = "k-";
	static final String wi_key = "w-";
	static final String beta_key = "beta-";
	static final String gamma_key = "gamma-";
	static final String delta_key = "delta-";

	public interface IContextOwner {
		IPdsaAgent pdsaAgnet();
		void roundDone(String key);
		void setIndex(int newX);
	}

	private IContextOwner owner;
	private IPdsaAgent me;
	private String contextKey;
	private int round;
	private int waiters;
	private int u;
	private boolean debug;
	
	public PdsaRoundCtx(String contextKey, int round, IContextOwner owner) {
		this.owner = owner;
		this.me = owner.pdsaAgnet();
		this.contextKey = contextKey;
		this.round = round;		
	}
	

	public String contextKey() {
		return contextKey;
	}
	
	public void startNewRound() {
		// Clean seq + storage
		// Check last round
		sendSelectedVectorToEveryone();
	}
	
	public void sendSelectedVectorToEveryone( ) {
		//sendSelectedVectorToTarget(agent.agentID());
		for (int targetID : me.ids()) {
			sendSelectedVectorToTarget(targetID);
		}
		
	}
	
	public void sendSelectedVectorToTarget(int targetID) {
		me.debug(debug, "sending shareds to taget %d", targetID);

		int xIndex = me.xIndex();
		int[][] constraints = me.constraints(targetID);
		if (constraints == null) {
			// set vector to zero
		}
		int[] vector = Arrays.copyOf(constraints[xIndex], constraints[xIndex].length);

		// Create a secret generator
		VectorGenerater vGen = new VectorGenerater(vector, me.shamirThreshold(), me.prime(), me.cryptoRandom());
		for (int otherID : me.ids()) {
			Shared[] shareds = vGen.generate(otherID);
			ConstrainsRowSharingMsg msg = new ConstrainsRowSharingMsg(me.agentID(), otherID, round, shareds, contextKey);
			me.SendMsg(otherID, msg);
			waiters++;
		}

		Shared[] shareds = vGen.generate(me.agentID());
		ConstrainsRowSharingMsg msg = new ConstrainsRowSharingMsg(me.agentID(), targetID, round, shareds, contextKey);
		me.SendMsg(me.agentID(), msg);
		waiters++;

	}
	
	public void handleReadyToAssist() {
		waiters--;
		if (waiters != 0) {
			return;
		}
		kickStartEvaluateNewValue();		
	}	
	
	private void kickStartReconstruct() {
		// TODO code this
	}
	
	private void finishRound( ) {
		// TODO code this
	}
	
	private void kickStartEvaluateNewValue() {
		boolean skipRound = (me.roundThreshold() < me.algoRandom().nextFloat());
		if (skipRound) {
			// if skipping round, simply start a new round
			me.debug(false, "skipping round");
			finishRound();
			return;
		}
		// Starting for 1, as 0 was already set as the default min
		this.u = 1;
		evaluateIndex();
	}
	
	private int subReadyCounter = 0;
	private boolean cmpReady = false;
	
	private void evaluateIndex() {
		evaluateIndexPhase1();
	}
	private void evaluateIndexPhase1() { 		
		me.debug(false, "Evaluating vs index: " + u);
		if (u == me.domainPower()) {
			me.debug(false, "Done evaluating, reconstructing");
			kickStartReconstruct();
			return;
		}
		// Send a compare request msg to everyone
		String rightKey = wb_i_key + me.agentID() + "-" + u;
		String leftKey = wi_key + me.agentID();
		String betaKey = beta_key + me.agentID();		
		me.debug(false, "broadcasting to compare for agent " + me.agentID() + " u: " + u);
		
		// start for debug
		Shared left = me.shared(leftKey);
		if (left == null) {
			// Log error
			me.debug(true, "ERROR: Didn't find " + leftKey + " in agent " + me.agentID());
		} 
		Shared right = me.shared(rightKey);
		if (right == null) {
			// Log error
			me.debug(true, "ERROR: Didn't find " + rightKey + " in agent " + me.agentID());
		} 

		int res = left.real() > right.real() ? 1 : 0;
		cmpReady = false;
		OSecureCompareCtx compareCtx = new OSecureCompareCtx(String.format("%s-cmp", contextKey), rightKey, leftKey, betaKey, this);
		me.storeContext(compareCtx.contextKey(), compareCtx);
		compareCtx.init();
		
		// in parallel calc w_i(u) - w_i
		subReadyCounter = 0;
		OSecureSubCtx subCtx = new OSecureSubCtx(String.format("%s-wsub", contextKey), leftKey, rightKey, String.format("%s-wsub", contextKey), this);
		me.storeContext(subCtx.contextKey(), subCtx);
		subCtx.action();

		// and calc in parallel we could sub for w_i(u) - w_i
		OSecureKnownSubCtx kSubCtx = new OSecureKnownSubCtx(String.format("%s-ksub", contextKey), u, leftKey, String.format("%s-ksub", contextKey), this);
		me.storeContext(kSubCtx.contextKey(), kSubCtx);
		kSubCtx.action();		
	}


	@Override
	public IShamirAgent agnet() {
		// TODO Auto-generated method stub
		return me;
	}


	@Override
	public void compareDone(String key) {
		cmpReady = true;
		evaluateIndexPhase2();
	}

	public void subDone(String contextKey) {
		subReadyCounter++;
		evaluateIndexPhase2();
	}
	
	private int multiReadyCounter;
	
	public void evaluateIndexPhase2() {
		if (cmpReady == false) {
			return;
		}
		if (subReadyCounter != 2) {
			return;			
		}
		
		multiReadyCounter = 0;
		String betaKey = beta_key + me.agentID();
		
		OSecureMultiplyCtx multiGamma = new OSecureMultiplyCtx(String.format("%s-gamma", contextKey), betaKey, String.format("%s-wsub", contextKey), String.format("%s-gamma", contextKey), this);
		me.storeContext(multiGamma.contextKey(), multiGamma);
		multiGamma.action();
		
		OSecureMultiplyCtx multiDelta = new OSecureMultiplyCtx(String.format("%s-delta", contextKey), betaKey, String.format("%s-wsub", contextKey), String.format("%s-delta", contextKey), this);
		me.storeContext(multiDelta.contextKey(), multiDelta);
		multiDelta.action();
	}
	
	public void multiplyDone(String key) {
		multiReadyCounter++;
		evaluateIndexPhase3();
	}
	
	private int addReadyCounter = 0;
	
	public void evaluateIndexPhase3() {
		if (multiReadyCounter != 2) {
			return;
		}
		multiReadyCounter = 0;
		
		addReadyCounter = 0;
		String wiKey = wi_key + me.agentID();
		
		String ki = ki_key + me.agentID();
		
		OSecureAddCtx wAddCtx = new OSecureAddCtx(String.format("%s-addw", contextKey), wiKey, String.format("%s-gamma", contextKey), wiKey, this);
		me.storeContext(wAddCtx.contextKey(), wAddCtx);
		wAddCtx.action();
		
		OSecureAddCtx kAddCtx = new OSecureAddCtx(String.format("%s-addw", contextKey), ki, String.format("%s-delta", contextKey), ki, this);
		me.storeContext(wAddCtx.contextKey(), wAddCtx);
		wAddCtx.action();
			
	}
	
	
	public void addDone(String contextKey) {
		addReadyCounter++;
		evaluateIndexPhase4();
	}
	
	private void evaluateIndexPhase4( ) {
		if (addReadyCounter != 2) {
			return;
		}
		if (u == me.domainPower()) {
			reconstructK();
			return;
		}
		u++; 
		evaluateIndex();
	}
	
	private void reconstructK() {
		String ki = ki_key + me.agentID();
		OReconstructCtx kReconstructCtx = new OReconstructCtx(String.format("%s-kreconstruct", contextKey),ki, this);
		me.storeContext(kReconstructCtx.contextKey(), kReconstructCtx);
		kReconstructCtx.action();				
	}
	
	public void reconstructDone(String contextKey, long value) {
		owner.setIndex((int)value);
		owner.roundDone(contextKey);
	}
	
}