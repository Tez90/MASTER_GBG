package src.games.Poker;

import game.rules.play.Play;

public class PlayingCard {

    private int rank, suit;
    static String[] suits = { "♥", "♠", "♦", "♣" };
    static String[] ranks = { "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A" };

    public static String rankAsString( int rank ) {
        return ranks[rank];
    }
    public static String suitAsString( int rank ) {
        return suits[rank];
    }

    PlayingCard(int suit, int rank)
    {
        this.rank = rank;
        this.suit = suit;
    }

    public @Override String toString()
    {
        return "["+ suits[suit] + ranks[rank] + "]";
    }

    public int getRank() {
        return rank;
    }

    public int getSuit() {
        return suit;
    }

    public PlayingCard copy(){
        return new PlayingCard(this.suit,this.rank);
    }
}
