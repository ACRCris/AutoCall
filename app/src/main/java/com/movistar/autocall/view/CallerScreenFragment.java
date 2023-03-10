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
import android.app.AlertDialog;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;

import com.movistar.autocall.model.Code;
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
import java.util.Objects;


public class CallerScreenFragment extends Fragment {


    private FragmentCallerScreenBinding binding;
    private CallScreenViewModel mRequest;
    private MediaPlayer mediaPlayer;
    private String ciudadActual = "";
    int flags = View.SYSTEM_UI_FLAG_LOW_PROFILE
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
    private boolean inicial =false;

    @SuppressLint("MissingPermission")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mediaPlayer = MediaPlayer.create(requireActivity(), R.raw.tono);


        Activity activity = requireActivity();
        if (activity.getWindow() != null) {
            activity.getWindow().getDecorView().setSystemUiVisibility(flags);
        }

        mRequest = new CallScreenViewModel(requireActivity().getActivityResultRegistry(), requireContext());
        getLifecycle().addObserver(mRequest);
        //mRequest.instanceCall(requireActivity());
        mRequest.intiRequest(requireActivity());
        List<String> codes = requireArguments().getStringArrayList("codes");
        mRequest.setCiudad(codes.get(0).split("\\*")[1]);
        mRequest.setNumbers(codes);

        for (String code : codes) {
            Log.d("CODESxxxxx", code);
        }

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentCallerScreenBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);



        Button button = requireView().findViewById(R.id.call_button);
        Button export = requireView().findViewById(R.id.export_button);


        final Observer<Boolean> makeCallObserver = makeCall -> {

            if (makeCall) {
                synchronized (this){
                    try {
                        wait(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(ciudadActual.equals(mRequest.ciudad())) {
                        mRequest.sendUSSDCode();
                    }
                    else if (!inicial){
                        List<String> numbers = mRequest.getNumbers();
                        if(mRequest.ussdCode() != null)
                            numbers.add(0, mRequest.ussdCode());
                        List<Code> codes = mRequest.getCodes();
                        if(codes.size() > 0) {
                            codes.remove(codes.size() - 1);
                            //mRequest.setCodes(codes);
                        }
                        mRequest.setNumbers(numbers);
                        showAlertDialogSim2();
                    }else
                        inicial = false;
                }
            }else {
                mRequest.update(requireActivity(), mRequest.getCodes());
            }
        };


        mRequest.getMakeCall().observe(getViewLifecycleOwner(), makeCallObserver);


        button.setOnClickListener(view1 -> {
            if(mRequest.getNumbers().size()>0){
                if(ciudadActual.equals(mRequest.ciudad())){

                    mRequest.getMakeCall().setValue(true);
                }
                else {
                    showAlertDialogSim();
                    inicial = true;
                }
            }
        });

        final Observer<Boolean> exportObserver = exportCall -> {
            if (exportCall) {
                export.setEnabled(true);
            }
        };

        mRequest.getIsWriteData().observe(getViewLifecycleOwner(), exportObserver);

        export.setEnabled(false);

        export.setOnClickListener(view1 -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mRequest.createTxt();
            }
        });




        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
    }

    public void showAlertDialogSim() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Colocar SIM");
        mediaPlayer.start();
        builder.setMessage("Pofavor colocar la SIM " + mRequest.ciudad());
        builder.setPositiveButton("OK", (dialog, which) -> {
            mRequest.getMakeCall().setValue(true);
            ciudadActual = mRequest.ciudad();
            dialog.dismiss();
        });
        builder.show();
    }

    public void showAlertDialogSim2() {
        mediaPlayer.start();
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Colocar SIM");
        builder.setMessage("Pofavor colocar la SIM " + mRequest.ciudad());
        builder.setPositiveButton("OK", (dialog, which) -> {
            mRequest.sendUSSDCode();
            ciudadActual = mRequest.ciudad();
            dialog.dismiss();
        });
        builder.show();
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