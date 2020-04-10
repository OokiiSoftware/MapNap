package com.ookiisoftware.mapnap.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.ookiisoftware.mapnap.R;
import com.ookiisoftware.mapnap.auxiliar.Constantes;
import com.ookiisoftware.mapnap.auxiliar.Import;
import com.ookiisoftware.mapnap.auxiliar.Import.Log;
import com.ookiisoftware.mapnap.modelo.PerfilDeAcesso;
import com.ookiisoftware.mapnap.modelo.Usuario;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private EditText et_Login, et_Senha;
    private ProgressBar progressBar;
    private Button btn_Login;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Request_Permissao_GPS();

        et_Login = findViewById(R.id.editUsuarioLogin);
        et_Senha = findViewById(R.id.editSenhaLogin);
        btn_Login = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);

        firebaseAuth = Import.getFirebase.getFirebaseAuth();

        btn_Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!Import.get.Internet(LoginActivity.this)){
                    Mensagem(0);
                    return;
                }
                OrganizarDados();
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();

        if (AutoLogin()) {
            Log.m(TAG, "Login automatico");
            progressBar.setVisibility(View.VISIBLE);
            et_Login.setEnabled(false);
            et_Senha.setEnabled(false);
            btn_Login.setEnabled(false);
            SalvarUsuario();
        }
        else {
            NaoAutoLogin();
        }
    }

    private void NaoAutoLogin(){
        Log.m(TAG, "Fazer login");
        progressBar.setVisibility(View.INVISIBLE);
        et_Login.setEnabled(true);
        et_Senha.setEnabled(true);
        btn_Login.setEnabled(true);

        String id = Import.usuario.getId(getApplicationContext(), true);
        et_Login.setText(id, TextView.BufferType.EDITABLE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 10:
                if(!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED))
                    Mensagem(1);
                break;
            case 11:
                if(!(grantResults.length > 0 && grantResults[1] == PackageManager.PERMISSION_GRANTED))
                    Mensagem(2);
                break;
        }
    }

    private void OrganizarDados() {
        try{
            String login, senha;
            login = et_Login.getText().toString();
            senha = et_Senha.getText().toString();

            if (login.isEmpty()) {
                et_Login.setError("*");
                return;
            } else if (senha.isEmpty()) {
                et_Senha.setError("*");
                return;
            }
            Import.get.BarraDeLoad(progressBar, true);
            Usuario usuario = new Usuario();
            usuario.setId(login);
            usuario.setSenha(senha);
            LoginComEmailESenha(usuario);
        }catch (Exception e){
            Log.e(TAG, "OrganizarDados", e.getMessage());
            LimparCache();
        }
    }

    private void LoginComEmailESenha(final Usuario usuario) {
        try {
            firebaseAuth.signInWithEmailAndPassword(usuario.getId() + "@gmail.com", usuario.getSenha())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                if(task.getResult() == null) return;
                                else if(task.getResult().getUser() == null) return;

                                FirebaseUser user = task.getResult().getUser();
                                SalvarUsuario(usuario, user);
                            } else {
                                progressBar.setVisibility(View.INVISIBLE);
                                try {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                        throw Objects.requireNonNull(task.getException());
                                    }
                                    Import.toast(getApplicationContext(), "Ocorreu um erro! Tente novamente");
                                } catch (FirebaseAuthInvalidCredentialsException e) {
                                    Import.get.BarraDeLoad(progressBar, false);
                                    Import.toast(getApplicationContext(), "Usuário ou senha incorretos");
                                } catch (Exception e){
                                    Import.get.BarraDeLoad(progressBar, false);
                                    Import.toast(getApplicationContext(), "Erro: " + e.getMessage());
                                }
                            }
                        }
                    });
        }catch (Exception e){
            Log.e(TAG, "LoginComEmailESenha", e.getMessage());
            LimparCache();
        }
    }

    private void SalvarUsuario (final Usuario u, final FirebaseUser user) {
        try{
            DatabaseReference firebase;
            u.setId(user.getUid());

            // usuarios/id/dados
            firebase = Import.getFirebase.getRaiz()
                    .child(Constantes.Firebase.CHILD_USUARIO)
                    .child(u.getId());
//                    .child(Constantes.Firebase.CHILD_USUARIO_DADOS);

            final DatabaseReference finalFirebase = firebase;
            firebase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    try {
                        Usuario item = dataSnapshot.getValue(Usuario.class);
                        if (item == null)
                            return;
                        u.setPerfilId(item.getPerfilId());

                        final DatabaseReference firebase = Import.getFirebase.getRaiz()
                                .child(Constantes.Firebase.CHILD_PERFIL_ACESSO)
                                .child(u.getPerfilId());

                        firebase.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                PerfilDeAcesso perfil = dataSnapshot.getValue(PerfilDeAcesso.class);
                                try {
                                    if (perfil == null) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                            throw new Exception("Erro ao verificar perfil do usuário");
                                        } else {
                                            LimparCache();
                                            Import.toast(LoginActivity.this, "Erro ao verificar perfil do usuário");
                                            return;
                                        }
                                    }
                                    u.setPerfil(perfil);
                                    if (perfil.isMapnap()) {
                                        u.setNome((user.getDisplayName()));
                                        u.setFoto(user.getPhotoUrl() == null ? "null" : user.getPhotoUrl().toString());
                                        u.setExcluido(false);
                                        u.setOnline(true);
                                        if (user.getPhoneNumber() != null)
                                            u.setTelefone(user.getPhoneNumber().substring(3));
                                        u.setData(Import.get.Data());
                                        Import.usuario.setUsuarioLogado(getApplicationContext(), u);

                                        finalFirebase.setValue(u);
                                        IrParaPaginaPrincipal();
                                    } else {
                                        Import.toast(getApplicationContext(), "Você não tem autorização para usar o app");
                                        LimparCache();
                                    }
                                    Import.get.BarraDeLoad(progressBar, false);
                                }
                                catch (Exception e){
                                    Import.toast(getApplicationContext(), "" + e.getMessage());
                                    Import.Log.e(TAG, "SalvarUsuario 3", e.getMessage());
                                    LimparCache();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {}
                        });
                    }catch (Exception e){
                        Import.toast(getApplicationContext(), "Houve um erro ao verificar seus dados");
                        Log.e(TAG, "SalvarUsuario 2", e.getMessage());
                        LimparCache();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {}
            });
        }catch (Exception e){
            Log.e(TAG, "SalvarUsuario 1", e.getMessage());
            LimparCache();
        }
        Import.get.BarraDeLoad(progressBar, false);
    }

    private void SalvarUsuario(){
        try {
            String perfilId = Import.usuario.getPerfilId(getApplicationContext());

            DatabaseReference firebase = Import.getFirebase.getRaiz()
                    .child(Constantes.Firebase.CHILD_PERFIL_ACESSO)
                    .child(perfilId);

            firebase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    PerfilDeAcesso perfil = dataSnapshot.getValue(PerfilDeAcesso.class);
                    try {
                        if (perfil == null) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                throw new Exception("Erro ao verificar perfil do usuário");
                            } else {
                                LimparCache();
                                Import.toast(LoginActivity.this, "Erro ao verificar perfil do usuário");
                                return;
                            }
                        }
                        Usuario u = new Usuario();
                        u.setPerfil(perfil);
                        if (perfil.isMapnap()) {
                            Import.usuario.setUsuarioLogado(getApplicationContext(), u);
                            IrParaPaginaPrincipal();
                        } else {
                            Import.toast(getApplicationContext(), "Você não tem autorização para usar o app");
                            LimparCache();
                            NaoAutoLogin();
                        }
                        Import.get.BarraDeLoad(progressBar, false);
                    }
                    catch (Exception e){
                        Import.toast(getApplicationContext(), "" + e.getMessage());
                        Import.Log.e(TAG, "SalvarUsuario","AutoLogin 2", e.getMessage());
                        LimparCache();
                        NaoAutoLogin();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {}
            });
        } catch (Exception e){
            Import.Log.e(TAG, "SalvarUsuario", "AutoLogin 1", e.getMessage());
            LimparCache();
            NaoAutoLogin();
        }
    }

    private void IrParaPaginaPrincipal() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
//        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
        finish();
    }

    private void Request_Permissao_GPS() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET
                    }, 10);
                }
            }
        }
    }

    private boolean AutoLogin(){
        return Import.getFirebase.getFirebaseAuth().getCurrentUser() != null;
    }

    private void LimparCache(){
        Import.getFirebase.getFirebaseAuth().signOut();
    }

    private void Mensagem (int indide) {
        switch (indide){
            case 0:
                Toast.makeText(LoginActivity.this, "Verifique sua conexção com a Internet", Toast.LENGTH_LONG).show();
            case 1:
                Toast.makeText(LoginActivity.this, "Permissão de acesso ao GPS recusada", Toast.LENGTH_LONG).show();
            case 2:
                Toast.makeText(LoginActivity.this, "Permissão de acesso ao storange recusadaa", Toast.LENGTH_LONG).show();
        }
    }

    /*private void Request_Permissao_External_Estorage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE}, 11);
            }
        }
    }*/
}
