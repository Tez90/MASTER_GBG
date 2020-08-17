package controllers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.swing.JOptionPane;

import controllers.PlayAgent.AgentState;
import controllers.TD.ntuple2.TDNTuple3Agt;
import games.StateObservation;
import games.RubiksCube.StateObserverCube;
import gui.MessageBox;
import params.ParMaxN;
import params.ParOther;
import tools.ScoreTuple;
import tools.Types;
import tools.Types.ACTIONS;
import tools.Types.ACTIONS_ST;
import tools.Types.ACTIONS_VT;

/**
 * Wrapper class for n-ply look-ahead in deterministic games. This class is NOT derived from {@link MaxNAgent}
 * (but applies the same MaxN principles).   <br>
 * [The former, now deprecated, {@code class MaxNWrapper extends MaxNAgent} was found to be error-prone and too complicated  
 * to maintain as good and simple software.]
 * 
 * @author Wolfgang Konen, TH Koeln, 2020
 */
public class MaxN2Wrapper extends AgentBase implements PlayAgent, Serializable {
	private PlayAgent wrapped_pa;
	
	private Random rand;
	protected int m_depth=10;
	
	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip will become unreadable or you have
	 * to provide a special version transformation)
	 */
	private static final long  serialVersionUID = 12L;

	// --- never used ---
//	public MaxN2Wrapper(PlayAgent pa, int nply) {
//		super("MaxN2Wrapper");
//		super.setMaxGameNum(1000);		
//		super.setGameNum(0);
//        rand = new Random(System.currentTimeMillis());
//		super.setAgentState(AgentState.TRAINED);
//		this.wrapped_pa = pa;
//		this.m_depth = nply;
//	}
	
	// XArenaFuncs::wrapAgent is now based on this agent to get other params 
	// from ParOther oPar
	public MaxN2Wrapper(PlayAgent pa, int nPly, ParOther oPar) {
		super("MaxN2Wrapper",oPar);
		super.setMaxGameNum(1000);		
		super.setGameNum(0);
        rand = new Random(System.currentTimeMillis());
		super.setAgentState(AgentState.TRAINED);
		m_depth = nPly;
		this.wrapped_pa = pa;
	}
	
	/**
	 * Get the best next action and return it
	 * @param so			current game state (not changed on return)
	 * @param random		allow epsilon-greedy random action selection	
	 * @param silent
	 * @return actBest		the best action 
	 * <p>						
	 * actBest has predicate isRandomAction()  (true: if action was selected 
	 * at random, false: if action was selected by agent).<br>
	 * actBest has also the members vTable and vBest to store the value for each available
	 * action (as returned by so.getAvailableActions()) and the value for the best action actBest.
	 * 
	 */	
	@Override
	public ACTIONS_VT getNextAction2(StateObservation so, boolean random, boolean silent) {
        List<ACTIONS> actions = so.getAvailableActions();
		
		// this is just a runtime check whether the wrapped agent implements estimateGameValueTuple
        try {
        	this.estimateGameValueTuple(so, null);
        } catch (RuntimeException e) {
        	if (e.getMessage()==AgentBase.EGV_EXCEPTION_TEXT) {
            	String str = "MaxN2Wrapper: The wrapped agent does not implement estimateGameValueTuple "
            			+ "\n --> set Other pars: Wrapper nPly to 0";
    			MessageBox.show(null,str,
    					"MaxNAgent", JOptionPane.ERROR_MESSAGE);      
    			return null;
        	} else {
        		throw e;	// other exceptions: rethrow
        	}
        }

        assert so.isLegalState() : "Not a legal state"; 
        
        // this starts the recursion:
		ACTIONS_VT act_best = getBestAction(so, random,  silent, 0, null);
		
        return act_best;
	}

	/**
	 * Loop over all actions available for {@code so} to find in a recursive tree search 
	 * the action with the best game value (best score for {@code so}'s player).
	 * 
	 * @param so		current game state (not changed on return)
	 * @param random	(not used currently) allow epsilon-greedy random action selection	
	 * @param silent
	 * @param depth		tree depth
	 * @param prevTuple TODO
	 * @return		best action + V-table + score tuple
	 */
	private ACTIONS_VT getBestAction(StateObservation so, boolean random, 
			boolean silent, int depth, ScoreTuple prevTuple) 
	{
		int i,j;
		ScoreTuple currScoreTuple=null;
		StateObservation NewSO;
		ScoreTuple scBest = null;
        ACTIONS actBest = null;
        ACTIONS_VT act_vt = null;
        ArrayList<ACTIONS> bestActions = new ArrayList<>();
        double maxValue = -Double.MAX_VALUE;
        double value;

//		assert so.isLegalState() : "Not a legal state"; 		// only debug
        
        ArrayList<ACTIONS> acts = so.getAvailableActions();
        double[] VTable =  new double[acts.size()+1];
        boolean lowest = true;
    	currScoreTuple = (prevTuple==null) ? new ScoreTuple(so,lowest) : prevTuple;
        int P = so.getPlayer();
        
        for(i = 0; i < acts.size(); ++i)
        {
        	NewSO = so.copy();
        	NewSO.advance(acts.get(i));
        	
    		if (NewSO.isGameOver())
    		{
    			boolean rgs = m_oPar.getRewardIsGameScore();
    			currScoreTuple = NewSO.getRewardTuple(rgs);
    		} else {
    			if (depth<this.m_depth) {
    				if (this.getWrappedPlayAgent() instanceof TDNTuple3Agt)
    					prevTuple = estimateGameValueTuple(NewSO, prevTuple);
    					// this is for wrappedAgent==TDNTuple3Agt and the case (N>=3): fill in the game value estimate
    					// for the player who created sob. Will be used by subsequent states as a surrogate for the 
    					// then unknown value for that player. 
    				
    				// here is the recursion: call this method again with depth+1:
    				currScoreTuple = getBestAction(NewSO, random, silent, depth+1, prevTuple).getScoreTuple();
    				
    			} else {
    				// this terminates the recursion:
    				// (after finishing the for-loop for every element of acts)
    				currScoreTuple = estimateGameValueTuple(NewSO, currScoreTuple);
    				// estimateGameValueTuple returns the score tuple of the wrapped agent. 
    			}    			
    		}
			// only debug for RubiksCube:
//			System.out.println(depth+": "+((StateObserverCube)NewSO).getCubeState().getTwistSeq()+", "+currScoreTuple);

			value = VTable[i] = currScoreTuple.scTup[P];
			// always *maximize* P's element in the tuple currScoreTuple, 
			// where P is the player to move in state so:
        	if (value==maxValue) bestActions.add(acts.get(i));
        	if (value>maxValue) {
        		maxValue = value;
        		scBest = new ScoreTuple(currScoreTuple);
        		bestActions.clear();
        		bestActions.add(acts.get(i));
        	}
        } // for
        
        // There might be one or more than one action with minValue. 
        // Break ties by selecting one of them randomly:
        actBest = bestActions.get(rand.nextInt(bestActions.size()));

        assert actBest != null : "Oops, no best action actBest";
        // optional: print the best action
        if (!silent) {
        	NewSO = so.copy();
        	NewSO.advance(actBest);
        	if (depth<=0)
        		System.out.println("--- "+depth+": Best Move: "+NewSO.stringDescr()+"   "+maxValue);
        }			

        VTable[acts.size()] = maxValue;
        act_vt = new ACTIONS_VT(actBest.toInt(), false, VTable, maxValue, scBest); 
        return act_vt;         
	}

	/**
	 * DEPRECATED, use {@link #estimateGameValueTuple(StateObservation, ScoreTuple)} instead.
	 * <p>
	 * When the recursion tree has reached its maximal depth m_depth, then return
	 * an estimate of the game score.  
	 * <p>
	 * Here we use the wrapped {@link PlayAgent} to return a game value.
	 * 
	 * @param sob	the state observation
	 * @return		the estimated score 
	 */
	@Override
	@Deprecated
	public double estimateGameValue(StateObservation sob) {	
//		return wrapped_pa.getScore(sob);
		return this.estimateGameValueTuple(sob, null).scTup[sob.getPlayer()];
	}

	/**
	 * When the recursion tree has reached its maximal depth m_depth, then return
	 * an estimate of the game score (tuple for all players).  
	 * <p>
	 * Here we use the wrapped {@link PlayAgent} to return a tuple of game values.
	 * 
	 * @param sob	the state observation
	 * @return		the tuple of estimated score 
	 */
	@Override
	public ScoreTuple estimateGameValueTuple(StateObservation sob, ScoreTuple prevTuple) {
		return wrapped_pa.estimateGameValueTuple(sob, prevTuple);
	}
	
	/**
	 * Return the agent's score for that after state.
	 * @param sob			the current game state;
	 * @return				the probability that the player to move wins from that 
	 * 						state. If game is over: the score for the player who 
	 * 						*would* move (if the game were not over).
	 * Each player wants to maximize its score	 
	 */
	@Override
	public double getScore(StateObservation sob) {
		return getBestAction(sob, false, true, 0, null).getScoreTuple().scTup[sob.getPlayer()];
	}
	@Override
	public ScoreTuple getScoreTuple(StateObservation sob, ScoreTuple prevTuple) {
		return getBestAction(sob, false, true, 0, null).getScoreTuple();
	}
	
	public PlayAgent getWrappedPlayAgent() {
		return wrapped_pa;
	}

	@Override
	public String stringDescr() {
		String cs = wrapped_pa.getClass().getSimpleName();
		cs = cs + "[nPly="+m_depth+"]";
		return cs;
	}

    @Override
	public String stringDescr2() {
		return getClass().getName()+"["+wrapped_pa.getClass().getSimpleName()+"]" + ":" + "(nPly="+m_depth+")";
	}
    
	// override AgentBase::getName()
	@Override
	public String getName() {
		String cs = super.getName();;
		cs = cs + "["+wrapped_pa.getName()+","+m_depth+"]";
		return cs;
	}

	
	public String getFullName() {
		String cs = wrapped_pa.getName();
		cs = cs + "[nPly="+m_depth+"]";
		return cs;
	}

}