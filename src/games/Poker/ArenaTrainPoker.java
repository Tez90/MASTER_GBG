package src.games.Poker;

import TournamentSystem.tools.TSGameDataTransfer;
import controllers.PlayAgent;
import games.*;
import games.TicTacToe.EvaluatorTTT;
import games.TicTacToe.FeatureTTT;
import games.TicTacToe.GameBoardTTT;
import games.TicTacToe.XNTupleFuncsTTT;
import src.games.PenneysGame.EvaluatorPenney;
import src.games.PenneysGame.FeaturePenney;
import src.games.PenneysGame.GameBoardPenney;
import tools.ScoreTuple;
import tools.Types;

import java.io.IOException;

/**
 * {@link ArenaTrain} for TicTacToe. It borrows all functionality
 * from the general class {@link ArenaTrain} derived from {@link Arena}. It only overrides 
 * the abstract methods <ul>
 * <li> {@link Arena#makeGameBoard()}, 
 * <li> {@link Arena#makeEvaluator(PlayAgent, GameBoard, int, int, int)}, and 
 * <li> {@link Arena#makeFeatureClass(int)}, 
 * <li> {@link Arena#makeXNTupleFuncs()}, 
 * </ul> 
 * such that these factory methods return objects of class {@link GameBoardTTT},
 * {@link EvaluatorTTT}, {@link FeatureTTT}, and {@link XNTupleFuncsTTT}, respectively.
 * 
 * @see GameBoardTTT
 * @see EvaluatorTTT
 * 
 * @author Wolfgang Konen, TH Koeln, 2016-2020
 */
public class ArenaTrainPoker extends ArenaTrain   {

	public ArenaTrainPoker(String title, boolean withUI) {
		super(title,withUI);		
	}
	
	/**
	 * @return a name of the game, suitable as subdirectory name in the 
	 *         {@code agents} directory
	 */
	public String getGameName() {
		return "TicTacToe";
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
	 * @param mode		which evaluator mode. Throws a runtime exception 
	 * 					if {@code mode} is not in the set {@link EvaluatorTTT#getAvailableModes()}.
	 * @param verbose	how verbose or silent the evaluator is
	 * @return
	 */
	public Evaluator makeEvaluator(PlayAgent pa, GameBoard gb, int stopEval, int mode, int verbose) {
		return new EvaluatorPenney(pa,gb,stopEval,mode,verbose);
	}

	public Feature makeFeatureClass(int featmode) {
		return new FeaturePenney(featmode);
	}

	public XNTupleFuncs makeXNTupleFuncs() {
		return new XNTupleFuncsTTT();
	}

	/**
	 * Start GBG for TicTacToe (trainable version)
	 * 
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException 
	{
		PokerLog.setup();
		ArenaTrainPoker t_Frame = new ArenaTrainPoker("General Board Game Playing",true);

// ---  just for analysis: compute the state space & game tree complexity ---		
//		System.out.println("Rough approximation for nStates = "+(int) Math.pow(3, 9)+ " = (3^9)");
//		TicTDBase.countStates2(false);
//		TicTDBase.countStates2(true);
		
		if (args.length==0) {
			t_Frame.init();
		} else {
			throw new RuntimeException("[ArenaTrainPenney.main] args="+args+" not allowed.");
		}
	}
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
}
