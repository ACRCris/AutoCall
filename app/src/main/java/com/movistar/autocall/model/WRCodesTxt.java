package com.movistar.autocall.model;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.util.Log;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WRCodesTxt {

    public void getMetadataTxt(Uri uri,Context context) {

        try (Cursor cursor = context.getContentResolver()
                .query(uri, null, null, null, null, null)) {

            if (cursor != null && cursor.moveToFirst()) {

                String displayName = cursor.getString(
                        cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                Log.i("mostrarcont", "Display Name: " + displayName);

                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);

                String size = null;
                if (!cursor.isNull(sizeIndex)) {

                    size = cursor.getString(sizeIndex);
                } else {
                    size = "Unknown";
                }

                Log.i("mostrarcont", "Size: " + size);
            }
        }
    }

    public List<String> readTextFromUri(Uri uri, Context context) throws IOException {
        List<String> list =new ArrayList<>();
        try (InputStream inputStream =
                     context.getContentResolver().openInputStream(uri)) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(Objects.requireNonNull(inputStream)))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    Log.i("mostrarcont", "readTextFromUri: " + line);
                    list.add(line);
                }
            }
        }
        return list;

    }

}


