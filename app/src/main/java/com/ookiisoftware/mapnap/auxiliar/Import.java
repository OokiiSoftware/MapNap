package com.ookiisoftware.mapnap.auxiliar;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ookiisoftware.mapnap.modelo.Caixa;
import com.ookiisoftware.mapnap.modelo.Conversa;
import com.ookiisoftware.mapnap.modelo.Mensagem;
import com.ookiisoftware.mapnap.modelo.PerfilDeAcesso;
import com.ookiisoftware.mapnap.modelo.Usuario;
import com.ookiisoftware.mapnap.sqlite.SQLiteUsuario;
import com.ookiisoftware.mapnap.sqlite.SQLiteConversa;
import com.ookiisoftware.mapnap.sqlite.SQLiteMensagem;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Random;

public class Import  {
    private static final String TAG = "Import";

    public static void toast(Context context, String msg){
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    public static void snakeBar(View view, String texto){
        Snackbar.make(view, texto, Snackbar.LENGTH_LONG)
        .setAction("Fechar", null).show();
    }

    public static String splitData(String data){//2019-11-29 06:25:01
        String t = data.substring(data.indexOf(" "));// yyyy-MM-dd HH:mm:ss:SSS
        t = t.substring(0, t.length()-3);// HH:mm:ss:SSS
        return t;// HH:mm
    }

    public static void cancelNotification(Context ctx, int notifyId) {
        NotificationManager manager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if(manager != null)
            manager.cancel(notifyId);
    }

    public static boolean SalvarMensagemNoDispositivo(Context context, Mensagem mensagem) {
        try {
            SQLiteMensagem db = new SQLiteMensagem(context);
            db.update(mensagem);
            return true;
        } catch (Exception e){
            return false;
        }
    }

    public static boolean SalvarConversaNoDispositivo(Context context, Conversa conversa) {
        try {
            SQLiteConversa db = new SQLiteConversa(context);
            db.update(conversa);
            return true;
        } catch (Exception e){
            return false;
        }
    }

    public static  class get {

        public static Locale locale = new Locale("pt", "BR");


        public static void BarraDeLoad(ProgressBar progressBar, boolean visible){
            progressBar.setVisibility(visible ? View.VISIBLE : View.GONE);
        }

        public static boolean Internet(Context contexto){
            final ConnectivityManager cm = (ConnectivityManager) contexto.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                if (Build.VERSION.SDK_INT < 23) {
                    final NetworkInfo ni = cm.getActiveNetworkInfo();
                    if (ni != null) {
                        return (ni.isConnected() && (ni.getType() == ConnectivityManager.TYPE_WIFI || ni.getType() == ConnectivityManager.TYPE_MOBILE));
                    }
                } else {
                    final Network n = cm.getActiveNetwork();
                    if (n != null) {
                        final NetworkCapabilities nc = cm.getNetworkCapabilities(n);
                        return (nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI));
                    }
                }
            }
            return false;
        }

        public static String RandomString(int size) {
            StringBuilder builder = new StringBuilder();
            Random random = new Random();
            char ch;
            for (int i = 0; i < size; i++)
            {
                ch = (char) Math.floor(26 * random.nextDouble() + 65);
                builder.append(ch);
            }
            return builder.toString();
        }

        public static String Data(){
            Calendar c = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", locale);
            return dateFormat.format(c.getTime());
        }

        public static Caixa NewCaixa(Caixa c){
            Caixa c2 = new Caixa();
            c2.setId(c.getId());
            c2.setPon(c.getPon());
            c2.setNome(c.getNome());
            c2.setData(c.getData());
            c2.setPortas(c.getPortas());
            c2.setStatus(c.getStatus());
            c2.setExcluido(c.isExcluido());
            c2.setEndereco(c.getEndereco());
            c2.setLatitude(c.getLatitude());
            c2.setLongitude(c.getLongitude());
            c2.setId_usuario(c.getId_usuario());
            c2.setEm_manutencao(c.isEm_manutencao());
            c2.setClientes(new LinkedList<String>());
            if(c.getClientes() != null)
                for (String cliente : c.getClientes())
                    c2.getClientes().add(cliente);
            if(c.getNovo_id() != null)
                c2.setNovo_id(c.getNovo_id());
            if(c.getMotivo() != null)
                c2.setMotivo(c.getMotivo());
            return  c2;
        }

        public static String SQLiteDatabaseName(Context context){
            SharedPreferences pref = context.getSharedPreferences("info", Context.MODE_PRIVATE);
            return pref.getString(Constantes.SQLITE_BANCO_DE_DADOS, usuario.getId(context, false));
        }
    }

    public static class Log{
        public static void e(String tag, String titulo, String texto){
            android.util.Log.e(tag, titulo + ": " + texto);
        }
        public static void e(String tag, String titulo, String texto, String valor){
            android.util.Log.e(tag, titulo + ": " + texto + ": " + valor);
        }
        public static void m(String tag, String titulo){
            android.util.Log.e(tag, titulo);
        }
        public static void m(String tag, String titulo, String texto){
            android.util.Log.e(tag, titulo + ": " + texto);
        }
        public static void m(String tag, String titulo, String texto, String valor){
            android.util.Log.e(tag, titulo + ": " + texto + ": " + valor);
        }
    }

    public static class getFirebase{
        private static FirebaseAuth firebaseAuth;
        private static DatabaseReference firebase;

        private static StorageReference firebaseStorage;

        public static DatabaseReference getRaiz() {
            if(firebase == null)
                firebase = FirebaseDatabase.getInstance().getReference();
            return firebase;
        }

        public static FirebaseAuth getFirebaseAuth() {
            if(firebaseAuth == null)
                firebaseAuth = FirebaseAuth.getInstance();
            return firebaseAuth;
        }

        public static StorageReference getFirebaseStorage() {
            if(firebaseStorage == null)
                firebaseStorage = FirebaseStorage.getInstance().getReference(Constantes.Firebase.CHILD_USUARIO + "/" + Constantes.Firebase.CHILD_PERFIL);
            return firebaseStorage;
        }
    }

    public static class usuario {
        private static Usuario uLogado;
        private static Usuario usuarioConversa;

        public static Usuario getUsuarioConversa() {
            return usuarioConversa;
        }

        public static void setUsuarioConversa(Usuario usuarioConversa) {
            usuario.usuarioConversa = usuarioConversa;
        }

        public static void setUsuarioLogado(Context context, Usuario u) {
            try {
                if(uLogado == null)
                    uLogado = new Usuario();

                if(u.getId() != null) uLogado.setId(u.getId());
                if(u.getNome() != null) uLogado.setNome(u.getNome());
                if(u.getFoto() != null) uLogado.setFoto(u.getFoto());
                if(u.getSenha() != null) uLogado.setSenha(u.getSenha());
                if(u.getPerfil() != null) uLogado.setPerfil(u.getPerfil());
                if(u.getPerfilId() != null) uLogado.setPerfilId(u.getPerfilId());
                if(u.getTelefone() != null) uLogado.setTelefone(u.getTelefone());

                SQLiteUsuario.Criptografar(uLogado);

                SharedPreferences pref = context.getSharedPreferences("info", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();

                if(uLogado.getId() != null) editor.putString(Constantes.USUARIO_LOGADO_ID, uLogado.getId());
                if(uLogado.getNome() != null) editor.putString(Constantes.USUARIO_LOGADO_NOME, uLogado.getNome());
                if(uLogado.getFoto() != null) editor.putString(Constantes.USUARIO_LOGADO_FOTO, uLogado.getFoto());
                if(uLogado.getPerfilId() != null) editor.putString(Constantes.USUARIO_LOGADO_PERFIL, uLogado.getPerfilId());
                if(uLogado.getTelefone() != null) editor.putString(Constantes.USUARIO_LOGADO_TELEFONE, uLogado.getTelefone());
                if(uLogado.getSenha() != null) {
                    String senha = Criptografia.criptografar(uLogado.getSenha());
                    editor.putString(Constantes.USUARIO_LOGADO_SENHA, senha);
                }

                if(u.getId() != null) editor.putString(Constantes.SQLITE_BANCO_DE_DADOS, u.getId());
                editor.apply();
            }catch (Exception e){
                Log.e(TAG, "setUsuarioLogado", e.getMessage());
            }
        }
        //====================================================
        public static String getId(Context context, boolean decript){
            SharedPreferences pref = context.getSharedPreferences("info", Context.MODE_PRIVATE);
            String value = pref.getString(Constantes.USUARIO_LOGADO_ID, "");
            if(decript)
                value = Criptografia.descriptografar(value);
            return value;
        }
        public static String getNome(Context context){
            SharedPreferences pref = context.getSharedPreferences("info", Context.MODE_PRIVATE);
            String value = pref.getString(Constantes.USUARIO_LOGADO_NOME, "");
            return Criptografia.descriptografar(value);
        }
        public static String getSenha(Context context){
            SharedPreferences pref = context.getSharedPreferences("info", Context.MODE_PRIVATE);
            String value = pref.getString(Constantes.USUARIO_LOGADO_SENHA, "");
            return Criptografia.descriptografar(value);
        }
        public static String getFoto(Context context){
            SharedPreferences pref = context.getSharedPreferences("info", Context.MODE_PRIVATE);
            String value = pref.getString(Constantes.USUARIO_LOGADO_FOTO, "");
            return Criptografia.descriptografar(value);
        }
        public static String getPerfilId(Context context){
            SharedPreferences pref = context.getSharedPreferences("info", Context.MODE_PRIVATE);
            String value = pref.getString(Constantes.USUARIO_LOGADO_PERFIL, "");
            return Criptografia.descriptografar(value);
        }
        public static PerfilDeAcesso getPerfil(){
            if(uLogado != null)
                return uLogado.getPerfil();
            return null;
        }
    }
}
