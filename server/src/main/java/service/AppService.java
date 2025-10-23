package service;

import dataaccess.DaoCollection;
import io.javalin.http.Context;

public class AppService extends Service{
    public DaoCollection daos;

    public AppService(DaoCollection daos) {
        this.daos = daos;
    }

    public void clear(Context context) {
        this.daos.userDao.clear();
        this.daos.authDao.clear();
        this.daos.gameDao.clear();
    }
}

