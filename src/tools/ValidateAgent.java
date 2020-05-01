package tools;

import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import controllers.PlayAgent;
import controllers.TD.ntuple2.TDNTuple3Agt;
import games.Arena;
import games.BoardVector;
import games.XArenaFuncs;
import games.XNTupleFuncs;
import games.StateObservation;
import games.Othello.ArenaOthello;
import games.Othello.ArenaTrainOthello;
import games.Othello.BenchmarkPlayer.BenchMarkPlayer;
import games.TicTacToe.ArenaTrainTTT;
import params.ParNT;
import params.ParOther;
import params.ParTD;
import tools.Types.ACTIONS;

/**
 * Class {@link ValidateAgent} performs certain consistency checks of an agent class. These checks 
 * test whether certain methods run through successfully, produce the expected results and so on.
 * <p>
 * Run these checks by constructing in {@link #main(String[])} a certain {@link Arena} {@code ar} object, 
 * a {@link StateObservation} {@code sob} object  and a {@link PlayAgent} {@code p} object of the agent 
 * class you want to validate. Then perform the checks by calling 
 * {@link #runTests(PlayAgent, StateObservation, Arena)}.
 * <p>
 * IMPORTANT: Run {@link #main(String[])} with VM argument {@code -ea} (enable assertions).
 */
public class ValidateAgent {

	public ValidateAgent() {};
	
	public boolean runTests(PlayAgent pa, StateObservation sob, Arena ar) {
		//
		// Check if pa.getScoreTuple and pa.estimateGameValueTuple run correctly through and return valid
		// numbers. Check this with different StateObservation objects so that there is at least one 
		// state for every player. (Of course this test is not exhaustive.)
		//		
		boolean verbose=true;
		ScoreTuple sc;
		sc = pa.getScoreTuple(sob, null);
		checkScoreTuple(sc,sob,verbose);
		sc = pa.estimateGameValueTuple(sob, null);
		checkScoreTuple(sc,sob,verbose);
		for (int i=1; i<sob.getNumPlayers(); i++) {
			ACTIONS a = sob.getAvailableActions().get(0);
			sob.advance(a);
			sc = pa.getScoreTuple(sob, null);
			checkScoreTuple(sc,sob,verbose);
			sc = pa.estimateGameValueTuple(sob, null);
			checkScoreTuple(sc,sob,verbose);
		}
		System.out.println("getScoreTuple check ... OK");
		
		// 
		// Check if the final score tuple (which usually contains non-zero rewards) has a sum of zero 
		// for 2-player games, i.e. 2 opposite entries:
		//
		while (!sob.isGameOver()) {
			ArrayList<ACTIONS> arr = sob.getAvailableActions();
			ACTIONS a = pa.getNextAction2(sob, false, true);
			sob.advance(a);
		}
		sc = pa.getScoreTuple(sob, null);
		checkScoreTuple(sc,sob,verbose);
		sc = sob.getGameScoreTuple();
		checkScoreTuple(sc,sob,verbose);
		System.out.println("final getScoreTuple check ... OK");
		
		//
		// check if a training episode runs through successfully 
		//
		System.out.print("starting pa.trainAgent ... ");
		int num = pa.getGameNum();
		sob = ar.getGameBoard().getDefaultStartState();
		pa.trainAgent(sob);
		assert (pa.getGameNum()-num == 1) : "Game counter not correctly incremented!";
		int dummy = 1;
		System.out.println("OK");
		
		// 
		// construct a board vector bv where each element is different and check that each board 
		// vector returned by  xnf.symmetryVectors(bv) is different from all the others.
		// 
		XNTupleFuncs xnf = ar.makeXNTupleFuncs();
		BoardVector bv = xnf.makeBoardVectorEachCellDifferent();
		BoardVector[] sym = xnf.symmetryVectors(bv,0);
		boolean testPassed=true;
		for (int i=0; i<(sym.length-1); i++) {
			for (int j=i+1; j<sym.length; j++) {
				if (assertBvDifferent(sym[i],sym[j])==false) {
					System.out.println("Error: symmetry states identical: "+i+", "+j);
					testPassed=false;
				}
			}
		}
		if (testPassed) {
			System.out.println("symmetryVectors check ... OK");
		} else {
			throw new RuntimeException("symmetryVectors check ... FAILED");
		}
		
		return true;
	}
	
	private boolean assertBvDifferent(BoardVector bv1, BoardVector bv2) {
		for (int i=0; i<bv1.bvec.length; i++) 
			if (bv1.bvec[i]!=bv2.bvec[i]) return true;
		
		return false;
	}

	/**
	 * Check that {@code sc}'s values are valid, finite numbers, lie between min and max score
	 * and that they sum to 0.0.
	 * 
	 * @param sc
	 * @param sob
	 * @param verbose
	 * @return
	 */
	private boolean checkScoreTuple(ScoreTuple sc, StateObservation sob, boolean verbose) {
		double scMin = sob.getMinGameScore();
		double scMax = sob.getMaxGameScore();
		double scSum = 0.0;
		if (verbose) {
			System.out.print(sc);
			System.out.println("     "+scMin+"-->"+ scMax);
		}
		for (int i=0; i<sc.scTup.length; i++) {
			scSum += sc.scTup[i];
			assert !Double.isNaN(sc.scTup[i]);
			assert Double.isFinite(sc.scTup[i]);		
			assert (scMin <= sc.scTup[i]) : "ScoreTuple < getMinScore() : "+sc.scTup[i]+" < "+scMin;
			assert (sc.scTup[i] <= scMax) : "ScoreTuple > getMaxScore() : "+sc.scTup[i]+" > "+scMax;
		}
		assert (Math.abs(scSum) < 1e-20) : "ScoreTuple does not sum to 0: "+scSum;
		return true;
	}
	
	private static PlayAgent constructTDNTuple3Agt(Arena ar) {
		PlayAgent p;
		try {
			p = ar.m_xfun.constructAgent(0, "TD-Ntuple-3", ar.m_xab);
		} catch (IOException e1) {
			e1.printStackTrace();
			p=null;			
		} 
		return p;
	}
	
	public static void main(String[] args) {
		//
		// choose an Arena
		//
		Arena ar = new ArenaTrainOthello("",true);
//		Arena ar = new ArenaTrainTTT("",true);
		
		//
		// choose an agent to validate
		//
		PlayAgent pa;
//		pa = new BenchMarkPlayer("bp",1);
		pa = constructTDNTuple3Agt(ar);
		
		//
		// start validation
		//
		if (pa==null) {
			System.out.println("PlayAgent p is null!");
		} else {
			StateObservation sob = ar.getGameBoard().getDefaultStartState();
			ValidateAgent va = new ValidateAgent();
			try {
				va.runTests(pa,sob, ar);				
				System.out.println("ValidateAgent finished successfully");
			} catch(Exception e) {
				e.printStackTrace();
				System.out.println("ValidateAgent finished with errors");
			} catch (AssertionError e2) {
				e2.printStackTrace();
				System.out.println("ValidateAgent finished with errors");
			}
			
		}
		
		ar.destroy();
	}

}
