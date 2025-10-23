package service;
import dataaccess.DaoCollection;
import dataaccess.DataAccessException;
import dataaccess.exceptions.BadRequestException;
import dataaccess.exceptions.UserNotValidatedException;

import java.util.UUID;

public class AuthService extends Service
{
    public DaoCollection DAOs;
    public AuthService(DaoCollection DAOs)
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

        this.DAOs.authDao.addAuthToken(username, newToken);
        return newToken;
    }

    public void logout(String token) throws DataAccessException
    {
        if (DAOs.authDao.authenticateToken(token) == null)
        {
            throw new UserNotValidatedException("Not validated");
        }
        DAOs.authDao.remove(token);
    }
}