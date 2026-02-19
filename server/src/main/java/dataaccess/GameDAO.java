package dataaccess;

import model.GameData;
import requests.CreateRequest;
import requests.JoinRequest;

import java.util.List;

public interface GameDAO
{
    int createGame(CreateRequest request) throws DataAccessException;
    GameData getGame(int gameID) throws DataAccessException;
    List<GameData> listGames();
    void joinGame(JoinRequest request, String username) throws AlreadyTakenException;
    void clear();
}
