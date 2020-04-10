package com.ookiisoftware.mapnap.modelo;

public class PerfilDeAcesso {

    public PerfilDeAcesso() {}

    public PerfilDeAcesso(String Id, boolean MultMap, boolean MapNap, boolean UsuarioCadastro, boolean UsuarioAlterar, boolean UsuarioExcluir, boolean UsuarioEnviarEmail,
                          boolean CaixaViabilidade, boolean CaixaCancelamento, boolean CaixaAdicionar, boolean CaixaAlterar, boolean CaixaExcluir, boolean CaixaRestaurar,
                          boolean PerfilAdd, boolean PerfilAlterar, boolean PerfilExcluir)
    {
        this.id = Id;

        this.multmap = MultMap;
        this.mapnap = MapNap;

        this.usuarioCadastro = UsuarioCadastro;
        this.usuarioAlterar = UsuarioAlterar;
        this.usuarioExcluir = UsuarioExcluir;
        this.usuarioEnviarEmail = UsuarioEnviarEmail;

        this.caixaViabilidade = CaixaViabilidade;
        this.caixaCancelamento = CaixaCancelamento;
        this.caixaAdicionar = CaixaAdicionar;
        this.caixaAlterar = CaixaAlterar;
        this.caixaExcluir = CaixaExcluir;
        this.caixaRestaurar = CaixaRestaurar;

        this.perfilAdicionar = PerfilAdd;
        this.perfilAlterar = PerfilAlterar;
        this.perfilExcluir = PerfilExcluir;
    }

    public String id;

    private boolean multmap;
    private boolean mapnap;

    private boolean usuarioCadastro;
    private boolean usuarioAlterar;
    private boolean usuarioExcluir;
    private boolean usuarioEnviarEmail;

    private boolean perfilAdicionar;
    private boolean perfilAlterar;
    private boolean perfilExcluir;

    private boolean caixaViabilidade;
    private boolean caixaCancelamento;
    private boolean caixaAdicionar;
    private boolean caixaAlterar;
    private boolean caixaExcluir;
    private boolean caixaRestaurar;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isMultmap() {
        return multmap;
    }

    public void setMultmap(boolean multmap) {
        this.multmap = multmap;
    }

    public boolean isMapnap() {
        return mapnap;
    }

    public void setMapnap(boolean mapnap) {
        this.mapnap = mapnap;
    }

    public boolean isUsuarioCadastro() {
        return usuarioCadastro;
    }

    public void setUsuarioCadastro(boolean usuarioCadastro) {
        this.usuarioCadastro = usuarioCadastro;
    }

    public boolean isUsuarioAlterar() {
        return usuarioAlterar;
    }

    public void setUsuarioAlterar(boolean usuarioAlterar) {
        this.usuarioAlterar = usuarioAlterar;
    }

    public boolean isUsuarioExcluir() {
        return usuarioExcluir;
    }

    public void setUsuarioExcluir(boolean usuarioExcluir) {
        this.usuarioExcluir = usuarioExcluir;
    }

    public boolean isUsuarioEnviarEmail() {
        return usuarioEnviarEmail;
    }

    public void setUsuarioEnviarEmail(boolean usuarioEnviarEmail) {
        this.usuarioEnviarEmail = usuarioEnviarEmail;
    }

    public boolean isPerfilAdicionar() {
        return perfilAdicionar;
    }

    public void setPerfilAdicionar(boolean perfilAdicionar) {
        this.perfilAdicionar = perfilAdicionar;
    }

    public boolean isPerfilAlterar() {
        return perfilAlterar;
    }

    public void setPerfilAlterar(boolean perfilAlterar) {
        this.perfilAlterar = perfilAlterar;
    }

    public boolean isPerfilExcluir() {
        return perfilExcluir;
    }

    public void setPerfilExcluir(boolean perfilExcluir) {
        this.perfilExcluir = perfilExcluir;
    }

    public boolean isCaixaViabilidade() {
        return caixaViabilidade;
    }

    public void setCaixaViabilidade(boolean caixaViabilidade) {
        this.caixaViabilidade = caixaViabilidade;
    }

    public boolean isCaixaCancelamento() {
        return caixaCancelamento;
    }

    public void setCaixaCancelamento(boolean caixaCancelamento) {
        this.caixaCancelamento = caixaCancelamento;
    }

    public boolean isCaixaAdicionar() {
        return caixaAdicionar;
    }

    public void setCaixaAdicionar(boolean caixaAdicionar) {
        this.caixaAdicionar = caixaAdicionar;
    }

    public boolean isCaixaAlterar() {
        return caixaAlterar;
    }

    public void setCaixaAlterar(boolean caixaAlterar) {
        this.caixaAlterar = caixaAlterar;
    }

    public boolean isCaixaExcluir() {
        return caixaExcluir;
    }

    public void setCaixaExcluir(boolean caixaExcluir) {
        this.caixaExcluir = caixaExcluir;
    }

    public boolean isCaixaRestaurar() {
        return caixaRestaurar;
    }

    public void setCaixaRestaurar(boolean caixaRestaurar) {
        this.caixaRestaurar = caixaRestaurar;
    }
}
