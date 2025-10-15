package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;

import java.util.UUID;

public class UserService
{
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO)
    {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public AuthData register(UserData user) throws DataAccessException
    {
        if(user.username() == null || user.password() == null || user.email() == null)
        {
            throw new DataAccessException("Error: bad request");
        }

        if (userDAO.getUser(user.username()) != null)
        {
            throw new DataAccessException("Error: already taken");
        }

        userDAO.insertUser(user);

        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, user.username());
        authDAO.createAuth(authData);

        return authData;
    }
}
