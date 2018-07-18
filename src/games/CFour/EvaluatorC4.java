package games.CFour;

import controllers.MCTS.MCTSAgentT;
import controllers.MCTSExpectimax.MCTSExpectimaxAgt;
import controllers.TD.ntuple2.TDNTuple2Agt;
import controllers.MaxNAgent;
import controllers.PlayAgent;
import controllers.RandomAgent;
import games.Evaluator;
import games.GameBoard;
import games.StateObservation;
import games.XArenaFuncs;
import games.TicTacToe.Evaluator9;
import games.TicTacToe.EvaluatorTTT;
import games.ZweiTausendAchtundVierzig.ConfigEvaluator;
import games.ZweiTausendAchtundVierzig.StateObserver2048;
import games.CFour.openingBook.BookSum;
import params.ParMCTS;
import params.ParMaxN;
import params.ParOther;
import tools.MessageBox;
import tools.Types;
import tools.Types.ACTIONS;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JOptionPane;

import agentIO.AgentLoader;


/**
 * Evaluator for the game C4 (ConnectFour). Depending on the value of parameter {@code mode} in constructor:
 * <ul>
 * <li> -1: no evaluation
 * <li>  0: compete against MCTS
 * <li>  1: compete against Random
 * <li>  2: compete against Max-N (limited tree depth: 6)
 * <li>  3: compete against AlphaBetaAgent (perfect play)
 * <li> 10: compete against MCTS, different start states
 * <li> 11: compete against TDReferee.agt.zip, different start states
 * </ul>  
 * The value of mode is set in the constructor. 
 */
public class EvaluatorC4 extends Evaluator {
    private MaxNAgent maxnAgent = null; 
    private final String logDir = "logs/ConnectFour/train";
    protected int verbose = 0;
    private MCTSAgentT mctsAgent = null;
    private RandomAgent randomAgent = new RandomAgent("Random");
    private double trainingThreshold = 0.8;
    private GameBoard m_gb;
    private PlayAgent playAgent;
    private double lastResult = 0;
    private int numStartStates = 1;
    private int m_mode = 0;
    private String m_msg = null;
	private AgentLoader agtLoader = null;
	
	// The opening-books are loaded only once to save memory. All agents, that
	// need them, use the same books.
	private final BookSum books = new BookSum();
	private AlphaBetaAgent alphaBetaStd = null;

    /**
     * logResults toggles logging of training progress to a csv file located in {@link #logDir}
     */
    private boolean logResults = false;
    private boolean fileCreated = false;
    private PrintWriter logFile;
    private StringBuilder logSB;

    public EvaluatorC4(PlayAgent e_PlayAgent, GameBoard gb, int stopEval, int mode, int verbose) {
        super(e_PlayAgent, stopEval, verbose);
        m_mode = mode;
        if (verbose == 1) {
            System.out.println("Using evaluation mode " + mode);
        }
        initEvaluator(e_PlayAgent, gb);
        if (m_mode == 2 && maxnAgent.getDepth() < C4Base.CELLCOUNT) {
            System.out.println("Using Max-N with limited tree depth: " +
                    maxnAgent.getDepth() + " used, " + C4Base.CELLCOUNT + " needed (for perfect play)");
        }
    }

    private void initEvaluator(PlayAgent playAgent, GameBoard gameBoard) {
        this.m_gb = gameBoard;
        this.playAgent = playAgent;
        //maxnAgent is set once in evaluator constructor, so the search tree does not have to be rebuilt 
        //every time a new evaluation is started.
        //However, this prevents MaxN parameters from being adjusted while the program is running. 
    	//Set needed tree depth during compile time.
        ParMaxN params = new ParMaxN();
        int maxNDepth =  6;
        params.setMaxNDepth(maxNDepth);
        maxnAgent = new MaxNAgent("Max-N", params, new ParOther());
        
		// Initialize the standard Alpha-Beta-Agent
		// (same as winOptionsGTB in MT's C4)
		alphaBetaStd = new AlphaBetaAgent(books);
		alphaBetaStd.resetBoard();
		alphaBetaStd.setTransPosSize(4);		// index into table
		alphaBetaStd.setBooks(true,false,true);	// use normal book and deep book dist
		alphaBetaStd.setDifficulty(42);			// search depth
		alphaBetaStd.randomizeEqualMoves(true);

    }

    @Override
    protected boolean eval_Agent(PlayAgent pa) {
    	this.playAgent = pa;
        //Disable evaluation by using mode -1
        if (m_mode == -1) {
            return true;
        }

        //Disable logging for the final evaluation after training
        if (!fileCreated && playAgent.getGameNum() == playAgent.getMaxGameNum()) {
            logResults = false;
        }

        if (logResults && !fileCreated) {
    		tools.Utils.checkAndCreateFolder(logDir);
            logSB = new StringBuilder();
            logSB.append("training_matches");
            logSB.append(",");
            logSB.append("result");
            logSB.append("\n");
            try {
                logFile = new PrintWriter(new File(logDir + "/" + getCurrentTimeStamp() + ".csv"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            fileCreated = true;
        }

        double result;
        int numEpisodes=8;
        switch (m_mode) {
            case 0:
                result = competeAgainstMCTS(playAgent, m_gb, numEpisodes);
                break;
            case 1:
                result = competeAgainstRandom(playAgent, m_gb);
                break;
            case 2:
                result = competeAgainstMaxN(playAgent, m_gb, numEpisodes);
                break;
            case 3:
            	numEpisodes=20;
                result = competeAgainstAlphaBeta(playAgent, m_gb, numEpisodes);
                break;
            case 10:
            	if (playAgent instanceof TDNTuple2Agt) {
            		// we can only call the parallel version, if playAgent's getNextAction2 is 
            		// thread-safe, which is the case for TDNTuple2Agt
            		// Also we have to construct MCTS opponent inside the callables, otherwise
            		// we are not thread-safe as well:
                    result = competeAgainstMCTS_diffStates_PAR(playAgent, m_gb, numEpisodes);
            	} else {
                    ParMCTS params = new ParMCTS();
                    int numIterExp =  (Math.min(C4Base.CELLCOUNT,5) - 1);
                    params.setNumIter((int) Math.pow(10, numIterExp));
                    mctsAgent = new MCTSAgentT("MCTS", new StateObserverC4(), params);

            		result = competeAgainstOpponent_diffStates(playAgent, mctsAgent, m_gb, numEpisodes);
            	}
                break;
            case 11:
    			if (agtLoader==null) agtLoader = new AgentLoader(m_gb.getArena(),"TDReferee.agt.zip");
        		result = competeAgainstOpponent_diffStates(playAgent, agtLoader.getAgent(), m_gb, numEpisodes);
                break;
            default:
                return false;
        }


        if (logResults) {
            logSB.append(playAgent.getGameNum());
            logSB.append(",");
            logSB.append(result);
            logSB.append("\n");
            logFile.write(logSB.toString());
            logSB.delete(0, logSB.length());
            logFile.flush();

            //If the last game has been played, close the file handle.
            //Does not work if the maximum number of training games is not divisible by the number of games per eval.
            if (playAgent.getMaxGameNum() == playAgent.getGameNum()) {
                logFile.close();
            }
        }

        return result >= trainingThreshold;
    }

    /**
     * Evaluates an agent's performance with perfect play, as long as tree and rollout depth are not limited.
     * Scales poorly with board size, requires lots of GB and time for increased tree depth.
     *
     * @param playAgent Agent to be evaluated
     * @param gameBoard	game board for the evaluation episodes
     * @return Percentage of games won on a scale of [0, 1] as double
     */
    private double competeAgainstMaxN(PlayAgent playAgent, GameBoard gameBoard, int numEpisodes) {
        double[] res = XArenaFuncs.compete(playAgent, maxnAgent, new StateObserverC4(), numEpisodes, verbose);
        double success = res[0] - res[2];
        m_msg = playAgent.getName() + ": " + this.getPrintString() + success;
        if (this.verbose > 0) System.out.println(m_msg);
        lastResult = success;
        return success;
    }

    /**
     * Evaluates an agent's performance against perfect play, since {@link AlphaBetaAgent} with 
     * opening books is an agent playing perfectly. We test only the games where  
     * {@code playAgent} starts, since games where {@link AlphaBetaAgent} starts are a safe 
     * win for {@link AlphaBetaAgent}.
     *
     * @param playAgent Agent to be evaluated
     * @param gameBoard	game board for the evaluation episodes
     * @param numEpisodes
     * @return a value between +1 and -1, depending on the rate of episodes won by the agent 
     * 		or oppenent.
     * <p>
     * Note that the return value has a slightly different meaning than in 
     * {@link #competeAgainstMCTS(PlayAgent, GameBoard, int)}. There the agent is evaluated 
     * bothas 1st and 2nd player. Here it is evaluated only as 1st player, since it would  
     * loose against the perfect {@link AlphaBetaAgent}. If v is the return value from this function,
     * then v_both = (v+(-1)/2 = v/2 - 0.5 \in [-1,0] would be the corresponding competeBoth-value
     * (best is 0.0). But we return here v in order to evaluate the same as in [Thill14]. 
     */
    private double competeAgainstAlphaBeta(PlayAgent playAgent, GameBoard gameBoard, int numEpisodes) {
//    	verbose=1;
    	// only as debug test: if AlphaBetaAgent is implemented correctly and playing perfect,  
    	// it should win every game when starting from the empty board, for each playAgent.
    	// (This is indeed the case.)
//        double[] res = XArenaFuncs.compete(alphaBetaStd, playAgent, new StateObserverC4(), numEpisodes, verbose);
//        double success = res[2] - res[0];
        double[] res = XArenaFuncs.compete(playAgent, alphaBetaStd, new StateObserverC4(), numEpisodes, verbose);
        double success = res[0] - res[2];
        m_msg = playAgent.getName() + ": " + this.getPrintString() + success;
//      if (this.verbose > 0) 
        	System.out.println(m_msg);
        lastResult = success;
        return success;
    }

    /**
     * Evaluates {@code playAgent}'s performance when playing against MCTS using 1000 iterations.
     * Plays {@code numEpisodes} episodes both as 1st and as 2nd player.
     *
     * @param playAgent agent to be evaluated
     * @param gameBoard game board for the evaluation episodes
     * @param numEpisodes number of episodes played during evaluation
     * @return a value between +1 and -1, depending on the rate of episodes won by the agent 
     * 		or oppenent. Best for {@code playAgent} is +1, worst is -1. (If opponent were perfect,  
     * 		best is 0, since the agent can then only win those games where he is 1st player.)
     */
    private double competeAgainstMCTS(PlayAgent playAgent, GameBoard gameBoard, int numEpisodes) {
        ParMCTS params = new ParMCTS();
        int numIterExp =  (Math.min(C4Base.CELLCOUNT,5) - 2);
        params.setNumIter((int) Math.pow(10, numIterExp));
        mctsAgent = new MCTSAgentT("MCTS", new StateObserverC4(), params);

//        double[] res = XArenaFuncs.compete(playAgent, mctsAgent, new StateObserverC4(), numEpisodes, 0);
//        double success = res[0];        	
        double success = XArenaFuncs.competeBoth(playAgent, mctsAgent,  new StateObserverC4(), numEpisodes, 0, gameBoard);
        m_msg = playAgent.getName() + ": " + this.getPrintString() + success;
        //if (this.verbose > 0) 
        	System.out.println(m_msg);
        lastResult = success;
        return success;
    }

    /**
     * Similar to {@link EvaluatorC4#competeAgainstMCTS(PlayAgent, GameBoard, int)}, but:
     * <ul> 
     * <li>It does not only play evaluation games from the default start state (empty board) but 
     * also games where the first player (Black) has made a losing moves and the agent as second
     * player (White) will win, if it plays perfect (see {@link HexConfig#EVAL_START_ACTIONS}). 
     * <li>It allows a different opponent than MCTS to be passed in as 2nd argument
     * </ul>
     *
     * @param playAgent agent to be evaluated (it plays both 1st and 2nd)
     * @param opponent 	agent against which {@code playAgent} plays
     * @param gameBoard game board for the evaluation episodes
     * @param numEpisodes number of episodes played during evaluation
     * @return a value between 0 or 1, depending on the rate of evaluation games won by the agent
     * 
     * @see EvaluatorC4#competeAgainstMCTS(PlayAgent, GameBoard, int)
     * @see HexConfig#EVAL_START_ACTIONS
     */
    private double competeAgainstOpponent_diffStates(PlayAgent playAgent, PlayAgent opponent, GameBoard gameBoard, int numEpisodes) {
        double[] res;
        double success = 0;
        
		if (opponent == null) {
			String tdstr = agtLoader.getLoadMsg() + " (no opponent)";
			MessageBox.show(gameBoard.getArena(),"ERROR: " + tdstr,
					"Load Error", JOptionPane.ERROR_MESSAGE);
			return Double.NaN;
		} 

        // find the start states to evaluate:
        int [] startAction = {-1};
        int N = 1; //HexConfig.BOARD_SIZE;
        if (N>0) { //HexConfig.EVAL_START_ACTIONS.length-1) {
            System.out.println("*** WARNING ***: 1-ply winning boards for board size N="+N+
            		"are not coded in " +"HexConfig.EVAL_START_ACTIONS." );
            System.out.println("*** WARNING ***: Evaluator(mode 10) will use only " +
            		"empty board for evaluation.");
        } else {
            // the int's in startAction code the start board. -1: empty board (a winning 
            // board for 1st player Black), 
            // 0/1/...: Black's 1st move was tile 00/01/... (it is a losing move, a winning
            // board for 2nd player White)
            startAction = null; //HexConfig.EVAL_START_ACTIONS[N];
        }
        numStartStates = startAction.length;
        
        // evaluate each start state in turn and return average success rate: 
        for (int i=0; i<startAction.length; i++) {
        	StateObserverC4 so = new StateObserverC4();
        	if (startAction[i] == -1) {
        		res = XArenaFuncs.compete(playAgent, opponent, so, numEpisodes, 0);
                success += res[0];        	
        	} else {
        		so.advance(new ACTIONS(startAction[i]));
        		res = XArenaFuncs.compete(opponent, playAgent, so, numEpisodes, 0);
                success += res[2];        	
        	}
        }
        success /= startAction.length;
        m_msg = playAgent.getName() + ": " + this.getPrintString() + success;
//        if (this.verbose > 0) 
        	System.out.println(m_msg);
        lastResult = success;
        return success;
    }

    /**
     * Does the same as {@code competeAgainstMCTS_diffStates}, but with 6 parallel cores, 
     * so it is 6 times faster. 
     * <p>
     * NOTES: <ul>
     * <li> The memory consumption grows when this function is repeatedly called (by about 60 kB
     * for each call, but there seems to be an effective limit for the Java process at 4.3 GB -- 
     * beyond the garbage collector seems to do its work effectively)
     * <li> The call to compete may not alter anything in {@code playAgent}. So the function 
     * getNextAction2 invoked by compete should be thread-safe. This is valid, if getNextAction2 
     * does not modify members in playAgent. Possible for TD-n-Tuple-agents, if we do not write 
     * on class-global members (e.g. do not use BestScore, but use local variable BestScore2). 
     * Parallel threads are not possible when playAgent is MCTSAgentT or MaxNAgent.
     * </ul>
     * 
     * @param playAgent
     * @param gameBoard		game board for the evaluation episodes
     * @param numEpisodes
     * @return
     */
    private double competeAgainstMCTS_diffStates_PAR(PlayAgent playAgent, GameBoard gameBoard, int numEpisodes) {
        ExecutorService executorService = Executors.newFixedThreadPool(6);
        ParMCTS params = new ParMCTS();
        int numIterExp =  (Math.min(C4Base.CELLCOUNT,5) - 1);
        params.setNumIter((int) Math.pow(10, numIterExp));
        //mctsAgent = new MCTSAgentT(Types.GUI_AGENT_LIST[5], new StateObserverHex(), params);

        // find the start states to evaluate:
        int [] startAction = {-1};
        int N = 1; //HexConfig.BOARD_SIZE;
        if (N>0) { //HexConfig.EVAL_START_ACTIONS.length-1) {
            System.out.println("*** WARNING ***: 1-ply winning boards for board size N="+N+
            		"are not coded in " +"HexConfig.EVAL_START_ACTIONS." );
            System.out.println("*** WARNING ***: Evaluator(mode 10) will use only " +
            		"the empty board for evaluation.");
        } else {
            // the int's in startAction code the start board. -1: empty board (a winning 
            // board for 1st player Black), 
            // 0/1/...: Black's 1st move was tile 00/01/... (it is a losing move, a winning
            // board for 2nd player White)
            startAction = null; //HexConfig.EVAL_START_ACTIONS[N];
        }
        numStartStates = startAction.length;
        
        List<Double> successObservers = new ArrayList<>();
        List<Callable<Double>> callables = new ArrayList<>();
        final int[] startAction2 = startAction;

        // evaluate each start state in turn and return average success rate: 
        for (int i=0; i<startAction.length; i++) {
            int gameNumber = i+1;
            final int i2 = i;
            callables.add(() -> {
                double[] res;
                double success = 0;
                long gameStartTime = System.currentTimeMillis();
                StateObserverC4 so = new StateObserverC4();
                
                // important: mctsAgent2 has to be constructed inside the 'callables' function, 
                // otherwise all parallel calls would operate on the same agent and would produce
                // garbage.
                MCTSAgentT mctsAgent2 = new MCTSAgentT("MCTS", new StateObserverC4(), params);

            	if (startAction2[i2] == -1) {
            		res = XArenaFuncs.compete(playAgent, mctsAgent2, so, numEpisodes, 0);
                    success = res[0];        	
            	} else {
            		so.advance(new ACTIONS(startAction2[i2]));
            		res = XArenaFuncs.compete(mctsAgent2, playAgent, so, numEpisodes, 0);
                    success = res[2];        	
            	}
                if(verbose == 0) {
                    System.out.println("Finished evaluation " + gameNumber + " after " + (System.currentTimeMillis() - gameStartTime) + "ms. ");
                }
            	return Double.valueOf(success);
            });
        }
   
        // invoke all callables and store results on List successObservers
        try {
            executorService.invokeAll(callables).stream().map(future -> {
                try {
                    return future.get();
                }
                catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }).forEach(successObservers::add);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // reduce results (here: calculate average success)
        double averageSuccess = 0; 
        for(Double suc : successObservers) {
            averageSuccess += suc.doubleValue();
        }
        averageSuccess/= startAction.length;

        m_msg = playAgent.getName() + ": " + this.getPrintString() + averageSuccess;
//        if (this.verbose > 0) 
        	System.out.println(m_msg);
        lastResult = averageSuccess;
        return averageSuccess;
    }

    /**
     * Very weak but fast evaluator to see if there is a training progress at all.
     * Getting a high win rate against this evaluator does not guarantee good performance of the evaluated agent.
     *
     * @param playAgent Agent to be evaluated
     * @param gameBoard	game board for the evaluation episodes
     * @return Percentage of games won on a scale of [0, 1] as double
     */
    private double competeAgainstRandom(PlayAgent playAgent, GameBoard gameBoard) {
    	StateObservation so = new StateObserverC4();
        double success = XArenaFuncs.competeBoth(playAgent, randomAgent,  so, 50, 0, gameBoard);
        //double[] res = XArenaFuncs.compete(playAgent, randomAgent, so, 100, verbose);
        //double success = res[0];
        m_msg = playAgent.getName() + ": " + this.getPrintString() + success;
        if (this.verbose > 0) System.out.println(m_msg);
        lastResult = success;
        return success;
    }

    @Override
    public double getLastResult() {
        return lastResult;
    }

    @Override
    public boolean isAvailableMode(int mode) {
        int[] availableModes = getAvailableModes();
        for (int availableMode : availableModes) {
            if (mode == availableMode) {
                return true;
            }
        }

        return false;
    }

    @Override
    public int[] getAvailableModes() {
        return new int[]{-1, 0, 1, 2, 3, 10, 11};
    }

    @Override
    public int getQuickEvalMode() {
        return 0;
    }

    @Override
    public int getTrainEvalMode() {
        return -1;
    }

    @Override
    public int getMultiTrainEvalMode() {
        return 0; //getAvailableModes()[0];
//        return getQuickEvalMode();
    }

    @Override
    public String getPrintString() {
        switch (m_mode) {
            case 0:  return "success against MCTS (best is 1.0): ";
            case 1:  return "success against Random (best is 1.0): ";
            case 2:  return "success against Max-N (best is 1.0): ";
            case 3:  return "success against AlphaBetaAgent (best is 1.0): ";
            case 10: return "success against MCTS (" + numStartStates + " diff. start states, best is 1.0): ";
            case 11: return "success against TDReferee (" + numStartStates + " diff. start states, best is 1.0): ";
            default: return null;
        }
    }

    @Override
    public String getPlotTitle() {
        switch (m_mode) {
            case 0:  return "success against MCTS";
            case 1:  return "success against Random";
            case 2:  return "success against Max-N";
            case 3:  return "success against AlphaBeta";
            case 10: return "success against MCTS";
            case 11: return "success against TDReferee";
            default: return null;
        }
    }

    @Override
    public String getMsg() {
        return m_msg;
    }

    /**
     * generates String containing the current timestamp
     *
     * @return the timestamp
     */
    private static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }
}