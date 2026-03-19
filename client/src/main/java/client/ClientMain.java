package client;

import chess.*;
import server.ServerFacade;
import ui.ChessClient;

public class ClientMain {
    public static void main(String[] args) {
//        ServerFacade facade = new ServerFacade("http://localhost:8080/");
        System.out.println("Success!");
        ChessClient chessClient = new ChessClient();
        chessClient.start();


    }
}
