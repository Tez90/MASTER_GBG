package src.games.PenneysGame;

import games.Arena;
import games.Arena.Task;
import tools.ScoreTuple;
import tools.Types;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

/**
 * Graphical User Interface for a simple version of the Penney's Game.
 *
 * @author Tim Zeh
 *
 */
public class GameBoardPenneyGui extends JFrame {

	private int GAMEHEIGHT = 500;

	private JPanel BoardPanel;

	private JLabel leftInfo=new JLabel("");
	private JLabel rightInfo=new JLabel("");

	protected Random rand;

	private JLabel gameStatePlayer0 = new JLabel("");
	private JLabel gameStatePlayer1 = new JLabel("");

	private JLabel winState=new JLabel("?");

	private int[][] Table;			// =1: position occupied by "X" player
									//=-1: position occupied by "O" player
	private double[][] VTable;

	/**
	 * a reference to the 'parent' {@link GameBoardPenney} object
	 */
	private GameBoardPenney m_gb=null;

	// the colors of the TH Koeln logo (used for button coloring):
	private Color colTHK1 = new Color(183,29,13);
	private Color colTHK2 = new Color(255,137,0);
	private Color colTHK3 = new Color(162,0,162);

	public GameBoardPenneyGui(GameBoardPenney gb) {
		super("Penney's Game");
		m_gb = gb;
		initGui("");
	}
	
    private void initGui(String title) 
	{
		gameStatePlayer0 = new JLabel();
		gameStatePlayer1 = new JLabel();
		BoardPanel	= InitBoard();

		Table       = new int[3][3];
		VTable		= new double[3][3];

        rand 		= new Random(System.currentTimeMillis());	

		JPanel titlePanel = new JPanel();

		titlePanel.setBackground(Types.GUI_BGCOLOR);

		JLabel Blank = new JLabel(" My Title ");		// a little bit of space

		titlePanel.add(Blank);

		JPanel boardPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

		boardPanel.add(BoardPanel);
		boardPanel.setBackground(Types.GUI_BGCOLOR);
		
		JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		infoPanel.setBackground(Types.GUI_BGCOLOR);

		Font font=new Font("Arial",0,(int)(1.2*Types.GUI_HELPFONTSIZE));			
		leftInfo.setFont(font);	
		rightInfo.setFont(font);	

		infoPanel.add(leftInfo);
		infoPanel.add(rightInfo);
		
		setLayout(new BorderLayout(10,0));

		add(titlePanel,BorderLayout.NORTH);

		add(BoardPanel,BorderLayout.CENTER);

		add(infoPanel,BorderLayout.SOUTH);

		pack();

		setVisible(false);		// note that the true size of this component is set in 
								// showGameBoard(Arena,boolean)
	}

	// called from initGame() 
	private JPanel InitBoard()
	{
		JPanel panel=new JPanel();
		panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));


		Font font=new Font("Arial",Font.BOLD,Types.GUI_HELPFONTSIZE);
		int buSize = (int)(60*Types.GUI_SCALING_FACTOR_X);
		Dimension minimumSize = new Dimension(buSize,buSize);

		JPanel actionPanel = new JPanel();
		//actionPanel.setLayout(new GridLayout(1,2));
		actionPanel.setLayout(new FlowLayout());
		JButton headsButton = new JButton("H");

			headsButton.setFont(font);
			headsButton.setEnabled(true);
			headsButton.setPreferredSize(minimumSize);

			headsButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Task aTaskState = m_gb.m_Arena.taskState;
					if (aTaskState == Task.PLAY)
					{
						m_gb.HGameMove(0);		// i.e. make human move (i,j), if Board[i][j] is clicked
					}
					if (aTaskState == Task.INSPECTV)
					{
						m_gb.InspectMove(0);	// i.e. update inspection, if Board[i][j] is clicked
					}
					int dummy=1;
				}
			});

		actionPanel.add(headsButton);
		JButton tailsButton = new JButton("T");

		tailsButton.setFont(font);
		tailsButton.setEnabled(true);
		tailsButton.setPreferredSize(minimumSize);

		tailsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Task aTaskState = m_gb.m_Arena.taskState;
				if (aTaskState == Task.PLAY)
				{
					m_gb.HGameMove(1);		// i.e. make human move (i,j), if Board[i][j] is clicked
				}
				if (aTaskState == Task.INSPECTV)
				{
					m_gb.InspectMove(1);	// i.e. update inspection, if Board[i][j] is clicked
				}
				int dummy=1;
			}
		});

		actionPanel.add(tailsButton);

		JPanel infoPanelP0 = new JPanel();
		infoPanelP0.setLayout(new FlowLayout());
		infoPanelP0.add(new Label("Player O: "));
		infoPanelP0.add(gameStatePlayer0);
		gameStatePlayer0.setText("");


		JPanel infoPanelP1 = new JPanel();
		infoPanelP1.setLayout(new FlowLayout());
		infoPanelP1.add(new Label("Player 1: "));
		infoPanelP1.add(gameStatePlayer1);
		gameStatePlayer1.setText("");

		JPanel infoPanelWin = new JPanel();
		infoPanelWin.setLayout(new FlowLayout());
		infoPanelWin.add(new Label("Winning: "));
		infoPanelWin.add(winState);

		panel.add(actionPanel);
		panel.add(infoPanelP0);
		panel.add(infoPanelP1);
		panel.add(infoPanelWin);
		return panel;
	}
	
	public void clearBoard(boolean boardClear, boolean vClear) {
		if (boardClear) {
			gameStatePlayer0.setText("");
			gameStatePlayer1.setText("");
			winState.setText("");
		}
		if (vClear) {
			// reset virtual board?
		}
	}

	/**
	 * Update the play board and the associated values (labels).
	 * 
	 * @param soP	the game state
	 * @param withReset  if true, reset the board prior to updating it to state so
	 * @param showValueOnGameboard	if true, show the game values for the available actions
	 * 				(only if they are stored in state {@code so}).
	 */
	public void updateBoard(StateObserverPenney soP,
							boolean withReset, boolean showValueOnGameboard) {
		int i,j;

		if (soP!=null) {

			//int Player=Types.PLAYER_PM[soP.getPlayer()];
			int Player=soP.getPlayer();
			switch(Player) {
			case(0):
				leftInfo.setText("Player 0 to move"); break;
			case(1):
				leftInfo.setText("Player 1 to move"); break;
			}

			if (soP.isGameOver()) {
				ScoreTuple sc = soP.getGameScoreTuple();
				int winner = sc.argmax();
				if (sc.max()==0.0) winner = -2;	// tie indicator
				switch(winner) {
				case( 0 ):
					leftInfo.setText("Player 0 has won"); break;
				case( 1 ):
					leftInfo.setText("Player 1 has won"); break;
				case(-2):
					leftInfo.setText("Tie         "); break;
				}

				int spot = soP.getWinning_spot();
				String tosses = soP.getWinning().replace("1","H").replace("2","T");

				winState.setText(tosses.substring(0,spot)+" || "+tosses.substring(spot,spot+3)+" || "+tosses.substring(spot+3));
				this.repaint();
			}
			/*
			if (showValueOnGameboard) {
				if (soP.getStoredValues()!=null) {
					for(i=0;i<3;i++)
						for(j=0;j<3;j++) 
							VTable[i][j]=Double.NaN;	
					
					for (int k=0; k<soP.getStoredValues().length; k++) {
						Types.ACTIONS action = soP.getStoredAction(k);
						int iAction = action.toInt();
						j=iAction%3;
						i=(iAction-j)/3;
						VTable[i][j] = soP.getStoredValues()[k];
					}	
				}

				String splus = (m_gb.m_Arena.taskState == Task.INSPECTV) ? "X" : "O";
				String sminus= (m_gb.m_Arena.taskState == Task.INSPECTV) ? "O" : "X";
				switch(Player) {
				case(+1): 
					rightInfo.setText("    Score for " + splus); break;
				case(-1):
					rightInfo.setText("    Score for " + sminus); break;
				}					
			} else {
				rightInfo.setText("");					
			}

			*/
		} // if(so!=null)
		
		guiUpdateBoard(false,showValueOnGameboard);
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
		double value, maxvalue=Double.NEGATIVE_INFINITY;
		String valueTxt;
		gameStatePlayer0.setText(this.m_gb.getStateObs().getDescPlayer(0).replace("1","H").replace("2","T"));
		gameStatePlayer1.setText(this.m_gb.getStateObs().getDescPlayer(1).replace("1","H").replace("2","T"));
		this.repaint();
//		paint(this.getGraphics());   // this sometimes leave one of the buttons un-painted
	}		

	public void enableInteraction(boolean enable) {

	}

	public void showGameBoard(Arena arena, boolean alignToMain) {
		this.setVisible(true);
		if (alignToMain) {
			// place window with game board below the main window
			int x = arena.m_xab.getX() + arena.m_xab.getWidth() + 8;
			int y = arena.m_xab.getLocation().y;
			if (arena.m_ArenaFrame!=null) {
				x = arena.m_ArenaFrame.getX();
				y = arena.m_ArenaFrame.getY() + arena.m_ArenaFrame.getHeight() +1;
				this.setSize(arena.m_ArenaFrame.getWidth(),
							 (int)(Types.GUI_SCALING_FACTOR_Y* GAMEHEIGHT));
			}
			this.setLocation(x,y);	
		}		
//		System.out.println("GameBoardTTT size = " +super.getWidth() + " * " + super.getHeight());
//		System.out.println("Arena size = " +ticGame.m_ArenaFrame.getWidth() + " * " + ticGame.m_ArenaFrame.getHeight());

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
