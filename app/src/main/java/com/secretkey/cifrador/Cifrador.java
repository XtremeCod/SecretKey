package com.secretkey.cifrador;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class Cifrador {


    public Cifrador() {
    }

    /**
     * Método que crea una key
     *
     * @param password Contraseña del usuario
     * @param salt     Se utiliza como salt la contraseña del usuario
     * @return Retorna un string con la cadena generada
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public static String createKey(String password, String salt) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException {

        //Creamos un objeto de key
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

        // Configuración de la contraseña y la sal
        int iterations = 10000;
        int keyLength = 256; // Longitud de la clave en bits

        char[] passwordChars = password.toCharArray();
        byte[] saltBytes = salt.getBytes();

        PBEKeySpec spec = new PBEKeySpec(passwordChars, saltBytes, iterations, keyLength);

        SecretKey secretKey = factory.generateSecret(spec);

        SecretKeySpec key = new SecretKeySpec(secretKey.getEncoded(), "AES");

        byte[] keyt = key.getEncoded();
        String keyS = android.util.Base64.encodeToString(keyt, Base64.DEFAULT);
        return keyS;
    }

    /**
     * Método que crea el HASH de una contraseña
     *
     * @param password Contraseña del usuario
     * @return HASH de la contraseña generado
     */
    public static String generateHASH(String password) {

        try {
            // Obtener una instancia de MessageDigest con el algoritmo deseado
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Convertir la cadena de texto a bytes y calcular el hash
            byte[] hashBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));

            // Convertir el hash a una representación hexadecimal
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Método que compara dos HASH
     *
     * @param stored_password HASH obtenido de la base de datos
     * @param user_password   HASH generado de la contraseña introducida
     * @return Retorna si coinciden
     */
    public static boolean checkLogin(String stored_password, String user_password) {
        if (stored_password.equals(generateHASH(user_password))) {
            return true;
        }
        return false;
    }

    /**
     * Método para cifrar un texto
     *
     * @param clave          Key generada para el cifrado
     * @param passwdToCipher Cadena a cifrar
     * @return Cadena cifrada en base 64
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     */
    public static String cifrar(String clave, String passwdToCipher) throws NoSuchAlgorithmException,
            NoSuchPaddingException {

        try {

            //Iniciamos el cifrador y decodificamos la key
            byte[] decodedKey = android.util.Base64.decode(clave, Base64.DEFAULT);
            SecretKey originalKey = new SecretKeySpec(Arrays.copyOf(decodedKey, 16), "AES");
            Cipher cifrador = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cifrador.init(Cipher.ENCRYPT_MODE, originalKey);

            //Generamos la cadena y la guardamos en un array de bytes
            byte[] text = passwdToCipher.getBytes("UTF8");

            //Ciframos el array
            byte[] cifrado = cifrador.doFinal(text);

            //Convertimos nuestro array en un string codificado en base64
            String s = android.util.Base64.encodeToString(cifrado, Base64.DEFAULT);
            return s;

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        } catch (BadPaddingException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Método que descifra una cadena
     *
     * @param clave          Key generada para descifrar
     * @param passwdToDecode Cadena cifrada para descifrar
     * @return Cadena descifrada
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public static String descifrar(String clave, String passwdToDecode) throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

        try {
            //Iniciamos el descifrador e inicilizamos la key
            byte[] decodedKey = android.util.Base64.decode(clave, Base64.DEFAULT);
            SecretKey originalKey = new SecretKeySpec(Arrays.copyOf(decodedKey, 16), "AES");
            Cipher descifrador = Cipher.getInstance("AES/ECB/PKCS5Padding");
            descifrador.init(Cipher.DECRYPT_MODE, originalKey);

            //Decodificamos y desciframos el contenido del fichero cifrado
            byte[] textoCifrado = descifrador.doFinal(android.util.Base64.decode(passwdToDecode, Base64.DEFAULT));

            //Creamos un string y mostramos por consola la cadena descifrada
            String s = new String(textoCifrado);
            return s;

        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        } catch (BadPaddingException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
}
