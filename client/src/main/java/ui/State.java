package ui;


import chess.ChessGame;
import model.GameData;

import java.util.List;

public class State
{
    public enum LoggedInState
    {
        SIGNEDOUT,
        SIGNEDIN,
        INGAME
    }

    public LoggedInState loggedInState;
    public List<GameData> games;
    public ChessGame currentGame = null;
    public int currentGameId = 0;
    public ChessGame.TeamColor perspective = null;
    public String username = null;

    public State()
    {
        this.loggedInState = LoggedInState.SIGNEDOUT;
    }
}