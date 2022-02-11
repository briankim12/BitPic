package edu.ucsb.cs.cs184.bustester.ui.home;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import edu.ucsb.cs.cs184.bustester.R;

public class NearStopRouteTimeDialog extends DialogFragment {

    private static final String TAG = "NearStopRouteTimeDialog";
    private static final String ARG_PARAM1 = "param1";

    public NearStopRouteTimeDialog () {
    }

    public static NearStopRouteTimeDialog newInstance(ArrayList<String> times) {
        NearStopRouteTimeDialog fragment = new NearStopRouteTimeDialog();
        Bundle args = new Bundle();

        args.putSerializable(ARG_PARAM1, times);
        fragment.setArguments(args);
        return fragment;
//        InfoWindowDialog fragment = new InfoWindowDialog();
//        Bundle args = new Bundle();
//
//        args.putSerializable(ARG_PARAM1, info);
//        fragment.setArguments(args);
//        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.near_stop_route_time_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if ((ArrayList<String>)getArguments().getSerializable(ARG_PARAM1) != null) {
            ArrayList<String> times = (ArrayList<String>) getArguments().getSerializable(ARG_PARAM1);

            LinearLayout ll = view.findViewById(R.id.near_stop_route_time_liearLayout);

            if (times != null && times.size() != 0) {
                Collections.sort(times);

                int maxSizeOfTime = 15;
                if (times.size() < 15) {
                    maxSizeOfTime = times.size();
                }
                for (int i = 0; i < maxSizeOfTime; i++) {

                    final Button button = new Button(view.getContext());
                    button.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    ));

                    button.setText(times.get(i));

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
                            HomeFragment.setTime(button.getText().toString());
                            getDialog().dismiss();
                        }
                    });
                }
            }
        }
    }
}
