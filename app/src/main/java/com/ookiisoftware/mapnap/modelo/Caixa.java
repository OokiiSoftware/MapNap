package com.ookiisoftware.mapnap.modelo;

import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.Exclude;
import com.ookiisoftware.mapnap.auxiliar.Constantes;
import com.ookiisoftware.mapnap.auxiliar.Import;

import java.util.ArrayList;
import java.util.List;

public class Caixa
{
    private final static String TAG = "Caixa";

    private String id;
    private String nome;
    private String data;
    private String motivo;
    private String novo_id;
    private String id_usuario;

    private int pon;
    private int portas;
    private double latitude;
    private double longitude;
    private boolean isExcluido;
    private boolean isEmManutencao;

    private List<String> clientes;
    private Endereco endereco;

    public void Salvar(boolean excluidas) {
        try{
            String child = excluidas ? Constantes.Firebase.CHILD_CAIXAS_EXCLUIDAS : Constantes.Firebase.CHILD_CAIXAS;

            Import.getFirebase.getRaiz().child(child).push().setValue(this);
        }catch (Exception e) {
            Log.e(TAG, "Add: " + e.getMessage());
        }
    }

    public void Remove() {
        try{
            setExcluido(true);
            Import.getFirebase.getRaiz()
                    .child(Constantes.Firebase.CHILD_CAIXAS)
                    .child(getId())
                    .child(Constantes.Firebase.CHILD_IS_EXCLUIDO)
                    .setValue(isExcluido()) // <- isExcluido = TRUE pro appDesktop entender que o item foi excluido
                    .addOnSuccessListener(
                    new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Salvar(true);// Salva na lista de excluidos
                            Import.getFirebase.getRaiz()
                                    .child(Constantes.Firebase.CHILD_CAIXAS)
                                    .child(getId()).removeValue();
                        }
                    });
        } catch (Exception e){
            Log.e(TAG, "Remove: " + e.getMessage());
        }
    }

    public void SalvarClientes() {
        try {
            Import.getFirebase.getRaiz().child(Constantes.Firebase.CHILD_CAIXAS)
                    .child(getId())
                    .child(Constantes.Firebase.CHILD_CLIENTES)
                    .setValue(getClientes());
            SalvarIdUsuario();
            SalvarData();
        } catch (Exception e) {
            Log.e(TAG, "SalvarClientes: " + e.getMessage());
        }
    }

    public void SalvarPon() {
        try {
            Import.getFirebase.getRaiz().child(Constantes.Firebase.CHILD_CAIXAS)
                    .child(getId())
                    .child(Constantes.Firebase.CHILD_PON)
                    .setValue(getPon());
            SalvarIdUsuario();
            SalvarData();
        } catch (Exception e) {
            Log.e(TAG, "SalvarPon: " + e.getMessage());
        }
    }

    private void SalvarIdUsuario() {
        try {
            Import.getFirebase.getRaiz().child(Constantes.Firebase.CHILD_CAIXAS)
                    .child(getId())
                    .child(Constantes.Firebase.CHILD_ID_USUARIO)
                    .setValue(getId_usuario());
        } catch (Exception e) {
            Log.e(TAG, "SalvarIdUsuario: " + e.getMessage());
        }
    }

    private void SalvarData() {
        try{
            Import.getFirebase.getRaiz().child(Constantes.Firebase.CHILD_CAIXAS)
                    .child(getId())
                    .child(Constantes.Firebase.CHILD_DATA)
                    .setValue(getData());
        } catch (Exception e){
            Log.e(TAG, "SalvarData: " + e.getMessage());
        }
    }

    public void SalvarEmManutencao(boolean value) {
        try {
            setIsEmManutencao(value);
            Import.getFirebase.getRaiz().child(Constantes.Firebase.CHILD_CAIXAS)
                    .child(getId())
                    .child(Constantes.Firebase.CHILD_ALERT)
                    .setValue(isIsEmManutencao());

            SalvarIdUsuario();
            SalvarData();

        } catch (Exception e) {
            Log.e(TAG, "Manutencao: " + e.getMessage());
        }
    }

    //region get set

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getPortas() {
        return portas;
    }

    public void setPortas(int porta) {
        this.portas = porta;
    }

    public String getNovo_id() {
        return novo_id;
    }

    public void setNovo_id(String novo_id) {
        this.novo_id = novo_id;
    }

    @Exclude
    public String getStatus() {
        return getClientes().size() == getPortas() ? "CHEIO" : "LIVRE";
    }

//    public void setStatus(String status) {
//        this.status = status;
//    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public boolean isExcluido() {
        return isExcluido;
    }

    public void setExcluido(boolean isExcluido) {
        this.isExcluido = isExcluido;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }


    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }

    public List<String> getClientes() {
        if(clientes == null)
            clientes = new ArrayList<>();
        return clientes;
    }

    public void setClientes(List<String> clientes) {
        this.clientes = clientes;
    }

    public String getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(String id_usuario) {
        this.id_usuario = id_usuario;
    }

    public int getPon() {
        return pon;
    }

    public void setPon(int pon) {
        this.pon = pon;
    }

    public boolean isIsEmManutencao() {
        return isEmManutencao;
    }

    public void setIsEmManutencao(boolean isEmManutencao) {
        this.isEmManutencao = isEmManutencao;
    }

    //endregion

}
