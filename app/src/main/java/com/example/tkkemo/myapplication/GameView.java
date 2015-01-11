package com.example.tkkemo.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.v4.view.MotionEventCompat;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Stack;


/**
 * Created by tkkemo on 9/18/14.
 * This class implements the main view for the solitaire game
 * it includes a seperate thread that takes care of the graphics
 */
public class GameView extends SurfaceView
        implements SurfaceHolder.Callback {

    /**
     * Subclass used to run the game on a seperate thread.
     */
    class BubbleThread extends Thread {

        /**
         * BUBBLE THREAD PROPERTIES
         */
        //game object that controls logic for the game
        private Game g;
        private int drawSize;
        private boolean run = false, pullCards = false, deckMove = false;
        private boolean animateDeckOnly = false;

        private boolean setUndo; // if true, set undo once the the cursor is released on the undo button
        private boolean undo; // undo the last move
        private Stack<Move> moveStack; // used to undo card movement
        private int undoX, undoY; // come from the card before it is moved so it can easily be undone


        //variables used for dealing with card touches, etc.

        //index of the current card being touched
        private int ind = -1, recipientInd = -1, deckCardIndex = -1;
        private int stack = -1, recipientStack = -1;

        private int numCards, cardsAvailable;

        Card[] topThree;


        private int tapX, tapY;

        private int mLastTouchX;
        private int mLastTouchY;
        private int initialX;
        private int initialY;
        private double headingX;
        private double headingY;

        private int canvasWidth;
        private int canvasHeight;

        private boolean snapBack = false;

        private int cardWidth = 32;
        private int cardHeight = 43;
        private int cardOffsetY = 10;
        private int cardOffsetX = 4;
        private int faceUpCardStagger = 10;

        private int deckOffsetX = 2;
        private int deckOffsetY = 2;
        private int deckCanvasOffsetX = 5;
        private int deckCanvasOffsetY = 5;

        private int border;
        private int buttonWidth;
        private int buttonHeight;

        private int deckFaceUpCardOffsetX = 5 + cardWidth + 5;
        private int deckFaceUpCardOffsetY = 5;

        private boolean deal;
        private int dealSt;
        private int dealIn;


        //used with touch events stores the pointer ID and constant for inv pointerID
        private static final int INVALID_POINTER_ID = -1;
        private int mActivePointerId = INVALID_POINTER_ID;

        //map used to test locations of drawable objects
        //private Map<Integer, int[]> locations;

        private int[] stackX;
        private final int stackY = 70;

        private int[] fourX;
        private final int fourY = 5;

        private ArrayList<Integer> cardFaces;
        private ArrayList<Card> faceUpDeckCards;


        private int finalFourX;
        private int finalFourStack = -1;


        private int numUpCards;


        /**
         * Construct a bubble thread
         *
         * @param surfaceHolder
         * @param context
         */
        public BubbleThread(SurfaceHolder surfaceHolder, Context context) {
            sh = surfaceHolder;
            ctx = context;
        }

        /**
         * Initialize the bubble thread
         */
        public void doStart() {
            synchronized (sh) {

                WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
                Display display = wm.getDefaultDisplay();
                canvasWidth = display.getWidth();
                canvasHeight = display.getHeight();

                setUndo = false;
                undo = false;
                moveStack = new Stack<Move>();  // hold pertinent information for each move
                undoX = 0;
                undoY = 0;

                cardWidth = (int) (canvasWidth * .8 / 7);
                cardHeight = (int) (cardWidth*1.3);
                cardOffsetY = 10;
                cardOffsetX = 4;
                faceUpCardStagger = 10;

                border = (int) (canvasWidth * .10);
                buttonWidth = (int) (canvasWidth * .20);
                buttonHeight = (int) (buttonWidth * 66.0/158.0);

                deckOffsetX = 2;
                deckOffsetY = 2;
                deckCanvasOffsetX = 5;
                deckCanvasOffsetY = 5;

                deckFaceUpCardOffsetX = 5 + cardWidth + 5;
                deckFaceUpCardOffsetY = 5;
                //fill row x locations
                g = new Game();
                deal = true;

                numUpCards = 0;

                topThree = new Card[3];
                // initialize drawSize to 3 for now
                drawSize = 1;
                numCards = 0;

                // used for tap-moving cards
                tapX = 0;
                tapY = 0;

                // how many cards are face up next to the deck
                cardsAvailable = 0;

                //initialize the stack locations
                stackX = new int[7];
                for (int q = 0; q < 7; q++) {
                    stackX[q] = cardOffsetY + q * (cardWidth + cardOffsetX);
                }

                fourX = new int[4];
                for (int q = 0; q < 4; q++) {
                    fourX[q] = stackX[q + 3];
                }

                finalFourX = stackX[3];

                dealSt = 0;
                dealIn = 0;

                int initX, initY;
                initX = deckCanvasOffsetX + deckOffsetX * 3;
                initY = deckCanvasOffsetY + deckOffsetY * 3;

                for (int i = 0; i < 7; i++) {
                    for (int j = 0; j < g.sizeSevenList(i); j++) {
                        g.getSevenList(i, j).setX(initX);
                        g.getSevenList(i, j).setY(initY);
                    }
                }

                //sets the correct starting positions for cards in the seven stacks

                cardFaces = new ArrayList<Integer>();
                faceUpDeckCards = new ArrayList<Card>();
                setDownLocations();

                //adding drawable items
                cardFaces.add(R.drawable.bs1);
                cardFaces.add(R.drawable.bs2);
                cardFaces.add(R.drawable.bs3);
                cardFaces.add(R.drawable.bs4);
                cardFaces.add(R.drawable.bs5);
                cardFaces.add(R.drawable.bs6);
                cardFaces.add(R.drawable.bs7);
                cardFaces.add(R.drawable.bs8);
                cardFaces.add(R.drawable.bs9);
                cardFaces.add(R.drawable.bs10);
                cardFaces.add(R.drawable.bs11);
                cardFaces.add(R.drawable.bs12);
                cardFaces.add(R.drawable.bs13);

                cardFaces.add(R.drawable.rh1);
                cardFaces.add(R.drawable.rh2);
                cardFaces.add(R.drawable.rh3);
                cardFaces.add(R.drawable.rh4);
                cardFaces.add(R.drawable.rh5);
                cardFaces.add(R.drawable.rh6);
                cardFaces.add(R.drawable.rh7);
                cardFaces.add(R.drawable.rh8);
                cardFaces.add(R.drawable.rh9);
                cardFaces.add(R.drawable.rh10);
                cardFaces.add(R.drawable.rh11);
                cardFaces.add(R.drawable.rh12);
                cardFaces.add(R.drawable.rh13);

                cardFaces.add(R.drawable.bc1);
                cardFaces.add(R.drawable.bc2);
                cardFaces.add(R.drawable.bc3);
                cardFaces.add(R.drawable.bc4);
                cardFaces.add(R.drawable.bc5);
                cardFaces.add(R.drawable.bc6);
                cardFaces.add(R.drawable.bc7);
                cardFaces.add(R.drawable.bc8);
                cardFaces.add(R.drawable.bc9);
                cardFaces.add(R.drawable.bc10);
                cardFaces.add(R.drawable.bc11);
                cardFaces.add(R.drawable.bc12);
                cardFaces.add(R.drawable.bc13);

                cardFaces.add(R.drawable.rd1);
                cardFaces.add(R.drawable.rd2);
                cardFaces.add(R.drawable.rd3);
                cardFaces.add(R.drawable.rd4);
                cardFaces.add(R.drawable.rd5);
                cardFaces.add(R.drawable.rd6);
                cardFaces.add(R.drawable.rd7);
                cardFaces.add(R.drawable.rd8);
                cardFaces.add(R.drawable.rd9);
                cardFaces.add(R.drawable.rd10);
                cardFaces.add(R.drawable.rd11);
                cardFaces.add(R.drawable.rd12);
                cardFaces.add(R.drawable.rd13);
            }
        }

        /**
         * runs the thread, locks the canvas and calls doDraw(), runs in loop
         * until run is set to false
         */
        public void run() {
            while (run) {
                Canvas c = null;
                try {
                    c = sh.lockCanvas(null);
                    synchronized (sh) {
                        doDraw(c);
                    }
                } finally {
                    if (c != null) {
                        sh.unlockCanvasAndPost(c);
                    }
                }
            }
        }

        /**
         * setter for run
         *
         * @param b
         */
        public void setRunning(boolean b) {
            run = b;
        }


        public void deal(int st, int in, int headX, int headY) {
            //card will start at the top of the deck, then it will move to the correct spot on the stacks

            //top deck
            g.getSevenList(st, in).setX((int) (g.getSevenList(st, in).getX() + headX * .2));
            g.getSevenList(st, in).setY((int) (g.getSevenList(st, in).getY() + headY * .2));


            int destX = stackX[st];
            int destY = (stackY + in * cardOffsetY);

            if ((Math.abs(g.getSevenList(st, in).getX() - destX) < 10) &&
                    (Math.abs(g.getSevenList(st, in).getY() - destY) < 10)) {
                g.getSevenList(st, in).setX(stackX[st]);
                g.getSevenList(st, in).setY(stackY + cardOffsetY * in);

                if (st == 6 && in == 6)
                    deal = false;

            }
        }

        public void drawStack(Canvas canvas, int st) {

            int left, top, right, bottom;
            for (int j = 0; j < g.sizeSevenList(st); j++) {
                left = g.getSevenList(st, j).getX();
                top = g.getSevenList(st, j).getY();
                right = left + cardWidth;
                bottom = top + cardHeight;

                if (g.getSevenList(st, j).isFaceUp()) {
                    Drawable d = ctx.getResources().getDrawable(cardFaces.get(g.getSevenList(st, j).getGraInd()));
                    d.setBounds(left, top, right, bottom);
                    d.draw(canvas);
                } else {
                    Drawable d = ctx.getResources().getDrawable(R.drawable.b1fv);
                    d.setBounds(left, top, right, bottom);
                    d.draw(canvas);
                }
            }

        }

        public void drawFour(Canvas canvas) {
            int left, top, right, bottom;

            for (int i = 7; i < 11; ++i) {
                for (int j = 0; j < g.sizeFourList(i); j++) {
                    left = g.getDeck(i, j).getX();
                    top = g.getDeck(i, j).getY();
                    right = left + cardWidth;
                    bottom = top + cardHeight;

                    if (g.getDeck(i, j).isFaceUp()) {
                        Drawable d = ctx.getResources().getDrawable(cardFaces.get(g.getDeck(i, j).getGraInd()));
                        d.setBounds(left, top, right, bottom);
                        d.draw(canvas);
                    } else {
                        Drawable d = ctx.getResources().getDrawable(R.drawable.b1fv);
                        d.setBounds(left, top, right, bottom);
                        d.draw(canvas);
                    }
                }
            }

        }

        /**
         * Face-up deck stagger setup
         */
        private void setUpLocations() {

            int size = g.sizeUpList();
            for (int i = 0; i < numUpCards && i < size; ++i) {
                g.getDrawList(size - numUpCards + i).setX(deckFaceUpCardOffsetX + faceUpCardStagger * i);
                g.getDrawList(size - numUpCards + i).setY(deckFaceUpCardOffsetY + faceUpCardStagger * i);
            }
            for (int i = 0; i < size - numUpCards; ++i) {
                g.getDrawList(i).setX(deckFaceUpCardOffsetX + faceUpCardStagger);
                g.getDrawList(i).setY(deckFaceUpCardOffsetY + faceUpCardStagger);
            }
        }


        private void setDownLocations() {

            int drawDeckCards;
            if (g.sizeDownList() > 3)
                drawDeckCards = 3;
            else
                drawDeckCards = g.sizeDownList();


            int size = g.sizeDownList();
            for (int i = 0; i < drawDeckCards && i < size; ++i) {
                g.getDownList(size - drawDeckCards + i).setX(deckCanvasOffsetX + deckOffsetX * i);
                g.getDownList(size - drawDeckCards + i).setY(deckCanvasOffsetY + deckOffsetY * i);
            }
            for (int i = 0; i < size - drawDeckCards; ++i) {
                g.getDownList(i).setX(deckCanvasOffsetX + deckOffsetX);
                g.getDownList(i).setY(deckCanvasOffsetY + deckOffsetY);
            }
        }

        private void doDraw(Canvas canvas) {
            if (undo)
                undoCardMove(); // undo the last move

            int left, top, bottom, right;
            if (snapBack)
                snapBack();



            if (deal) {
                int headX, headY, initX, initY;
                initX = deckCanvasOffsetX + deckOffsetX * 3;
                initY = deckCanvasOffsetY + deckOffsetY * 3;

                for (int i = 0; i < 7; i++) {
                    for (int j = 0; j < g.sizeSevenList(i); j++) {
                        headX = stackX[i] - initX;
                        headY = stackY + cardOffsetY * j - initY;

                        deal(i, j, headX, headY);
                    }
                }

            }

            if (stack == 12) {
                for (int i = 0; i < drawSize; i++) {
                    if (g.sizeDownList() == 0 && i == 0) {
                        numUpCards = 0;
                        g.nextCard();
                        break;
                    } else if (g.sizeDownList() == 0 && i == 1) {
                        numUpCards = 1;
                        break;
                    } else if (g.sizeDownList() == 0 && i == 2) {
                        numUpCards = 2;
                        break;
                    } else {
                        g.nextCard();
                        numUpCards = (g.sizeUpList() < 3) ? g.sizeUpList() : 3;
                    }


                }
                setUpLocations();
                setDownLocations();
                stack = -1;
            }

            canvas.save();
            canvas.restore();
            canvas.drawColor(Color.GREEN);

            drawDown(canvas);
            drawUp(canvas);

            drawFour(canvas);

            // iterate through the 7 stacks to draw
            for (int i = 0; i < 7; i++) {
                drawStack(canvas, i);
            }

            if (stack > -1) {
                left = g.getDeck(stack, ind).getX();
                right = left + cardWidth;
                top = g.getDeck(stack, ind).getY();
                bottom = top + cardHeight;
                // draw the next card
                Drawable d = ctx.getResources().getDrawable(cardFaces.get(g.getDeck(stack, ind).getGraInd()));
                d.setBounds(left, top, right, bottom);
                d.draw(canvas);
            }

            right = canvasWidth - border;
            left = right - buttonWidth;
            bottom = canvasHeight - 3*border;
            top = bottom - buttonHeight;

            Drawable und = ctx.getResources().getDrawable(R.drawable.undo);
            und.setBounds(left, top, right, bottom);
            und.draw(canvas);

            //redraw the carried stack so that it animates on top of other stacks
            if (stack >= 0 && stack < 7 && ind >= 0) {
                drawStack(canvas, stack);
            }

            if (g.finalConditions())
                doStart();

        }


        private void drawDown(Canvas canvas) {
            int left, top, right, bottom;


            // this will reset the deckIndex to 0. This whole setup allows us to have no face-up
            // cards animate when the deck is clicked. We will stop animating cards since the
            // deckIndex will be -1. In nextCard(), when the index is -1 we simply reset the
            // deckIndex to 0, while returning null.


            int drawDeckCards = 0;

            if (g.sizeDownList() > 3)
                drawDeckCards = 3;
            else
                drawDeckCards = g.sizeDownList();

            // draw the deck

            int size = g.sizeDownList();
            for (int i = 0; i < drawDeckCards; i++) {// if the the last card in the deck has been pulled, don't animate the deck
                left = g.getDownList(size - drawDeckCards + i).getX();
                top = g.getDownList(size - drawDeckCards + i).getY();
                right = left + cardWidth;
                bottom = top + cardHeight;

                Drawable d = ctx.getResources().getDrawable(R.drawable.b1fv);
                d.setBounds(left, top, right, bottom);
                d.draw(canvas);
            }
        }

        private void drawUp(Canvas canvas) {
            int left, top, right, bottom;


            // this will reset the deckIndex to 0. This whole setup allows us to have no face-up
            // cards animate when the deck is clicked. We will stop animating cards since the
            // deckIndex will be -1. In nextCard(), when the index is -1 we simply reset the
            // deckIndex to 0, while returning null.


            int size = g.sizeUpList();
            // animate the face-up cards
            if (size > 0) {
                for (int i = 0; i < numUpCards && i < size; ++i) {
                    left = g.getDrawList(size - numUpCards + i).getX();
                    right = left + cardWidth;
                    top = g.getDrawList(size - numUpCards + i).getY();
                    bottom = top + cardHeight;
                    // draw the next card
                    Drawable d = ctx.getResources().getDrawable(cardFaces.get(g.getDrawList(size - numUpCards + i).getGraInd()));
                    d.setBounds(left, top, right, bottom);
                    d.draw(canvas);
                }
            }
        }

        /**
         * Snap back to the old location if the card could not move or snap to the recipient stack if it can move.
         */
        private void snapBack() {

            //
            for (int z = ind; z < g.sizeDeck(stack); z++) {
                g.getDeck(stack, z).addX((int) (headingX * .2));
                g.getDeck(stack, z).addY((int) (headingY * .2));
            }


            if ((Math.abs(g.getDeck(stack, ind).getX() - initialX) < 10) &&
                    (Math.abs(g.getDeck(stack, ind).getY() - initialY) < 10)) {
                for (int z = ind; z < g.sizeDeck(stack); z++) {
                    g.getDeck(stack, z).setX(initialX);
                    g.getDeck(stack, z).setY(initialY + (z - ind) * cardOffsetY);
                }

                if (recipientStack != -1 && stack != -1)
                {
                    boolean wasFlipped;

                    // check to see if the card under the one being moved is currently face up so we can set Move appropriately
                    if(g.sizeDeck(stack) > 1)
                        wasFlipped = g.getDeck(stack, ind - 1).isFaceUp() ? false : true;
                    else
                        wasFlipped = false;

                    // public Move(int movedFrom, int movedToIndex, int movedTo, int baseX, int baseY, boolean flippedPrevious)
                    moveStack.add(new Move(stack, g.sizeDeck(recipientStack), recipientStack, undoX, undoY, wasFlipped));
                    // move the card from stack @ ind to the recipientStack
                    g.moveCard(stack, ind, recipientStack);

                    // decrement the number of cards showing if a card was moved from the face-up deck
                    if(stack == 11)
                        numUpCards--;
                }

                deckMove = false;
                snapBack = false;
                stack = -1;
                recipientStack = -1;
                ind = -1;
                setUpLocations();
                setDownLocations();
            }
        }

        /**
         * Undo the last move.
         */
        public void undoCardMove()
        {
            Move move = moveStack.pop();
            int fromIndex = move.getMovedToIndex();
            int from = move.getMovedTo();
            int to = move.getMovedFrom();
            int baseX = move.getBaseX();
            int baseY = move.getBaseY();
            boolean flipUnderCard = move.getFlippedPrevious();


            for (int z = fromIndex; z <= (g.sizeDeck(from) - 1); z++) {
                g.getDeck(from, z).setX(baseX);
                g.getDeck(from, z).setY(baseY + (z - (g.sizeDeck(from)-1)) * cardOffsetY);
            }

            // flip the card over if flipUnderCard is set
            if(flipUnderCard)
                g.getDeck(to, g.sizeDeck(to)-1).undoFlip();

            g.unconditionalUndoMove(from, fromIndex, to);

            setUndo = false;
            undo = false;
        }

        /**
         * callback for touch events
         *
         * @param e
         * @return
         */
        public boolean doTouch(MotionEvent e) {
            Canvas c = null;

            try {
                final int action = MotionEventCompat.getActionMasked(e);
                c = sh.lockCanvas(null);


                switch (action) {
                    case MotionEvent.ACTION_DOWN: {
                        final int pointerIndex = MotionEventCompat.getActionIndex(e);
                        final int x = (int) MotionEventCompat.getX(e, pointerIndex);
                        final int y = (int) MotionEventCompat.getY(e, pointerIndex);

                        tapX = x;
                        tapY = y;

                        // reset to make sure it works only when you both click and release the undo button
                        setUndo = false;
                        undo = false;

                        mLastTouchX = x;
                        mLastTouchY = y;

                        //minimize this, have only one function that handles card touches
                        //either you click on a card or on nothing, also stack should
                        //correspond to all situations when you would be picking up a card
                        cardTouch(x, y);

                        // check if undo is clicked
                        undoTouch(x, y);


                        //get initial X and Y
                        if (stack > -1 && stack < 12) {
                            initialX = g.getDeck(stack, ind).getX();
                            initialY = g.getDeck(stack, ind).getY();
                        }
                        mActivePointerId = MotionEventCompat.getPointerId(e, 0);
                        break;
                    }
                    case MotionEvent.ACTION_MOVE: {
                        // Find the index of the active pointer and fetch its position
                        final int pointerIndex =
                                MotionEventCompat.findPointerIndex(e, mActivePointerId);

                        final int x = (int) MotionEventCompat.getX(e, pointerIndex);
                        final int y = (int) MotionEventCompat.getY(e, pointerIndex);

                        // Calculate the distance moved
                        final int dx = x - mLastTouchX;
                        final int dy = y - mLastTouchY;

                        //code handles moving a card and the stack underneath it
                        if (stack > -1 && stack < 12) {
                            g.getDeck(stack, ind).addX(dx);
                            g.getDeck(stack, ind).addY(dy);
                            if ((g.sizeDeck(stack) - 1) > ind)
                                for (int z = ind + 1; z < g.sizeDeck(stack); z++) {
                                    g.getDeck(stack, z).addX(dx);
                                    g.getDeck(stack, z).addY(dy);
                                }
                        }

                        invalidate();

                        // Remember this touch position for the next move event
                        mLastTouchX = x;
                        mLastTouchY = y;

                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        // we need to determine if the current card (if there is one) is hovering
                        // over another card. If so, then we need to calculate recipientStack and
                        // recipientInd. We then need to attempt to move the card. If it can move,
                        // we calculate the new x and y positions. We then snapback to those positions.

                        final int pointerIndex = MotionEventCompat.getActionIndex(e);
                        final int x = (int) MotionEventCompat.getX(e, pointerIndex);
                        final int y = (int) MotionEventCompat.getY(e, pointerIndex);

                        //Clean up action-up area
                        cardTouchRecipient(x, y);

                        // check if undo is clicked
                        undoTouch(x, y);

                        //auto complete if user does not drag card
                        if (Math.abs(x - tapX) < 10 && Math.abs(y - tapY) < 10) {

                            int i = 7;
                            while (true) {
                                if ((stack > 11) || stack == -1)
                                    break; // don't attempt auto completion if no card or face-down deck selected
                                else if (g.canMoveCard(stack, ind, i)) // if move is possible, set parameters accordingly
                                {
                                    recipientStack = i;
                                    setInitXY(i);
                                    break;
                                }

                                i++;
                                i = i % 11; // keep i from attempting to place cards on the face-up or face-down deck
                                if (i == 7) {
                                    stack = -1; // if the card can't be placed, set stack to -1
                                    break;
                                }
                            }
                        }

                        //if recipient stack is a valid move then set the initialX and initialY values to recipientStack's Coordinates
                        else if (recipientStack != -1 && recipientStack < 12 && g.canMoveCard(stack, ind, recipientStack))
                        { setInitXY(recipientStack); }

                        if (stack != -1 && stack < 12) {
                            headingX = initialX - g.getDeck(stack, ind).getX();
                            headingY = initialY - g.getDeck(stack, ind).getY();
                            snapBack = true;
                        }

                        mActivePointerId = INVALID_POINTER_ID;
                        break;
                    }

                    case MotionEvent.ACTION_CANCEL: {
                        mActivePointerId = INVALID_POINTER_ID;
                        break;
                    }

                    case MotionEvent.ACTION_POINTER_UP: {

                        final int pointerIndex = MotionEventCompat.getActionIndex(e);
                        final int pointerId = MotionEventCompat.getPointerId(e, pointerIndex);

                        if (pointerId == mActivePointerId) {
                            // This was our active pointer going up. Choose a new
                            // active pointer and adjust accordingly.
                            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                            mLastTouchX = (int) MotionEventCompat.getX(e, newPointerIndex);
                            mLastTouchY = (int) MotionEventCompat.getY(e, newPointerIndex);
                            mActivePointerId = MotionEventCompat.getPointerId(e, newPointerIndex);
                        }
                        break;
                    }
                }
                synchronized (sh) {
                    doDraw(c);
                }
            } finally {
                if (c != null) {
                    sh.unlockCanvasAndPost(c);
                }
            }
            return true;
        }

        /**
         * Set the initial X & Y when doing auto-completion on single clicks.
         * @param dest
         */
        public void setInitXY(int dest) {
            if (dest < 7) {
                initialX = stackX[dest];
                initialY = stackY + g.sizeDeck(dest) * cardOffsetY;
            } else {

                initialX = fourX[dest - 7];
                initialY = fourY;
            }
        }

        /**
         * Modify the canvas size.
         * @param width
         * @param height
         */
        public void setSurfaceSize(int width, int height) {
            synchronized (sh) {
                canvasWidth = width;
                canvasHeight = height;
                doStart();
            }
        }

        /**
         * Set undo to true if the cursor was depressed and released in the undo-button area.
         * @param x
         * @param y
         */
        public void undoTouch(double x, double y)
        {
            int right = canvasWidth - border;
            int left = right - buttonWidth;
            int bottom = canvasHeight - 3*border;
            int top = bottom - buttonHeight;
            // for now, make the entire bottom half of the screen the undo button
            if(!setUndo)
            {
                if ( y > top && y < bottom && x > left && x < right)
                    setUndo = true;
                else
                    setUndo = false;

                undo = false;
            }
            else
            {
                if ( y > top && y < bottom && x > left && x < right)
                    setUndo = true;
                else
                    setUndo = false;

                if(setUndo)
                    undo = true;
            }


        }

        /**
         * Return the index and the stack of the card touched when appropriate.
         * @param x
         * @param y
         * @return
         */
        public void cardTouch(double x, double y)
        {
            int left, top, right, bottom;
            stack = -1;
            ind = -1;

            int check = checkSeven(x, y);
            //checks the bottom 7 stacks
            if (check > -1) {
                stack = check;
                int temp = (int) ((y - stackY) / cardOffsetY);

                if (temp < g.sizeSevenList(stack)) {
                    ind = temp;
                } else {
                    ind = g.sizeSevenList(stack) - 1;
                }


                if(stack != -1 && g.sizeDeck(stack) > 0)
                {
                    undoX = g.getDeck(stack, ind).getX();
                    undoY = g.getDeck(stack, ind).getY();
                }

                return;
            }

            // check the four winning piles
            check = checkFour(x,y);
            if (check > -1) {
                stack = check;
                ind = g.sizeFourList(stack) - 1;
            }

            // check the face-down deck
            check = checkDown(x, y);
            if (check > -1) {
                stack = check;
                ind = g.sizeDownList() - 1;
            }

            // check the face-up deck
            check = checkUp(x, y);
            if (check > -1) {
                stack = check;
                ind = g.sizeUpList() - 1;
            }

            // set the coordinates of the card's corresponding Move so the movement can be undone
            if(stack != -1 && g.sizeDeck(stack) > 0)
            {
                undoX = g.getDeck(stack, ind).getX();
                undoY = g.getDeck(stack, ind).getY();
            }
        }

        /**
         * Determines the recipient stack when moving a card.
         * @param x
         * @param y
         */
        private void cardTouchRecipient(int x, int y) {
            recipientStack = -1;
            int check = checkSeven(x, y);
            if (check > -1)
                recipientStack = check;

            check = checkFour(x, y);
            if (check > -1)
                recipientStack = check;
        }

        /**
         * Determines if the selected area is the face-down deck.
         * @param x
         * @param y
         * @return
         */
        private int checkDown(double x, double y) {
            int left, top, right, bottom;

            for (int i = 0; i < 3; i++) {
                left = i*deckOffsetX + deckCanvasOffsetX;
                top = i*deckOffsetY + deckCanvasOffsetY;
                right = left + cardWidth;
                bottom = top + cardHeight;

                if (x > left && x < right && y > top && y < bottom) {
                    return 12;
                }
            }
            return -1;
        }

        /**
         * Check to see if a card was touched in the seven-stack region.
         * @param x
         * @param y
         * @return
         */
        private int checkSeven(double x, double y) {
            int left, top, right, bottom;

            for (int i = 0; i < 7; i++) {
                left = stackX[i];
                top = stackY;
                right = left + cardWidth;
                bottom = stackY + cardOffsetY * (g.sizeSevenList(i) - 1) + cardHeight;

                if (x > left && x < right && y > top && y < bottom) {
                    return i;
                }
            }
            return -1;
        }

        /**
         * Check to see if a card was touched in the winning-stack region.
         * @param x
         * @param y
         * @return
         */
        private int checkFour(double x, double y) {
            for (int i = 0; i < 4; i++) {
                if (x > fourX[i] && x < (fourX[i] + cardWidth))
                {
                    if (y > fourY && y < fourY + cardHeight)
                    {
                        return i + 7;
                    }
                }
            }
            return -1;
        }

        /**
         * Determine if a card was touched on the face-up deck.
         * @param x
         * @param y
         * @return
         */
        private int checkUp(double x, double y) {
            int left, top, right, bottom;


            left = deckFaceUpCardOffsetX + (numUpCards-1)*faceUpCardStagger;
            top = deckFaceUpCardOffsetY+ (numUpCards-1)*faceUpCardStagger;
            right = left + cardWidth;
            bottom = top + cardHeight;

            if (x > left && x < right && y > top && y < bottom)
                return 11;
            return -1;
        }


        /**
         * For now, you can touch anywhere on the three-card deck to get cards.
         * @param x
         * @param y
         */
        public void cardTouchDeck(double x, double y)
        {
            pullCards = false;
            if (x > deckCanvasOffsetX && x < (deckCanvasOffsetX + cardWidth + 2*deckOffsetX) &&
                    y > deckCanvasOffsetY && y < (deckCanvasOffsetY + cardHeight + 2*deckOffsetY))
            { pullCards = true; }
        }


    }

    private Context ctx;
    BubbleThread thread;

    private SurfaceHolder sh;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);


    public GameView(Context context) {
        super(context);
        sh = getHolder();
        sh.addCallback(this);

        ctx = context;
        setFocusable(true);
    }
    public BubbleThread getThread() {
        return thread;
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Canvas canvas = sh.lockCanvas();
        canvas.drawColor(Color.GREEN);
        sh.unlockCanvasAndPost(canvas);

        thread = new BubbleThread(sh, ctx);
        thread.setSurfaceSize(1080, 1920);

        //start the game up and then start the graphics thread
        thread.setRunning(true);
        thread.start();
    }


    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        thread.setSurfaceSize(width, height);
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        thread.setRunning(false);
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        return thread.doTouch(e);
    }

    /**
     * This class is used to undo moves. For each move in the game, a Move object is placed onto
     * a stack. This stack can be popped back to undo the entire game. Each Move item will provide
     * information on how to undo the last move.
     */
    private class Move
    {
        int movedFrom, movedToIndex, movedTo; // determine which card to remove and where to put it
        int baseX, baseY; // initial coordinates of the card that was moved
        boolean flippedPrevious;


        public Move(int movedFrom, int movedToIndex, int movedTo, int baseX, int baseY, boolean flippedPrevious)
        {
            this.movedFrom = movedFrom;
            this.movedToIndex = movedToIndex;
            this.movedTo = movedTo;
            this.baseX = baseX;
            this.baseY = baseY;
            this.flippedPrevious = flippedPrevious;
        }

        public int getMovedFrom()
        { return movedFrom; }

        public int getMovedToIndex()
        { return movedToIndex; }

        public int getMovedTo()
        { return movedTo; }

        public int getBaseY()
        { return baseY; }

        public int getBaseX()
        { return baseX; }

        public boolean getFlippedPrevious()
        { return flippedPrevious; }



    }
}

