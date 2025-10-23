package dataaccess.local;

import dataaccess.UserDao;
import model.UserData;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LocalUserDao implements UserDao
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