package com.secretkey.functions;

import java.security.SecureRandom;
import java.util.regex.Pattern;

public class SecurePassword {

    /**
     * Método que verifica si una contraseña es segura
     * @param password Contraseña a comprobar
     * @return Devuelve si es segura
     */
    public static boolean verificarPassword(String password) {
        // Comprobar la longitud mínima de la contraseña
        if (password.length() < 12) {
            return false;
        }

        // Comprobar si la contraseña contiene al menos un dígito
        if (!Pattern.compile("\\d").matcher(password).find()) {
            return false;
        }

        // Comprobar si la contraseña contiene al menos una letra minúscula
        if (!Pattern.compile("[a-z]").matcher(password).find()) {
            return false;
        }

        // Comprobar si la contraseña contiene al menos una letra mayúscula
        if (!Pattern.compile("[A-Z]").matcher(password).find()) {
            return false;
        }

        // Comprobar si la contraseña contiene al menos un carácter especial
        if (!Pattern.compile("[!@#$%^&*(),.?\":{}|<>]").matcher(password).find()) {
            return false;
        }

        // Si la contraseña pasa todas las comprobaciones, se considera segura
        return true;
    }

    /**
     * Método que genera una contraseña segura
     * @return Cadena con la contraseña generada
     */
    public static String generarPassword(){
        final String CARACTERES_ESPECIALES = "!@#$%^&*-+(),.?\":{}|<>";
        final String LETRAS_MINUSCULAS = "abcdefghijklmnopqrstuvwxyz";
        final String LETRAS_MAYUSCULAS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        final String DIGITOS = "0123456789";
        int longitud = 12;

        SecureRandom random = new SecureRandom();
        StringBuilder password= new StringBuilder();

        // Agregar al menos un dígito
        password.append(DIGITOS.charAt(random.nextInt(DIGITOS.length())));

        // Agregar al menos una letra minúscula
        password.append(LETRAS_MINUSCULAS.charAt(random.nextInt(LETRAS_MINUSCULAS.length())));

        // Agregar al menos una letra mayúscula
        password.append(LETRAS_MAYUSCULAS.charAt(random.nextInt(LETRAS_MAYUSCULAS.length())));

        // Agregar al menos un carácter especial
        password.append(CARACTERES_ESPECIALES.charAt(random.nextInt(CARACTERES_ESPECIALES.length())));

        // Generar el resto de la contraseña
        for (int i = 4; i < longitud; i++) {
            String caracteresPermitidos = DIGITOS + LETRAS_MINUSCULAS + LETRAS_MAYUSCULAS + CARACTERES_ESPECIALES;
            password.append(caracteresPermitidos.charAt(random.nextInt(caracteresPermitidos.length())));
        }

        // Mezclar los caracteres de la contraseña generada
        for (int i = 0; i < longitud; i++) {
            int posicion = random.nextInt(longitud);
            char temp = password.charAt(i);
            password.setCharAt(i, password.charAt(posicion));
            password.setCharAt(posicion, temp);
        }

        return password.toString();
    }
}
