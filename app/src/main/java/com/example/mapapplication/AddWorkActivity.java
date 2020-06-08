package com.example.mapapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddWorkActivity extends AppCompatActivity {

    private EditText title  , message , minDist ;
    private String dateTime;
    private Button addWork , selectDate , selectTime ;
    private FirebaseUser currUser ;
    private FirebaseAuth mAuth ;
    private DatabaseReference RootRef ;
    private Location mcurrLocation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_work);

        mAuth = FirebaseAuth.getInstance();
        currUser = mAuth.getCurrentUser();
        RootRef = FirebaseDatabase.getInstance().getReference();

        final String currLocation =  getIntent().getExtras().get("location").toString();

        dateTime = new String();

        initiate();


         FusedLocationProviderClient mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            try {
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful())
                        {
                            Location mCurrLocation = (Location) task.getResult();
                            if(mCurrLocation!=null)
                                mcurrLocation = mCurrLocation ;

                        }
                        else
                        {}
                    }
                });

            }
            catch (SecurityException e)
            {
                //Toast.makeText(this, "Security Exception:"+e.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }



            selectDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onDeadlineClick();
                }
            });

            selectTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onTimeClick();
                }
            });


        addWork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String titleV = title.getText().toString();


                String deadlineV = dateTime;
                String messageV = message.getText().toString();
                String minDistV = minDist.getText().toString();

                DatabaseReference userWorkKeyRef=RootRef.child("Works").child(currUser.getUid()).push();
                String PushId= userWorkKeyRef.getKey();

                String loca = String.valueOf(mcurrLocation.getLatitude())+","+String.valueOf(mcurrLocation.getLongitude());

                Map messageTextBody = new HashMap();
                messageTextBody.put("title",titleV);
                messageTextBody.put("deadline",deadlineV);
                messageTextBody.put("message",messageV);
                messageTextBody.put("minDist",minDistV);
                messageTextBody.put("location",currLocation);
                messageTextBody.put("currLocation",loca);
                messageTextBody.put("key",PushId);
                messageTextBody.put("currUser",currUser.getUid());


                Map messageTextDetails = new HashMap();
                messageTextDetails.put(currUser.getUid()+"/"+PushId,messageTextBody);

                RootRef.child("Works").updateChildren(messageTextDetails).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful())
                        {
                            Intent back = new Intent(AddWorkActivity.this ,MapWorkActivity.class);
                            back.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
                            startActivity(back);
                            finish();
                        }
                        else
                        {
                            Toast.makeText(AddWorkActivity.this, "Error", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

            }
        });

    }

    public void onDeadlineClick() {

        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);


        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {

                        dateTime+=(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);

                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    public void onTimeClick()
    {

            // Get Current Time
            final Calendar c = Calendar.getInstance();
            int mHour = c.get(Calendar.HOUR_OF_DAY);
            int mMinute = c.get(Calendar.MINUTE);

            // Launch Time Picker Dialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    new TimePickerDialog.OnTimeSetListener() {

                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay,
                                              int minute) {

                            dateTime+=(" "+hourOfDay + ":" + minute);
                        }
                    }, mHour, mMinute, false);
            timePickerDialog.show();

    }


    private void initiate() {
        title = (EditText) findViewById(R.id.edit_text1);
        message = (EditText) findViewById(R.id.edit_text3);
        minDist = (EditText) findViewById(R.id.edit_text4);
        addWork = (Button) findViewById(R.id.add_job);
        selectDate=(Button) findViewById(R.id.select_date);
        selectTime=(Button) findViewById(R.id.select_time);

    }
}
