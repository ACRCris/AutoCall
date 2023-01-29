package com.movistar.autocall.view;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import android.app.Activity;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;

import com.movistar.autocall.viewmodel.CallScreenViewModel;

import com.movistar.autocall.R;
import com.movistar.autocall.databinding.FragmentCallerScreenBinding;
import com.romellfudi.ussdlibrary.USSDApi;
import com.romellfudi.ussdlibrary.USSDController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


public class CallerScreenFragment extends Fragment {


    private FragmentCallerScreenBinding binding;
    private CallScreenViewModel mRequest;
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
    HashMap<String, HashSet<String>> map = new HashMap<>();
    int flags = View.SYSTEM_UI_FLAG_LOW_PROFILE
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        Activity activity = requireActivity();
        if (activity.getWindow() != null) {
            activity.getWindow().getDecorView().setSystemUiVisibility(flags);
        }

        mRequest = new CallScreenViewModel(requireActivity().getActivityResultRegistry());
        getLifecycle().addObserver(mRequest);
        mRequest.instanceCall(requireActivity());

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentCallerScreenBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("MissingPermission")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);



        Button button = requireView().findViewById(R.id.call_button);
        map.put("KEY_LOGIN",new HashSet<>(Arrays.asList("espere", "waiting", "loading", "esperando")));
        map.put("KEY_ERROR",new HashSet<>(Arrays.asList("problema", "problem", "error", "null")));
        USSDApi ussdApi = USSDController.getInstance(requireActivity());


        final Observer<Boolean> makeCallObserver = makeCall -> {

            if (makeCall) {
                if (!numbers.isEmpty()) {
                    synchronized (this){
                        try {
                            wait(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        Log.i("numerororor", "numoermo " + numbers.get(0));

                        ussdApi.callUSSDInvoke("*454#", 0, map, new USSDController.CallbackInvoke() {

                            @Override
                            public void responseInvoke(String message) {
                                String dataToSend = numbers.get(0).replace("*454*", "").replace("#","");// <- send "data" into USSD's input text

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
                                if(!message.contains("Check your accessibility")) {
                                    mRequest.getMakeCall().postValue(true);
                                numbers.remove(0);}
                            }
                        });

                    }


                }
            }
        };


        mRequest.getMakeCall().observe(getViewLifecycleOwner(), makeCallObserver);


        button.setOnClickListener(view1 -> {
            mRequest.getMakeCall().setValue(true);
        });




        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
    }


    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null && getActivity().getWindow() != null) {
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            getActivity().getWindow().getDecorView().setSystemUiVisibility(flags);

        }


        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() != null && getActivity().getWindow() != null) {
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

            // Clear the systemUiVisibility flag
            getActivity().getWindow().getDecorView().setSystemUiVisibility(flags);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Nullable
    private ActionBar getSupportActionBar() {
        ActionBar actionBar = null;
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            actionBar = activity.getSupportActionBar();
        }
        return actionBar;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}