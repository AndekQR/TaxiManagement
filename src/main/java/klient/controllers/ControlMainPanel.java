package klient.controllers;

import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.MapComponentInitializedListener;
import com.lynden.gmapsfx.javascript.object.DirectionsPane;
import com.lynden.gmapsfx.javascript.object.GoogleMap;
import com.lynden.gmapsfx.javascript.object.LatLong;
import com.lynden.gmapsfx.javascript.object.MapOptions;
import com.lynden.gmapsfx.service.directions.DirectionsRenderer;
import com.lynden.gmapsfx.service.directions.DirectionsRequest;
import com.lynden.gmapsfx.service.directions.DirectionsService;
import com.lynden.gmapsfx.service.directions.TravelModes;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import jfxtras.scene.control.CalendarTimePicker;
import klient.History;
import klient.Przejazd;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Date;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

import static com.lynden.gmapsfx.javascript.object.MapTypeIdEnum.ROADMAP;

public class ControlMainPanel implements MapComponentInitializedListener {

    private ViewManager manager;
    private ObjectInputStream objectInputStream = null;
    private ObjectOutputStream objectOutputStream = null;
    private final static String KEY = "YOUR_KEY";

    private Przejazd aktualnyPrzejazd = null;

    private DirectionsService directionsService;
    private DirectionsRequest request;
    private DirectionsPane directionsPane ;
    private DirectionsRenderer directionsRenderer = null;

    @FXML TextField name;
    @FXML GoogleMapView mapView;
    @FXML Pane nowyPrzejazdPane;
    @FXML Label kmPrzejazd;
    @FXML Label cenaPrzejazd;
    @FXML TextField from;
    @FXML TextField to;
    @FXML Label time;
    @FXML DatePicker date;
    @FXML CalendarTimePicker ride_time; //godzina wybrana
    @FXML Pane historiaPane;
    @FXML TableView historyTable;

    private void turnOffThePanes(){
        nowyPrzejazdPane.setVisible(false);
        nowyPrzejazdPane.setDisable(true);

        historiaPane.setVisible(false);
        historiaPane.setDisable(true);
    }

    @FXML
    private void nowyPrzejazdHandle() {
        this.turnOffThePanes();

        nowyPrzejazdPane.setVisible(true);
        nowyPrzejazdPane.setDisable(false);

    }

    @FXML
    private void edytujPrzejazdHandle() {

    }

    @FXML
    private void anulujPrzejazdHandle() {

    }

    @FXML
    private void historiaHandle() {
        this.turnOffThePanes();
        historiaPane.setDisable(false);
        historiaPane.setVisible(true);

        historyTable.getColumns().clear();

        History history = new History(manager);
        ObservableList<ObservableList> lista = null;
        try {
            lista = history.getHistoryData();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        historyTable.getColumns().addAll(history.getTableNames());
        try {
            historyTable.setItems(lista);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @FXML
    private void wylogujHandle(){
        manager.showLoginPanel();
    }

    @FXML
    private void sprawdzPrzejazdHandle(){
        Przejazd przejazd = new Przejazd(from.getText(), to.getText(), date.getValue(), manager);
        directionsRenderer.clearDirections();
        request = new DirectionsRequest(from.getText(), to.getText(), TravelModes.DRIVING);
        directionsRenderer = new DirectionsRenderer(true, mapView.getMap(), directionsPane);
        directionsService.getRoute(request, null, directionsRenderer);
        aktualnyPrzejazd = przejazd;

        try {
            przejazd.przelicz();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        DecimalFormat decimalFormat = new DecimalFormat("#.##");

        Long distanceKM = przejazd.getM()/1000;
        String distanceToShow = decimalFormat.format(distanceKM);

        Double price = przejazd.getPrice();
        String priceToShow = decimalFormat.format(price);

        cenaPrzejazd.setText(priceToShow + " zł");
        kmPrzejazd.setText(distanceToShow + " km");
        time.setText(przejazd.getDuration());
    }

    @FXML
    private void zarezerwujPrzejazd(){
        LocalDate startDateRide = date.getValue();
        int hours = ride_time.getCalendar().get(Calendar.HOUR_OF_DAY);
        int minutes = ride_time.getCalendar().get(Calendar.MINUTE);
        int seconds = ride_time.getCalendar().get(Calendar.SECOND);

        String trueHours, trueMinutes, trueSeconds;

        if (hours<10){
            trueHours = "0"+hours;
        }
        else
            trueHours = String.valueOf(hours);
        if (minutes<10){
            trueMinutes = "0"+minutes;
        }
        else
            trueMinutes = String.valueOf(minutes);
        if (seconds<10){
            trueSeconds = "0"+seconds;
        }
        else
            trueSeconds = String.valueOf(seconds);

        String result = trueHours+":"+trueMinutes+":"+trueSeconds;
        LocalTime startTimeRide = LocalTime.parse(result, DateTimeFormatter.ISO_LOCAL_TIME);


        aktualnyPrzejazd.setdataRozpoczecia(startDateRide);
        aktualnyPrzejazd.setGodzinaRozpoczecia(startTimeRide);

        try {
            aktualnyPrzejazd.confirm();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    public void setManager(ViewManager manager) {
        this.manager=manager;
    }

    private void setHeaderName() throws IOException, ClassNotFoundException {
        String[] order = {manager.HEADER_NAME};
        objectOutputStream.writeObject(order);

        String[] result=null;
        while ((result=(String[]) objectInputStream.readObject()) != null) {
            if (result[0].startsWith(manager.HEADER_NAME) && result[1].startsWith(manager.SUCCESS)) {
                name.setText(result[2]);
                break;
            } else if (result[0].startsWith(manager.HEADER_NAME) && result[1].startsWith(manager.FAIL)) {
                name.setText("Error");
                break;
            }
        }
        return;
    }

    private String[] getClientHomeLocation() throws IOException, ClassNotFoundException {
        String[] order = {manager.CLIENT_HOME};
        objectOutputStream.writeObject(order);


        String[] result=null;
        while ((result=(String[]) objectInputStream.readObject()) != null) {
            if (result[0].startsWith(manager.CLIENT_HOME) && result[1].startsWith(manager.SUCCESS)) {
                return result;
            } else if (result[0].startsWith(manager.CLIENT_HOME) && result[1].startsWith(manager.FAIL)) {
                return null;
            }
        }
        return null;
    }




    public void init() {
        mapView.setKey(KEY);
        mapView.addMapInializedListener(this);


        if (objectOutputStream == null || objectInputStream == null){
            objectInputStream = manager.getObjectInputStream();
            objectOutputStream = manager.getObjectOutputStream();
        }

        Runnable myRunnable = () -> {
            try {
                this.setHeaderName();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        };
        Thread thread = new Thread(myRunnable);
        thread.start();



    }

    @Override
    public void mapInitialized() {
        MapOptions options = new MapOptions();
        directionsService = new DirectionsService();


        /**
         * //zamiast Runnable nazwa = () bo jest tutaj aktualizowany interfejs aplikacji javyFX
         * Platform.runLater mówie aplikacji że może wykonać ten kawałek kodu gdy bedzie miała 'chwilę czasu'
         * inaczej w tym miejscu aplikacja zatrzymałaby się, ponieważ musi czekać na odpoiwedź od serwera
         *
         * Do wyświetlania mapy używany jest GMapsFX framwork, a po stronie serwera do obliczeń client google maps**/
        Platform.runLater(()->{
            String[] result = null;
            try {
                result = getClientHomeLocation();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            Double latitude = Double.valueOf(result[2]);
            Double longitude = Double.valueOf(result[3]);
            options.center(new LatLong(latitude, longitude))
                    .mapType(ROADMAP)
                    .overviewMapControl(false)
                    .panControl(false)
                    .rotateControl(false)
                    .scaleControl(false)
                    .streetViewControl(false)
                    .zoomControl(false)
                    .mapTypeControl(false)
                    .zoom(14);


            GoogleMap map = mapView.createMap(options);
            directionsPane = mapView.getDirec();
            directionsRenderer = new DirectionsRenderer(true, mapView.getMap(), directionsPane);
        });



    }
}
