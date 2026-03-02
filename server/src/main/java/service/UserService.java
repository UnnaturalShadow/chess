package service;

import dataaccess.*;
import dataaccess.exceptions.AlreadyTakenException;
import dataaccess.exceptions.DataAccessException;
import dataaccess.exceptions.InvalidCredentialsException;
import dataaccess.exceptions.MissingFieldException;

import model.UserData;
import requests.AuthResult;

import java.util.Objects;
import java.util.Optional;

public class UserService
{

    private final UserDAO userDAO;
    private final AuthService authService;

    public UserService(UserDAO userDAO, AuthDAO authDAO)
    {
        this.userDAO = Objects.requireNonNull(userDAO);
        this.authService = new AuthService(authDAO);
    }

    // --- Public API ---

    public void clear()
    {
        userDAO.clear();
    }

    public AuthResult register(String username, String password, String email)
            throws DataAccessException, AlreadyTakenException, MissingFieldException
    {

        validateInput(username, password);

        ensureUserDoesNotExist(username);

        UserData newUser = new UserData(username, password, email);
        userDAO.save(newUser);

        return issueToken(username);
    }

    public AuthResult login(String username, String password)
            throws DataAccessException, InvalidCredentialsException, MissingFieldException
    {

        validateInput(username, password);

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

    private void ensureUserDoesNotExist(String username) throws AlreadyTakenException
    {
        Optional<UserData> existing = userDAO.findByUsername(username);
        if (existing.isPresent())
        {
            throw new AlreadyTakenException("Error: Username already in use"); // 403
        }
    }

    private void authenticateUser(String username, String password) throws InvalidCredentialsException
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