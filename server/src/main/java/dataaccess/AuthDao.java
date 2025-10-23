package dataaccess;

public interface AuthDao extends Dao
{
    void addAuthToken(String username, String token);
    String authenticateToken(String token);
    void remove(String token) throws DataAccessException;
    void clear();
}
