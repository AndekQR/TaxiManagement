package klient.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;


public class ControlLoginPanel {

    private ObjectInputStream objectInputStream = null;
    private ObjectOutputStream objectOutputStream = null;
    private ViewManager manager;

    @FXML TextField noAccount;
    @FXML private TextField login; //nazwy zmiennych musza byc takie same jak id w log.fxml
    @FXML private PasswordField password;



    public void setManager(ViewManager manager) {
        this.manager=manager;
    }


    @FXML
    private void signinHandle() throws IOException, ClassNotFoundException { //funnkcja do obługi przycisku signin, jest to zaznaczone w log.fxml

        if (objectOutputStream == null || objectInputStream == null)
            this.init();

        String log=login.getText();
        String pass=password.getText();


//        //TODO: Dwie linijki są do testów mainPanela, trzeba usunąć
//        log = "email@wp.pl";
//       pass = "haslo";


        String[] order = {manager.LOGIN, log, pass};
        objectOutputStream.writeObject(order); //przed zlymi danymi oraz przed pustymi Stringami jest zabezpieczone w serverze i w klasie bazy danych

        String[] result;
        while ((result=(String[]) objectInputStream.readObject()) != null) {
            if (result[1].equals(manager.SUCCESS) && result[0].equals(manager.LOGIN)) { //startWith bo na koncu wyslanej wiadomosci pojawia sie znak nowej lini albo cos takiego i if go nie lapie
                noAccount.setVisible(false);
                manager.showMainPanel();
                break;
            } else if (result[1].equals(manager.FAIL) && result[0].equals(manager.LOGIN)) {
                noAccount.setText("Konto nie istnieje!");
                noAccount.setVisible(true);
                break;
            }
        }

    }

    @FXML
    private void signupHandle() {
        manager.showRegisterPanel();
    }

    public void init() {
        if (objectOutputStream == null || objectInputStream == null){
            objectInputStream = manager.getObjectInputStream();
            objectOutputStream = manager.getObjectOutputStream();
        }

    }
}