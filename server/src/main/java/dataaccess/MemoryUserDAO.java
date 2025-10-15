package dataaccess;
import model.GameData;
import model.UserData;
import java.util.*;

public class MemoryUserDAO implements UserDAO
{
    private final Map<String, UserData> users = new HashMap<>()
;
    @Override
    public void clear()
    {
        users.clear();
    }

    @Override
    public UserData getUser(String username) throws DataAccessException
    {
        return users.get(username);
    }

    @Override
    public void insertUser(UserData user) throws DataAccessException
    {
        if (users.containsKey(user.username()))
        {
            throw new DataAccessException("User already exists: " + user.username());
        }
        users.put(user.username(), user);
    }

    @Override
    public Collection<UserData> listUsers() throws DataAccessException
    {
        return users.values();
    }

}
