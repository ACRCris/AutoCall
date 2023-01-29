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
    private List<String> numbers = new ArrayList<>(Arrays.asList(
            "*454*3*3*4*636014*3*3259718017*19790130*cantillo*3176460922*1*3116967498*18017*20230131*1#",
            "*454*3*3*4*636014*3*3259719171*19790130*clavijo*3176794414*1*3006250363*19171*20230131*1#",
            "*454*3*3*4*636014*3*3259722404*19790130*cubillos*3176794197*1*3132943168*22404*20230131*1#",
            "*454*3*3*4*636014*3*3259731219*19790130*lozano*3176794006*1*3229478402*31219*20230131*1#",
            "*454*3*3*4*636014*3*3259757677*19790130*diaz*3177428367*1*3219413471*57677*20230131*1#",
            "*454*3*3*4*636014*3*3259764083*19790130*gonzalez*3177427951*1*3133028947*64083*20230131*1#",
            "*454*3*3*4*636014*3*3259781862*19790130*coy*3177408729*1*3108056553*81862*20230131*1#",
            "*454*3*3*4*636014*3*3259789489*19790130*acosta*3175132359*1*3142793108*89489*20230131*1#",
            "*454*3*3*4*636014*3*3259799766*19790130*castro*3175953328*1*3223206855*99766*20230131*1#",
            "*454*3*3*4*636014*3*3259704550*19810605*CORZO*3175951192*1*3222525089*04550*20230131*1#",
            "*454*3*3*4*636014*3*3259706169*19810605*MALDONADO*3175941245*1*3118414880*06169*20230131*1#",
            "*454*3*3*4*636014*3*3259706712*19810605*MEDINA*3175753444*1*3133872845*06712*20230131*1#",
            "*454*3*3*4*636014*3*3259707492*19810605*VALENZUELA*3175141791*1*3214291179*07492*20230131*1#",
            "*454*3*3*4*636014*3*3259707825*19810605*OLARTE*3176449323*1*3142920054*07825*20230131*1#",
            "*454*3*3*4*636014*3*3259708168*19810605*SOPO*3174246910*1*3114879533*08168*20230131*1#",
            "*454*3*3*4*636014*3*3259711721*19810605*VALENCIA*3173811287*1*3146681247*11721*20230131*1#",
            "*454*3*3*4*636014*3*3259711827*19810605*CRUZ*3173811260*1*3135739353*11827*20230131*1#",
            "*454*3*3*4*636014*3*3259712253*19810605*GARZON*3176444657*1*3138887803*12253*20230131*1#",
            "*454*3*3*4*636014*3*3259713320*19810605*SOLER*3176444616*1*3228572708*13320*20230131*1#",
            "*454*3*3*4*636014*3*3259713855*19810605*MEDINA*3176794454*1*3124413161*13855*20230131*1#",
            "*454*3*3*4*636014*3*3259714497*19810605*QUIÑONES*3176461287*1*3143408210*14497*20230131*1#",
            "*454*3*3*4*636014*3*3259714830*19810605*MORENO*3176400196*1*3016106973*14830*20230131*1#",
            "*454*3*3*4*636014*3*3259714853*19810605*SOSA*3167411780*1*3124391779*14853*20230131*1#",
            "*454*3*3*4*636014*3*3259714856*19810605*MORALES*3176796118*1*3133027157*14856*20230131*1#",
            "*454*3*3*4*636014*3*3259715019*19810605*ARIZA*3176750433*1*3223273065*15019*20230131*1#",
            "*454*3*3*4*636014*3*3259715159*19810605*HERNANDEZ*3155917002*1*3237158560*15159*20230131*1#",
            "*454*3*3*4*636014*3*3259715628*19810605*AMAYA*3178776049*1*3123338405*15628*20230131*1#",
            "*454*3*3*4*636014*3*3259717278*19810605*GUEVARA*3176744882*1*3133124970*17278*20230131*1#",
            "*454*3*3*4*636014*3*3259717373*19810605*PINILLOS*3185444487*1*3223973968*17373*20230131*1#",
            "*454*3*3*4*636014*3*3259719404*19810605*OVALLE*3176236325*1*3222521327*19404*20230131*1#",
            "*454*3*3*4*636014*3*3259722357*19810605*NAVARRO*3173579277*1*3143470488*22357*20230131*1#"));

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
                        Log.i("RepuestaALA", "responseMessage: "+message + " " + dataToSend);
                    }
                });
            }

            @Override
            public void over(String message) {
                Log.i("RepuestaALA", "responseMessage: "+ " " + message + " " + numbers.get(0));
                if(!message.contains("Check your accessibility") && !numbers.isEmpty()) {
                    numbers.remove(0);
                    getMakeCall().postValue(true);
                }
            }
        });
    }


}