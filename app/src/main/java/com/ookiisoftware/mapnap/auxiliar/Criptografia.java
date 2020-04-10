package com.ookiisoftware.mapnap.auxiliar;

import android.util.Base64;

public class Criptografia {

    public static String criptografar(String texto){
        return Base64.encodeToString(texto.getBytes(), Base64.DEFAULT).replaceAll("(\\n|\\r)", (String) "");
    }

    public static String descriptografar(String textoCriptografado){
        return new String(Base64.decode(textoCriptografado, Base64.DEFAULT));
    }
}
