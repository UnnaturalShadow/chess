package dataaccess.memory;

import dataaccess.DaoCollection;

public class MemoryDaoCollection extends DaoCollection
{
    public MemoryDaoCollection(){
        this.userDao = new MemoryUserDao();
        this.authDao = new MemoryAuthDao();
        this.gameDao = new MemoryGameDao();
    }
}