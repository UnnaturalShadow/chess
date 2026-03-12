package dataaccess;

import dataaccess.exceptions.DataAccessException;
import model.UserData;



public interface UserDAO
{
    void save(UserData user) throws DataAccessException;
    UserData findByUsername(String username) throws DataAccessException;
    boolean validateCredentials(String username, String password) throws DataAccessException;
    void clear() throws DataAccessException;
}