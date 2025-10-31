package dataaccess.database;

import dataaccess.AuthDao;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;

public class DatabaseAuthDao extends AuthDao
{
    public void addAuthToken(String username, String token) throws DataAccessException {
        long count = DatabaseManager.getCount("SELECT COUNT(*) FROM authdata WHERE token = ?", token);
        if (count > 0) throw new DataAccessException("Token already exists: " + token);

        String sql = "INSERT INTO authdata (username, token) VALUES(?, ?)";
        this.executeCommand(sql, username, token);
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