package dataaccess;

import dataaccess.exceptions.AlreadyTakenException;
import model.GameData;
import requestobjects.CreateRequest;
import requestobjects.JoinRequest;
import chess.ChessGame;

import java.util.List;

public abstract class GameDao extends Dao
{
    abstract public int create(CreateRequest request) throws DataAccessException;
    abstract public GameData getGame(int gameID) throws DataAccessException;
    abstract public List<GameData> list() throws DataAccessException;
    abstract public void join(JoinRequest request, String username) throws AlreadyTakenException, DataAccessException;
    abstract public void clear() throws DataAccessException;
    abstract public void updateGame(int gameID, ChessGame updateGame) throws DataAccessException;
}