package dataaccess.memory;

import dataaccess.DataAccessException;
import dataaccess.UserDAO;
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
    public Optional<UserData> findByUsername(String username)
    {
        return Optional.ofNullable(users.get(username));
    }

    @Override
    public boolean validateCredentials(String username, String password)
    {
        return findByUsername(username)
                .map(u -> Objects.equals(u.password(), password))
                .orElse(false);
    }

    @Override
    public void clear()
    {
        users.clear();
    }
}