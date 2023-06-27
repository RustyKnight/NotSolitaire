package solitaire.concept.ui;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import solitaire.concept.models.Card;

public class CardFactory {
    
    private static Map<Card, BufferedImage> mapCards = new HashMap<>();
    private static BufferedImage cardBack;
    
    public static BufferedImage imageForCard(Card card) throws IOException {
        BufferedImage img = mapCards.get(card);
        if (img != null) {
            return img;
        }
        String suit = suitNameForCard(card);
        String face = faceNameForCard(card);
        if (suit == null || face == null) {
            throw new IOException("Invalid card suit/face");
        }
        
        String resourceName = "/cards/" + suit + face + ".png";
        img = ImageIO.read(CardFactory.class.getResource(resourceName));
        mapCards.put(card, img);
        return img;
    }
    
    public static BufferedImage cardCover() throws IOException {
        if (cardBack != null) {
            return cardBack;
        }
        cardBack = ImageIO.read(CardFactory.class.getResource("/cards/Cover.png"));
        return cardBack;
    }
    
    protected static String suitNameForCard(Card card) {
        switch (card.getSuit()) {
            case CLUB: return "Clubs";
            case DIAMOND: return "Diamonds";
            case HEART: return "Hearts";
            case SPADE: return "Spades";
        }
        return null;
    }
    
    protected static String faceNameForCard(Card card) {
        switch (card.getFace()) {
            case ACE: return "Ace";
            case JACK: return "Jack";
            case QUEEN: return "Queen";
            case KING: return "King";
            case TWO: return "02";
            case THREE: return "03";
            case FOUR: return "04";
            case FIVE: return "05";
            case SIX: return "06";
            case SEVEN: return "07";
            case EIGHT: return "08";
            case NINE: return "09";
            case TEN: return "10";
        }
        return null;
    }
}
