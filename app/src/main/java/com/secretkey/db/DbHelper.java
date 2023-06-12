package com.secretkey.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;

import androidx.annotation.Nullable;

import com.secretkey.R;
import com.secretkey.cifrador.Cifrador;
import com.secretkey.functions.DateInformation;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.crypto.NoSuchPaddingException;

public class DbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "secretKey.db";
    private static final String TABLE_USERS = "users";
    private static final String TABLE_SERVICES = "services";
    private static final String TABLE_TYPES = "types";
    private static final String TABLE_LOGS = "logs";
    private String redes_sociales,compras,educacion,email,bancos,otros,media,alta,baja;

    public DbHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        this.redes_sociales = context.getString(R.string.TYPE1);
        this.compras = context.getString(R.string.TYPE2);
        this.educacion = context.getString(R.string.TYPE3);
        this.email = context.getString(R.string.TYPE4);
        this.bancos = context.getString(R.string.TYPE5);
        this.otros = context.getString(R.string.TYPE6);
        this.baja = context.getString(R.string.LOW);
        this.media = context.getString(R.string.MEDIUM);
        this.alta = context.getString(R.string.HIGHT);
    }

    /**
     * Método que se ejecuta al cargar la vista
     *
     * @param sqLiteDatabase The database.
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_USERS + "(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "usuario TEXT NOT NULL," +
                "password TEXT NOT NULL," +
                "fecha_acceso TEXT NOT NULL)");

        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_TYPES + "(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nombre TEXT NOT NULL," +
                "importancia TEXT)");

        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_SERVICES + "(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id TEXT NOT NULL," +
                "nombre TEXT NOT NULL," +
                "password TEXT NOT NULL," +
                "email TEXT NOT NULL," +
                "tipo TEXT NOT NULL," +
                "fecha TEXT NOT NULL," +
                "FOREIGN KEY (tipo) REFERENCES servicio(tipo)," +
                "FOREIGN KEY (user_id) REFERENCES users(id))");

        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_LOGS + "(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id TEXT NOT NULL," +
                "fecha TEXT NOT NULL," +
                "activity TEXT NOT NULL," +
                "FOREIGN KEY (user_id) REFERENCES users(id))");
    }

    /**
     * Método que se ejecuta cuando se actualiza la vista
     *
     * @param sqLiteDatabase The database.
     * @param i              The old database version.
     * @param i1             The new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    /**
     * Método que inserta en la base de datos los tipos de servicios predeterminados
     */
    public void crearServicios() {

        String[] servicios = {redes_sociales, media, compras, media, educacion, baja, email, alta, bancos, alta, otros, media};

        SQLiteDatabase db = this.getWritableDatabase();

        String countQuery = "SELECT COUNT(*) FROM types";
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.moveToFirst();
        int rowCount = cursor.getInt(0);
        cursor.close();

        if (rowCount == 0) {

            for (int i = 0; i < servicios.length; i++) {
                ContentValues values = new ContentValues();
                values.put("nombre", servicios[i]);
                i++;
                values.put("importancia", servicios[i]);
                db.insert(TABLE_TYPES, null, values);
            }
        }

        db.close();

    }


    /**
     * Método para insertar un usuario en la base de datos
     *
     * @param user  Nombre de usuario
     * @param paswd HASH de la contraseña
     */
    public void createUser(String user, String paswd) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put("usuario", user);
        values.put("password", paswd);
        values.put("fecha_acceso", DateInformation.getDate());
        db.insert(TABLE_USERS, null, values);

        db.close();
    }

    /**
     * Método que inserta una contraseña en la base de datos
     *
     * @param user_id  Id del usuario
     * @param servicio Nombre del servicio indicado
     * @param password Contraseña elegida cifrada
     * @param email    Email o usuario indicado
     * @param tipo     Tipo de servicio elegido
     */
    public void createPassword(int user_id, String servicio, String password, String email, String tipo) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put("user_id", user_id);
        values.put("nombre", servicio);
        values.put("password", password);
        values.put("email", email);
        values.put("tipo", tipo);
        values.put("fecha", DateInformation.getDate());
        db.insert(TABLE_SERVICES, null, values);

        db.close();
    }

    /**
     * Método que cambia la contraseña maestra
     *
     * @param id    Id del usuario
     * @param paswd Nueva contraseña del usuario
     */
    public void changeMasterKey(String id, String paswd) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put("password", paswd);

        String whereClause = "id = ?";
        String[] whereArgs = new String[]{id};

        db.update(TABLE_USERS, values, whereClause, whereArgs);

        db.close();
    }

    /**
     * Método que actualiza el ultimo acceso del usuario en la base de datos
     *
     * @param id Id del usuario
     */
    public void updateLastLogin(String id) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put("fecha_acceso", DateInformation.getDate());

        String whereClause = "id = ?";
        String[] whereArgs = new String[]{id};

        db.update(TABLE_USERS, values, whereClause, whereArgs);

        db.close();
    }

    /**
     * Método que inserta un nuevo Log en la base de datos
     *
     * @param user_id   Id del usuario
     * @param actividad Informacion sobre la actividad realizada
     */
    public void insertLog(String user_id, String actividad) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put("user_id", user_id);
        values.put("fecha", DateInformation.getDate());
        values.put("activity", actividad);

        db.insert(TABLE_LOGS, null, values);

        db.close();
    }

    /**
     * Método que comprueba si un usuario existe en la base de datos
     *
     * @param user Nombre del usuario
     * @return Retorna si existe un usuario con ese nombre
     */
    public boolean userExists(String user) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {"usuario"};

        Cursor cursor =
                db.query(TABLE_USERS,
                        projection,
                        " usuario = ?",
                        new String[]{user},
                        null,
                        null,
                        null,
                        null);

        if (cursor.getCount() > 0) {
            return true;
        }

        db.close();

        return false;
    }

    /**
     * Método que obtiene todos los datos de un usuario
     *
     * @param user     Nombre de usuario
     * @param password Contraseña del usuario
     * @return Devuelve un Array con los datos del usuario
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public String[] allDataUser(String user, String password) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {"id", "usuario", "password", "fecha_acceso"};
        String[] allData = null;

        Cursor cursor =
                db.query(TABLE_USERS,
                        projection,
                        " usuario = ?",
                        new String[]{user},
                        null,
                        null,
                        null,
                        null);


        while (cursor.moveToNext()) {
            allData = new String[]{cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    Cifrador.createKey(password, password)};

            if (!Cifrador.checkLogin(cursor.getString(2), password)) {
                allData = null;
            }
        }

        db.close();

        return allData;
    }

    /**
     * Método que devuelve todos los tipos de servicio disponibles
     *
     * @return Devuelve un Array con todos los tipos de servicio
     */
    public String[] allTypeServices() {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {"nombre"};
        HashSet<String> uniqueTypes = new HashSet<>();

        Cursor cursor =
                db.query(TABLE_TYPES,
                        projection,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null);

        while (cursor.moveToNext()) {
            String type = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));
            uniqueTypes.add(type);
        }

        cursor.close();
        db.close();

        String[] allTypes = uniqueTypes.toArray(new String[0]);

        return allTypes;
    }

    /**
     * Método que devuelve todos los servicios almacenados de un usuario
     *
     * @param id Id de usuario
     * @return Devuelve un Array con todos los servicios
     */
    public String[] allServices(String id) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {"nombre"};
        HashSet<String> uniqueTypes = new HashSet<>();

        Cursor cursor =
                db.query(TABLE_SERVICES,
                        projection,
                        " user_id = ?",
                        new String[]{id},
                        null,
                        null,
                        null,
                        null);

        while (cursor.moveToNext()) {
            String type = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));
            uniqueTypes.add(type);
        }

        cursor.close();
        db.close();

        String[] allServices = uniqueTypes.toArray(new String[0]);

        return allServices;
    }

    /**
     * Método que devuelve los datos del servicio seleccionado
     *
     * @param id       Id de usuario
     * @param servicio Servicio elegido
     * @return Devuelve la informacion de ese servicio
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public String[] selectedService(String id, String servicio) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {"email", "password", "fecha"};
        String[] requiredData = null;

        Cursor cursor =
                db.query(TABLE_SERVICES,
                        projection,
                        " user_id = ? AND nombre = ?",
                        new String[]{id, servicio},
                        null,
                        null,
                        null,
                        null);


        while (cursor.moveToNext()) {
            requiredData = new String[]{cursor.getString(0), cursor.getString(1), cursor.getString(2)};
        }

        db.close();

        return requiredData;
    }

    /**
     * Método que obtiene todas las contraseñas de un usuario
     *
     * @param id Id de usuario
     * @return Devuelve los datos de esa contraseña
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public List<String[]> changeKeyPasswords(String id) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {"id", "email", "password"};
        List<String[]> passwordList = new ArrayList<>();

        Cursor cursor =
                db.query(TABLE_SERVICES,
                        projection,
                        " user_id = ?",
                        new String[]{id},
                        null,
                        null,
                        null,
                        null);


        while (cursor.moveToNext()) {
            String[] datachange = new String[]
                    {cursor.getString(0),
                            cursor.getString(1),
                            cursor.getString(2)};
            passwordList.add(datachange);
        }

        db.close();

        return passwordList;
    }

    /**
     * Método que actualiza una contraseña
     *
     * @param id       Id de la contraseña
     * @param email    Email / usuario del servicio
     * @param password Password del servicio
     */
    public void updateEncriptedData(String id, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put("email", email);
        values.put("password", password);

        String whereClause = "id = ?";
        String[] whereArgs = new String[]{id};

        db.update(TABLE_SERVICES, values, whereClause, whereArgs);

        db.close();
    }

    /**
     * Método que elimina un servicio
     *
     * @param id_usuario Id del usuario
     * @param servicio   Servicio elegido
     */
    public void deleteRowPassword(String id_usuario, String servicio) {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] whereArgs = {id_usuario, servicio};

        db.delete(TABLE_SERVICES, "user_id = ? AND nombre = ?", whereArgs);

        db.close();
    }

    /**
     * Método que actualiza un servicio
     *
     * @param servicio   Servicio seleccionado
     * @param userId      Id del usuario
     * @param email       Email / usuario indicado
     * @param newPassword Password indicado
     */
    public void updateRowPassword(String servicio, String userId, String email, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("password", newPassword);
        values.put("email", email);
        values.put("fecha", DateInformation.getDate());

        String[] whereArgs = {servicio, userId};

        db.update(TABLE_SERVICES, values, "nombre = ? AND user_id = ?", whereArgs);

        db.close();
    }

    /**
     * Método que comprueba si un servicio existe
     *
     * @param id       Id del usuario
     * @param servicio Servicio a comprobar
     * @return Devuelve si ya existe
     */
    public boolean serviceExists(String id, String servicio) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {"user_id", "nombre"};
        String selection = "user_id = ? AND nombre = ?";
        String[] selectionArgs = {id, servicio};

        Cursor cursor = db.query(TABLE_SERVICES, columns, selection, selectionArgs, null, null, null);

        boolean exists = cursor.moveToFirst();

        cursor.close();
        db.close();

        return exists;
    }

    /**
     * Método que comprueba si existe alguna contraseña guardada
     *
     * @param user_id Id del usuario
     * @return Devuelve si existe al menos una contraseña guardada
     */
    public boolean anyPassword(String user_id) {
        String query = "SELECT COUNT(*) FROM " + TABLE_SERVICES + " WHERE user_id = ?";
        String[] selectionArgs = {user_id};

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, selectionArgs);

        if (cursor.moveToFirst()) {
            int count = cursor.getInt(0);
            if (count > 0) {
                cursor.close();
                db.close();
                return true;
            } else {
                cursor.close();
                db.close();
                return false;
            }
        }
        cursor.close();
        db.close();
        return false;
    }
}
