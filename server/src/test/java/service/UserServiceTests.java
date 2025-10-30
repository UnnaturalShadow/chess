package service;

import dataaccess.DaoCollection;
import dataaccess.DataAccessException;
import dataaccess.UserDao;
import dataaccess.database.DatabaseDaoCollection;
import dataaccess.exceptions.AlreadyTakenException;
import dataaccess.exceptions.UserNotValidatedException;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import requestobjects.LoginRequest;
import requestobjects.LoginResult;
import requestobjects.RegisterRequest;
import service.UserService;

public class UserServiceTests
{
    public static UserService userService;
    @BeforeEach
    public void setup() throws DataAccessException
    {
        DaoCollection daos = new DatabaseDaoCollection();
        userService = new UserService(daos);
        daos.gameDao.clear();
        daos.authDao.clear();
        daos.userDao.clear();
    }

    @Nested
    class RegisterTests
    {
        @Test
        public void createUserThatDoesNotExist() throws AlreadyTakenException, DataAccessException
        {
            RegisterRequest request = new RegisterRequest(
                    "Jesus", "password123", "jesus@email.com"
            );
            userService.register(request);

            UserDao dao =  userService.daos.userDao;
            UserData user = dao.getUser("Jesus");

            Assertions.assertEquals("Jesus", user.username());
            Assertions.assertTrue(
                    BCrypt.checkpw("password123", user.password()),
                    "Stored password hash does not match plaintext password"
            );
            Assertions.assertEquals("jesus@email.com", user.email());
        }

        @Test
        public void createUserThatExists() throws AlreadyTakenException, DataAccessException
        {
            RegisterRequest request1 = new RegisterRequest(
                    "Jesus", "password123", "jesus@email.com"
            );
            userService.register(request1);

            RegisterRequest request2 = new RegisterRequest(
                    "Jesus", "password123", "jesus@email.com"
            );
            Assertions.assertThrows(AlreadyTakenException.class, () -> userService.register(request2));
        }
    }

    @Nested
    class LoginTests
    {
        @BeforeEach
        public void registerJesus() throws AlreadyTakenException, DataAccessException
        {
            DaoCollection daos = new DatabaseDaoCollection();
            daos.gameDao.clear();
            daos.authDao.clear();
            daos.userDao.clear();
            RegisterRequest request = new RegisterRequest(
                    "Jesus", "password123", "jesus@email.com"
            );
            userService.register(request);
        }

        @Test
        public void loginUserThatExists() throws DataAccessException
        {
            LoginResult result = userService.login(new LoginRequest("Jesus", "password123"));

            Assertions.assertEquals("Jesus", result.username());
            Assertions.assertNotNull(result.authToken());
            Assertions.assertFalse(result.authToken().isEmpty());
        }

        @Test
        public void loginUserThatDoesNotExist()
        {
            Assertions.assertThrows(UserNotValidatedException.class,
                    () -> userService.login(new LoginRequest("Jerome", "password123")));
        }

        @Test
        public void badPassword()
        {
            Assertions.assertThrows(UserNotValidatedException.class,
                    () -> userService.login(new LoginRequest("Jesus", "notthepassword")));
        }
    }
}