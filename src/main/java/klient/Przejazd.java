package klient;

import klient.controllers.ViewManager;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;

public class Przejazd {
    private String odbior;
    private String cel;
    private LocalDate dataRozpoczecie;
    private LocalTime godzinaRozpoczecia;
    private ViewManager manager;
    private ObjectInputStream objectInputStream = null;
    private ObjectOutputStream objectOutputStream = null;

    Double price;
    Long km;
    String czas_przejazdu;

    public Double getPrice(){
        return price;
    }

    public Long getM(){
        return km;
    }

    public String getDuration(){
        return czas_przejazdu;
    }

    public void setdataRozpoczecia(LocalDate date){
        this.dataRozpoczecie = date;
    }

    public void setGodzinaRozpoczecia(LocalTime time){
        this.godzinaRozpoczecia = time;
    }


    public Przejazd(String odbior, String cel, LocalDate czasRozpoczacia, ViewManager manager){
        this.odbior = odbior;
        this.cel = cel;
        this.dataRozpoczecie = czasRozpoczacia;
        this.manager = manager;

        objectInputStream = manager.getObjectInputStream();
        objectOutputStream = manager.getObjectOutputStream();
    }

    public void przelicz() throws IOException, ClassNotFoundException {
        String[] order = {manager.PRICE, odbior, cel};
        objectOutputStream.writeObject(order);


        String[] result=null;
        while ((result=(String[]) objectInputStream.readObject()) != null) {
            if (result[0].startsWith(manager.PRICE) && result[1].startsWith(manager.SUCCESS)) {
                price = Double.valueOf(result[3]);
                km = Long.valueOf(result[2]);
                czas_przejazdu = result[4];
                break;
            } else if (result[0].startsWith(manager.PRICE) && result[1].startsWith(manager.FAIL)) {
                System.err.println("Przejazd: result = fail PRICE");
                break;
            }
        }

    }

    public boolean confirm() throws IOException, ClassNotFoundException {
        String[] order = {manager.RIDE, odbior, cel, String.valueOf(dataRozpoczecie), String.valueOf(godzinaRozpoczecia)};
        objectOutputStream.writeObject(order);

        String[] result=null;
        while ((result=(String[]) objectInputStream.readObject()) != null) {
            if (result[0].startsWith(manager.RIDE) && result[1].startsWith(manager.SUCCESS)) {
                    return true;
            } else if (result[0].startsWith(manager.RIDE) && result[1].startsWith(manager.FAIL)) {
                System.err.println("Przejazd: result = fail RIDE");
                    return false;
            }
        }
        return false;
    }


}
