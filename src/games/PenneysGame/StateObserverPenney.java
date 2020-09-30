package src.games.PenneysGame;

import games.ObserverBase;
import games.StateObservation;
import games.TicTacToe.TicTDBase;
import tools.ScoreTuple;
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

    private static final int STARTING_PLAYER = 1;

    private static final int MOVES = 3;

	private int m_Player;			// player who makes the next move (0/1)

	private int winner;

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

	/**
	 * Constructor to create a new empty StateObserver for Penney's Game.
	 */
	public StateObserverPenney() {

		s_p0 = "";
		s_p1 = "";

		//Starting Player
		m_Player = STARTING_PLAYER;

		setAvailableActions();
		winner = -1;
		winning = "";
		winning_spot = 0;
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
		this.winning_spot = other.winning_spot;
		this.winner = other.winner;
		setAvailableActions();
	}

	/**
	 * Creates a copy of the StateObserver.
	 * @return a copy of the StateObserver.
	 */
	public StateObserverPenney copy() {
		StateObserverPenney sop = new StateObserverPenney(this);
		return sop;
	}

	/**
		@return if the game is over
	 */
    @Override
	public boolean isGameOver() {
		// the last action of the game is done by player 1
		// after the defined number of movesq
		return this.s_p1.length() == MOVES;
	}

	/**
	 * Because coin tosses are involved the game is not deterministic.
	 * @return FALSE
	 */
    @Override
	public boolean isDeterministicGame() {
		return false;
	}

	/**
	 * The rewards are only in the final state of the game.
	 * @return TRUE
	 */
    @Override
	public boolean isFinalRewardGame() {
		return true;
	}

	/**
	 * For now I assume that the game can't get into a illegal state.
	 * @return TRUE
	 */
    @Override
	public boolean isLegalState() {
		return true;
	}

	/**
	 * All actions (Heads and Tails) are legal for each move.
	 * @param act inteded action in a given state
	 * @return is that a legal action?
	 */
	public boolean isLegalAction(ACTIONS act) {
		return true;
	}

	/**
	 * @return String representation of the current state
	 */
	@Deprecated
    public String toString() {
    	return stringDescr();
    }

	/**
	 * @return String representation of the current state
	 */
	@Override
    public String stringDescr() {
		String sout = "";
		sout += "\r\nPlayer 0: " + s_p0;
		sout += "\r\nPlayer 1: " + s_p1;
 		return sout;
	}

	/**
	 * Returns the String representation of a certain player
	 * @param x Player
	 * @return String representation of the player's selection
	 */
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
	 * 			For Penney's Game only game-over states have a non-zero game score.
	 * 			It is the reward from the perspective of {@code refer}.
	 */
	public double getGameScore(StateObservation refer) {
		if(isGameOver()) {
			int player_i = refer.getPlayer();
			if(this.winner==-1)
				return 0;
			if(this.winner==player_i)
				return 1;
			return -1;
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
    	if(m_Player==1)
			s_p0 += Integer.toString(iAction+1);
    	else
			s_p1 += Integer.toString(iAction+1);

		// Switch after wull selection
		if(s_p0.length()==MOVES) {
			m_Player = -1;
			if(s_p1.length() == MOVES)
				findWinner();
		}

		super.incrementMoveCounter();
	}

	/**
	 * "tosses a coin" to determine the winner of the game.
	 */
	private void findWinner(){
		if(s_p0.length() == MOVES && s_p1.length() == MOVES && this.winning.equals("")){
			if(s_p0.equals(s_p1)) {
				winner = -1;
			}else{
				int x = 0;
				for (int i = 0; i < MOVES; i++) {
					winning += Integer.toString((random.nextInt(2) + 1));
				}
				do{
					String check = winning.substring(x);
					if(s_p0.equals(check)) {
						winner = 0;
						break;
					}
					if(s_p1.equals(check)) {
						winner = 1;
						break;
					}
					winning += Integer.toString((random.nextInt(2) + 1));
					x++;
				}while(true);
				winning_spot = x;

				System.out.println("-------------------------------");
				System.out.println(System.currentTimeMillis());
				System.out.println("The Winner is Player "+winner+
						"\r\nPlayer O:"+s_p0+
						"\r\nPlayer X:"+s_p1+
						"\r\nCointosses:"+winning+
						"\r\nWinning:"+winning_spot);
				System.out.println("-------------------------------");
			}
		}
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

	/**
	 *  Returns the gamestate in form of a two dimensional array.
	 */
	public int[][] getTable() {
		int[][] table = new int[2][MOVES];
		for(int i = 0;i<MOVES;i++){
			if(i < s_p0.length())
				table[0][i] = s_p0.charAt(i);
			if(i < s_p1.length())
				table[1][i] = s_p1.charAt(i);
		}
		return table;
	}

	/**
	 * @return 	{0,1} for the player to move in this state 
	 * 			Player 0 is X, the player who starts the game. Player 1 is O.
	 */
	public int getPlayer() {
		return (-m_Player+1)/2;
	}
	
	public int getNumPlayers() {
		return 2;				// Penney's Game is a 2-player game
	}
}
