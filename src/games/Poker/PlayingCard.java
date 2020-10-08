package src.games.Poker;

import game.rules.play.Play;

public class PlayingCard {

    private short rank, suit;
    private static String[] suits = { "♥", "♠", "♦", "♣" };
    private static String[] ranks = { "A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K" };

    public static String rankAsString( int rank ) {
        return ranks[rank];
    }

    PlayingCard(short suit, short rank)
    {
        this.rank = rank;
        this.suit = suit;
    }

    public @Override String toString()
    {
        return "["+ suits[suit] + ranks[rank] + "]";
    }

    public short getRank() {
        return rank;
    }

    public short getSuit() {
        return suit;
    }

    public PlayingCard copy(){
        return new PlayingCard(this.suit,this.rank);
    }
}
