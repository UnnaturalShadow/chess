package service;

import dataaccess.DaoCollection;
import dataaccess.DataAccessException;
import io.javalin.http.Context;

import static server.Server.setErrorContext;

public class AppService extends Service
{
    public DaoCollection daos;

    public AppService(DaoCollection daos)
    {
        this.daos = daos;
    }

    public void clear(Context context)
    {
        try
        {
            this.daos.userDao.clear();
            this.daos.authDao.clear();
            this.daos.gameDao.clear();
        } catch (DataAccessException e)
        {
            setErrorContext(context, "500 Data Access Error: Could not connect to database", 500);
        }

    }
}