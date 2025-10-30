package dataaccess;

import model.UserData;

public abstract class UserDao extends Dao
{
    public abstract void createUser(UserData userData) throws DataAccessException;
    public abstract UserData getUser(String username) throws DataAccessException;
    public abstract boolean validateWithPassword(String username, String password) throws DataAccessException;
    public abstract void clear() throws DataAccessException;
}