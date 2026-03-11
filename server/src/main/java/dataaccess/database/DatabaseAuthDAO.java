package dataaccess.database;

import dataaccess.AuthDAO;
import dataaccess.DatabaseManager;
import dataaccess.exceptions.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static dataaccess.DatabaseManager.getConnection;
import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class DatabaseAuthDAO implements AuthDAO
{
    @Override
    public void clear() throws DataAccessException
    {
        String sqlCommand = "TRUNCATE TABLE authdata";
        executeCommand(sqlCommand);
    }

    @Override
    public void addToken(String username, String token) throws DataAccessException
    {
        long count = getCount("SELECT COUNT(*) FROM authdata WHERE token = ?", token);
        if(count > 0)
        {
            throw new DataAccessException("Token already exists");
        }
        executeCommand("INSERT INTO authdata (username, token) VALUES(?, ?)", username, token);
    }

    @Override
    public String findUsernameByToken(String token) throws DataAccessException
    {
        return executeQuery("SELECT username FROM authdata WHERE token = ?", results -> results.getString("username"), token);
    }

    @Override
    public void removeToken(String token) throws DataAccessException
    {
        executeCommand("DELETE FROM authdata WHERE token = ?", token);
    }

    private int executeCommand(String statement, Object... params) throws DataAccessException
    {
        try (Connection conn = getConnection())
        {
            try (PreparedStatement ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS))
            {
                for (int i = 0; i < params.length; i++)
                {
                    ps.setObject(i + 1, params[i]);
                }
                ps.executeUpdate();

                try (var generatedKeys = ps.getGeneratedKeys())
                {
                    if (generatedKeys.next())
                    {
                        return generatedKeys.getInt(1); // assuming ID is integer primary key
                    }
                }
            }
        } catch (SQLException e)
        {
            throw new DataAccessException("Unable to update database", e);
        }
        return 0;
    }

    private final String[] createStatements =
            {
                    """
            CREATE TABLE IF NOT EXISTS `authdata` (
              `idauthData` INT NOT NULL AUTO_INCREMENT,
              `userName` VARCHAR(100) NOT NULL,
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

    private void configureDatabase() throws DataAccessException
    {
        DatabaseManager.createDatabase();
        try (Connection conn = getConnection())
        {
            for (String statement : createStatements)
            {
                try (var preparedStatement = conn.prepareStatement(statement))
                {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException ex)
        {
            throw new DataAccessException("Could not create database");
        }
    }

    public <T> T executeQuery(String query, DatabaseGameDAO.ResultMapper<T> resultMap, Object... params) throws DataAccessException
    {
        try (var conn = getConnection();
             var preparedStatement = conn.prepareStatement(query))
        {

            for (int i = 0; i < params.length; i++)
            {
                preparedStatement.setObject(i + 1, params[i]);
            }

            try (var results = preparedStatement.executeQuery())
            {
                if (results.next())
                {
                    return resultMap.map(results);
                } else
                {
                    return null;
                }
            }
        } catch (SQLException ex)
        {
            throw new DataAccessException("Failed to execute query", ex);
        }
    }

    @FunctionalInterface
    public interface ResultMapper<T>
    {
        T map(ResultSet rs) throws SQLException;
    }

    public static long getCount(String sql, String param) throws DataAccessException
    {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            stmt.setString(1, param);  // bind parameter
            try (ResultSet rs = stmt.executeQuery())
            {
                if (rs.next())
                {
                    return rs.getLong(1);  // first column of first row
                } else
                {
                    return 0; // no rows returned
                }
            }
        }
        catch (SQLException ex)
        {
            throw new DataAccessException("Error executing getCount query", ex);
        }
    }
}
