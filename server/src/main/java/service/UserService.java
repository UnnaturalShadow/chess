package service;

import dataaccess.DAOCollection;
import dataaccess.DataAccessException;
//import dataaccess.exceptions.AlreadyTakenException;
//import dataaccess.exceptions.BadRequestException;
//import dataaccess.exceptions.UserNotValidatedException;
import model.UserData;
import requestobjects.LoginRequest;
import requestobjects.LoginResult;
import requestobjects.RegisterRequest;
import requestobjects.RegisterResult;

public class UserService extends Service
{
    public DAOCollection DAOs;
    public AuthService authService;
    public UserService(DAOCollection DAOs)
    {
        this.DAOs = DAOs;
        authService = new AuthService(DAOs);
    }

    public void clear()
    {
        this.DAOs.userDAO.clear();
    }

    public RegisterResult register(RegisterRequest request)
            throws DataAccessException
    {
        checkForBadRequest(request.username(), request.password(), request.email());
        UserData user = new UserData(request.username(), request.password(), request.email());


        if (DAOs.userDAO.getUser(user.username()) != null)
        {
            throw new DataAccessException("Username " + user.username() + " already exists");
        }

        DAOs.userDAO.createUser(user);
        String token = authService.generateNewToken(user.username());
        return new RegisterResult(request.username(), token);
    }

    public LoginResult login(LoginRequest request) throws DataAccessException
    {
        checkForBadRequest(request.username(), request.password());

        if (!DAOs.userDAO.validateWithPassword(request.username(), request.password()))
        {
            throw new DataAccessException("Error: unauthorized");
        }

        String token = authService.generateNewToken(request.username());
        return new LoginResult(request.username(), token);
    }
}
