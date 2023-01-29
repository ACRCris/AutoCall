package com.movistar.autocall.viewmodel;

import android.Manifest;
import android.content.Context;
import android.content.Intent;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.jetbrains.annotations.NotNull;

import java.security.Permission;
import java.util.Arrays;

public class LoadScreenViewModel extends ViewModel implements DefaultLifecycleObserver {

    private MutableLiveData<Boolean> mIsRoleGranted;



    private final ActivityResultRegistry mRegistry;
    private ActivityResultLauncher<String[]> requestPermissionLauncher;
    private ActivityResultLauncher<Intent> getRequestPermissionManager;


    public MutableLiveData<Boolean> getIsRoleGranted() {
        if (mIsRoleGranted == null) {
            mIsRoleGranted = new MutableLiveData<>();
        }
        return mIsRoleGranted;
    }


    public LoadScreenViewModel(@NotNull ActivityResultRegistry mRegistry) {
        this.mRegistry = mRegistry;
    }

    public void onCreate(@NotNull LifecycleOwner owner) {

        requestPermissionLauncher =  mRegistry.register("call_phone_permission", owner, new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    if(Boolean.TRUE.equals(result.get(Manifest.permission.CALL_PHONE))
                            && Boolean.TRUE.equals(result.get(Manifest.permission.READ_PHONE_STATE))){
                        mIsRoleGranted.setValue(true);
                        //permission granted
                    }else{
                        mIsRoleGranted.setValue(false);
                    }
                });
    }

    public void requestRole(Context context) {
        String[] permissions = new String[]{Manifest.permission.CALL_PHONE, Manifest.permission.READ_PHONE_STATE, Manifest.permission.MODIFY_PHONE_STATE};
        requestPermissionLauncher.launch(permissions);

    }


}
