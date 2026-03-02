package service;

import dataaccess.AlreadyTakenException;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.UserData;
import requests.AuthResult;

import java.util.Objects;

public class UserService
{

    private final UserDAO userDAO;
    private final AuthService authService;

    public UserService(UserDAO userDAO, AuthDAO authDAO)
    {
        this.userDAO = Objects.requireNonNull(userDAO);
        this.authService = new AuthService(authDAO);
    }


    public void clear()
    {
        userDAO.clear();
    }


    public AuthResult register(String username,
                               String password,
                               String email)
            throws DataAccessException, AlreadyTakenException
    {

        validateCredentials(username, password);

        ensureUserDoesNotExist(username);

        UserData newUser = buildUser(username, password, email);
        persistUser(newUser);

        return issueAuth(username);
    }

    public AuthResult login(String username,
                            String password)
            throws DataAccessException
    {

        validateCredentials(username, password);

        authenticateUser(username, password);

        return issueAuth(username);
    }

    private void validateCredentials(String username,
                                     String password)
            throws DataAccessException
    {

        if (isBlank(username) || isBlank(password))
        {
            throw new DataAccessException("Username and password required");
        }
    }

    private void ensureUserDoesNotExist(String username)
            throws DataAccessException, AlreadyTakenException
    {

        if (userDAO.getUser(username) != null)
        {
            throw new AlreadyTakenException(
                    "Username already in use"
            );
        }
    }

    private void authenticateUser(String username,
                                  String password)
            throws DataAccessException
    {

        boolean valid = userDAO.validate(username, password);
        if (!valid)
        {
            throw new DataAccessException("Unauthorized");
        }
    }

    private UserData buildUser(String username,
                               String password,
                               String email)
    {
        return new UserData(username, password, email);
    }

    private void persistUser(UserData user)
            throws DataAccessException
    {
        userDAO.createUser(user);
    }

    private AuthResult issueAuth(String username)
            throws DataAccessException
    {

        String token = authService.generateToken(username);
        return new AuthResult(username, token);
    }

    private boolean isBlank(String value)
    {
        return value == null || value.isBlank();
    }
}