package dataaccess.memory;

import chess.ChessGame;
import dataaccess.GameDao;
import dataaccess.exceptions.AlreadyTakenException;
import model.GameData;
import requestobjects.CreateRequest;
import requestobjects.JoinRequest;
import dataaccess.DataAccessException;

import java.util.*;

public class MemoryGameDao extends GameDao
{
    public Map<Integer, GameData> games = new HashMap<>();
    private int nextId = 1;

    public int create(CreateRequest request)
    {
        GameData newGame = new GameData(
                nextId, request.gameName(), null, null, new ChessGame()
        );
        games.put(nextId, newGame);

        nextId++;
        return nextId-1;
    }

    public GameData getGame(int gameID)
    {
        return games.get(gameID);
    }

    public void updateGame(int gameID, ChessGame updateGame) throws DataAccessException
    {
    }

    public List<GameData> list()
    {
        return new ArrayList<>(games.values());
    }

    public void clear()
    {
        games = new HashMap<>();
    }

    public void join(JoinRequest request, String username) throws AlreadyTakenException
    {
        GameData gameData = games.get(request.gameID());
        GameData newGame;

        if(Objects.equals(request.playerColor(), "WHITE") && gameData.whiteUsername() == null)
        {
            newGame = new GameData(
                    gameData.gameID(), gameData.gameName(), username, gameData.blackUsername(), gameData.game()
            );
        }
        else if (Objects.equals(request.playerColor(), "BLACK") && gameData.blackUsername() == null)
        {
            newGame = new GameData(
                    gameData.gameID(), gameData.gameName(), gameData.whiteUsername(), username, gameData.game()
            );
        }
        else
        {
            throw new AlreadyTakenException("Color already taken");
        }
        games.put(request.gameID(), newGame);
    }
}