package com.ookiisoftware.mapnap.modelo;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.ValueEventListener;
import com.ookiisoftware.mapnap.auxiliar.Constantes;
import com.ookiisoftware.mapnap.auxiliar.Import;

import java.util.LinkedList;
import java.util.List;

public class CaixaAlterada {

    private final static String TAG = "CaixaAlterada";

    private String id;
    private List<Caixa> caixas;

    public static void Salvar(final Caixa c) {
        c.setNovo_id(Import.get.RandomString(10));
        try{
            Import.getFirebase.getRaiz()
                    .child(Constantes.Firebase.CHILD_CAIXAS_ALTERADAS)
                    .child(c.getId())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            CaixaAlterada cA = dataSnapshot.getValue(CaixaAlterada.class);
                            if (cA == null) {
                                cA = new CaixaAlterada();
                                cA.setCaixas(new LinkedList<Caixa>());
                            }

                            cA.setId(dataSnapshot.getKey());
                            if (cA.getCaixas() == null)
                                cA.setCaixas(new LinkedList<Caixa>());
                            cA.getCaixas().add(c);
                            cA.Salvar();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {}
                    });
        } catch (Exception ex) {
            Log.e(TAG, "AddAlterada: " + ex.getMessage());
        }
    }

    public void Salvar() {
        try {
            Import.getFirebase.getRaiz()
                    .child(Constantes.Firebase.CHILD_CAIXAS_ALTERADAS)
                    .child(getId()).setValue(this);
        } catch (Exception e) {
            Log.e(TAG, "Salvar: " + e.getMessage());
        }
    }

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Exclude
    public String getData() {
        if (getCaixas().size() == 0) return "Data Indisponível";
        return getCaixas().get(getCaixas().size() -1).getData();
    }

    public List<Caixa> getCaixas() {
        if (caixas == null)
            caixas = new LinkedList<>();
        return caixas;
    }

    public void setCaixas(List<Caixa> caixas) {
        this.caixas = caixas;
    }
}
