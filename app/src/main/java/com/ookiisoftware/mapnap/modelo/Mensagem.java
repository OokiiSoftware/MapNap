package com.ookiisoftware.mapnap.modelo;

public class Mensagem {
    private String id_conversa, id_remetente, mensagem, data_de_envio;
    private int status, arquivo;

    public Mensagem() {}

    public Mensagem(String id_conversa, String id_remetente, String mensagem, String data_de_envio, int status, int arquivo) {
        this.id_conversa = id_conversa;
        this.id_remetente = id_remetente;
        this.mensagem = mensagem;
        this.data_de_envio = data_de_envio;
        this.status = status;
        this.arquivo = arquivo;
    }

    public String getId_conversa() {
        return id_conversa;
    }

    public void setId_conversa(String id_conversa) {
        this.id_conversa = id_conversa;
    }

    public String getId_remetente() {
        return id_remetente;
    }

    public void setId_remetente(String id_remetente) {
        this.id_remetente = id_remetente;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getData_de_envio() {
        return data_de_envio;
    }

    public void setData_de_envio(String data_de_envio) {
        this.data_de_envio = data_de_envio;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getArquivo() {
        return arquivo;
    }

    public void setArquivo(int arquivo) {
        this.arquivo = arquivo;
    }
}
