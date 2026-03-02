package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.GameData;
import requests.CreateRequest;
import requests.JoinRequest;

import java.util.List;
import java.util.Objects;


public class GameService
{
    public GameDAO gameDAO;
    public AuthDAO authDAO;

    public GameService(GameDAO gameDAO, AuthDAO authDAO)
    {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    void checkRequest(Object... requestFields) throws DataAccessException
    {
        for (Object requestField : requestFields)
        {
            if (requestField == null)
            {
                throw new DataAccessException("Missing one or more fields in request");
            }
        }
    }

    public void join(String token, JoinRequest req) throws DataAccessException
    {
        String name = authDAO.authenticate(token);

        if (name == null)
        {
            throw new DataAccessException("Not validated");
        }

        checkRequest(req.color(), req.gameID(), gameDAO.getGame(req.gameID()));
        if(!Objects.equals(req.color(), "WHITE") && !Objects.equals(req.color(), "BLACK"))
        {
            throw new DataAccessException("Not a valid color.");
        }

        gameDAO.joinGame(req,name);
    }

    public List<GameData> list(String token) throws DataAccessException
    {
        if(authDAO.authenticate(token) == null)
        {
            throw new DataAccessException("Not validated");
        }

        return gameDAO.listGames();
    }

    public int create(String token, CreateRequest request) throws DataAccessException
    {
        if(authDAO.authenticate(token) == null)
        {
            throw new DataAccessException("Not validated");
        }
        checkRequest(request.gameName());

        return gameDAO.createGame(request);
    }

}
