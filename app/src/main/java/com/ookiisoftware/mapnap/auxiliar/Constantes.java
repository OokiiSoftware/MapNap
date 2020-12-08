package com.ookiisoftware.mapnap.auxiliar;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.net.Uri;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ookiisoftware.mapnap.BuildConfig;
import com.ookiisoftware.mapnap.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class Constantes {

    //===================== Conversas
    public static final String CONVERSA_CONTATO_ID = "conversa_contato_id";
    public static final String CONVERSA_CONTATO_NOME = "conversa_contato_nome";
    public static final String CONVERSA_CONTATO_EMAIL = "conversa_contato_email";
    public static final String CONVERSA_CONTATO_FOTO = "conversa_contato_foto";

    public static final int CONVERSA_MENSAGEM_LIDA = 2;
    public static final int CONVERSA_MENSAGEM_RECEBIDA = 1;
    public static final int CONVERSA_MENSAGEM_NAO_ENVIADA = 0;

    //===================== Firebase Database

    public class Firebase{
        public static final String CHILD_IDENTIFICADOR = "identificadores";
        public static final String CHILD_CLIENTES = "clientes";
        public static final String CHILD_PON = "pon";
        public static final String CHILD_DATA = "data";
        public static final String CHILD_ALERT = "isEmManutencao";

        public static final String CHILD_USUARIO = "usuarios";
        public static final String CHILD_CONTATO = "contatos";
        public static final String CHILD_ID_USUARIO = "id_usuario";
        public static final String CHILD_USUARIO_DADOS = "_dados";
        public static final String CHILD_USUARIO_CONVERSAS = "conversas";

        public static final String CHILD_PERFIL_ACESSO = "perfil_acesso";

        public static final String CHILD_CAIXAS = "caixas";
        public static final String CHILD_IS_EXCLUIDO = "isExcluido";
        public static final String CHILD_CAIXAS_ALTERADAS = "caixas_alteradas";
        public static final String CHILD_CAIXAS_EXCLUIDAS = "caixas_excluidas";
        //===================== Firebase Storage
        public static final String CHILD_PERFIL = "perfil";
    }

    //
    public static final String ACESSO_ADMINISTRADOR = "Administrador";
    public static final String ACESSO_TECNICO = "Tecnico";
    public static final String ACESSO_VENDAS = "Vendas";


    //============================== SQLite
    public static final int SQLITE_BANCO_DE_DADOS_VERSAO = 1;
    public static final String SQLITE_BANCO_DE_DADOS = "db_name" ;
    public static final int MENU_TIME_TO_HIDE = 2000;

    //================================= Eventos de deslize e cliques
    @SuppressLint("StaticFieldLeak")
    public static View item_clicado;

    public static boolean SELECIONAR_ITEM;

    public static final int LONGCLICK = 300;
    public static final int DOUBLETAP = 500;
    public static final int SWIPE_RADIO_LIMITE = 10;
    public static final int SWIPE_RANGE_LIMITE = 10;

    //================================================ Dados do usupario logado

    public static final String USUARIO_LOGADO_ID = "usuario_logado_id";
    public static final String USUARIO_LOGADO_NOME = "usuario_logado_nome";
    public static final String USUARIO_LOGADO_SENHA = "usuario_logado_senha";
    public static final String USUARIO_LOGADO_PERFIL = "usuario_logado_categoria";
    public static final String USUARIO_LOGADO_FOTO = "usuario_logado_foto";
    public static final String USUARIO_LOGADO_TELEFONE = "usuario_logado_telefone";

    //======================================= Notificações
    public static final int NOTIFICACAO_ID = 45493;
    static final long[] NOTIFICACAO_VIBRACAO = {0L, 500L};
    static final Uri NOTIFICACAO_SOUND_PATH =
            Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + BuildConfig.APPLICATION_ID + "/" + R.raw.notification_sound);
}
