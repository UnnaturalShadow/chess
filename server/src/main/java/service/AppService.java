package service;

import dataaccess.DAOCollection;
import io.javalin.http.Context;

public class AppService extends Service{
    public DAOCollection DAOs;

    public AppService(DAOCollection DAOs) {
        this.DAOs = DAOs;
    }

    public void clear(Context context) {
        this.DAOs.userDAO.clear();
        this.DAOs.authDAO.clear();
        this.DAOs.gameDAO.clear();
    }
}
