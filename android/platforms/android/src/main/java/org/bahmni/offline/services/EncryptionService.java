package org.bahmni.offline.services;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionService {

    SharedPreferences sharedPref;

    public EncryptionService(Context context) {
        this.sharedPref = context.getSharedPreferences("EncryptionKey", Context.MODE_PRIVATE);
    }

    public String generateKey() {
        String existingKey = sharedPref.getString("key", null);
        if(existingKey != null)
            return getKey();

        final int outputKeyLength = 256;
        SecureRandom secureRandom = new SecureRandom();
        KeyGenerator keyGenerator = null;
        try {
            keyGenerator = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        keyGenerator.init(outputKeyLength, secureRandom);
        SecretKey key = keyGenerator.generateKey();
        storeKey(key);
        return getKey();
    }

    public void storeKey(SecretKey key){
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("key", Base64.encodeToString(key.getEncoded(), Base64.NO_WRAP));
        editor.commit();
    }

    public String getKey(){
        return sharedPref.getString("key", null);
    }
}
