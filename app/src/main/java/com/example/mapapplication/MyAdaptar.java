package com.example.mapapplication;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.acos;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;
import static java.util.Collections.sort;

class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder>{

    private List<Works>listData;
    private Context context ;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance() ;
    private String locationOf="00,00";
    private DatabaseReference RootRef ;
    private  String currentUserID;
    private Location currLocation ;

    public MyAdapter(Context c , List<Works> listData) {
        this.context = c;
        this.listData = listData;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.list_data,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        Works ld=listData.get(position);




        holder.txtTitle.setText(ld.getTitle()+":"+ld.getMessage());

        try {


            FusedLocationProviderClient mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
            try {
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Location mCurrLocation = (Location) task.getResult();
                            if (mCurrLocation != null) {
                                currLocation = mCurrLocation;
                                retrieve(holder, position);
                            }
                        }
                        else
                        {
                        }
                    }
                });

            }
            catch (SecurityException e)
            { }

        }
        catch (Exception e) {
            holder.txtTitle.setText(e.getMessage().toString());
        }

        holder.txtDeadline.setText(ld.getDeadline());

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                alertDialog(holder,position);

                return false;
            }
        });

    }

    private void alertDialog(final ViewHolder holder , final int position ) {

        CharSequence options[] = new CharSequence[]
                {
                        "Mark As Done",
                        "Edit",
                        "Cancel"
                };
        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
        builder.setTitle("Delete Message ?");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                if(i==0)
                {
                    Toast.makeText(context, "i=0", Toast.LENGTH_SHORT).show();
                    deleteWork(position,holder);
                    Intent intent = new Intent(holder.itemView.getContext(),MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
                    holder.itemView.getContext().startActivity(intent);
                }
                else if(i==1)
                {

                    Toast.makeText(context, "i=1", Toast.LENGTH_SHORT).show();
                    editWork(position,holder);
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(listData.get(position).getMessage()));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
                    holder.itemView.getContext().startActivity(intent);

                }


            }
        });
        builder.show();
    }

    private void deleteWork(int position, final ViewHolder holder) {
        Toast.makeText(context, "deleteWork", Toast.LENGTH_SHORT).show();

        try {



        Toast.makeText(context, listData.get(position).getKey() , Toast.LENGTH_SHORT).show();
        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        Toast.makeText(context, (listData.get(position).getCurrUser()) , Toast.LENGTH_SHORT).show();

            rootRef.child("Works").child( listData.get(position).getCurrUser() ).child(listData.get(position).getKey())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(holder.itemView.getContext(), "Message Deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(holder.itemView.getContext(), "Could not delete message", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }
        catch (Exception e)
        {
            Toast.makeText(context, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private void editWork(int position, final ViewHolder holder) {

        Toast.makeText(context, "editWork", Toast.LENGTH_SHORT).show();

        try {
            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

            Intent intent = new Intent(context, EditWorkActivity.class);
            intent.putExtra("currUser",  listData.get(position).getCurrUser());
            intent.putExtra("pushId", listData.get(position).getKey());
            holder.itemView.getContext().startActivity(intent);

        } catch (Exception e) {
            Toast.makeText(context, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
        }
    }




    private void retrieve(ViewHolder holder, int position) {

        Works ld=listData.get(position);
        double clong = currLocation.getLongitude();
        double clat = currLocation.getLatitude();

        String[] latLng = ld.getLocation().split(",");

        double latitude = Double.parseDouble(latLng[0]);
        double longitude = Double.parseDouble(latLng[1]);

        float[] results = new float[5];
        Location.distanceBetween(latitude,longitude , clat,  clong, results);

        DecimalFormat df2 = new DecimalFormat("#.##");
        double dist = results[0]/1000;
        String dist1 = df2.format(dist);

        holder.txtDist.setText(dist1+"Km");

        double minD = Double.parseDouble(ld.getMinDist());


        if (dist< minD)
            holder.imageView.setImageResource(R.drawable.bell);
        else
            holder.imageView.setImageResource(R.drawable.online);

    }


    @Override
    public int getItemCount() {
        return listData.size();
    }

    public double CalculationByDistance(double lat1,double lat2,double lon1 ,double lon2 ) {

            double l1 = toRadians(lat1);
            double l2 = toRadians(lat2);
            double g1 = toRadians(lon1);
            double g2 = toRadians(lon2);

            double dist = acos(sin(l1) * sin(l2) + cos(l1) * cos(l2) * cos(g1 - g2));
            if(dist < 0) {
                dist = dist + Math.PI;
            }

            return Math.round(dist * 6378100);
        }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView txtDeadline,txtTitle,txtDist;
        private ImageView imageView ;
        public ViewHolder(View itemView) {
            super(itemView);
            txtTitle=(TextView)itemView.findViewById(R.id.title_text);
            txtDeadline=(TextView)itemView.findViewById(R.id.deadline_text);
            txtDist=(TextView)itemView.findViewById(R.id.dist_text);
            imageView = (ImageView) itemView.findViewById(R.id.urgency_image);
        }
    }
}
