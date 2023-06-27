package solitaire.concept.ui;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import solitaire.concept.models.Face;
import solitaire.concept.models.Suit;
import static solitaire.concept.models.Suit.CLUB;
import static solitaire.concept.models.Suit.DIAMOND;
import static solitaire.concept.models.Suit.HEART;
import static solitaire.concept.models.Suit.SPADE;

public class TabularCardViewStack extends CardViewStack {
    // This reduces the need to constentaly re-create this stuff, and since it's
    // been used by each instance of TabularCardViewStack (and should never change)
    // we make it static
    private static ArrayList<Suit> BLACK_SUITS = new ArrayList<>(Arrays.asList(new Suit[] { Suit.SPADE, Suit.CLUB }));
    private static ArrayList<Suit> RED_SUITS = new ArrayList<>(Arrays.asList(new Suit[] { Suit.DIAMOND, Suit.HEART }));    
    private static Map<Suit, List<Suit>> ACCEPTABLE_SUITS = new HashMap<>();
    
    static {
        ACCEPTABLE_SUITS.put(CLUB, RED_SUITS);
        ACCEPTABLE_SUITS.put(SPADE, RED_SUITS);
        ACCEPTABLE_SUITS.put(DIAMOND, BLACK_SUITS);
        ACCEPTABLE_SUITS.put(HEART, BLACK_SUITS);
    }

    public TabularCardViewStack(List<CardView> cards, Rectangle bounds) {
        super(cards, bounds);
    }
    
    @Override
    public boolean canPushCard(CardView card) {
        CardView lastCard = peekLast();
        // If the stack is empty, we only accept kings
        if (lastCard == null) {
            return card.getCard().getFace() == Face.KING;
        }
        // Figure out which suites could acttually be pushed ontop...
        List<Suit> acceptableSuits = ACCEPTABLE_SUITS.get(lastCard.getCard().getSuit());
        if (!acceptableSuits.contains(card.getCard().getSuit())) {
            return false;
        }
        // The incomming face value should one less then the last card face value
        int distance = lastCard.getCard().getFace().comparedTo(card.getCard().getFace());
        return distance == -1;
    }
    
}
