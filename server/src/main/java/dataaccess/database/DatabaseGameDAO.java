package dataaccess.database;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.GameDAO;
import dataaccess.exceptions.AlreadyTakenException;
import dataaccess.exceptions.DataAccessException;
import model.GameData;
import model.PlayerColor;
import dataaccess.DatabaseManager;

import java.util.ArrayList;
import java.util.List;

public class DatabaseGameDAO extends AbstractDatabaseDAO implements GameDAO {

    private static final Gson GSON = new Gson();

    public DatabaseGameDAO() throws DataAccessException {
        configureDatabase();
    }

    @Override
    public int save(GameData game) throws DataAccessException {
        String json = GSON.toJson(game);
        return executeCommand("INSERT INTO games (name, whiteUsername, blackUsername, game) VALUES (?, ?, ?, ?)",
                game.gameName(), null, null, json);
    }

    @Override
    public GameData findById(int gameId) throws DataAccessException {
        return executeQuery("SELECT * FROM games WHERE idgames = ?", rs -> new GameData(
                rs.getInt("idgames"),
                rs.getString("whiteUsername"),
                rs.getString("blackUsername"),
                rs.getString("name"),
                GSON.fromJson(rs.getString("game"), ChessGame.class)
        ), gameId);
    }

    @Override
    public List<GameData> findAll() throws DataAccessException {
        List<GameData> games = new ArrayList<>();
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement("SELECT * FROM games");
             var rs = ps.executeQuery()) {
            while (rs.next()) {
                games.add(new GameData(
                        rs.getInt("idgames"),
                        rs.getString("whiteUsername"),
                        rs.getString("blackUsername"),
                        rs.getString("name"),
                        GSON.fromJson(rs.getString("game"), ChessGame.class)
                ));
            }
        } catch (Exception e) {
            throw new DataAccessException("Failed to retrieve games", e);
        }
        return games;
    }

    @Override
    public void assignPlayer(int gameId, String username, PlayerColor color)
            throws AlreadyTakenException, DataAccessException {

        String sql;
        if (color == PlayerColor.WHITE) {
            sql = "UPDATE games SET whiteUsername = ? WHERE idgames = ? AND (whiteUsername IS NULL OR whiteUsername = '' OR whiteUsername = 'null')";
        } else if (color == PlayerColor.BLACK) {
            sql = "UPDATE games SET blackUsername = ? WHERE idgames = ? AND (blackUsername IS NULL OR blackUsername = '' OR blackUsername = 'null')";
        } else {
            throw new DataAccessException("Invalid player color");
        }

        int rows = executeCommand(sql, username, gameId);
        if (rows == 0)
        {
            throw new AlreadyTakenException("Error: " + color + " player already assigned");
        }
    }

    // ✅ NEW METHOD (CRITICAL FOR GAMEPLAY)
    @Override
    public void update(GameData game) throws DataAccessException {
        String json = GSON.toJson(game.game());

        int rows = executeCommand(
                "UPDATE games SET whiteUsername = ?, blackUsername = ?, game = ? WHERE idgames = ?",
                game.whiteUsername(),
                game.blackUsername(),
                json,
                game.gameID()
        );

        if (rows == 0) {
            throw new DataAccessException("Error: Failed to update game");
        }
    }

    @Override
    public void clear() throws DataAccessException {
        executeCommand("TRUNCATE TABLE games");
    }

    @Override
    protected String[] getCreateStatements() {
        return new String[]{
                """
            CREATE TABLE IF NOT EXISTS `games` (
                `idgames` INT NOT NULL AUTO_INCREMENT,
                `name` VARCHAR(45) NOT NULL,
                `whiteUsername` VARCHAR(45) NULL,
                `blackUsername` VARCHAR(45) NULL,
                `game` JSON NOT NULL,
                PRIMARY KEY (`idgames`),
                UNIQUE INDEX `idgames_UNIQUE` (`idgames` ASC) VISIBLE);
            """
        };
    }
}