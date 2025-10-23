package service;

import dataaccess.DaoCollection;
import dataaccess.DataAccessException;
import dataaccess.exceptions.AlreadyTakenException;
import dataaccess.exceptions.BadRequestException;
import dataaccess.exceptions.UserNotValidatedException;
import model.UserData;
import requestobjects.LoginRequest;
import requestobjects.LoginResult;
import requestobjects.RegisterRequest;
import requestobjects.RegisterResult;

public class UserService extends Service
{
    public DaoCollection daos;
    public AuthService authService;
    public UserService(DaoCollection daos)
    {
        this.daos = daos;
        authService = new AuthService(daos);
    }

    public void clear()
    {
        this.daos.userDao.clear();
    }

    public RegisterResult register(RegisterRequest request)
            throws DataAccessException, AlreadyTakenException, BadRequestException
    {
        checkForBadRequest(request.username(), request.password(), request.email());
        UserData user = new UserData(request.username(), request.password(), request.email());


        if (daos.userDao.getUser(user.username()) != null)
        {
            throw new AlreadyTakenException("Username " + user.username() + " already exists");
        }

        daos.userDao.createUser(user);
        String token = authService.generateNewToken(user.username());
        return new RegisterResult(request.username(), token);
    }

    public LoginResult login(LoginRequest request) throws UserNotValidatedException, BadRequestException
    {
        checkForBadRequest(request.username(), request.password());

        if (!daos.userDao.validateWithPassword(request.username(), request.password()))
        {
            throw new UserNotValidatedException("Error: unauthorized");
        }

        String token = authService.generateNewToken(request.username());
        return new LoginResult(request.username(), token);
    }
}
