package dataaccess;

import dataaccess.exceptions.AlreadyTakenException;
import dataaccess.exceptions.DataAccessException;
import model.GameData;
import model.PlayerColor;

import java.util.List;
import java.util.Optional;

public interface GameDAO
{
    GameData save(GameData game) throws DataAccessException;
    Optional<GameData> findById(int gameId);
    List<GameData> findAll();
    void assignPlayer(int gameId, String username, PlayerColor color)
            throws AlreadyTakenException, DataAccessException;
    void clear();
}