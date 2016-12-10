package com.jelanidenis.courseq;

/**
 * Created by jelanidenis on 11/5/16.
 */

public class User {

    public String name;
    public String year;
    public String major;
    //public ArrayList<String> neighbors;
    //public HashMap<String, HashMap<String, Object>> history;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String name, String year, String major) {
        this.name = name;
        this.year = year;
        this.major = major;
        //this.neighbors = null;
        //this.history = null;
    }

}
