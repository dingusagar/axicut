package com.example.dingu.axicut.Inward.Despatch.DialogFunctions;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.example.dingu.axicut.Inward.InwardUtilities;
import com.example.dingu.axicut.Inward.MyCustomDialog;
import com.example.dingu.axicut.R;
import com.example.dingu.axicut.SaleOrder;
import com.example.dingu.axicut.Utils.ErrorMessage;
import com.example.dingu.axicut.Utils.General.MyDatabase;
import com.example.dingu.axicut.Utils.RecyclerViewRefresher;
import com.example.dingu.axicut.WorkOrder;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

/**
 * Created by dingu on 20/7/17.
 */

public class DoDespatch implements MyCustomDialog {
    private Context context;
    private String title = "Confirm Despatch";
    private int layout = R.layout.despatch_alert_dialog_fragment;

    LayoutInflater inflater;
    AlertDialog.Builder builder;
    View contentView;
    boolean selectedItems[];
    SaleOrder saleOrder;
    ArrayList<WorkOrder> workOrders;
    EditText dateText ,dcText;
    RecyclerViewRefresher refresher;
    String currentDate = "";
    ErrorMessage errorMessage;


    DatabaseReference dbRef = MyDatabase.getDatabase().getReference().child("Orders");



    public DoDespatch(Context context, RecyclerViewRefresher refresher,boolean[] selectedItems, SaleOrder saleOrder) {
        this.context = context;
        this.refresher = refresher;
        this.selectedItems = selectedItems;
        this.saleOrder = saleOrder;
        workOrders = saleOrder.getWorkOrders();
        errorMessage = new ErrorMessage(context);

        if(InwardUtilities.getServerDate() != null)
            currentDate = InwardUtilities.getServerDate();

    }

    @Override
    public void showDialog() {
        AlertDialog dialog =  builder.create();
        dialog.show();
    }

    @Override
    public void setupDialog() {
        inflater = LayoutInflater.from(context);
        contentView = inflater.inflate(layout,null);

         dateText = (EditText) contentView.findViewById(R.id.Date);
         dcText = (EditText) contentView.findViewById(R.id.DCNumber);
        InwardUtilities.fetchServerTimeStamp();
        dateText.setText(InwardUtilities.getServerDate());

        builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setView(contentView);

        builder.setPositiveButton("Despatch", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
           if(validateSelection())
                onPositiveButtonClicked();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onNegativeButtonClicked();
            }
        });

    }


    @Override
    public void onPositiveButtonClicked() {

        for(int i =0;i<workOrders.size() ;i++)
        {
            WorkOrder wo = workOrders.get(i);
            if(selectedItems[wo.getWorkOrderNumber()]) // work order needs to be edited
            {
                wo.setDespatchDate(dateText.getText().toString());
                wo.setDespatchDC(dcText.getText().toString());
            }

        }
        refresher.refreshRecyclerView();

        for(int i =0;i<workOrders.size() ;i++)
        {
            WorkOrder wo = workOrders.get(i);

            if(selectedItems[wo.getWorkOrderNumber()]) // work order needs to be edited
            {
                dbRef.child(saleOrder.getSaleOrderNumber()).child("workOrders").child(""+(wo.getWorkOrderNumber() -1)).setValue(wo);
            }

        }



    }

    @Override
    public void onNegativeButtonClicked() {

    }

    private boolean validateSelection() {

        ArrayList<Integer> invalidSelections = new ArrayList<>();
        for(int i =0;i<workOrders.size() ;i++)
        {
            WorkOrder wo = workOrders.get(i);
            if(selectedItems[wo.getWorkOrderNumber()])
            {
                if(!(wo.getScrapDC().equals(""))) // if scrap is assigned
                    invalidSelections.add(wo.getWorkOrderNumber());

            }

        }
        if(invalidSelections.size() == 0)
            return true;

        errorMessage.displayToast("Invalid Selection :\n Already scraped out for work orders : \n" + invalidSelections.toString());
        return false;
    }



}
