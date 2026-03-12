package service;

import dataaccess.*;
import dataaccess.exceptions.*;

import model.UserData;
import requests.AuthResult;

import java.util.Objects;
import java.util.UUID;

public class UserService
{

    private final UserDAO userDAO;
    private final AuthService authService;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO)
    {
        this.userDAO = Objects.requireNonNull(userDAO);
        this.authService = new AuthService(authDAO);
        this.authDAO = authDAO;
    }

    // --- Public API ---

    public void clear() throws DataAccessException
    {
        userDAO.clear();
    }

    public AuthResult register(String username, String password, String email)
            throws DataAccessException, AlreadyTakenException, MissingFieldException {
        validateInput(username, password);
        ensureUserDoesNotExist(username);
        UserData newUser = new UserData(username, password, email);
        userDAO.save(newUser);
        return issueToken(username);
    }

    public AuthResult login(String username, String password)
            throws DataAccessException, InvalidCredentialsException, UserNotAuthenticatedException
    {

        validateInput(username, password);
        UserData existing = userDAO.findByUsername(username);
        if (existing == null)
        {
            throw new UserNotAuthenticatedException("Error: User not registered."); // 403
        }
        authenticateUser(username, password);

        return issueToken(username);
    }

    // --- Private Helpers ---

    private void validateInput(String username, String password) throws MissingFieldException
    {
        if (isBlank(username) || isBlank(password))
        {
            throw new MissingFieldException("Error: Username and password are required"); // 400
        }
    }

    private void ensureUserDoesNotExist(String username) throws AlreadyTakenException, DataAccessException
    {
        UserData existing = userDAO.findByUsername(username);
        if (existing != null)
        {
            throw new AlreadyTakenException("Error: Username already in use"); // 403
        }
    }

    private void authenticateUser(String username, String password) throws InvalidCredentialsException, DataAccessException
    {
        boolean valid = userDAO.validateCredentials(username, password);
        if (!valid)
        {
            throw new InvalidCredentialsException("Error: Invalid username or password"); // 401
        }
    }

    private AuthResult issueToken(String username) throws DataAccessException
    {
        String token = authService.generateToken(username);
        return new AuthResult(username, token);
    }

    private boolean isBlank(String value)
    {
        return value == null || value.isBlank();
    }
}