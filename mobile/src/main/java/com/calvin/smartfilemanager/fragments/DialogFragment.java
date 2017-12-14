/*
 * Copyright (c) 2009-2016 by Grandstream Networks, Inc. All rights reserved.
 *  This material is proprietary to Grandstream Networks, Inc. and, in addition to the above
 *  mentioned Copyright, may be subject to protection under other intellectual property
 *  regimes, including patents, trade secrets, designs and/or trademarks.
 *  Any use of this material for any purpose, except with an express license from Grandstream
 *  Networks, Inc. is strictly prohibited.
 */

package com.calvin.smartfilemanager.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.widget.Button;

import com.calvin.smartfilemanager.misc.CrashReportingManager;
import com.calvin.smartfilemanager.misc.Utils;

/**
 * Created by HaKr on 12/06/16.
 */

public class DialogFragment extends AppCompatDialogFragment {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(!getShowsDialog()){
            return;
        }
        getDialog().setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                try{
                    tintButtons(getDialog());
                } catch (Exception e){
                    CrashReportingManager.logException(e);
                }
            }
        });
    }

    public static void tintButtons(Dialog dialog){
        Utils.tintButton(getButton(dialog, DialogInterface.BUTTON_POSITIVE));
        Utils.tintButton(getButton(dialog, DialogInterface.BUTTON_NEGATIVE));
        Utils.tintButton(getButton(dialog, DialogInterface.BUTTON_NEUTRAL));
    }

    private static Button getButton(Dialog dialog, int which){
        return ((AlertDialog)dialog).getButton(which);
    }

    public static void showThemedDialog(AlertDialog.Builder builder){
        Dialog dialog = builder.create();
        dialog.show();
        tintButtons(dialog);
    }

}
