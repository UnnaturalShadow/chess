package dataaccess;

import model.UserData;

public interface UserDAO
{
    void createUser(UserData userData) throws DataAccessException;
    void clear();
    UserData getUser(String username) throws DataAccessException;
    boolean validate(String username, String password);
}
