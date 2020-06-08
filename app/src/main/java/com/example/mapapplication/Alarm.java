package com.example.mapapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Alarm extends BroadcastReceiver {
    private MediaPlayer mp;
    private int SPLASH_TIME_OUT = 3000;
    private FirebaseAuth mAuth;
    private List<Works> listData;
    private String currLocation="00,00";
    private boolean inRange;
    private boolean loopBreak ;


    @Override
    public void onReceive(final Context context, Intent intent) {
        try {

            currLocation = "00,00";
            inRange = false;


            mAuth = FirebaseAuth.getInstance();

            DatabaseReference RootRef = FirebaseDatabase.getInstance().getReference();
            final FirebaseUser currUser = mAuth.getCurrentUser();

            RootRef.child("Users").child(currUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("currLocation")  ))
                    {
                        String retrieveCurrLocation = dataSnapshot.child("currLocation").getValue().toString();
                        currLocation = retrieveCurrLocation ;
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


            DatabaseReference workRef = RootRef.child("Works").child(currUser.getUid());
            listData = new ArrayList<>();

            Toast.makeText(context, "OnReceiveAlarm", Toast.LENGTH_SHORT).show();

            workRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                            Works l = dataSnapshot1.getValue(Works.class);
                            listData.add(l);
                            //Toast.makeText(context, l.getTitle(), Toast.LENGTH_SHORT).show();

                        }
                        //while (!loopBreak) {

                            //new Handler().postDelayed(new Runnable() {
                                //@Override
                               // public void run() {
                                    Toast.makeText(context, "check!!", Toast.LENGTH_SHORT).show();
                                    checkTheList(context,listData);

                             //   }
                           // }, SPLASH_TIME_OUT);


                        //}
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


            }

        catch (Exception e) {
            Toast.makeText(context, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private void checkTheList(Context context, List<Works> listData2) {

        try {
            getDeviceLocation(context);
            for (Works var : listData2) {
                //Toast.makeText(context, var.getMinDist(), Toast.LENGTH_SHORT).show();
                if (currLocation == null) {
                    getDeviceLocation(context);
                }
                    String[] workLocation = var.getLocation().split(",");
                    double lati1 = Double.parseDouble(workLocation[0]);
                    double longi1 = Double.parseDouble(workLocation[1]);
                    String[] result = currLocation.split(",");
                    double lati2 = Double.valueOf(result[0]);
                    double longi2 =Double.valueOf(result[1]);
                    float[] results = new float[5];
                    Location.distanceBetween(lati1, longi1, lati2, longi2, results);

                    double dist = results[0] / 1000;

                    //Toast.makeText(context, String.valueOf( dist) +(var.getMinDist()), Toast.LENGTH_SHORT).show();

                    if (dist < Double.parseDouble(var.getMinDist())) {
                        inRange = true;
                        //Toast.makeText(context, var.getTitle(), Toast.LENGTH_SHORT).show();
                    }


            }
            checkForAlarm(context);
        }
        catch (Exception e)
        {
            Toast.makeText(context, "checkTheList:"+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void checkForAlarm(Context context) {
        try {
            //Toast.makeText(context, "ALARM RECEIVE" + inRange, Toast.LENGTH_SHORT).show();
            if (inRange) {
                if (mp != null)
                    mp.pause();

                Intent intent = new Intent(context,AlarmTask.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
                context.startActivity(intent);

                int z=0;
                while (z<5) {
                    mp = MediaPlayer.create(context, R.raw.expert_jatt);
                    mp.start();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mp.pause();

                        }
                    }, SPLASH_TIME_OUT);
                    z++;
                }
                loopBreak = true;
            }
        }
        catch (Exception e)
        {
            Toast.makeText(context, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
        }
    }


    public void getDeviceLocation(final Context context)
    {
        FusedLocationProviderClient mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        try {
            Task location = mFusedLocationProviderClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful())
                    {
                        Location mCurrLocation = (Location) task.getResult();
                        if(mCurrLocation!=null) {
                            Location l = mCurrLocation;
                            currLocation = String.valueOf(l.getLatitude())+","+String.valueOf(l.getLongitude());
                            //Toast.makeText(context, currLocation.toString(), Toast.LENGTH_SHORT).show();
                            //Toast.makeText(context, mCurrLocation.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    else
                    {}
                }
            });

        }
        catch (SecurityException e)
        {
        }

    }




}