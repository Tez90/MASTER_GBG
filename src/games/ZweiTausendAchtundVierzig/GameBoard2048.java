package games.ZweiTausendAchtundVierzig;

import games.Arena;
import games.GameBoard;
import games.StateObservation;
import tools.Types;

import controllers.PlayAgent;
import controllers.TD.ntuple2.TDNTuple2Agt;

/**
 * This class implements the GameBoard interface for 2048.
 * Its member {@link GameBoard2048Gui} {@code m_gameGui} has the game board GUI. 
 * {@code m_gameGui} may be {@code null} in batch runs. 
 * <p>
 * It implements the interface functions and has the user interaction methods HGameMove and 
 * InspectMove (used to enter legal moves during game play or 'Inspect'), 
 * since these methods need access to local  members. They are called from {@link GameBoard2048Gui}'s
 * action handlers
 * 
 * @author Johannes Kutsch, Wolfgang Konen, TH Koeln, 2016-2020
 */
public class GameBoard2048 implements GameBoard {
    protected Arena m_Arena;
    protected StateObserver2048 m_so;
    private boolean arenaActReq = false;
	private transient GameBoard2048Gui m_gameGui = null;

    public GameBoard2048(Arena ztavGame) {
        initGameBoard(ztavGame);
    }

    @Override
    public void initialize() {}

    private void initGameBoard(Arena ztavGame) {
        m_Arena = ztavGame;
        m_so = new StateObserver2048();
        if (m_Arena.hasGUI() && m_gameGui==null) {
        	m_gameGui = new GameBoard2048Gui(this);
        }
       
    }

    @Override
    public void clearBoard(boolean boardClear, boolean vClear) {
        if (boardClear) {
            m_so = new StateObserver2048();
        }
        					// considerable speed-up during training (!)
        if (m_gameGui!=null && m_Arena.taskState!=Arena.Task.TRAIN)
			m_gameGui.clearBoard(boardClear, vClear);
    }

    @Override
    public void updateBoard(StateObservation so,  
    						boolean withReset, boolean showValueOnGameboard) {
		StateObserver2048 soZTAV = null;
        if (so != null) {
	        assert (so instanceof StateObserver2048)
			: "StateObservation 'so' is not an instance of StateObserver2048";
            soZTAV = (StateObserver2048) so;
            m_so = soZTAV.copy();
        }
		if (m_gameGui!=null)
			m_gameGui.updateBoard(soZTAV, withReset, showValueOnGameboard);
    }

    @Override
    public boolean isActionReq() {
        return arenaActReq;
    }

    @Override
    public void setActionReq(boolean actionReq) {
        arenaActReq = actionReq;
    }

    @Override
    public StateObservation getStateObs() {
        return m_so;
    }

    @Override
    public StateObservation getDefaultStartState() {
        clearBoard(true, true);
//        if (TDNTuple2Agt.DBG2_FIXEDSEQUENCE) 
//        	return new StateObserver2048();
        return m_so;
    }

    @Override
    public StateObservation chooseStartState() {
        return getDefaultStartState();
    }

	@Override
    public StateObservation chooseStartState(PlayAgent pa) {
    	return chooseStartState();
    }
	
    protected void HGameMove(int move) {
        Types.ACTIONS act = Types.ACTIONS.fromInt(move);
        assert m_so.isLegalAction(act) : "Desired action is not legal";
        m_so.advance(act);
		(m_Arena.getLogManager()).addLogEntry(act, m_so, m_Arena.getLogSessionID());
        arenaActReq = true;            // ask Arena for next action
    }

    protected void InspectMove(int move) {
        Types.ACTIONS act = Types.ACTIONS.fromInt(move);
        assert m_so.isLegalAction(act) : "Desired action is not legal";
        m_so.advance(act);
        arenaActReq = true;
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
	public void showGameBoard(Arena arena, boolean alignToMain) {
		if (m_gameGui!=null)
			m_gameGui.showGameBoard(arena, alignToMain);
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
