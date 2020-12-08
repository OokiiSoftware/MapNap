package com.ookiisoftware.mapnap.modelo;

import com.google.firebase.database.Exclude;

public class Usuario {
    private String Id, Nome, Senha, Foto, Telefone, Data, PerfilId;
    private boolean isOnline, isExcluido;
    private PerfilDeAcesso Perfil;

    public Usuario() {}

    public Usuario(String id, String nome, String foto, String perfil, String telefone, int isOnline) {
        this.Id = id;
        this.Nome = nome;
        this.Foto = foto;
        this.PerfilId = perfil;
        this.Telefone = telefone;
        this.isOnline = isOnline == 1;
    }

    @Exclude
    public String getId() {
        return Id;
    }

    public String getNome() {
        return Nome;
    }

    public String getSenha() {
        return Senha;
    }

    public String getTelefone() {
        return Telefone;
    }

    public String getFoto() {
        return Foto;
    }

    public String getData() {
        return Data;
    }

    public boolean isIsOnline() {
        return isOnline;
    }

    public boolean isIsExcluido() {
        return isExcluido;
    }

    public String getPerfilId() {
        return PerfilId;
    }

    @Exclude
    public PerfilDeAcesso getPerfil() {
        return Perfil;
    }

    public void setId(String id) {
        this.Id = id;
    }

    public void setNome(String nome) {
        this.Nome = nome;
    }

    public void setSenha(String senha) {
        this.Senha = senha;
    }

    public void setTelefone(String telefone) {
        Telefone = telefone;
    }

    public void setFoto(String foto) {
        this.Foto = foto;
    }

    public void setData(String data) {
        Data = data;
    }

    public void setIsOnline(boolean online) {
        this.isOnline = online;
    }

    public void setIsExcluido(boolean excluido) {
        this.isExcluido = excluido;
    }

    public void setPerfilId(String perfilId) {
        PerfilId = perfilId;
    }

    public void setPerfil(PerfilDeAcesso perfil) {
        Perfil = perfil;
    }
}
