package com.movistar.autocall.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class Code {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "ciudad")
    private String ciudad;

    @ColumnInfo(name = "code")
    private String code;

    @ColumnInfo(name = "result")
    private String result;

    @Ignore
    public Code(int id,String code, String result, String ciudad) {
        this.id = id;
        this.code = code;
        this.result = result;
        this.ciudad = ciudad;
    }
    public Code(String code, String result, String ciudad) {
        this.code = code;
        this.result = result;
        this.ciudad = ciudad;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
            this.result = result;
        }



}
