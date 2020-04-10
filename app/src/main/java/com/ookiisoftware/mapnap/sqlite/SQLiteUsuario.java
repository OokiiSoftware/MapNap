package com.ookiisoftware.mapnap.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.ookiisoftware.mapnap.auxiliar.Constantes;
import com.ookiisoftware.mapnap.auxiliar.Criptografia;
import com.ookiisoftware.mapnap.auxiliar.Import;
import com.ookiisoftware.mapnap.modelo.Usuario;

import java.util.ArrayList;

public class SQLiteUsuario extends SQLiteOpenHelper {

    private static final String TAG = "SQLiteUsuario";

    private static final int VERSAO_BANCO = Constantes.SQLITE_BANCO_DE_DADOS_VERSAO;
    private static final String TABELA_CONTATOS = "contatos";

    private final String ID = "id";
    private final String NOME = "nome";
    private final String FOTO = "foto";
    private final String PERFIL = "perfil";
    private final String TELEFONE = "telefone";
    private final String ONLINE = "online";

    public SQLiteUsuario(@Nullable Context context) {
        super(context, Import.get.SQLiteDatabaseName(context), null, VERSAO_BANCO);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
//        db.execSQL("DROP TABLE IF EXISTS " + TABELA_CONTATOS);
        String sql = "CREATE TABLE IF NOT EXISTS "+TABELA_CONTATOS+" ("
                + ID + " text primary key,"
                + NOME + " text,"
                + FOTO + " text,"
                + PERFIL + " text,"
                + TELEFONE + " text,"
                + ONLINE + " integer"
                +")";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {}

    private boolean add(Usuario c) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            onCreate(db);

            if(c.getFoto() == null)
                c.setFoto("");
            ContentValues values = new ContentValues();
            values.put(ID, c.getId());
            values.put(NOME, c.getNome());
            values.put(FOTO, c.getFoto());
            values.put(PERFIL, c.getPerfilId());
            values.put(TELEFONE, c.getTelefone());
            values.put(ONLINE, c.isOnline() ? 1 : 0);
            db.insert(TABELA_CONTATOS, null, values);
            db.close();
            Log.e(TAG, "add OK: " + c.getId());
            return true;
        } catch (Exception e){
            Log.e(TAG, "add: " + e.getMessage());
            return false;
        }
    }

    public void remove(String id) {
        id = Criptografia.criptografar(id);
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABELA_CONTATOS, ID + " = ?", new String[] { id });
        db.close();
    }

    private boolean buscarPeloNome;
    // Busca pelo id, se não encontrar nada busca pelo nome
    public Usuario get(String busca){
        busca = Criptografia.criptografar(busca);

        String indice = ID + " = ?";
        if(buscarPeloNome)
            indice = NOME + " = ?";

        try {
            SQLiteDatabase db = this.getReadableDatabase();
            String[] query = {ID, NOME, FOTO, PERFIL, TELEFONE, ONLINE};
            Cursor cursor = db.query(TABELA_CONTATOS, query, indice, new String[]{busca}, null, null, null);
            Usuario c;
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                c = new Usuario(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getInt(5));
                db.close();
                cursor.close();
                Descriptografar(c);
                buscarPeloNome = false;
                Log.e(TAG, "get OK");
                return c;
            }else {
                if(!buscarPeloNome) {
                    buscarPeloNome = true;
                    return get(busca);
                } else {
                    buscarPeloNome = false;
                    return null;
                }
            }
            /*else {
                indice = NOME + " = ?";
                cursor = db.query(TABELA_CONTATOS, query, indice, new String[]{busca}, null, null, null);
                c = null;
                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    c = new Usuario(
                            cursor.getString(0),
                            cursor.getString(1),
                            cursor.getString(2),
                            cursor.getString(3),
                            cursor.getInt(4));
                    db.close();
                    cursor.close();
                }
            }*/
        }catch (Exception e){
            Log.e(TAG, "get: " + e.getMessage());
            return null;
        }
    }

    public ArrayList<Usuario> getAll() {
            ArrayList<Usuario> contatos = new ArrayList<>();
        try{
            String[] query = {ID, NOME, FOTO, PERFIL, TELEFONE, ONLINE};

            SQLiteDatabase db = this.getWritableDatabase();
            onCreate(db);
            Cursor cursor = db.query(TABELA_CONTATOS, query, null, null, null, null, NOME);

            if (cursor.moveToFirst()) {
                do {
                    Usuario c = new Usuario();
                    c.setId(cursor.getString(0));
                    c.setNome(cursor.getString(1));
                    c.setFoto(cursor.getString(2));
                    c.setPerfilId(cursor.getString(3));
                    c.setTelefone(cursor.getString(4));
                    c.setOnline(cursor.getInt(5) == 1);
                    Descriptografar(c);
                    contatos.add(c);
                } while (cursor.moveToNext());

                db.close();
                cursor.close();
            }
            Log.e(TAG, "getAll OK");
       } catch (Exception e){
            Log.e(TAG, "getAll: " + e.getMessage());
        }
        return contatos;
    }

    public boolean update(Usuario u) {
        try {

            Usuario u2 = newUsuario(u);
            Criptografar(u2);
            // O metodo 'get' por padrão criptografa a busca pra procurar no db, então trnho que descriptografar aqui
            if (get(Criptografia.descriptografar(u2.getId())) == null) {
                return add(u2);
            }
            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(ID, u2.getId());
            values.put(NOME, u2.getNome());
            values.put(FOTO, u2.getFoto());
            values.put(PERFIL, u2.getPerfilId());
            values.put(TELEFONE, u2.getTelefone());
            values.put(ONLINE, u2.isOnline() ? 1 : 0);
            db.update(TABELA_CONTATOS, values, ID + " = ?", new String[] { u2.getId() });
            db.close();
            Log.e(TAG, "update OK: " + u2.getId());
            return true;
        } catch (Exception e){
            Log.e(TAG, "update: " + e.getMessage());
            return false;
        }
    }

    public static void Criptografar(Usuario u){
        if(u.getNome() != null) u.setNome(Criptografia.criptografar(u.getNome()));
        if(u.getFoto() != null) u.setFoto(Criptografia.criptografar(u.getFoto()));
        if(u.getPerfilId() != null) u.setPerfilId(Criptografia.criptografar(u.getPerfilId()));
        if(u.getTelefone() != null) u.setTelefone(Criptografia.criptografar(u.getTelefone()));
    }
    public static void Descriptografar(Usuario u){
        if(u.getNome() != null) u.setNome(Criptografia.descriptografar(u.getNome()));
        if(u.getFoto() != null) u.setFoto(Criptografia.descriptografar(u.getFoto()));
        if(u.getPerfilId() != null) u.setPerfilId(Criptografia.descriptografar(u.getPerfilId()));
        if(u.getTelefone() != null) u.setTelefone(Criptografia.descriptografar(u.getTelefone()));
    }
    private Usuario newUsuario(Usuario u){
        Usuario u2 = new Usuario();
        u2.setId(u.getId());
        u2.setNome(u.getNome());
        u2.setFoto(u.getFoto());
        u2.setPerfilId(u.getPerfilId());
        u2.setTelefone(u.getTelefone());
        u2.setOnline(u.isOnline());
        u2.setExcluido(u.isExcluido());
        u2.setData(u.getData());
        if (u.getSenha() != null)
            u2.setSenha(u.getSenha());
        return u2;
    }
}
