package src.games.PenneysGame;

import controllers.AgentBase;
import controllers.PlayAgent;
import games.Feature;
import games.StateObservation;
import games.TicTacToe.TicTDBase;
import tools.Types;

import java.io.Serializable;


/**
 * Implementation of {@link Feature} for Penney's Game.<p>
 * 
 * Method {@link #prepareFeatVector(StateObservation)} returns the feature vector. 
 * The constructor accepts argument {@code featmode} to construct different types 
 * of feature vectors. The acceptable values for {@code featmode} are
 * retrieved with {@link #getAvailFeatmode()}.
 *
 * @author Wolfgang Konen, TH Koeln, Nov'16
 */
public class FeaturePenney implements Feature, Serializable {
	private int featmode;

	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip will become unreadable or you have
	 * to provide a special version transformation)
	 */
	private static final long  serialVersionUID = 12L;

	public FeaturePenney(int featmode) {
		this.featmode = featmode;
	}
	
	/**
	 * This dummy stub is just needed here, because {@link FeaturePenney} is derived from
	 * {@link AgentBase}, which implements {@link PlayAgent} and thus requires this method. 
	 * It should not be called. If called, it throws a RuntimeException.
	 */
	public Types.ACTIONS_VT getNextAction2(StateObservation sob, boolean random, boolean silent) {
		throw new RuntimeException("FeatureTTT does not implement getNextAction2");
	}
	
	@Override
	public double[] prepareFeatVector(StateObservation sob) {
		assert (sob instanceof StateObserverPenney) : "Input 'sob' is not of class StateObserverPenney";
		StateObserverPenney so = (StateObserverPenney) sob;
		int[][] table = so.getTable();
		int player = Types.PLAYER_PM[so.getPlayer()];
		double[] input = new double[table[0].length];
		if(player==1)
			for(int i=0;i<table.length;i++)
				input[i] = table[0][i];
		return input;
	}

	@Override
	public String stringRepr(double[] featVec) {
		String repr = "";
		for(int i = 0;i<featVec.length;i++)
			repr += Double.toString(featVec[i]);
		return repr;
	}

	@Override
	public int[] getAvailFeatmode() {
		int[] featlist = {0};
		return featlist;
	}

	@Override
	public int getFeatmode() {
		return featmode;
	}

	@Override
	public int getInputSize(int featmode) {
		// inpSize[i] has to match the length of the vector which
		// TicTDBase.prepareInputVector() returns for featmode==i:
		int inpSize[] = { 3 };
		if (featmode>(inpSize.length-1) || featmode<0)
			throw new RuntimeException("featmode outside allowed range 0,...,"+(inpSize.length-1));
		return inpSize[featmode];
	}

	public double getScore(StateObservation sob) {
		// Auto-generated method stub (just needed because AgentBase,
		// the superclass of TicTDBase, requires it)
		return 0;
	}

}
