package dataaccess.memory;

import chess.ChessGame;
import dataaccess.AlreadyTakenException;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.GameData;
import requests.CreateRequest;
import requests.JoinRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MemoryGameDAO implements GameDAO
{
    private int currID = 1;
    public Map<Integer, GameData> gameList = new HashMap<>();

    @Override
    public int createGame(CreateRequest request) throws DataAccessException
    {
        try
        {
            GameData game = new GameData(currID, request.gameName(), null, null, new ChessGame());
            gameList.put(currID, game);
            currID++;
            return currID-1;
        }
        catch (Exception e)
        {
            throw new DataAccessException("Could not create game", e);
        }
    }

    @Override
    public GameData getGame(int gameID)
    {
        return gameList.get(gameID);
    }

    @Override
    public void clear()
    {
        gameList = new HashMap<>();
    }

    @Override
    public void joinGame(JoinRequest request, String username) throws AlreadyTakenException
    {
        GameData gameData = getGame(request.gameID());
        GameData newGame;

        if(gameData != null)
        {
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
            gameList.put(request.gameID(), newGame);
        }
        throw new AlreadyTakenException("Invalid game ID");

    }

    @Override
    public List<GameData> listGames()
    {
        return List.of();
    }
}
