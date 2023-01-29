package com.movistar.autocall.viewmodel;

import static android.content.Context.TELEPHONY_SERVICE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.romellfudi.ussdlibrary.USSDApi;
import com.romellfudi.ussdlibrary.USSDController;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class CallScreenViewModel extends ViewModel implements DefaultLifecycleObserver {


    private MutableLiveData<Boolean> makeCall;
    private List<String> numbers = new ArrayList<>();


    private final ActivityResultRegistry mRegistry;
    private TelecomManager telecomManager;
    private Uri callUri;
    private String rootUssdCode;
    private String ussdCode;
    private PhoneAccountHandle sim1;
    private String mmi;
    private HashMap<String, HashSet<String>> map = new HashMap<>();
    private USSDApi ussdApi;

    public MutableLiveData<Boolean> getMakeCall() {
        if (makeCall == null) {
            makeCall = new MutableLiveData<>();
        }
        return makeCall;
    }


    public CallScreenViewModel(@NotNull ActivityResultRegistry mRegistry) {
        this.mRegistry = mRegistry;
    }

    @SuppressLint("MissingPermission")
    public void instanceCall(Context context) {
        telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);

        List<PhoneAccountHandle> phoneAccounts = null;
        phoneAccounts = telecomManager.getCallCapablePhoneAccounts();

// choose the SIM card you want to use
        assert phoneAccounts != null;
         sim1 = phoneAccounts.get(1);

        PhoneAccountHandle sim2 = phoneAccounts.get(1);
        Log.d("CallScreenViewModel", "instanceCall: " + sim1);
        Log.d("CallScreenViewModel", "instanceCall: " + sim2);


    }

    @SuppressLint("MissingPermission")
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
        Log.i("CallScreenViewModel", "netwotk: " + telephonyManager.getNetworkOperatorName());
        telephonyManager.sendUssdRequest(mmi, new TelephonyManager.UssdResponseCallback() {
            @Override
            public void onReceiveUssdResponse(TelephonyManager telephonyManager, String request, CharSequence response) {
                // Handle USSD response
                Log.i("Call2ScreenViewModel", "onReceiveUssdResponse: " + response+" "+request);
                //getMakeCall().postValue(true);
            }

            @Override
            public void onReceiveUssdResponseFailed(TelephonyManager telephonyManager, String request, int failureCode) {
                switch (failureCode) {
                    case TelephonyManager.USSD_RETURN_FAILURE:
                        Log.e("Call2 Error", "USSD request failed " + request);
                        break;
                    case TelephonyManager.USSD_ERROR_SERVICE_UNAVAIL:
                        Log.e("Call2 Error", "Service is not available" + request);
                        break;
                    default:
                        Log.e("Call2 Error", "Unknown error " + request);
                        break;
                }
                //getMakeCall().postValue(true);

            }
        }, null);
    }

    public void intiRequest(Context context){
        map.put("KEY_LOGIN",new HashSet<>(Arrays.asList("espere", "waiting", "loading", "esperando")));
        map.put("KEY_ERROR",new HashSet<>(Arrays.asList("problema", "problem", "error", "null")));
        ussdApi = USSDController.getInstance(context);
    }

    public void setRootUssdCode(String rootUssdCode) {
        this.rootUssdCode = rootUssdCode;
    }

    public void setUssdCode(String ussdCode) {
        this.ussdCode = ussdCode;
    }

    public void sendUSSDCode(){
        ussdApi.callUSSDInvoke(rootUssdCode, 0, map, new USSDController.CallbackInvoke() {

            @Override
            public void responseInvoke(String message) {
                String dataToSend = numbers.get(0).replace(rootUssdCode.replace("#","")+"*",
                        "").replace("#","");// <- send "data" into USSD's input text


                ussdApi.send(dataToSend,new USSDController.CallbackMessage(){
                    @Override
                    public void responseMessage(String message) {
                        if(message.contains("MENU ACTIVACIONES")){
                            ussdApi.send(
                                    dataToSend,
                                    new USSDController.CallbackMessage() {
                                        @Override
                                        public void responseMessage(String message) {
                                            Log.i("RepuestaALA", "responseMessage: "+message + " " + dataToSend);
                                            ussdApi.cancel();
                                            numbers.remove(0);
                                            if (!numbers.isEmpty()) {
                                                getMakeCall().postValue(true);
                                            }
                                        }
                                    });
                        }else{
                            Log.i("RepuestaALA", "responseMessage: "+message + " " + dataToSend);
                            ussdApi.cancel();
                            numbers.remove(0);
                            if (!numbers.isEmpty()) {
                                getMakeCall().postValue(true);
                            }                        }
                    }
                });
            }

            @Override
            public void over(String message) {
                Log.i("RepuestaALA2", "responseMessage: "+ " " + message + " " + numbers.get(0));

                if(!message.contains("Check your accessibility") && !numbers.isEmpty()) {

                    numbers.remove(0);
                    if (!numbers.isEmpty()) {
                        getMakeCall().postValue(true);
                    }
                }
            }
        });
    }

    public void setNumbers(List<String> numbers) {
        this.numbers = numbers;
    }


}