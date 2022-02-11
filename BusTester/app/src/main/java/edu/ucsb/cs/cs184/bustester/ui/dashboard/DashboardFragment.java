package edu.ucsb.cs.cs184.bustester.ui.dashboard;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.w3c.dom.Text;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import edu.ucsb.cs.cs184.bustester.MainActivity;
import edu.ucsb.cs.cs184.bustester.R;
import edu.ucsb.cs.cs184.bustester.ui.home.HomeFragment;
import edu.ucsb.cs.cs184.bustester.ui.home.NearStopRouteTimeDialog;

import static android.content.Context.MODE_PRIVATE;
import static edu.ucsb.cs.cs184.bustester.R.id.accessibility_action_clickable_span;
import static edu.ucsb.cs.cs184.bustester.R.id.display_timer;
import static edu.ucsb.cs.cs184.bustester.R.id.icon;

public class DashboardFragment extends Fragment {

    private final String CHANNEL_ID = "nearby_notification";
    private final int NOTIFICATION_ID = 001;

    private DashboardViewModel dashboardViewModel;
    private TextView timeTv;
    private TextView timeDiffTV;
    private TextView timeCurrTV;
    private TextView mTextViewCountDown;
    private Spinner spinner;
    private FloatingActionButton play_fb;
    private FloatingActionButton cancel_fb;


    // https://www.youtube.com/watch?v=lvibl8YJfGo&list=PLrnPJCHvNZuB8wxqXCwKw2_NkyEmFwcSd&index=3
    private long START_TIME_IN_MILLIS = 0;
//    private long START_TIME_IN_MILLIS = 1000;
    private CountDownTimer countDownTimer;
    private boolean mTimerRunning;
//    private long mTimerLeftInMillis = START_TIME_IN_MILLIS;
    private long mTimerLeftInMillis;
    private long mEndTime;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                ViewModelProviders.of(this).get(DashboardViewModel.class);
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        timeTv = root.findViewById(R.id.time_dashboard);
        timeTv.setText("You seleted time: " + HomeFragment.getTime());
        timeCurrTV = root.findViewById(R.id.time_curr_dashboard);
//        timeDiffTV = root.findViewById(R.id.time_diff_dashboard);
        spinner = root.findViewById(R.id.time_spinner);



        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"));
        SimpleDateFormat hms = new SimpleDateFormat("HH:mm:ss");
        String getCurrentTime = hms.format(c.getTime());


        mTextViewCountDown = getActivity().findViewById(R.id.display_timer);
        timeCurrTV.setText("Current time: " + getCurrentTime);

        int busTimeMin = toMins(HomeFragment.getTime());
        if (busTimeMin != 0) {
            int currentTimeMin = toMins(String.valueOf(getCurrentTime));
            final long[] diffMin = {busTimeMin - currentTimeMin};

            if (diffMin[0] > 0) {
                START_TIME_IN_MILLIS = diffMin[0] * 60000;
                mTimerLeftInMillis = START_TIME_IN_MILLIS;
                updateCountDownText();
            }


            List<String> minList = new ArrayList<>();
            for (int i = 1; i <= 60; i++) {
                String min = String.valueOf(i) + " min";
                minList.add(min);
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, minList);
            spinner.setAdapter(adapter);

            spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (!mTimerRunning) {
                        Log.d("spinner", "pos--->" + position + "id" + id);

                        long diffTemp = diffMin[0] - id - 1;
                        START_TIME_IN_MILLIS = diffTemp * 60000;
                        mTimerLeftInMillis = START_TIME_IN_MILLIS;
//                    timeDiffTV.setText(String.valueOf(diffMin[0]));
                        updateCountDownText();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });


            // ---------------------------------------------------------
            play_fb = getActivity().findViewById(R.id.play_fb_timer);
            cancel_fb = getActivity().findViewById(R.id.cancel_fb_timer);
            play_fb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startTimer();
                }
            });

            cancel_fb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    resetTimer();
                }
            });
        }
//        updateCountDownText();
    }

    private void sendNotification() {
        // send notiti for user
        ((MainActivity)getActivity()).createNotificationChannel();

        Intent mainIntent = new Intent(getContext(), MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent mainPendingIntent = PendingIntent.getActivity(getContext(), 0,mainIntent,PendingIntent.FLAG_ONE_SHOT);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), CHANNEL_ID);

        builder.setSmallIcon(R.drawable.ic_notifications_black_24dp);

        builder.setContentTitle("Time for Bus");
        builder.setContentText("It is time for bus!");
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setAutoCancel(true);
        //Notification onClick
        builder.setContentIntent(mainPendingIntent);
        //Vibrate if sound if turned off
        builder.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000, 1000, 1000});
        //Notification Sound
        Notification notification = builder.build();
        notification.defaults = Notification.DEFAULT_SOUND;
        //Notification Initialize
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getContext());
        notificationManagerCompat.notify(NOTIFICATION_ID, builder.build());

    }

    private void startTimer() {
        mEndTime = System.currentTimeMillis() + mTimerLeftInMillis;

        countDownTimer = new CountDownTimer(mTimerLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimerLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                sendNotification();

                mTimerRunning = false;
            }
        }.start();
        mTimerRunning = true;

    }
    private void resetTimer() {
        mTimerLeftInMillis = START_TIME_IN_MILLIS;
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        mTimerRunning = false;
        updateCountDownText();

    }
    private void updateCountDownText() {
        int hours = (int) (mTimerLeftInMillis / 1000) / 3600;
//        int minutes = (int) (mTimerLeftInMillis / 1000) / 60;
        int minutes = (int) (mTimerLeftInMillis / 1000) % 3600 / 60;
        int seconds = (int) (mTimerLeftInMillis / 1000) % 60;

        String timeLeftFormatted = String.format("%02d:%02d:%02d",hours, minutes,seconds);
        mTextViewCountDown.setText(timeLeftFormatted);
    }


    @Override
    public void onStop() {
        super.onStop();

        SharedPreferences prefs = getActivity().getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("millisLeft", mTimerLeftInMillis);
        editor.putBoolean("timerRunning", mTimerRunning);
        editor.putLong("endTime", mEndTime);

        editor.apply();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }


    @Override
    public void onStart() {
        super.onStart();

        SharedPreferences prefs = getActivity().getSharedPreferences("prefs", MODE_PRIVATE);

        mTimerLeftInMillis = prefs.getLong("millisLeft", START_TIME_IN_MILLIS);
        mTimerRunning = prefs.getBoolean("timerRunning", false);

//        updateCountDownText();

        if (mTimerRunning) {
            mEndTime = prefs.getLong("endTime", 0);
            mTimerLeftInMillis = mEndTime - System.currentTimeMillis();

            if (mTimerLeftInMillis < 0) {
                mTimerLeftInMillis = 0;
                mTimerRunning = false;
                updateCountDownText();
            } else {
                startTimer();
            }
        } else {
            if (START_TIME_IN_MILLIS == 0) {
                mTimerLeftInMillis = START_TIME_IN_MILLIS;
                updateCountDownText();
            }
        }
    }

    private static int toMins(String s) {
         String[] hourMin = s.split(":");
         int hour = Integer.valueOf(hourMin[0]);
         int mins = Integer.valueOf(hourMin[1]);
         int hoursInMins = hour * 60;
         return hoursInMins + mins;
    }
}