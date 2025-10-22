package dataaccess.local;

import dataaccess.AuthDAO;

import java.util.HashMap;
import java.util.Map;

public class LocalAuthDAO implements AuthDAO
{
    public Map<String, String> authTokens = new HashMap<>();

    public void addAuthToken(String username, String token)
    {
        authTokens.put(token, username);
    }

    public String authenticateToken(String token)
    {
        return authTokens.getOrDefault(token, null);
    }

    public void clear()
    {
        authTokens = new HashMap<>();
    }

    @Override
    public void remove(String token)
    {
        authTokens.remove(token);
    }
}