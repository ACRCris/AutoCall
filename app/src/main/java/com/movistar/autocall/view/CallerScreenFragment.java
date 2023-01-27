package com.movistar.autocall.view;

import static android.content.Context.TELEPHONY_SERVICE;

import android.Manifest;
import android.annotation.SuppressLint;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.telecom.Call;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.DisconnectCause;
import android.telephony.PhoneStateListener;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.hover.sdk.actions.HoverAction;
import com.hover.sdk.api.Hover;
import com.hover.sdk.api.HoverParameters;
import com.movistar.autocall.CallReceiver;
import com.movistar.autocall.R;
import com.movistar.autocall.databinding.FragmentCallerScreenBinding;
import com.movistar.autocall.viewmodel.CallScreenViewModel;
import com.movistar.autocall.viewmodel.LoadScreenViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class CallerScreenFragment extends Fragment {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler(Looper.myLooper());
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            int flags = View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

            Activity activity = getActivity();
            if (activity != null
                    && activity.getWindow() != null) {
                activity.getWindow().getDecorView().setSystemUiVisibility(flags);
            }
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.hide();
            }

        }
    };
    private View mContentView;
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    private FragmentCallerScreenBinding binding;
    private CallScreenViewModel mRequest;
    private List<String> numbers = new ArrayList<>(Arrays.asList("*454#"));
            /*"*255*4#",
            "*10#",
            "*111#"));
            "*133#",
            /*"*454*3*3*4*636014*3*3259718017*19790130*cantillo*3176460922*1*3116967498*18017*20230123*1#",
            "*454*3*3*4*636014*3*3259719171*19790130*clavijo*3176794414*1*3006250363*19171*20230123*1#",
            "*454*3*3*4*636014*3*3259722404*19790130*cubillos*3176794197*1*3132943168*22404*20230123*1#",
            "*454*3*3*4*636014*3*3259731219*19790130*lozano*3176794006*1*3229478402*31219*20230123*1#",
            "*454*3*3*4*636014*3*3259757677*19790130*diaz*3177428367*1*3219413471*57677*20230123*1#",
            "*454*3*3*4*636014*3*3259764083*19790130*gonzalez*3177427951*1*3133028947*64083*20230123*1#",
            "*454*3*3*4*636014*3*3259781862*19790130*coy*3177408729*1*3108056553*81862*20230123*1#",
            "*454*3*3*4*636014*3*3259789489*19790130*acosta*3175132359*1*3142793108*89489*20230123*1#",
            "*454*3*3*4*636014*3*3259799766*19790130*castro*3175953328*1*3223206855*99766*20230123*1#",
            "*454*3*3*4*636014*3*3259704550*19810605*CORZO*3175951192*1*3222525089*04550*20230123*1#",
            "*454*3*3*4*636014*3*3259706169*19810605*MALDONADO*3175941245*1*3118414880*06169*20230123*1#",
            "*454*3*3*4*636014*3*3259706712*19810605*MEDINA*3175753444*1*3133872845*06712*20230123*1#",
            "*454*3*3*4*636014*3*3259707492*19810605*VALENZUELA*3175141791*1*3214291179*07492*20230123*1#",
            "*454*3*3*4*636014*3*3259707825*19810605*OLARTE*3176449323*1*3142920054*07825*20230123*1#",
            "*454*3*3*4*636014*3*3259708168*19810605*SOPO*3174246910*1*3114879533*08168*20230123*1#",
            "*454*3*3*4*636014*3*3259711721*19810605*VALENCIA*3173811287*1*3146681247*11721*20230123*1#",
            "*454*3*3*4*636014*3*3259711827*19810605*CRUZ*3173811260*1*3135739353*11827*20230123*1#",
            "*454*3*3*4*636014*3*3259712253*19810605*GARZON*3176444657*1*3138887803*12253*20230123*1#",
            "*454*3*3*4*636014*3*3259713320*19810605*SOLER*3176444616*1*3228572708*13320*20230123*1#",
            "*454*3*3*4*636014*3*3259713855*19810605*MEDINA*3176794454*1*3124413161*13855*20230123*1#",
            "*454*3*3*4*636014*3*3259714497*19810605*QUIÑONES*3176461287*1*3143408210*14497*20230123*1#",
            "*454*3*3*4*636014*3*3259714830*19810605*MORENO*3176400196*1*3016106973*14830*20230123*1#",
            "*454*3*3*4*636014*3*3259714853*19810605*SOSA*3167411780*1*3124391779*14853*20230123*1#",
            "*454*3*3*4*636014*3*3259714856*19810605*MORALES*3176796118*1*3133027157*14856*20230123*1#",
            "*454*3*3*4*636014*3*3259715019*19810605*ARIZA*3176750433*1*3223273065*15019*20230123*1#",
            "*454*3*3*4*636014*3*3259715159*19810605*HERNANDEZ*3155917002*1*3237158560*15159*20230123*1#",
            "*454*3*3*4*636014*3*3259715628*19810605*AMAYA*3178776049*1*3123338405*15628*20230123*1#",
            "*454*3*3*4*636014*3*3259717278*19810605*GUEVARA*3176744882*1*3133124970*17278*20230123*1#",
            "*454*3*3*4*636014*3*3259717373*19810605*PINILLOS*3185444487*1*3223973968*17373*20230123*1#",
            "*454*3*3*4*636014*3*3259719404*19810605*OVALLE*3176236325*1*3222521327*19404*20230123*1#",
            "*454*3*3*4*636014*3*3259722357*19810605*NAVARRO*3173579277*1*3143470488*22357*20230123*1#"));*/   
    int cont = 0;
    private BroadcastReceiver smsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.i("SMS", "SMS Received");
            // Get the SMS message
            Bundle bundle = intent.getExtras();
            SmsMessage[] msgs = null;
            String strMessage = "";
            if (bundle != null) {
                // Retrieve the SMS message received
                Object[] pdus = (Object[]) bundle.get("pdus");
                msgs = new SmsMessage[pdus.length];
                for (int i = 0; i < msgs.length; i++) {
                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    // Build the message to show
                    strMessage += "SMS from " + msgs[i].getOriginatingAddress();
                    strMessage += " :" + msgs[i].getMessageBody() + "\n";
                }
                // Display the message
                Toast.makeText(context, strMessage, Toast.LENGTH_SHORT).show();
            }
        }
    };

    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

       boolean isPresent = Hover.isActionSimPresent("cd16b7af", requireActivity());
        Log.i("isPresent", String.valueOf(isPresent));
        Log.i("sim0", Hover.getPresentSims(requireActivity()).get(0).getOperatorName());
        //.i("sim1", Hover.getPresentSims(requireActivity()).get(1).getOperatorName());




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

    private ActivityResultLauncher<Intent> sendUssd = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Log.i("holallala", "" + result.getResultCode());
                }
                Log.i("holallala", "" + result.getResultCode());
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // There are no request codes
                    Intent data = result.getData();
                }
            });



    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("MissingPermission")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mVisible = true;

        mControlsView = binding.fullscreenContentControls;
        mContentView = binding.fullscreenContent;

        Button button = requireView().findViewById(R.id.call_button);

        Intent i = new HoverParameters.Builder(requireActivity())
                .request("cd16b7af")
                .extra("apellido", "3*3*4*636014*3*3259718017*19790130*cantillo*3176460922*1*3116967498*18017*20230123*1#") // Only if your action has variables
                .buildIntent();


        final Observer<Boolean> makeCallObserver = makeCall -> {

            if (makeCall) {
                if (!numbers.isEmpty()) {
                    synchronized (this){
                        try {
                            wait(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        mRequest.setMMI(numbers.get(0));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            mRequest.getRequest(requireActivity());
                        }
                        Log.i("numerororor", "numoermo " + numbers.get(0));
                        //numbers.remove(0);
                        sendUssd.launch(i);

                    }


                }
            }else{
                synchronized (this) {
                    try {
                        wait(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        Log.i("Call2ScreenViewModel", "makeCall: " +mRequest.getTelecomManager().isInCall() + cont);
                        cont++;
                    }


                }
            }
        };


        mRequest.getMakeCall().observe(getViewLifecycleOwner(), makeCallObserver);


        button.setOnClickListener(view1 -> {
            mRequest.getMakeCall().setValue(true);
        });


        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        binding.dummyButton.setOnTouchListener(mDelayHideTouchListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter("CallReceiver");
        requireActivity().getApplication().registerReceiver(new CallReceiver(), intentFilter);
        if (getActivity() != null && getActivity().getWindow() != null) {
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() != null && getActivity().getWindow() != null) {
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

            // Clear the systemUiVisibility flag
            getActivity().getWindow().getDecorView().setSystemUiVisibility(0);
        }
        show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mContentView = null;
        mControlsView = null;
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
        }
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
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