package dataaccess.database;

import dataaccess.UserDAO;
import dataaccess.exceptions.DataAccessException;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

public class DatabaseUserDAO extends AbstractDatabaseDAO implements UserDAO {

    public DatabaseUserDAO() throws DataAccessException {
        configureDatabase();
    }

    @Override
    public void clear() throws DataAccessException {
        executeCommand("TRUNCATE TABLE users");
    }

    @Override
    public void save(UserData user) throws DataAccessException {
        String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());
        executeCommand("INSERT INTO users (username, password, email) VALUES(?,?,?)",
                user.username(), hashedPassword, user.email());
    }

    @Override
    public UserData findByUsername(String username) throws DataAccessException {
        return executeQuery("SELECT username, password, email FROM users WHERE username = ?",
                rs -> new UserData(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("email")
                ), username);
    }

    @Override
    public boolean validateCredentials(String username, String password) throws DataAccessException {
        String dbPassword = executeQuery("SELECT password FROM users WHERE username = ?",
                rs -> rs.getString("password"), username);
        return dbPassword != null && BCrypt.checkpw(password, dbPassword);
    }

    @Override
    protected String[] getCreateStatements() {
        return new String[]{
                """
            CREATE TABLE IF NOT EXISTS `users` (
              `idusers` INT NOT NULL AUTO_INCREMENT,
              `username` VARCHAR(45) NOT NULL,
              `password` VARCHAR(255) NOT NULL,
              `email` VARCHAR(45) NOT NULL,
              PRIMARY KEY (`idusers`),
              UNIQUE INDEX `idusers_UNIQUE` (`idusers` ASC) VISIBLE,
              UNIQUE INDEX `username_UNIQUE` (`username` ASC) VISIBLE);
            """
        };
    }
}