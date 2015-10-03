package com.oose2015.jxu55.hareandhounds;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class Game {

    private String gameId, startingPlayerPieceType, joinedPlayerPieceType, startingPlayerId, joinedPlayerId, gameState;
    private Piece hare, hound1, hound2, hound3;
    private List<Piece> pieces = new ArrayList<>();

    // forbidden board positions
    private final List<Point2D> FORBIDDEN_BOARD_POSITIONS = new ArrayList<>();

    // certain diagonal moves are forbidden on the board, store them as strings
    private final String[] FORBIDDEN_MOVES = new String[8];

    // to make everything simple, when tracking board position, we give each position a single number to replace the coordinates:
    // the board is represented as follows:
    // ------------------------------
    //        1     2     3
    //
    //  4     5     6     7     8
    //
    //        9     10    11
    // ------------------------------

    // maps the coordinates to their corresponding numbers according to the picture above
    private final Map<String, Integer> BOARD_POSITION_MAP = new HashMap<>();

    // tracks board appearance, key is 4 numbers concatenated as a string, value is the time that this string appears
    private Map<String, Integer> boardPositionTracker = new HashMap<>();

    // keeps track of the current board position as a string (same thing as the keys in boardPositionTracker)
    private String currentBoardPosition;

    public Game(String gameId, String startingPlayerPieceType){
        this.gameId = gameId;
        this.startingPlayerPieceType = startingPlayerPieceType;
        this.joinedPlayerPieceType = (startingPlayerPieceType == "HOUND" ? "HARE": "HOUND");
        this.startingPlayerId = "player 1";
        this.joinedPlayerId = "player 2";
        this.gameState = "WAITING_FOR_SECOND_PLAYER";
        hound1 = new Piece("HOUND", 0, 1);
        hound2 = new Piece("HOUND", 1, 0);
        hound3 = new Piece("HOUND", 1, 2);
        hare = new Piece("HARE", 4, 1);
        pieces.add(hound1);
        pieces.add(hound2);
        pieces.add(hound3);
        pieces.add(hare);
        FORBIDDEN_BOARD_POSITIONS.add(new Point(0, 0));
        FORBIDDEN_BOARD_POSITIONS.add(new Point(0, 2));
        FORBIDDEN_BOARD_POSITIONS.add(new Point(4, 0));
        FORBIDDEN_BOARD_POSITIONS.add(new Point(4, 2));
        FORBIDDEN_MOVES[0] = "1120";
        FORBIDDEN_MOVES[1] = "2011";
        FORBIDDEN_MOVES[2] = "1122";
        FORBIDDEN_MOVES[3] = "2211";
        FORBIDDEN_MOVES[4] = "2031";
        FORBIDDEN_MOVES[5] = "3120";
        FORBIDDEN_MOVES[6] = "2231";
        FORBIDDEN_MOVES[7] = "3122";
        BOARD_POSITION_MAP.put("10", 1);
        BOARD_POSITION_MAP.put("20", 2);
        BOARD_POSITION_MAP.put("30", 3);
        BOARD_POSITION_MAP.put("01", 4);
        BOARD_POSITION_MAP.put("11", 5);
        BOARD_POSITION_MAP.put("21", 6);
        BOARD_POSITION_MAP.put("31", 7);
        BOARD_POSITION_MAP.put("41", 8);
        BOARD_POSITION_MAP.put("12", 9);
        BOARD_POSITION_MAP.put("22", 10);
        BOARD_POSITION_MAP.put("32", 11);
        boardPositionTracker.put("1498", Integer.valueOf(0));
        currentBoardPosition = "1498";
    }

    public String getGameId() {
        return gameId;
    }

    public String getStartingPlayerPieceType() {
        return startingPlayerPieceType;
    }

    public String getJoinedPlayerPieceType() {
        return joinedPlayerPieceType;
    }

    public String getStartingPlayerId() {
        return startingPlayerId;
    }

    public String getJoinedPlayerId() {
        return joinedPlayerId;
    }

    public String getPlayerPieceType(String playerId) {
        return (playerId.equals("player 1")? getStartingPlayerPieceType() : getJoinedPlayerPieceType());
    }

    public String getGameState() {
        return gameState;
    }

    public void changeGameState(String gameState){
        this.gameState = gameState;
    }

    public Piece getHare() {
        return hare;
    }

    public Piece getHound1() {
        return hound1;
    }

    public Piece getHound2() {
        return hound2;
    }

    public Piece getHound3() {
        return hound3;
    }

    public List<Piece> getPieces() {
        return pieces;
    }

    public List<Point2D> getForbiddenBoardPositions() {
        return FORBIDDEN_BOARD_POSITIONS;
    }

    public String[] getForbiddenMoves() {
        return FORBIDDEN_MOVES;
    }

    public String getCurrentBoardPosition() {
        return currentBoardPosition;
    }

    public void changeCurrentBoardPosition(String boardPosition){
        currentBoardPosition = boardPosition;
    }

    public int mapCoordinatesToNumber(String key){
        return BOARD_POSITION_MAP.get(key);
    }

    public void updateBoardPositionTracker(String boardPosition) {
        if(boardPositionTracker.containsKey(boardPosition)){
            boardPositionTracker.put(boardPosition, boardPositionTracker.get(boardPosition) + 1);
            if(boardPositionTracker.get(boardPosition) == 3){
                gameState = "WIN_HARE_BY_STALLING";
            }
        } else {
            boardPositionTracker.put(boardPosition, 1);
        }
    }

    public Piece getPieceByCoordinates(int x, int y){
        for(Piece entry : pieces){
            if(entry.getX() == x && entry.getY() == y){
                return entry;
            }
        }
        return null;
    }
}
