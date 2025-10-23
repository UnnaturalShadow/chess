package service;
import dataaccess.DaoCollection;
import dataaccess.DataAccessException;
import dataaccess.exceptions.BadRequestException;
import dataaccess.exceptions.UserNotValidatedException;

import java.util.UUID;

public class AuthService extends Service
{
    public DaoCollection daos;
    public AuthService(DaoCollection daos)
    {
        this.daos = daos;
    }

    public String generateNewToken(String username)
    {
        if (username == null)
        {
            throw new BadRequestException("No username supplied");
        }
        String newToken = UUID.randomUUID().toString();

        this.daos.authDao.addAuthToken(username, newToken);
        return newToken;
    }

    public void logout(String token) throws UserNotValidatedException {
        // Token missing or invalid
        if (token == null || daos.authDao.authenticateToken(token) == null) {
            throw new UserNotValidatedException("Not validated");
        }

        // Safe to remove; LocalAuthDao never throws DataAccessException
        daos.authDao.remove(token);
    }

}