package edu.ucsb.cs.cs184.bustester;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class user_schedule_dialog extends DialogFragment {

    private static GradientDrawable shapeB;
    private static GradientDrawable shapeR;

    //private static final String ARG_PARAM1 = "param1";
    //private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private Schedule_info_class mParam1;
    //private String mParam2;

    public user_schedule_dialog() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static user_schedule_dialog newInstance() {
        user_schedule_dialog fragment = new user_schedule_dialog();

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.user_schedule_dialog, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        shapeB = new GradientDrawable();
        shapeB.setShape(GradientDrawable.RECTANGLE);
        shapeB.setColor(Color.parseColor("#B3E5FC"));
        shapeB.setStroke(10, Color.WHITE);
        shapeB.setCornerRadius(20);

        shapeR = new GradientDrawable();
        shapeR.setShape(GradientDrawable.RECTANGLE);
        shapeR.setColor(Color.parseColor("#F8BBD0"));
        shapeR.setStroke(10, Color.WHITE);
        shapeR.setCornerRadius(20);


        final LinearLayout ll = view.findViewById(R.id.schedule_ll);
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("user_schedules", 0);
        String keys_primitive = sharedPreferences.getString("key", null);
        if(keys_primitive == null){
            TextView tv = new TextView(view.getContext());
//            Button b1 = new Button(view.getContext());
           /* Typeface tf = Typeface.createFromAsset(getActivity().getAssets(),
                    "fonts/Arial.otf");
            tv.setTypeface(tf);*/
            tv.setBackground(shapeB);
            tv.setText("  Currently, there are no schedules.  \n  You can try to add it in direction mode!  ");
            ll.addView(tv);
            return;
        }
        String[] key_arr = keys_primitive.split(";");

        Log.d("Long Message", "onViewCreated: primitive " + keys_primitive);
        int schedule_count = 0;
        String newKey = "";

        for(int i = 0; i < key_arr.length; i++){
            schedule_count++;
            Log.d("Long Message", "onViewCreated: " + key_arr[i]);
            final String current_key = key_arr[i];
            String primitive_schedule = sharedPreferences.getString(key_arr[i], null);
            if(primitive_schedule == null){
                schedule_count--;
            }
            else{
                String[] schedule_arr = primitive_schedule.split(";");
                if(schedule_arr.length == 3){
                    newKey = newKey + current_key + ";";
//                    final TextView tv = new TextView(view.getContext());
                    final Button b2 = new Button(view.getContext());
                    b2.setBackground(shapeB);
                    b2.setText("Schedule " + schedule_count + " :" + "\n" + "    Line: " + schedule_arr[1] + "   Time:  " + schedule_arr[0]
                            + "\n" + "    Arrive time: " + schedule_arr[2] + "\n");
                    b2.setClickable(true);
                    b2.setFocusable(true);
                    b2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            SharedPreferences preferences = getContext().getSharedPreferences("user_schedules", 0);
                            preferences.edit().remove(current_key).commit();
                            ll.removeView(b2);
                        }
                    });
                    ll.addView(b2);
                }
                else if(schedule_arr.length == 8){
                    newKey = newKey + current_key + ";";
//                    final TextView tv = new TextView(view.getContext());
                    final Button b3 = new Button(view.getContext());
                    b3.setBackground(shapeB);
                    b3.setText("  Schedule " + schedule_count + " :" + "\n" + "    Line " + schedule_arr[1] + "   Time:  " + schedule_arr[0]
                            + "\n" + "    Take off Stop:" + schedule_arr[3] + "   Time:  " + schedule_arr[2] + "\n" + "    Transit Line: " + schedule_arr[5] + "  Transit Stop: " + schedule_arr[6] + "\n     Time:  " + schedule_arr[4]
                            + "\n" + "    Arrive Time: " + schedule_arr[7] + "\n");
                    b3.setClickable(true);
                    b3.setFocusable(true);
                    b3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            SharedPreferences preferences = getContext().getSharedPreferences("user_schedules", 0);
                            preferences.edit().remove(current_key).commit();
                            ll.removeView(b3);
                        }
                    });
                    ll.addView(b3);
                }
                else{
                    schedule_count--;
                    Log.d("Long Message", "onViewCreated: " + "Undefined behavior");
                }
            }
            Log.d("Long Message", "onViewCreated: " + primitive_schedule);
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("key", newKey); //Update key
        editor.commit();
    }
}

