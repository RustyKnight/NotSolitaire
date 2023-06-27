package solitaire.concept.models;


public class Card implements Comparable<Card> {
    private Suit suit;
    private Face face;

    public Card(Suit suit, Face face) {
        this.suit = suit;
        this.face = face;
    }

    public Face getFace() {
        return face;
    }

    public Suit getSuit() {
        return suit;
    }

    @Override
    public int compareTo(Card o) {
        int suitComparison = getSuit().comparedTo(o.getSuit());
        if (suitComparison == 0) {
            return suitComparison;
        }

        return getFace().comparedTo(o.getFace());
    }

    @Override
    public String toString() {
        return getSuit() + "/" + getFace();
    }
}
