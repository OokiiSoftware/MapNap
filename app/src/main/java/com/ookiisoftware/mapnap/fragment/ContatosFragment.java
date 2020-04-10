package com.ookiisoftware.mapnap.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.rtoshiro.util.format.SimpleMaskFormatter;
import com.github.rtoshiro.util.format.text.MaskTextWatcher;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.ookiisoftware.mapnap.R;
import com.ookiisoftware.mapnap.activity.ConversaActivity;
import com.ookiisoftware.mapnap.auxiliar.Constantes;
import com.ookiisoftware.mapnap.auxiliar.Import;
import com.ookiisoftware.mapnap.modelo.PerfilDeAcesso;
import com.ookiisoftware.mapnap.modelo.Usuario;
import com.ookiisoftware.mapnap.sqlite.SQLiteUsuario;

import java.util.ArrayList;

public class ContatosFragment extends Fragment {

    private static final String TAG = "ContatosFragment";

    //Variaveis
    private SQLiteUsuario db;
    private SingleItemContatoAdapter adapter;
    private DatabaseReference firebase;
    private ChildEventListener eventListener;
    private ArrayList<Usuario> contatos = new ArrayList<>();

    public ContatosFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_batepapo, container, false);
        Init(view);
        return view;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        firebase.removeEventListener(eventListener);
    }


    private void Init(View view) {
        // Elementos do Layout
        RecyclerView recyclerView = view.findViewById(R.id.batepapo_recyclerview);

        db = new SQLiteUsuario(getContext());
        contatos.clear();
        contatos.addAll(db.getAll());

        //Na hierarquia = root > usuarios >
        firebase = Import.getFirebase.getRaiz().child(Constantes.Firebase.CHILD_USUARIO);

        /*
         * Lista de Contatos     <-- OK
         */
        {
            adapter = new SingleItemContatoAdapter(contatos);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(adapter);
            recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        }// Adaptar RecyclerView

        // Sincronizar contatos local com o firebase
        eventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                atualizarContato(dataSnapshot);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                atualizarContato(dataSnapshot);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };

        firebase.addChildEventListener(eventListener);
    }

    private void atualizarContato(DataSnapshot dataSnapshot){
        final String key = dataSnapshot.getKey();
        if(key != null)
            // Se este for o meu ID então não quero add no SQLite
            if(key.equals(Import.usuario.getId(getContext(), false))) {
                Log.e(TAG, "meu ID: " + key);
                return;
            }
        // root > usuarios > idFuncionario > dados { valores.json }
        dataSnapshot
                .getRef()
                .child(Constantes.Firebase.CHILD_USUARIO_DADOS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final Usuario u = dataSnapshot.getValue(Usuario.class);
                if (u == null)
                    return;
                Import.getFirebase.getRaiz()
                        .child(Constantes.Firebase.CHILD_PERFIL_ACESSO)
                        .child(u.getPerfilId())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                PerfilDeAcesso perfil = dataSnapshot.getValue(PerfilDeAcesso.class);
                                if(perfil == null)
                                    return;
                                u.setId(key);
                                if(!perfil.isMapnap())
                                    return;
                                u.setPerfil(perfil);
                                db.update(u);

                                if(Find(u) == null)
                                    contatos.add(u);
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {}
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }
    private Usuario Find(Usuario u){
        for (Usuario u2 : contatos)
            if (u2.getId().equals(u.getId())){
                Log.e(TAG, "ID Atualizado: " + u.getId());
                contatos.set(contatos.indexOf(u2), u);
                return u;
            }
        Log.e(TAG, "ID Novo: " + u.getId());
        return null;
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
    private class SingleItemContatoAdapter extends RecyclerView.Adapter<ViewHolder> {

        private ArrayList<Usuario> contatos;
        private SimpleMaskFormatter maskFormatter = new SimpleMaskFormatter("(NN) NNNNN-NNNN");

        SingleItemContatoAdapter(ArrayList<Usuario> contatos) {
            this.contatos = contatos;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.item_batepapo, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
            if (contatos.get(position).getFoto() != null)
                Glide.with(getActivity()).load(contatos.get(position).getFoto()).into(holder.img_foto_contato);
            holder.nome_contato.setText(contatos.get(position).getNome());
            MaskTextWatcher m = new MaskTextWatcher(holder.telefone_contato, maskFormatter);
            holder.telefone_contato.addTextChangedListener(m);
            holder.telefone_contato.setText(contatos.get(position).getTelefone());
            {
                holder.btn_click.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), ConversaActivity.class);
                        intent.putExtra(Constantes.CONVERSA_CONTATO_ID, contatos.get(position).getId());
                        intent.putExtra(Constantes.CONVERSA_CONTATO_NOME, contatos.get(position).getNome());
                        intent.putExtra(Constantes.CONVERSA_CONTATO_FOTO, contatos.get(position).getFoto());
                        startActivity(intent);
                    }
                });
                holder.btn_click.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        Log.e(TAG, "Long: " + position);
                        return false;
                    }
                });
            }// ClickListener();
        }

        @Override
        public int getItemCount() {
            return contatos != null ? contatos.size() : 0;
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
        CoordinatorLayout btn_click;
        ImageView img_foto_contato, ic_msg_lida;
        TextView nome_contato, telefone_contato, data;

        @SuppressLint("ClickableViewAccessibility")
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            btn_click = itemView.findViewById(R.id.item_batepapo_btn_principal);
            img_foto_contato = itemView.findViewById(R.id.item_batepapo_foto);
            nome_contato = itemView.findViewById(R.id.item_batepapo_titulo);
            telefone_contato = itemView.findViewById(R.id.item_batepapo_subtitulo);

            ic_msg_lida = itemView.findViewById(R.id.item_batepapo_ic_msg_lida);
            data = itemView.findViewById(R.id.item_batepapo_data);
            ic_msg_lida.setVisibility(View.INVISIBLE);
            data.setVisibility(View.INVISIBLE);
        }

    }
}
