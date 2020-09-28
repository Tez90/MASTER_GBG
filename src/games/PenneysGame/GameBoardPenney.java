package src.games.PenneysGame;

import controllers.PlayAgent;
import games.Arena;
import games.Arena.Task;
import games.GameBoard;
import games.StateObservation;
import games.TicTacToe.GameBoardTTTGui;
import tools.Types;

import java.util.ArrayList;
import java.util.Random;

/**
 * This class implements the GameBoard interface for Poker.
 * Its member {@link GameBoardTTTGui} {@code m_gameGui} has the game board GUI.
 * {@code m_gameGui} may be {@code null} in batch runs. 
 * <p>
 * It implements the interface functions and has the user interaction methods HGameMove and 
 * InspectMove (used to enter legal moves during game play or 'Inspect'), 
 * since these methods need access to local  members. They are called from {@link GameBoardTTTGui}'s
 * action handlers
 * 
 * @author Tim Zeh 2020
 *
 */
public class GameBoardPenney implements GameBoard {

	protected Arena  m_Arena;		// a reference to the Arena object, needed to
									// infer the current taskState
	protected Random rand;
	private transient GameBoardPenneyGui m_gameGui = null;

	protected StateObserverPenney m_so;
	private boolean arenaActReq=false;

	public GameBoardPenney(Arena penneyGame) {
		initGameBoard(penneyGame);
	}
	
    @Override
    public void initialize() {}

    private void initGameBoard(Arena arGame) 
	{
		m_Arena		= arGame;
		m_so		= new StateObserverPenney();	// empty table
        rand 		= new Random(System.currentTimeMillis());	

        if (m_Arena.hasGUI() && m_gameGui==null) {
        	m_gameGui = new GameBoardPenneyGui(this);
        }

	}

	@Override
	public void clearBoard(boolean boardClear, boolean vClear) {
		if (boardClear) {
			m_so = new StateObserverPenney();			// empty Table
		}

							// considerable speed-up during training (!)
        if (m_gameGui!=null && m_Arena.taskState!= Task.TRAIN)
			m_gameGui.clearBoard(boardClear, vClear);


	}

	/**
	 * Update the play board and the associated values (labels).
	 * 
	 * @param so	the game state
	 * @param withReset  if true, reset the board prior to updating it to state so
	 * @param showValueOnGameboard	if true, show the game values for the available actions
	 * 				(only if they are stored in state {@code so}).
	 */
	@Override
	public void updateBoard(StateObservation so, 
							boolean withReset, boolean showValueOnGameboard) {
		StateObserverPenney soT = (StateObserverPenney) so;
		if (so!=null) {
	        assert (so instanceof StateObserverPenney): "StateObservation 'so' is not an instance of StateObserverPoker";
			m_so = soT.copy();
		} // if(so!=null)

		if (m_gameGui!=null)
			m_gameGui.updateBoard(soT, withReset, showValueOnGameboard);
	}

	/**
	 * @return  true: if an action is requested from Arena or ArenaTrain
	 * 			false: no action requested from Arena, next action has to come 
	 * 			from GameBoard (e.g. user input / human move) 
	 */
	@Override
	public boolean isActionReq() {
		return arenaActReq;
	}

	/**
	 * @param	actReq true : GameBoard requests an action from Arena 
	 * 			(see {@link #isActionReq()})
	 */
	@Override
	public void setActionReq(boolean actReq) {
		arenaActReq=actReq;
	}

	protected void HGameMove(int x)
	{
		int iAction = x;
		Types.ACTIONS act = Types.ACTIONS.fromInt(iAction);
		assert m_so.isLegalAction(act) : "Desired action is not legal";
		m_so.advance(act);			// perform action (optionally add random elements from game 
									// environment - not necessary in TicTacToe)
		(m_Arena.getLogManager()).addLogEntry(act, m_so, m_Arena.getLogSessionID());
//		updateBoard(null,false,false);
		arenaActReq = true;			// ask Arena for next action
	}
	
	protected void InspectMove(int x)
	{
		int iAction = x;
		Types.ACTIONS act = Types.ACTIONS.fromInt(iAction);
		if (!m_so.isLegalAction(act)) {
			System.out.println("Desired action is not legal!");
			m_Arena.setStatusMessage("Desired action is not legal");
			return;
		} else {
			m_Arena.setStatusMessage("Inspecting the value function ...");
		}
		m_so.advance(act);			// perform action (optionally add random elements from game 
									// environment - not necessary in TicTacToe)
//		updateBoard(null,false,false);
		arenaActReq = true;		
	}
	/*
	public StateObservation getStateObs() {
		return m_so;
	}
*/
	public StateObserverPenney getStateObs() {
		return m_so;
	}

	/**
	 * @return the 'empty-board' start state
	 */
	@Override
	public StateObservation getDefaultStartState() {
		clearBoard(true, true);
		return m_so;
	}

	/**
	 * @return a start state which is with probability 0.5 the default start state 
	 * 		start state and with probability 0.5 one of the possible one-ply 
	 * 		successors
	 */
	@Override
	public StateObservation chooseStartState() {
		getDefaultStartState();			// m_so is in default start state

		if (rand.nextDouble()>0.5) {
			// choose randomly one of the possible actions in default 
			// start state and advance m_so by one ply
			ArrayList<Types.ACTIONS> acts = m_so.getAvailableActions();
			int i = (int) (rand.nextInt(acts.size()));
			m_so.advance(acts.get(i));
		}
		return m_so;
	}

	@Override
    public StateObservation chooseStartState(PlayAgent pa) {
    	return chooseStartState();
    }

	@Override
	public String getSubDir() {
		return null;
	}
	
    @Override
    public Arena getArena() {
        return m_Arena;
    }
    
	@Override
	public void enableInteraction(boolean enable) {
		if (m_gameGui!=null)
			m_gameGui.enableInteraction(enable);
	}

	@Override
	public void showGameBoard(Arena pokerGame, boolean alignToMain) {
		if (m_gameGui!=null)
			m_gameGui.showGameBoard(pokerGame, alignToMain);
	}

 @Override
	public void toFront() {
		if (m_gameGui!=null)
			m_gameGui.toFront();
	}

 @Override
 public void destroy() {
		if (m_gameGui!=null)
			m_gameGui.destroy();
 }

}
