package service;

import dataaccess.UserDAO;
import dataaccess.AuthDAO;
import dataaccess.exceptions.*;
import model.UserData;
import requests.AuthResult;

import java.util.Objects;

public class UserService {

    private final UserDAO users;
    private final AuthService auth;

    public UserService(UserDAO users, AuthDAO authDAO) {
        this.users = Objects.requireNonNull(users);
        this.auth = new AuthService(authDAO);
    }

    public void clear() throws DataAccessException {
        users.clear();
    }

    public AuthResult register(String username, String password, String email)
            throws DataAccessException, AlreadyTakenException, MissingFieldException {

        requireValidCredentials(username, password);

        if (users.findByUsername(username) != null) {
            throw new AlreadyTakenException("Error: Username already in use");
        }

        users.save(new UserData(username, password, email));

        return issue(username);
    }

    public AuthResult login(String username, String password)
            throws DataAccessException,
            InvalidCredentialsException,
            UserNotAuthenticatedException {

        requireValidCredentials(username, password);

        UserData existing = users.findByUsername(username);
        if (existing == null) {
            throw new UserNotAuthenticatedException("Error: User not registered.");
        }

        if (!users.validateCredentials(username, password)) {
            throw new InvalidCredentialsException("Error: Invalid username or password");
        }

        return issue(username);
    }

    // ---------------- helpers ----------------

    private void requireValidCredentials(String u, String p) throws MissingFieldException {
        if (isBlank(u) || isBlank(p)) {
            throw new MissingFieldException("Error: Username and password required");
        }
    }

    private AuthResult issue(String username) throws DataAccessException {
        String token = auth.generateToken(username);
        return new AuthResult(username, token);
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}