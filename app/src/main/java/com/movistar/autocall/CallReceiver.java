package com.movistar.autocall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class CallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_CALL)) {
            // Extract the phone number from the intent
            String phoneNumber = intent.getData().getSchemeSpecificPart();
            // Perform your custom action
            // For example, you can start an activity to show the call

            Log.i("CallReceiver", "onReceive: " + phoneNumber);


            Intent callIntent = new Intent(context, MainActivity.class);
            callIntent.putExtra("phone_number", phoneNumber);
            callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(callIntent);
        }
    }
}