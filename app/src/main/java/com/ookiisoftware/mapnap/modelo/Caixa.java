package com.ookiisoftware.mapnap.modelo;

import com.google.firebase.database.Exclude;

import java.util.List;

public class Caixa
{
    private String id;
    private String status;
    private int portas;
    private String nome;
    private String data;
    private String motivo;
    private String id_usuario;
    private String novo_id;
    private double latitude;
    private double longitude;
    private boolean excluido;
    private List<String> clientes;
    private Endereco endereco;
    private boolean em_manutencao;

    public enum Pon{
        PacPon,
        GPon
    }
    private int pon;

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
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

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
        return excluido;
    }

    public void setExcluido(boolean excluido) {
        this.excluido = excluido;
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

    public boolean isEm_manutencao() {
        return em_manutencao;
    }

    public void setEm_manutencao(boolean em_manutencao) {
        this.em_manutencao = em_manutencao;
    }
}
