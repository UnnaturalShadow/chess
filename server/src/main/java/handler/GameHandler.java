package handler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
//import dataaccess.exceptions.AlreadyTakenException;
//import dataaccess.exceptions.BadRequestException;
//import dataaccess.exceptions.NotAValidColorException;
//import dataaccess.exceptions.UserNotValidatedException;
import requestobjects.CreateRequest;
import requestobjects.CreateResult;
import requestobjects.JoinRequest;
import requestobjects.ListResult;
import service.GameService;

import io.javalin.http.Context;

import static server.Server.buildJson;
import static server.Server.setErrorContext;

public class GameHandler
{
    GameService gameService;
    Gson serializer = new Gson();
    public GameHandler(GameService gameService)
    {
        this.gameService = gameService;
    }

    public void list(Context context)
    {
        try
        {
            ListResult result = gameService.list(context.header("authorization"));
            context.result(buildJson("games", result.games()));
        }
        catch (DataAccessException e)
        {
            setErrorContext(context, "401 Unauthorized Error: Unauthorized", 401);
        }
    }

    public void create(Context context)
    {
        try
        {
            CreateResult result = gameService.create(
                    context.header("authorization"), serializer.fromJson(context.body(), CreateRequest.class)
            );
            context.result(buildJson("gameID", result.gameID()));
        }
//        catch (UserNotValidatedException e)
//        {
//            setErrorContext(context, "401 Unauthorized Error: Unauthorized", 401);
//        }
        catch (DataAccessException e) {
            setErrorContext(context,"500 Data Access Error: Failed to create new game", 500);
        }
//        catch (BadRequestException e)
//        {
//            setErrorContext(context,"400 Bad Request Error: Some field was missing", 400);
//        }
    }

    public void join(Context context)
    {
        try {
            gameService.join(context.header("authorization"), serializer.fromJson(context.body(), JoinRequest.class));
        }
        catch (DataAccessException e)
        {
            setErrorContext(context, "401 Unauthorized Error: Unauthorized", 401);
        }
//        catch (BadRequestException e) {
//            setErrorContext(context,"400 Bad Request Error: Some field was missing", 400);
//        } catch (AlreadyTakenException e) {
//            setErrorContext(context,"403 Bad Request Error: Color already taken", 403);
//        } catch (NotAValidColorException e) {
//            setErrorContext(context,"400 Bad Request Error: Not a valid color", 400);
//        }
    }
}