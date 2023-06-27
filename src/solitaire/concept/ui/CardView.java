package solitaire.concept.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.IOException;
import solitaire.concept.models.Card;

public class CardView {
    private Card card;
    private boolean isFaceUp;
    
    private BufferedImage cardImage;
    private BufferedImage cardBack;
    
    private Rectangle bounds;

    public CardView(Card card, boolean isFaceUp, Rectangle bounds) throws IOException {
        this.card = card;
        this.isFaceUp = isFaceUp;
        this.bounds = bounds;
        
        cardImage = CardFactory.imageForCard(card);
        cardBack = CardFactory.cardCover();
    }

    public CardView(Card card, boolean isFaceUp) throws IOException {
        this(card, isFaceUp, null);
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
    
    public void setSize(Dimension size) {
        getBounds().setSize(size);
    }

    public Card getCard() {
        return card;
    }

    public boolean isFaceUp() {
        return isFaceUp;
    }

    public void setFaceUp(boolean isFaceUp) {
        this.isFaceUp = isFaceUp;
    }

    protected BufferedImage getCardImage() {
        return cardImage;
    }

    protected BufferedImage getCardBack() {
        return cardBack;
    }
    
    protected BufferedImage getDisplayableImage() {
        return isFaceUp() ? getCardImage() : getCardBack();
    }
    
    public void paint(Graphics2D g2d, ImageObserver observer) {
        BufferedImage img = getDisplayableImage();
        Rectangle bounds = getBounds();
        g2d.setColor(Color.BLUE);
        g2d.fill(bounds);
        // This is really bad scaling - search for a "divide and conqure"
        // approach instead (of single large step)
        g2d.drawImage(img, bounds.x, bounds.y, bounds.width, bounds.height, observer);
    }
}
