package dataaccess.database;

import dataaccess.DaoCollection;

public class DatabaseDaoCollection extends DaoCollection
{
    public DatabaseDaoCollection()
    {
        this.userDao = new DatabaseUserDao();
        this.authDao = new DatabaseAuthDao();
        this.gameDao = new DatabaseGameDao();
    }
}