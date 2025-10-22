package dataaccess;

import dataaccess.local.LocalAuthDAO;
import dataaccess.local.LocalGameDAO;
import dataaccess.local.LocalUserDAO;

public class DAOCollection
{
    public UserDAO userDAO;
    public AuthDAO authDAO;
    public GameDAO gameDAO;

    public DAOCollection()
    {
        this.userDAO = new LocalUserDAO();
        this.authDAO = new LocalAuthDAO();
        this.gameDAO = new LocalGameDAO();
    }
}