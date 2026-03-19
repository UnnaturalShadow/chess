package client;

import chess.*;
import ui.ChessClient;

public class ClientMain {
    static void main(String[] args) {
        ServerFacade facade = new ServerFacade(8080);
        System.out.println("Success!");
        ChessClient chessClient = new ChessClient(facade);
        chessClient.run();


    }
}
