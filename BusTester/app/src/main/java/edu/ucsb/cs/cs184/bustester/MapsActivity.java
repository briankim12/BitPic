package edu.ucsb.cs.cs184.bustester;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static GoogleMap map;
    private final int Access_Fine_Location = 1001;
    private final int Internet = 1002;
    private final int ERROR_DIALOG_REQUEST = 1003;
    private final int PERMISSIONS_REQUEST_ENABLE_GPS = 1004;
    private final String CHANNEL_ID = "nearby_stop_notification";
    private final int NOTIFICATION_ID = 002;
    private static float marker_user_distance = 0;
    private static String user_defined_time = "";
    private static boolean mLocationPermissionGranted = false;
    private static boolean send_notification_indicator = false;
    private static boolean fab_expanded = false;
    private static boolean transit_result_returned = false;
    private FragmentManager fm = getSupportFragmentManager();
    private FusedLocationProviderClient user_location_client;
    private static FirebaseDatabase db;
    private static HashMap<String, Stop> all_stops = new HashMap<>();
    private static ArrayList<Marker> find_transit_helper_arr = new ArrayList<>(); // Used for putting the two target markers
    private static boolean find_route_mode = false;
    private static ArrayList<Marker> find_route_marker_arr = new ArrayList<>();
    private static ArrayList<Polyline> find_route_polyline_arr = new ArrayList<>();
    private static ArrayList<Marker> find_transit_route_marker_arr = new ArrayList<>();
    private static ArrayList<Marker> user_marker = new ArrayList<>();
    private static ArrayList<Marker> user_marker_count = new ArrayList<>();
    private static String notification_stop_id = "";
    private static LatLng user_location;

    public static class Stop implements Serializable {

        private Double latitude;
        private Double longitude;
        private String name;
        private int stop_id;

        public Stop() {
        }

        public Stop(double longitude, double latitude, String name, int id) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.name = name;
            this.stop_id = id;
        }

        public Double getLatitude() {
            return latitude;
        }

        public Double getLongitude() {
            return longitude;
        }

        public String getName() {
            return name;
        }

        public int getStop_id() {
            return stop_id;
        }

    }

    public static class geoPosition implements Serializable {
        private Double latitude;
        private Double longitude;

        public geoPosition() {
        }

        public Double getLatitude() {
            return latitude;
        }

        public Double getLongitude() {
            return longitude;
        }
    } //Similar to Latlon

    public static class Route implements Serializable {

        public String name_id;
        public String name;
        public String route_id;
        private ArrayList<String> Shape;

        public Route(String name, String id, String route_id, ArrayList<String> shape) {
            this.name = name;
            this.name_id = id;
            this.route_id = route_id;
            this.Shape = shape;
        }

        public String getName() {
            return name;
        }

        public String getName_id() {
            return name_id;
        }

        public String getRoute_id() {
            return route_id;
        }

        public ArrayList<String> getShape() {
            return Shape;
        }

        public Route() {
        }
    }

    public static class Trip_Time implements Serializable {

        public String time;
        public String stop_id;
        public String stop_sequence;

        public Trip_Time(String time, String stop_id, String stop_sequence) {
            this.time = time;
            this.stop_id = stop_id;
            this.stop_sequence = stop_sequence;
        }

        public String getTime() {
            return time;
        }

        public String getStop_id() {
            return stop_id;
        }

        public String getStop_sequence() {
            return stop_sequence;
        }

        public Trip_Time() {
        }
    }

    public static class Route_service implements Serializable {
        private String service_id;
        private String time;
        private String direction;
        private String route_name;
        private String route_id;

        public Route_service(String service_id, String time, String direction, String route_name, String route_id) {
            this.service_id = service_id;
            this.time = time;
            this.direction = direction;
            this.route_name = route_name;
            this.route_id = route_id;
        }

        public Route_service() {
        }

        public String getService_id() {
            return service_id;
        }

        public String getTime() {
            return time;
        }

        public String getDirection() {
            return direction;
        }

        public String getRoute_name() {
            return route_name;
        }

        public String getRoute_id() {
            return route_id;
        }
    }

    public static class Trip implements Serializable {

        public String route_id;
        public String service_id;
        public String trip_id;
        public String direction;
        public String trip_headsign;
        public String shape_id;

        public Trip(String route_id, String service_id, String trip_id, String direction, String trip_headsign, String shape_id) {
            this.route_id = route_id;
            this.service_id = service_id;
            this.trip_headsign = trip_headsign;
            this.direction = direction;
            this.shape_id = shape_id;
            this.trip_id = trip_id;
        }

        public String getService_id() {
            return service_id;
        }

        public String getTrip_id() {
            return trip_id;
        }

        public String getDirection() {
            return direction;
        }

        public String getTrip_headsign() {
            return trip_headsign;
        }

        public String getShape_id() {
            return shape_id;
        }

        public String getRoute_id() {
            return route_id;
        }

        public Trip() {
        }
    }

    private void collectBasic_Stop_info() {
        DatabaseReference stop = db.getReference().child("Basic_Stops");
        stop.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Stop stop = data.getValue(Stop.class);
                    all_stops.put(Integer.toString(stop.stop_id), stop);
                }
                do_operations();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void do_operations() { //Define all your operations during initialization at there!
        DatabaseReference fb = db.getReference();
        DatabaseReference table = fb.child("Basic_Stops");
        ArrayList<String[]> outcome = new ArrayList<>();
        //outcome = Helper.parseData("https://openmobilitydata-data.s3-us-west-1.amazonaws.com/public/feeds/santa-barbara-mtd/1156/20191119/original/routes.txt");

        ArrayList<String> Route_list = new ArrayList<>();
        DatabaseReference stop = fb.child("Routes");

        ArrayList<String> depart_route = new ArrayList<>();
        ArrayList<String> destination_route = new ArrayList<>();
        Query order = table.orderByKey();
        LatLng current = new LatLng(user_location.latitude, user_location.longitude);
        displayNearByStop(current, 450, 0);

        /*DatabaseReference local = table.child("Stop: 180"); //Temp

        local.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Stop s = dataSnapshot.getValue(Stop.class);
                Log.d("Long Message", "onDataChange: " + s.getStop_id());
                MarkerOptions marker = new MarkerOptions();
                marker.position(new LatLng(s.getLatitude(), s.getLongitude()));
                marker.title("User Nearby stop:" + s.stop_id);
                display_nearby_stop_helper(s.stop_id, map.addMarker(marker));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/

        /*local = table.child("Stop: 371");
        local.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Stop s = dataSnapshot.getValue(Stop.class);
                Log.d("Long Message", "onDataChange: " + s.getStop_id());
                MarkerOptions marker = new MarkerOptions();
                marker.position(new LatLng(s.getLatitude(), s.getLongitude()));
                marker.title("User Nearby stop:" + s.stop_id);
                display_nearby_stop_helper(s.stop_id, map.addMarker(marker));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/

    }

    public static void setTripRoute(String shape_id) { //Display the trip route
        DatabaseReference shape = db.getReference().child("Shape").child("Trip: " + shape_id);
        Query order = shape.orderByKey();

        order.addListenerForSingleValueEvent(new ValueEventListener() {
            ;

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

    public void displayNearByStop(final LatLng currentPosition, final int max_distance, int indicator) { //distance in meters, find the nearby stops to the position, indicator for behavior control
        for (String key : all_stops.keySet()) {
            Stop current_stop = all_stops.get(key);
            float[] results = new float[1];
            Location.distanceBetween(currentPosition.latitude, currentPosition.longitude, current_stop.getLatitude(), current_stop.getLongitude(), results);
            if (results[0] < max_distance) {
                MarkerOptions marker = new MarkerOptions();
                marker.position(new LatLng(current_stop.getLatitude(), current_stop.getLongitude()));
                marker.title("Stop:" + current_stop.stop_id);
                Marker m = map.addMarker(marker);
                if (indicator == 1) {
                    user_marker.add(m);
                }
                display_nearby_stop_helper(current_stop.getStop_id(), m);
            }
        }
    }

    private static void display_nearby_stop_helper(int stop_id, final Marker m) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(cal.getTime());
        DatabaseReference schedule;
        DatabaseReference special_schedule;

        if (cal.get(Calendar.DAY_OF_WEEK) > 1 && cal.get(Calendar.DAY_OF_WEEK) < 7) { // Workdays
            schedule = db.getReference().child("Stops").child("Stop: " + stop_id).child("Weekday Service");
            special_schedule = db.getReference().child("Stops").child("Stop: " + stop_id).child("Special_Weekday Service");
        } else if (cal.get(Calendar.DAY_OF_WEEK) == 7) { //Saturday
            schedule = db.getReference().child("Stops").child("Stop: " + stop_id).child("Saturday Service");
            special_schedule = db.getReference().child("Stops").child("Stop: " + stop_id).child("Special_Saturday Service");
        } else { //Sunday
            schedule = db.getReference().child("Stops").child("Stop: " + stop_id).child("Sunday Service");
            special_schedule = db.getReference().child("Stops").child("Stop: " + stop_id).child("Special_Sunday Service");
        }

        final GenericTypeIndicator<ArrayList<Route_service>> t = new GenericTypeIndicator<ArrayList<Route_service>>() {
        };
        final ArrayList<Route_service> outcome = new ArrayList<>();
        final int[] counter = {0};

        schedule.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() != 0) {
                    outcome.addAll(dataSnapshot.getValue(t));
                }
                counter[0]++;
                display_nearby_stop_finish(outcome, counter[0], m);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        special_schedule.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() != 0) {
                    outcome.addAll(dataSnapshot.getValue(t));
                }
                counter[0]++;
                display_nearby_stop_finish(outcome, counter[0], m);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private static void display_nearby_stop_finish(ArrayList<Route_service> outcome, int indicator, Marker m) {
        if (indicator == 2) { //All outcome added
            if (m != null) {
                StopInfoClass info = new StopInfoClass();
                info.setTime_schedule(outcome);
                m.setTag(info);
            } else {
                //Define your function there
            }
        }
    }

    public static void display_specific_route(ArrayList<String> service_id) { //Helper function for infoWindowDialog: display the route shape and stops when a route's textview is clicked
        map.clear();
        find_route_marker_arr.clear();
        find_transit_helper_arr.clear();
        for (int i = 0; i < service_id.size(); i++) {
            DatabaseReference service = db.getReference().child("Trips").child("Trip: " + service_id.get(i));
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

            final GenericTypeIndicator<ArrayList<Trip_Time>> t = new GenericTypeIndicator<ArrayList<Trip_Time>>() {
            };
            DatabaseReference trip_time = db.getReference().child("Trip_Time").child("Trip: " + service_id.get(i));
            trip_time.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    ArrayList<Trip_Time> trip = dataSnapshot.getValue(t); //Index starts from 1
                    ArrayList<String> stop = new ArrayList<>();
                    for (int i = 0; i < trip.size(); i++) {
                        if (trip.get(i) != null) {
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

    private static void show_route_stop_helper(ArrayList<String> stop_id_arr) {
        for (int i = 0; i < stop_id_arr.size(); i++) {
            DatabaseReference stop = db.getReference().child("Stops").child("Stop: " + stop_id_arr.get(i));
            stop.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Stop s = dataSnapshot.getValue(Stop.class);
                    MarkerOptions marker = new MarkerOptions();
                    marker.position(new LatLng(s.getLatitude(), s.getLongitude()));
                    marker.title("Route Marker:" + s.stop_id); //Special name route marker

                    display_nearby_stop_helper(s.getStop_id(), map.addMarker(marker));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    public void find_routes_between_two_marker(Marker start, Marker end) {
        Log.d("Long Message", "find_routes_between_two_marker: " + "Called");
        find_transit_helper_arr.add(start);
        find_transit_helper_arr.add(end);
        StopInfoClass start_info = (StopInfoClass) start.getTag();
        StopInfoClass end_info = (StopInfoClass) end.getTag();

        if (start_info == null || end_info == null) {
            clear_all();
            return;
        }

        HashMap<String, Route_service> close_start_service = new HashMap<>();
        HashMap<String, ArrayList<Route_service>> target_end_service = new HashMap<>();
        ArrayList<Route_service> start_service = start_info.getTime_schedule();
        ArrayList<Route_service> end_service = end_info.getTime_schedule();

        String current_time;
        if (user_defined_time.length() > 2) {
            current_time = user_defined_time;
        } else {
            Calendar now = Calendar.getInstance();
            current_time = now.get(Calendar.HOUR_OF_DAY) + ":" + now.get(Calendar.MINUTE) + ":" + now.get(Calendar.SECOND);
        }

        //Calculate the distance to estimate time? Temp
        //float[] results = new float[1];
        //Location.distanceBetween(currentPosition.latitude, currentPosition.longitude,current_stop.getLatitude(), current_stop.getLongitude(),results);

        for (int i = 0; i < start_service.size(); i++) {
            int remaining = InfoWindowDialog.compareTime(current_time, start_service.get(i).getTime());
            if (remaining > 2) { //Temp Range
                if (close_start_service.get(start_service.get(i).getRoute_id()) == null) {
                    close_start_service.put(start_service.get(i).getRoute_id(), start_service.get(i));
                }
            }
        }

        //All unique start route service info are in the hashmap: close_start_service (Key is route id, value is the Route service value)
        for (int j = 0; j < end_service.size(); j++) {
            int remaining = InfoWindowDialog.compareTime(current_time, end_service.get(j).getTime());
            if (remaining > 2) { //Temp Range
                if (target_end_service.get(end_service.get(j).getRoute_id()) == null) {
                    ArrayList<Route_service> single_route_service = new ArrayList<>();
                    single_route_service.add(end_service.get(j));
                    target_end_service.put(end_service.get(j).getRoute_id(), single_route_service);
                } else {
                    target_end_service.get(end_service.get(j).getRoute_id()).add(end_service.get(j));
                }
            }
        }

        //All possible end service are in the hashmap: target_end_service
        ArrayList<String> possible_keys = new ArrayList<>();
        for (String s : close_start_service.keySet()) {
            if (target_end_service.containsKey(s)) {
                possible_keys.add(s);
            }
        }

        if (possible_keys.size() != 0) { //Have direct bus connecting two stops
            HashMap<String, ArrayList<ArrayList<String>>> direct_route_outcome = new HashMap<>();

            for (String route_name : possible_keys) {
                ArrayList<String> specific_info = new ArrayList<>();
                specific_info.add(route_name); //Target Route Name
                specific_info.add(close_start_service.get(route_name).getTime()); // Depart time
                Log.d("Long Message", "find_routes_between_two_marker: depart" + close_start_service.get(route_name).getTime());
                for (Route_service service : target_end_service.get(route_name)) {
                    Log.d("Long Message", "find_routes_between_two_marker: " + service.getService_id() + " taerget   " + close_start_service.get(route_name).getService_id());
                    if (service.getService_id().compareTo(close_start_service.get(route_name).service_id) == 0) {
                        Log.d("Long Message", "find_routes_between_two_marker: " + service.getTime());
                        if (InfoWindowDialog.compareTime(specific_info.get(1), service.getTime()) > 0) {
                            specific_info.add(service.getTime()); // Take off time
                        }
                        break;
                    }
                }
                ArrayList<ArrayList<String>> temp = new ArrayList<>();
                temp.add(specific_info);
                direct_route_outcome.put(route_name, temp);
            }
            Schedule_info_class schedule_info_class = new Schedule_info_class();
            schedule_info_class.setSchedule_info(direct_route_outcome);
            show_schedule_info_dialog(schedule_info_class);
        } else { //We need to find the transit
            Log.d("Long Message", "find_routes_between_two_marker: We need to find transit!");
            String start_stop_title_arr[] = start.getTitle().split(":");
            String start_stop_title = start_stop_title_arr[1];
            String end_stop_title_arr[] = start.getTitle().split(":");
            String end_stop_title = start_stop_title_arr[1];
            ArrayList<Route_service> route_services = new ArrayList<>(close_start_service.values());
            //ArrayList<String> target_key_set = new ArrayList<>(target_end_service.keySet());
            find_transit(route_services, target_end_service, start_stop_title, end_stop_title);
        }
    }

    //Helper function to find transit between two markers (Currently, maximum one transit)
    private void find_transit(final ArrayList<Route_service> current_service, final HashMap<String, ArrayList<Route_service>> target_route_name, final String stop_id, final String end_stop_id) {
        Log.d("Long Message", "find_transit: Find transit get called");
        final HashMap<String, ArrayList<String>> stop_ids = new HashMap<>();
        final int[] counter_helper = {0};
        for (int i = 0; i < current_service.size(); i++) {
            final String service = current_service.get(i).getService_id();
            DatabaseReference single_service = db.getReference().child("Trip_Time").child("Trip: " + service);
            final int temp = i;
            Query query = single_service.orderByKey();
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    boolean after_stop = false;
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        Trip_Time trip_time = data.getValue(Trip_Time.class);
                        if (trip_time.getStop_id().compareTo(stop_id) == 0) {
                            after_stop = true;
                        }
                        if (after_stop) {
                            if (stop_ids.get(current_service.get(temp).getRoute_id()) == null) {
                                ArrayList<String> single_stop_id = new ArrayList<>();
                                single_stop_id.add(trip_time.getStop_id() + ":" + service);
                                stop_ids.put(current_service.get(temp).getRoute_id(), single_stop_id);
                            } else {
                                stop_ids.get(current_service.get(temp).getRoute_id()).add(trip_time.getStop_id() + ":" + service);
                            }
                        }
                    }
                    counter_helper[0]++;
                    find_transit_helper(stop_ids, target_route_name, end_stop_id, current_service.size(), counter_helper[0]);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void find_transit_helper(HashMap<String, ArrayList<String>> service_stop_mapping, final HashMap<String, ArrayList<Route_service>> target_route, final String end_stop_id, int target_size, int current_size) {
        if (target_size == current_size) {
            Log.d("Long Message", "find_transit_helper: find transit helper get called");
            final int[] count_helper = {0};
            int size = 0;
            for (String service : service_stop_mapping.keySet()) {
                size += service_stop_mapping.get(service).size();
            }
            final int total_size = size;
            final HashMap<String, HashSet<String>> possible_route_stops = new HashMap<>(); //Route name is the key, Hashset contains all possible stop for that route
            for (String service : service_stop_mapping.keySet()) {
                Log.d("Long Message", "find_transit_helper: " + service);
                final ArrayList<String> stop = service_stop_mapping.get(service);
                final ArrayList<String> target_stop_key_set = new ArrayList<String>(target_route.keySet());
                for (String stop_info : stop) {
                    String[] split_stop_info = stop_info.split(":"); //0 is the stop_id, 1 is the service id
                    final String stop_id = split_stop_info[0];
                    final String service_id = split_stop_info[1];
                    Log.d("Long Message", "find_transit_helper: real " + service_id);
                    DatabaseReference operation_route_info = db.getReference().child("Operation Route").child("Stop: " + stop_id);
                    operation_route_info.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot data : dataSnapshot.getChildren()) {
                                if (data.getKey().charAt(0) != 'T') {
                                    String route_name = data.getValue(String.class);
                                    if (target_stop_key_set.contains(route_name)) {
                                        Log.d("Long Message", "onDataChange: " + "    stop   " + stop_id + "   direct transfer    " + route_name);
                                        if (possible_route_stops.get(route_name) == null) {
                                            HashSet<String> route_temp = new HashSet<>();
                                            route_temp.add(stop_id + ":" + service_id); //The drop-off stop id
                                            possible_route_stops.put(route_name, route_temp);
                                        } else {
                                            possible_route_stops.get(route_name).add(stop_id + ":" + service_id);
                                        }
                                    }
                                } else {
                                    GenericTypeIndicator<ArrayList<String>> t = new GenericTypeIndicator<ArrayList<String>>() {
                                    };
                                    ArrayList<String> transfer_route = data.getValue(t);
                                    for (String transfer : transfer_route) {
                                        String[] transfer_arr = transfer.replaceAll("\\s+", "").split(":");
                                        if (target_stop_key_set.contains(transfer_arr[1])) {
                                            Log.d("Long Message", "onDataChange: " + "  parent stop" + stop_id + "  stop   " + transfer_arr[0] + "   route_name " + transfer_arr[1]);
                                            if (possible_route_stops.get(transfer_arr[1]) == null) {
                                                HashSet<String> route_temp = new HashSet<>();
                                                route_temp.add(stop_id + ":" + service_id); //The drop-off stop id
                                                possible_route_stops.put(transfer_arr[1], route_temp);
                                            } else {
                                                possible_route_stops.get(transfer_arr[1]).add(stop_id + ":" + service_id);
                                            }
                                        }
                                    }
                                }
                            }
                            count_helper[0]++;
                            find_transit_finish(total_size, count_helper[0], possible_route_stops, target_route, end_stop_id);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        }
    }

    private void find_transit_finish(int total_size, int current_size, final HashMap<String, HashSet<String>> possible_route_stops, final HashMap<String, ArrayList<Route_service>> target_route, final String end_stop_id) {
        Log.d("Long Message", "get_transit_finish: current size " + current_size + "   total   " + total_size);
        if (total_size == current_size) { //Ready
            for (String s : possible_route_stops.keySet()) {
                HashSet<String> set = possible_route_stops.get(s);
                for (String x : set) {
                    Log.d("Long Message", "find_transit_finish: route   " + x);
                }

            }
            final HashMap<String, ArrayList<String>> final_mapping = new HashMap<>(); //Key is the service id and route name, value is the reachable stop by backtracing the services of final stop and the service id for starting stop
            final int size = possible_route_stops.keySet().size();
            final int[] count_helper = {0};
            for (final String route_name : possible_route_stops.keySet()) { //Every key should be existed in the target route's keyset, if not something goes wrong

                ArrayList<Route_service> special_route_service = target_route.get(route_name);
                DatabaseReference service = db.getReference().child("Trip_Time").child("Trip: " + special_route_service.get(0).getService_id());
                Query query = service.orderByKey();
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        boolean before_stop = true;
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            Trip_Time trip_time = data.getValue(Trip_Time.class);
                            if (trip_time.stop_id.compareTo(end_stop_id) == 0) {
                                before_stop = false;
                            }
                            if (before_stop) { //backtrace
                                LatLng start = new LatLng(all_stops.get(trip_time.stop_id).latitude, all_stops.get(trip_time.stop_id).longitude);
                                for (String target_possible_stops_info : possible_route_stops.get(route_name)) {
                                    String[] spilt_target_possible_stops_info = target_possible_stops_info.split(":"); //0 is the stop id, 1 is the service id
                                    String target_possible_stops = spilt_target_possible_stops_info[0];
                                    String service_id = spilt_target_possible_stops_info[1];
                                    LatLng end = new LatLng(all_stops.get(target_possible_stops).latitude, all_stops.get(target_possible_stops).longitude);
                                    float distance = calculateDistance(start, end);
                                    if (distance < 10) { //Currently set the direct transit as the priority (Can't handle opposite direction properly now)
                                        Log.d("Long Message", "onDataChange: direct transit  reachable stop:  " + trip_time.stop_id + "   target stop   " + target_possible_stops + "  route name  " + route_name + "    start service id   " + service_id);
                                        ArrayList<String> temp = new ArrayList<>();
                                        temp.add(Float.toString(distance)); // Index 0 is the distance to represent direct transit or indirect transfer
                                        temp.add(target_possible_stops); // Index 1 is the reachable stop for the starting stop's service
                                        temp.add(trip_time.stop_id); // Index 2 is the reachable stop for the ending stop's service by backtrace
                                        temp.add(route_name); // Index 3 is the target service id for transfer, not the starting service's id (Query that by service id)
                                        temp.add(service_id); // Index 4 is the starting service id
                                        temp.add(trip_time.stop_sequence); // Index 5 is the stop sequence, used for deciding which stop to use

                                        final_mapping.put(service_id + ":" + route_name, temp); //Override any value, regard the direct transfer as the first option
                                        break;
                                    } else if (distance < 300) {
                                        Log.d("Long Message", "onDataChange: reachable stop:  " + trip_time.stop_id + "   target stop   " + target_possible_stops + "  route name  " + route_name + "    start service id   " + service_id);
                                        if (final_mapping.get(service_id + ":" + route_name) == null) {
                                            ArrayList<String> temp = new ArrayList<>();
                                            temp.add(Float.toString(distance)); // Index 0 is the distance to represent direct transit or indirect transfer
                                            temp.add(target_possible_stops); // Index 1 is the reachable stop for the starting stop's service
                                            temp.add(trip_time.stop_id); // Index 2 is the reachable stop for the ending stop's service by backtrace
                                            temp.add(route_name); // Index 3 is the target service id for transfer, not the starting service's id (Query that by service id)
                                            temp.add(service_id); // Index 4 is the starting service id
                                            temp.add(trip_time.stop_sequence); // Index 5 is the stop sequence, used for deciding which stop to use
                                            final_mapping.put(service_id + ":" + route_name, temp);
                                        } else {
                                            if (Integer.parseInt(trip_time.stop_sequence) - Integer.parseInt(final_mapping.get(service_id + ":" + route_name).get(5)) > 4) { //Close in sequence stop has priority
                                                ArrayList<String> temp = new ArrayList<>();
                                                temp.add(Float.toString(distance)); // Index 0 is the distance to represent direct transit or indirect transfer
                                                temp.add(target_possible_stops); // Index 1 is the reachable stop for the starting stop's service
                                                temp.add(trip_time.stop_id); // Index 2 is the reachable stop for the ending stop's service by backtrace
                                                temp.add(route_name); // Index 3 is the target service id for transfer, not the starting service's id (Query that by service id)
                                                temp.add(service_id); // Index 4 is the starting service id
                                                temp.add(trip_time.stop_sequence); // Index 5 is the stop sequence, used for deciding which stop to use
                                                final_mapping.put(service_id + ":" + route_name, temp);
                                            } else {
                                                if (Float.parseFloat(final_mapping.get(service_id + ":" + route_name).get(0)) < distance) { //Less distance stop has priority
                                                    continue; //Do nothing
                                                } else {
                                                    ArrayList<String> temp = new ArrayList<>();
                                                    temp.add(Float.toString(distance)); // Index 0 is the distance to represent direct transit or indirect transfer
                                                    temp.add(target_possible_stops); // Index 1 is the reachable stop for the starting stop's service
                                                    temp.add(trip_time.stop_id); // Index 2 is the reachable stop for the ending stop's service by backtrace
                                                    temp.add(route_name); // Index 3 is the target service id for transfer, not the starting service's id (Query that by service id)
                                                    temp.add(service_id); // Index 4 is the starting service id
                                                    temp.add(trip_time.stop_sequence); // Index 5 is the stop sequence, used for deciding which stop to use
                                                    final_mapping.put(service_id + ":" + route_name, temp);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        count_helper[0]++;
                        Log.d("Long Message", "onDataChange: total " + size + "   current   " + count_helper[0]);
                        get_transit_schedule_helper(final_mapping, size, count_helper[0]);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }
    }

    private void get_transit_schedule_helper(final HashMap<String, ArrayList<String>> mapper, int total_size, int current_size) {
        // Mapper : 0 distance, 1 reachable stop for starting service, 2 reachable stop for end stop by backtrace, 3 route name, 4 starting service id, 5 sequence (no usage there)
        Log.d("Long Message", "get_transit_schedule_helper: current size " + current_size + "   total   " + total_size);
        if (current_size == total_size) {
            final int size = mapper.keySet().size();
            final int[] count_helper = {0};
            Calendar cal = Calendar.getInstance();
            cal.setTime(cal.getTime());
            final int time_identifier = cal.get(Calendar.DAY_OF_WEEK); // 0 for weekday, 1 for Saturday, 2 for Sunday
            final HashMap<String, ArrayList<Route_service>> all_stop_service_info = new HashMap<>();

            for (String key : mapper.keySet()) {
                Log.d("Long Message", "get_transit_schedule_helper: " + mapper.get(key));
                ArrayList<Route_service> temp = new ArrayList<>();
                all_stop_service_info.put(mapper.get(key).get(1), temp); //Avoid query same stop multiple time
                all_stop_service_info.put(mapper.get(key).get(2), temp);
            }

            final int stop_size_indicator = all_stop_service_info.size();
            //Key is the route id, key is the service info for that

            for (final String stop_name : all_stop_service_info.keySet()) {
                DatabaseReference stop_info = db.getReference().child("Stops").child("Stop: " + stop_name);
                stop_info.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (time_identifier == 7) { // Saturday
                            for (DataSnapshot data : dataSnapshot.getChildren()) {
                                //Log.d("Long Message", "onDataChange: Saturday  " + data.getKey());
                                if (data.getKey().compareTo("Saturday Service") == 0) {
                                    GenericTypeIndicator<ArrayList<Route_service>> t = new GenericTypeIndicator<ArrayList<Route_service>>() {
                                    };
                                    ArrayList<Route_service> route_services = data.getValue(t);
                                    all_stop_service_info.get(stop_name).addAll(route_services);
                                } else if (data.getKey().compareTo("Special_Saturday Service") == 0) {
                                    GenericTypeIndicator<ArrayList<Route_service>> t = new GenericTypeIndicator<ArrayList<Route_service>>() {
                                    };
                                    ArrayList<Route_service> route_services = data.getValue(t);
                                    all_stop_service_info.get(stop_name).addAll(route_services);
                                }
                            }
                        } else if (time_identifier == 0) { // Sunday
                            for (DataSnapshot data : dataSnapshot.getChildren()) {
                                //Log.d("Long Message", "onDataChange: Sunday " + data.getKey());
                                if (data.getKey().compareTo("Saturday Service") == 0) {
                                    GenericTypeIndicator<ArrayList<Route_service>> t = new GenericTypeIndicator<ArrayList<Route_service>>() {
                                    };
                                    ArrayList<Route_service> route_services = data.getValue(t);
                                    all_stop_service_info.get(stop_name).addAll(route_services);
                                } else if (data.getKey().compareTo("Special_Sunday Service") == 0) {
                                    GenericTypeIndicator<ArrayList<Route_service>> t = new GenericTypeIndicator<ArrayList<Route_service>>() {
                                    };
                                    ArrayList<Route_service> route_services = data.getValue(t);
                                    all_stop_service_info.get(stop_name).addAll(route_services);
                                }
                            }
                        } else { //Workday
                            for (DataSnapshot data : dataSnapshot.getChildren()) {
                                //Log.d("Long Message", "onDataChange: Saturday  " + data.getKey());
                                if (data.getKey().compareTo("Weekday Service") == 0) {
                                    GenericTypeIndicator<ArrayList<Route_service>> t = new GenericTypeIndicator<ArrayList<Route_service>>() {
                                    };
                                    ArrayList<Route_service> route_services = data.getValue(t);
                                    all_stop_service_info.get(stop_name).addAll(route_services);
                                } else if (data.getKey().compareTo("Special_Weekday Service") == 0) {
                                    GenericTypeIndicator<ArrayList<Route_service>> t = new GenericTypeIndicator<ArrayList<Route_service>>() {
                                    };
                                    ArrayList<Route_service> route_services = data.getValue(t);
                                    all_stop_service_info.get(stop_name).addAll(route_services);
                                }
                            }
                        }
                        count_helper[0]++;
                        get_transit_schedule_finish(mapper, all_stop_service_info, stop_size_indicator, count_helper[0]);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }
    }

    private void get_transit_schedule_finish(HashMap<String,ArrayList<String>> mapper, HashMap<String, ArrayList<Route_service>> all_stop_service_info, int total_size, int current_size){
        // Mapper : 0 distance, 1 reachable stop for starting service, 2 reachable stop for end stop by backtrace, 3 transit route name, 4 starting service id, 6 is the transit time
        if(total_size == current_size){ // Collect all info together
            Log.d("Long Message", "get_transit_schedule_finish: finish get called");
            for(String key: mapper.keySet()){
                Log.d("Long Message", "get_transit_schedule_finish: mapper  " + mapper.get(key));
            }
            Marker start = find_transit_helper_arr.get(0);
            Marker end = find_transit_helper_arr.get(1);
            if(start.getTag() == null || end.getTag() == null){ //Error handling
                find_transit_helper_arr.clear();
                return;
            }
            StopInfoClass start_info = (StopInfoClass)start.getTag();
            StopInfoClass end_info = (StopInfoClass)end.getTag();

            //String start_title = start.getTitle().split(":")[1];
            //String end_title = end.getTitle().split(":")[1];

            ArrayList<Route_service> start_service = start_info.getTime_schedule();
            ArrayList<Route_service> end_service = end_info.getTime_schedule();
            String transit_time = "01:00:00"; // Temp
            String depart_time = "";
            String target_service_id = "";
            HashMap<String, ArrayList<ArrayList<String>>> all_info_collections = new HashMap<>(); // Will store one quickest transfer info for each route


            for(String info : mapper.keySet()){
                Log.d("Long Message", "get_transit_schedule_finish: keyset " + info);
                boolean have_service = false;
                ArrayList<String> collection = new ArrayList<>(); //Compare collection
                String service = info.split(":")[0];
                for(Route_service route_service : start_service){
                    if(route_service.service_id.compareTo(service) == 0){
                        collection.add(route_service.getTime()); // 0 for starting time
                        collection.add(route_service.getRoute_id()); // 1 for depart route id
                    }
                }
                for(Route_service route_service : all_stop_service_info.get(mapper.get(info).get(1))){
                    if(route_service.service_id.compareTo(service) == 0){
                        transit_time = route_service.getTime();
                        collection.add(route_service.getTime()); // 2 for Take off time
                        collection.add(mapper.get(info).get(1)); // 3 for take off stop
                    }
                }
                for(Route_service route_service : all_stop_service_info.get(mapper.get(info).get(2))){
                    if(route_service.getRoute_id().compareTo(mapper.get(info).get(3)) == 0){
                        int remaining = InfoWindowDialog.compareTime(transit_time, route_service.getTime());
                        if(remaining > 3){ //3 is temp
                            depart_time = route_service.getTime();
                            target_service_id = route_service.getService_id();
                            collection.add(depart_time); // 4 for transit depart time
                            collection.add(mapper.get(info).get(3)); // 5 for transit route name
                            collection.add(mapper.get(info).get(2)); // 6 for transit stop id
                            have_service = true;
                            break;
                        }
                    }
                }
                if(!have_service){
                    Log.d("Long Message", "get_transit_schedule_finish: no service called");
                    continue; //No connecting service (Edge case)
                }
                for(Route_service route_service : end_service){
                    if(route_service.getService_id().compareTo(target_service_id) == 0){
                        Log.d("Long Message ", "get_transit_schedule_finish: " + route_service.getService_id() + "   route name   " + route_service.getRoute_id() +  "  arrive time  " + route_service.getTime());
                        int remaining = InfoWindowDialog.compareTime(transit_time, route_service.getTime());
                        if(remaining > 0){
                            collection.add(route_service.getTime()); // 7 for arrive time
                        }
                    }
                }
                if(collection.size() == 8){
                    if(all_info_collections.get(collection.get(1)) == null){
                        Log.d("Long Message", "get_transit_schedule_finish: " + collection);
                        ArrayList<ArrayList<String>> temp = new ArrayList<>();
                        temp.add(collection);
                        all_info_collections.put(collection.get(1), temp);
                    }
                    else{
                        all_info_collections.get(collection.get(1)).add(collection);
                    }
                }
            }

            Schedule_info_class information_class = new Schedule_info_class();
            information_class.setSchedule_info(all_info_collections);

            show_schedule_info_dialog(information_class);
        }
    }

    private static float calculateDistance(LatLng start_position, LatLng end_position){
        float[] results = new float[1];
        Location.distanceBetween(start_position.latitude, start_position.longitude,end_position.latitude, end_position.longitude,results);
        return results[0];
    }

    public static void direct_stop_connection(){
        Marker start = find_route_marker_arr.get(0);
        Marker end = find_route_marker_arr.get(1);

        PolylineOptions option = new PolylineOptions().color(Color.CYAN);
        option.add(start.getPosition(), end.getPosition());
        find_route_polyline_arr.add(map.addPolyline(option));
    }

    public static void connectingStops(String transit_stop_id, String transit_stop_id_2){
        Marker start = find_route_marker_arr.get(0);
        Marker end = find_route_marker_arr.get(1);
        Stop transit1 = all_stops.get(transit_stop_id);
        Stop transit2 = all_stops.get(transit_stop_id_2);

        MarkerOptions marker = new MarkerOptions();
        marker.position(new LatLng(transit1.getLatitude(), transit1.getLongitude()));
        marker.title(transit1.stop_id + "");
        marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        Marker m1 = map.addMarker(marker);
        find_transit_route_marker_arr.add(m1);
        display_nearby_stop_helper(transit1.getStop_id(), m1);

        MarkerOptions marker2 = new MarkerOptions();
        marker2.position(new LatLng(transit2.getLatitude(), transit2.getLongitude()));
        marker2.title(transit1.stop_id + "");
        marker2.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        Marker m2 = map.addMarker(marker2);
        find_transit_route_marker_arr.add(m2);
        display_nearby_stop_helper(transit2.getStop_id(), m2);

        PolylineOptions option = new PolylineOptions().color(Color.CYAN);
        option.add(start.getPosition()).add(new LatLng(transit1.getLatitude(), transit1.getLongitude()))
                .add(new LatLng(transit2.getLatitude(), transit2.getLongitude())).add(end.getPosition());
        find_route_polyline_arr.add(map.addPolyline(option));
    }

    private void dealwithfabs(){
        final FloatingActionButton direction_button = findViewById(R.id.direction_fab);
        final FloatingActionButton clear_button = findViewById(R.id.clear_fab);
        direction_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(find_route_mode == false){
                    direction_button.setImageResource(R.drawable.ic_map_black_24dp);
                    clear_button.setImageResource(R.drawable.ic_replay_black_24dp);
                    find_route_mode = true;
                }
                else{
                    clear_button.setImageResource(R.drawable.ic_clear);
                    direction_button.setImageResource(R.drawable.ic_direction);
                    find_route_mode = false;
                    for(Polyline p : find_route_polyline_arr){
                        p.remove();
                    }
                    find_route_polyline_arr.clear();
                    for(Marker m : find_transit_route_marker_arr){
                        m.remove();
                    }
                    for(Marker m : user_marker_count){
                        m.remove();
                    }
                    user_marker_count.clear();
                    find_transit_helper_arr.clear();
                    find_route_marker_arr.clear();
                }
            }
        });


        clear_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(find_route_mode){
                    for(Polyline p : find_route_polyline_arr){
                        p.remove();
                    }
                    find_route_polyline_arr.clear();
                    for(Marker m : find_transit_route_marker_arr){
                        m.remove();
                    }
                    find_transit_helper_arr.clear();
                    find_route_marker_arr.clear();
                    for(Marker m : user_marker){
                        m.remove();
                    }
                    for(Marker m : user_marker_count){
                        m.remove();
                    }
                    user_marker_count.clear();
                    user_marker.clear();
                    transit_result_returned = false;
                }
                else{
                    user_marker.clear();
                    clear_all();
                }
            }
        });

        final FloatingActionButton calendar_button = findViewById(R.id.calendar_fab);
        calendar_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show_calendar_schedule();
            }
        });

        final FloatingActionButton  expansion_button = findViewById(R.id.expansion_fab);
        expansion_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fab_expanded){
                    clear_button.animate().translationY(0);
                    direction_button.animate().translationY(0);
                    calendar_button.animate().translationY(0);
                    fab_expanded = false;
                    expansion_button.setImageResource(R.drawable.ic_add_black_24dp);
                }
                else{
                    calendar_button.animate().translationY(-getResources().getDimension(R.dimen.size_150));
                    clear_button.animate().translationY(-getResources().getDimension(R.dimen.size_100));
                    direction_button.animate().translationY(-getResources().getDimension(R.dimen.size_50));
                    fab_expanded = true;
                    expansion_button.setImageResource(R.drawable.ic_remove);
                }
            }
        });
    }

    public static float getMarker_user_distance(){
        return marker_user_distance;
    }

    public static void setUser_defined_time(String time){
        user_defined_time = time;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Deal with fetch data issue
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        generateLocationUpdate();

        //Deal with fabs
        dealwithfabs();
        //checkMapServices();
        checkPermissions();

        FirebaseApp.initializeApp(this);
        db = FirebaseDatabase.getInstance();

    }

    private void generateLocationUpdate(){
        try{
            LocationRequest mLocationRequest = new LocationRequest();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setInterval(50000);
            mLocationRequest.setFastestInterval(50000); // Update for every 50s
            LocationCallback mLocationCallBack = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) {
                        Log.d("Long Message", "onLocationResult: location  1" );
                        return;
                    }
                    else{
                        Location location = locationResult.getLastLocation();
                        user_location = new LatLng(location.getLatitude(), location.getLongitude());
                        checkGeoLocation(user_location);
                        send_notification(notification_stop_id, user_location);
                    }
                }
            };
            user_location_client = LocationServices.getFusedLocationProviderClient(this);

            user_location_client.requestLocationUpdates(mLocationRequest, mLocationCallBack, null);
        }catch(Exception e){
            user_location = new LatLng(34.412936, -119.847863);
            Log.d("Long Message", "generateLocationUpdate: " + e.getMessage());
        }

    }

    private void clear_all(){
        transit_result_returned = false;
        find_route_marker_arr.clear();
        find_transit_helper_arr.clear();
        find_transit_route_marker_arr.clear();
        find_route_polyline_arr.clear();
        user_marker_count.clear();
        map.clear();
        LatLng current = new LatLng(user_location.latitude, user_location.longitude);
        displayNearByStop(current, 450, 0);
    }

    public static void setNotification_stop_id(String stop_id){
        notification_stop_id = stop_id;
    }

    private void checkGeoLocation(LatLng ll){
        if(ll.latitude < 33 || ll.latitude > 36 || ll.longitude > -118|| ll.longitude < -121){
            user_location = new LatLng(34.412936, -119.847863);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        LatLng ucsb = new LatLng(34.412936, -119.847863);
        map.setMinZoomPreference(13);
        map.setMaxZoomPreference(22);
        map.moveCamera(CameraUpdateFactory.newLatLng(ucsb));
        //CustomInfoWindowGoogleMap customInfoWindow = new CustomInfoWindowGoogleMap(this);
        //mMap.setInfoWindowAdapter(customInfoWindow);


        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(transit_result_returned){
                    return;
                }
                else{
                    map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    if(user_marker_count.size() < 2){
                        MarkerOptions marker = new MarkerOptions();
                        marker.position(latLng);
                        marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                        marker.title("User Point");
                        displayNearByStop(latLng, 450, 1);
                        Log.d("Long Message", "onMapClick: less" + user_marker_count.size());
                        user_marker_count.add(map.addMarker(marker));
                    }
                    else if(user_marker_count.size() == 2){
                        Log.d("Long Message", "onMapClick: equal" + user_marker_count.size());
                        for(Marker m : user_marker_count){
                            m.remove();
                        }
                        user_marker_count.clear();
                        for(Marker m : user_marker){
                            m.remove();
                        }
                        MarkerOptions marker = new MarkerOptions();
                        marker.position(latLng);
                        marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                        marker.title("User Point");
                        displayNearByStop(latLng, 450, 1);
                        user_marker_count.add(map.addMarker(marker));
                    }
                    else{
                        Log.d("Long Message", "onMapClick: else" + user_marker_count.size());
                        for(Marker m : user_marker_count){
                            m.remove();
                        }
                        user_marker_count.clear();
                        for(Marker m : user_marker){
                            m.remove();
                        }
                    }
                }
            }
        });

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                map.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                if(marker.getTitle().compareTo("User Point") == 0){
                    //temp
                }
                else{
                    if(find_route_mode == false){
                        showStopInfoDialog((StopInfoClass)marker.getTag());
                    }
                    else{
                        if(find_route_marker_arr.size() == 0){ //First marker
                            transit_result_returned = false;
                            send_notification_indicator = false;
                            marker_user_distance = calculateDistance(new LatLng(user_location.latitude,user_location.longitude), marker.getPosition());
                            showUserMarkerChoiceDialog();
                            find_route_marker_arr.add(marker);
                        }
                        else if(find_route_marker_arr.size() == 1){
                            Log.d("Long Message", "onMarkerClick: " + user_defined_time);
                            find_route_marker_arr.add(marker);
                            transit_result_returned = true;
                            find_routes_between_two_marker(find_route_marker_arr.get(0), find_route_marker_arr.get(1));
                        }
                        else if(find_route_marker_arr.size() == 2){
                            if(send_notification_indicator){
                                transit_result_returned = true;
                                find_routes_between_two_marker(find_route_marker_arr.get(0), find_route_marker_arr.get(1));
                            }
                            else{
                                String title = marker.getTitle();
                                String[] title_arr = title.split(":");
                                Log.d("Title", "onMarkerClick: " + marker.getTitle());
                                if(title_arr.length == 2){
                                    notification_stop_id = title_arr[1];
                                }
                                showPositionTrackDialog();
                                send_notification_indicator = true;
                                transit_result_returned = true;
//                                find_routes_between_two_marker(find_route_marker_arr.get(0), find_route_marker_arr.get(1));
                            }

                        }
                        else{
                            Log.e("Long Message", "onMarkerClick: " + "Undefined behavior" );
                        }
                    }
                }
                return false;
            }
        });

        collectBasic_Stop_info();
        /*ArrayList<String[]> stop = Helper.getPositionData("https://openmobilitydata-data.s3-us-west-1.amazonaws.com/public/feeds/santa-barbara-mtd/1156/20191119/original/shapes.txt");
        PolylineOptions option = new PolylineOptions();
        PolylineOptions option2 = new PolylineOptions();
        option2.color(Color.CYAN);
        for(int i = 0; i < stop.size(); i++){
            if(stop.get(i)[0].compareTo("19213") == 0){
                LatLng ll = new LatLng(Double.parseDouble(stop.get(i)[1]), Double.parseDouble(stop.get(i)[2]));
                option.add(ll);
            }
            else{
                LatLng ll = new LatLng(Double.parseDouble(stop.get(i)[1]), Double.parseDouble(stop.get(i)[2]));
                option2.add(ll);
            }
        }
        mMap.addPolyline(option);
        mMap.addPolyline(option2);*/
    }

    public void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Personal Notifications";
            String description = "Include all the personal notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, name, importance);

            notificationChannel.setDescription(description);

            NotificationManager notificationManager = (NotificationManager) getSystemService (NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }


    private void send_notification(String stopID, LatLng current_location){
        //Define your operation there, at this time, the all_stops will be filled with information
        float[] distanceVal = new float[1];
        if(stopID == ""){
            return;
        }

        if (all_stops.containsKey(stopID)) {
            Stop DestinationStop = all_stops.get(stopID);
            String DestinationStopName;
            DestinationStopName = DestinationStop.getName();
            //Calculate Distance
            Location.distanceBetween(current_location.latitude, current_location.longitude, DestinationStop.getLatitude(), DestinationStop.getLongitude(), distanceVal);


            //Notification
            if (distanceVal[0] < 300) {
                Log.d("distance", "less than 200");
                createNotificationChannel();

                Intent mainIntent = new Intent(getApplicationContext(), getClass());
                mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);

                PendingIntent mainPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,mainIntent, PendingIntent.FLAG_ONE_SHOT);


                NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);

                builder.setSmallIcon(R.drawable.ic_notifications_black_24dp);

                builder.setContentTitle("BusTask");
                builder.setContentText("Get ready for your Destination Stop: " + DestinationStopName);
                builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
                builder.setAutoCancel(true);
                //Notification onClick
                builder.setContentIntent(mainPendingIntent);
                //Vibrate if sound if turned off
                builder.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000, 1000, 1000});
                //Notification Sound
                Notification notification = builder.build();
                notification.defaults = Notification.DEFAULT_SOUND;
//
//                notification.defaults |= Notification.DEFAULT_VIBRATE;
                //Notification Initialize
                NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
                notificationManagerCompat.notify(NOTIFICATION_ID, builder.build());
            }

            Log.d("distance", Float.toString(distanceVal[0]));
        }
        else {
            Log.d("distance", "invalid stop id");
        }




    } //End of do_operation

    //Dialogs
    public void showPositionTrackDialog(){
        bus_position_track_dialog u = new bus_position_track_dialog();
        u.show(getSupportFragmentManager(), "Position_track");
    }

    public void showUserMarkerChoiceDialog(){
        ShowUserMarkerChoiceDialog u = new ShowUserMarkerChoiceDialog();
        u.show(getSupportFragmentManager(), "Direction");
    }

    private void show_schedule_info_dialog(Schedule_info_class info){
        fm = getSupportFragmentManager();
        scheduleInfoDialog infoDialog = scheduleInfoDialog.newInstance(info);
        infoDialog.show(fm, "Schedule_Window");
    }

    private void show_calendar_schedule(){
        fm = getSupportFragmentManager();
        user_schedule_dialog schedule_dialog = user_schedule_dialog.newInstance();
        schedule_dialog.show(fm,"Calendar_Window");
    }

    public static void remove_temp_direction_marker(){
        find_route_marker_arr.clear();
    }

    public void showStopInfoDialog(StopInfoClass info){
        fm = getSupportFragmentManager();
        InfoWindowDialog info_window = InfoWindowDialog.newInstance(info);
        info_window.show(fm, "Info_Window");
    }



    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
        } else {
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION}, Access_Fine_Location);
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET)
                == PackageManager.PERMISSION_GRANTED) {
        } else {
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.INTERNET}, Internet);
        }
    }
}