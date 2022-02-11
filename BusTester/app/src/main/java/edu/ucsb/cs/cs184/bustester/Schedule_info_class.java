package edu.ucsb.cs.cs184.bustester;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class Schedule_info_class implements Serializable {
    private HashMap<String, ArrayList<ArrayList<String>>> schedule_info;

    public Schedule_info_class() {}

    public HashMap<String, ArrayList<ArrayList<String>>> getSchedule_info() { return schedule_info;}

    public void setSchedule_info(HashMap<String, ArrayList<ArrayList<String>>> info) {schedule_info = info;}
}
