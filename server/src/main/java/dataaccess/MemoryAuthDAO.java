package dataaccess;

import model.AuthData;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MemoryAuthDAO implements AuthDAO
{
    private final Map<String, AuthData> auths = new HashMap<>();

    @Override
    public void clear() throws DataAccessException
    {
        auths.clear();
    }

    @Override
    public AuthData createAuth(AuthData auth) throws DataAccessException
    {
        if (auth == null || auth.authToken() == null)
        {
            throw new DataAccessException("AuthData or authToken is null");
        }
        if (auths.containsKey(auth.authToken()))
        {
            throw new DataAccessException("Auth token already exists: " + auth.authToken());
        }
        auths.put(auth.authToken(), auth);
        return auth;
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException
    {
        return auths.get(authToken);
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException
    {
        auths.remove(authToken);
    }

    @Override
    public Collection<AuthData> listAuths() throws DataAccessException
    {
        return auths.values();
    }
}
