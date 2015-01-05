package com.example.tkkemo.myapplication;

/**
 * Created by BWash on 10/3/14.
 */
public class Card {

    private boolean faceUp;
    private String color, suit, graphic;
    private int value, x, y, graphicIndex;


    public Card(String Color, String Suit, int Value, String Graphic, int x, int y, int graphicIndex)
    {
        faceUp = false;
        this.color = Color;
        this.suit = Suit;
        this.value = Value;
        this.graphic = Graphic;
        this.x = x;
        this.y = y;
        this.graphicIndex = graphicIndex;
    }

    /**
     * Toggles the variable faceUp to true.
     */
    public void show()
    { this.faceUp = true; }

    public int getX()
    { return x; }

    public int getY()
    { return y; }


    public void setX(int x)
    { this.x = x; }

    public int getGraInd() {
        return graphicIndex;
    }

    public void addX(int n) {
        x += n;
    }

    public void addY(int n) {
        y += n;
    }

    public void setY(int y)
    { this.y = y; }
    /**
     * Turn the card face up once it has been dealt.
     */
    public void flip()
    { faceUp = true; }

    /**
     * @return provide value of the faceUp variable.
     */
    public boolean isFaceUp()
    { return faceUp; }

    public void changeFace()
    {
        faceUp = !faceUp;
    }

    public String getColor()
    { return color; }

    public String getSuit()
    { return suit; }

    public int getValue()
    { return value;}

    @Override
    public String toString()
    { return color + "" + suit + "" + value; }

    /**
     * Tests the equality of two Card objects. If the color, suit
     * and value are equivalent then these objects are the same..
     * @param c
     * @return
     */
    public boolean equals(Card c)
    {
        // if the color, suit and value are equivalent then
        // these objects are the same.
        return (c.color.equals(color) && c.suit.equals(suit) &&
                c.value == value) ? true : false;
    }
}
