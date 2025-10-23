package dataaccess;

import model.UserData;

public interface UserDao extends dao
{
    void createUser(UserData userData) throws DataAccessException;
    UserData getUser(String username) throws DataAccessException;
    boolean validateWithPassword(String username, String password);
    void clear();
}