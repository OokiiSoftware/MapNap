package com.ookiisoftware.mapnap.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.ookiisoftware.mapnap.R;
import com.ookiisoftware.mapnap.auxiliar.Constantes;
import com.ookiisoftware.mapnap.auxiliar.Criptografia;
import com.ookiisoftware.mapnap.auxiliar.Import;
import com.ookiisoftware.mapnap.modelo.Conversa;
import com.ookiisoftware.mapnap.modelo.Mensagem;
import com.ookiisoftware.mapnap.modelo.Usuario;
import com.ookiisoftware.mapnap.sqlite.SQLiteConversa;
import com.ookiisoftware.mapnap.sqlite.SQLiteMensagem;

import java.util.ArrayList;

public class ConversaActivity extends AppCompatActivity {

    private static final String TAG = "ConversaActivity";
    private RecyclerView recyclerView;
    private EditText txt_caixa_de_texto;
    //==================================== Firebase
    private DatabaseReference fbRefUserDestino;
    private DatabaseReference fbRefMensagens;

    private ValueEventListener valueEventListenerMensagens;
    //======================= Dados dos usuarios da conversa
    private Usuario usuarioDestino = new Usuario();
    private String usuarioLogadoId;
    //======================================================
    private SingleItemMensagemAdapter adapter;
    private ArrayList<Mensagem> mensagems = new ArrayList<>();

    private boolean ERRO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversa);
        try {
            Init();
        }catch (Exception e){
            ERRO = true;
            Alert("Ocorreu um erro ao abrir esta conversa");
            Log.e(TAG, "onCreate " + e.getMessage());
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        if (ERRO)
            return;
        Import.cancelNotification(getApplication(), Constantes.NOTIFICACAO_ID);
        fbRefMensagens.addValueEventListener(valueEventListenerMensagens);
        Import.usuario.setUsuarioConversa(usuarioDestino);
        adapter.notifyDataSetChanged();
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (ERRO)
            return;
        fbRefMensagens.removeEventListener(valueEventListenerMensagens);
        Import.usuario.setUsuarioConversa(null);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }


    private void Init() {
        ImageButton btn_enviar;
        //=================Elementos do Layout
        Toolbar toolbar;
        {
            txt_caixa_de_texto = findViewById(R.id.conversa_caixa_de_texto);
            recyclerView = findViewById(R.id.conversa_recyclerView);
            btn_enviar = findViewById(R.id.conversa_tbn_enviar);
            toolbar = findViewById(R.id.conversa_toolbar);
        }// pegar elementos do layout pelo ID

        usuarioLogadoId = Import.usuario.getId(this, false);

        Bundle bundle = getIntent().getExtras();
        if(bundle == null){
            onBackPressed();
            return;
        }
        usuarioDestino.setId(bundle.getString(Constantes.CONVERSA_CONTATO_ID));
        usuarioDestino.setNome(bundle.getString(Constantes.CONVERSA_CONTATO_NOME));
        usuarioDestino.setFoto(bundle.getString(Constantes.CONVERSA_CONTATO_FOTO));

        // Atualizar o banco de dados ao entrar na conversa, coloca a msg como lida
        {
            SQLiteConversa db = new SQLiteConversa(this);
            Conversa conversa = new Conversa();
            conversa.setId(usuarioDestino.getId());
            conversa.setLido(Constantes.CONVERSA_MENSAGEM_LIDA);
            db.update(conversa);
        }

        /*
         * ID do usuário logado     <-- OK
         * ID do contato da conversa    <-- OK
         */
        PegarReferenciaDaConversa();

        /*
         * NOME do contato da conversa    <-- OK
         */
        {
            toolbar.setTitle(usuarioDestino.getNome());
            toolbar.setNavigationIcon(R.drawable.ic_seta_esquerda);
            setSupportActionBar(toolbar);
        }// toolbar

        /*
         * ID do contato da conversa    <-- OK
         */
        {
            SQLiteMensagem dbMensagem = new SQLiteMensagem(this);
            mensagems.addAll(dbMensagem.getAll(usuarioDestino.getId()));
        }// Pegar dados da conversa salva no dispositivo

        /*
         * LISTA_DE_MENSAGENS   <-- OK
         */
        {
            adapter = new SingleItemMensagemAdapter(mensagems);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(adapter);
            recyclerView.scrollToPosition(adapter.getItemCount()-1);
        }// adaptar recyclerView

        /*
         * ID do contato da conversa    <-- OK
         * FbRefUserLogado
         * ADAPTER
         */
        // evento pra receber mensagens do firebase
        valueEventListenerMensagens = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Mensagem mensagem = data.getValue(Mensagem.class);
                    if (mensagem != null){
                        SQLiteMensagem.Descriptografar(mensagem);
                        mensagems.add(mensagem);
                    }
                }
                adapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(adapter.getItemCount()-1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };

        {
            btn_enviar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String texto = txt_caixa_de_texto.getText().toString();
                    if(texto.trim().isEmpty())
                        return;
                    OgranizarDados(texto);
                    txt_caixa_de_texto.setText("");
                    recyclerView.scrollToPosition(adapter.getItemCount()-1);
                }
            });
        }// botão enviar msg
    }

    /*  Dados que preciso
    *
    * ID do usuário logado    <-- OK
    * ID do contato da conversa    <-- OK
    * */
    private void PegarReferenciaDaConversa() {
        //Na hierarquia root > usuarios > id_usuario_logado > conversas > id_usuario_destino
        fbRefMensagens = Import.getFirebase.getRaiz()
                .child(Constantes.Firebase.CHILD_USUARIO_CONVERSAS)
                .child(usuarioLogadoId)
                .child(usuarioDestino.getId());
        //Na hierarquia root > usuarios > id_usuario_destino > conversas > id_usuario_logado
        fbRefUserDestino = Import.getFirebase.getRaiz()
                .child(Constantes.Firebase.CHILD_USUARIO_CONVERSAS)
                .child(usuarioDestino.getId())
                .child(usuarioLogadoId);

//        fbRefMensagens = fbRefUserLogado;
    }

    private void OgranizarDados(String texto) {
        String data_de_envio = Import.get.Data();

        // Todos os dados devem estar descriptografados, na hora de salvar que criptografa
        Mensagem mensagem = new Mensagem();
        mensagem.setId_remetente(usuarioLogadoId);
        mensagem.setId_conversa(usuarioDestino.getId());
        mensagem.setData_de_envio(data_de_envio);
        mensagem.setMensagem(texto);
        mensagem.setStatus(Constantes.CONVERSA_MENSAGEM_NAO_ENVIADA);
        mensagem.setArquivo(0);

        if(Import.SalvarMensagemNoDispositivo(this, mensagem)) {// aqui salva a msg
            mensagems.add(mensagem);
            adapter.notifyDataSetChanged();

            Conversa c = new Conversa();
            c.setId(mensagem.getId_conversa());// C
            c.setNome_contato(usuarioDestino.getNome());
            c.setUltima_msg(mensagem.getMensagem());// C
            c.setData(mensagem.getData_de_envio());
            c.setFoto(usuarioDestino.getFoto());
            c.setLido(Constantes.CONVERSA_MENSAGEM_LIDA);

            Log.e(TAG, "ID Conversa M: " + c.getId());
            Import.SalvarConversaNoDispositivo(this, c);
            mensagem.setId_conversa(usuarioLogadoId);
            SQLiteMensagem.Criptografar(mensagem);
            if(SalvarDadosNoFirebaseUsuarioDestino(fbRefUserDestino, mensagem)){
                mensagem.setStatus(Constantes.CONVERSA_MENSAGEM_RECEBIDA);
                mensagem.setId_conversa(usuarioDestino.getId());
                if(Import.SalvarMensagemNoDispositivo(this, mensagem)) {// aqui atualiza a msg (se foi enviada ou não)
                    SQLiteMensagem.Descriptografar(mensagem);
                    mensagems.set(mensagems.size()-1, mensagem);
                    adapter.notifyDataSetChanged();
                }
            } else {
                Alert("Não foi possível enviar esta mensagem");
            }
        } else
            Alert("Não foi possível enviar esta mensagem!");
    }

    private void Alert(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    private boolean SalvarDadosNoFirebaseUsuarioDestino(DatabaseReference reference, Mensagem mensagem){
        try {
            DatabaseReference referenceTemp = reference.child(mensagem.getData_de_envio());
            referenceTemp.setValue(mensagem);
            Log.e(TAG, "Salvar Dados No Firebase Usuario Destino: OK");
            return true;
        }catch (Exception e){
            Log.e(TAG, "SalvarDadosNoFirebaseUsuarioDestino: " + e.getMessage());
            return false;
        }
    }
    /*
     *
     * *
     * *
     * *
     * *
     * *
     * *
     * *
     * *
     * *
     * *
     *
     */
    private class SingleItemMensagemAdapter extends RecyclerView.Adapter<ViewHolder> {

        ArrayList<Mensagem> mensagems;

        SingleItemMensagemAdapter(ArrayList<Mensagem> mensagems) {
            this.mensagems = mensagems;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(ConversaActivity.this).inflate(R.layout.item_conversa_mensagem, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
                holder.mensagem.setText(mensagems.get(position).getMensagem());
                String data = mensagems.get(position).getData_de_envio();
            Log.e(TAG,"Data: A- "+ data);
                holder.data.setText(Import.splitData(data));
                RelativeLayout.LayoutParams paramsGeral = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                RelativeLayout.LayoutParams paramsData = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

                paramsData.addRule(RelativeLayout.BELOW, R.id.item_conversa_layout);

                if (Criptografia.criptografar(mensagems.get(position).getId_remetente()).equals(usuarioLogadoId)) {
//                    holder.mensagem.setBackground(getDrawable(R.drawable.bg_circulo_conversa_mensagem_direita));
                    holder.mensagem.setBackground(getResources().getDrawable(R.drawable.bg_circulo_conversa_mensagem_direita));
                    paramsGeral.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    paramsGeral.addRule(RelativeLayout.RIGHT_OF, R.id.item_conversa_layout_geral);
                    paramsData.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    paramsData.addRule(RelativeLayout.RIGHT_OF, R.id.item_conversa_layout_geral);
                    switch (mensagems.get(position).getStatus()){
                        case Constantes.CONVERSA_MENSAGEM_NAO_ENVIADA:{
                            holder.data.setTextColor(getResources().getColor(R.color.vermelho));
                            break;
                        }
                        case Constantes.CONVERSA_MENSAGEM_RECEBIDA:{
                            holder.data.setTextColor(getResources().getColor(R.color.amareloDark));
                            break;
                        }
                        case Constantes.CONVERSA_MENSAGEM_LIDA:{
                            holder.data.setTextColor(getResources().getColor(R.color.verde));
                            break;
                        }
                    }
                }
                else {
//                    holder.mensagem.setBackground(getDrawable(R.drawable.bg_circulo_conversa_mensagem_esquerda));
                    holder.mensagem.setBackground(getResources().getDrawable(R.drawable.bg_circulo_conversa_mensagem_esquerda));
                    paramsGeral.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    paramsGeral.addRule(RelativeLayout.LEFT_OF, R.id.item_conversa_layout_geral);
                    paramsData.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    paramsData.addRule(RelativeLayout.LEFT_OF, R.id.item_conversa_layout_geral);
                    holder.data.setTextColor(getResources().getColor(R.color.verde));
                }
                holder.relativeLayout.setLayoutParams(paramsGeral);
                holder.data.setLayoutParams(paramsData);
        }

        @Override
        public int getItemCount() {
            return mensagems != null ? mensagems.size() : 0;
        }
    }
    /*
     *
     * *
     * *
     * *
     * *
     * *
     * *
     * *
     * *
     * *
     * *
     *
     */
    private class ViewHolder extends RecyclerView.ViewHolder {
        TextView mensagem, data;
        RelativeLayout relativeLayout;

        @SuppressLint("ClickableViewAccessibility")
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            relativeLayout = itemView.findViewById(R.id.item_conversa_layout);
            mensagem = itemView.findViewById(R.id.item_conversa_mensagem);
            data = itemView.findViewById(R.id.item_conversa_data);
        }

    }
}
