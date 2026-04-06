package client.websocket;

import chess.ChessGame;

public interface NotificationHandler {

    void loadGame(ChessGame game);

    void notify(String message);

    void error(String message);
}