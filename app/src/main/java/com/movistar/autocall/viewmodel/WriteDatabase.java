package com.movistar.autocall.viewmodel;

import android.content.Context;

public interface WriteDatabase<T> {
    void write(Context context,T t);
}
