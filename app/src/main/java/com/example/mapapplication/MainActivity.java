package com.example.mapapplication;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION=1024;
    private FirebaseAuth mAuth ;
    private DatabaseReference Rootref;
    private FirebaseUser currUser ;
    private RecyclerView recyclerView ;
    private List<Works> listData;
    private MyAdapter adapter ;
    private Location currLocation ;
    private FusedLocationProviderClient mFusedLocationProviderClient ;
    private Boolean proximityFlag ;
    Intent broadcastIntent;
    private Alarm mAlarm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
            try {

                mAlarm = new Alarm();
                broadcastIntent = new Intent(this, mAlarm.getClass());

                if(isMyServiceRunning(Alarm.class))
                    stopService(broadcastIntent);

                proximityFlag = false;
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_main);

                this.setVolumeControlStream(AudioManager.STREAM_ALARM);


                mAuth = FirebaseAuth.getInstance();
                Rootref = FirebaseDatabase.getInstance().getReference();

                currUser = mAuth.getCurrentUser();

                recyclerView = (RecyclerView) findViewById(R.id.main_recycler_view);
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
            }
            catch (Exception e)
            {
                Toast.makeText(this, "create:"+e.getMessage(), Toast.LENGTH_SHORT).show();
            }


    }

    private boolean isMyServiceRunning(Class<?> alarmClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (alarmClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onStart() {
        try {
            super.onStart();

            FirebaseUser currentUser = mAuth.getCurrentUser();

            getDeviceLocation();

            if (currentUser == null) {
                sendUserToLoginActivity();

            } else {
                verifyUserExistance();
            }


            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }


            final Context context = MainActivity.this;


            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            boolean gps_enabled = false;
            boolean network_enabled = false;

            try {
                gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            } catch (Exception ex) {
            }

            try {
                network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            } catch (Exception ex) {
            }

            if (!gps_enabled && !network_enabled) {
                buildAlertMessageNoGps();
            }
        }
        catch (Exception e)
        {
            Toast.makeText(this, "Start:"+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            broadcastIntent = new Intent(this,Alarm.class);
            broadcastIntent.setAction("restartservice");
            broadcastIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            broadcastIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.sendBroadcast(broadcastIntent);
        }
        catch (Exception e)
        {
            Toast.makeText(this, "OnDestroy"+e.getMessage().toString(), Toast.LENGTH_SHORT).show();
        }
    }

   /* private void createAlarm() {

        Toast.makeText(this, "createAlarm", Toast.LENGTH_SHORT).show();
        //Create a new PendingIntent and add it to the AlarmManager
        Intent intent = new Intent(MainActivity.this, Alarm.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(),0 ,intent, 0);
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+5000, pendingIntent);

    }*/


    private  void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Yout GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void retreiveWorks() {

        try {
            double lati = currLocation.getLatitude();
            double longi = currLocation.getLongitude();
            String location = String.valueOf(lati) + "," + String.valueOf(longi);
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("currLocation", location);


            Rootref.child("Users").child(currUser.getUid()).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) { } else { }
                        }
                    });

            DatabaseReference workRef = Rootref.child("Works").child(currUser.getUid());
            listData = new ArrayList<>();


            workRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                            Works l = dataSnapshot1.getValue(Works.class);
                            listData.add(l);
                        }



                        Collections.sort(listData, new Comparator<Works>() {
                            @Override
                            public int compare(Works lhs, Works rhs) {
                                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                                if(!proximityFlag) {
                                    if (lhs.getDist() <= Double.parseDouble(lhs.getMinDist())) {
                                        proximityFlag = true;
                                    }
                                    if (rhs.getDist() < Double.parseDouble(rhs.getMinDist())) {
                                        proximityFlag = true;
                                    }
                                }
                                String s1 = lhs.deadline;
                                String s2 = rhs.deadline;
                                try {
                                    Date date1=new SimpleDateFormat("dd-MM-yyyy HH:mm").parse(s1);
                                    Date date2=new SimpleDateFormat("dd-MM-yyyy HH:mm").parse(s2);
                                    int k = date1.compareTo(date2);
                                    return k;
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                return 0 ;

                            }
                        });

                        adapter = new MyAdapter(MainActivity.this , listData);
                        recyclerView.setAdapter(adapter);

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }

    }


    public void getDeviceLocation()
    {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            Task location = mFusedLocationProviderClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful())
                    {
                        Location mCurrLocation = (Location) task.getResult();
                        if(mCurrLocation!=null)
                            currLocation = mCurrLocation ;
                           retreiveWorks();
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

    private void verifyUserExistance() {

            String currentUserID = mAuth.getCurrentUser().getUid();
            Rootref.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if ((dataSnapshot.child("name").exists())) {
                    } else {
                        sendUserToSettingActivity();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });

    }

    private void sendUserToSettingActivity() {
        Intent settingsActivity = new Intent(MainActivity.this,SettingActivity.class);
        startActivity(settingsActivity);
    }

    private void sendUserToLoginActivity() {
        Intent loginActivity = new Intent(MainActivity.this,LoginActivity.class);
        loginActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
        startActivity(loginActivity);
        finish();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }
            }
        }

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId()==R.id.main_logout_option) {
            mAuth.signOut();
            sendUserToLoginActivity();
        }
        if(item.getItemId()==R.id.main_settings_option)
        {
            sendUserToSettingActivity();
        }

        if(item.getItemId()==R.id.add_work_option)
        {
            Intent addWork = new Intent(MainActivity.this, MapWorkActivity.class);
            startActivity(addWork);
        }

        if(item.getItemId()==R.id.open_map_option)
        {
            Intent enterMap = new Intent(MainActivity.this, MapsActivity.class);
            startActivity(enterMap);
        }

        return true;
    }

}
