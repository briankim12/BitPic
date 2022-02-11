package edu.ucsb.cs.cs184.bustester;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.TimePicker;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.FragmentManager;

import java.util.Calendar;

public class ShowUserMarkerChoiceDialog extends AppCompatDialogFragment{
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        float distance = MapsActivity.getMarker_user_distance();
        int time_to_stop = (int)(distance / 100);
        if(time_to_stop < 15){
            builder.setTitle("Navigation").setMessage("Do you want to start from there?" + "\n"
                    + "(You need approximately " + time_to_stop + " minutes to reach this stop)" + "\n"
                    + "If so, please then tap a marker as your destination")
                    .setPositiveButton("Yes, go at now", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MapsActivity.setUser_defined_time(""); //will use default current time
                        }
                    })
                    .setNegativeButton("Yes, let me specify a time", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Calendar current_time = Calendar.getInstance();
                            int hour = current_time.get(Calendar.HOUR_OF_DAY);
                            int minute = current_time.get(Calendar.MINUTE);
                            TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                                @Override
                                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                    String time = hourOfDay+":"+minute+":00";
                                    MapsActivity.setUser_defined_time(time);
                                }
                            },hour,minute,true);
                            timePickerDialog.show();
                        }
                    })
                    .setNeutralButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Remove the temp marker
                            MapsActivity.remove_temp_direction_marker();
                        }
                    });
        }
        else{
            builder.setTitle("Navigation").setMessage("Do you want to start from there?" + "\n"
                    + "(You can't reach this stop in 15 minutes!)" + "\n"
                    + "If so, please then tap a marker as your destination")
                    .setPositiveButton("Yes, go at now", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MapsActivity.setUser_defined_time(""); //will use default current time
                        }
                    })
                    .setNegativeButton("Yes, let me specify a time", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Calendar current_time = Calendar.getInstance();
                            int hour = current_time.get(Calendar.HOUR_OF_DAY);
                            int minute = current_time.get(Calendar.MINUTE);
                            TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                                @Override
                                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                    String time = hourOfDay+":"+minute+":00";
                                    MapsActivity.setUser_defined_time(time);
                                }
                            },hour,minute,true);
                            timePickerDialog.show();
                        }
                    })
                    .setNeutralButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Remove the temp marker
                            MapsActivity.remove_temp_direction_marker();
                        }
                    });
        }

        return builder.create();
    }
}

