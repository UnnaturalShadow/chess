package service;

import chess.InvalidMoveException;
import dataaccess.AuthDAO;
import dataaccess.exceptions.*;
import dataaccess.GameDAO;
import model.GameData;
import model.PlayerColor;
import requests.CreateRequest;
import requests.JoinRequest;
import chess.ChessGame;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class GameService
{

    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    private static final Set<PlayerColor> VALID_COLORS = EnumSet.of(PlayerColor.WHITE, PlayerColor.BLACK);

    public GameService(GameDAO gameDAO, AuthDAO authDAO)
    {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    // List all games
    public List<GameData> list(String token) throws InvalidCredentialsException
    {
        authenticate(token);
        return gameDAO.findAll();
    }

    // Create a new game
    public int create(String token, CreateRequest request) throws DataAccessException, MissingFieldException, InvalidCredentialsException
    {
        authenticate(token);

        if (request == null || request.gameName() == null || request.gameName().isBlank())
        {
            throw new MissingFieldException("Error: Game name is required");
        }

        // Build domain object directly
        GameData game = new GameData(0, request.gameName(), null, null, new ChessGame());
        return gameDAO.save(game).gameID();
    }

    // Join an existing game
    public void join(String token, JoinRequest request) throws DataAccessException, AlreadyTakenException,
            InvalidCredentialsException, GameNotFoundException
    {
        String username = authenticate(token);
        PlayerColor color = validateJoinRequest(request);
        gameDAO.assignPlayer(request.gameID(), username, color);
    }

    // --- Helper methods ---

    // Authenticate token via AuthDAO
    private String authenticate(String token) throws InvalidCredentialsException
    {
        return authDAO.findUsernameByToken(token)
                .orElseThrow(() -> new InvalidCredentialsException("Error: Authentication required"));
    }

    // Validate join request
    private PlayerColor validateJoinRequest(JoinRequest request) throws MissingFieldException, GameNotFoundException,
            DataAccessException, AlreadyTakenException
    {
        if (request == null)
        {
            throw new MissingFieldException("Error: Join request cannot be null");
        }

        if (request.gameID() <= 0)
        {
            throw new GameNotFoundException("Error: Game ID must be positive");
        }

        GameData game = gameDAO.findById(request.gameID())
                .orElseThrow(() -> new DataAccessException("Game does not exist"));

        PlayerColor color = parseColor(request.color());
        if (!VALID_COLORS.contains(color))
        {
            throw new AlreadyTakenException("Error: Invalid color");
        }

        return color;
    }

    // Convert string to PlayerColor enum
    private PlayerColor parseColor(String raw) throws DataAccessException
    {
        if (raw == null) return null;
        if(raw == "WHITE") return PlayerColor.WHITE;
        if(raw == "BLACK") return PlayerColor.BLACK;
        throw new DataAccessException("Error: Not a color.");
    }
}