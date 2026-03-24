package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.exceptions.*;
import model.GameData;
import model.PlayerColor;
import requests.CreateRequest;
import requests.JoinRequest;
import chess.ChessGame;

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
                new ChessGame()
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

        // Assign player (DAO handles occupancy check)
        gameDAO.assignPlayer(request.gameID(), username, color);
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