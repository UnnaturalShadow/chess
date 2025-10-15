package dataaccess;

import model.UserData;
import java.util.Collection;

public interface UserDAO
{
    void clear() throws DataAccessException;
    UserData getUser(String username) throws DataAccessException;
    void insertUser(UserData user) throws DataAccessException;
    Collection<UserData> listUsers() throws DataAccessException;
}
