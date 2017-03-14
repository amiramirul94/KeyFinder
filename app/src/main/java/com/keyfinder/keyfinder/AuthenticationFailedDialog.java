package com.keyfinder.keyfinder;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

/**
 * Created by Lenovo on 11/3/2016.
 */

public class AuthenticationFailedDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.authentication_failed_dialog,null);

        return new AlertDialog.Builder(getActivity()).setView(v).setTitle("Key Not Found")
                .setPositiveButton("Yes",null)
                .create();
    }
}
