package dataaccess;

import dataaccess.exceptions.AlreadyTakenException;
import dataaccess.exceptions.DataAccessException;
import model.GameData;
import model.PlayerColor;

import java.util.List;

public interface GameDAO
{
    int save(GameData game) throws DataAccessException;

    GameData findById(int gameId) throws DataAccessException;

    List<GameData> findAll() throws DataAccessException;

    void assignPlayer(int gameId, String username, PlayerColor color)
            throws AlreadyTakenException, DataAccessException;

    // ✅ NEW METHOD (REQUIRED FOR PHASE 6)
    void update(GameData game) throws DataAccessException;

    void clear() throws DataAccessException;
}