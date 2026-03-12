package dataaccess.database;

import dataaccess.AuthDAO;
import dataaccess.exceptions.DataAccessException;

public class DatabaseAuthDAO extends AbstractDatabaseDAO implements AuthDAO {

    public DatabaseAuthDAO() throws DataAccessException {
        configureDatabase();
    }

    @Override
    public void clear() throws DataAccessException {
        executeCommand("TRUNCATE TABLE authdata");
    }

    @Override
    public void addToken(String username, String token) throws DataAccessException {
        if (getCount("SELECT COUNT(*) FROM authdata WHERE token = ?", token) > 0) {
            throw new DataAccessException("Token already exists");
        }
        executeCommand("INSERT INTO authdata (username, token) VALUES(?, ?)", username, token);
    }

    @Override
    public String findUsernameByToken(String token) throws DataAccessException {
        return executeQuery("SELECT username FROM authdata WHERE token = ?", rs -> rs.getString("username"), token);
    }

    @Override
    public void removeToken(String token) throws DataAccessException {
        executeCommand("DELETE FROM authdata WHERE token = ?", token);
    }

    @Override
    protected String[] getCreateStatements() {
        return new String[]{
                """
            CREATE TABLE IF NOT EXISTS `authdata` (
              `idauthData` INT NOT NULL AUTO_INCREMENT,
              `username` VARCHAR(100) NOT NULL,
              `token` VARCHAR(100) NOT NULL,
              PRIMARY KEY (`idauthData`),
              UNIQUE INDEX `idauthData_UNIQUE` (`idauthData` ASC) VISIBLE);
            """
        };
    }
}