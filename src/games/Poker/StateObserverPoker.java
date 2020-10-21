package src.games.Poker;

import game.rules.play.Play;
import games.ObserverBase;
import games.StateObsNondeterministic;
import games.StateObservation;
import tools.Types.ACTIONS;

import java.lang.management.PlatformLoggingMXBean;
import java.lang.reflect.Array;
import java.util.*;
import java.util.function.IntFunction;

/**
 * Class StateObservation observes the current state of the game, it has utility functions for
 * <ul>
 * <li> returning the available actions ({@link #getAvailableActions()}), 
 * <li> advancing the state of the game with a specific action ({@link #advance(ACTIONS)}),
 * <li> copying the current state
 * <li> signaling end, score and winner of the game
 * </ul>
 *
 */
public class StateObserverPoker extends ObserverBase implements StateObsNondeterministic {
    private static final double REWARD_NEGATIVE = -1.0;
    private static final double REWARD_POSITIVE =  1.0;

	public static final int NUM_PLAYER = 4;
	private static final int START_CHIPS = 100;
	private static final int SMALLBLIND = 5;
	private static final int BIGBLIND = 2*SMALLBLIND;

    private int m_Player;			// player who makes the next move
	protected ArrayList<ACTIONS> availableActions = new ArrayList();	// holds all available actions

	private CardDeck m_deck;
	private int chips[];
	private PlayingCard[][] holeCards; //[player][card]
	private PlayingCard[] communityCards;
	private int dealer;

	private static String[] phases = {"setup","pre-flop","flop","flop-bet","turn","turn-bet","river","river-bet","showdown"};
	private short m_phase;

	private boolean activePlayers[];
	private boolean playingPlayers[];
	private boolean foldedPlayers[];
	private Queue<Integer> openPlayers;

	private boolean isNextActionDeterministic;
	private ACTIONS nextNondeterministicAction;
	protected List<Integer> availableRandoms = new ArrayList();


	private boolean GAMEOVER;

	private Pots pots;

	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .gamelog containing this object will become 
	 * unreadable or you have to provide a special version transformation)
	 */
	private static final long serialVersionUID = 12L;

	public StateObserverPoker() {
		GAMEOVER = false;
		dealer = 0;

		// information about the player:
		chips =  new int[NUM_PLAYER];
		activePlayers = new boolean[NUM_PLAYER];
		foldedPlayers = new boolean[NUM_PLAYER];
		playingPlayers = new boolean[NUM_PLAYER];

		for(int i = 0;i<NUM_PLAYER;i++){
			chips[i] = START_CHIPS;
			activePlayers[i] = true;
			playingPlayers[i] = true;
			foldedPlayers[i] = false;
		}

		initRound();

		m_Player = openPlayers.remove();
		setAvailableActions();
		isNextActionDeterministic = true;
	}

	public StateObserverPoker(StateObserverPoker other)	{
		GAMEOVER = other.GAMEOVER;
		this.dealer = other.dealer;

		m_Player = other.m_Player;

		chips =  new int[NUM_PLAYER];
		holeCards = new PlayingCard[NUM_PLAYER][2];
		communityCards = new PlayingCard[5];

		this.pots = new Pots(other.pots);

		this.m_deck = other.m_deck.copy();
		this.m_phase = other.m_phase;

		openPlayers = new LinkedList<Integer>();
		int len = other.openPlayers.size();
		for(int i = 0;i<len;i++){
			int x = other.openPlayers.remove();
			this.openPlayers.add(x);
			other.openPlayers.add(x);
		}

		chips =  new int[NUM_PLAYER];
		activePlayers = new boolean[NUM_PLAYER];
		playingPlayers = new boolean[NUM_PLAYER];
		foldedPlayers = new boolean[NUM_PLAYER];
		for(int i = 0;i<NUM_PLAYER;i++){
			this.holeCards[i] = other.holeCards[i];
			this.chips[i] = other.chips[i];
			this.activePlayers[i] = other.activePlayers[i];
			this.playingPlayers[i] = other.playingPlayers[i];
			this.foldedPlayers[i] = other.foldedPlayers[i];
		}

		for(int i=0;i<communityCards.length;i++)
			this.communityCards[i]=other.communityCards[i];

		this.availableActions = other.availableActions;
	}

	public void initRound(){
		m_deck = new CardDeck();
		m_phase = 0;
		// everybody with chips plays again
		for(int i = 0;i<NUM_PLAYER;i++){
			if(chips[i]<=0) {
				playingPlayers[i] = false;
				activePlayers[i] = false;
				foldedPlayers[i] = true;

			}else{
				activePlayers[i] = true;
				foldedPlayers[i] = false;
			}
		}

		// cards
		holeCards = new PlayingCard[NUM_PLAYER][2];
		communityCards = new PlayingCard[5];

		openPlayers = new LinkedList<Integer>();

		pots = new Pots();

		// turn order

		while(!playingPlayers[dealer])
			dealer = (dealer+1)%NUM_PLAYER;

		int smallBlind = (dealer+1)%NUM_PLAYER;
		while(!playingPlayers[smallBlind])
			smallBlind = (smallBlind+1)%NUM_PLAYER;

		int bigBlind = (smallBlind+1)%NUM_PLAYER;
		while(!playingPlayers[bigBlind])
			bigBlind = (bigBlind+1)%NUM_PLAYER;

		System.out.println("\r\n-------------------------------START--------------------------------------");
		System.out.println("Dealer: "+Integer.toString(dealer)+" Smallblind: "+Integer.toString(smallBlind) + " Bigblind: "+Integer.toString(bigBlind));

		if(chips[smallBlind]>SMALLBLIND) {
			chips[smallBlind] -= SMALLBLIND;
			pots.add(SMALLBLIND,smallBlind);
		}else{
			pots.add(SMALLBLIND,chips[smallBlind],true);
			chips[smallBlind] = 0;
		}

		if(chips[bigBlind]>BIGBLIND) {
			chips[bigBlind] -= BIGBLIND;
			pots.add(BIGBLIND,bigBlind);
		}else{
			pots.add(BIGBLIND,chips[bigBlind],true);
			chips[bigBlind] = 0;
		}

		int t = (bigBlind+1)%NUM_PLAYER;
		while(!playingPlayers[t]) {
			t=(t+1)%NUM_PLAYER;
		}

		for(int i = t;i<NUM_PLAYER;i++)
			if(playingPlayers[i])
				openPlayers.add(i);

		for(int i = 0;i<t;i++)
			if(playingPlayers[i])
				openPlayers.add(i);

		if(getNumPlayingPlayers()==1){
			GAMEOVER = true;
		}

		//small & big blind
		dealCards();

		dealer = (dealer+1)%NUM_PLAYER;
	}


	
	public StateObserverPoker copy() {
		StateObserverPoker sot = new StateObserverPoker(this);
		return sot;
	}

    @Override
	public boolean isGameOver() {
		return GAMEOVER;
	}

    @Override
	public boolean isDeterministicGame() {
		return false;
	}
	
    @Override
	public boolean isFinalRewardGame() {
		return true;
	}

    @Override
	public boolean isLegalState() {
		return true;
	}
	
	public boolean isLegalAction(ACTIONS act) {
		return true;
	}

	@Deprecated
    public String toString() {
    	return stringDescr();
    }
	
	@Override
    public String stringDescr() {
		return "";
	}


	public boolean isNextActionDeterministic() {
		return isNextActionDeterministic;
	}

	public ACTIONS getNextNondeterministicAction() {
		setNextNondeterministicAction();
		return nextNondeterministicAction;
	}

	private void setNextNondeterministicAction() {
		if(isNextActionDeterministic) {
			throw new RuntimeException("next Action is Deterministic");
		} else if(nextNondeterministicAction != null) {
			return;
		}
	}

	public ArrayList<ACTIONS> getAvailableRandoms() {
		ArrayList<ACTIONS> availRan = new ArrayList<>();
		for(int viableMove : availableRandoms) {
			availRan.add(ACTIONS.fromInt(viableMove));
		}
		return availRan;

	}

	public int getNumAvailableRandoms() {
		return availableRandoms.size();
	}

	public double getProbability(ACTIONS action) {
		int iAction = action.toInt();
		int numEmptyTiles = iAction/2;
		double prob = (iAction%2==0) ? 0.9 : 0.1;
		return prob/numEmptyTiles;
	}

	/**
	 * 
	 * @return true, if the current position is a win (for either player)
	 */
	public boolean win() {
		return GAMEOVER;
	}


	/**
	 * @return 	the game score, i.e. the sum of rewards for the current state. 
	 * 			For TTT only game-over states have a non-zero game score. 
	 * 			It is the reward from the perspective of {@code refer}.
	 */
	public double getGameScore(StateObservation refer) {
        return 0; 
	}

	public double getMinGameScore() { return REWARD_NEGATIVE; }

	public double getMaxGameScore() { return REWARD_POSITIVE; }

	public String getName() { return "Poker";	}

	public void dealCards(){

		// shuffling the deck to make sure we have a random deck
		m_deck.suffle();
		if(this.m_phase==0){
			// set small blind
			// set big blind
			// deal holecards


			System.out.println("Deal holecards");
			for(int i = 0;i<NUM_PLAYER;i++){
				this.holeCards[i][0] = this.m_deck.draw();
				this.holeCards[i][1] = this.m_deck.draw();
			}
		}

		if(m_phase == 1){
			// deal flop
			System.out.println("Deal flop");
			this.communityCards[0] = this.m_deck.draw();
			this.communityCards[1] = this.m_deck.draw();
			this.communityCards[2] = this.m_deck.draw();
		}

		if(m_phase == 2){
			// deal turn
			System.out.println("Deal turn");
			this.communityCards[3] = this.m_deck.draw();
		}

		if(m_phase == 3){
			// deal river
			System.out.println("Deal river");
			this.communityCards[4] = this.m_deck.draw();
		}

		if(m_phase > 3){

			int[] scores = determineWinner();
			boolean[][] claims = pots.getClaims();
			for(int p = 0;p<claims.length;p++){
				int maxScore = 0;
				ArrayList<Integer> winners = new ArrayList<Integer>();
				for(int i = 0;i<scores.length;i++) {
					if(maxScore == scores[i])
						winners.add(i);
					if(claims[p][i]&&maxScore<scores[i]){
						maxScore = scores[i];
						winners.clear();
						winners.add(i);
					}

				}
				int numWinners = winners.size();

				System.out.println("\r\n------------------------------RESULT---------------------------------------");
				for(int winner:winners){
					System.out.print(Integer.toString(winner)+" has won Pot ("+Integer.toString(p)+")");
					chips[winner] += pots.getPotSize(p)/numWinners;
				}
				System.out.println("\r\n------------------------------RESULT END---------------------------------------");
			}
			initRound();
		}else {
			m_phase++;
		}
	}

	public int[] determineWinner(){
		ArrayList<PlayingCard> cards = new ArrayList<PlayingCard>();
		int[] handscore = new int[NUM_PLAYER];
		for(int i = 0; i < NUM_PLAYER;i++){
			if(!foldedPlayers[i]){
				cards.add(holeCards[i][0]);
				cards.add(holeCards[i][1]);
				cards.add(communityCards[0]);
				cards.add(communityCards[1]);
				cards.add(communityCards[2]);
				cards.add(communityCards[3]);
				cards.add(communityCards[4]);
				handscore[i] = findBestHand(cards);
			}else{
				handscore[i] = -1;
			}
		}
		return handscore;
	}

	public int findBestHand(ArrayList<PlayingCard> cards){
		Random rand = new Random();
		int[] suits = new int[4];
		int[] ranks = new int[13];
		for(PlayingCard c : cards){
			suits[c.getSuit()]++;
			ranks[c.getRank()]++;
		}
		return rand.nextInt(100);
	}

	public void fold(){
		System.out.println("Player "+Integer.toString(m_Player)+": 'FOLD'");
		activePlayers[m_Player] = false;
		foldedPlayers[m_Player] = true;
	}

	public void check(){
		System.out.println("Player "+Integer.toString(m_Player)+": 'CHECK'");
	}

	public void allIn(){
		int betsize = chips[m_Player];
		System.out.println("Player "+Integer.toString(m_Player)+": ALL IN ("+Integer.toString(betsize)+")");
		boolean raise = betsize > pots.getOpenPlayer(m_Player);

		pots.add(betsize, m_Player,true);
		chips[m_Player] = 0;

		if(raise){
			openPlayers.clear();
			for (int i = m_Player + 1; i < NUM_PLAYER; i++)
				if (playingPlayers[i]&&activePlayers[i])
					openPlayers.add(i);

			for (int i = 0; i < m_Player; i++)
				if (playingPlayers[i]&&activePlayers[i])
					openPlayers.add(i);
		}
		activePlayers[m_Player] = false;
	}

	public void bet(int m_betsize){
		int betsize = m_betsize;
		System.out.println("Player "+Integer.toString(m_Player)+": BET ("+Integer.toString(betsize)+")");

		pots.add(betsize, m_Player);
		chips[m_Player] -= betsize;

		//add all but the active player to open players
		int player = (m_Player + 1) % NUM_PLAYER;

		openPlayers.clear();
		for (int i = m_Player + 1; i < NUM_PLAYER; i++)
			if (playingPlayers[i]&&activePlayers[i])
				openPlayers.add(i);

		for (int i = 0; i < m_Player; i++)
			if (playingPlayers[i]&&activePlayers[i])
				openPlayers.add(i);

	}

	public void call(){
		int toCall = pots.getOpenPlayer(m_Player);
		System.out.println("Player "+Integer.toString(m_Player)+": CALL ("+Integer.toString(toCall)+")");

		// shouldn't happen - action is only available if player has more chips.
		if(toCall > chips[m_Player]){
			toCall = chips[m_Player];
		}
		pots.add(toCall,m_Player);
		chips[m_Player] -= toCall;

		if(chips[m_Player]==0) {
			activePlayers[m_Player] = false;
		}
	}

	public void raise(){
		int betsize = pots.getOpenPlayer(m_Player)+BIGBLIND;
		bet(betsize);
	}



	/**
	 * Advance the current state with 'action' to a new state
	 * Actions:
	 * - fold: 0
	 * - check:
	 * - bet:
	 * - call:
	 * - raise[1]:
	 * - raise[2]:
	 * - raise[3]:
	 * - raise[4]:
	 * - raise[5]:
	 *
	 * @param action
	 */
	public void advance(ACTIONS action) {
		int iAction = action.toInt();

		switch (iAction){
			case 0: // FOLD
				fold();
				break;
			case 1: // CHECK
				check();
				break;
			case 2: // BET
				bet(BIGBLIND);
				break;
			case 3: // CALL
				call();
				break;
			case 4: // RAISE
				raise();
				break;
			case 5: // RAISE
				allIn();
				break;

		}

		// only one active player left and game can progress to showdown
		if(getNumActivePlayers()<2&&openPlayers.size()==0){
			while(m_phase<4)
				dealCards();
			dealCards();
		}else {
			// If there are no persons left with an open action the dealer will reveal the next card(s)
			if (openPlayers.size() == 0) {
				System.out.println("Pot "+pots.toString());
				System.out.println("\r\n------------------------------END---------------------------------------");

				// next player from the last action will have the next action if it's not a showdown
				m_Player = (m_Player + 1) % NUM_PLAYER;
				for (int i = m_Player; i < NUM_PLAYER; i++)
					if (playingPlayers[i] && activePlayers[i])
						openPlayers.add(i);
				for (int i = 0; i < m_Player; i++)
					if (playingPlayers[i] && activePlayers[i])
						openPlayers.add(i);
				dealCards();
			}
		}
		if(!GAMEOVER) {
			// next player becomes the active one
			m_Player = openPlayers.remove();

			setAvailableActions();
		}
	}

	public void advanceDeterministic(ACTIONS action) {
		if(!isNextActionDeterministic) {
			throw new RuntimeException("Next action is nondeterministic but called advanceDeterministic()");
		}

		int iAction = action.toInt();

		switch (iAction){
			case 0: // FOLD
				fold();
				break;
			case 1: // CHECK
				check();
				break;
			case 2: // BET
				bet(BIGBLIND);
				break;
			case 3: // CALL
				call();
				break;
			case 4: // RAISE
				raise();
				break;
			case 5: // RAISE
				allIn();
				break;

		}

		if(getNumActivePlayers()<2&&openPlayers.size()==0){
			isNextActionDeterministic = false;
		}else {
			// If there are no persons left with an open action the dealer will reveal the next card(s)
			if (openPlayers.size() == 0) {
				isNextActionDeterministic = false;
			} else {
				isNextActionDeterministic = true;
			}
		}
		if(!GAMEOVER) {
			// next player becomes the active one
			m_Player = openPlayers.remove();
			setAvailableActions();
		}
	}

	public void advanceNondeterministic(ACTIONS action) {
		advanceNondeterministic();
	}

	public void advanceNondeterministic() {
		if(isNextActionDeterministic) {
			throw new RuntimeException("Next action is deterministic but called advanceNondeterministic()");
		}
		// only one active player left and game can progress to showdown
		if(getNumActivePlayers()<2&&openPlayers.size()==0){
			while(m_phase<4)
				dealCards();
			dealCards();
		}else {
			// If there are no persons left with an open action the dealer will reveal the next card(s)
			if (openPlayers.size() == 0) {
				System.out.println("Pot "+pots.toString());
				System.out.println("\r\n------------------------------END---------------------------------------");

				// next player from the last action will have the next action if it's not a showdown
				m_Player = (m_Player + 1) % NUM_PLAYER;
				for (int i = m_Player; i < NUM_PLAYER; i++)
					if (playingPlayers[i] && activePlayers[i])
						openPlayers.add(i);
				for (int i = 0; i < m_Player; i++)
					if (playingPlayers[i] && activePlayers[i])
						openPlayers.add(i);
				dealCards();
			}
		}
		if(!GAMEOVER) {
			// next player becomes the active one
			m_Player = openPlayers.remove();
			setAvailableActions();
		}
		isNextActionDeterministic = true;
	}

    /**
     * Return the afterstate preceding {@code this}. 
     */
    @Override
    public StateObservation getPrecedingAfterstate() {
    	return this;
    }

    @Override
    public ArrayList<ACTIONS> getAllAvailableActions() {
        ArrayList allActions = new ArrayList<>();
		allActions.add(ACTIONS.fromInt(0)); // FOLD
		allActions.add(ACTIONS.fromInt(1)); // CHECK
		allActions.add(ACTIONS.fromInt(2)); // BET
		allActions.add(ACTIONS.fromInt(3)); // CALL
		allActions.add(ACTIONS.fromInt(4)); // RAISE
		allActions.add(ACTIONS.fromInt(5)); // ALL-IN
        return allActions;
    }
    
	public ArrayList<ACTIONS> getAvailableActions() {
		return availableActions;
	}
	
	public int getNumAvailableActions() {
		return availableActions.size();
	}

	/**
	 * Given the current state in m_Table, what are the available actions? 
	 * Set them in member ACTIONS[] actions.
	 */
	public void setAvailableActions() {
		availableActions.clear();
		// You always have the option to fold.
		availableActions.add(ACTIONS.fromInt(0)); // FOLD

		// You can only check if nobody has bet before
		if(pots.getOpenPlayer(m_Player)==0)
			availableActions.add(ACTIONS.fromInt(1)); // CHECK

		// You can only bet if nobody has bet before (otherwise it's a raise)
		if(pots.getOpenPlayer(m_Player)==0&&chips[m_Player]>BIGBLIND)
			availableActions.add(ACTIONS.fromInt(2)); // BET

		// You can only call if somebody has bet before
		if(pots.getOpenPlayer(m_Player)>0&&chips[m_Player]>pots.getOpenPlayer(m_Player))
			availableActions.add(ACTIONS.fromInt(3)); // CALL

		// You can only raise if somebody has bet before
		if(pots.getOpenPlayer(m_Player)>0&&chips[m_Player]>pots.getOpenPlayer(m_Player)+BIGBLIND)
			availableActions.add(ACTIONS.fromInt(4)); // RAISE

		// You can only raise if somebody has bet before
		if(true)
			availableActions.add(ACTIONS.fromInt(5)); // RAISE
	}
	
	public ACTIONS getAction(int i) {
		return availableActions.get(i);
	}

	public int[][] getTable() {
		return new int[1][1];
	}

	/**
	 * @return the id of the active player.
	 */
	public int getPlayer() {
		return m_Player;
	}
	
	public int getNumPlayers() {
		return NUM_PLAYER;				// TicTacToe is a 2-player game
	}

	public int[] getChips(){
		return chips;
	}

	public boolean[] getActivePlayers(){
		return activePlayers;
	}

	public int getPot(){
		return pots.getSize();
	}

	public int getNumActivePlayers(){
		int p = 0;
		for(int i = 0;i<activePlayers.length;i++)
			if(activePlayers[i])
				p++;
		return p;
	}

	public int getNumPlayingPlayers(){
		int p = 0;
		for(int i = 0;i<playingPlayers.length;i++)
			if(playingPlayers[i])
				p++;
		return p;
	}

	public PlayingCard[] getCommunityCards(){
		return communityCards;
	}

	public PlayingCard[] getHoleCards(int player){
		return holeCards[player];
	}

	public PlayingCard[] getHoleCards(){
		if(holeCards!=null)
			return holeCards[m_Player];
		return null;
	}
}
