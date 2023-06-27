package solitaire.concept.ui;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// This is basically a FILO stack
public class CardViewStack {
    private List<CardView> cards;
    // I spent a lot of time um'ing and ah'ing over how to handle the visual
    // bounds of the stacks and cards.  I started out with them been seperated
    // and either storing a limited number of elements and/or calculating the
    // bounds on the fly.  I then thought about using a `Map` to map the 
    // Rectangle and view model together, but then I'd need a bi-directional
    // look up workflow.
    // So, instead, I decided that the "view model" could carry the bounds as
    // well.
    private Rectangle bounds;

    public CardViewStack() {
        cards = new ArrayList<>(52);
    }

    public CardViewStack(List<CardView> cards, Rectangle bounds) {
        this.cards = cards;
        this.bounds = bounds;
    }

    public CardViewStack(List<CardView> cards) {
        this(cards, null);
    }

    public CardViewStack(Rectangle bounds) {
        this(new ArrayList<>(52), bounds);
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void setBounds(Rectangle bounds) {
        this.bounds = bounds;
    }

    public void setLocation(Point point) {
        getBounds().setLocation(point);
    }

    public int size() {
        return cards.size();
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public List<CardView> getCards() {
        return Collections.unmodifiableList(cards);
    }

    public boolean canPushCard(CardView card) {
        return true;
    }

    public boolean push(CardView card) {
        if (canPushCard(card)) {
            cards.add(card);
            return true;
        }
        return false;
    }

    public CardView pop() {
        if (cards.isEmpty()) {
            return null;
        }
        return cards.remove(cards.size() - 1);
    }

    public List<CardView> allCardsUpToAndIncluding(CardView view) {
        int startIndex = cards.indexOf(view);
        if (startIndex < 0) {
            return null;
        }
        return new ArrayList<>(cards.subList(startIndex, cards.size()));
    }

    public void removeAll(List<CardView> views) {
        cards.removeAll(views);
    }

    public List<CardView> popAll() {
        List<CardView> allCards = new ArrayList<>(cards);
        cards.clear();
        return allCards;
    }

    public List<CardView> pushAll(List<CardView> otherCards) {
        List<CardView> discarded = new ArrayList<>(otherCards.size());
        for (CardView view : otherCards) {
            if (canPushCard(view)) {
                cards.addAll(otherCards);
            } else {
                discarded.add(view);
            }
        }
        return discarded;
    }

    public CardView peekLast() {
        if (isEmpty()) {
            return null;
        }
        return cards.get(cards.size() - 1);
    }

    public CardView peekFirst() {
        if (isEmpty()) {
            return null;
        }
        return cards.get(0);
    }

    // This will calcualate the total visiual bounds that this stack and it's
    // cards occupy.  It's important that to remember, that some stacks present
    // there cards staggered, so the "visual" bounds is not just the bounds
    // of the underlying stack itsef (when it's empty)
    public Rectangle combinedBounds() {
        Area area = new Area(getBounds());
        for (CardView view : cards) {
            area.add(new Area(view.getBounds()));
        }
        return area.getBounds();
    }
}
