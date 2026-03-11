package dataaccess;

import dataaccess.exceptions.AlreadyTakenException;
import dataaccess.exceptions.DataAccessException;
import model.GameData;
import model.PlayerColor;

import java.util.List;
import java.util.Optional;

public interface GameDAO
{
    int save(GameData game) throws DataAccessException;
    GameData findById(int gameId) throws DataAccessException;

    List<GameData> findAll() throws DataAccessException;
    void assignPlayer(int gameId, String username, PlayerColor color)
            throws AlreadyTakenException, DataAccessException;

    void clear() throws DataAccessException;
}