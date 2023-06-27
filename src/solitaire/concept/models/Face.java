package solitaire.concept.models;

public enum Face {
    TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7), EIGHT(8), NINE(9), TEN(10),
    ACE(1), JACK(11), QUEEN(12), KING(13);

    private int value;

    private Face(int value) {
        this.value = value;
    }

    protected int getValue() {
        return value;
    }

    public int comparedTo(Face o) {
        return o.getValue() - getValue();
    }
}
