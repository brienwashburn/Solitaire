package com.example.tkkemo.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;


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


        //canvas variables
        private int canvasWidth = 200;
        private int canvasHeight = 400;

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

        private static final double SPEED = .1;
        private boolean snapBack = false;

        private final int cardWidth = 32;
        private final int cardHeight = 43;
        private final int cardOffsetY = 10;
        private final int cardOffsetX = 4;
        private final int faceUpCardStagger = 10;

        private final int deckOffsetX = 2;
        private final int deckOffsetY = 2;
        private final int deckCanvasOffsetX = 5;
        private final int deckCanvasOffsetY = 5;

        private int deckFaceUpCardOffsetX = 5 + cardWidth + 5;
        private int deckFaceUpCardOffsetY = 5;

        private boolean deal;
        private int dealSt;
        private int dealIn;

        //used with touch events stores the pointer ID and constant for inv pointerid
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
        private int finalFourStack = - 1;


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
                    fourX[q] = stackX[q+3];
                }

                finalFourX = stackX[3];

                dealSt = 0;
                dealIn = 0;

                int initX, initY;
                initX = deckCanvasOffsetX+ deckOffsetX*3;
                initY = deckCanvasOffsetY+ deckOffsetY*3;

                for (int i = 0 ; i < 7; i++) {
                    for (int j = 0; j < g.sizeSevenList(i); j++) {
                        g.getSevenList(i, j).setX(initX);
                        g.getSevenList(i, j).setY(initY);
                    }
                }

                //sets the correct starting positions for cards in the seven stacks

                cardFaces = new ArrayList<Integer>();
                faceUpDeckCards = new ArrayList<Card>();

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


        /**
         * method modifies the canvas size
         *
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

        public void deal(int st, int in, int headX, int headY){
            //card will start at the top of the deck, then it will move to the correct spot on the stacks

            //top deck
            g.getSevenList(st, in).setX((int) (g.getSevenList(st, in).getX() + headX * .2));
            g.getSevenList(st, in).setY((int) (g.getSevenList(st, in).getY() + headY * .2));


            int destX = stackX[st];
            int destY = (stackY + in * cardOffsetY);

            if ((Math.abs(g.getSevenList(st, in).getX() - destX) < 10) &&
                    (Math.abs(g.getSevenList(st, in).getY() - destY) < 10)) {
                g.getSevenList(st, in).setX(stackX[st]);
                g.getSevenList(st, in).setY(stackY + cardOffsetY * in );

                if (st == 6&& in == 6)
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

            for(int i = 7; i < 11; ++i) {
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


        private void setUpLocations() {

            int size = g.sizeUpList();
            for (int i = 0 ; i < numUpCards && i  < size ; ++i) {
                g.getDrawList(size - numUpCards + i).setX(deckFaceUpCardOffsetX + faceUpCardStagger * i);
                g.getDrawList(size - numUpCards + i).setY(deckFaceUpCardOffsetY + faceUpCardStagger * i);
            }
            for (int i = 0; i < size - numUpCards; ++i){
                g.getDrawList(i).setX(deckFaceUpCardOffsetX + faceUpCardStagger);
                g.getDrawList(i).setY(deckFaceUpCardOffsetY + faceUpCardStagger);
            }
        }

        private void doDraw(Canvas canvas) {

            int left, top, bottom, right;
            if (snapBack) {
                snapBack();
            }

            if (deal) {

                int headX, headY, initX, initY;
                initX = deckCanvasOffsetX+ deckOffsetX*3;
                initY = deckCanvasOffsetY+ deckOffsetY*3;

                for (int i = 0; i < 7; i++)
                {
                    for (int j = 0; j < g.sizeSevenList(i);  j++){
                        headX = stackX[i] - initX;
                        headY = stackY + cardOffsetY * j - initY;

                        deal(i, j, headX, headY);
                    }
                }

            }

            if (stack == 12) {
                for (int i = 0; i< drawSize; i ++)
                {
                    if (g.sizeDownList() == 0 && i == 0) {
                        numUpCards = 0;
                        g.nextCard();
                        break;
                    }
                    else if (g.sizeDownList() == 0 && i == 1) {
                        numUpCards = 1;
                        break;
                    }
                    else if (g.sizeDownList() == 0 && i == 2) {
                        numUpCards = 2;
                        break;
                    }
                    else {
                        g.nextCard();
                        numUpCards = (g.sizeUpList() < 3) ? g.sizeUpList():3;
                    }
                }
                setUpLocations();
                stack = -1;
            }

            canvas.save();
            canvas.restore();
            canvas.drawColor(Color.GREEN);

            drawDown(canvas);
            drawUp(canvas);

            drawFour(canvas);

            // iterate through the 7 stacks to draw
            for (int i = 0; i < 7 ; i++) {
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



            //redraw the carried stack so that it animates on top of other stacks
            if (stack >= 0 && stack < 7 && ind >= 0) {
                drawStack(canvas, stack);
            }

            if(g.finalConditions())
                doStart();



        }



        private void drawDown(Canvas canvas)
        {
            int left, top, right, bottom;


            // this will reset the deckIndex to 0. This whole setup allows us to have no face-up
            // cards animate when the deck is clicked. We will stop animating cards since the
            // deckIndex will be -1. In nextCard(), when the index is -1 we simply reset the
            // deckIndex to 0, while returning null.


            int drawDeckCards = 0;

            if(g.sizeDownList() > 3)
                drawDeckCards = 3;
            else
                drawDeckCards = g.sizeDownList();

            // draw the deck
            for (int i = 0 ; i < drawDeckCards ; i++)
            {// if the the last card in the deck has been pulled, don't animate the deck
                left = deckCanvasOffsetX + deckOffsetX*i;
                top = deckCanvasOffsetY + deckOffsetY*i;
                right = left + cardWidth;
                bottom = top + cardHeight;

                Drawable d = ctx.getResources().getDrawable(R.drawable.b1fv);
                d.setBounds(left, top, right, bottom);
                d.draw(canvas);
            }
        }

        private void drawUp(Canvas canvas)
        {
            int left, top, right, bottom;


            // this will reset the deckIndex to 0. This whole setup allows us to have no face-up
            // cards animate when the deck is clicked. We will stop animating cards since the
            // deckIndex will be -1. In nextCard(), when the index is -1 we simply reset the
            // deckIndex to 0, while returning null.


            int size = g.sizeUpList();
            // animate the face-up cards
            if (size > 0) {
                for (int i = 0; i < size - numUpCards; ++i) {
                    left = g.getDrawList(i).getX();
                    right = left + cardWidth;
                    top = g.getDrawList(i).getY();
                    bottom = top + cardHeight;
                    // draw the next card
                    Drawable d = ctx.getResources().getDrawable(cardFaces.get(g.getDrawList(i).getGraInd()));
                    d.setBounds(left, top, right, bottom);
                    d.draw(canvas);
                }
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
         * Snap cards back to their original position if they aren't moving to another stack.
         */
        private void snapBack() {

            //
            for (int z = ind; z < g.sizeDeck(stack); z++) {
                g.getDeck(stack, z).addX((int)  (headingX * .2));
                g.getDeck(stack, z).addY((int)  (headingY * .2));
            }


            if ((Math.abs(g.getDeck(stack, ind).getX() - initialX) < 10) &&
                    (Math.abs(g.getDeck(stack, ind).getY() - initialY) < 10)) {
                for (int z = ind; z < g.sizeDeck(stack); z++) {
                    g.getDeck(stack, z).setX(initialX);
                    g.getDeck(stack, z).setY(initialY + (z - ind) * cardOffsetY);
                }

                if(recipientStack != -1 && stack != -1)
                    g.moveCard(stack, ind, recipientStack);

                if(recipientStack > -1 && stack == 11)
                    numUpCards--;
                deckMove = false;
                snapBack = false;
                stack = -1;
                recipientStack = -1;
                ind = -1;
                setUpLocations();
            }
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
                    case MotionEvent.ACTION_DOWN:
                    {
                        final int pointerIndex = MotionEventCompat.getActionIndex(e);
                        final int x = (int) MotionEventCompat.getX(e, pointerIndex);
                        final int y = (int) MotionEventCompat.getY(e, pointerIndex);

                        tapX = x;
                        tapY = y;

                        mLastTouchX = x;
                        mLastTouchY = y;

                        //minimize this, have only one function that handles card touches
                        //either you click on a card or on nothing, also stack should
                        //correspond to all situations when you would be picking up a card
                        cardTouch(x, y);


                        //get initial X and Y
                        if (stack > -1 && stack < 12) {
                            initialX = g.getDeck(stack, ind).getX();
                            initialY = g.getDeck(stack, ind).getY();
                        }
                        mActivePointerId = MotionEventCompat.getPointerId(e, 0);
                        break;
                    }
                    case MotionEvent.ACTION_MOVE:
                    {
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

                        //Clean up action up area
                        cardTouchRecipient(x, y);


                        //auto complete if user does not drag card
                        if (Math.abs(x - tapX) < 10 && Math.abs(y - tapY) < 10)
                        {

                            int i = 7;
                            while(true)
                            {
                                if((stack > 10) || stack == -1)
                                    break;
                                else if (g.canMoveCard(stack, ind, i))
                                {
                                    setInitXY(i);
                                    break;
                                }
                                i++;
                                i = i % 11;
                                if(i == 6)
                                {
                                    stack = -1;
                                    break;
                                }
                            }
                        }
                        // use recipientStack as the test to see if our card is hovering over
                        // another card.
                        else if (recipientStack != -1 && recipientStack < 12 && g.canMoveCard(stack, ind, recipientStack))
                        {
                            setInitXY(recipientStack);
                        }

                        if (stack != -1 && stack < 12)
                        {
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


        public void setInitXY(int dest)
        {
            if (dest < 7) {
                initialX = stackX[dest];
                initialY = stackY + g.sizeDeck(dest) * cardOffsetY;
            }
            else {

                initialX = fourX[dest - 7];
                initialY = fourY;
            }
        }



        /**
         * method returns what card was touched by index for the seven stacks, or -1 if no card
         * was touched
         *
         * @param x
         * @param y
         * @return
         */
        public void cardTouch(double x, double y) {
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
                return;
            }

            check = checkFour(x,y);
            if (check > -1) {
                stack = check;
                ind = g.sizeFourList(stack) - 1;
            }

            check = checkDown(x, y);
            if (check > -1) {
                stack = check;
                ind = g.sizeDownList() - 1;

            }

            check = checkUp(x, y);
            if (check > -1) {
                stack = check;
                ind = g.sizeUpList() - 1;

            }



        }


        private void cardTouchRecipient(int x, int y) {
            recipientStack = -1;
            int check = checkSeven(x, y);
            if (check > -1)
                recipientStack = check;

            check = checkFour(x, y);
            if (check > -1)
                recipientStack = check;
        }


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
}

