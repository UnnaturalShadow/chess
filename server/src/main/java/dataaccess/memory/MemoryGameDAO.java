package dataaccess.memory;

import dataaccess.exceptions.AlreadyTakenException;
import dataaccess.GameDAO;
import dataaccess.exceptions.DataAccessException;
import model.GameData;
import model.PlayerColor;

import java.util.*;

public class MemoryGameDAO implements GameDAO
{

    private int nextId = 1;
    private final Map<Integer, GameData> games = new HashMap<>();

    @Override
    public int save(GameData game)
    {
        int id = game.gameID() <= 0 ? nextId++ : game.gameID();
        GameData newGame = new GameData(id, game.whiteUsername(),
                game.blackUsername(), game.gameName(), game.game());
        games.put(id, newGame);
        return newGame.gameID();
    }

    @Override
    public GameData findById(int gameId)
    {
        return (games.get(gameId));
    }

    @Override
    public List<GameData> findAll()
    {
        return new ArrayList<>(games.values());
    }

    @Override
    public void assignPlayer(int gameId, String username, PlayerColor color)
            throws AlreadyTakenException {

        GameData game = findById(gameId);

        // Observer case
        if (color == null) {
            return; // joining as observer, no change to white/black
        }

        GameData updated;

        switch (color) {
            case WHITE -> {
                if (game.whiteUsername() != null)
                {
                    throw new AlreadyTakenException("Error: White already taken");
                }

                updated = new GameData(
                        game.gameID(),
                        username,
                        game.blackUsername(),
                        game.gameName(),
                        game.game()
                );
            }

            case BLACK -> {
                if (game.blackUsername() != null)
                {
                    throw new AlreadyTakenException("Error: Black already taken");
                }

                updated = new GameData(
                        game.gameID(),
                        game.whiteUsername(),
                        username,
                        game.gameName(),
                        game.game()
                );
            }

            default -> throw new IllegalStateException("Unexpected playerColor: " + color);
        }

        games.put(gameId, updated);
    }

    @Override
    public void update(GameData game) throws DataAccessException
    {
        return;
    }

    @Override
    public void clear()
    {
        games.clear();
    }
}