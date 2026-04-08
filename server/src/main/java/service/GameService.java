package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.exceptions.*;
import model.GameData;
import model.PlayerColor;
import requests.CreateRequest;
import requests.JoinRequest;
import chess.ChessGame;
import chess.ChessMove;
import exception.InvalidMoveException;

import java.util.List;

public class GameService {

    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    // -------------------------------------------------------
    // LIST GAMES
    // -------------------------------------------------------
    public List<GameData> list(String token) throws InvalidCredentialsException, DataAccessException {
        authenticate(token);
        return gameDAO.findAll();
    }

    // -------------------------------------------------------
    // CREATE GAME
    // -------------------------------------------------------
    public int create(String token, CreateRequest request)
            throws InvalidCredentialsException, MissingFieldException, DataAccessException {

        authenticate(token);

        if (request == null || request.gameName() == null || request.gameName().isBlank()) {
            throw new MissingFieldException("Error: Game name is required");
        }

        GameData game = new GameData(
                0,
                null,
                null,
                request.gameName(),
                new ChessGame(),
                false
        );

        return gameDAO.save(game);
    }

    // -------------------------------------------------------
    // JOIN GAME
    // -------------------------------------------------------
    public void join(String token, JoinRequest request)
            throws InvalidCredentialsException,
            MissingFieldException,
            GameNotFoundException,
            AlreadyTakenException,
            DataAccessException {

        String username = authenticate(token);

        if (request == null) {
            throw new MissingFieldException("Error: Join requires a game ID");
        }

        if (request.gameID() <= 0) {
            throw new GameNotFoundException("Error: Game ID must be positive");
        }

        GameData game = gameDAO.findById(request.gameID());
        if (game == null) {
            throw new GameNotFoundException("Error: Game not found");
        }

        PlayerColor color = parseColor(request.playerColor());

        gameDAO.assignPlayer(request.gameID(), username, color);
    }

    // -------------------------------------------------------
    // MAKE MOVE (NEW - PHASE 6)
    // -------------------------------------------------------
    public GameData makeMove(String token, int gameID, ChessMove move)
            throws InvalidCredentialsException,
            GameNotFoundException,
            DataAccessException,
            InvalidMoveException {

        String username = authenticate(token);

        GameData gameData = gameDAO.findById(gameID);
        if (gameData == null) {
            throw new GameNotFoundException("Error: Game not found");
        }

        ChessGame game = gameData.game();
        if (game == null) {
            throw new InvalidMoveException("Error: Game state missing");
        }

        // Determine player color
        PlayerColor playerColor = null;
        if (username.equals(gameData.whiteUsername())) {
            playerColor = PlayerColor.WHITE;
        } else if (username.equals(gameData.blackUsername())) {
            playerColor = PlayerColor.BLACK;
        } else {
            throw new InvalidMoveException("Error: Observers cannot make moves");
        }

        // Check turn
        ChessGame.TeamColor expectedTurn = ChessGame.TeamColor.valueOf(playerColor.name());
        if (!game.getTeamTurn().equals(expectedTurn))
        {
            throw new InvalidMoveException("Error: Not your turn");
        }

        // Attempt move (ChessGame should validate legality)
        try {
            System.out.println("[game-service] before move " + move);
            game.makeMove(move);
            System.out.println("[game-service] after move turn=" + game.getTeamTurn());
        } catch (Exception e) {
            System.out.println("[game-service] move rejected: " + e.getMessage());
            throw new InvalidMoveException("Error: Invalid move");
        }

        // Save updated game
        GameData updated = new GameData(
                gameData.gameID(),
                gameData.whiteUsername(),
                gameData.blackUsername(),
                gameData.gameName(),
                game,
                gameData.gameOver()
        );

        gameDAO.update(updated);
        System.out.println("[game-service] persisted move for game " + gameID);

        return updated;
    }

    public void resign(String token, int gameID)
            throws InvalidCredentialsException,
            GameNotFoundException,
            DataAccessException,
            InvalidMoveException {

        String username = authenticate(token);

        GameData gameData = gameDAO.findById(gameID);
        if (gameData == null) {
            throw new GameNotFoundException("Error: Game not found");
        }

        if (gameData.gameOver()) {
            throw new InvalidMoveException("Error: Game already over");
        }

        boolean isWhite = username.equals(gameData.whiteUsername());
        boolean isBlack = username.equals(gameData.blackUsername());

        if (!isWhite && !isBlack) {
            throw new InvalidMoveException("Error: Observers cannot resign");
        }

        GameData updated = new GameData(
                gameData.gameID(),
                gameData.whiteUsername(),
                gameData.blackUsername(),
                gameData.gameName(),
                gameData.game(),
                true // gameOver = true
        );

        gameDAO.update(updated);
    }

    // -------------------------------------------------------
    // HELPER METHODS
    // -------------------------------------------------------
    private String authenticate(String token) throws InvalidCredentialsException, DataAccessException {
        String username = authDAO.findUsernameByToken(token);
        if (username == null) {
            throw new InvalidCredentialsException("Error: User not found.");
        }
        return username;
    }

    private PlayerColor parseColor(String raw) throws MissingFieldException {
        if (raw == null || raw.isBlank()) {
            throw new MissingFieldException("Error: Color is required");
        }

        if (raw.equalsIgnoreCase("WHITE")) {
            return PlayerColor.WHITE;
        }

        if (raw.equalsIgnoreCase("BLACK")) {
            return PlayerColor.BLACK;
        }

        throw new MissingFieldException("Error: Invalid Color");
    }
}
