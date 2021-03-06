package klient.controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ViewManager {

    private final Stage primaryStage;
    private final Socket skt;
    private ObjectInputStream objectInputStream = null;
    private ObjectOutputStream objectOutputStream = null;

    private static final String REGISTER_SCREEN = "/panels/registerScreen.fxml";
    private static final String LOGIN_PANEL = "/panels/log.fxml";
    private static final String MAIN_PANEL = "/panels/mainPanel.fxml";


    public final String LOGIN="login";
    public final String SUCCESS="success";
    public final String FAIL="fail";
    public final String REGISTER="register";
    public final String HEADER_NAME = "headerName";
    public final String CLIENT_HOME = "clientHome";
    public final String PRICE = "price"; //przekazuje rowniez ilosc kilometrow
    public final String RIDE = "ride"; //dla przejazdów
    public final String HISTORY_CONTENT = "history_content";

    public ViewManager(Stage primaryStage, Socket skt){
        this.primaryStage=primaryStage;
        this.skt=skt;

        try {
            objectInputStream = new ObjectInputStream(this.skt.getInputStream());
            objectOutputStream = new ObjectOutputStream(this.skt.getOutputStream());
            objectOutputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showLoginPanel(){
        FXMLLoader loader = getLoader(LOGIN_PANEL);
        try {
            Pane root = loader.load();

            Scene scene = new Scene(root);
            primaryStage.setTitle("Logowanie");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.sizeToScene();

            ControlLoginPanel controlLoginPanel = loader.getController();
            controlLoginPanel.setManager(this);
            controlLoginPanel.init();

            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showRegisterPanel(){
        FXMLLoader loader = getLoader(REGISTER_SCREEN);
        try {
            ScrollPane root =loader.load();

            Scene scene = new Scene(root);
            primaryStage.setTitle("Rejestracja");
            primaryStage.setScene(scene);

            ControlRegisterPanel controlRegisterPanel = loader.getController();
            controlRegisterPanel.setManager(this);
            controlRegisterPanel.init();

            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showMainPanel(){
        FXMLLoader loader = getLoader(MAIN_PANEL);

        try {
            Pane root = loader.load();

            Scene scene = new Scene(root);
            primaryStage.setTitle("Start");
            primaryStage.setScene(scene);

            ControlMainPanel controlMainPanel = loader.getController();
            controlMainPanel.setManager(this);
            controlMainPanel.init();

            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private FXMLLoader getLoader(String fxmlPath) {
        FXMLLoader loader;
        loader = new FXMLLoader(this.getClass().getResource(fxmlPath));
        return loader;
    }

    public ObjectInputStream getObjectInputStream(){
        return objectInputStream;
    }

    public ObjectOutputStream getObjectOutputStream() {
        return objectOutputStream;
    }

    public Socket getSocket(){
        return skt;
    }
}
