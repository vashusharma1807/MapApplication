package com.example.mapapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class EditWorkActivity extends AppCompatActivity {

    private EditText title  , message , minDist ;
    private String dateTime = "";
    private Button updateButton , selectDate , selectTime ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_work);

        final String currUser =  getIntent().getExtras().get("currUser").toString();
        final String pushId = getIntent().getExtras().get("pushId").toString() ;
        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();



        title = (EditText) findViewById(R.id.edit_text1);
        message = (EditText) findViewById(R.id.edit_text3);
        minDist = (EditText) findViewById(R.id.edit_text4);
        updateButton = (Button) findViewById(R.id.add_job);
        selectDate=(Button) findViewById(R.id.select_date);
        selectTime=(Button) findViewById(R.id.select_time);

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


        String titleV = title.getText().toString();

        String messageV = message.getText().toString();
        String minDistV = minDist.getText().toString();


        final Map hashMap = new HashMap();
        hashMap.put("title",titleV);
        if(dateTime!=null)
            hashMap.put("deadline",dateTime);


        hashMap.put("message",messageV);
        hashMap.put("minDist",minDistV);


        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                rootRef.child("Works").child(currUser).child((pushId))
                        .updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        Intent intent = new Intent(EditWorkActivity.this,MainActivity.class);
                        startActivity(intent);
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



    @Override
    protected void onStart() {
        super.onStart();
        final String currUser =  getIntent().getExtras().get("currUser").toString();
        final String pushId = getIntent().getExtras().get("pushId").toString() ;
        retrieveMessage(currUser,pushId);

    }

    private void retrieveMessage(String currUser, String pushId)
    {
        Toast.makeText(this, "Retrieve", Toast.LENGTH_SHORT).show();
        DatabaseReference RootRef = FirebaseDatabase.getInstance().getReference();
        RootRef.child("Works").child( currUser ).child(pushId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("title")))
                        {
                            String retrieveUserName = dataSnapshot.child("title").getValue().toString();
                            title.setText(retrieveUserName);

                        }
                        if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("minDist")))
                        {
                            String retrieveUserName = dataSnapshot.child("minDist").getValue().toString();
                            minDist.setText(retrieveUserName);
                        }

                        if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("message")))
                        {
                            String retrieveUserName = dataSnapshot.child("message").getValue().toString();
                            message.setText(retrieveUserName);

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }
}
