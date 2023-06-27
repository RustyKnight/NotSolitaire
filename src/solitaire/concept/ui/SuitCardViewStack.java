package solitaire.concept.ui;

import java.awt.Rectangle;
import solitaire.concept.models.Face;

public class SuitCardViewStack extends CardViewStack {
    public SuitCardViewStack() {
        super();
    }

    public SuitCardViewStack(Rectangle bounds) {
        super(bounds);
    }
    
    @Override
    public boolean canPushCard(CardView card) {
        if (isEmpty()) {
            return card.getCard().getFace()== Face.ACE;
        }
        CardView last = peekLast();
        // Do the suits match...
        if (last.getCard().getSuit() != card.getCard().getSuit()) {
            return false;
        }
        // The distance between the face value of the last card and the new card
        // should be one ... nice side effect
        return last.getCard().getFace().comparedTo(card.getCard().getFace()) == 1;
    }
}
