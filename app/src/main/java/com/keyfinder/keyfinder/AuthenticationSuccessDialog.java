package com.keyfinder.keyfinder;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

/**
 * Created by Lenovo on 11/3/2016.
 */

public class AuthenticationSuccessDialog extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.authentication_success_dialog,null);

        return new AlertDialog.Builder(getActivity()).setView(v).setTitle("Key Found")
                .setPositiveButton("Ok",null)
                .create();
    }
}
