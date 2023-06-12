package com.secretkey.functions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.DateFormat;

public class DateInformation {

    /**
     * Método que devuelve la hora y fecha actual
     * @return Cadena con la hora y fecha actual GTM
     */
    public static String getDate(){

        DateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm:ss z");

        String date = dateFormat.format(new Date());
        return date;
    }
}
