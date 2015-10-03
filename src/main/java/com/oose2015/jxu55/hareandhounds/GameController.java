package com.oose2015.jxu55.hareandhounds;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.ArrayList;
import java.util.Collections;

import static spark.Spark.*;

public class GameController {

    private static final String API_CONTEXT = "/hareandhounds/api/games";

    private final GameService gameService;

    private final Logger logger = LoggerFactory.getLogger(GameController.class);

    public GameController(GameService gameService) {
        this.gameService = gameService;
        setupEndpoints();
    }

    private void setupEndpoints() {

        // start a game
        post(API_CONTEXT, "application/json", (request, response) -> {
            try {
                response.status(201);
                return gameService.createAGame(request.body());
            } catch (GameService.GameServiceException ex) {
                logger.error("Failed to create new game");
                System.out.println("Error occurred when starting game: failed to create new game!");
                return Collections.EMPTY_MAP;
            }
        }, new JsonTransformer());

        // join a game
        put(API_CONTEXT + "/:gameId", "application/json", (request, response) -> {
            String gameId = request.params(":gameId");
            Game game = gameService.searchGame(gameId);
            if (game == null) {
                response.status(404);
                logger.error("Invalid game id");
                System.out.println("Error occurred when joining game state: invalid game id!");
                return Collections.emptyMap();
            } else if (game.getGameState().equals("WAITING_FOR_SECOND_PLAYER")) {
                response.status(200);
                game.changeGameState("TURN_HOUND");
                return new GameWrapper(game.getGameId(), game.getJoinedPlayerId(), game.getJoinedPlayerPieceType());
            } else{
                response.status(410);
                logger.error("Second player already joined");
                System.out.println("Error occurred when joining game: second player already joined!");
                return Collections.emptyMap();
            }
        }, new JsonTransformer());

        // fetch game state
        get(API_CONTEXT + "/:gameId/state", "application/json", (request, response)-> {
            String gameId = request.params(":gameId");
            Game game = gameService.searchGame(gameId);
            if (game != null) {
                response.status(200);
                return new GameStateWrapper(game.getGameState());
            }
            response.status(404);
            logger.error("Invalid game id");
            System.out.println("Error occurred when fetching game state: invalid game id!");
            return Collections.emptyMap();
        }, new JsonTransformer());

        // fetch board
        get(API_CONTEXT + "/:gameId/board", "application/json", (request, response)-> {
            String gameId = request.params(":gameId");
            Game game = gameService.searchGame(gameId);
            if (game != null) {
                response.status(200);
                return game.getPieces();
            }
            response.status(404);
            logger.error("Invalid game id");
            System.out.println("Error occurred when fetching board: invalid game id!");
            return Collections.emptyMap();
        }, new JsonTransformer());

        // move a piece
        post(API_CONTEXT + "/:gameId/turns", "application/json", (request, response) -> {
            JsonElement content = new JsonParser().parse(request.body());
            String gameId = content.getAsJsonObject().get("gameId").getAsString();
            String playerId = content.getAsJsonObject().get("playerId").getAsString();
            int fromX = content.getAsJsonObject().get("fromX").getAsInt();
            int fromY = content.getAsJsonObject().get("fromY").getAsInt();
            int toX = content.getAsJsonObject().get("toX").getAsInt();
            int toY = content.getAsJsonObject().get("toY").getAsInt();
            Game game = gameService.searchGame(gameId);
            // check the four error conditions
            // perform the piece move if the request passed all the checks
            if (game == null) {
                response.status(404);
                logger.error("Invalid game id");
                System.out.println("Error occurred when moving a piece: invalid game id!");
                return new ReasonWrapper("INVALID_GAME_ID");
            }
            if (!playerId.equals("player 1") && !playerId.equals("player 2")) {
                response.status(404);
                logger.error("Invalid player id");
                System.out.println("Error occurred when moving a piece: invalid player id!");
                return new ReasonWrapper("INVALID_PLAYER_ID");
            }
            if (!gameService.isCorrectTurn(gameId, playerId)) {
                response.status(422);
                logger.error("Incorrect turn");
                System.out.println("Error occurred when moving a piece: incorrect turn!");
                return new ReasonWrapper("INCORRECT_TURN");
            }
            if (!gameService.isLegalMove(gameId, playerId, fromX, fromY, toX, toY)) {
                response.status(422);
                logger.error("Illegal move");
                System.out.println("Error occurred when moving a piece: illegal move!");
                return new ReasonWrapper("ILLEGAL_MOVE");
            }
            gameService.moveAPiece(gameId, fromX, fromY, toX, toY);
            response.status(200);
            return new PlayerIdWrapper(playerId);
        }, new JsonTransformer());
    }
}
