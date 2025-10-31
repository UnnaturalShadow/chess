package dataaccess.database;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.GameDao;
import dataaccess.exceptions.AlreadyTakenException;
import model.GameData;
import requestobjects.CreateRequest;
import requestobjects.JoinRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DatabaseGameDao extends GameDao
{
    Gson serializer = new Gson();

    public int create(CreateRequest request) throws DataAccessException {
        if (request == null) {
            throw new DataAccessException("CreateRequest cannot be null");
        }

        String serializedGame = serializer.toJson(new ChessGame());
        String sqlStatement = "INSERT INTO games (name, whiteUsername, blackUsername, game) VALUES (?, ?, ?, ?)";
        return executeCommand(sqlStatement, request.gameName(), null, null, serializedGame);
    }


    public GameData getGame(int gameID) throws DataAccessException
    {
        String sqlStatement = "SELECT * FROM games WHERE idgames = ?";
        return executeQueryAndGetOne(sqlStatement, results -> new GameData(
                results.getInt("idgames"),
                results.getString("name"),
                results.getString("whiteUsername"),
                results.getString("blackUsername"),
                serializer.fromJson(String.valueOf(results.getString("game")), ChessGame.class)
        ), gameID);
    }

    public List<GameData> list() throws DataAccessException
    {
        List<GameData> games = new ArrayList<>();
        List<Integer> gameIds = getAllIds("SELECT idgames FROM games", "idgames");

        for (int id: gameIds)
        {
            games.add(getGame(id));
        }

        return games;
    }

    public void clear() throws DataAccessException
    {
        String sqlStatement = "TRUNCATE TABLE games";
        executeCommand(sqlStatement);
    }

    public void join(JoinRequest request, String username) throws AlreadyTakenException, DataAccessException
    {
        GameData gameData = getGame(request.gameID());
        String sqlStatement;

        if(Objects.equals(request.playerColor(), "WHITE") && gameData.whiteUsername() == null)
        {
            sqlStatement = "UPDATE games SET whiteUsername = ? WHERE idgames = ?";
        }
        else if (Objects.equals(request.playerColor(), "BLACK") && gameData.blackUsername() == null)
        {
            sqlStatement = "UPDATE games SET blackUsername = ? WHERE idgames = ?";
        }
        else
        {
            throw new AlreadyTakenException("Color already taken");
        }

        executeCommand(sqlStatement, username, request.gameID());
    }
}