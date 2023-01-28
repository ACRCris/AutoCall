package com.movistar.autocall.viewmodel;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.HashMap;

public class TransactionReceiver extends BroadcastReceiver {
    public TransactionReceiver() { }

    @Override
    public void onReceive(Context context, Intent intent) {
        String uuid = intent.getStringExtra("uuid");

        Log.i("recividododo", "Recieved pending transaction created broadcast");
        Log.i("recividododo", "uuid: " + intent.getStringExtra("uuid"));
        String confirmationCode, balance;
        /*if (intent.hasExtra("parsed_variables")) {
            HashMap<String, String> parsed_variables = (HashMap<String, String>) intent.getSerializableExtra("parsed_variables");
            if (parsed_variables.containsKey("confirmCode"))
                confirmationCode = parsed_variables.get("confirmCode");
            if (parsed_variables.containsKey("balance"))
                balance = parsed_variables.get("balance");
        }*/
    }
}
