package dataaccess.memory;

import dataaccess.AuthDAO;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MemoryAuthDAO implements AuthDAO {

    private final Map<String, String> tokenStore = new HashMap<>();

    @Override
    public void addToken(String username, String token) {
        tokenStore.put(token, username);
    }

    @Override
    public String findUsernameByToken(String token) {
        return (tokenStore.get(token));
    }

    @Override
    public void removeToken(String token) {
        tokenStore.remove(token);
    }

    @Override
    public void clear() {
        tokenStore.clear();
    }
}