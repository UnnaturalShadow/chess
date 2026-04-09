package service;

import dataaccess.AuthDAO;
import dataaccess.exceptions.DataAccessException;
import dataaccess.exceptions.InvalidCredentialsException;

import java.util.Objects;
import java.util.UUID;

public class AuthService {

    private final AuthDAO dao;

    public AuthService(AuthDAO dao) {
        this.dao = Objects.requireNonNull(dao);
    }

    public String generateToken(String username) throws DataAccessException {
        assertNotBlank(username, "Username is required");

        String token = UUID.randomUUID().toString();
        persistToken(username, token);

        return token;
    }

    public void logout(String token) throws DataAccessException, InvalidCredentialsException {
        String user = resolveUser(token);

        if (user == null) {
            throw new InvalidCredentialsException("Error: Invalid or expired token");
        }

        revoke(token);
    }

    // ---------------- internal helpers ----------------

    private void persistToken(String username, String token) throws DataAccessException {
        dao.addToken(username, token);
    }

    private void revoke(String token) throws DataAccessException {
        dao.removeToken(token);
    }

    private String resolveUser(String token) throws DataAccessException {
        if (token == null || token.isBlank()) {
            throw new DataAccessException("Token required");
        }
        return dao.findUsernameByToken(token);
    }

    private void assertNotBlank(String value, String msg) throws DataAccessException {
        if (value == null || value.trim().isEmpty()) {
            throw new DataAccessException(msg);
        }
    }
}