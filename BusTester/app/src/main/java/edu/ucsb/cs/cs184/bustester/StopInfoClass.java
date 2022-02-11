package edu.ucsb.cs.cs184.bustester;


import java.io.Serializable;
import java.util.ArrayList;

public class StopInfoClass implements Serializable {
    private ArrayList<MapsActivity.Route_service> time_schedule;

    public ArrayList<MapsActivity.Route_service>  getTime_schedule(){
        return time_schedule;
    }

    public void setTime_schedule(ArrayList<MapsActivity.Route_service>  target){
        time_schedule = target;
    }
}