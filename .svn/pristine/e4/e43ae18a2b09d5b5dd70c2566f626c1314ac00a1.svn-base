package klient;


import javafx.application.Application;
import javafx.stage.Stage;
import klient.controllers.ViewManager;

import java.io.IOException;
import java.net.Socket;


public class Klient extends Application {

    private static final String host="localhost";
    private static final int port=8000;
    private Socket skt=null;

    public static void main(String args[]) {
        Application.launch(args); //wywolanie start()

    }

    private void connect() {
        try {
            skt=new Socket(host, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void start(Stage primaryStage) {
        this.connect();
        ViewManager viewManager = new ViewManager(primaryStage, skt);
        viewManager.showLoginPanel();
    }
}