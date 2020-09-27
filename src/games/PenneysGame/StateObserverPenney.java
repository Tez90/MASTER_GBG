package src.games.PenneysGame;

import games.ObserverBase;
import games.StateObservation;
import games.TicTacToe.TicTDBase;
import tools.Types.ACTIONS;

import javax.print.DocFlavor;
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

    private static final int STARTING_PLAYER = 0;

    private static final int MOVES = 5;

	private int m_Player;			// player who makes the next move (0/1)

	private Random random = new Random();
	protected ArrayList<ACTIONS> availableActions = new ArrayList();	// holds all available actions
	private String winning;

	private String s_p0, s_p1;

	private int winning_spot;
	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .gamelog containing this object will become 
	 * unreadable or you have to provide a special version transformation)
	 */
	private static final long serialVersionUID = 1L;

	public StateObserverPenney() {

		s_p0 = "";
		s_p1 = "";

		//Starting Player
		m_Player = STARTING_PLAYER;

		setAvailableActions();
		winning = "";

		// hidden information
		for(int i = 0;i<50;i++) {
			winning += Integer.toString((random.nextInt(2)+1));
		}
	}

	/**
	 * Constructor to create a copy of an existing StateObserverPenney.
	 * @param other StateObserver that should be copied
	 */
	public StateObserverPenney(StateObserverPenney other) {

		this.s_p0 = other.s_p0;
		this.s_p1 = other.s_p1;
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
		// the last action of the game is done by player 1
		// after the defined number of moves
		return this.s_p1.length() == MOVES;
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
		sout += "\r\nPlayer 0: " + s_p0;
		sout += "\r\nPlayer 1: " + s_p1;
 		return sout;
	}

	public String getDescPlayer(int x){
		return x == 0 ? s_p0:s_p1;
	}
	
	/**
	 * @return true, if the current position is a win (for either player)
	 */
	public boolean win() {
		return(isGameOver()&&!s_p0.equals(s_p1));
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
			String player = player_i == 0?s_p0:s_p1;
			String enemy = enemy_i == 0?s_p0:s_p1;

			if(player.equals(enemy))
				return 0 ;

			String check = "";

			for(int i = 0; i<this.winning.length()-MOVES+1;i++){
				check = winning.substring(i,i+MOVES);
				if(player.equals(check)) {
					this.winning_spot = i;
					return 1;
				}
				if(enemy.equals(check)) {
					this.winning_spot = i;
					return -1;
				}
			}
        }
        return 0; 
	}

	public String getWinning(){
		return this.winning;
	}
	public int getWinning_spot() {return this.winning_spot;}
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
    	//m_Table[m_Player][turn] = iAction+1;
    	if(m_Player==0)
			s_p0 += Integer.toString(iAction+1);
    	else
			s_p1 += Integer.toString(iAction+1);

    	m_Player = m_Player > 0?0:1; // alternating between 0 and 1
		//if(m_Player==STARTING_PLAYER)
		//	turn++;
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
		return new int[2][MOVES];
	}

	/**
	 * @return 	{0,1} for the player to move in this state 
	 * 			Player 0 is X, the player who starts the game. Player 1 is O.
	 */
	public int getPlayer() {
		return m_Player;
	}
	
	public int getNumPlayers() {
		return 2;				// Penney's Game is a 2-player game
	}
}
