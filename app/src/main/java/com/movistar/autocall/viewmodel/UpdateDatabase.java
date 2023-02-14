package com.movistar.autocall.viewmodel;

import android.content.Context;

import java.util.List;

public interface UpdateDatabase<T> {
    void update(Context context, List<T> t);
}
