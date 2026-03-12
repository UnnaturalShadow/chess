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

    private static final Gson gson = new Gson();

    public DatabaseGameDAO() throws DataAccessException {
        configureDatabase();
    }

    // -------------------------------------------------------
    // SAVE
    // -------------------------------------------------------
    @Override
    public int save(GameData game) throws DataAccessException {
        String sqlCommand = "INSERT INTO games (name, whiteUsername, blackUsername, game) VALUES (?, ?, ?, ?)";
        String json = gson.toJson(game);
        // Ensure null for usernames initially
        return executeCommand(sqlCommand, game.gameName(), null, null, json);
    }

    // -------------------------------------------------------
    // FIND BY ID
    // -------------------------------------------------------
    @Override
    public GameData findById(int gameId) throws DataAccessException {
        String sqlCommand = "SELECT * FROM games WHERE idgames = ?";
        return executeQueryAndGetOne(sqlCommand, results -> new GameData(
                results.getInt("idgames"),                // gameID
                results.getString("whiteUsername"),       // whiteUsername
                results.getString("blackUsername"),       // blackUsername
                results.getString("name"),                // gameName
                gson.fromJson(results.getString("game"), ChessGame.class) // game
        ), gameId);
    }

    // -------------------------------------------------------
    // FIND ALL
    // -------------------------------------------------------
    @Override
    public List<GameData> findAll() throws DataAccessException {
        List<GameData> games = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM games");
             ResultSet results = ps.executeQuery()) {

            while (results.next()) {
                games.add(new GameData(
                        results.getInt("idgames"),
                        results.getString("whiteUsername"),
                        results.getString("blackUsername"),
                        results.getString("name"),
                        gson.fromJson(results.getString("game"), ChessGame.class)
                ));
            }

        } catch (SQLException e) {
            throw new DataAccessException("Failed to retrieve games", e);
        }

        return games;
    }

    // -------------------------------------------------------
    // ASSIGN PLAYER
    // -------------------------------------------------------
    @Override
    public void assignPlayer(int gameId, String username, PlayerColor color)
            throws AlreadyTakenException, DataAccessException {

        String sqlCommand;

        if (color == PlayerColor.WHITE) {
            sqlCommand = "UPDATE games SET whiteUsername = ? WHERE idgames = ? AND (whiteUsername IS NULL OR whiteUsername = '' OR whiteUsername = 'null')";
        } else if (color == PlayerColor.BLACK) {
            sqlCommand = "UPDATE games SET blackUsername = ? WHERE idgames = ? AND (blackUsername IS NULL OR blackUsername = '' OR blackUsername = 'null')";
        } else {
            throw new DataAccessException("Invalid player color");
        }

        int rowsAffected = executeCommand(sqlCommand, username, gameId);
        if (rowsAffected == 0) {
            throw new AlreadyTakenException("Error: " + color + " player already assigned");
        }
    }

    // -------------------------------------------------------
    // CLEAR TABLE
    // -------------------------------------------------------
    @Override
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
            int rowsUpdated = ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }

            return rowsUpdated; // fallback for updates without generated keys
        } catch (SQLException e) {
            throw new DataAccessException("Unable to execute database command", e);
        }
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