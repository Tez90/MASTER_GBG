package src.games.Poker;

import games.ObserverBase;
import games.StateObservation;
import games.TicTacToe.TicTDBase;
import tools.Types.ACTIONS;

import java.util.ArrayList;

/**
 * Class StateObservation observes the current state of the game, it has utility functions for
 * <ul>
 * <li> returning the available actions ({@link #getAvailableActions()}), 
 * <li> advancing the state of the game with a specific action ({@link #advance(ACTIONS)}),
 * <li> copying the current state
 * <li> signaling end, score and winner of the game
 * </ul>
 *
 */
public class StateObserverPoker extends ObserverBase implements StateObservation {
    private static final double REWARD_NEGATIVE = -1.0;
    private static final double REWARD_POSITIVE =  1.0;
	private int[][] m_Table;		// current board position
	private int m_Player;			// player who makes the next move (+1 or -1)
	protected ArrayList<ACTIONS> availableActions = new ArrayList();	// holds all available actions
    
	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .gamelog containing this object will become 
	 * unreadable or you have to provide a special version transformation)
	 */
	private static final long serialVersionUID = 12L;

	public StateObserverPoker() {
		m_Table = new int[3][3]; 
		m_Player = 1;
		setAvailableActions();
	}
	
	public StateObserverPoker(int[][] Table, int Player) {
		m_Table = new int[3][3];
		TicTDBase.copyTable(Table,m_Table);
		m_Player = Player;
		setAvailableActions();
	}
	
	public StateObserverPoker(StateObserverPoker other)
	{
		super(other);		// copy members m_counter and stored*
		this.m_Table = new int[3][3];
		TicTDBase.copyTable(other.m_Table,m_Table); 
		m_Player = other.m_Player;
		if (other.availableActions!=null)	// this check is needed when loading older logs
			this.availableActions = (ArrayList<ACTIONS>) other.availableActions.clone();
				// Note that clone does only clone the ArrayList, but not the contained ACTIONS, they are 
				// just copied by reference. However, these ACTIONS are never altered, so it is o.k.
//		setAvailableActions();		// this would be a bit slower
	}
	
	public StateObserverPoker copy() {
		StateObserverPoker sot = new StateObserverPoker(this);
		return sot;
	}

    @Override
	public boolean isGameOver() {
		return TicTDBase.isGameOver(m_Table);
	}

    @Override
	public boolean isDeterministicGame() {
		return true;
	}
	
    @Override
	public boolean isFinalRewardGame() {
		return true;
	}

    @Override
	public boolean isLegalState() {
		return TicTDBase.legalState(m_Table,m_Player);
	}
	
	public boolean isLegalAction(ACTIONS act) {
		int iAction = act.toInt();
		int j=iAction%3;
		int i=(iAction-j)/3;		// reverse: iAction = 3*i + j
		
		return (m_Table[i][j]==0); 
		
	}

	@Deprecated
    public String toString() {
    	return stringDescr();
    }
	
	@Override
    public String stringDescr() {
		String sout = "";
		String str[] = new String[3]; 
		str[0] = "o"; str[1]="-"; str[2]="X";
		
		for (int i=0;i<3;i++) 
			for (int j=0;j<3;j++)
				sout = sout + (str[this.m_Table[i][j]+1]);
		
 		return sout;
	}
	
	/**
	 * 
	 * @return true, if the current position is a win (for either player)
	 */
	public boolean win()
	{
		if (TicTDBase.Win(this.getTable(),+1)) return true;
		return TicTDBase.Win(this.getTable(),-1);
	}
	

//	public Types.WINNER getGameWinner() {
//		assert isGameOver() : "Game is not yet over!";
//		if (TicTDBase.Win(m_Table, -m_Player))		// why -m_Player? advance() has changed m_player (although game is over) 
//			return Types.WINNER.PLAYER_LOSES;
//		if (TicTDBase.tie(m_Table)) 
//			return Types.WINNER.TIE;
//		
//		throw new RuntimeException("Unexpected case: we cannot have a win for the player to move!");
//	}

	/**
	 * @return 	the game score, i.e. the sum of rewards for the current state. 
	 * 			For TTT only game-over states have a non-zero game score. 
	 * 			It is the reward from the perspective of {@code refer}.
	 */
	public double getGameScore(StateObservation refer) {
		int sign = (refer.getPlayer()==this.getPlayer()) ? 1 : (-1);
        if(isGameOver()) {
    		if (TicTDBase.tie(m_Table)) 
    			return 0;
        	// if the game is over and not a tie, it is a win for the player who made the action towards 
        	// state 'this' --> it is a loss for this.getPlayer().
        	return -sign;
    		
        	// old code, more complicated and it uses getGameWinner() which we want to be obsolete
//            Types.WINNER win = this.getGameWinner();
//        	switch(win) {
//        	case PLAYER_LOSES:
//                return sign*REWARD_NEGATIVE;
//        	case TIE:
//                return 0;
//        	case PLAYER_WINS:
//                return sign*REWARD_POSITIVE;
//            default:
//            	throw new RuntimeException("Wrong enum for Types.WINNER win !");
//        	}
        }
        
        return 0; 
	}

	public double getMinGameScore() { return REWARD_NEGATIVE; }
	public double getMaxGameScore() { return REWARD_POSITIVE; }

	public String getName() { return "TicTacToe";	}

	/**
	 * Advance the current state with 'action' to a new state
	 * Actions:
	 * - fold: 0
	 * - check:
	 * - bet:
	 * - call:
	 * - raise[1]:
	 * - raise[2]:
	 * - raise[3]:
	 * - raise[4]:
	 * - raise[5]:
	 *
	 * @param action
	 */
	public void advance(ACTIONS action) {
		int iAction = action.toInt();
		assert (availableActions.contains(iAction)) : "iAction is not available";



    	setAvailableActions(); 		// IMPORTANT: adjust the available actions (have reduced by one)

		// next player
		m_Player = m_Player*(-1);    // 2-player games: 1,-1,1,-1,...

		super.incrementMoveCounter();
	}

    /**
     * Return the afterstate preceding {@code this}. 
     */
    @Override
    public StateObservation getPrecedingAfterstate() {
    	// for deterministic games, this state and its preceding afterstate are the same
    	return this;
    }

    @Override
    public ArrayList<ACTIONS> getAllAvailableActions() {
        ArrayList allActions = new ArrayList<>();
        for (int i = 0; i < 3; i++) 
            for (int j = 0; j < 3; j++) 
            	allActions.add(ACTIONS.fromInt(i * 3 + j));
        
        return allActions;
    }
    
	public ArrayList<ACTIONS> getAvailableActions() {
		return availableActions;
	}
	
	public int getNumAvailableActions() {
		return availableActions.size();
	}

	/**
	 * Given the current state in m_Table, what are the available actions? 
	 * Set them in member ACTIONS[] actions.
	 */
	public void setAvailableActions() {
		availableActions.clear();
		if (m_Table[0][0]==0)  availableActions.add(ACTIONS.fromInt(0));
		if (m_Table[0][1]==0)  availableActions.add(ACTIONS.fromInt(1));
		if (m_Table[0][2]==0)  availableActions.add(ACTIONS.fromInt(2));
		if (m_Table[1][0]==0)  availableActions.add(ACTIONS.fromInt(3));
		if (m_Table[1][1]==0)  availableActions.add(ACTIONS.fromInt(4));
		if (m_Table[1][2]==0)  availableActions.add(ACTIONS.fromInt(5));
		if (m_Table[2][0]==0)  availableActions.add(ACTIONS.fromInt(6));
		if (m_Table[2][1]==0)  availableActions.add(ACTIONS.fromInt(7));
		if (m_Table[2][2]==0)  availableActions.add(ACTIONS.fromInt(8));
//        // /WK/ Get the available actions in an array.
//		// *TODO* Does this work if acts.size()==0 ?
//        ArrayList<Types.ACTIONS> acts = this.getAvailableActions();
//        actions = new Types.ACTIONS[acts.size()];
//        for(int i = 0; i < actions.length; ++i)
//        {
//            actions[i] = acts.get(i);
//        }
		
	}
	
	public ACTIONS getAction(int i) {
		return availableActions.get(i);
	}

	public int[][] getTable() {
		return m_Table;
	}

	/**
	 * @return 	{0,1} for the player to move in this state 
	 * 			Player 0 is X, the player who starts the game. Player 1 is O.
	 */
	public int getPlayer() {
		return (-m_Player+1)/2;
	}
	
	public int getNumPlayers() {
		return 2;				// TicTacToe is a 2-player game
	}


}
