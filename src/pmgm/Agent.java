package pmgm;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import projects.dcopsim.IDcopAgent;
import dcop.Problem.ConstraintsMatrix;
import dcop.pdsa.contexts.PdsaRoundCtx;
import dcop.pdsa.contexts.interfaces.IPdsaAgent;
import dcop.pdsa.OMessage;
import crypto.mpc.barrier.OBarrierCtx;
import crypto.mpc.barrier.OBarrierNotifyMsg;
import crypto.mpc.interfaces.*;
import crypto.mpc.testing.OTestCmpZeroCtx;
import crypto.mpc.testing.OTestCompareMPCCtx;
import crypto.mpc.testing.OTestMultiplyMPCCtx;
import crypto.utils.shamir.Shared;
import crypto.utils.shamir.SharedStorage;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.runtime.Global;
import utils.*;

public class Agent extends Node implements 
	IDcopAgent, IShamirAgent,
	crypto.mpc.testing.OTestMultiplyMPCCtx.IContextOwner, 
	crypto.mpc.testing.OTestCompareMPCCtx.IContextOwner,
	crypto.mpc.testing.OTestCmpZeroCtx.IContextOwner,
	dcop.pdsa.contexts.interfaces.IPdsaAgent,
	dcop.pdsa.contexts.PdsaRoundCtx.IContextOwner,
	crypto.mpc.barrier.OBarrierCtx.IContextOwner {
		
	@Override
    /**
     * handles messages from the inbox, assume that  
      *
     * @param inbox - the node's messages inbox 
     */
	public void handleMessages(Inbox inbox) {		
		while(inbox.hasNext()) {
			Message m = inbox.next();
			handleMessage(inbox.getSender(), m);
		}		
	}
	
	public void handleMessage(Node sender, Message msg) {
		OMessage m = (OMessage) msg;
		m.action(this, sender);
	}

	@Override
	public void preStep() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void neighborhoodChange() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postStep() {
		if (triggerPDSA) {
			StartPDSA();
		}
		
		if (triggerCommand) {
			runCommand();
		}
	}

	@Override
	public void checkRequirements() throws WrongConfigurationException {
		// TODO Auto-generated method stub
		
	}

			
	/////////////////////////////////
	// Agent Configuration Members //
	/////////////////////////////////
	private boolean debug = false;
	private long alogSeed;
	private Random algoRandom;
	private long cryptoSeed;
	private Random cryptoRandom;

	////////////////////////////////
	// DCOP Configuration Members //
	////////////////////////////////
	private int domainPower;
	private int xIndex;

	////////////////////////////////
	// PDSA Configuration Members //
	////////////////////////////////	
	private double roundThreshold;
	private long prime;
	private int round;


	/////////////////
	// PDSA status //
	/////////////////	
	private boolean triggerPDSA;
	private boolean runningPDSA;

	private boolean triggerCommand;
	private boolean runningCommand;
	
	////////////////////
	// PDSA Internals //
	////////////////////
	private SharedStorage sharedStorage;
	private OTicker ticker;
	
	public Shared storeShared(String key, Shared shared) {
		return sharedStorage.put(key, shared);
	}

	public Shared shared(String key) {
		return sharedStorage.get(key);
	}

	private Map<String, IContext> contextMgr;
	
	public IContext context(String contextKey) {
		return contextMgr.get(contextKey);
	}

	public void storeContext(String contextKey, IContext ctx) {
		contextMgr.put(contextKey, ctx);
	}

		
	public long prime() {
		return this.prime;
	}
	public int shamirThreshold() {		
		return this.shamirThreshold;
	}

	
	public Agent(int agentID, int domainPower, double roundThreshold, long prime, long algoSeed, long cryptoSeed) {
		//super(agentID);
		super();
		this.alogSeed = algoSeed;
		this.cryptoSeed = cryptoSeed;
		
		
		this.domainPower = domainPower;
		
		this.roundThreshold = roundThreshold;
		this.prime = prime;
		
		sharedStorage = new SharedStorage();
		ticker = new OTicker();
		setup();
		
		this.constraints = new HashMap<Integer, int[][]>();
		this.connections = new HashMap<Integer, Integer>();
		this.neighbors = new Vector<IDcopAgent>();
		
		this.contextMgr = new HashMap<String, IContext>();
		
	}
	
	
	private void setup() {
		this.round = 0;
		
		if (alogSeed == 0) {
			this.algoRandom = new Random();
		} else {
			this.algoRandom = new Random(alogSeed + 1000 + ID);
		}
		
		if (cryptoSeed == 0) {
			this.cryptoRandom = new Random();
		} else {
			this.cryptoRandom = new Random(alogSeed + 2000 + ID);
		}

		sharedStorage.clear(false);
	}
	
	
    /**
     * log the message for if enable flag is true 
     *
     * @param enable - indicates if to output the log string 
     * @param logStr - the log string 
     */
	@Override
	public void debug(boolean flag, String format, Object... args) {
		Global.log.logln(flag || this.debug, "ID:" + ID + " " + String.format(format, args));
	}
	
	// COP info
	private Map<Integer, int[][]> constraints;	
	private Vector<IDcopAgent> neighbors;
	public Map<Integer, Integer> connections;
	private int shamirThreshold;

	public Vector<IDcopAgent> Neighborhood(){
		return neighbors;
	}
	
	private void sendOMsg(int agentID, OMessage msg) {
		if (agentID == ID) {
			// If the message is for me, no need to send it
			handleMsg(this, msg);
			return;
		}
		Agent a = getAgentByID(agentID);		
		if (a == null) {
			// Log an error before panic
			debug(true, "ERROR: Agent " + agentID + " wasn't found in agent " + ID);
		}
		send(msg, a);		
	}
	
	public void SendMsg(int agentID, IMessage msg) {
		OMessage omsg = new OMessage(msg);
		sendOMsg(agentID, omsg);
	} 

	
	public void BroadcastMsg(IMessage imsg) {
		OMessage msg = new OMessage(imsg);
		for (IDcopAgent a: neighbors) {
			sendOMsg(a.agentID(), msg);
		}
		// Also need to take part of the compare so sending the message to myself
		handleMsg(this, msg);

	}
	

    public Iterable<Integer> ids() {
        return () -> new Iterator<Integer>() {
            private final Iterator<IDcopAgent> it = neighbors.iterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public Integer next() {
                return it.next().agentID();
            }
        };
    }
    
	/*
	public void BroadcastMsg(OMessage msg) {
		for (IDcopAgent a: neighbors) {
			SendMsg(a.agentID(), msg);
		}
		// Also need to take part of the compare so sending the message to myself
		handleMsg(this, msg);
	}
	*/
	public int networkSize() {
		return neighbors.size()+1;
	}
	
	
	private Agent getAgentByID(int otherID) {
		IDcopAgent other = null;		
		for (IDcopAgent a: neighbors) {
			if (a.agentID() == otherID) {
				other = a;
			}
		}
		return (Agent)other;
	}


	public void handleMsg(Node sender, OMessage m) {
		//debug(false, "got " + m.getClass() + " from: " + sender.ID);
		m.action(this, sender);
	}

	
	private void DebugCOPInfo(boolean xdebug) {
		debug(xdebug, "COP Info");
		for (int key : constraints.keySet()) {
			Global.log.logln(xdebug, "Other ID:" + key);
			for (int i = 0; i < constraints.get(key).length; i++) {
				Global.log.log(xdebug, "\t");
				for (int j = 0; j < constraints.get(key)[i].length; j++) {
					Global.log.log(xdebug, " "+ constraints.get(key)[i][j]);
				}
				Global.log.log(xdebug, "\n");
			}
		}
	}

	public void installConstraintsMatrix(ConstraintsMatrix Constraints) {
		// TODO Auto-generated method stub
		
	}
	
	public void addConnectionConstraints(Agent other, int[][] constraints, boolean zero) {
		
		this.constraints.put(other.ID, constraints);
		
		int connected =  zero ? 1 : 0;
		this.connections.put(other.ID, connected);
		
		neighbors.add(other);
		// only one agent needs to add a bi connection, let it be the agent with higher id  
		if (ID > other.ID) {
			addBidirectionalConnectionTo(other);
		}
	}


	public void logState() {
		DebugCOPInfo(true);		
	}

	public int assignment() {
		return xIndex;
	}

	public int getInternal(String key) {
		// TODO write a better function that return value by the key
		return round;
	}

	public void connect(IDcopAgent other) {
		// TODO Auto-generated method stub
		
	}

	public void trigger() {
		triggerPDSA = true;
	}
	
	private void StartPDSA() {
		debug = (ID == 1);
		debug(debug, "Starting PDSA algo");
		setup();
		triggerPDSA = false;
		runningPDSA = true;
		
		shamirThreshold = (int) Math.floor((double)(neighbors.size()+1)/2);
		
		// select init value
		xIndex = algoRandom.nextInt(domainPower);
		debug(debug, "Init value %d", xIndex);
		startRound();
	}

	private void startRound() {
		contextMgr.clear();
		sharedStorage.clear(false);
		OBarrierCtx barrierCtx = new OBarrierCtx(String.format("b-round-%d", round), neighbors.size()+1, this);
		storeContext(barrierCtx.contextKey(), barrierCtx);
		PdsaRoundCtx roundCtx = new PdsaRoundCtx(String.format("pdsa-round-%d", round), round, this);
		storeContext(roundCtx.contextKey(), roundCtx);
		roundCtx.startNewRound();		
	}
	
	public boolean doneStatus() {
		// TODO fix the logic here
		//return !runningCommand;
		return !runningPDSA;
	}

	public int agentID() {
		return ID;
	}

	
	@Override
	public IShamirAgent agnet() {
		return this;
	}

	@Override
	public boolean ticker(String tickerKey, int goal) {
		return ticker.ticker(tickerKey, goal);
	}

	@Override
	public int xIndex() {
		return xIndex;
	}

	public void setXIndex(int x) {
		xIndex = x;
	}
	
	@Override
	public Random cryptoRandom() {
		return cryptoRandom;
	}

	@Override
	public Random algoRandom() {
		return algoRandom;
	}

	@Override
	public int[][] constraints(int targetID) {
		return constraints.get(targetID);
	}

	@Override
	public double roundThreshold() {
		return roundThreshold;
	}

	@Override
	public int domainPower() {
		return domainPower;
	}

	@Override
	public IPdsaAgent pdsaAgnet() {
		return this;
	}

	@Override
	public void roundDone(String key) {
		// TODO wait for new round
		OBarrierNotifyMsg msg = new OBarrierNotifyMsg(String.format("b-round-%d", round));
		this.BroadcastMsg(msg);
		
	}

	@Override
	public void barrier(String key) {
		// P2 need to check the the context is the context of round done
		// start a new round
		debug(true, "moving to the next round");
		round++;
		startRound();
	}

	
	@Override
	public void setIndex(int newX) {
		debug(true, "the new index is %d", newX);
		xIndex = newX;		
	}
	
	////////////////////////////////////////////////////////////////
	// add the code below here is for testing need to move it out
	
	
	public void triggerCommand(String command) {
		// only the first agent answer the call
		if (agentID() != 1) {
			return;
		}
		
		triggerCommand = true;
	}


	public void InjectRef(String refKey, Shared ref, Shared refBits[]) {
		sharedStorage.InjectRef(refKey, ref, refBits);
	}
	
		
	public void testMultiFlow() {
		// Inject first 
	}

	//////////////////////////////////////////////////
	
	private int a;
	private int b;
	
	private void runCmpTest() {
		sharedStorage.clear(false);
		String ctxName = "cmp-test";
		OTestCompareMPCCtx cmpTestCtx = new OTestCompareMPCCtx(
				ctxName, 
				this,
				this,
				"key-a", a,
				"key-b", b,
//				"key-a", algoRandom.nextInt(100), 
				///"key-b", algoRandom.nextInt(100), 
				"key-c");
		contextMgr.put(cmpTestCtx.contextKey(), cmpTestCtx);
		cmpTestCtx.action();		
	}
	
	private void runCmpZeroTest( ) {
		sharedStorage.clear(false);
		constraints.clear();
		String ctxName = "cmp-test";
		OTestCmpZeroCtx cmpTestCtx = new OTestCmpZeroCtx(
				ctxName, 
				a,
				this);
		contextMgr.put(cmpTestCtx.contextKey(), cmpTestCtx);
		cmpTestCtx.action();			
	}
	
	private void runCommand() {		
		triggerCommand = false;
		runningCommand = true;
		
		debug = true;
		debug(debug, "starting run command");

		// recalc shamir threshold
		shamirThreshold = (int) Math.floor((double)(neighbors.size()+1)/2);
		
		// tasks
		// 1. create a blank context for testing multi
		// 2. add the context to the context bank

		a = 24;
		b = 25;
/*
		String ctxName = "multiply-test"; 
		OTestMultiplyMPCCtx multiplyTest = new OTestMultiplyMPCCtx(
				ctxName, 
				this,
				"key-a", a,
				"key-b", b, 
				"key-c" 
				);
		contextMgr.put(multiplyTest.contextKey(), multiplyTest);
		multiplyTest.action();
*/
		
		//a = 0;
		//b = 0;
		runCmpTest();
		//runCmpZeroTest();
		
	}

	public void multiplyTestOver(String contextKey) {
		runningCommand = false;
	}
	
	public void compareTestOver(String contextKey) {
		a++;
		if (a > 100) {
			a = 0;
			b++;
			if (b > 100) {
				return;
			}
		}
		runCmpTest();
	}
	
	public void cmpTestOver(String contextKey, long value) {
		debug(true, ">>>>>> value: %d: %d", a, value);
		if ((a > 0) && (value != 0)) {
			Shared z = shared("dfadsfas");
			z.real();
		}
		a++;
		runCmpZeroTest();
	}

	@Override
	public IShamirAgent me() {
		return this;
	}

}