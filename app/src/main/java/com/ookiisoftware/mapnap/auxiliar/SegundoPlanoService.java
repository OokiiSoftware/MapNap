package com.ookiisoftware.mapnap.auxiliar;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.ookiisoftware.mapnap.R;
import com.ookiisoftware.mapnap.activity.ConversaActivity;
import com.ookiisoftware.mapnap.activity.MainActivity;
import com.ookiisoftware.mapnap.modelo.Conversa;
import com.ookiisoftware.mapnap.modelo.Mensagem;
import com.ookiisoftware.mapnap.modelo.Usuario;
import com.ookiisoftware.mapnap.sqlite.SQLiteUsuario;

public class SegundoPlanoService extends Service {

    private static final String TAG = "SegundoPlanoService";

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        final String usuarioLogadoId = Import.usuario.getId(this, false);
        //Na hierarquia = root > usuarios > id_usuario_logado > conversas
        final DatabaseReference fbRefConversas = PegarReferenciaDoUsuarioNoFirebase(usuarioLogadoId);

        //Na hierarquia = root > usuarios > id_usuario_logado > conversas(...)
        ChildEventListener eventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                final StringBuilder textoNotificacao = new StringBuilder();
                final StringBuilder tituloNotificacao = new StringBuilder();

                final DatabaseReference referenceMensagem = dataSnapshot.getRef();
                //Na hierarquia = root > usuarios > id_usuario_logado > conversas > idConversa(n..) > mensagens(...) { valores.json }
                dataSnapshot.getRef().addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        Mensagem m = dataSnapshot.getValue(Mensagem.class);
                        Conversa c = new Conversa();

                        if (m == null)
                            return;
                        {

                            // Aqui vou procurar o nome do remetente nos contatos, se não tiver coloco o id
                            String nomeConversa;
                            {
                                String id_remetente = Criptografia.descriptografar(m.getId_conversa());
                                SQLiteUsuario dbContato = new SQLiteUsuario(getApplicationContext());
                                Usuario u = dbContato.get(id_remetente);
                                if (u == null){
                                    Log.e(TAG, "nome nulo");
                                    nomeConversa = id_remetente;
                                    c.setFoto("");
                                }
                                else{
                                    nomeConversa = u.getNome();
                                    c.setFoto(u.getFoto());
                                    Log.e(TAG, "Mensagem de: " + nomeConversa);
                                }
                            }

                            if (Import.SalvarMensagemNoDispositivo(getApplicationContext(), m)) {
                                c.setId(m.getId_conversa());
                                c.setNome_contato(nomeConversa);
                                c.setUltima_msg(m.getMensagem());
                                c.setData(m.getData_de_envio());

                                // Verificar quem mandou a mensagem e se a conversa está aberta com o remetente
                                {
                                    boolean mostrarNotificacao = false;
                                    boolean marcarMsgComoLido = false;

                                    if (Import.usuario.getUsuarioConversa() == null) {
                                        mostrarNotificacao = true;
                                    } else if (Import.usuario.getUsuarioConversa().getId().equals(c.getId()))
                                        marcarMsgComoLido = true;// se eu estou conversando com a pessoa então marca como lida no meu dispositivo
//
                                    c.setLido(marcarMsgComoLido ? Constantes.CONVERSA_MENSAGEM_LIDA : Constantes.CONVERSA_MENSAGEM_RECEBIDA);

                                    if (mostrarNotificacao) {
                                        String msg = Criptografia.descriptografar(m.getMensagem());
                                        tituloNotificacao.append(nomeConversa).append(",");
                                        textoNotificacao.append(nomeConversa).append(": ").append(msg).append("\n");

                                        data d = new data();
                                        d.titulo = tituloNotificacao.toString();
                                        d.texto = textoNotificacao.toString();
                                        d.id_conversa = c.getId();
                                        d.foto = c.getFoto();
                                        CriarNotificacaoDaMensagem(d);
                                    }
                                }

                                Log.e(TAG, "ID Conversa S: " + c.getId());
                                Import.SalvarConversaNoDispositivo(getApplicationContext(), c);
                                RemoverMensagemDoFirebase(referenceMensagem, m);
                            }
                        }
                    }
                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}
                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };

        fbRefConversas.addChildEventListener(eventListener);

        // START_STICKY serve para executar seu serviço até que você pare ele, é reiniciado automaticamente sempre que termina
        return START_STICKY;
    }

    private DatabaseReference PegarReferenciaDoUsuarioNoFirebase(String usuarioLogadoId) {
        //Na hierarquia = root > usuarios > id_usuario_logado > conversas
        return Import.getFirebase.getRaiz()
                .child(Constantes.Firebase.CHILD_USUARIO)
                .child(usuarioLogadoId)
                .child(Constantes.Firebase.CHILD_USUARIO_CONVERSAS);
    }
    private void RemoverMensagemDoFirebase(DatabaseReference reference, Mensagem mensagem) {
        DatabaseReference refTemp = reference;
        refTemp = refTemp.child(mensagem.getData_de_envio());
        refTemp.removeValue();
    }
    private void CriarNotificacaoDaMensagem(data d) {
        Intent intent;
        String[] tituloAux = d.titulo.split(",");
        String tituloReal;

        if(tituloAux.length == 1) {
            tituloReal = tituloAux[0];
            intent = new Intent(getApplicationContext(), ConversaActivity.class);
            intent.putExtra(Constantes.CONVERSA_CONTATO_ID, d.id_conversa);
            intent.putExtra(Constantes.CONVERSA_CONTATO_NOME, tituloReal);
            intent.putExtra(Constantes.CONVERSA_CONTATO_FOTO, d.foto);
            Log.e(TAG, "id conversa: " + d.id_conversa);
        } else {
            tituloReal = getApplicationContext().getResources().getString(R.string.app_name);
            intent = new Intent(getApplicationContext(), MainActivity.class);
        }

        Notificacao(this, intent, R.drawable.ic_notificacao, tituloReal, d.texto);
    }

    private void Notificacao(Context context, Intent intent, int icone, String titulo, String texto){
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViews notificacaoSimples = new RemoteViews(context.getPackageName(), R.layout.notification_simples);
        RemoteViews notificacaoExpandida = new RemoteViews(context.getPackageName(), R.layout.notification_expandida);

        notificacaoExpandida.setOnClickPendingIntent(R.id.notification_titulo, pendingIntent);

        notificacaoSimples.setTextViewText(R.id.notification_titulo, titulo);
        notificacaoExpandida.setTextViewText(R.id.notification_titulo, titulo);
        notificacaoSimples.setTextViewText(R.id.notification_subtitulo, texto);
        notificacaoExpandida.setTextViewText(R.id.notification_texto, texto);

        Notification notification = new NotificationCompat.Builder(context)
                .setSmallIcon(icone)
                .setCustomContentView(notificacaoSimples)
                .setCustomBigContentView(notificacaoExpandida)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setContentIntent(pendingIntent)
                .setSound(Constantes.NOTIFICACAO_SOUND_PATH)
                .setVibrate(Constantes.NOTIFICACAO_VIBRACAO)
                .setAutoCancel(true)
                .build();

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(manager != null)
            manager.notify(Constantes.NOTIFICACAO_ID, notification);

        /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(titulo, texto, NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }else{}*/
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private class data{
        String titulo;
        String texto;
        String id_conversa;
        String foto;
    }
}
