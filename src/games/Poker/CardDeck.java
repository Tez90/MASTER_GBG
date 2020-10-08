package src.games.Poker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class CardDeck {
    private ArrayList<PlayingCard> cards;

    public CardDeck()
    {
        cards = new ArrayList<PlayingCard>();

        for (short suit=0; suit<=3; suit++) {
            for (short rank=0; rank < 13; rank++) {
                cards.add(new PlayingCard(suit,rank));
            }
        }

        Collections.shuffle(cards);
    }

    public CardDeck(ArrayList<PlayingCard> cards){
        cards = new ArrayList<PlayingCard>();
        for(PlayingCard card:cards)
            this.cards.add(card.copy());
    }

    public CardDeck(CardDeck cd){
        cards = new ArrayList<PlayingCard>();
        for(PlayingCard card:cd.cards)
            this.cards.add(card.copy());
    }


    public CardDeck copy(){
        return new CardDeck(this);
    }

    public void suffle(){
        Collections.shuffle(cards);
    }

    public PlayingCard draw() {
        return cards.remove( 0 );
    }

    public int getTotalCards(){return cards.size();}
}
