package solitaire.concept.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
    private List<Card> cards = new ArrayList<>(52);

    public Deck() {
        for (Suit suit : Suit.values()) {
            for (Face face : Face.values()) {
                cards.add(new Card(suit, face));
            }
        }
        sort();
    }

    public List<Card> getCards() {
        return Collections.unmodifiableList(cards);
    }

    public Card pop() {
        if (cards.isEmpty()) {
            return null;
        }
        return cards.remove(0);
    }

    public void push(Card card) {
        cards.add(card);
    }

    public void sort() {
        Collections.sort(cards);
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }
}
