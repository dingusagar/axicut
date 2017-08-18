package com.example.dingu.axicut.Admin.Company;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.SharedPreferencesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.dingu.axicut.R;
import com.example.dingu.axicut.Utils.General.MyDatabase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class AdminAddCompany extends AppCompatActivity {
    private DatabaseReference dbRef , dbRefQuickAccess;
    private EditText companyNameText,companyDescText;
    private EditText companyIdText;
    private Button addComapanyButton;
    private ProgressDialog progress;
    private  Company companyToBeEdited;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_company);
        companyToBeEdited = (Company) getIntent().getSerializableExtra("Company");
        dbRef = FirebaseDatabase.getInstance().getReference().child("Company");
        dbRefQuickAccess = FirebaseDatabase.getInstance().getReference().child("InwardUtilities").child("customerIDs");
        companyNameText = (EditText) findViewById(R.id.CompanyName);
        companyIdText = (EditText) findViewById(R.id.CompanyId);
        companyDescText=(EditText)findViewById(R.id.CompanyDesc);
        addComapanyButton = (Button) findViewById(R.id.AddCompany);
        progress = new ProgressDialog(this);
        addComapanyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCompanyToDatabase();
            }
        });
        if(companyToBeEdited!=null)showInfoToBeEdited(companyToBeEdited);

    }
    public void addCompanyToDatabase(){
        DatabaseReference dbRootRef= MyDatabase.getDatabase().getInstance().getReference();
        Map<String, Object> update = new HashMap<>();
        String companyName =companyNameText.getText().toString().trim();
        final String companyId = companyIdText.getText().toString().trim();
        String companyDesc = companyDescText.getText().toString().trim();
        final Company company = new Company(companyName,companyId);
        company.setDescription(companyDesc);
        if(companyAddRules(company)) {
            progress.setMessage("Adding new Company......");
            progress.show();
            update.put("Company/"+company.getCompanyId(),company);
            update.put("InwardUtilities/customerIDs/"+companyId,true);
//            dbRef.child(company.getCompanyId()).setValue(company);
//            dbRefQuickAccess.child(companyId).setValue(true);
            dbRootRef.updateChildren(update).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                    {
                        progress.dismiss();
                        Toast.makeText(getApplicationContext(), "Added new Company :"+ companyId ,Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progress.dismiss();
                    Toast.makeText(getApplicationContext(), "Error adding : " +e.toString(),Toast.LENGTH_LONG).show();
                    onBackPressed();
                }
            });
        }
        else{
            Toast.makeText(getApplicationContext(), "Company name and ID are mandatory !", Toast.LENGTH_SHORT).show();
        }
    }

    private void showInfoToBeEdited(Company company){
        if(company.getCompanyId()!=null)
            companyIdText.setText(company.getCompanyId().toString().trim());
        if(company.getCompanyName()!=null)
            companyNameText.setText(company.getCompanyName().toString().trim());
        if(company.getCompanyName()!=null)
            companyDescText.setText(company.getDescription().toString().trim());

    }

    private boolean companyAddRules(Company company){
        if(company.getCompanyId()!=null && !company.getCompanyId().equals(""))
            if(company.getCompanyName()!=null && !company.getCompanyName().equals(""))
                return true;
        return false;
    }
}