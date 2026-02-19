package dataaccess.memory;

import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.UserData;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MemoryUserDAO implements UserDAO
{
    public Map<String, UserData> userList = new HashMap<>();

    @Override
    public void createUser(UserData userData)
    {
        userList.put(userData.username(), userData);
    }

    @Override
    public UserData getUser(String username) throws DataAccessException
    {
        return userList.get(username);
    }

    @Override
    public boolean validate(String username, String password)
    {
        return userList.containsKey(username) && Objects.equals(userList.get(username).password(), password);
    }

    @Override
    public void clear()
    {
        userList = new HashMap<>();
    }
}
