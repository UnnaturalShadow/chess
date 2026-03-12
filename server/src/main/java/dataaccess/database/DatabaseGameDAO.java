package dataaccess.database;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.DatabaseManager;
import dataaccess.GameDAO;
import dataaccess.exceptions.AlreadyTakenException;
import dataaccess.exceptions.DataAccessException;
import model.GameData;
import model.PlayerColor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class DatabaseGameDAO implements GameDAO {

    private static final Gson serializer = new Gson();

    public DatabaseGameDAO() throws DataAccessException {
        configureDatabase();
    }

    // -------------------------------------------------------
    // SAVE
    // -------------------------------------------------------
    public int save(GameData game) throws DataAccessException {
        String sqlCommand = "INSERT INTO games (name, whiteUsername, blackUsername, game) VALUES (?, ?, ?, ?)";
        String json = serializer.toJson(game);
        return executeCommand(sqlCommand, game.gameName(), null, null, json);
    }

    // -------------------------------------------------------
    // FIND BY ID
    // -------------------------------------------------------
    public GameData findById(int gameId) throws DataAccessException {
        String sqlCommand = "SELECT * FROM games WHERE idgames = ?";
        return executeQueryAndGetOne(sqlCommand, results -> new GameData(
                results.getInt("idgames"),
                results.getString("name"),
                results.getString("whiteUsername"),
                results.getString("blackUsername"),
                serializer.fromJson(results.getString("game"), ChessGame.class)
        ), gameId);
    }

    // -------------------------------------------------------
    // FIND ALL
    // -------------------------------------------------------
    public List<GameData> findAll() throws DataAccessException {
        List<Integer> gameIds = new ArrayList<>();
        executeQueryAndGetOne(
                "SELECT idgames FROM games",
                results -> {
                    do {
                        gameIds.add(results.getInt("idgames"));
                    } while (results.next());
                    return null;
                }
        );

        List<GameData> games = new ArrayList<>();
        for (int id : gameIds) {
            games.add(findById(id));
        }
        return games;
    }

    // -------------------------------------------------------
    // ASSIGN PLAYER
    // -------------------------------------------------------
    public void assignPlayer(int gameId, String username, PlayerColor color)
            throws AlreadyTakenException, DataAccessException {

        GameData game = findById(gameId);
        if (game == null) {
            throw new DataAccessException("Game not found");
        }

        String sqlCommand;
        switch (color) {
            case WHITE:
                if (game.whiteUsername() == null) {
                    sqlCommand = "UPDATE games SET whiteUsername = ? WHERE idgames = ?";
                    executeCommand(sqlCommand, username, gameId);
                    return;
                }
                break;
            case BLACK:
                if (game.blackUsername() == null) {
                    sqlCommand = "UPDATE games SET blackUsername = ? WHERE idgames = ?";
                    executeCommand(sqlCommand, username, gameId);
                    return;
                }
                break;
        }
        throw new AlreadyTakenException("Color already taken");
    }

    // -------------------------------------------------------
    // CLEAR TABLE
    // -------------------------------------------------------
    public void clear() throws DataAccessException {
        String sqlCommand = "TRUNCATE TABLE games";
        executeCommand(sqlCommand);
    }

    // -------------------------------------------------------
    // EXECUTE COMMAND
    // -------------------------------------------------------
    private int executeCommand(String statement, Object... params) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {

            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to update database", e);
        }
        return 0;
    }

    // -------------------------------------------------------
    // DATABASE CREATION
    // -------------------------------------------------------
    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS `authdata` (
              `idauthData` INT NOT NULL AUTO_INCREMENT,
              `username` VARCHAR(100) NOT NULL,
              `token` VARCHAR(100) NOT NULL,
              PRIMARY KEY (`idauthData`),
              UNIQUE INDEX `idauthData_UNIQUE` (`idauthData` ASC) VISIBLE);
            """,
            """
            CREATE TABLE IF NOT EXISTS `games` (
                `idgames` INT NOT NULL AUTO_INCREMENT,
                `name` VARCHAR(45) NOT NULL,
                `whiteUsername` VARCHAR(45) NULL,
                `blackUsername` VARCHAR(45) NULL,
                `game` JSON NOT NULL,
                PRIMARY KEY (`idgames`),
                UNIQUE INDEX `idgames_UNIQUE` (`idgames` ASC) VISIBLE);
            """,
            """
            CREATE TABLE IF NOT EXISTS `users` (
              `idusers` INT NOT NULL AUTO_INCREMENT,
              `username` VARCHAR(45) NOT NULL,
              `password` VARCHAR(255) NOT NULL,
              `email` VARCHAR(45) NOT NULL,
              PRIMARY KEY (`idusers`),
              UNIQUE INDEX `idusers_UNIQUE` (`idusers` ASC) VISIBLE,
              UNIQUE INDEX `username_UNIQUE` (`username` ASC) VISIBLE);
            """
    };

    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (Connection conn = DatabaseManager.getConnection()) {
            for (String statement : createStatements) {
                try (PreparedStatement ps = conn.prepareStatement(statement)) {
                    ps.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not create database", ex);
        }
    }

    // -------------------------------------------------------
    // EXECUTE QUERY
    // -------------------------------------------------------
    public <T> T executeQueryAndGetOne(String query, ResultMapper<T> resultMap, Object... params) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }

            try (ResultSet results = ps.executeQuery()) {
                if (results.next()) {
                    return resultMap.map(results);
                }
                return null;
            }

        } catch (SQLException ex) {
            throw new DataAccessException("Failed to execute query", ex);
        }
    }

    @FunctionalInterface
    public interface ResultMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }
}