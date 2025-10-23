package service;

import dataaccess.DaoCollection;
import dataaccess.DataAccessException;
import dataaccess.exceptions.AlreadyTakenException;
import dataaccess.exceptions.BadRequestException;
import dataaccess.exceptions.NotAValidColorException;
import dataaccess.exceptions.UserNotValidatedException;
import requestobjects.CreateRequest;
import requestobjects.CreateResult;
import requestobjects.JoinRequest;
import requestobjects.ListResult;

import java.util.Objects;

public class GameService extends Service{
    DaoCollection daos;
    public GameService(DaoCollection daos) {
        this.daos = daos;
    }

    public ListResult list(String token) throws UserNotValidatedException {
        if(daos.authDao.authenticateToken(token) == null){throw new UserNotValidatedException("Not validated");}

        return new ListResult(daos.gameDao.list());
    }

    public CreateResult create(String token, CreateRequest request) throws DataAccessException, BadRequestException {
        if(daos.authDao.authenticateToken(token) == null) {throw new UserNotValidatedException("Not validated");}
        checkForBadRequest(request.gameName());

        int id = daos.gameDao.create(request);

        return new CreateResult(id);
    }

    public void join(String token, JoinRequest request) throws UserNotValidatedException, AlreadyTakenException {
        String username = daos.authDao.authenticateToken(token);

        if(username == null) {throw new UserNotValidatedException("Not validated");}
        checkForBadRequest(request.playerColor(), request.gameID(), daos.gameDao.getGame(request.gameID()));
        if(!Objects.equals(request.playerColor(), "WHITE") && !Objects.equals(request.playerColor(), "BLACK")){
            throw new NotAValidColorException("Not a valid color");
        }

        daos.gameDao.join(request, username);
    }
}