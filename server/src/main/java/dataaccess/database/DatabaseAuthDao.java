package dataaccess.database;

import dataaccess.AuthDao;
import dataaccess.DataAccessException;

public class DatabaseAuthDao extends AuthDao
{
    public void addAuthToken(String username, String token) throws DataAccessException {
        String sqlStatement = "INSERT INTO authdata (username, token) VALUES(?, ?)";
        try {
            this.executeCommand(sqlStatement, username, token);
        } catch (Exception e) { // catch whatever your executeCommand throws
            // If the exception indicates a duplicate key, wrap and throw DataAccessException
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("duplicate")) {
                throw new DataAccessException("Token already exists: " + token, e);
            }
            // Otherwise, rethrow as a generic DataAccessException
            throw new DataAccessException("Error adding auth token", e);
        }
    }

    public String authenticateToken(String token) throws DataAccessException
    {
        String sqlStatement = "SELECT username FROM authdata WHERE token = ?";
        return executeQueryAndGetOne(sqlStatement, results -> results.getString("username"), token);
    }

    public void clear() throws DataAccessException
    {
        String sqlStatement = "TRUNCATE TABLE authdata";
        executeCommand(sqlStatement);
    }

    public void remove(String token) throws DataAccessException
    {
        executeCommand("DELETE FROM authdata WHERE token = ?", token);
    }
}