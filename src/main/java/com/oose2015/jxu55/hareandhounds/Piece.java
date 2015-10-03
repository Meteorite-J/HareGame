package com.oose2015.jxu55.hareandhounds;

// class for pieces on the board
public class Piece {
    private String pieceType;
    private int x, y;
    public Piece (String pieceType, int x, int y){
        this.pieceType = pieceType;
        this.x = x;
        this.y = y;
    }

    public String getPieceType() {
        return pieceType;
    }

    public void changeX(int x) {
        this.x = x;
    }

    public void changeY(int y) {
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    /**
     * get the coordinates as a 2 character string
     * @return
     */
    public String getCoordinatesString(){
        return "" + x + y;
    }

}

