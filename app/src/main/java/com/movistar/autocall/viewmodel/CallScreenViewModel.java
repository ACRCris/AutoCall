package com.movistar.autocall.viewmodel;

import static android.content.Context.TELEPHONY_SERVICE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.movistar.autocall.CallReceiver;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CallScreenViewModel extends ViewModel implements DefaultLifecycleObserver {


    private MutableLiveData<Boolean> makeCall;
    private MutableLiveData<Boolean> begin;


    ActivityResultLauncher<Intent> makeCallLauncher;
    private final ActivityResultRegistry mRegistry;
    private TelecomManager telecomManager;
    private Uri callUri;
    PhoneAccountHandle sim1;
    private String mmi;

    public MutableLiveData<Boolean> getMakeCall() {
        if (makeCall == null) {
            makeCall = new MutableLiveData<>();
        }
        return makeCall;
    }

    public MutableLiveData<Boolean> getBegin() {
        if (begin == null) {
            begin = new MutableLiveData<>();
        }
        return begin;
    }


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
    public void instanceCall(Context context) {
        telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);

        List<PhoneAccountHandle> phoneAccounts = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            phoneAccounts = telecomManager.getCallCapablePhoneAccounts();
        }

// choose the SIM card you want to use
        assert phoneAccounts != null;
         sim1 = phoneAccounts.get(0);
        PhoneAccountHandle sim2 = phoneAccounts.get(1);


    }

    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void makeCall(Context context) {

        callUri = Uri.parse("tel:" + mmi);

        Bundle bundle = new Bundle();
        bundle.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, sim1);

        telecomManager.placeCall(callUri, bundle);

    }
    public void setMMI(String mmi){

        this.mmi = mmi;
    }

    public  TelecomManager getTelecomManager(){
        return telecomManager;
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void getRequest(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);

        telephonyManager.sendUssdRequest(mmi, new TelephonyManager.UssdResponseCallback() {
            @Override
            public void onReceiveUssdResponse(TelephonyManager telephonyManager, String request, CharSequence response) {
                // Handle USSD response
                Log.i("CallScreenViewModel", "onReceiveUssdResponse: " + response+" "+request);
            }

            @Override
            public void onReceiveUssdResponseFailed(TelephonyManager telephonyManager, String request, int failureCode) {
                switch (failureCode) {
                    case TelephonyManager.USSD_RETURN_FAILURE:
                        Log.e("Call Error", "USSD request failed " + request);
                        break;
                    case TelephonyManager.USSD_ERROR_SERVICE_UNAVAIL:
                        Log.e("Call Error", "Service is not available" + request);
                        break;
                    default:
                        Log.e("Call Error", "Unknown error " + request);
                        break;
                }
            }
        }, null);
    }

}