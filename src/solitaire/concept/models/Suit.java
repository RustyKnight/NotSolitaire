package solitaire.concept.models;

public enum Suit {
    CLUB(0), DIAMOND(1), HEART(2), SPADE(3);

    private int value;

    private Suit(int value) {
        this.value = value;
    }

    protected int getValue() {
        return value;
    }

    public int comparedTo(Suit o) {
        return o.getValue() - getValue();
    }
}
