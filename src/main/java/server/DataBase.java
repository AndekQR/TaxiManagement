package server;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

public class DataBase {

    private Connection connect=null;
    private Statement statement=null;
    private PreparedStatement preparedStatement=null;
    private ResultSet resultSet=null;

    private int howManyRows(ResultSet result) throws SQLException {
        int count = 0;
        while(result.next()){
            count++;
        }

        return count;
    }

    public synchronized boolean createAccount(String[] data){ //register, name, lastname, email, password, city, street, houseNumber, apartmentNumber, bankAccount
        for (int i=1; i<data.length; i++) {//od 1 bo w 0 jest komunikat REGISTER
            if (data[i].length() == 0)
                return false;
        }

            try {
                preparedStatement = connect.prepareStatement("SELECT * FROM KLIENCI WHERE email = ? AND haslo=?");
                preparedStatement.setString(1, data[3]);
                preparedStatement.setString(2, data[4]);
                resultSet = preparedStatement.executeQuery();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            int returnedRows=0;

            try {
                returnedRows = this.howManyRows(resultSet);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            if (returnedRows >= 1) //czy w bazie istnieje juz takie konto
                return false;
            else{
                try {
                    preparedStatement = connect.prepareStatement("INSERT INTO OSOBY (IMIE, NAZWISKO, MIASTO, ULICA, NRDOMU, NRMIESZKANIA, KONTO, OSTRZEZENIA) VALUES (?, ?, ?, ?, ?, ?, ?, ?)", new String[]{"ID"});
                    preparedStatement.setString(1, data[1]);//register, name, lastname, email, password, city, street, houseNumber, apartmentNumber, bankAccount
                    preparedStatement.setString(2, data[2]);
                    preparedStatement.setString(3, data[5]);
                    preparedStatement.setString(4, data[6]);
                    preparedStatement.setString(5, data[7]);
                    preparedStatement.setInt(6, Integer.parseInt(data[8]));
                    preparedStatement.setLong(7, Long.parseLong(data[9]));
                    preparedStatement.setInt(8, 0);//warning level jest domyslnie ustawiany na 0


                    int result = preparedStatement.executeUpdate();
                    if(result == 0)
                        return false;


                    int idOfOsoby = 0;
                    ResultSet generatedKey = preparedStatement.getGeneratedKeys();
                    if(generatedKey.next()){
                        System.out.println(generatedKey.getString(1));
                        idOfOsoby = generatedKey.getInt(1);
                    }

                    if (idOfOsoby == 0)
                        return false;

                    preparedStatement = connect.prepareStatement("INSERT INTO KLIENCI (ID_OSOBY, EMAIL, HASLO) VALUES (?,?,?)");
                    preparedStatement.setInt(1, idOfOsoby);
                    preparedStatement.setString(2, data[3]);
                    preparedStatement.setString(3, data[4]);

                    preparedStatement.executeUpdate();

                } catch (SQLException e) {
                    e.printStackTrace();
                    return false;
                }

                return true;
            }
    }

    public synchronized String getName(String login, String password) throws SQLException {
        try {
            preparedStatement = connect.prepareStatement("SELECT imie, nazwisko FROM OSOBY, KLIENCI WHERE email = ? AND haslo=? AND OSOBY.ID=KLIENCI.ID_OSOBY");
            preparedStatement.setString(1, login);
            preparedStatement.setString(2, password);
            resultSet = preparedStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String result="";

        while (resultSet.next()){
            result = resultSet.getString("IMIE") + " "+resultSet.getString("NAZWISKO");
        }

        return result;
    }

    public synchronized String[] getClientHomeLocation(String login, String password) throws SQLException {
        try {
            preparedStatement = connect.prepareStatement("SELECT MIASTO, ULICA FROM OSOBY, KLIENCI WHERE email = ? AND HASLO=? AND OSOBY.ID=KLIENCI.ID_OSOBY");
            preparedStatement.setString(1, login);
            preparedStatement.setString(2, password);
            resultSet = preparedStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String[] result = new String[2];

        while (resultSet.next()){
            result[0] = resultSet.getString("MIASTO");
            result[1] = resultSet.getString("ULICA");
        }

        return result;
    }


    //sprawdzanie czy konto z passaim podanymi na ekranie logowania istnieje
    public synchronized boolean isAccount(String login, String password){
        if (login.length()==0 || password.length()==0)
            return false;
        try {
            preparedStatement = connect.prepareStatement("SELECT * FROM KLIENCI WHERE email = ? AND haslo=?");
            preparedStatement.setString(1, login);
            preparedStatement.setString(2, password);
            resultSet = preparedStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        int rows = 0;
        try {
            rows = this.howManyRows(resultSet);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (rows == 1)
            return true;
        else
            return false;

    }

    public synchronized int getIDOfCurrentClient(String login, String password) throws SQLException {

            preparedStatement = connect.prepareStatement("SELECT ID FROM KLIENCI WHERE email = ? AND haslo=?");
            preparedStatement.setString(1, login);
            preparedStatement.setString(2, password);
            resultSet = preparedStatement.executeQuery();


        int id = 0;

        while (resultSet.next()){
            id = resultSet.getInt("id");
        }

        return id;
    }

    public void conectToDataBase() {
        try {
            connect=DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:taxiDB", "admin", "admin");  //gdy wyskoczy timeZone excpetion to wykonaj skrypt sql 'SET GLOBAL time_zone = '+1:00';'

        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    private int getFreeDriver() throws SQLException {

        preparedStatement = connect.prepareStatement("SELECT * FROM KIEROWCY WHERE PRACUJE=?");
        preparedStatement.setString(1, "NIE");

        resultSet = preparedStatement.executeQuery();

        int idFreeDriver = 0;

        while (resultSet.next()){
            System.out.println(resultSet.getInt("ID"));
            idFreeDriver = resultSet.getInt("ID");
        }


        return idFreeDriver;
    }

    public synchronized boolean newRide(String from, String to, int clientID, Double price, LocalDate date, Long secondsDuration, LocalTime time) throws SQLException {
        preparedStatement = connect.prepareStatement("INSERT INTO PRZEJAZDY (ODBIOR, CEL) VALUES (?, ?)", new String[]{"ID"});
        preparedStatement.setString(1, from);
        preparedStatement.setString(2, to);

        int result = preparedStatement.executeUpdate();

        if(result == 0)
            return false;

        int idOfRide = 0;
        ResultSet generatedKey = preparedStatement.getGeneratedKeys();
        if(generatedKey.next()){
            idOfRide = generatedKey.getInt(1);
        }

        if (idOfRide == 0 )
            return false;

        int idOfFreeDriver = this.getFreeDriver();
        if (idOfFreeDriver == 0){
            return false;
        }

        preparedStatement = connect.prepareStatement("UPDATE KIEROWCY SET PRACUJE='TAK' WHERE ID=?");
        preparedStatement.setInt(1, idOfFreeDriver);
        preparedStatement.executeUpdate();

        preparedStatement = connect.prepareStatement("INSERT INTO REZERWACJE (ID_KIEROWCY, ID_KLIENTA, CENA, ID_ZAMOWIENIA, ID_PRZEJAZDU, DATA_ROZPOCZECIA, DATA_ZAKONCZENIA, CZAS_ROZPOCZECIA, CZAS_ZAKOCZENIA) VALUES (?, ?, ?, null, ?, ?, ?, ?, ?)");
        preparedStatement.setInt(1, idOfFreeDriver);                    //1           2        3                        4            5                   6                7                 8
        preparedStatement.setInt(2, clientID);
        preparedStatement.setDouble(3, price);
        preparedStatement.setInt(4, idOfRide);
        preparedStatement.setObject(5, date); //6 data zakoczenia , 8  czas zakonczenia
        preparedStatement.setObject(6, date);

        preparedStatement.setString(7, time.toString());
        LocalTime nowy = time.plusSeconds(secondsDuration);
        preparedStatement.setString(8,nowy.toString());


        try{
            int resultwynik = preparedStatement.executeUpdate();
        }
        catch(Exception ex){
            ex.printStackTrace();
        }


        return true;
    }


    public ArrayList<ArrayList>  getHistory(int userID) throws SQLException {
        System.out.println("1");
        preparedStatement = connect.prepareStatement("SELECT DISTINCT o.imie, o.nazwisko, p.odbior, p.cel, r.cena, r.data_rozpoczecia, r.czas_rozpoczecia, r.czas_zakoczenia \n" +
                "FROM osoby o, przejazdy p, rezerwacje r, klienci k, kierowcy kr\n" +
                "WHERE r.id_klienta = k.id AND  p.id = r.id_przejazdu AND kr.id_osoby = o.id AND K.ID=?");
        preparedStatement.setInt(1, userID);
        ResultSet rs = null;
        System.out.println("2");
        try{
            rs = preparedStatement.executeQuery();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        System.out.println("3");
        //ObservableList<ObservableList> data = FXCollections.observableArrayList();
        ArrayList<ArrayList> lista = new ArrayList<>();

        System.out.println("4");
        while (rs.next()) {
            System.out.println("4.5");
            //po wierszu
            //ObservableList<String> row = FXCollections.observableArrayList();
            ArrayList<String> row = new ArrayList<>();
            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                //po kolumnie
                System.out.println("4.8");
                row.add(rs.getString(i));
            }
            System.out.println("Row [1] added " + row);
            try{
                //data.add(row);
                lista.add(row);

            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
        System.out.println("5");

        //return data;
        return lista;
    }

    public void close() {
        System.out.println("baza danych zamykanie");
        try {
            if (resultSet != null) {
                resultSet.close();
            }

            if (statement != null) {
                statement.close();
            }

            if (connect != null) {
                connect.close();
            }

            if (preparedStatement != null)
                preparedStatement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}