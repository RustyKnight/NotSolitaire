package solitaire.concept.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.JPanel;
import solitaire.concept.models.Card;
import solitaire.concept.models.Deck;

public class SolitairePane extends JPanel {

    protected static final int CARD_WIDTH = 64;
    protected static final int CARD_HEIGHT = (int) (CARD_WIDTH * 1.478723404255319);
    protected static final Dimension CARD_SIZE = new Dimension(CARD_WIDTH, CARD_HEIGHT);
    protected static final int CARD_VERTICAL_OFFSET = (int) (CARD_HEIGHT * 0.2);

    private Color stackAreaColor = Color.GREEN.darker();
    private Color stackBackgroundColor;
    private Color stackStrokeColor;

    // Can pretty much accept any card
    private CardViewStack stockStack = new CardViewStack();
    private CardViewStack wasteStack = new CardViewStack();

    // Look at the point in the code where we create these stacks, it's important!
    private List<CardViewStack> suitFoundationStacks = new ArrayList<>(7);
    private List<CardViewStack> tabularStacks = new ArrayList<>(7);

    private BufferedImage cardCoverImage;

    // Some dragging support
    // There may be more then one card been dragged... :/
    private List<CardView> cardsBeenDragged;
    private CardViewStack stackDraggedFrom;
    private Point dragOffset;
    private Point dragPoint;

    //private Rectangle dragBounds;
    private ComponentListener componentHandler;
    private MouseAdapter mouseHandler;

    public SolitairePane(Deck deck) throws IOException {
        deck.shuffle();

        cardCoverImage = CardFactory.cardCover();

        int x = 0;
        for (int index = 0; index < 4; index++) {
            SuitCardViewStack stackView = new SuitCardViewStack(new Rectangle(x, 0, CARD_WIDTH, CARD_HEIGHT));
            suitFoundationStacks.add(stackView);
            x += CARD_WIDTH + 4;
        }

        x = 0;
        for (int stack = 0; stack < 7; stack++) {
            int y = CARD_HEIGHT + 4;
            Rectangle stackBounds = new Rectangle(x, y, CARD_WIDTH, CARD_HEIGHT);

            List<CardView> cards = new ArrayList<>(7);
            // The cards are offset from there stack position, so, lots of fun
            for (int draw = 0; draw < stack; draw++) {
                cards.add(new CardView(deck.pop(), false, new Rectangle(x, y, CARD_WIDTH, CARD_HEIGHT)));
                y += CARD_VERTICAL_OFFSET;
            }
            cards.add(new CardView(deck.pop(), true, new Rectangle(x, y, CARD_WIDTH, CARD_HEIGHT)));

            CardViewStack stackView = new TabularCardViewStack(cards, stackBounds);
            tabularStacks.add(stackView);

            x += CARD_WIDTH + 4;
        }

        Card card = null;
        Rectangle bounds = stockStack.getBounds();
        while ((card = deck.pop()) != null) {
            CardView cardView = new CardView(card, false);
            //cardView.setBounds(new Rectangle(bounds));
            stockStack.push(cardView);
        }

        addComponentListener(getComponentHandler());

        addMouseListener(getMouseHandler());
        addMouseMotionListener(getMouseHandler());
    }

    protected List<CardView> cardsFromExcludingCardsBeenDragged(CardViewStack stack) {
        // Remember, getCards returns a copy, not the actually list
        List<CardView> cards = stack.getCards();
        if (cardsBeenDragged != null) {
            cards.removeAll(cardsBeenDragged);
        }
        return cards;
    }

    protected void layoutCards() {
        System.out.println(">> Layout...");
        int width = getWidth() - 8;
        int x = width - (CARD_WIDTH * 2) - 4;
        Rectangle bounds = new Rectangle(x, 0, CARD_WIDTH, CARD_HEIGHT);
        wasteStack.setBounds(bounds);

        List<CardView> cards = cardsFromExcludingCardsBeenDragged(wasteStack);
        for (CardView cardView : cards) {
            cardView.setBounds(new Rectangle(bounds));
        }
        x += CARD_WIDTH + 4;

        bounds = new Rectangle(x, 0, CARD_WIDTH, CARD_HEIGHT);
        stockStack.setBounds(bounds);
        cards = cardsFromExcludingCardsBeenDragged(stockStack);
        for (CardView cardView : cards) {
            cardView.setBounds(new Rectangle(bounds));
        }

        for (CardViewStack stack : suitFoundationStacks) {
            cards = cardsFromExcludingCardsBeenDragged(stack);
            for (CardView cardView : cards) {
                cardView.setBounds(new Rectangle(stack.getBounds()));
            }
        }

        for (CardViewStack stack : tabularStacks) {
            cards = cardsFromExcludingCardsBeenDragged(stack);
            x = stack.getBounds().x;
            int y = stack.getBounds().y;
            for (CardView cardView : cards) {
                cardView.setBounds(new Rectangle(x, y, CARD_WIDTH, CARD_HEIGHT));
                y += CARD_VERTICAL_OFFSET;
            }
        }
    }

    protected ComponentListener getComponentHandler() {
        if (componentHandler != null) {
            return componentHandler;
        }

        componentHandler = new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                layoutCards();
            }
        };
        return componentHandler;
    }

    protected MouseAdapter getMouseHandler() {
        if (mouseHandler != null) {
            return mouseHandler;
        }

        mouseHandler = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // I've put a lot of work into the mouse handlers, much of this
                // could be delegated to some kind of delegate, but that's
                // another level...bady steps here
                Point p = e.getPoint();
                p.translate(-4, -4);
                // Did you click the stock pile or the waste pile or something else...
                if (stockStack.getBounds() != null && stockStack.getBounds().contains(p)) {
                    // If the stock pile is empty, recycle the waste pile...
                    if (stockStack.isEmpty()) {
                        CardView view = null;
                        while ((view = wasteStack.pop()) != null) {
                            view.setFaceUp(false);
                            view.setBounds(new Rectangle(stockStack.getBounds()));
                            stockStack.push(view);
                        }
                    } else {
                        // We know this can succeed
                        CardView view = stockStack.pop();
                        view.setFaceUp(true);
                        view.setBounds(new Rectangle(wasteStack.getBounds()));
                        wasteStack.push(view);
                    }
                } else if (wasteStack.getBounds() != null && wasteStack.getBounds().contains(p)) {
                    // Was it a double click?
                    if (e.getClickCount() == 2) {
                        // Oppurtunity for some animation here, but that would be
                        // taking things a little to far

                        // Peek at the last card on the stack (ie the one that's
                        // on "top")
                        CardView cardView = wasteStack.peekLast();
                        // Simply make use of the `SuitCardViewStack#canPushCard`
                        // to test if the card can be placed onto any of the
                        // suitFoundationStacks
                        for (CardViewStack stack : suitFoundationStacks) {
                            if (!stack.canPushCard(cardView)) {
                                continue;
                            }
                            // Remove the card from the waste stack...
                            wasteStack.pop();
                            cardView.setBounds(new Rectangle(stack.getBounds()));
                            stack.push(cardView);
                            break;
                        }
                    }
                } else if (e.getClickCount() == 2) {
                    // Let's see if we can find a card which might have been
                    // double clicked

                    for (CardViewStack stackView : tabularStacks) {
                        CardView lastCard = stackView.peekLast();
                        if (lastCard == null) {
                            continue;
                        }
                        if (!lastCard.getBounds().contains(p)) {
                            continue;
                        }
                        System.out.println("Double tapped " + lastCard.getCard());
                        // This basically works along the same lines as the
                        // wasteStack block above.
                        // Walk the `suitFoundationStacks` and see if the card
                        // can be placed on the stack.
                        // If it can, you also need to flip the last card in
                        // the existing stack (once you pop the current card
                        // off the stack)
                        //
                        // Depending on how much you might like to help the player
                        // you could also walk all tabular stacks and see if the card
                        // can be placed on those, the difficulty comes in when
                        // you realise you should probably actually take into
                        // all the cards facing up ... so, yeah, not doing that...
                    }
                }
                repaint();
            }

            // This will combine all the cards and calculate the bounding
            // rectangle they occupy.  This is important when trying to calculate
            // the drag offset need to make it look like the cards are been picked
            // up and dragged from the original mouse press point
            protected Rectangle boundsOf(List<CardView> cards) {
                Area area = new Area();
                for (CardView view : cards) {
                    area.add(new Area(view.getBounds()));
                }
                return area.getBounds();
            }

            // Unified workflow for starting the drag...
            protected void startDrag(CardViewStack originalStack, List<CardView> cards, Point startDragPoint) {
                // We're going to make the cards "appear" larger...
                for (CardView view : cards) {
                    Rectangle bounds = view.getBounds();
                    bounds.x -= 5;
                    bounds.y -= 5;
                    bounds.width += 10;
                    bounds.height += 10;
                }

                cardsBeenDragged = cards;
                stackDraggedFrom = originalStack;
                dragPoint = startDragPoint;
                Rectangle dragBounds = boundsOf(cardsBeenDragged);
                dragOffset = new Point(dragBounds.x - dragPoint.x, dragBounds.y - dragPoint.y);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                // I don't know why and I no longer care :/ - it shold -4/-4 in
                // my head
                p.translate(-6, -7);
                // Did we drag from the waste pile?
                if (wasteStack.getBounds().contains(p) && !wasteStack.isEmpty()) {
                    startDrag(wasteStack, Arrays.asList(new CardView[]{wasteStack.peekLast()}), p);
                } else {
                    // Did we drag from one of the tabular piles?
                    for (CardViewStack stackView : tabularStacks) {
                        // The card list from the stack is unmodifiable
                        // but we'd like to reverse the list so we can start at
                        // the end...
                        List<CardView> cardViews = new ArrayList<>(stackView.getCards());
                        Collections.reverse(cardViews);
                        for (CardView cardView : cardViews) {
                            // Only consider those cards facing up
                            if (!cardView.isFaceUp()) {
                                continue;
                            }
                            if (!cardView.getBounds().contains(p)) {
                                continue;
                            }
                            // Grab all the cards in the stack which are visually
                            // ontop of the one pressed, this should allow the player
                            // to drag a number of cards in one go, because...reasons
                            startDrag(stackView, stackView.allCardsUpToAndIncluding(cardView), p);
                            break;
                        }
                    }
                }
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (cardsBeenDragged != null) {
                    Point p = e.getPoint();
                    p.translate(-6, -7);

                    // This is where it becomes a little more complicated
                    // A the suitFoundationStacks can only accept a single 
                    // card drop (think about it for a second), so if we have
                    // more the one card been dragged, we can ignore those,
                    // otherwise we need to look at both those and the tabular
                    // stacks
                    //
                    // Then we need to know if the bottom in the card stack
                    // can be pushed onto the stack we've dropped on and then
                    // push each card in the drag list onto it ...
                    // 
                    // Like I said, complciated
                    if (cardsBeenDragged.size() == 1) {
                        CardView cardToBeDropped = cardsBeenDragged.get(0);
                        List<CardViewStack> stacksToCheck = new ArrayList<>(11);
                        stacksToCheck.addAll(suitFoundationStacks);
                        stacksToCheck.addAll(tabularStacks);
                        for (CardViewStack stack : stacksToCheck) {
                            if (!stack.combinedBounds().contains(p)) {
                                continue;
                            }
                            if (!stack.canPushCard(cardToBeDropped)) {
                                continue;
                            }
                            // Probably could just pop the card, but to be safe
                            stackDraggedFrom.removeAll(cardsBeenDragged);
                            stack.push(cardToBeDropped);

                            // Flip the last card on the old stack
                            CardView lastCard = stackDraggedFrom.peekLast();
                            if (lastCard != null) {
                                lastCard.setFaceUp(true);
                            }
                            break;
                        }
                    } else {
                        // This is the "link" card, if it can't be pushed onto
                        // the stack, then nothing else matters
                        CardView topCard = cardsBeenDragged.get(0);
                        for (CardViewStack stack : tabularStacks) {
                            if (!stack.combinedBounds().contains(p)) {
                                continue;
                            }
                            if (!stack.canPushCard(topCard)) {
                                continue;
                            }
                            stackDraggedFrom.removeAll(cardsBeenDragged);
                            // Since each subsequent card is already stacked
                            // on the topCard and the topCard can be dropped
                            // on this stack, then, nothing else matters
                            for (CardView card : cardsBeenDragged) {
                                stack.push(card);
                            }
                            // Flip the last card on the old stack
                            CardView lastCard = stackDraggedFrom.peekLast();
                            if (lastCard != null) {
                                lastCard.setFaceUp(true);
                            }
                            break;
                        }
                    }
                    cardsBeenDragged = null;
                    layoutCards();
                }

                dragPoint = null;
                dragOffset = null;
                cardsBeenDragged = null;
                stackDraggedFrom = null;
                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (cardsBeenDragged == null) {
                    return;
                }
                dragPoint = e.getPoint();
                dragPoint.translate(-6, -7);
                int yOffset = 0;
                for (CardView view : cardsBeenDragged) {
                    Rectangle bounds = view.getBounds();
                    bounds.x = dragPoint.x + dragOffset.x;
                    bounds.y = dragPoint.y + dragOffset.y + yOffset;
                    yOffset += CARD_VERTICAL_OFFSET;
                }
                repaint();
            }
        };

        return mouseHandler;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(
                (CARD_WIDTH * 7) + (4 * 6) + 8,
                (int) ((CARD_HEIGHT * 2) + 4 + (CARD_VERTICAL_OFFSET * 6)) + 8
        );
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    public Color getStackAreaColor() {
        return stackAreaColor;
    }

    protected Color getStackBackgroundColor() {
        if (stackBackgroundColor == null) {
            stackBackgroundColor = getStackAreaColor();
            stackBackgroundColor = new Color(stackBackgroundColor.getRed(), stackBackgroundColor.getGreen(), stackBackgroundColor.getBlue(), 128);
        }
        return stackBackgroundColor;
    }

    protected Color getStackStrokeColor() {
        if (stackStrokeColor == null) {
            stackStrokeColor = getStackAreaColor().darker();
        }
        return stackStrokeColor;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        // You might like to play around with these and see what you get
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        // We translate the origin, this makes it easier to place all the
        // content, without needing to first offset it ... again ...
        g2d.translate(4, 4);

        Color stackBackground = getStackBackgroundColor();
        Color stackStroke = getStackStrokeColor();

        // Ability to paint all the card "place holders"
        List<Rectangle> groupedStacks = new ArrayList<>(14);
        for (CardViewStack stack : suitFoundationStacks) {
            groupedStacks.add(stack.getBounds());
        }
        for (CardViewStack stack : tabularStacks) {
            groupedStacks.add(stack.getBounds());
        }
        if (wasteStack.getBounds() != null) {
            groupedStacks.add(wasteStack.getBounds());
        }
        if (stockStack.getBounds() != null) {
            groupedStacks.add(stockStack.getBounds());
        }

        for (Rectangle bounds : groupedStacks) {
            g2d.setColor(stackBackground);
            g2d.fill(bounds);
            g2d.setColor(stackStroke);
            g2d.draw(bounds);
        }

        for (CardViewStack stack : tabularStacks) {
            for (CardView cardView : stack.getCards()) {
                cardView.paint(g2d, this);
            }
        }

        if (!stockStack.isEmpty()) {
            Rectangle bounds = stockStack.getBounds();
            if (bounds != null) {
                g2d.drawImage(cardCoverImage, bounds.x, bounds.y, bounds.width, bounds.height, this);
            }
        }

        if (!wasteStack.isEmpty()) {
            wasteStack.peekLast().paint(g2d, this);
        }

        for (CardViewStack stack : suitFoundationStacks) {
            CardView viewable = stack.peekLast();
            if (viewable != null) {
                stack.peekLast().paint(g2d, this);
            }
        }

        if (cardsBeenDragged != null) {
            // This will cause these cards to be painted on top of everything
            for (CardView view : cardsBeenDragged) {
                view.paint(g2d, this);
            }
        }

        g2d.dispose();
    }

    protected class DragSource {
        private CardViewStack stackView;
        private Rectangle bounds;

        public DragSource(CardViewStack stackView, Rectangle bounds) {
            this.stackView = stackView;
            this.bounds = bounds;
        }

        public Rectangle getBounds() {
            return bounds;
        }

        public CardViewStack getStackView() {
            return stackView;
        }
    }
}
