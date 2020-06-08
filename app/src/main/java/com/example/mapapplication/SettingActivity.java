package com.example.mapapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;




public class SettingActivity extends AppCompatActivity
{
    private Button UpdateAccountSettings;
    private EditText userName, userStatus;


    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef , mDatabaseRef;
    private Location currLocation ;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private Toolbar SettingsToolBar;
    private ProgressDialog loadingBar ;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);


        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();

        //now//
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Notes").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(currentUserID);



        InitializeFields();


        userName.setVisibility(View.INVISIBLE);


        UpdateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                UpdateSettings();
            }
        });


        RetrieveUserInfo();



    }


    private void InitializeFields()
    {
        UpdateAccountSettings = (Button) findViewById(R.id.update_settings_button);
        userName = (EditText) findViewById(R.id.set_username);
        userStatus = (EditText) findViewById(R.id.set_profile_status);
        getDeviceLocation();
        /*loadingBar = new ProgressDialog(this);

        SettingsToolBar = (Toolbar) findViewById(R.id.settings_Toolbar);
        setSupportActionBar(SettingsToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Account Settings")*/;
    }






    private void UpdateSettings()
    {
        String setUserName = userName.getText().toString();
        String setStatus = userStatus.getText().toString();

        if (TextUtils.isEmpty(setUserName))
        {
            Toast.makeText(this, "Please write your user name first....", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(setStatus))
        {
            Toast.makeText(this, "Please write your status....", Toast.LENGTH_SHORT).show();
        }
        else
        {
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid", currentUserID);
            profileMap.put("name", setUserName);
            profileMap.put("status", setStatus);
            profileMap.put("currLocation",currLocation.toString());

            RootRef.child("Users").child(currentUserID).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if (task.isSuccessful())
                            {
                                SendUserToMainActivity();
                                Toast.makeText(SettingActivity.this, "Profile Updated Successfully...", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                String message = task.getException().toString();
                                Toast.makeText(SettingActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }



    private void RetrieveUserInfo()
    {
        RootRef.child("Users").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name") && (dataSnapshot.hasChild("status")) ))
                        {
                            String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                            String retrievesStatus = dataSnapshot.child("status").getValue().toString();



                            userName.setText(retrieveUserName);
                            userStatus.setText(retrievesStatus);

                        }

                        else
                        {
                            userName.setVisibility(View.VISIBLE);
                            Toast.makeText(SettingActivity.this, "Please set & update your profile information...", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }



    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(SettingActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
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
                        //Toast.makeText(MainActivity.this, "CurrLocation:"+mCurrLocation.toString(), Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        //Toast.makeText(SettingActivity.this, "Unable to get Location", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
        catch (SecurityException e)
        {
            //Toast.makeText(this, "Security Exception:"+e.getMessage().toString(), Toast.LENGTH_SHORT).show();
        }

    }


}
