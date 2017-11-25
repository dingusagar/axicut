package com.example.dingu.axicut.Admin.user;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;


import com.example.dingu.axicut.R;
import com.example.dingu.axicut.Utils.General.MyDatabase;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by dingu on 23/5/17.
 */

public class UserAdapter extends RecyclerView.Adapter<UserViewHolder> implements Filterable{

    ArrayList<User> filteredUserList;
    ArrayList<User> userList;

    DatabaseReference dbRef = MyDatabase.getDatabase().getInstance().getReference().child("Users");
    Boolean isTouched = false;

    public UserAdapter() {
        dbRef.keepSynced(true);
        userList = new ArrayList<>();
        filteredUserList = new ArrayList<>();
        fetchDataFromDatabase();
    }


    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_card_view,parent,false);

        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final UserViewHolder holder, int position) {

        final User user = filteredUserList.get(position);
        holder.setUserEmail(user.getEmail());
        holder.setName(user.getName());
        holder.setMode(user.getUserMode());
        holder.setSwitchStatus(user.isActive());
        ImageButton removeButton = (ImageButton) holder.mView.findViewById(R.id.UserRemoveButton);
        Switch isActiveSwitch = (Switch)holder.mView.findViewById(R.id.isActiveSwitch);

        isActiveSwitch.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                isTouched = true;
                return false;
            }
        });

        isActiveSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (isTouched) {
                    isTouched = false;
                    user.setActive(isChecked);
                    ProgressDialog progressDialog = new ProgressDialog(buttonView.getContext());
                    if(isChecked)
                        progressDialog.setMessage("Enabling User...");
                    else
                        progressDialog.setMessage("Disabling User...");
                    progressDialog.show();
                    updateUser(user);
                    progressDialog.dismiss();
                }
            }
        });


        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("Do you want to remove this user ??");
                builder.setCancelable(false);
                builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeUser(user.getEmail());
                    }
                });
                builder.setNegativeButton("Cancel",null);
                builder.show();
            }
        });



    }

    private void removeUser(final String email) {
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot != null)
                {
                    for(DataSnapshot childSnapshot : dataSnapshot.getChildren())
                    {
                        User user = childSnapshot.getValue(User.class);
                        if(user.getEmail() != null && user.getEmail().equals(email))
                            childSnapshot.getRef().removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredUserList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String searchKey = constraint.toString();
                if (searchKey.isEmpty())
                {
                    filteredUserList = new ArrayList<>(userList);
                }
                else {
                    ArrayList<User> filterlist = new ArrayList<>();
                    for(User user : userList)
                    {
                        String concat = user.getName() +" " + user.getEmail() + " " + user.getUserMode();
                        concat = concat.toLowerCase();
                        if(concat.contains(searchKey.toLowerCase()))
                            filterlist.add(user);
                    }
                    filteredUserList = filterlist;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredUserList;
                return filterResults;

            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                filteredUserList = (ArrayList<User>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public void fetchDataFromDatabase()
    {
        dbRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot != null && dataSnapshot.getValue() != null)
                {
                    try{
                        User user = dataSnapshot.getValue(User.class);
                        if(!userList.contains(user))
                        {
                            userList.add(0,user);
                            filteredUserList.add(0,user);
                            notifyDataSetChanged();
                        }

                    }catch (Exception e)
                    {
//                        Toast.makeText(getApplicationContext(), "Error : " + e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                if(dataSnapshot != null)
                {
                    User user = dataSnapshot.getValue(User.class);

                    removeAndNotify(user);

                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void removeAndNotify(User user) {

        String userEmail = user.getEmail();

        for(int i=0; i<userList.size();i++)
        {
            if(userList.get(i).getEmail().equals(userEmail))
            {
                userList.remove(i);


            }
        }

        for(int i=0; i<filteredUserList.size();i++)
        {
            if(filteredUserList.get(i).getEmail().equals(userEmail))
            {
                filteredUserList.remove(i);
            }
        }
        notifyDataSetChanged();

    }

    private void updateUser(final User user){
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot != null)
                {
                    for(DataSnapshot childSnapshot : dataSnapshot.getChildren())
                    {
                        User userDb = childSnapshot.getValue(User.class);
                        if(userDb.getEmail() != null && userDb.getEmail().equals(user.getEmail()))
                            childSnapshot.getRef().setValue(user);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
