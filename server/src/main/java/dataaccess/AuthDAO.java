package dataaccess;

public interface AuthDAO extends DAO
{
    void addAuthToken(String username, String token);
    String authenticateToken(String token);
    void remove(String token) throws DataAccessException;
    void clear();
}
