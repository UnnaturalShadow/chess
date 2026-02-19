package dataaccess;

public interface AuthDAO
{
    void clear();
    String authenticate(String token);
    void remove(String token);
    void addToken(String username, String token);
}
