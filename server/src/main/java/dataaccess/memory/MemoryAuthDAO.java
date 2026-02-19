package dataaccess.memory;

import dataaccess.AuthDAO;

import java.util.HashMap;
import java.util.Map;

public class MemoryAuthDAO implements AuthDAO
{
    public Map<String, String> authTokens = new HashMap<>();

    @Override
    public void addToken(String username, String token)
    {
        authTokens.put(token, username);
    }

    @Override
    public String authenticate(String token)
    {
        return authTokens.getOrDefault(token, null);
    }

    @Override
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
