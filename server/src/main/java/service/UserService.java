package service;

import dataaccess.*;
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
            throws DataAccessException, AlreadyTakenException
    {

        validateInput(username, password);

        ensureUserDoesNotExist(username);

        UserData newUser = new UserData(username, password, email);
        userDAO.save(newUser);

        return issueToken(username);
    }

    public AuthResult login(String username, String password)
            throws DataAccessException, InvalidCredentialsException {

        validateInput(username, password);

        if (!userDAO.validateCredentials(username, password)) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        return issueToken(username);
    }

    // --- Private Helpers ---

    private void validateInput(String username, String password) throws DataAccessException
    {
        if (isBlank(username) || isBlank(password))
        {
            throw new DataAccessException("Username and password are required");
        }
    }

    private void ensureUserDoesNotExist(String username) throws AlreadyTakenException
    {
        Optional<UserData> existing = userDAO.findByUsername(username);
        if (existing.isPresent())
        {
            throw new AlreadyTakenException("Username already in use");
        }
    }

    private void authenticateUser(String username, String password) throws DataAccessException
    {
        boolean valid = userDAO.validateCredentials(username, password);
        if (!valid)
        {
            throw new DataAccessException("Unauthorized");
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