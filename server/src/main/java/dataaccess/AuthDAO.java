package dataaccess;

import dataaccess.exceptions.DataAccessException;

import java.util.Optional;

public interface AuthDAO
{
    void addToken(String username, String token) throws DataAccessException;

    String findUsernameByToken(String token) throws DataAccessException;

    void removeToken(String token) throws DataAccessException;

    void clear() throws DataAccessException;
}