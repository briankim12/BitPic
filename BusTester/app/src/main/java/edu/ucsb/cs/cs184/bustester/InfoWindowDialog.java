package edu.ucsb.cs.cs184.bustester;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class InfoWindowDialog extends DialogFragment {
    private static final String ARG_PARAM1 = "param1";
    //private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private StopInfoClass mParam1;
    //private String mParam2;

    public InfoWindowDialog() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static InfoWindowDialog newInstance(StopInfoClass info) {
        InfoWindowDialog fragment = new InfoWindowDialog();
        Bundle args = new Bundle();

        args.putSerializable(ARG_PARAM1, info);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.custom_info_window, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        StopInfoClass info = (StopInfoClass)getArguments().getSerializable(ARG_PARAM1);
        LinearLayout ll = view.findViewById(R.id.scroll);

        final ArrayList<MapsActivity.Route_service>  stop_info = info.getTime_schedule();
        Calendar now = Calendar.getInstance();
        String current_time = now.get(Calendar.HOUR_OF_DAY) + ":" + now.get(Calendar.MINUTE) + ":" + now.get(Calendar.SECOND);
        //String current_time = "13:45:22";
        final HashMap<String, ArrayList<MapsActivity.Route_service>> separate_service = new HashMap<>();

        for(int i = 0; i < stop_info.size(); i++){

            int remaining = compareTime(current_time, stop_info.get(i).getTime());
            //Log.d("Long Message", "compareTime: remain  "+ remaining);
            if( remaining > 0){
                if(separate_service.get(stop_info.get(i).getRoute_id()) == null){
                    ArrayList<MapsActivity.Route_service> a = new ArrayList<>();
                    a.add(stop_info.get(i));
                    separate_service.put(stop_info.get(i).getRoute_id(), a);
                }
                else{
                    if(separate_service.get(stop_info.get(i).getRoute_id()).get(separate_service.get(stop_info.get(i).getRoute_id()).size() - 1).getTime().compareTo(stop_info.get(i).getTime()) == 0 ){
                        separate_service.get(stop_info.get(i).getRoute_id()).remove(separate_service.get(stop_info.get(i).getRoute_id()).size() - 1);
                    }
                    separate_service.get(stop_info.get(i).getRoute_id()).add(stop_info.get(i));
                }
            }
        }

        for(String s : separate_service.keySet()){ //Data is ordered by route name
            final ArrayList<MapsActivity.Route_service> specific_route_info = separate_service.get(s);

            int maxSize = specific_route_info.size();
            if (specific_route_info.size() > 16){
                maxSize = 15;
            }

            for(int i = 0; i < maxSize; i++){

                Button myB = new Button(view.getContext());
                myB.setText(specific_route_info.get(i).getRoute_id() + ": " + specific_route_info.get(i).getRoute_name() + "       " + specific_route_info.get(i).getTime());
                myB.setTextSize(16);
                myB.setGravity(Gravity.CENTER);

//                TextView tv = new TextView(view.getContext());
//                tv.setText(specific_route_info.get(i).getRoute_id() + ": " + specific_route_info.get(i).getRoute_name() + "       " + specific_route_info.get(i).getTime());
//                tv.setTextSize(16);
//                tv.setClickable(true);
//                tv.setFocusable(true);
//                tv.setGravity(Gravity.CENTER);

                GradientDrawable shape = new GradientDrawable();
                shape.setShape(GradientDrawable.RECTANGLE);
                shape.setColor(Color.parseColor("#B3E5FC"));
                shape.setStroke(10, Color.WHITE);
                shape.setCornerRadius(20);

                myB.setBackground(shape);

                final int temp = i;
                myB.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ArrayList<String> service = new ArrayList<>();
                        service.add(specific_route_info.get(temp).getService_id());
                        getDialog().dismiss();
                        MapsActivity.display_specific_route(service);
                    }
                });
                ll.addView(myB);
            }
        }

        Button b = new Button(view.getContext());
        b.setText(" Display all possible routes ");

        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setColor(Color.parseColor("#ef5350"));
        shape.setStroke(5, Color.WHITE);
        shape.setCornerRadius(20);

        b.setBackground(shape);

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> service = new ArrayList<>();
                for(String s : separate_service.keySet()){
                    service.add(separate_service.get(s).get(0).getService_id());
                }
                getDialog().dismiss();
                MapsActivity.display_specific_route(service);
            }
        });
        ll.addView(b);

        Button direction_button = new Button(view.getContext());
        direction_button.setText("Go to there");
        direction_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //MapsActivity.
            }
        });
    }

    public static int compareTime(String current, String target){ //Compare two time in HH:mm:ss format
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
