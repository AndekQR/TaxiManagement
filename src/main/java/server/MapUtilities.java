package server;

import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.appengine.repackaged.com.google.gson.GsonBuilder;
import com.google.maps.*;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.TravelMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class MapUtilities {

    /**
     * Wykorzystywany klient google maps od google, ponieważ w GMapsFX nie ma odpowiednich narzędzi do obliczania odległości itd.**/

    private static final String API_KEY = "AIzaSyCTG9ziQFOt1ZxlPxMtzr_7T1zTCNFAmMo";
    private static GeoApiContext
            context = new GeoApiContext.Builder()
            .apiKey(API_KEY)
            .queryRateLimit(50)
            .build();


    public synchronized Double[] geocode(String... address) throws InterruptedException, ApiException, IOException, JSONException {

        String finalAddress="";
        for (int i=0; i < address.length; i++) {
            finalAddress = finalAddress + address[i] + " ";
        }
        GeocodingResult[] results =  GeocodingApi.geocode(context, finalAddress).await();
        String latLong = "";
        if(results != null && results.length > 0) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            latLong = gson.toJson(results[0].geometry);
            JSONObject jsonObject = new JSONObject(latLong);
            JSONObject location = jsonObject.getJSONObject("location");
            double latitude = location.getDouble("lat");
            double longitude = location.getDouble("lng");

            Double[] latlong = {latitude, longitude};

            return latlong;
        }
        return  null;
    }

    public synchronized Long getDistance(String from, String to) throws InterruptedException, ApiException, IOException {
        DistanceMatrixApiRequest distanceMatrixApiRequest = DistanceMatrixApi.newRequest(context);
        DistanceMatrix result = distanceMatrixApiRequest
                .origins(from)
                .destinations(to)
                .mode(TravelMode.DRIVING)
                .avoid(DirectionsApi.RouteRestriction.TOLLS)
                .language("en-US")
                .await();

        Long distance = result.rows[0].elements[0].distance.inMeters;

        return distance;
    }

    public synchronized String[] getDuration(String from, String to) throws InterruptedException, ApiException, IOException {
        DistanceMatrixApiRequest distanceMatrixApiRequest = DistanceMatrixApi.newRequest(context);
        DistanceMatrix result = distanceMatrixApiRequest
                .origins(from)
                .destinations(to)
                .mode(TravelMode.DRIVING)
                .avoid(DirectionsApi.RouteRestriction.TOLLS)
                .language("en-US")
                .await();

        String[] time = {result.rows[0]. elements[0].duration.humanReadable, String.valueOf(result.rows[0]. elements[0].duration.inSeconds)};


        return time;
    }
}
