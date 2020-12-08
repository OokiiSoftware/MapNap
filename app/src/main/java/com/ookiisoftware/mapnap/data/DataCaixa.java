package com.ookiisoftware.mapnap.data;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.ookiisoftware.mapnap.auxiliar.Constantes;
import com.ookiisoftware.mapnap.auxiliar.Import;
import com.ookiisoftware.mapnap.modelo.Caixa;
import com.ookiisoftware.mapnap.modelo.CaixaAlterada;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class DataCaixa {
    private static String TAG = "DataCaixa";
    private static Random random = new Random();

    public static boolean _Add(Caixa c){
        try{
            DatabaseReference reference = Import.getFirebase.getRaiz();
            reference.child(Constantes.Firebase.CHILD_CAIXAS).push().setValue(c);
            return true;
        }catch (Exception e){
            Log.e(TAG, "Add: " + e.getMessage());
            return false;
        }
    }
    public static boolean _Add(Caixa c, boolean excluidas){
        try{
            DatabaseReference reference = Import.getFirebase.getRaiz();
            reference.child(Constantes.Firebase.CHILD_CAIXAS_EXCLUIDAS).child(c.getId()).setValue(c);
            return true;
        }catch (Exception e){
            Log.e(TAG, "Update: " + e.getMessage());
            return false;
        }
    }
    private static boolean _Add(CaixaAlterada c){
        try{
            DatabaseReference reference = Import.getFirebase.getRaiz();
            reference.child(Constantes.Firebase.CHILD_CAIXAS_ALTERADAS).child(c.getId()).setValue(c);
            return true;
        }catch (Exception e){
            Log.e(TAG, "AddA: " + e.getMessage());
            return false;
        }
    }

    public static List<Caixa> GetAll(){
        final List<Caixa> caixas = new ArrayList<>();
        try{
            DatabaseReference reference = Import.getFirebase.getRaiz();
            reference.child(Constantes.Firebase.CHILD_CAIXAS).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        if (data.getValue() != null)
                        {
                            Caixa c = data.getValue(Caixa.class);
                            if(c != null)
                            {
                                c.setId(data.getKey());
                                boolean n = false;
                                for (Caixa s : caixas)
                                    if(s.getId().equals(c.getId()))
                                    {
                                        n = true;
                                        caixas.set(caixas.indexOf(s), c);
                                        break;
                                    }
                                if(!n)
                                    caixas.add(c);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {}
            });
        }catch (Exception e){
            Log.e(TAG, "GetAll: " + e.getMessage());
        }
        return caixas;
    }

    public static boolean _AddAlterada(final Caixa c){
        int size = random.nextInt(10);
        c.setNovo_id(Import.get.RandomString(size));
        try{
            DatabaseReference reference = Import.getFirebase.getRaiz();
            reference.child(Constantes.Firebase.CHILD_CAIXAS_ALTERADAS).child(c.getId())
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
//                    Add(cA);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {}
            });
            return true;
        }catch (Exception ex){
            Log.e(TAG, "AddAlterada: " + ex.getMessage());
            return false;
        }
    }

    public static boolean _Remove(Caixa c){
        try{
            c.Salvar(false);

            DatabaseReference reference = Import.getFirebase.getRaiz();
            reference.child(Constantes.Firebase.CHILD_CAIXAS).child(c.getId()).removeValue();
            return true;
        }catch (Exception e){
            Log.e(TAG, "Remove: " + e.getMessage());
            return false;
        }
    }

    public static boolean _Update(Caixa c, boolean clientes, boolean pon){
        try{
            DatabaseReference reference = Import.getFirebase.getRaiz();

            if(clientes)
                reference.child(Constantes.Firebase.CHILD_CAIXAS)
                        .child(c.getId())
                        .child(Constantes.Firebase.CHILD_CLIENTES)
                        .setValue(c.getClientes());
            if(pon)
                reference.child(Constantes.Firebase.CHILD_CAIXAS)
                        .child(c.getId())
                        .child(Constantes.Firebase.CHILD_PON)
                        .setValue(c.getPon());

            reference.child(Constantes.Firebase.CHILD_CAIXAS)
                    .child(c.getId())
                    .child(Constantes.Firebase.CHILD_ID_USUARIO)
                    .setValue(c.getId_usuario());

            reference.child(Constantes.Firebase.CHILD_CAIXAS)
                    .child(c.getId())
                    .child(Constantes.Firebase.CHILD_DATA)
                    .setValue(c.getData());
            return true;
        }catch (Exception e){
            Log.e(TAG, "Update: " + e.getMessage());
            return false;
        }
    }

    public static boolean _Manutencao(Caixa c) {
        try{
            DatabaseReference reference = Import.getFirebase.getRaiz();

            reference.child(Constantes.Firebase.CHILD_CAIXAS).child(c.getId()).child(Constantes.Firebase.CHILD_ALERT).setValue(c.isIsEmManutencao());
            reference.child(Constantes.Firebase.CHILD_CAIXAS).child(c.getId()).child(Constantes.Firebase.CHILD_ID_USUARIO).setValue(c.getId_usuario());
            reference.child(Constantes.Firebase.CHILD_CAIXAS).child(c.getId()).child(Constantes.Firebase.CHILD_DATA).setValue(c.getData());
            return true;
        }catch (Exception e){
            Log.e(TAG, "Manutencao: " + e.getMessage());
            return false;
        }
    }
}
