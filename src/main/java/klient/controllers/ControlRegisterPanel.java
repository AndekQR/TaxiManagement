package klient.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.io.*;

public class ControlRegisterPanel {


    @FXML TextField name;
    @FXML TextField lastname;
    @FXML TextField email;
    @FXML TextField password;
    @FXML TextField rePassword;
    @FXML TextField city;
    @FXML TextField street;
    @FXML TextField houseNumber;
    @FXML TextField apartmentNumber;
    @FXML TextField bankAccount;
    @FXML TextField fail;

    private ViewManager manager;
    private ObjectInputStream objectInputStream = null;
    private ObjectOutputStream objectOutputStream = null;


    public void setManager(ViewManager manager) {
        this.manager=manager;
    }

    @FXML private void loginPanelHandle(){
        manager.showLoginPanel();
    }

    public void init() {

        if (objectOutputStream == null || objectInputStream == null){
            objectInputStream = manager.getObjectInputStream();
            objectOutputStream = manager.getObjectOutputStream();
        }

    }

    @FXML private void submitHandle() throws IOException, ClassNotFoundException {
        boolean messeageSend = false;

        String name = this.name.getText();
        String lastname = this.lastname.getText();
        String email = this.email.getText();
        String password = this.password.getText();
        String rePassword = this.rePassword.getText();
        String city = this.city.getText();
        String street = this.street.getText();
        String houseNumber = this.houseNumber.getText();
        String apartmentNumber = this.apartmentNumber.getText();
        String bankAccount = this.bankAccount.getText();

        if (password.equals(rePassword) && password.length()>0){ //czy reszta pol jest pusta sprawdza serwer
            messeageSend = true;
            fail.setVisible(false);
            String[] order = {manager.REGISTER, name, lastname, email, password, city, street, houseNumber, apartmentNumber, bankAccount};
            objectOutputStream.writeObject(order);
        }
        else
            fail.setVisible(true);


        if (messeageSend){
            String[] result;
            while ((result = (String[]) objectInputStream.readObject()) != null) {
                if (result[1].equals(manager.SUCCESS) && result[0].equals(manager.REGISTER)) { //startWith bo na koncu wyslanej wiadomosci pojawia sie znak nowej lini albo cos takiego i if go nie lapie
                    fail.setText("Konto zostalo utworzone!");
                    fail.setVisible(true);
                    manager.showLoginPanel();
                    break;
                } else if (result[1].equals(manager.FAIL) && result[0].equals(manager.REGISTER)) {
                    fail.setText("Błąd przy tworzeniu konta. Dane niepoprawne!");
                    fail.setVisible(true);
                    break;
                }
            }
        }

    }


}
