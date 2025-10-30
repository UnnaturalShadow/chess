package dataaccess.database;

import dataaccess.DataAccessException;
import dataaccess.UserDao;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

public class DatabaseUserDao extends UserDao {
    @Override
    public void createUser(UserData userData) throws DataAccessException {
        String hashedPassword = BCrypt.hashpw(userData.password(), BCrypt.gensalt());
        String sqlStatement = "INSERT INTO users (username, password, email) VALUES(?,?,?)";
        executeCommand(sqlStatement, userData.username(), hashedPassword, userData.email());
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        String sqlStatement = "SELECT username, password, email FROM users WHERE username = ?";

        return executeQueryAndGetOne(sqlStatement, results -> new UserData(
                results.getString("username"),
                results.getString("password"),
                results.getString("email")
        ), username);
    }

    @Override
    public boolean validateWithPassword(String username, String password) throws DataAccessException {
        String sqlStatement = "SELECT password FROM users WHERE username = ?";
        String passwordFromDb = executeQueryAndGetOne(
                sqlStatement, results -> results.getString("password"), username
        );

        return passwordFromDb != null && BCrypt.checkpw(password, passwordFromDb);
    }

    @Override
    public void clear() throws DataAccessException {
        String sqlStatement = "TRUNCATE TABLE users";
        executeCommand(sqlStatement);
    }
}