package com.example.tkkemo.myapplication;

import java.util.ArrayList;
import java.util.LinkedList;

public class Game {


    private LinkedList<Card> orderedList;


    //0-6 are the main game stacks
    //7-10 are the final stacks/ace stacks
    //11 is the draw stack
    //12 is the down stack
    private ArrayList<LinkedList<Card>> deck;
    private final int[] dealIndices = {0, 1, 2, 3, 4, 5, 6, 1, 2, 3, 4, 5, 6, 2,
            3, 4, 5, 6, 3, 4, 5, 6, 4, 5, 6, 5, 6, 6};




    public Game()
    {
        orderedList = new LinkedList<Card>();
        deck = new ArrayList<LinkedList<Card>>();

        // suits and values to generate each card
        final String[] suitImmutable = {"S", "H", "C", "D",};
        final int[] valuesImmutable = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
                11, 12, 13};

        String color;
        for(int i = 0 ; i < 4 ; i++)
            for(int j = 0 ; j < 13 ; j++)
            {
                color = (i % 2) == 0 ?  "B": "R";
                orderedList.add(new Card(color, suitImmutable[i], valuesImmutable[j],
                        color + suitImmutable[i] + valuesImmutable[j], 0, 0, i*13+j));
            }

        for (int i = 0 ; i < 13; i++) deck.add(new LinkedList<Card>());


        shuffle();
        deal();
    }

    /**
     * Shuffle the deck. Shuffling is separate from deck compilation
     * since people might want to shuffle multiple times using th
     * shuffle button.
     */
    public void shuffle()
    {
        java.util.Random randomGen;

        randomGen = new java.util.Random();
        // allow people to shuffle the deck multiple times before dealing
        LinkedList<Card> preShuffleDeck = deck.get(12).size() == 0 ? orderedList : DownList();

        // shuffle the deck
        for (int x = 0 ; x < 52 ; x++)
        { addDownList(preShuffleDeck.remove(randomGen.nextInt(preShuffleDeck.size()))); }

        // The deck is now randomized and can be dealt or shuffled again if desired.
    }

    public Card pollDownList() {
        return deck.get(12).poll();
    }

    public LinkedList<Card> DownList() {
        return deck.get(12);
    }

    public void addDownList(Card card) {
        deck.get(12).add(card);
    }

    public Card getDrawList(int ind) { return deck.get(11).get(ind); }

    public Card getDeck(int list, int ind) { return deck.get(list).get(ind); }

    // used only when undoing a move
    public Card unconditionalRemove(int list, int ind) { return deck.get(list).remove(ind); }

    public int sizeDeck(int list) {
        return deck.get(list).size();
    }


    public void addSevenList(int list, Card card) {
        if (list > 6 || list < 0) { throw new IndexOutOfBoundsException(); }
        deck.get(list).add(card);
    }

    public Card getLastSevenList(int list) {
        if (list > 6 || list < 0) { throw new IndexOutOfBoundsException(); }
        return deck.get(list).getLast();
    }

    public Card getSevenList(int list, int ind) {
        if (list > 6 || list < 0) { throw new IndexOutOfBoundsException(); }
        return deck.get(list).get(ind);
    }

    public int sizeSevenList(int list) {
        if (list > 6 || list < 0) { throw new IndexOutOfBoundsException(); }
        return deck.get(list).size();
    }

    public Card removeSevenList(int list, int ind) {
        if (list > 6 || list < 0) { throw new IndexOutOfBoundsException(); }
        return deck.get(list).remove(ind);
    }

    public Card removeFourList(int list, int ind) {
        if (list > 10 || list < 7) { throw new IndexOutOfBoundsException(); }
        return deck.get(list).remove(ind);
    }

    public int sizeFourList(int list ) {
        if (list > 10 || list < 7) { throw new IndexOutOfBoundsException(); }
        return deck.get(list).size();
    }

    public int sizeDownList() {
        return deck.get(12).size();
    }

    public int sizeUpList() {
        return deck.get(11).size();
    }


    /**
     * Populates the board.
     */
    public void deal()
    {
        for (int r = 0 ; r < 28 ; r++)
        { addSevenList(dealIndices[r], pollDownList()); }

        // flip the last card in each List
        for (int f = 0 ; f < 7 ; f ++)
            deck.get(f).getLast().flip();
    }

    /**
     * Pulls the next card in the deck. We might need to modify this in the graphical version.
     * We need to check for nulls.
     */
    public void nextCard()
    {
        if (deck.get(12).size() > 0) {
            deck.get(12).getLast().changeFace();
            deck.get(11).add(deck.get(12).removeLast());
        }
        else {
            int loop = deck.get(11).size();
            for (int i = 0; i < loop; i++) {
                deck.get(11).getLast().changeFace();
                deck.get(12).add(deck.get(11).removeLast());
            }
        }
    }

    /**
     * Tests to see if the current face-up deck card can move to a given stack.
     * @param to Stack that the deck card is attempting to move to
     * @return True if the suit is different any the moving card's value is one
     * less than that of the recipient card.
     */
//    public boolean canMoveCard(int to)
//    {
//
//        Card movingCard = deck.get(11).getLast();
//        Card recipientCard = (sizeDeck(to) != 0) ? deck.get(to).getLast() : null;
//
//        if (to < 7)
//        {
//            if(recipientCard == null && ) {
//
//            }
//            if(!recipientCard.isFaceUp()) return false;
//            else if (!movingCard.getColor().equals(recipientCard.getColor()) &&
//                    (movingCard.getValue()+1 == recipientCard.getValue()))
//                return true;
//        }
//        else if (to < 10 && to > 6)
//        {
//            if(movingCard.getSuit().equals(recipientCard.getSuit()) &&
//                    movingCard.getValue()-1 == recipientCard.getValue())
//                return true;
//        }
//
//        return false;
//    }

    /**
     * Unconditionally move a card when undo is set in GameView.
     * @param from
     * @param fromIndex
     * @param to
     */
    public void unconditionalUndoMove(int from, int fromIndex, int to)
    {
        while(fromIndex < (deck.get(from).size()))
            deck.get(to).add(unconditionalRemove(from, fromIndex));
    }

    /**
     * It is important to remember that during console testing you need to use zero indexing
     * so everything isn't off by one.
     */
    public void moveCard(int from, int fromIndex, int to)
    {
        if(from == 11) { // if we are moving from face-up deck
            if (canMoveCard(from, fromIndex, to)) {
                deck.get(to).add(deck.get(11).remove(deck.get(11).size() - 1));
            }
        }
        else
        {

            if(canMoveCard(from, fromIndex, to))
            {
                // if the from list is greater than 6, remove from the four lists; otherwise, remove from seven lists
                while(fromIndex < (deck.get(from).size()))
                    deck.get(to).add((from > 6) ? removeFourList(from, fromIndex) : removeSevenList(from, fromIndex));


                /* if the list you pulled from still has a card on it, flip that card. (If it is already flipped
		        it won't hurt to do it again.) */
                if(!deck.get(from).isEmpty())
                    deck.get(from).getLast().flip();
            }
        }
    }


    /**
     * Determine if the card selected can be moved to the list specified.
     * @param from
     * @param fromIndex
     * @param to
     * @return
     */
    public boolean canMoveCard(int from, int fromIndex, int to)
    {	// get the cards being moved & being moved to
        if(to > 6 && to < 11 && ((deck.get(from).size()-1) == fromIndex))
        {
            Card movingCard = deck.get(from).get(fromIndex);
            Card recipientCard = deck.get(to).size() == 0 ? null : deck.get(to).getLast();

            if(recipientCard == null && movingCard.getValue() == 1)
                return true;
            else if (recipientCard != null && movingCard.getSuit().equals(recipientCard.getSuit()) &&
                    movingCard.getValue() - 1 == recipientCard.getValue())
                return true;
        }
        else if (to < 7){
            Card movingCard = deck.get(from).get(fromIndex);
            Card recipientCard = (sizeSevenList(to) != 0) ? getLastSevenList(to) : null;

            // if face down, return false. Otherwise, if color is different and the moving
            // card's value is one less than the recipient card then return true.
            // If the stack you are moving to is empty and the card that is moving is a King,
            // return true.
            if(recipientCard == null)
            {
                if (movingCard.isFaceUp() && (movingCard.getValue() == 13))
                    return true;
            }
            else
            {
                if (!movingCard.isFaceUp() || !recipientCard.isFaceUp()) return false;
                else if ((!movingCard.getColor().equals(recipientCard.getColor()) &&
                        (movingCard.getValue()+1) == recipientCard.getValue()))
                    return true;
            }
        }

        return false;
    }

    /**
     * Check to see if each of the final four decks has all its cards. If so, return
     * true and end the game.
     * @return
     */
    public boolean finalConditions()
    {
        for(int i = 0 ; i < 4 ; i++)
            if(sizeFourList(7+i) != 13)
                return false;

        return true;
    }
}


/** This is the console-tester code.
 *
 *
 public void drawBoard()
 {
 System.out.println("\n\n");
 String deckLine = "", finalFourLine = "      ", finalSevenLine = "  ",
 line = "\n\n", faceDownCard = " .*******. ", newLineStagger = "";

 // draw deck
 if(deckIndex < (deck.size()-1))
 deckLine = faceDownCard + deck.get(deckIndex).toString() + "    ";
 else if (deckIndex == (deck.size()-1))
 deckLine = "    " + deck.get(deckIndex).toString() + "    ";

 // draw final four piles
 for (int q = 0 ; q < 4 ; q++)
 if (!fourLists.get(q).isEmpty())
 finalFourLine += " " + fourLists.get(q).getLast().toString();
 else
 finalFourLine += "  ";

 // The rest of the code in this method will draw the seven main piles.

 int tallestStackSize = sevenLists.get(0).size(); // assign the first stack's size to start
 for (int i = 1 ; i < 7 ; i++)
 if (sevenLists.get(i).size() > tallestStackSize)
 tallestStackSize = sevenLists.get(i).size();

 // s is the index of the List, a is the list. We are traversing across and then down
 for (int s = 0 ; s < tallestStackSize+1 ; s++)
 {
 for (int a = 0 ; a < 7; a++)
 {
 if((sevenLists.get(a).size() > s) && (!sevenLists.get(a).get(s).isFaceUp()))
 line += faceDownCard;
 else if (((sevenLists.get(a).size()) > s) && (sevenLists.get(a).get(s).isFaceUp()))
 line += " " + sevenLists.get(a).get(s).toString() + "  "; // print the card info
 else if (((sevenLists.get(a).size()) <= s))
 line += "     ";

 }
 // compile the final String for the seven Stacks.

 newLineStagger += "      ";
 finalSevenLine += line + "\n" + newLineStagger;
 line = "";
 }

 // combine the Strings from deck, four, and seven Lists and print it
 System.out.println(deckLine + finalFourLine + "\n\n" + finalSevenLine);
 } */