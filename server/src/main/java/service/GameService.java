package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
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
    public List<GameData> list(String token) throws DataAccessException
    {
        authenticate(token);
        return gameDAO.findAll();
    }

    // Create a new game
    public int create(String token, CreateRequest request) throws DataAccessException
    {
        authenticate(token);

        if (request == null || request.gameName() == null || request.gameName().isBlank())
        {
            throw new DataAccessException("Game name is required");
        }

        // Build domain object directly
        GameData game = new GameData(0, request.gameName(), null, null, new ChessGame());
        return gameDAO.save(game).gameID();
    }

    // Join an existing game
    public void join(String token, JoinRequest request) throws DataAccessException
    {
        String username = authenticate(token);
        PlayerColor color = validateJoinRequest(request);
        gameDAO.assignPlayer(request.gameID(), username, color);
    }

    // --- Helper methods ---

    // Authenticate token via AuthDAO
    private String authenticate(String token) throws DataAccessException
    {
        return authDAO.findUsernameByToken(token)
                .orElseThrow(() -> new DataAccessException("Authentication required"));
    }

    // Validate join request
    private PlayerColor validateJoinRequest(JoinRequest request) throws DataAccessException
    {
        if (request == null)
        {
            throw new DataAccessException("Join request cannot be null");
        }

        if (request.gameID() <= 0)
        {
            throw new DataAccessException("Game ID must be positive");
        }

        GameData game = gameDAO.findById(request.gameID())
                .orElseThrow(() -> new DataAccessException("Game does not exist"));

        PlayerColor color = parseColor(request.color());
        if (!VALID_COLORS.contains(color))
        {
            throw new DataAccessException("Invalid color");
        }

        return color;
    }

    // Convert string to PlayerColor enum
    private PlayerColor parseColor(String raw)
    {
        if (raw == null) return null;
        try
        {
            return PlayerColor.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e)
        {
            return null;
        }
    }
}