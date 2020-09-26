package src.games.PenneysGame;

import games.ObserverBase;
import games.StateObservation;
import games.TicTacToe.TicTDBase;
import tools.Types.ACTIONS;

import java.util.ArrayList;
import java.util.Random;

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
public class StateObserverPenney extends ObserverBase implements StateObservation {
    private static final double REWARD_NEGATIVE = -1.0;
    private static final double REWARD_POSITIVE =  1.0;

	private int[][] m_Table;		// current board position
	private int m_Player;			// player who makes the next move (0/1)

	private Random random = new Random();
	protected ArrayList<ACTIONS> availableActions = new ArrayList();	// holds all available actions
	private int turn;
	private String winning;

	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .gamelog containing this object will become 
	 * unreadable or you have to provide a special version transformation)
	 */
	private static final long serialVersionUID = 1L;

	public StateObserverPenney() {
		m_Table = new int[2][3];
		m_Player = 1;
		turn = 0;
		setAvailableActions();
		winning = "";
		for(int i = 0;i<50;i++) {
			winning += Integer.toString((random.nextInt(2)+1));
		}
	}

	public StateObserverPenney(StateObserverPenney other) {
		m_Table = new int[2][3];

		this.turn = other.turn;

		for(int i = 0;i < 3;i++){
			this.m_Table[0][i] = other.m_Table[0][i];
		}

		for(int i = 0;i < 3;i++){
			this.m_Table[1][i] = other.m_Table[1][i];
		}

		this.m_Player = other.m_Player;

		this.winning = other.winning;
		setAvailableActions();
	}

	public StateObserverPenney copy() {
		StateObserverPenney sop = new StateObserverPenney(this);
		return sop;
	}

    @Override
	public boolean isGameOver() {
		return this.m_Table[1][2] > 0 ;
	}

    @Override
	public boolean isDeterministicGame() {
		return false;
	}
	
    @Override
	public boolean isFinalRewardGame() {
		return true;
	}

    @Override
	public boolean isLegalState() {
		return true;
	}
	
	public boolean isLegalAction(ACTIONS act) {
		return true;
	}

	@Deprecated
    public String toString() {
    	return stringDescr();
    }
	
	@Override
    public String stringDescr() {
		String sout = "";

		sout += "\r\nPlayer X: ";
		for (int j=0;j<3;j++)
			sout = sout + this.m_Table[1][j];

		sout += "\r\nPlayer O: ";
		for (int j=0;j<3;j++)
			sout = sout + this.m_Table[0][j];

 		return sout;
	}

	public String getDescPlayer(int x){
		String sout = "";
		for (int j=0;j<3;j++)
			sout += this.m_Table[x][j];
		return sout;
	}
	
	/**
	 * 
	 * @return true, if the current position is a win (for either player)
	 */
	public boolean win()
	{
		return(isGameOver());
	}
	/**
	 * @return 	the game score, i.e. the sum of rewards for the current state. 
	 * 			For TTT only game-over states have a non-zero game score. 
	 * 			It is the reward from the perspective of {@code refer}.
	 */
	public double getGameScore(StateObservation refer) {
		int player_i = refer.getPlayer();
		int enemy_i = (player_i==0) ? 1 : 0;

		if(isGameOver()) {
			String player = Integer.toString(m_Table[player_i][0])+ Integer.toString(m_Table[player_i][1])+Integer.toString(m_Table[player_i][2]);
			String enemy = Integer.toString(m_Table[enemy_i][0])+ Integer.toString(m_Table[enemy_i][1])+Integer.toString(m_Table[enemy_i][2]);

			if(player.equals(enemy))
				return 0 ;
			System.out.println(winning);
			for(int i = 0; i<this.winning.length()-2;i++){
				System.out.println(winning.substring(i,i+3));
				if(player.equals(winning.substring(i,i+3)))
					return 1;
				if(enemy.equals(winning.substring(i,i+3)))
					return -1;
			}
        }
        return 0; 
	}

	public String getWinState(){
		return this.winning;
	}

	public double getMinGameScore() { return REWARD_NEGATIVE; }
	public double getMaxGameScore() { return REWARD_POSITIVE; }

	public String getName() { return "Penney's Game";	}

	/**
	 * Advance the current state with 'action' to a new state
	 * @param action
	 */
	public void advance(ACTIONS action) {
		int iAction = action.toInt();
		assert (0<=iAction && iAction<2) : "iAction is not in 0,1.";
    	m_Table[m_Player][turn] = iAction+1;
    	if(m_Player==0)
    		turn++;
		m_Player = m_Player > 0?0:1; // alternating between 0 and 1
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
		allActions.add(ACTIONS.fromInt(0));
		allActions.add(ACTIONS.fromInt(1));
        return allActions;
    }
    
	public ArrayList<ACTIONS> getAvailableActions() {
		return availableActions;
	}
	
	public int getNumAvailableActions() {
		return availableActions.size();
	}

	/**
	 * For the Game the both options Heads or Tails are available all the time.
	 * Set them in member ACTIONS[] actions.
	 */
	public void setAvailableActions() {
		availableActions.clear();
		availableActions.add(ACTIONS.fromInt(0));
		availableActions.add(ACTIONS.fromInt(1));
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
		return m_Player==0?1:0;// (-m_Player+1)/2;
	}
	
	public int getNumPlayers() {
		return 2;				// Penney's Game is a 2-player game
	}
}
