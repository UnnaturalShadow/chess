package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.UserData;
import requests.AuthResult;

public class UserService
{

    public UserDAO user;
    public AuthService auth;

    public UserService(UserDAO user, AuthDAO authDAO)
    {
        this.user = user;
        this.auth = new AuthService(authDAO);

    }

    public void clear()
    {
        user.clear();
    }

    public AuthResult register(String username, String password, String email) throws DataAccessException
    {
        checkRequest(username, password);
        UserData userData = new UserData(username, password, email);

        if (user.getUser(username) != null)
        {
            throw new DataAccessException("Already exists user with username " + username);
        }

        user.createUser(userData);
        String tok = auth.genToken(username);
        return new AuthResult(username, tok);

    }

    public AuthResult login(String username, String password) throws DataAccessException
    {
        checkRequest(username, password);

        if (!user.validate(username, password))
        {
            throw new DataAccessException("Error: Unauthorized");
        }

        String tok = auth.genToken(username);
        return new AuthResult(username, tok);

    }

}
