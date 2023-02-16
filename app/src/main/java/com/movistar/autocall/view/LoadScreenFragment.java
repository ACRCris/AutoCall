package com.movistar.autocall.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.app.Activity;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.movistar.autocall.viewmodel.LoadScreenViewModel;
import com.movistar.autocall.R;
import com.movistar.autocall.databinding.FragmentLoadScreenBinding;

import java.util.ArrayList;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class LoadScreenFragment extends Fragment {

    private FragmentLoadScreenBinding binding;
    private LoadScreenViewModel mRequest;
    int flags = View.SYSTEM_UI_FLAG_LOW_PROFILE
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        Activity activity = requireActivity();
        if (activity.getWindow() != null) {
            activity.getWindow().getDecorView().setSystemUiVisibility(flags);
        }

        mRequest = new LoadScreenViewModel(requireActivity().getActivityResultRegistry(), requireActivity());
        getLifecycle().addObserver(mRequest);
        mRequest.setUriToLoad( Uri.parse(requireActivity().getExternalFilesDir(null).getAbsolutePath()));


    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mRequest.openDirectory();
        //mRequest.requestRole();


        NavHostFragment navHostFragment =
                (NavHostFragment) requireActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();

        final Observer<Boolean> isGrantedObserver = isGranted -> {

            if (isGranted) {
                Bundle bundle = new Bundle();
                bundle.putStringArrayList("codes", new ArrayList<>(mRequest.getCodes()));
                navController.navigate(R.id.callerScreenFragment, bundle);

            } else {
                navController.navigate(R.id.errorsScreenFragment);
            }
        };

        final Observer<Boolean> isReadDataObserver = isReadData -> {
            if (isReadData) {
                mRequest.requestRole();
            }
        };

        mRequest.getIsRoleGranted().observe(getViewLifecycleOwner(), isGrantedObserver);
        mRequest.getIsReadData().observe(getViewLifecycleOwner(), isReadDataObserver);

        // Inflate the layout for this fragment*/
        binding = FragmentLoadScreenBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null && getActivity().getWindow() != null) {
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            getActivity().getWindow().getDecorView().setSystemUiVisibility(flags);

        }

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