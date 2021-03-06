package games.RubiksCube;

import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JFrame;

import controllers.PlayAgent;
import games.Arena;
import games.Evaluator;
import games.Feature;
import games.GameBoard;
import games.XNTupleFuncs;
import games.RubiksCube.CubeConfig.BoardVecType;
import games.RubiksCube.CubeConfig.CubeType;
import games.RubiksCube.CubeConfig.TwistType;
import games.Sim.ConfigSim;
import games.ArenaTrain;

/**
 * {@link ArenaTrain} for Rubik's Cube. It borrows all functionality
 * from the general class {@link ArenaTrain} derived from {@link Arena}. It only overrides 
 * the abstract methods <ul>
 * <li> {@link Arena#makeGameBoard()}, 
 * <li> {@link Arena#makeEvaluator(PlayAgent, GameBoard, int, int, int)}, and 
 * <li> {@link Arena#makeFeatureClass(int)}, 
 * </ul> such that 
 * these factory methods return objects of class {@link GameBoardCube}, 
 * {@link EvaluatorCube}, and {@link FeatureCube}, respectively.
 * 
 * @see GameBoardCube
 * @see EvaluatorCube
 * 
 * @author Wolfgang Konen, TH Koeln, Feb'18
 */
public class ArenaTrainCube extends ArenaTrain   {
	
	public ArenaTrainCube(String title, boolean withUI) {
		super(title,withUI);
		CubeStateFactory.generateInverseTs();
		CubeState.generateForwardTs();
	}
	
	/**
	 * @return a name of the game, suitable as subdirectory name in the 
	 *         {@code agents} directory
	 *         
	 * @see GameBoardCube#getSubDir() 
	 */
	@Override
	public String getGameName() {
		return "RubiksCube";
	}
	
	/**
	 * Factory pattern method
	 */
	@Override
	public GameBoard makeGameBoard() {
		CubeStateFactory.generateInverseTs();		// since makeGameBoard is called via super --> Arena
		CubeState.generateForwardTs();		// prior to finishing ArenaTrainCube(String,boolean)
		gb = new GameBoardCube(this);	
		return gb;
	}
	/**
	 * Factory pattern method: make a new Evaluator
	 * @param pa		the agent to evaluate
	 * @param gb		the game board
	 * @param stopEval	the number of successful evaluations needed to reach the 
	 * 					evaluator goal (may be used to stop training prematurely)
	 * @param mode		which evaluator mode: 0,1,2,9. Throws a runtime exception 
	 * 					if {@code mode} is not in the set {@link Evaluator#getAvailableModes()}.
	 * @param verbose	how verbose or silent the evaluator is
	 * @return
	 */
	@Override
	public Evaluator makeEvaluator(PlayAgent pa, GameBoard gb, int stopEval, int mode, int verbose) {
//		if (mode==-1) mode=EvaluatorCube.getDefaultEvalMode();
		return new EvaluatorCube(pa,gb,stopEval,mode,verbose);
	}

	@Override
	public Feature makeFeatureClass(int featmode) {
		return new FeatureCube(featmode);
	}

	@Override
	public XNTupleFuncs makeXNTupleFuncs() {
		return new XNTupleFuncsCube();
	}

    /**
     * set the cube type (POCKET or RUBIKS) for Rubik's Cube
     */
    public static void setCubeType(String sCube) {
    	switch(sCube) {
    	case "2x2x2": CubeConfig.cubeType = CubeType.POCKET; break;
    	case "3x3x3": CubeConfig.cubeType = CubeType.RUBIKS; break;
    	default: throw new RuntimeException("Cube type "+sCube+" is not known.");
    	}
    }

    /**
     * set the board vector type for Rubik's Cube
     */
    public static void setBoardVecType(String bvType) {
    	switch(bvType) {
    	case "CSTATE": CubeConfig.boardVecType = BoardVecType.CUBESTATE; break;
    	case "CPLUS": CubeConfig.boardVecType = BoardVecType.CUBEPLUSACTION; break;
    	case "STICKER": CubeConfig.boardVecType = BoardVecType.STICKER; break;
    	default: throw new RuntimeException("Board vector type "+bvType+" is not known.");
    	}
    }

    /**
     * set the twist type (ALLTWISTS or QUARTERTWISTS) for Rubik's Cube
     */
    public static void setTwistType(String tCube) {
    	switch(tCube) {
    	case "ALL": CubeConfig.twistType = TwistType.ALLTWISTS; break;
    	case "QUARTER": CubeConfig.twistType = TwistType.QUARTERTWISTS; break;
    	default: throw new RuntimeException("Twist type "+tCube+" is not known.");
    	}
    }

	/**
	 * Start GBG for Rubik's Cube (trainable version)
	 * 
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException 
	{
		ArenaTrainCube t_Frame = new ArenaTrainCube("General Board Game Playing",true);

		if (args.length==0) {
			t_Frame.init();
		} else {
			throw new RuntimeException("[ArenaTrainCube.main] args="+args+" not allowed. Use batch facility.");
		}
	}
	
}
