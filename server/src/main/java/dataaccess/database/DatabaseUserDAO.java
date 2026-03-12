package dataaccess.database;

import dataaccess.DatabaseManager;
import dataaccess.UserDAO;
import dataaccess.exceptions.DataAccessException;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import static dataaccess.DatabaseManager.getConnection;
import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class DatabaseUserDAO implements UserDAO
{

    public DatabaseUserDAO() throws DataAccessException
    {
        configureDatabase();
    }
    @Override
    public void clear() throws DataAccessException
    {
        String sqlCommand = "TRUNCATE TABLE users";
        executeCommand((sqlCommand));

    }

    public void save(UserData user) throws DataAccessException
    {
        String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());
        String sqlCommand = "INSERT INTO users (username, password, email) VALUES(?,?,?)";
        executeCommand(sqlCommand, user.username(), hashedPassword, user.email());
    }

    public UserData findByUsername(String username) throws DataAccessException
    {

        String sqlCommand = "SELECT username, password, email FROM users WHERE username = ?";
        return executeQuery(sqlCommand, results -> new UserData(
                results.getString("username"),
                results.getString("password"),
                results.getString("email")
        ), username);
    }

    @Override
    public boolean validateCredentials(String username, String password) throws DataAccessException
    {
        String sqlCommand = "SELECT password FROM users WHERE username = ?";
        String dbPassword = executeQuery(sqlCommand, rs -> rs.getString("password"), username);
        return dbPassword != null && BCrypt.checkpw(password, dbPassword);
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

    public <T> T executeQuery(String query, ResultMapper<T> resultMap, Object... params) throws DataAccessException
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
