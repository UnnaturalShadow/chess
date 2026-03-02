package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;

import java.util.UUID;

public class AuthService
{
    private AuthDAO authDAO;
    public AuthService(AuthDAO authDAO)
    {
        this.authDAO = authDAO;
    }

    public String genToken(String username) throws DataAccessException
    {
        if (username == null)
        {
            throw new DataAccessException("No username supplied");
        }
        String newToken = UUID.randomUUID().toString();

        authDAO.addToken(username, newToken);
        return newToken;
    }

    public void logout(String token) throws DataAccessException
    {
        if (token == null || authDAO.authenticate(token) == null)
        {
            throw new DataAccessException("Not validated");
        }
        authDAO.remove(token);
    }

}
