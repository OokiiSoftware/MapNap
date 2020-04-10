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
import com.ookiisoftware.mapnap.modelo.Conversa;

import java.util.ArrayList;

public class SQLiteConversa extends SQLiteOpenHelper {

    private static final String TAG = "SQLiteConversa";

    private static final int VERSAO_BANCO = Constantes.SQLITE_BANCO_DE_DADOS_VERSAO;
    private static final String TABELA_CONVERSAS = "conversas";

    private final String CONTATO_ID = "id_contato";
    private final String CONTATO_NOME = "nome_contato";
    private final String CONTATO_FOTO = "foto_contato";
    private final String ULTIMA_MENSAGEM = "ultima_mensagem";
    private final String DATA = "data";
    private final String LIDO = "lido";

    public SQLiteConversa(@Nullable Context context) {
        super(context, Import.get.SQLiteDatabaseName(context), null, VERSAO_BANCO);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABELA_CONVERSAS + " ("
                + CONTATO_ID + " text,"
                + CONTATO_NOME + " text,"
                + CONTATO_FOTO + " text,"
                + ULTIMA_MENSAGEM + " text,"
                + DATA + " text,"
                + LIDO + " int"
                + ")";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {}

    private void add(Conversa conversa) {
        try{
            SQLiteDatabase db = this.getWritableDatabase();
            onCreate(db);

            ContentValues values = new ContentValues();
            values.put(CONTATO_ID, conversa.getId());
            values.put(CONTATO_NOME, conversa.getNome_contato());
            values.put(CONTATO_FOTO, conversa.getFoto());
            values.put(ULTIMA_MENSAGEM, conversa.getUltima_msg());
            values.put(DATA, conversa.getData());
            values.put(LIDO, conversa.getLido());
            db.insert(TABELA_CONVERSAS, null, values);
            db.close();
            Log.e(TAG, "add OK");
        }
        catch (Exception e){
            Log.e(TAG, "add: " + e.getMessage());
        }
    }

    public void remove(String id) {
        id = Criptografia.criptografar(id);
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABELA_CONVERSAS, CONTATO_ID + " = ?", new String[]{id});
        db.close();
    }

    public Conversa get(String busca) {
        busca = Criptografia.criptografar(busca);
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            String indice = CONTATO_ID + " = ?";
            String[] query = {CONTATO_ID, CONTATO_NOME, CONTATO_FOTO, ULTIMA_MENSAGEM, DATA, LIDO};
            Cursor cursor = db.query(TABELA_CONVERSAS, query, indice, new String[]{busca}, null, null, null);
            Conversa conversa = null;
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                conversa = new Conversa(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getInt(5));
                db.close();
                cursor.close();
            }
            if (conversa != null)
                Descriptografar(conversa);
            Log.e(TAG, "get OK");
            return conversa;
        } catch (Exception e) {
            Log.e(TAG, "get: " + e.getMessage());
            return null;
        }
    }

    public ArrayList<Conversa> getAll() {
        ArrayList<Conversa> conversas = new ArrayList<>();
        try {
            String[] query = {CONTATO_ID, CONTATO_NOME, CONTATO_FOTO, ULTIMA_MENSAGEM, DATA, LIDO};

            SQLiteDatabase db = this.getWritableDatabase();
            onCreate(db);
            Cursor cursor = db.query(TABELA_CONVERSAS, query, null, null, null, null, DATA + " DESC");

            if (cursor.moveToFirst()) {
                do {
                    Conversa conversa = new Conversa();
                    conversa.setId(cursor.getString(0));
                    conversa.setNome_contato(cursor.getString(1));
                    conversa.setFoto(cursor.getString(2));
                    conversa.setUltima_msg(cursor.getString(3));
                    conversa.setData(cursor.getString(4));
                    conversa.setLido(cursor.getInt(5));
                    Descriptografar(conversa);
                    conversas.add(conversa);
                } while (cursor.moveToNext());
                db.close();
                cursor.close();
                Log.e(TAG, "getAll OK");
            }
        } catch (Exception e) {
            Log.e(TAG, "getAll: " + e.getMessage());
        }

        return conversas;
    }

    public void update(Conversa c) {
        try{
//            Log.e(TAG, String.format("Antes - %s, %s, %s, %s, %s", c.getId(), c.getNome_contato(), c.getData(), c.getFoto(), c.getUltima_msg()));
            Criptografar(c);
//            Log.e(TAG, String.format("Depois - %s, %s, %s, %s, %s", c.getId(), c.getNome_contato(), c.getData(), c.getFoto(), c.getUltima_msg()));

            //================= Agora vou verificar se a conversa já existe
            if (get(Criptografia.descriptografar(c.getId())) == null) {
                if(DadosOK(c))
                    add(c);
                else
                    Log.e(TAG, "update: Erro nos dados da conversa");
                return;
            }
            SQLiteDatabase db = this.getWritableDatabase();

            //  Só atualiza o dado que não está com valor null
            ContentValues values = new ContentValues();
            if (c.getNome_contato() != null)
                values.put(CONTATO_NOME, c.getNome_contato());
            if (c.getFoto() != null)
                values.put(CONTATO_FOTO, c.getFoto());
            if (c.getUltima_msg() != null)
                values.put(ULTIMA_MENSAGEM, c.getUltima_msg());
            if (c.getData() != null)
                values.put(DATA, c.getData());
            if (c.getLido() >= 0)
                values.put(LIDO, c.getLido());
            db.update(TABELA_CONVERSAS, values, CONTATO_ID + " = ?", new String[]{c.getId()});
            db.close();

            Log.e(TAG, "update OK");
        }
        catch (Exception e){
            Log.e(TAG, "update: " + e.getMessage());
        }
    }

    private boolean DadosOK (Conversa c){
        if(c.getId() == null)
            return false;
        else if(c.getData() == null)
            return false;
        else if(c.getUltima_msg() == null)
            return false;
        else if(c.getFoto() == null)
            return false;
        else return c.getNome_contato() != null;
    }
    private void Criptografar(Conversa c) {
//        conversa.setId(Criptografia.criptografar(conversa.getId()));
        if (c.getNome_contato() != null)
            c.setNome_contato(Criptografia.criptografar(c.getNome_contato()));
//        conversa.setUltima_msg(Criptografia.criptografar(conversa.getUltima_msg()));
//        conversa.setData(Criptografia.criptografar(conversa.getData()));
        if (c.getFoto() != null)
            c.setFoto(Criptografia.criptografar(c.getFoto()));
    }
    private void Descriptografar(Conversa conversa) {
        conversa.setNome_contato(Criptografia.descriptografar(conversa.getNome_contato()));
        conversa.setUltima_msg(Criptografia.descriptografar(conversa.getUltima_msg()));
        conversa.setData(Criptografia.descriptografar(conversa.getData()));
        conversa.setFoto(Criptografia.descriptografar(conversa.getFoto()));
    }
}
