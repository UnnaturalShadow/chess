package dataaccess.database;

import dataaccess.AuthDao;
import dataaccess.DataAccessException;

public class DatabaseAuthDao extends AuthDao
{
    public void addAuthToken(String username, String token) throws DataAccessException
    {
        String sqlStatement = "INSERT INTO authdata (username, token) VALUES(?, ?)";
        this.executeCommand(sqlStatement, username, token);
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