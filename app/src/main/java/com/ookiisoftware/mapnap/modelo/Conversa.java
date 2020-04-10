package com.ookiisoftware.mapnap.modelo;

public class Conversa {
    private String id, nome_contato, foto, ultima_msg, data;
    private int lido;

    public Conversa() {}

    public Conversa(String id, String nome_contato, String foto, String ultima_msg, String data, int lido) {
        this.id = id;
        this.nome_contato = nome_contato;
        this.foto = foto;
        this.ultima_msg = ultima_msg;
        this.data = data;
        this.lido = lido;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome_contato() {
        return nome_contato;
    }

    public void setNome_contato(String nome_contato) {
        this.nome_contato = nome_contato;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public String getUltima_msg() {
        return ultima_msg;
    }

    public void setUltima_msg(String ultima_msg) {
        this.ultima_msg = ultima_msg;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getLido() {
        return lido;
    }

    public void setLido(int lido) {
        this.lido = lido;
    }
}
