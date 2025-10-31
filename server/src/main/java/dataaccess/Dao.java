package dataaccess;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import static dataaccess.DatabaseManager.loadPropertiesFromResources;

public class Dao {

    // Static block runs once when the class is loaded
    static {
        loadPropertiesFromResources();
        try {
            DatabaseManager dbManager = new DatabaseManager();
            dbManager.configureDatabase();
        } catch (Exception e) {
            throw new RuntimeException("Failed to configure database", e);
        }
    }

    /**
     * Execute an SQL command like INSERT, UPDATE, DELETE
     * @param command SQL string with placeholders
     * @param params parameters to fill in placeholders
     * @return generated key if present, otherwise 0
     * @throws DataAccessException if SQL fails
     */
    public int executeCommand(String command, Object... params) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(command, java.sql.Statement.RETURN_GENERATED_KEYS)) {

            for (int i = 0; i < params.length; i++) {
                preparedStatement.setObject(i + 1, params[i]);
            }
            preparedStatement.executeUpdate();

            try (var generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1); // assuming ID is integer primary key
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to execute command", ex);
        }
        return 0;
    }

    /**
     * Functional interface to map a ResultSet to a single object
     */
    @FunctionalInterface
    public interface ResultMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    /**
     * Execute a query expected to return a single row
     */
    public <T> T executeQueryAndGetOne(String query, ResultMapper<T> resultMap, Object... params) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(query)) {

            for (int i = 0; i < params.length; i++) {
                preparedStatement.setObject(i + 1, params[i]);
            }

            try (var results = preparedStatement.executeQuery()) {
                if (results.next()) {
                    return resultMap.map(results);
                } else {
                    return null;
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to execute query", ex);
        }
    }

    /**
     * Return all integer IDs from a query
     */
    public ArrayList<Integer> getAllIds(String query, String column) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(query)) {

            try (var results = preparedStatement.executeQuery()) {
                ArrayList<Integer> ids = new ArrayList<>();
                while (results.next()) {
                    ids.add(results.getInt(column));
                }
                return ids;
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }
}
