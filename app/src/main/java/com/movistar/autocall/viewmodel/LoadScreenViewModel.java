package com.movistar.autocall.viewmodel;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.util.Log;

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

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LoadScreenViewModel extends ViewModel implements DefaultLifecycleObserver, ReadDatabase, WriteDatabase<List<Code>> {

    private MutableLiveData<Boolean> mIsRoleGranted;
    private MutableLiveData<Boolean> isReadData;
    private Uri uriToLoad;
    private List<String> codes;

    private List<Code> codesList = new ArrayList<>();
    private final ActivityResultRegistry mRegistry;
    private ActivityResultLauncher<Intent> open_txt;
    private ActivityResultLauncher<Intent> openDirectory;
    private ActivityResultLauncher<String[]> requestPermissionLauncher;
    private ActivityResultLauncher<Intent> getRequestPermissionManager;
    private WRCodesTxt wrCodesTxt;
    private final Context context;

    public MutableLiveData<Boolean> getIsRoleGranted() {
        if (mIsRoleGranted == null) {
            mIsRoleGranted = new MutableLiveData<>();
        }
        return mIsRoleGranted;
    }

    public MutableLiveData<Boolean> getIsReadData() {
        if (isReadData == null) {
            isReadData = new MutableLiveData<>();
        }
        return isReadData;
    }



    public LoadScreenViewModel(@NotNull ActivityResultRegistry mRegistry, Context context) {
        this.mRegistry = mRegistry;
        this.context = context;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
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

        open_txt = mRegistry.register("open_txt", owner, new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == -1) {
                        Log.i("WRCodesTXT", "openTxt: " + result.getData().getDataString() + " " + result.getResultCode());
                        //getMetaTxt(result.getData().getData(), context);
                        WRCodesTxt wrCodesTxt = new WRCodesTxt();
                        try {
                            codes= wrCodesTxt.readTextFromUri(result.getData().getData(), context);
                            List<Code> listCodes = fromListStrigToListCodes();
                            write(context, listCodes);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        //permission granted
                    }
                });

        openDirectory = mRegistry.register("openDirectory", owner, new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Log.i("WRCodesTXT", "onCreate: " + result.getData().getDataString() + " " + result.getResultCode());
                    if (result.getResultCode() == -1) {
                        openTxt(Uri.parse(result.getData().getDataString()));

                    } else {
                        //permission granted
                    }
                });
    }


    public void requestRole() {
        String[] permissions = new String[]{Manifest.permission.CALL_PHONE, Manifest.permission.READ_PHONE_STATE, Manifest.permission.MODIFY_PHONE_STATE};
        requestPermissionLauncher.launch(permissions);

    }

    public void setUriToLoad(Uri uriToLoad) {
        this.uriToLoad = uriToLoad;
    }

    public List<String> getCodes() {
        return codes;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void openDirectory() {
        // Choose a directory using the system's file picker.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);

        // Optionally, specify a URI for the directory that should be opened in
        // the system file picker when it loads.
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uriToLoad);
        openDirectory.launch(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void openTxt(Uri pickerInitialUri){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");

        // Optionally, specify a URI for the directory that should be opened in
        // the system file picker when your app creates the document.
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);
        open_txt.launch(intent);

    }

    @Override
    public void read(Context context) {
        Future<List<Code>> future = Executors.newSingleThreadExecutor().submit(() -> {
            synchronized (this) {
                AppDatabase db = DatabaseHelper.getDB(context);
                CodeDao doctorDao = db.codeDao();
                return doctorDao.getCodesOrderByCiudad();
            }
        });

        Runnable runnable = () -> {
            try {
                codesList.addAll(future.get());
                getIsReadData().postValue(true);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        };

        Thread thread = new Thread(runnable);
        thread.start();

    }

    private List<Code>  fromListStrigToListCodes(){
        List<Code> codes = new ArrayList<>();
        for (String code: this.codes) {
            String[] partsCode = code.split("\\*", 2);

            codes.add(new Code(partsCode[1], "", partsCode[0]));
        }
        return codes;
    }
    public List<String> getListStringFromListCodes(List<Code> codes){
        List<String> listString = new ArrayList<>();
        for (Code code: codes) {
            listString.add(code.getId()+"*"+code.getCiudad()+"*"+code.getCode());
        }
        return listString;
    }
    @Override
    public void write(Context context, List<Code> codes) {
        Runnable runnable = () -> {
            AppDatabase db = DatabaseHelper.getDB(context);

            CodeDao doctorDao = db.codeDao();
            doctorDao.insertAll(codes);
            read(context);

        };

        new Thread(runnable).start();


    }

    public List<Code> getCodesList() {
        return codesList;
    }
}
