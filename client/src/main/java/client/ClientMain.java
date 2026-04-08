package client;

import ui.ChessClient;

public class ClientMain {
    public static void main(String[] args) {
        ServerFacade facade = new ServerFacade(8080);
        ChessClient chessClient = new ChessClient(facade);
        chessClient.run();
    }
}
