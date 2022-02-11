package edu.ucsb.cs.cs184.bustester;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;


public class DirectionDialog extends DialogFragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public DirectionDialog() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static DirectionDialog newInstance(String title, String timestamp) {
        DirectionDialog fragment = new DirectionDialog();
        Bundle args = new Bundle();

        args.putString(ARG_PARAM1, title);
        args.putString(ARG_PARAM2, timestamp);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.direction_dialog, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }
}
