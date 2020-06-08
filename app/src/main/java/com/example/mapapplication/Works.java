package com.example.mapapplication;


import android.location.Location;

import java.text.DecimalFormat;

public class Works {
    public String deadline , location ,message , minDist , title , currLocation , key , currUser;

    public Works()
    {

    }

    public Works(String deadline, String location, String message , String minDist , String title , String key ,String currUser) {
        this.deadline = deadline;
        this.location = location;
        this.message = message;
        this.minDist = minDist;
        this.title = title;
        this.key = key ;
        this.currUser=currUser;
    }

    public String getDeadline() {
        return deadline;
    }

    public String getLocation() {
        return  location;
    }

    public String getMessage() {
        return message;
    }

    public String getMinDist() {
        return minDist;

    }

    public String getKey(){
        return key;
    }

    public String getTitle() {
        return title;
    }

    public String getCurrUser() {
        return currUser;
    }

    public  double getDist()
    {
        String[] loca = location.split(",");
        double lati = Double.parseDouble(loca[0]);
        double longi = Double.parseDouble(loca[1]);


        String[] loca2 = currLocation.split(",");
        double lati2 = Double.parseDouble(loca2[0]);
        double longi2 = Double.parseDouble(loca2[1]);

        float[] results = new float[5];
        Location.distanceBetween(lati,longi , lati2,  longi2, results);

        DecimalFormat df2 = new DecimalFormat("#.##");
        double dist = results[0]/1000;

        return dist;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setMinDist(String minDist) {
        this.minDist = minDist;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public void setCurrUser(String currUser) {
        this.currUser = currUser;
    }






}