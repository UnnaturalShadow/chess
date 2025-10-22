package dataaccess.local;

import dataaccess.UserDAO;
import model.UserData;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LocalUserDAO implements UserDAO
{
    public Map<String, UserData> users = new HashMap<>();

    @Override
    public void createUser(UserData userData)
    {
        users.put(userData.username(), userData);
    }

    @Override
    public UserData getUser(String username)
    {
        return users.get(username);
    }

    @Override
    public boolean validateWithPassword(String username, String password)
    {
        return users.containsKey(username) && Objects.equals(users.get(username).password(), password);
    }

    @Override
    public void clear()
    {
        users = new HashMap<>();
    }
}