package com.movistar.autocall.viewmodel;

import static android.content.Context.TELEPHONY_SERVICE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
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

import com.movistar.autocall.model.AppDatabase;
import com.movistar.autocall.model.Code;
import com.movistar.autocall.model.CodeDao;
import com.movistar.autocall.model.DatabaseHelper;
import com.movistar.autocall.model.WRCodesTxt;
import com.romellfudi.ussdlibrary.USSDApi;
import com.romellfudi.ussdlibrary.USSDController;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class CallScreenViewModel extends ViewModel implements DefaultLifecycleObserver, WriteDatabase<List<Code>>, UpdateDatabase<List<Code>> {


    private MutableLiveData<Boolean> makeCall;
    private MutableLiveData<Boolean> isWriteData;

    private List<String> numbers = new ArrayList<>();
    private List<Code> codes = new ArrayList<>();
    private ActivityResultLauncher<Intent> create_txt;

    private String ciudad;

    private final ActivityResultRegistry mRegistry;
    private TelecomManager telecomManager;
    private Uri callUri;



    private PhoneAccountHandle sim1;
    private String mmi;
    private HashMap<String, HashSet<String>> map = new HashMap<>();
    private USSDApi ussdApi;
    private Context context;
    private String ussdCode;
    private int id;

    public MutableLiveData<Boolean> getMakeCall() {
        if (makeCall == null) {
            makeCall = new MutableLiveData<>();
        }
        return makeCall;
    }

    public MutableLiveData<Boolean> getIsWriteData() {
        if (isWriteData == null) {
            isWriteData = new MutableLiveData<>();
        }
        return isWriteData;
    }


    public CallScreenViewModel(@NotNull ActivityResultRegistry mRegistry, Context context) {
        this.mRegistry = mRegistry;
        this.context = context;

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onCreate(@NotNull LifecycleOwner owner) {


        create_txt = mRegistry.register("create_txt", owner, new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == -1) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            WRCodesTxt wrCodesTxt = new WRCodesTxt();
                            Log.i("CallScreenViewModel", "onCreate: " + codes.size());
                            wrCodesTxt.alterDocument(uri, context, codes);
                        }
                    } else {
                        Log.i("CallScreenViewModel", "onCreate: " + "no se pudo crear el archivo");
                        //permission granted
                    }
                });
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

    public void setCiudad(String ciudad){
        this.ciudad = ciudad;
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

    public void sendUSSDCode(){
        String [] data = descompesCode();
        String rootUssdCode = "*" + data[2]+"#";
        String dataToSend = data[3];
        ussdCode = data[4];
        ciudad = data[1];
        id = Integer.parseInt(data[0]);
        ussdApi.callUSSDInvoke(rootUssdCode, 0, map, new USSDController.CallbackInvoke() {

            @Override
            public void responseInvoke(String message) {
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
                                            Code code = new Code(id,data[2]+"*"+dataToSend + "#",message, ciudad);
                                            codes.add(code);
                                            numbers.remove(0);
                                            if (!numbers.isEmpty()) {
                                                getMakeCall().postValue(true);
                                            } else  {
                                                getMakeCall().postValue(false);
                                            }
                                        }
                                    });
                        }else{
                            Log.i("RepuestaALA", "responseMessage: "+message + " " + dataToSend);
                            ussdApi.cancel();
                            Code code = new Code(id,data[2]+"*"+dataToSend + "#",message, ciudad);
                            codes.add(code);
                            numbers.remove(0);
                            if (!numbers.isEmpty()) {
                                getMakeCall().postValue(true);
                            } else  {
                                getMakeCall().postValue(false);
                            }
                        }
                    }
                });
            }

            @Override
            public void over(String message) {
                Log.i("RepuestaALA2", "responseMessage: "+ " " + message + " " + dataToSend);

                if(!message.contains("Check your accessibility") && !numbers.isEmpty()) {
                    Code code = new Code(id, data[2]+"*"+dataToSend + "#",message,ciudad);
                    codes.add(code);
                    numbers.remove(0);
                    if (!numbers.isEmpty()) {
                        getMakeCall().postValue(true);
                    }else {
                        getMakeCall().postValue(false);
                    }
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createTxt(){
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        Uri pickerInitialUri = Uri.parse(path);
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_TITLE, "codesAndResults.txt");

        // Optionally, specify a URI for the directory that should be opened in
        // the system file picker when your app creates the document.
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);
        create_txt.launch(intent);

    }

    public void setNumbers(List<String> numbers) {
        this.numbers = numbers;
    }


    @Override
    public void write(Context context, List<Code> codes) {
        Runnable runnable = () -> {
            AppDatabase db = DatabaseHelper.getDB(context);

            CodeDao doctorDao = db.codeDao();
            //doctorDao.insertAll(codes);
            getIsWriteData().postValue(true);
        };

        new Thread(runnable).start();


    }

    public List<Code> getCodes() {
        return codes;
    }

    public String ciudad(){
        return ciudad;
    }

    public List<String> getNumbers() {
        return numbers;
    }

    public String ussdCode(){
        return ussdCode;
    }

    public int getId(){
        return id;
    }

    public void setCodes(List<Code> codes) {
        this.codes = codes;
    }

    public String[] descompesCode(){
        String[] data = new String[5];

        data[2] = numbers.get(0).split("\\*")[2];
        data[1] = numbers.get(0).split("\\*")[1];
        data[0] = numbers.get(0).split("\\*")[0];
        data[4] = numbers.get(0);
        data[3]  = numbers.get(0).replace(data[0]+"*"+data[1]+"*"+data[2]+"*", "").replace("#","");


        Log.i("RepuestaALA", "responseMessage: "+data[0] + " " + data[1] + " " + data[2] + " " + data[3] + " " + data[4]);
        Log.i("RepuestaALA", "responseMessage:"+ numbers.get(0));
        return data;
    }
    @Override
    public void update(Context context, List<Code> codes) {
        Runnable runnable = () -> {
            AppDatabase db = DatabaseHelper.getDB(context);

            CodeDao doctorDao = db.codeDao();
            doctorDao.updateAll(codes);
            getIsWriteData().postValue(true);
        };

        new Thread(runnable).start();

    }
}