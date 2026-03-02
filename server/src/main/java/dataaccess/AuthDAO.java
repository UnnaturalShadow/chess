package dataaccess;

import java.util.Optional;

public interface AuthDAO
{
    void addToken(String username, String token);
    Optional<String> findUsernameByToken(String token);
    void removeToken(String token);
    void clear();
}