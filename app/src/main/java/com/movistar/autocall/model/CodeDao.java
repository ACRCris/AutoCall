package com.movistar.autocall.model;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CodeDao {
    @Insert
    void insert(Code code);

    @Insert
    void insertAll(List<Code> codes);

    @Query("SELECT * FROM code")
    java.util.List<Code> getAll();

    @Query("SELECT * FROM code WHERE code = :code")
    Code findByCode(String code);

    //get all codes ordered by ciudad and result empty
    @Query("SELECT * FROM code WHERE result = '' ORDER BY ciudad")
    List<Code> getCodesOrderByCiudad();

    @Query("DELETE FROM code")
    void deleteAll();

    @androidx.room.Delete
    void delete(Code code);

    @Update
    void update(Code code);

    @Update
    void updateAll(List<Code> codes);
}
