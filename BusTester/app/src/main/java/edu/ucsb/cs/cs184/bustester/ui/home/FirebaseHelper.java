package edu.ucsb.cs.cs184.bustester.ui.home;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class FirebaseHelper {
    private static int global_count = 0;
    private static LatLng marker_location  = new LatLng(34.439891, -119.742728);
    private static LatLng user_location = new LatLng(34.434862, -119.848546);
    private static ArrayList<Stop> user_stop = new ArrayList<>();
    private static ArrayList<Marker> marker_arr = new ArrayList<>(); //Manage the markers
    private static ArrayList<Stop> current_stop_list;
    private static ArrayList<Stop> destination_stop_list;

    //Class objects for database
    public static class Stop implements Serializable {

        private Double latitude;
        private Double longitude;
        private String name;
        private int stop_id;


        public Stop(double longitude, double latitude, String name, int id){
            this.latitude = latitude;
            this.longitude = longitude;
            this.name = name;
            this.stop_id = id;
        }

        public Double getLatitude(){
            return latitude;
        }

        public Double getLongitude(){
            return longitude;
        }

        public String getName(){
            return name;
        }

        public int getStop_id(){
            return stop_id;
        }

        public Stop(){}
    }

    public static class geoPosition implements Serializable{
        private Double latitude;
        private Double longitude;

        public geoPosition(){}

        public Double getLatitude(){
            return latitude;
        }

        public Double getLongitude(){
            return longitude;
        }
    } //Similar to Latlon

    public static class Route implements Serializable {

        public String name_id;
        public String name;
        public String route_id;
        private ArrayList<String> Shape;

        public Route(String name, String id, String route_id, ArrayList<String> shape){
            this.name = name;
            this.name_id = id;
            this.route_id = route_id;
            this.Shape = shape;
        }

        public String getName(){
            return name;
        }

        public String getName_id(){
            return name_id;
        }

        public String getRoute_id(){
            return route_id;
        }

        public ArrayList<String> getShape(){return Shape;}

        public Route(){}
    }

    public static class Trip_Time implements Serializable {

        public String time;
        public String stop_id;
        public String stop_sequence;

        public Trip_Time(String time, String stop_id, String stop_sequence){
            this.time = time;
            this.stop_id = stop_id;
            this.stop_sequence = stop_sequence;
        }

        public String getTime(){
            return time;
        }

        public String getStop_id(){
            return stop_id;
        }

        public String getStop_sequence(){
            return stop_sequence;
        }

        public Trip_Time(){}
    }

    public static class Route_service implements Serializable{
        private String service_id;
        private String time;
        private String direction;
        private String route_name;
        private String route_id;

        public Route_service(String service_id, String time, String direction, String route_name, String route_id){
            this.service_id = service_id;
            this.time = time;
            this.direction = direction;
            this.route_name = route_name;
            this.route_id = route_id;
        }

        public Route_service(){}

        public String getService_id(){
            return service_id;
        }

        public String getTime(){
            return time;
        }

        public String getDirection(){return direction;}

        public String getRoute_name() {return route_name;}

        public String getRoute_id() {return route_id;}
    }

    public static class Trip implements Serializable {

        public String route_id;
        public String service_id;
        public String trip_id;
        public String direction;
        public String trip_headsign;
        public String shape_id;

        public Trip(String route_id, String service_id, String trip_id, String direction, String trip_headsign, String shape_id){
            this.route_id = route_id;
            this.service_id = service_id;
            this.trip_headsign = trip_headsign;
            this.direction = direction;
            this.shape_id = shape_id;
            this.trip_id = trip_id;
        }

        public String getService_id(){
            return service_id;
        }

        public String getTrip_id(){
            return trip_id;
        }

        public String getDirection(){
            return direction;
        }

        public String getTrip_headsign(){
            return trip_headsign;
        }

        public String getShape_id(){
            return shape_id;
        }

        public String getRoute_id(){
            return route_id;
        }

        public Trip(){}
    }

    private static boolean initialized = false;

    private static GoogleMap map; //Map Instance

    public static void getMap(GoogleMap mMap){
        map = mMap;
    } //Retrieve map from map activity

    /** The Firebase database object */
    private static FirebaseDatabase db;

    public static void Initialize(final Context context) {
        if (!initialized) {
            initialized = true;
            FirebaseApp.initializeApp(context);
            // Call the OnDatabaseInitialized to setup application logic
        }
        OnDatabaseInitialized();
    }

    /** This is called once we initialize the firebase database object */
    private static void OnDatabaseInitialized() {
        //final GenericTypeIndicator<ArrayList<String>> t = new GenericTypeIndicator<ArrayList<String>>() {};
        db = FirebaseDatabase.getInstance();
        DatabaseReference fb = db.getReference();
        DatabaseReference table = fb.child("Stops");
        ArrayList<String[]> outcome = new ArrayList<>();
        //outcome = Helper.parseData("https://openmobilitydata-data.s3-us-west-1.amazonaws.com/public/feeds/santa-barbara-mtd/1156/20191119/original/routes.txt");

        ArrayList<String> Route_list = new ArrayList<>();
        DatabaseReference stop = fb.child("Routes");

        ArrayList<String> depart_route = new ArrayList<>();
        ArrayList<String> destination_route = new ArrayList<>();
        Query order = table.orderByKey();
        LatLng current = new LatLng(34.434862, -119.848546);

        DatabaseReference local = table.child("Stop: 1"); //Temp

        local.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Stop s = dataSnapshot.getValue(Stop.class);
                Log.d("Long Message", "onDataChange: " + s.getStop_id());
                MarkerOptions marker = new MarkerOptions();
                marker.position(new LatLng(s.getLatitude(), s.getLongitude()));
                marker.title(s.stop_id + "");
                display_nearby_stop_helper(s.stop_id, map.addMarker(marker));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //getDirection(current, marker_location, 500);

        //displayNearByStop(current, 500);
    }


    //Time comparator, using for sort
    public static class TimeComparator implements Comparator<String>{
        @Override
        public int compare(String time1, String time2) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            try {
                Date d1 = sdf.parse(time1);
                Date d2 = sdf.parse(time2);
                if (d1.getTime() > d2.getTime()) {
                    return 1;
                }
                else if(d1.getTime() < d2.getTime()){
                    return -1;
                }
                else{
                    return 0;
                }

            } catch (Exception e) {
                Log.e("Error", "compare: " + e.getMessage());
            }
            return 0;
        }
    }
    //Display the whole route
    public static void setTripRoute(String shape_id){ //Display the trip route
        DatabaseReference shape = db.getReference().child("Shape").child("Trip: " + shape_id);
        Query order = shape.orderByKey();

        order.addListenerForSingleValueEvent(new ValueEventListener() {;
            @Override
            public void onDataChange(DataSnapshot data) {
                Random rnd = new Random();
                PolylineOptions option = new PolylineOptions().color(Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256)));
                for (DataSnapshot dataSnapshot : data.getChildren()) {
                    //Log.d("Long Message", "onDataChange: " + dataSnapshot.getValue());
                    geoPosition position = dataSnapshot.getValue(geoPosition.class);
                    //Log.d("Long Message", "onDataChange: " + position.getLongitude());
                    LatLng ll = new LatLng(position.getLatitude(), position.getLongitude());
                    option.add(ll);
                }
                map.addPolyline(option);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public static void displayNearByStop(final LatLng currentPosition, final int max_distance){ //distance in meters, find the nearby stops to the position
        DatabaseReference table = db.getReference().child("Stops");
        //final ArrayList<Stop> result_stop = new ArrayList<>();
        table.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren()){
                    Stop current_stop = data.getValue(Stop.class);
                    float[] results = new float[1];
                    Location.distanceBetween(currentPosition.latitude, currentPosition.longitude,current_stop.getLatitude(), current_stop.getLongitude(),results);
                    if(results[0] < max_distance){
                        MarkerOptions marker = new MarkerOptions();
                        marker.position(new LatLng(current_stop.getLatitude(), current_stop.getLongitude()));
                        marker.title(current_stop.stop_id + "");
                        display_nearby_stop_helper(current_stop.getStop_id(), map.addMarker(marker));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private static void display_nearby_stop_helper(int stop_id, final Marker m){
        Calendar cal = Calendar.getInstance();
        cal.setTime(cal.getTime());
        DatabaseReference schedule;
        DatabaseReference special_schedule;

        if(cal.get(Calendar.DAY_OF_WEEK) > 1 && cal.get(Calendar.DAY_OF_WEEK) < 7){ // Workdays
            schedule = db.getReference().child("Stops").child("Stop: " + stop_id).child("Weekday Service");
            special_schedule = db.getReference().child("Stops").child("Stop: " + stop_id).child("Special_Weekday Service");
        }
        else if(cal.get(Calendar.DAY_OF_WEEK) == 7){ //Saturday
            schedule = db.getReference().child("Stops").child("Stop: " + stop_id).child("Saturday Service");
            special_schedule = db.getReference().child("Stops").child("Stop: " + stop_id).child("Special_Saturday Service");
        }
        else{ //Sunday
            schedule = db.getReference().child("Stops").child("Stop: " + stop_id).child("Sunday Service");
            special_schedule = db.getReference().child("Stops").child("Stop: " + stop_id).child("Special_Sunday Service");
        }

        final GenericTypeIndicator<ArrayList<Route_service>> t = new GenericTypeIndicator<ArrayList<Route_service>>() {};
        final ArrayList<Route_service> outcome = new ArrayList<>();
        final int[] counter = {0};

        schedule.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount() != 0){
                    outcome.addAll(dataSnapshot.getValue(t));
                }
                counter[0]++;
//                display_nearby_stop_finish(outcome, counter[0], m);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        special_schedule.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount() != 0){
                    outcome.addAll(dataSnapshot.getValue(t));
                }
                counter[0]++;
//                display_nearby_stop_finish(outcome, counter[0], m);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

//    private static void display_nearby_stop_finish(ArrayList<Route_service> outcome, int indicator, Marker m){
//        if(indicator == 2){ //All outcome added
//            if(m != null){
//                StopInfoClass info = new StopInfoClass();
//                info.setTime_schedule(outcome);
//                m.setTag(info);
//            }
//            else{
//                //Define your function there
//            }
//        }
//    }

    public static void display_specific_route(ArrayList<String> service_id){ //Helper function for infoWindowDialog: display the route shape and stops when a route's textview is clicked
        map.clear();
        for(int i = 0; i < service_id.size(); i++){
            DatabaseReference service = db.getReference().child("Trips").child("Trip: " + service_id.get(i));
            Log.d("Long Message", "onDataChange: " + service_id);
            service.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Trip t = dataSnapshot.getValue(Trip.class);
                    //Log.d("Long Message", "onDataChange: " + t.shape_id);
                    setTripRoute(t.shape_id);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            final GenericTypeIndicator<ArrayList<Trip_Time>> t = new GenericTypeIndicator<ArrayList<Trip_Time>>() {};
            DatabaseReference trip_time = db.getReference().child("Trip_Time").child("Trip: " + service_id.get(i));
            trip_time.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    ArrayList<Trip_Time> trip = dataSnapshot.getValue(t); //Index starts from 1
                    ArrayList<String> stop = new ArrayList<>();
                    for(int i = 0; i < trip.size(); i++){
                        if(trip.get(i) != null){
                            stop.add(trip.get(i).getStop_id());
                        }
                    }
                    show_route_stop_helper(stop);
                    //show_transfer_stop_helper(stop);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }

    private static void show_route_stop_helper(ArrayList<String> stop_id_arr){
        for(int i = 0; i < stop_id_arr.size(); i++){
            DatabaseReference stop = db.getReference().child("Stops").child("Stop: " + stop_id_arr.get(i));
            stop.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Stop s = dataSnapshot.getValue(Stop.class);
                    MarkerOptions marker = new MarkerOptions();
                    marker.position(new LatLng(s.getLatitude(), s.getLongitude()));
                    marker.title("Route Marker" + s.stop_id); //Special name route marker

                    display_nearby_stop_helper(s.getStop_id(), map.addMarker(marker));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

//    private static void find_routes_between_two_marker(Marker start, Marker end){
//        StopInfoClass start_info = (StopInfoClass)start.getTag();
//        StopInfoClass end_info = (StopInfoClass)end.getTag();
//
//        HashMap<String, Route_service> close_start_service = new HashMap<>();
//        HashMap<String, ArrayList<Route_service>> target_end_service = new HashMap<>();
//        ArrayList<Route_service> start_service = start_info.getTime_schedule();
//        ArrayList<Route_service> end_service = end_info.getTime_schedule();
//
//        Calendar now = Calendar.getInstance();
//        String current_time = now.get(Calendar.HOUR_OF_DAY) + ":" + now.get(Calendar.MINUTE) + ":" + now.get(Calendar.SECOND);
//
//        //Calculate the distance to estimate time? Temp
//        //float[] results = new float[1];
//        //Location.distanceBetween(currentPosition.latitude, currentPosition.longitude,current_stop.getLatitude(), current_stop.getLongitude(),results);
//
//        for(int i = 0; i < start_service.size(); i++){
//            int remaining = InfoWindowDialog.compareTime(current_time, start_service.get(i).getTime());
//            if(remaining > 4){ //Temp Range
//                if(close_start_service.get(start_service.get(i).getRoute_id()) == null){
//                    close_start_service.put(start_service.get(i).getRoute_id(), start_service.get(i));
//                }
//            }
//        }
//
//        //All unique start route service info are in the hashmap: close_start_service (Key is route id, value is the Route service value)
//        for(int j = 0; j < end_service.size(); j++){
//            int remaining = InfoWindowDialog.compareTime(current_time, end_service.get(j).getTime());
//            if(remaining > 0){ //Temp Range
//                if(target_end_service.get(end_service.get(j).getRoute_id()) != null){
//                    ArrayList<Route_service> single_route_service = new ArrayList<>();
//                    single_route_service.add(end_service.get(j));
//                    target_end_service.put(end_service.get(j).getRoute_id(), single_route_service);
//                }
//                else{
//                    target_end_service.get(end_service.get(j)).add(end_service.get(j));
//                }
//            }
//        }
//
//        //All possible end service are in the hashmap: target_end_service
//        ArrayList<String> possible_keys = new ArrayList<>();
//        for(String s : close_start_service.keySet()){
//            if(target_end_service.containsKey(s)){
//                possible_keys.add(s);
//            }
//        }
//
//        if(possible_keys.size() != 0){ //Have direct bus connecting two stops
//            Log.d("Long Message", "find_routes_between_two_marker: " + possible_keys.get(0) + " size " + possible_keys.size());
//        }
//        else{ //We need to find the transit
//            ArrayList<Route_service> route_services = new ArrayList<>(close_start_service.values());
//            ArrayList<String> target_key_set = new ArrayList<>(target_end_service.keySet());
//            find_transit(route_services,target_key_set);
//        }
//    }

    //Helper function to find transit between two markers (Currently, maximum one transit)
    private static void find_transit(ArrayList<Route_service> current_service, ArrayList<String> target_route_name){
        for(int i = 0; i < current_service.size(); i++){
            DatabaseReference single_service = db.getReference().child("Trip_Time").child("Trip: " + current_service.get(i).getService_id());
        }

    }
}
