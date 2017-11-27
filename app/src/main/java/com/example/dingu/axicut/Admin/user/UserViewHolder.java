package com.example.dingu.axicut.Admin.user;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import com.example.dingu.axicut.R;
import com.example.dingu.axicut.UserMode;
import com.example.dingu.axicut.Utils.Navigation.Projector;

/**
 * Created by grey-hat on 7/5/17.
 */

public class UserViewHolder extends RecyclerView.ViewHolder{
    public View mView;
    private TextView userName;
    private TextView userMode;
    private TextView userEmail;
    private Switch isActiveSwitch;
    public UserViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        userName=(TextView)mView.findViewById(R.id.UserName);
        userMode=(TextView)mView.findViewById(R.id.UserMode);
        userEmail=(TextView)mView.findViewById(R.id.UserEmail);
        isActiveSwitch = (Switch)mView.findViewById(R.id.isActiveSwitch);

    }
    public void setName(String name){
        userName.setText(name);
    }
    public void setMode(UserMode Mode){

        if(Mode != null)
        userMode.setText(Mode.toString());
        else
            userMode.setText("");
    }

    public void setUserEmail(String email){
        userEmail.setText(email);
    }
    public void setSwitchStatus(boolean isActive){
        isActiveSwitch.setChecked(isActive);
    }

    public void setUpdateUserOnViewTouch(final User user)
    {
        mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mView.getContext(),AdminAddUser.class);
                intent.putExtra(String.valueOf(R.string.existingUser),user);
                mView.getContext().startActivity(intent);
            }
        });
    }
}