package dataaccess;

import dataaccess.local.LocalAuthDao;
import dataaccess.local.LocalGameDao;
import dataaccess.local.LocalUserDao;

public class DaoCollection
{
    public UserDao userDao;
    public AuthDao authDao;
    public GameDao gameDao;

    public DaoCollection()
    {
        this.userDao = new LocalUserDao();
        this.authDao = new LocalAuthDao();
        this.gameDao = new LocalGameDao();
    }
}