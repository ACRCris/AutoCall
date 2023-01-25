package com.movistar.autocall.viewmodel;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModel;

import com.movistar.autocall.CallReceiver;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CallScreenViewModel extends ViewModel implements DefaultLifecycleObserver {


    ActivityResultLauncher<Intent> makeCallLauncher;
    private final ActivityResultRegistry mRegistry;
    private TelecomManager telecomManager;
    private Uri callUri;
    PhoneAccountHandle sim2;


    public CallScreenViewModel(@NotNull ActivityResultRegistry mRegistry) {
        this.mRegistry = mRegistry;
    }


    @Override
    public void onCreate(@NotNull LifecycleOwner owner) {

        makeCallLauncher = mRegistry.register("make_call", owner, new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Log.d("CallScreenViewModel", "onCreate: " + result.getResultCode());
                    Log.i("CallScreenViewModel", "onCreate: " + result.toString());
                    if (result.getResultCode() == View.VISIBLE) {
                        // There are no request codes
                        Intent data = result.getData();
                    }
                });

    }

    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void instanceCall(Context context){
         telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);

        List<PhoneAccountHandle> phoneAccounts = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            phoneAccounts = telecomManager.getCallCapablePhoneAccounts();
        }

// choose the SIM card you want to use
        assert phoneAccounts != null;
        PhoneAccountHandle sim1 = phoneAccounts.get(0);
        sim2 = phoneAccounts.get(1);


    }

    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void makeCall(){
        callUri = Uri.fromParts("tel", "*454*3*3*4*636014*3*3259718017*19790130*cantillo*3176460922*1*3116967498*18017*20230123*1#", null);

        Bundle bundle = new Bundle();
        bundle.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, sim2);

        telecomManager.placeCall(callUri, bundle);
        telecomManager.cancelMissedCallsNotification();

    }

}