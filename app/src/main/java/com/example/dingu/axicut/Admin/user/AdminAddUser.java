package com.example.dingu.axicut.Admin.user;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.dingu.axicut.R;
import com.example.dingu.axicut.UserMode;
import com.example.dingu.axicut.Utils.General.MyDatabase;
import com.example.dingu.axicut.Utils.Navigation.Projector;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminAddUser extends AppCompatActivity {


    UserMode userMode ;  // An enum of user modes
    boolean radioButtonChecked = false;
    ProgressDialog progress;

    private FirebaseAuth mAuth;
    private DatabaseReference mdatabaseRefUsers;

    Vibrator vibrator;
    int VIBRATE_DURATION = 100;

    private EditText nameField;
    private EditText emailField;
    private EditText passwordField;
    Button addUser;
    RadioGroup usermodRadioGroup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_user);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        mAuth = FirebaseAuth.getInstance();
        mdatabaseRefUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        progress = new ProgressDialog(this);

        nameField = (EditText)findViewById(R.id.name);
        emailField = (EditText)findViewById(R.id.email);
        passwordField = (EditText)findViewById(R.id.password);
        addUser = (Button)findViewById(R.id.signUp);
        usermodRadioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        addUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertAndRegister(view);

            }
        });


//        if this is a update to an existing user, the intent contains the required values
       Bundle extras = getIntent().getExtras();
       if(extras !=null && extras.containsKey(String.valueOf(R.string.existingUser)))
       {
           final User existingUser = (User) extras.getSerializable(String.valueOf(R.string.existingUser));
           updateUIFieldsFromBundle(existingUser);

           addUser.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   updateUserDetailsToDB(existingUser);
               }
           });


       }

    }

    private void updateUserDetailsToDB(final User existingUser) {

        final String username = nameField.getText().toString();
        final UserMode updatedUserMod;

        switch (usermodRadioGroup.getCheckedRadioButtonId())
        {
            case R.id.radio_admin:
                updatedUserMod = UserMode.ADMIN;
                break;
            case R.id.radio_inward:
                updatedUserMod = UserMode.INWARD;
                break;
            case R.id.radio_design:
                updatedUserMod = UserMode.DESIGN;
                break;
            case R.id.radio_production:
                updatedUserMod = UserMode.PRODUCTION;
                break;
            default:
                updatedUserMod = UserMode.PRODUCTION; // This will never happen


        }
        mdatabaseRefUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot userSnapshot: dataSnapshot.getChildren())
                {
                    User user = userSnapshot.getValue(User.class);
                    if(user.getEmail().equals(existingUser.getEmail()))
                    {

                        user.setName(username);
                        user.setUserMode(updatedUserMod);
                        final String userID = userSnapshot.getKey();
                        progress.setMessage("Updating User..");
                        progress.show();
                        mdatabaseRefUsers.child(userID).removeValue();
                        mdatabaseRefUsers.child(userID).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                existingUser.setName(username);
                                existingUser.setUserMode(updatedUserMod);
                                progress.dismiss();
                                Toast.makeText(getApplicationContext(),"Updated user details",Toast.LENGTH_LONG);
                                onBackPressed();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progress.dismiss();
                                Toast.makeText(getApplicationContext(),"updating Failed",Toast.LENGTH_LONG);

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void updateUIFieldsFromBundle(User existingUser) {

        emailField.setText(existingUser.getEmail());
        emailField.setFocusable(false); // should not update email
        passwordField.setVisibility(View.GONE); // No password filed while updating user
        nameField.setText(existingUser.getName());

        switch (existingUser.getUserMode())
        {
            case INWARD:
                usermodRadioGroup.check(R.id.radio_inward);
                break;
            case DESIGN:
                usermodRadioGroup.check(R.id.radio_design);
                break;
            case PRODUCTION:
                usermodRadioGroup.check(R.id.radio_production);
                break;
            case ADMIN:
                usermodRadioGroup.check(R.id.radio_admin);
                break;


        }
        addUser.setText("Update User");


    }

    private void alertAndRegister(View view) {
        vibrator.vibrate(VIBRATE_DURATION);
        final Button button = (Button)view;
        new AlertDialog.Builder(AdminAddUser.this)
                .setTitle("Confirm User Entry")
                .setMessage("Do you want to add this user?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
//
                        if (button.isEnabled()) {
                            button.setEnabled(false);
                            button.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.button_disabled_text_color));
                        }
                        registerNewUser();
                    }})
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        button.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.button_enabled_text_color));
                    }
                }).show();
    }


    private void registerNewUser() {
        final String name = nameField.getText().toString().trim();
        final String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        final FirebaseAuth tempAuth;
         tempAuth=FirebaseAuth.getInstance(MyDatabase.TempAuthCreator.getTempAuth(getApplicationContext()));
        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && radioButtonChecked) {
            progress.setMessage("Adding new user..");
            progress.show();
            tempAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        User user = new User(email,name,userMode);
                        String userId = tempAuth.getCurrentUser().getUid();
                        mdatabaseRefUsers.child(userId).setValue(user);
                        progress.dismiss();
                        tempAuth.signOut();
                        onBackPressed();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progress.dismiss();
//                    Log.e("App","Error sign up " + e);
                    Toast.makeText(getApplicationContext(), "Oops : Error - " + e.toString(), Toast.LENGTH_LONG).show();
                }
            });

        }
    }

    public void onProfileRadioButtonClicked (View view) {
        // Is the button now checked?
        radioButtonChecked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_inward:
                if (radioButtonChecked)
                    userMode = UserMode.INWARD;
                break;
            case R.id.radio_design:
                if (radioButtonChecked)
                    userMode = UserMode.DESIGN;
                break;
            case R.id.radio_production:
                if (radioButtonChecked)
                    userMode = UserMode.PRODUCTION;
                break;
            case R.id.radio_admin:
                if (radioButtonChecked)
                    userMode = UserMode.ADMIN;
                break;

        }
    }
}
