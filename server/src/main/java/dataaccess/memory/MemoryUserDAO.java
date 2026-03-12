package dataaccess.memory;

import dataaccess.UserDAO;
import dataaccess.exceptions.DataAccessException;
import model.UserData;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class MemoryUserDAO implements UserDAO
{

    private final Map<String, UserData> users = new HashMap<>();

    @Override
    public void save(UserData user)
    {
        users.put(user.username(), user);
    }

    @Override
    public UserData findByUsername(String username)
    {
        return (users.get(username));
    }

    @Override
    public boolean validateCredentials(String username, String password) throws DataAccessException
    {
        UserData user = findByUsername(username);
        if (user != null)
        {
            return Objects.equals(user.password(), password);
        }
        throw new DataAccessException("Error: User does not exist");
    }

    @Override
    public void clear()
    {
        users.clear();
    }
}