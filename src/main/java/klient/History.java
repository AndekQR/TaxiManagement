package klient;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import klient.controllers.ViewManager;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class History {

    private TableColumn[] columnNames ={new TableColumn("Imie kierowcy"),
            new TableColumn("nazwisko kierowcy"),
            new TableColumn("skąd"),
            new TableColumn("dokąd"),
            new TableColumn("cena"),
            new TableColumn("data"),
            new TableColumn("rozpoczęcie"),
            new TableColumn("zakończenie")} ;

    private ViewManager manager;
    private ObjectInputStream objectInputStream = null;
    private ObjectOutputStream objectOutputStream = null;

    public History(ViewManager manager){
        this.manager=manager;

        objectInputStream = manager.getObjectInputStream();
        objectOutputStream = manager.getObjectOutputStream();
    }

    public TableColumn[] getTableNames(){
        return columnNames;
    }

    public ObservableList getHistoryData() throws IOException, ClassNotFoundException {
        String[] order = {manager.HISTORY_CONTENT};
        objectOutputStream.writeObject(order);

        Object result=null;
        while ((result=objectInputStream.readObject()) != null) {
            ArrayList<ArrayList<String>> array = (ArrayList<ArrayList<String>>) result;
            //ObservableList<ObservableList<String>> list = FXCollections.<ObservableList<String>>observableArrayList(result)>;
            ObservableList<ObservableList> data = FXCollections.observableArrayList();


            for(int i=0; i<array.size(); i++){
                ObservableList<String> row = FXCollections.observableArrayList();
                for(int j=0; j<array.get(i).size(); j++){
                    System.out.println(array.get(i).get(j));
                    row.add(array.get(i).get(j));
                }
                data.add(row);
           }
            return  data;
        }
        return null;
    }
}
