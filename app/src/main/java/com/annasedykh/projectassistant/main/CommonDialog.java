package com.annasedykh.projectassistant.main;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;

import com.annasedykh.projectassistant.R;

/**
 * {@link CommonDialog} custom dialog with OK and cancel buttons.
 */
public class CommonDialog extends DialogFragment {

    private DialogInterface.OnClickListener listener;

    public void setListener(DialogInterface.OnClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getString(R.string.app_name);
        String message = getString(R.string.dialog_msg);

        Bundle args = getArguments();
        if (args != null) {
            title = args.getString("title", getString(R.string.app_name));
            message = args.getString("message", getString(R.string.dialog_msg));
        }

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(R.string.cancel, listener)
                .setPositiveButton(R.string.ok, listener)
                .create();

        return dialog;
    }

    /**
     * Method sets title, message and custom OnClickListener for dialog and shows it on the screen.
     */
    public static void show(String title, String message, DialogInterface.OnClickListener listener, FragmentManager fragmentManager) {
        CommonDialog dialog = new CommonDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("message", message);
        dialog.setArguments(args);
        dialog.setListener(listener);
        dialog.show(fragmentManager, "CommonDialog");
    }

}
