package com.ookiisoftware.mapnap.modelo;

import com.google.firebase.database.Exclude;

import java.util.List;

public class CaixaAlterada {

    private String id;
    private String data;
    private List<Caixa> caixas;

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Exclude
    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public List<Caixa> getCaixas() {
        return caixas;
    }

    public void setCaixas(List<Caixa> caixas) {
        this.caixas = caixas;
    }
}
