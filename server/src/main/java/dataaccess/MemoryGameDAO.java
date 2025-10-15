package dataaccess;
import model.GameData;
import java.util.*;

public class MemoryGameDAO implements GameDAO
{
    private final Map<Integer, GameData> games = new HashMap<>();
    private int nextID = 1;

    @Override
    public void clear()
    {
        games.clear();
        nextID = 1;
    }

    @Override
    public GameData createGame(GameData game)
    {
        var newGame = new GameData(nextID++, game.whiteUsername(), game.blackUsername(), game.gameName(), game.game());
        games.put(newGame.gameID(), newGame);
        return newGame;
    }

    @Override
    public GameData getGame(int gameID)
    {
        return games.get(gameID);
    }

    @Override
    public Collection<GameData> listGames()
    {
        return games.values();
    }

    @Override
    public void updateGame(GameData game)
    {
        games.put(game.gameID(), game);
    }
}