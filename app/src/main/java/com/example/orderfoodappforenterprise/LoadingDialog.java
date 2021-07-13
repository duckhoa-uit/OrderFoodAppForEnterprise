package com.example.orderfoodappforenterprise;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;

public class LoadingDialog {
    Activity activity;
    AlertDialog loadingDialog;

    public LoadingDialog(Activity myActivity){
        activity = myActivity;
    }

    public void startLoadingDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.activity_loading, null));
        builder.setCancelable(false);

        loadingDialog = builder.create();
        loadingDialog.show();
    }

    public void showLoading(){
        loadingDialog.show();
    }

    public void cancelLoadingDialog(){
        loadingDialog.dismiss();
    }
}
