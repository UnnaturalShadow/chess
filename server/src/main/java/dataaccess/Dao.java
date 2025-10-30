package dataaccess;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import static dataaccess.DatabaseManager.loadPropertiesFromResources;

public class Dao
{
    static
    {
        loadPropertiesFromResources();
    }


    public int executeCommand(String command, Object... params) throws DataAccessException
    {
        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(command, java.sql.Statement.RETURN_GENERATED_KEYS))
        {
            for (int i = 0; i < params.length; i++)
            {
                preparedStatement.setObject(i + 1, params[i]);
            }
            preparedStatement.executeUpdate();

            try (var generatedKeys = preparedStatement.getGeneratedKeys())
            {
                if (generatedKeys.next())
                {
                    return generatedKeys.getInt(1); // assuming your ID is an integer primary key
                }
            }
        }
        catch (SQLException ex)
        {
            throw new DataAccessException("failed to create execute command", ex);
        }
        return 0;
    }

    @FunctionalInterface
    public interface ResultMapper<T>
    {
        T map(ResultSet rs) throws SQLException;
    }

    public <T> T executeQueryAndGetOne(String query, ResultMapper<T> resultMap, Object... params) throws DataAccessException
    {
        try (var conn = DatabaseManager.getConnection();
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
        }
        catch (SQLException ex)
        {
            throw new DataAccessException("failed to execute command", ex);
        }
    }

    public ArrayList<Integer> getAllIds(String query, String column) throws DataAccessException
    {
        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(query))
        {

            try (var results = preparedStatement.executeQuery())
            {
                ArrayList<Integer> ids = new ArrayList<>();
                while (results.next())
                {
                    ids.add(results.getInt(column));
                }
                return ids;
            }

        }
        catch (SQLException e)
        {
            throw new DataAccessException(e.getMessage());
        }
    }
}