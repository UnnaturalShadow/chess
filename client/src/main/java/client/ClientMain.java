package client;

import chess.*;
import server.ServerFacade;

public class ClientMain {
    public static void main(String[] args) {
        ServerFacade facade = new ServerFacade("http://localhost:8080/");
        System.out.println("Success!");
    }
}
