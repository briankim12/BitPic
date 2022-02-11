package edu.ucsb.cs.cs184.bustester.ui.home;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.TestLooperManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.common.util.ArrayUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ThrowOnExtraProperties;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SimpleTimeZone;

import edu.ucsb.cs.cs184.bustester.MainActivity;
import edu.ucsb.cs.cs184.bustester.MapsActivity;
import edu.ucsb.cs.cs184.bustester.R;
import edu.ucsb.cs.cs184.bustester.ui.dashboard.DashboardFragment;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private static FirebaseDatabase db;
    private static ArrayList<Stop> near_stop_list  = new ArrayList<>();
    private static HashMap<String, Stop> all_stops = new HashMap<>();
    private static ArrayList<String> stop_ids = new ArrayList<>();
    private static Map<String, ArrayList<String>> route_id_with_time;
    private static FloatingActionButton fb;
    private static FloatingActionButton fbMap;
    private static LinearLayout ll;
    private static SparseArray<Float> stopID_distance = new SparseArray<>();

    private static FusedLocationProviderClient client;

    private static TextView timeTV;

    public static void setTime (String s) {
        timeTV.setText(s);
    }

    public static String getTime(){
        if (timeTV != null) {
            return timeTV.getText().toString();
        } else {
            return "00:00:00";
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

    public static class Stop implements Serializable {

        private Double latitude;
        private Double longitude;
        private String name;
        private int stop_id;


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

        public Stop() {
        }
    }


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        FirebaseApp.initializeApp(getActivity());
        collectBasic_Stop_info();

        requestPermission();

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final double lat[] = {0};
        final double lon[] = {0};

        fbMap = getActivity().findViewById(R.id.map_floatingActionButton);
        fbMap.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MapsActivity.class);
                getActivity().startActivity(intent);
            }
        });


        fb = getActivity().findViewById(R.id.find_near_stop_floatingActionButton);
        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TextView welcome = getActivity().findViewById(R.id.home_welcome_textView);
                welcome.setVisibility(View.INVISIBLE);

                if (ActivityCompat.checkSelfPermission(getActivity(), ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return ;
                }
                client = LocationServices.getFusedLocationProviderClient(getContext());
                client.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
//                            Log.d("mylocation", "lat--->" + String.valueOf(location.getLatitude()) + "long--->" + String.valueOf(location.getLongitude()));
                            lat[0] = location.getLatitude();
                            lon[0] = location.getLongitude();
                        }
                    }
                });

                LatLng userLocation = new LatLng(lat[0], lon[0]);
//                Log.d("mylocation", "lat--->" + String.valueOf(userLocation.latitude) + "long--->" + String.valueOf(userLocation.longitude));
                LatLng ucsb = new LatLng(34.412936, -119.847863);

                get_nearby_bus_stop(ucsb, 500);


                for (Stop s : near_stop_list) {
                    Log.d("key", s.getName());
                    Log.d("key", String.valueOf(s.getStop_id()));
                    stop_ids.add(String.valueOf(s.stop_id));
                }

                get_bus_info_helper_finish(stop_ids);
            }
        });

        if (timeTV != null) {
            MainActivity.setTime(timeTV.getText().toString());
        }
    }

    private void get_bus_info_helper_finish(ArrayList<String> stop_ids) {
        final int total_size = stop_ids.size(); // The num of stops we need to query
        final int count_helper[] = {0};

        Calendar cal = Calendar.getInstance(); //Get current date
        cal.setTime(cal.getTime());
        final int time_identifier = cal.get(Calendar.DAY_OF_WEEK); // 0 for weekday, 1 for Saturday, 2 for Sunday

        final HashMap<String, ArrayList<Route_service>> all_stop_service_info = new HashMap<>(); // Hashmap to catch all stop service info
        final HashMap<String, Stop> all_stop_basic_info = new HashMap<>(); // Hashmap to catch all stop basic info, key is the stop_id

        for(final String stop_id : stop_ids){ //Query stop one by one
            DatabaseReference stop_info = db.getReference().child("Stops").child("Stop: " + stop_id);
            ArrayList<Route_service> service_temp = new ArrayList<>();
            all_stop_service_info.put(stop_id, service_temp);

            stop_info.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Stop stop = dataSnapshot.getValue(Stop.class);
                    all_stop_basic_info.put(stop_id, stop);
                    if(time_identifier == 7){ // Saturday
                        for(DataSnapshot data : dataSnapshot.getChildren()){
                            //Log.d("Long Message", "onDataChange: Saturday  " + data.getKey());
                            if(data.getKey().compareTo("Saturday Service") == 0){
                                GenericTypeIndicator<ArrayList<Route_service>> t = new GenericTypeIndicator<ArrayList<Route_service>>(){};
                                ArrayList<Route_service> route_services = data.getValue(t);
                                all_stop_service_info.get(stop_id).addAll(route_services);
                            }
                            else if(data.getKey().compareTo("Special_Saturday Service") == 0){
                                GenericTypeIndicator<ArrayList<Route_service>> t = new GenericTypeIndicator<ArrayList<Route_service>>(){};
                                ArrayList<Route_service> route_services = data.getValue(t);
                                all_stop_service_info.get(stop_id).addAll(route_services);
                            }
                        }
                    }
                    else if(time_identifier == 0){ // Sunday
                        for(DataSnapshot data : dataSnapshot.getChildren()){
                            //Log.d("Long Message", "onDataChange: Sunday " + data.getKey());
                            if(data.getKey().compareTo("Saturday Service") == 0){
                                GenericTypeIndicator<ArrayList<Route_service>> t = new GenericTypeIndicator<ArrayList<Route_service>>(){};
                                ArrayList<Route_service> route_services = data.getValue(t);
                                all_stop_service_info.get(stop_id).addAll(route_services);
                            }
                            else if(data.getKey().compareTo("Special_Sunday Service") == 0){
                                GenericTypeIndicator<ArrayList<Route_service>> t = new GenericTypeIndicator<ArrayList<Route_service>>(){};
                                ArrayList<Route_service> route_services = data.getValue(t);
                                all_stop_service_info.get(stop_id).addAll(route_services);
                            }
                        }
                    }
                    else{ //Workday
                        for(DataSnapshot data : dataSnapshot.getChildren()){
                            //Log.d("Long Message", "onDataChange: Saturday  " + data.getKey());
                            if(data.getKey().compareTo("Weekday Service") == 0){
                                GenericTypeIndicator<ArrayList<Route_service>> t = new GenericTypeIndicator<ArrayList<Route_service>>(){};
                                ArrayList<Route_service> route_services = data.getValue(t);
                                all_stop_service_info.get(stop_id).addAll(route_services);
                            }
                            else if(data.getKey().compareTo("Special_Weekday Service") == 0){
                                GenericTypeIndicator<ArrayList<Route_service>> t = new GenericTypeIndicator<ArrayList<Route_service>>(){};
                                ArrayList<Route_service> route_services = data.getValue(t);
                                all_stop_service_info.get(stop_id).addAll(route_services);
                            }
                        }
                    }
                    count_helper[0]++;
                    get_bus_info_helper_finish(all_stop_basic_info, all_stop_service_info, total_size, count_helper[0]);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void get_bus_info_helper_finish(HashMap<String, Stop> all_stop_basic_info, final HashMap<String, ArrayList<Route_service>> all_stop_service_info, int total_size, int current_size){
        if(current_size == total_size){

            ll = getActivity().findViewById(R.id.find_near_stop_linearLayout);
            if(((LinearLayout) ll).getChildCount() > 0)
                ((LinearLayout) ll).removeAllViews();
            timeTV = new TextView(getActivity());
            timeTV.setText("");
            timeTV.setGravity(Gravity.CENTER);
            ll.addView(timeTV);

            for(final String s : all_stop_basic_info.keySet()){
                Log.d("Long Message", "get_bus_info_helper_finish-------------------: " + all_stop_basic_info.get(s).getName() + all_stop_basic_info.get(s).getStop_id());

                ll.setGravity(Gravity.CENTER);

                TextView tv = new TextView(getActivity());
                tv.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                tv.setText(all_stop_basic_info.get(s).getName());
                tv.setGravity(Gravity.CENTER);
                tv.setTextSize(24);
                ll.addView(tv);

                if (stopID_distance != null && stopID_distance.size() != 0) {
                    TextView tv1 = new TextView(getActivity());
                    tv1.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    ));
//                    String walk_time = String.valueOf((int)(stopID_distance.get(all_stop_basic_info.get(s).getStop_id()) / (2.5 * 60)));
                    String d_to_stop = "Distance between you and this bus stop: " +String.valueOf(Math.round(stopID_distance.get(all_stop_basic_info.get(s).getStop_id()))) + "m";
                    tv1.setText(d_to_stop);
                    tv1.setGravity(Gravity.CENTER);
                    tv1.setTextSize(12);
                    ll.addView(tv1);

                    TextView tv2 = new TextView(getActivity());
                    tv2.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    ));
//                    String walk_time = String.valueOf((int)(stopID_distance.get(all_stop_basic_info.get(s).getStop_id()) / (2.5 * 60)));
                    String t_to_stop = "You can get there in: " +String.valueOf(Math.round(stopID_distance.get(all_stop_basic_info.get(s).getStop_id()) / (1.3 * 60) )) + "min";
                    tv2.setText(t_to_stop);
                    tv2.setGravity(Gravity.CENTER);
                    tv2.setTextSize(12);
                    ll.addView(tv2);
                }

                // get current route id into temp set
                final Set<String> setTemp = new HashSet<>();
//
                for (Route_service r : all_stop_service_info.get(s)) {
                    setTemp.add(r.getRoute_id());
                     Log.d("Long Message", "routTemp>>>>>>>>"+r.getRoute_id());
                }

//                for (String sTemp : setTemp){
//                    Log.d("Long Message", "routTemp-->"+sTemp);
//                }

                // display current stop route ids
                for( final String rId : setTemp) {
                    Button button = new Button(getActivity());
                    button.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    ));

                    // set button size
                    DisplayMetrics dm = new DisplayMetrics();
                    getActivity().getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
                    int width = dm.widthPixels;

                    button.setText(rId);
                    button.getLayoutParams().width = (int) (width * 0.9);
                    button.setGravity(Gravity.CENTER);


                    GradientDrawable shape = new GradientDrawable();
                    shape.setShape(GradientDrawable.RECTANGLE);
                    shape.setColor(Color.parseColor("#B3E5FC"));
                    shape.setStroke(10, Color.WHITE);
                    shape.setCornerRadius(20);

                    button.setBackground(shape);

                    ll.addView(button);



                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            ArrayList<String> rTimes = new ArrayList<>();
//                            for ( String r1 : all_stop_service_info.keySet()) {
//                                for (Route_service r2 : all_stop_service_info.get(r1)) {
                                for (Route_service r2 : all_stop_service_info.get(s)) {

                                    Calendar c = Calendar.getInstance();
                                    SimpleDateFormat hms = new SimpleDateFormat("HH:mm:ss");
                                    String getCurrentTime = hms.format(c.getTime());

//                                    Log.d("Long Message", "current time ->>>" + getCurrentTime);
//                                    Log.d("Long Message", "route   time ->>>" + r2.getTime());

                                    if (rId.compareTo(r2.getRoute_id()) == 0 && realCompareTime(getCurrentTime, r2.getTime()) > 0){
//                                        Log.d("Long Message", "add time--->: " + r2.getTime());
                                        rTimes.add(r2.getTime());
                                    }
//                                    Log.d("rtime",  "rID-->" + rId + " add time--->: " + r2.getRoute_id() + " --->"+ r2.getTime());
                                }

                            FragmentManager fm = getActivity().getSupportFragmentManager();
                            NearStopRouteTimeDialog n = NearStopRouteTimeDialog.newInstance(rTimes);
                            n.show(fm, "route time");


                        }
                    });
                }
           }

        }
    }



    //    private static ArrayList<Stop> stop_list  = new ArrayList<>();
    private static void get_nearby_bus_stop(final LatLng currentPosition, final int max_distance){ //distance in meters, find the nearby stops to the position
        for ( Stop s : all_stops.values()){
            float[] results = new float[1];
            Location.distanceBetween(currentPosition.latitude, currentPosition.longitude,s.latitude, s.longitude,results);
            if(results[0] < max_distance){
                near_stop_list.add(s);
                stopID_distance.append(s.stop_id, results[0]);
            }
        }
    }


    private static void collectBasic_Stop_info(){
        db = FirebaseDatabase.getInstance();
        DatabaseReference stop = db.getReference().child("Basic_Stops");
        stop.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren()){
                    Stop stop = data.getValue(Stop.class);
                    all_stops.put(Integer.toString(stop.stop_id), stop);
                }
//                do_operations();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void requestPermission () {
        ActivityCompat.requestPermissions(getActivity(), new String[]{ACCESS_FINE_LOCATION}, 1);
    }

    private boolean compareTime(String busTime) {
        boolean after = false;

        Calendar c = Calendar.getInstance();
        SimpleDateFormat hms = new SimpleDateFormat("HH:MM:SS");
        String getCurrentTime = hms.format(c.getTime());

//        Log.d("Long Message", "current time -->" + getCurrentTime);
//        Log.d("Long Message", "Bus Time -->" + busTime);
//        Log.d("Long Message", "after -->" + getCurrentTime.compareTo(busTime));

        if (getCurrentTime.compareTo(busTime) < 0)
            after = true;

        return after;
    }

    public static int realCompareTime(String current, String target){ //Compare two time in HH:mm:ss format
        target = target.replaceAll("\\s+","");
        current = current.replaceAll("\\s+","");
        String[] current_time = current.split(":");
        String[] target_time = target.split(":");

        if(Integer.parseInt(current_time[0]) > Integer.parseInt(target_time[0])){
            return -1;
        }
        else if(Integer.parseInt(current_time[0]) == Integer.parseInt(target_time[0])){
            if(Integer.parseInt(current_time[1]) > Integer.parseInt(target_time[1])){
                return -1;
            }
            else{
                return Integer.parseInt(target_time[1]) - Integer.parseInt(current_time[1]);
            }
        }
        else{
            return (Integer.parseInt(target_time[0]) -Integer.parseInt(current_time[0])) * 60 + Integer.parseInt(target_time[1]) - Integer.parseInt(current_time[1]);
        }
    }
}