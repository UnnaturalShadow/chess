package dataaccess;

import dataaccess.memory.MemoryAuthDao;
import dataaccess.memory.MemoryGameDao;
import dataaccess.memory.MemoryUserDao;

public class DaoCollection
{
    public UserDao userDao;
    public AuthDao authDao;
    public GameDao gameDao;

    public DaoCollection()
    {
        this.userDao = new MemoryUserDao();
        this.authDao = new MemoryAuthDao();
        this.gameDao = new MemoryGameDao();
    }
}