package com.ispw.tryeshifts.dao;

import com.ispw.tryeshifts.excpetion.FetchDataException;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

public class SecurityUtils{

    private SecurityUtils(){
        throw new IllegalStateException("Utility class");
    }
    public static String hashPassword(String password) throws FetchDataException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(password.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new FetchDataException("Errore critico durante l'hashing");
        }
    }
}
