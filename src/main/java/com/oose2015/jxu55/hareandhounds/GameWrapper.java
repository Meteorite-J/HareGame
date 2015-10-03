package com.oose2015.jxu55.hareandhounds;

// wraps game as a java object
public class GameWrapper {

    private String gameId, playerId, pieceType;

    public GameWrapper(String gameId, String playerId, String pieceType){
        this.gameId = gameId;
        this.playerId = playerId;
        this.pieceType = pieceType;
    }

    public String getGameId() {
        return gameId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getPieceType() {
        return pieceType;
    }

}
