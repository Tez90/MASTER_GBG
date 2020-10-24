package src.games.Poker;

import games.Arena;
import games.Arena.Task;
import tools.ScoreTuple;
import tools.Types;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Random;

public class GameBoardPokerGui extends JFrame {

	private int POKERGAMEHEIGHT =280;
	private GameBoardPoker m_gb=null;
	private pokerForm pf;

	// the colors of the TH Koeln logo (used for button coloring):
	private Color colTHK1 = new Color(183,29,13);
	private Color colTHK2 = new Color(255,137,0);
	private Color colTHK3 = new Color(162,0,162);

	private JLabel[] playerNames;
	private JLabel[] playerChips;
	private JLabel[] playerActive;
	private JLabel[] playerCall;
	private JPanel[] playerData;

	public GameBoardPokerGui(GameBoardPoker gb) {
		super("Poker");
		m_gb = gb;
		initGui("");
	}

	private void initPlayerGUI(){
		playerNames = new JLabel[StateObserverPoker.NUM_PLAYER];
		playerChips = new JLabel[StateObserverPoker.NUM_PLAYER];
		playerActive = new JLabel[StateObserverPoker.NUM_PLAYER];
		playerCall = new JLabel[StateObserverPoker.NUM_PLAYER];
		playerData = new JPanel[StateObserverPoker.NUM_PLAYER];

		for(int i = 0 ; i < StateObserverPoker.NUM_PLAYER ; i++){
			playerNames[i] = new JLabel("Player "+Types.GUI_PLAYER_NAME[i]+": ");
			playerActive[i] = new JLabel();
			playerCall[i] = new JLabel();
			playerChips[i] = new JLabel();

			playerData[i] = new JPanel();

			playerData[i].add(playerNames[i]);
			playerData[i].add(playerActive[i]);
			playerData[i].add(playerCall[i]);
			playerData[i].add(playerChips[i]);

			playerData[i].setLayout(new GridLayout(2,2));
			pf.addPlayerData(playerData[i]);
		}
		updatePlayerInfo();
	}


    private void initGui(String title) 
	{
		pf = new pokerForm(m_gb,StateObserverPoker.NUM_PLAYER);
		initPlayerGUI();
		add(pf.gameBoardPanel);
		pack();
		setVisible(false);		// note that the true size of this component is set in 
								// showGameBoard(Arena,boolean)
	}

	// called from initGame() 
	private JPanel InitBoard()
	{
		JPanel panel=new JPanel();
		return panel;
	}
	
	public void clearBoard(boolean boardClear, boolean vClear) {

	}

	public void updatePlayerInfo(){
		double[] chips = m_gb.m_so.getChips();
		boolean[] active = m_gb.m_so.getActivePlayers();
		for(int i = 0 ; i < StateObserverPoker.NUM_PLAYER ; i++){
			playerChips[i].setText(Double.toString(chips[i]));
			playerActive[i].setText(Boolean.toString(active[i]));
		}
	}

	/**
	 * Update the play board and the associated values (labels).
	 * 
	 * @param soT	the game state
	 * @param withReset  if true, reset the board prior to updating it to state so
	 * @param showValueOnGameboard	if true, show the game values for the available actions
	 * 				(only if they are stored in state {@code so}).
	 */
	public void updateBoard(StateObserverPoker soT,
                            boolean withReset, boolean showValueOnGameboard) {
		updatePlayerInfo();
		pf.updatePot(m_gb.m_so.getPot());
		pf.updateHoleCards(m_gb.m_so.getHoleCards());
		pf.updateCommunityCards(m_gb.m_so.getCommunityCards());
		pf.disableButtons();
		ArrayList<Types.ACTIONS> actions = m_gb.m_so.getAvailableActions();
		for (Types.ACTIONS action : actions) {
			switch (action.toInt()){
				case 0: // FOLD
					pf.enableFold();
					break;
				case 1: // CHECK
					pf.enableCheck();
					break;
				case 2: // BET
					pf.enableBet();
					break;
				case 3: // CALL
					pf.enableCall();
					break;
				case 4: // RAISE
					pf.enableRaise();
					break;
				case 5: // RAISE
					pf.enableAllIn();
					break;
			}
		}
		repaint();
	}

	public void disableButtons(){
	}

	/**
	 * Update the play board and the associated values (labels) to the state in m_so.
	 * The labels contain the values (scores) for each unoccupied board position (raw score*100).  
	 * Occupied board positions have black/white background color, unoccupied positions are orange.
	 * 
	 * @param enable As a side effect the buttons Board[i][j] which are occupied by either "X" or "O"
	 * will be set into enabled state <code>enable</code>. (All unoccupied positions will get state 
	 * <code>true</code>.)
	 */ 
	private void guiUpdateBoard(boolean enable, boolean showValueOnGameboard)
	{
		this.repaint();
	}

	public void enableInteraction(boolean enable) {

	}

	public void showGameBoard(Arena arena, boolean alignToMain) {
		this.setVisible(true);
		if (alignToMain) {
			// place window with game board below the main window
			int x = arena.m_xab.getX() + arena.m_xab.getWidth() + 20;
			int y = arena.m_xab.getLocation().y;
			if (arena.m_ArenaFrame!=null) {
				x = arena.m_ArenaFrame.getX();
				y = arena.m_ArenaFrame.getY() + arena.m_ArenaFrame.getHeight() +1;
				this.setSize(1200,400);
			}
			this.setLocation(x,y);	
		}
	}

	public void toFront() {
    	super.setState(JFrame.NORMAL);	// if window is iconified, display it normally
		super.toFront();
	}

   public void destroy() {
		this.setVisible(false);
		this.dispose();
   }

}
