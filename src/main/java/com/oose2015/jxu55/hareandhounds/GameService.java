package com.oose2015.jxu55.hareandhounds;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// import org.sql2o.Connection;
import org.sql2o.Sql2o;
// import org.sql2o.Sql2oException;
import javax.sql.DataSource;
import java.awt.*;
import java.awt.geom.Point2D;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.time.LocalDateTime;


public class GameService {

    private Sql2o db;

    private int year, month, day, hour, minute, second, millis;
    private static int gameIdNumber = 1;
    private List<Game> games = new ArrayList<>();

    private final Logger logger = LoggerFactory.getLogger(GameService.class);

    // some strings that will be used
    private final String HOUND = "HOUND";
    private final String HARE = "HARE";
    private final String WAITING_FOR_SECOND_PLAYER = "WAITING_FOR_SECOND_PLAYER";
    private final String TURN_HARE = "TURN_HARE";
    private final String TURN_HOUND = "TURN_HOUND";
    private final String WIN_HARE_BY_ESCAPE = "WIN_HARE_BY_ESCAPE";
    private final String WIN_HARE_BY_STALLING = "WIN_HARE_BY_STALLING";
    private final String WIN_HOUND = "WIN_HOUND";

    // three trapped conditions as coordinates mapped to number strings
    private final String TRAPPED_1 = "1362";
    private final String TRAPPED_2 = "37118";
    private final String TRAPPED_3 = "691110";
    /**
     * Construct the model with a pre-defined datasource. The current implementation
     * also ensures that the DB schema is created if necessary.
     *
     * @param dataSource
     */
    public GameService(DataSource dataSource) throws GameServiceException {
        // create a new database for games
        // but I couldn't think of a good way to store all the game info
        // so this database is empty
        db = new Sql2o(dataSource);

        //Create the schema for the database if necessary. This allows this
        //program to mostly self-contained. But this is not always what you want;
        //sometimes you want to create the schema externally via a script.
        /*try (Connection conn = db.open()) {
            String sql = "CREATE TABLE IF NOT EXISTS item (item_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "                                 title TEXT, done BOOLEAN, created_on TIMESTAMP)" ;
            conn.createQuery(sql).executeUpdate();
        } catch(Sql2oException ex) {
            logger.error("Failed to create schema at startup", ex);
            throw new GameServiceException("Failed to create schema at startup", ex);
        }*/
    }

    /**
     * get the current time in the format yyyy-mm-dd hh:mm:ss.millis
     * @return
     */
    public String getTime(){
        LocalDateTime now = LocalDateTime.now();
        year = now.getYear();
        month = now.getMonthValue();
        day = now.getDayOfMonth();
        hour = now.getHour();
        minute = now.getMinute();
        second = now.getSecond();
        millis = now.get(ChronoField.MILLI_OF_SECOND);
        return year + "-" + month + "-" +  day + " " + hour + ":" +  minute + ":" +  second + "."  + millis;
    }

    /**
     * create a game with a piece type chosen by the starting player
     * the game id is set to be the creation time along with a unique serial number
     * @param body
     * @return
     * @throws GameServiceException
     */
    public GameWrapper createAGame(String body) throws GameServiceException {
        // the purpose of the gameIdNumber is to prevent games that are created at the same time from having same game ids
        Game game = new Game(getTime() + " serial: " + gameIdNumber, body.contains(HOUND) ? HOUND : HARE);
        games.add(game);
        gameIdNumber++;
        GameWrapper gameWrapper = new GameWrapper(game.getGameId(), game.getStartingPlayerId(), game.getStartingPlayerPieceType());
        return gameWrapper;
    }

    /**
     * search for a particular game using a game id
     * return the game if it exists in the server; null if not
     * @param gameId
     * @return
     */
    public Game searchGame(String gameId){
        for (Game entry : games) {
            if (entry.getGameId().equals(gameId)) {
                return entry;
            }
        }
        return null;
    }

    /**
     * check if this is a particular  player's turn
     * return true if it is; false if not
     * @param gameId
     * @param playerId
     * @return
     */
    public boolean isCorrectTurn(String gameId, String playerId) {
        Game game = searchGame(gameId);
        if (game.getPlayerPieceType(playerId).equals(HOUND) && game.getGameState().equals(TURN_HOUND)) {
            return true;
        }
        if (game.getPlayerPieceType(playerId).equals(HARE) && game.getGameState().equals(TURN_HARE)) {
            return true;
        }
        return false;
    }

    /**
     * check if a move is legal for various conditions
     * return true if legal; false if not
     * @param gameId
     * @param playerId
     * @param fromX
     * @param fromY
     * @param toX
     * @param toY
     * @return
     */
    public boolean isLegalMove(String gameId, String playerId, int fromX, int fromY, int toX, int toY) {

        int xDiff = Math.abs(fromX - toX);
        int yDiff = Math.abs(fromY - toY);

        Game game = searchGame(gameId);

        // our game board is a rectangle turned in to a hexagon
        // we cant move to any of the 4 vertices of the original rectangle because then don't exist any more
        List<Point2D> forbiddenBoardPositions= game.getForbiddenBoardPositions();
        Point2D dest = new Point(toX, toY);
        for (Point2D entry: forbiddenBoardPositions) {
            if (entry.equals(dest)) {
                return false;
            }
        }

        //convert the move into a 4 character string;
        String move = "" + fromX + fromY + toX + toY;

        // certain diagonal moves are prohibited according to the design of the board
        String[] forbiddenMoves = game.getForbiddenMoves();
        for (String entry : forbiddenMoves) {
            if (entry.equals(move)) {
                return false;
            }
        }

        // check if all of the x, y values are in their corresponding ranges
        if (toX >= 0 && toX < 5 && toY >= 0 && toY < 3) {
            // we can't move to an occupied position
            if (game.getPieceByCoordinates(toX, toY) != null) {
                return false;
            }
            // hound move
            if (game.getPlayerPieceType(playerId).equals(HOUND)) {
                // make sure both the x difference and y difference are not greater than 1
                if (xDiff <= 1 && yDiff <= 1 ) {
                    // hounds are not allowed to move to the left of the board
                    if (toX - fromX < 0) {
                        return false;
                    }
                    return true;
                }
                return false;
            }
            // hare move
            if (game.getPlayerPieceType(playerId).equals(HARE))  {
                // make sure both the x difference and y difference are not greater than 1
                if (game.getPieceByCoordinates(toX, toY) == null && xDiff <= 1 && yDiff <= 1 ) {
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    /**
     * move a piece on the board if game isn't end
     * also switch between TURN_HOUND and TURN_HARE
     * @param gameId
     * @param fromX
     * @param fromY
     * @param toX
     * @param toY
     */
    public void moveAPiece(String gameId, int fromX, int fromY, int toX, int toY) {
        Game game = searchGame(gameId);
        Piece pieceToBeMoved = game.getPieceByCoordinates(fromX, fromY);
        pieceToBeMoved.changeX(toX);
        pieceToBeMoved.changeY(toY);

        // store the mapped hound positions in an integer array
        int[] houndPositions = new int[3];
        houndPositions[0] = game.mapCoordinatesToNumber(game.getHound1().getCoordinatesString());
        houndPositions[1] = game.mapCoordinatesToNumber(game.getHound2().getCoordinatesString());
        houndPositions[2] = game.mapCoordinatesToNumber(game.getHound3().getCoordinatesString());

        // sort the integer array to avoid repetition, i.e. the three hounds are identical except for their positions on the board
        Arrays.sort(houndPositions);

        // summarize the current board position as a string
        String boardPosition = "" + houndPositions[0] + houndPositions[1] + houndPositions[2] + game.mapCoordinatesToNumber(game.getHare().getCoordinatesString());

        // update the board position tracker
        // the board position tracker will change the game state to "WIN_HARE_BY_STALLING" if a board position has occurred three times
        game.updateBoardPositionTracker(boardPosition);

        // update the the current board position
        game.changeCurrentBoardPosition(boardPosition);

        // if game isn't over, switch sides
        if (!isGameOver(gameId)) {
            if (game.getPieceByCoordinates(toX, toY).getPieceType().equals(HOUND)) {
                game.changeGameState(TURN_HARE);
            }
            if (game.getPieceByCoordinates(toX, toY).getPieceType().equals(HARE)) {
                game.changeGameState(TURN_HOUND);
            }
        }
    }

    /**
     * check the three game over conditions
     * return true if game is over; false if not
     * @param gameId
     * @return
     */
    public boolean isGameOver(String gameId) {
        Game game = searchGame(gameId);

        // check the hound stalling condition
        // the same board position occurs three times over the course of the game, the hounds are stalling and hare wins
        if (game.getGameState().equals(WIN_HARE_BY_STALLING)) {
            System.out.println("win hare by stalling");
            return true;
        }

        // check the hare escape condition
        // the hare manages to sneak past the hounds, the hare wins
        if (game.getHare().getX() <= game.getHound1().getX() && game.getHare().getX() <= game.getHound2().getX() && game.getHare().getX() <= game.getHound3().getX()) {
            game.changeGameState(WIN_HARE_BY_ESCAPE);
            System.out.println("win hare by escape");
            return true;
        }

        // check the hare trapped condition
        // the hare is trapped such that it has no valid move, the hounds win
        if (game.getGameState().equals(TURN_HOUND)) {
            // only three board positions fit the hare trapped case
            // so we check if the current board position is any of the three
            if (game.getCurrentBoardPosition().equals(TRAPPED_1) || game.getCurrentBoardPosition().equals(TRAPPED_2) || game.getCurrentBoardPosition().equals(TRAPPED_3)) {
                game.changeGameState(WIN_HOUND);
                System.out.println("win hound");
                return true;
            }
        }

        System.out.println(game.getGameState() + " finished, current board position:" + game.getCurrentBoardPosition() + ".");
        System.out.println("game continues");

        return false;
    }

    //-----------------------------------------------------------------------------//
    // Helper Classes and Methods
    //-----------------------------------------------------------------------------//

    public static class GameServiceException extends Exception {
        public GameServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

