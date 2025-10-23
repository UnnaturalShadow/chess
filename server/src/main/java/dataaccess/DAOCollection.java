package dataaccess;

import dataaccess.local.LocalAuthDao;
import dataaccess.local.LocalGameDao;
import dataaccess.local.LocalUserDao;

public class DAOCollection
{
    public UserDao userDAO;
    public AuthDao authDAO;
    public GameDao gameDAO;

    public DAOCollection()
    {
        this.userDAO = new LocalUserDao();
        this.authDAO = new LocalAuthDao();
        this.gameDAO = new LocalGameDao();
    }
}