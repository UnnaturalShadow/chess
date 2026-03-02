package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;

import java.util.Objects;
import java.util.UUID;

public class AuthService
{

    private final AuthDAO authDAO;

    public AuthService(AuthDAO authDAO)
    {
        this.authDAO = Objects.requireNonNull(authDAO);
    }

    // --- Public API ---

    public String generateToken(String username) throws DataAccessException
    {
        requireNonBlank(username, "Username is required");

        String token = createToken();
        storeToken(username, token);

        return token;
    }

    public void logout(String token) throws DataAccessException
    {
        String user = requireAuthenticated(token);
        revokeToken(token);
    }

    // --- Private Helpers ---

    private String createToken()
    {
        return UUID.randomUUID().toString();
    }

    private void storeToken(String username, String token) throws DataAccessException
    {
        authDAO.addToken(username, token);
    }

    private void revokeToken(String token) throws DataAccessException
    {
        authDAO.removeToken(token);
    }

    private String requireAuthenticated(String token) throws DataAccessException
    {
        requireNonBlank(token, "Token required");

        return authDAO.findUsernameByToken(token)
                .orElseThrow(() -> new DataAccessException("Invalid or expired token"));
    }

    private void requireNonBlank(String value, String message) throws DataAccessException
    {
        if (value == null || value.isBlank())
        {
            throw new DataAccessException(message);
        }
    }
}