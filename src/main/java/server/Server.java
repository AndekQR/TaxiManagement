package server;


import javafx.collections.ObservableList;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private boolean running = false;
    private DataBase dataBase = null;

    public static void main(String[] args) {
        new Server().startServer();
    }

    public void startServer() {

        dataBase = new DataBase();
        dataBase.conectToDataBase();

        if (running == false){
        final ExecutorService clientProcessingPool = Executors.newFixedThreadPool(10);
        running = true;

        Runnable serverTask = () -> {
            try {
                ServerSocket serverSocket = new ServerSocket(8000);
                System.out.println("Waiting for clients to connect...");
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    clientProcessingPool.submit(new ClientTask(clientSocket));
                }
            } catch (IOException e) {
                System.err.println("Unable to process client request");
                e.printStackTrace();
            }
            finally {
                dataBase.close();
            }
        };
        Thread serverThread = new Thread(serverTask);
        System.out.println("Server started!");
        serverThread.start();
    }

    }

    private class ClientTask implements Runnable {

        private static final String LOGIN = "login";
        private static final String SUCCESS = "success";
        private static final String FAIL = "fail";
        private static final String REGISTER="register";
        private static final String HEADER_NAME = "headerName";
        private static final String CLIENT_HOME = "clientHome";
        private final String PRICE = "price";
        private final String RIDE = "ride"; //dla przejazdów
        private final String HISTORY_CONTENT = "history_content";

        public final Double PRICEPERKM = 2.1;

        private final Socket clientSocket;
        private ObjectOutputStream objectOutputStream = null;
        private ObjectInputStream objectInputStream = null;
        private boolean currentClientThreadState = false;
        private MapUtilities mapUtilities = null;

        private ClientTask(Socket clientSocket) {
            this.clientSocket = clientSocket;

            mapUtilities = new MapUtilities();

            try {
                objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                objectOutputStream.flush();
                objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

            currentClientThreadState = true;
        }


        public void disconnect(){
            try{
                objectOutputStream.close();
                objectInputStream.close();
            } catch (IOException e) {
                objectOutputStream = null;
                objectInputStream = null;
                e.printStackTrace();
            }

            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            currentClientThreadState = false;
            System.out.println("Client "+Thread.currentThread().getName()+" just disconnect");
            Thread.currentThread().interrupt();
        }

        @Override
        public void run() {
            System.out.println("Got a client !");
            while (currentClientThreadState && clientSocket.isConnected()){
                try {
                    /**
                     * definiowane w chwili zalogowania się klienta
                     * potrzebne do odnajdywania klienta w bazie danych**/
                    String currentUserLogin = "";
                    String currentUserPassword = "";
                    int currentUserid = 0;

                    String[] line;
                    while ((line =(String[]) objectInputStream.readObject())!=null){

                        /**
                         * Klient wysyła do serwera rozkazy w postaci [0] - czego dotyczy rozkaz a w pozostałych komórkach tablicy sa argumenty
                         * Serwer odpowiada komunikatem w postaci: [0] - czego dotyczy komunikat, [1] - status wykonanej operacji, i w pozostałych ewentualne argumenty
                         * **/
                        switch (line[0]){
                            case LOGIN:
                                if (line.length<3) //przypadek gdy nie podano wszystkich danych
                                    objectOutputStream.writeObject(new String[]{LOGIN, FAIL});
                                else if (dataBase.isAccount(line[1], line[2])){//1 i 2 bo w 0 jest komunikat o logowaniu
                                    currentUserLogin = line[1];
                                    currentUserPassword = line[2];
                                    currentUserid = dataBase.getIDOfCurrentClient(line[1], line[2]);
                                    if (currentUserid == 0)
                                        System.err.println("Brak id aktualnego usera!!!");
                                    objectOutputStream.writeObject(new String[]{LOGIN, SUCCESS});
                                }

                                else
                                    objectOutputStream.writeObject(new String[]{LOGIN, FAIL});
                                break;
                            case REGISTER: //powinno być 10 elementow w tablicy
                                if(line.length<10)
                                    objectOutputStream.writeObject(new String[]{REGISTER, FAIL});
                                else if (dataBase.createAccount(line))
                                    objectOutputStream.writeObject(new String[]{REGISTER, SUCCESS});
                                else
                                    objectOutputStream.writeObject(new String[]{REGISTER, FAIL});

                                break;
                            case HEADER_NAME:// [HEADER_NAME], [login], [password]
                                    if(line.length>0){
                                        String name = dataBase.getName(currentUserLogin, currentUserPassword);
                                        if(name.length()>0)
                                            objectOutputStream.writeObject(new String[]{HEADER_NAME, SUCCESS, name});
                                        else
                                            objectOutputStream.writeObject(new String[]{HEADER_NAME,FAIL});
                                    }
                                    else
                                        objectOutputStream.writeObject(new String[]{HEADER_NAME,FAIL});

                                break;
                            case CLIENT_HOME:
                                if(line.length==1){
                                    String[] location = dataBase.getClientHomeLocation(currentUserLogin, currentUserPassword);
                                    Double[] latlong = mapUtilities.geocode(location);
                                    if(location.length == 2)
                                        objectOutputStream.writeObject(new String[]{CLIENT_HOME, SUCCESS, String.valueOf(latlong[0]), String.valueOf(latlong[1])});
                                    else
                                        objectOutputStream.writeObject(new String[]{CLIENT_HOME,FAIL});
                                }
                                else
                                    objectOutputStream.writeObject(new String[]{CLIENT_HOME,FAIL});

                                break;
                            case PRICE:
                                if(line.length == 3){
                                    Long distance = mapUtilities.getDistance(line[1], line[2]);
                                    Double price = (distance/1000) * PRICEPERKM; //bo jest w metrach
                                    String[] duration = mapUtilities.getDuration(line[1], line[2]);  //zwraca czas do czytania, czas w sekundach

                                    objectOutputStream.writeObject(new String[]{PRICE, SUCCESS, distance.toString(), price.toString(), duration[0]});
                                }
                                else{
                                    objectOutputStream.writeObject(new String[]{PRICE, FAIL});
                                }
                                break;
                            case RIDE:
                                    if (line.length == 5){ //ride, from, to,  data_rozpoczecia, godzinaRozpoczecia
                                        Long distance = mapUtilities.getDistance(line[1], line[2]);
                                        Double price = (distance/1000) * PRICEPERKM;
                                        String[] duration = mapUtilities.getDuration(line[1], line[2]);

                                        LocalDate rozpoczecie = LocalDate.parse(line[3]);
                                        LocalTime czasRozpoczecie = LocalTime.parse(line[4]);

                                       if( dataBase.newRide(line[1], line[2], currentUserid,  price, rozpoczecie, Long.valueOf(duration[1]), czasRozpoczecie)){
                                           objectOutputStream.writeObject(new String[]{RIDE, SUCCESS});
                                       }
                                       else{
                                           objectOutputStream.writeObject(new String[]{RIDE, FAIL});
                                       }

                                    }
                                    else{
                                        objectOutputStream.writeObject(new String[]{RIDE, FAIL});
                                    }
                                break;
                            case HISTORY_CONTENT:
                                if (line.length==1){
                                    ArrayList<ArrayList>  lista = dataBase.getHistory(currentUserid);


                                    System.out.println("wysylanie obiektu");
                                    objectOutputStream.writeObject(lista);
                                }
                                else
                                    objectOutputStream.writeObject(null);
                                break;
                        }

                    }
                } catch (Exception e) {
                    //e.printStackTrace(); //wywoływany zawsze gdy klient rozłącza się
                }
                finally {
                    disconnect();
                }

            }

        }
    }

}