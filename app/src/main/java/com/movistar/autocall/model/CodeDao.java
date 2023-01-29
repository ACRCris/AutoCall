package com.movistar.autocall.model;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CodeDao {
    @Insert
    void insert(Code code);

    @Insert
    void insertAll(List<Code> codes);

    @androidx.room.Query("SELECT * FROM code")
    java.util.List<Code> getAll();

    @androidx.room.Query("SELECT * FROM code WHERE code = :code")
    Code findByCode(String code);

    @androidx.room.Query("DELETE FROM code")
    void deleteAll();

    @androidx.room.Delete
    void delete(Code code);

    @androidx.room.Update
    void update(Code code);
}
