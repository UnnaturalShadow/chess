package dataaccess;

import dataaccess.exceptions.UserNotValidatedException;

public interface AuthDao extends Dao
{
    void addAuthToken(String username, String token);
    String authenticateToken(String token);
    void remove(String token) throws UserNotValidatedException;
    void clear();
}
