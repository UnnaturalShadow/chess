package service;

import dataaccess.DAOCollection;
import dataaccess.DataAccessException;
//import dataaccess.exceptions.AlreadyTakenException;
//import dataaccess.exceptions.BadRequestException;
//import dataaccess.exceptions.NotAValidColorException;
//import dataaccess.exceptions.UserNotValidatedException;
import requestobjects.CreateRequest;
import requestobjects.CreateResult;
import requestobjects.JoinRequest;
import requestobjects.ListResult;

import java.util.Objects;

public class GameService extends Service
{
    DAOCollection DAOs;
    public GameService(DAOCollection DAOs)
    {
        this.DAOs = DAOs;
    }

    public ListResult list(String token) throws DataAccessException
    {
        if(DAOs.authDAO.authenticateToken(token) == null){throw new DataAccessException("Not validated");}

        return new ListResult(DAOs.gameDAO.list());
    }

    public CreateResult create(String token, CreateRequest request) throws DataAccessException
    {
        if(DAOs.authDAO.authenticateToken(token) == null) {throw new DataAccessException("Not validated");}
        checkForBadRequest(request.gameName());

        int id = DAOs.gameDAO.create(request);

        return new CreateResult(id);
    }

    public void join(String token, JoinRequest request) throws DataAccessException
    {
        String username = DAOs.authDAO.authenticateToken(token);

        if(username == null) {throw new DataAccessException("Not validated");}
        checkForBadRequest(request.playerColor(), request.gameID(), DAOs.gameDAO.getGame(request.gameID()));
        if(!Objects.equals(request.playerColor(), "WHITE") && !Objects.equals(request.playerColor(), "BLACK")){
            throw new DataAccessException("Not a valid color");
        }

        DAOs.gameDAO.join(request, username);
    }
}