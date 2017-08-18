package com.example.dingu.axicut.Admin.Materials;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.dingu.axicut.Admin.Company.Company;
import com.example.dingu.axicut.R;
import com.example.dingu.axicut.Utils.General.MyDatabase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class AdminAddMaterials extends AppCompatActivity {
    private EditText desc;
    private EditText id;
    private Button addMaterial;
    DatabaseReference dbRef = MyDatabase.getDatabase().getInstance().getReference().child("Material");
    DatabaseReference dbRefQuick = MyDatabase.getDatabase().getInstance().getReference().child("InwardUtilities").child("materialTypes");

    private DatabaseReference materialRef= FirebaseDatabase.getInstance().getReference().child("Material");
    private DatabaseReference materialRefQuickAccess= FirebaseDatabase.getInstance().getReference().child("InwardUtilities").child("materialTypes");
    private ProgressDialog progress;
    private Material materialToBeEdited;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_materials);
        materialToBeEdited = (Material) getIntent().getSerializableExtra("Material");
        desc=(EditText)findViewById(R.id.MaterialName);
        id=(EditText)findViewById(R.id.MaterialId);
        addMaterial=(Button)findViewById(R.id.AddMaterial);
        progress = new ProgressDialog(this);
        addMaterial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMaterial();
            }
        });
        if(materialToBeEdited!=null)showInfoToBeEdited(materialToBeEdited);
    }

    public void addMaterial(){
        DatabaseReference dbRootRef= MyDatabase.getDatabase().getInstance().getReference();
        Map<String, Object> update = new HashMap<>();
        String materialDesc = desc.getText().toString().trim();
        final String materialId = id.getText().toString().trim();
        Material material = new Material(materialDesc, materialId);
        if(materialAddRules(material)) {
            progress.setMessage("Adding new Material......");
            progress.show();
            if(materialToBeEdited!=null){
                dbRef.child(materialToBeEdited.getId()).removeValue();
                dbRefQuick.child(materialToBeEdited.getId()).removeValue();
            }
            update.put("Material/"+material.getId(),material);
            update.put("InwardUtilities/materialTypes/" + material.getId(),true);
            dbRootRef.updateChildren(update).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                    {
                        progress.dismiss();
                        Toast.makeText(getApplicationContext(), "Added new Material :"+ materialId ,Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progress.dismiss();
                    Toast.makeText(getApplicationContext(), "Error adding : "+e.toString(),Toast.LENGTH_LONG).show();
                    onBackPressed();
                }
            });

        }
        else {
            Toast.makeText(getApplicationContext(),"Material ID and name are mandatory",Toast.LENGTH_SHORT).show();
        }
    }
    private void showInfoToBeEdited(Material material){
        id.setText(material.getId());
        desc.setText(material.getDesc());

    }

    private boolean materialAddRules(Material material){
        if(material.getId()!=null && !material.getId().equals(""))
            if(material.getDesc()!=null && !material.getDesc().equals(""))
                return true;
        return false;
    }
}
