package service;
import dataaccess.DAOCollection;
import dataaccess.DataAccessException;
import dataaccess.exceptions.BadRequestException;
import dataaccess.exceptions.UserNotValidatedException;

import java.util.UUID;

public class AuthService extends Service
{
    public DAOCollection DAOs;
    public AuthService(DAOCollection DAOs)
    {
        this.DAOs = DAOs;
    }

    public String generateNewToken(String username)
    {
        if (username == null)
        {
            throw new BadRequestException("No username supplied");
        }
        String newToken = UUID.randomUUID().toString();

        this.DAOs.authDAO.addAuthToken(username, newToken);
        return newToken;
    }

    public void logout(String token) throws DataAccessException
    {
        if (DAOs.authDAO.authenticateToken(token) == null)
        {
            throw new UserNotValidatedException("Not validated");
        }
        DAOs.authDAO.remove(token);
    }
}