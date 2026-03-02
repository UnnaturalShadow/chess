package dataaccess.memory;

import dataaccess.exceptions.AlreadyTakenException;
import dataaccess.exceptions.DataAccessException;
import dataaccess.GameDAO;
import model.GameData;
import model.PlayerColor;

import java.util.*;

public class MemoryGameDAO implements GameDAO
{

    private int nextId = 1;
    private final Map<Integer, GameData> games = new HashMap<>();

    @Override
    public GameData save(GameData game) throws DataAccessException
    {
        int id = game.gameID() <= 0 ? nextId++ : game.gameID();
        GameData newGame = new GameData(id, game.gameName(),
                game.whiteUsername(), game.blackUsername(), game.game());
        games.put(id, newGame);
        return newGame;
    }

    @Override
    public Optional<GameData> findById(int gameId)
    {
        return Optional.ofNullable(games.get(gameId));
    }

    @Override
    public List<GameData> findAll()
    {
        return new ArrayList<>(games.values());
    }

    @Override
    public void assignPlayer(int gameId, String username, PlayerColor color)
            throws AlreadyTakenException, DataAccessException
    {

        GameData game = findById(gameId)
                .orElseThrow(() -> new DataAccessException("Invalid game ID"));

        GameData updated;
        switch (color)
        {
            case WHITE ->
            {
                if (game.whiteUsername() != null)
                    throw new AlreadyTakenException("White already taken");
                updated = new GameData(game.gameID(), game.gameName(),
                        username, game.blackUsername(), game.game());
            }
            case BLACK ->
            {
                if (game.blackUsername() != null)
                    throw new AlreadyTakenException("Black already taken");
                updated = new GameData(game.gameID(), game.gameName(),
                        game.whiteUsername(), username, game.game());
            }
            default -> throw new IllegalStateException("Unexpected color: " + color);
        }
        games.put(gameId, updated);
    }

    @Override
    public void clear()
    {
        games.clear();
    }
}