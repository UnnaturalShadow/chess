package service;

import dataaccess.DaoCollection;
import dataaccess.DataAccessException;
import dataaccess.exceptions.AlreadyTakenException;
import dataaccess.exceptions.NotAValidColorException;
import dataaccess.exceptions.UserNotValidatedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import requestobjects.*;

public class GameServiceTests
{
    GameService gameService;
    UserService userService;
    String validToken;

    @BeforeEach
    public void setup() throws AlreadyTakenException, DataAccessException
    {
        DaoCollection daos = new DaoCollection();
        userService = new UserService(daos);
        gameService = new GameService(daos);
        RegisterResult result = userService.register(
            new RegisterRequest("Garrett", "Password", "email@email.com"));
        validToken = result.authToken();

        gameService.create(validToken, new CreateRequest("newGame"));
    }

    @Nested
    class ListTests
    {
        @Test
        public void listWithValidToken() throws DataAccessException
        {
            Assertions.assertNotNull(gameService.list(validToken));
        }

        @Test
        public void listWithInvalidToken()
        {
            Assertions.assertThrows(UserNotValidatedException.class, () -> gameService.list("Badtoken"));
        }
    }

    @Nested
    class CreateTests
    {
        @Test
        public void goodGameCreation() throws DataAccessException
        {
            CreateResult result = gameService.create(validToken, new CreateRequest("newGame"));
            Assertions.assertEquals(2, result.gameID());
        }

        @Test
        public void nonAuthorizedGameCreation()
        {
            Assertions.assertThrows(UserNotValidatedException.class,
                () -> gameService.create("BadToken", new CreateRequest("newGame")));
        }
    }

    @Nested
    class JoinTests
    {
        @Test
        public void goodJoin()
        {
            Assertions.assertDoesNotThrow(() ->
                gameService.join(validToken, new JoinRequest("WHITE", 1)));
        }

        @Test
        public void badJoin()
        {
            Assertions.assertThrows(NotAValidColorException.class, () ->
                gameService.join(validToken, new JoinRequest("GREEN", 1)));
        }
    }
}
