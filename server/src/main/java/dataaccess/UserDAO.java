package dataaccess;

import model.UserData;

import java.util.Optional;

public interface UserDAO {
    void save(UserData user) throws DataAccessException;
    Optional<UserData> findByUsername(String username);
    boolean validateCredentials(String username, String password);
    void clear();
}