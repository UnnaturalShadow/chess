package dataaccess;

import dataaccess.exceptions.AlreadyTakenException;
import model.GameData;
import requestobjects.CreateRequest;
import requestobjects.JoinRequest;

import java.util.List;

public interface GameDao extends Dao
{
    int create(CreateRequest request) throws DataAccessException;
    GameData getGame(int gameID);
    List<GameData> list();
    void join(JoinRequest request, String username) throws AlreadyTakenException;
    void clear();
}