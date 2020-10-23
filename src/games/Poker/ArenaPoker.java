package src.games.Poker;

import TournamentSystem.tools.TSGameDataTransfer;
import controllers.PlayAgent;
import games.*;
import src.games.PenneysGame.EvaluatorPenney;
import src.games.PenneysGame.FeaturePenney;
import src.games.PenneysGame.GameBoardPenney;
import src.games.PenneysGame.XNTupleFuncsPenney;
import tools.ScoreTuple;
import tools.Types;

import java.io.IOException;


public class ArenaPoker extends Arena   {

	public ArenaPoker(String title, boolean withUI) {
		super(title,withUI);		
	}
	
	/**
	 * @return a name of the game, suitable as subdirectory name in the 
	 *         {@code agents} directory
	 */
	public String getGameName() {
		return "Poker";
	}

	/**
	 * Factory pattern method: make a new GameBoard 
	 * @return	the game board
	 */
	public GameBoard makeGameBoard() {
		gb = new GameBoardPoker(this);
		return gb;
	}

	/**
	 * Factory pattern method: make a new Evaluator
	 * @param pa		the agent to evaluate
	 * @param gb		the game board
	 * @param stopEval	the number of successful evaluations needed to reach the 
	 * 					evaluator goal (may be used during training to stop it 
	 * 					prematurely)
	 * @param mode		which evaluator mode: 0,1,2,9. Throws a runtime exception 
	 * 					if {@code mode} is not in the set {@link Evaluator#getAvailableModes()}.
	 * @param verbose	how verbose or silent the evaluator is
	 * @return
	 */
	public Evaluator makeEvaluator(PlayAgent pa, GameBoard gb, int stopEval, int mode, int verbose) {
		return new EvaluatorPoker(pa,gb,stopEval,mode,verbose);
	}
	
	public Feature makeFeatureClass(int featmode) {
		return new FeaturePoker(featmode);
	}
	
	public XNTupleFuncs makeXNTupleFuncs() {
		return new XNTupleFuncsPoker();
	}

	public void performArenaDerivedTasks() {  }

	/**
	 * Build the game-over string (helper for {@link #PlayGame()}, to be shown in MessageBox).
	 * <p>
	 * Generic function is not suitable for >3 players.
	 *
	 * @param so			the game-over state
	 * @param agentVec		the names of all agents
	 * @param spDT			needed only in the tournament-case
	 * @return
	 */
	@Override
	public String gameOverString(StateObservation so, String[] agentVec, TSGameDataTransfer spDT) {
		ScoreTuple sc = so.getGameScoreTuple();
		String goStr="";
		StateObserverPoker sop = (StateObserverPoker) so;
		for(int i = 0;i<so.getNumPlayers();i++){
			if(sop.getChips()[i]>0) {
				goStr+= Types.GUI_PLAYER_NAME[i]+ " has won!";
				break;
			}
		}
		return goStr;
	}
	
	/**
	 * Start GBG for Penney's Game
	 * 
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException 
	{
		PokerLog.setup();
		ArenaPoker t_Frame = new ArenaPoker("General Board Game Playing",true);

		if (args.length==0) {
			t_Frame.init();
		} else {
			throw new RuntimeException("[Arena.main] args="+args+" not allowed.");
		}
	}
	
}
