package src.games.PenneysGame;

import controllers.PlayAgent;
import games.*;
import games.TicTacToe.EvaluatorTTT;
import games.TicTacToe.FeatureTTT;
import games.TicTacToe.GameBoardTTT;
import games.TicTacToe.XNTupleFuncsTTT;

import java.io.IOException;

/**
 * {@link Arena} for TicTacToe. It borrows all functionality
 * from the general class {@link Arena}. It only overrides 
 * the abstract methods <ul>
 * <li> {@link Arena#makeGameBoard()}, 
 * <li> {@link Arena#makeEvaluator(PlayAgent, GameBoard, int, int, int)}, and 
 * <li> {@link Arena#makeFeatureClass(int)}, 
 * <li> {@link Arena#makeXNTupleFuncs()}, 
 * </ul> 
 * such that these factory methods return objects of class {@link GameBoardTTT},
 * {@link EvaluatorTTT}, {@link FeatureTTT}, and {@link XNTupleFuncsTTT}, respectively.
 * <p>
 * {@link ArenaPenney} has a short {@link #main(String[])} for launching the non-trainable
 * version of GBG. 
 * 
 * @see GameBoardTTT
 * @see EvaluatorTTT
 * 
 * @author Wolfgang Konen, TH Koeln, Nov'16
 */
public class ArenaPenney extends Arena   {
	
	public ArenaPenney(String title, boolean withUI) {
		super(title,withUI);		
	}
	
	/**
	 * @return a name of the game, suitable as subdirectory name in the 
	 *         {@code agents} directory
	 */
	public String getGameName() {
		return "Penney's Game";
	}
	
	/**
	 * Factory pattern method: make a new GameBoard 
	 * @return	the game board
	 */
	public GameBoard makeGameBoard() {
		gb = new GameBoardPenney(this);
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
		return new EvaluatorPenney(pa,gb,stopEval,mode,verbose);
	}
	
	public Feature makeFeatureClass(int featmode) {
		return new FeaturePenney(featmode);
	}
	
	public XNTupleFuncs makeXNTupleFuncs() {
		return new XNTupleFuncsPenney();
	}

	public void performArenaDerivedTasks() {  }

	
	/**
	 * Start GBG for Penney's Game
	 * 
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException 
	{
		ArenaPenney t_Frame = new ArenaPenney("General Board Game Playing",true);

		if (args.length==0) {
			t_Frame.init();
		} else {
			throw new RuntimeException("[Arena.main] args="+args+" not allowed. Use TicTacToeBatch.");
		}
	}
	
}
